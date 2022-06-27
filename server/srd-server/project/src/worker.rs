use chan::{cbchan_bounded, cbchan_unbounded, CbchanSender, CbchanReceiverStream};

use crossbeam_channel as cbchan;

use futures::future::{self, Either, Future};
use futures::stream::Stream;

use listener::OptionalTcpListenerStream;

use service::SrdHttpService;

use hyper::Chunk;
use hyper::server::Http;

use native_tls::{self, TlsAcceptor};

use std;
use std::cell::RefCell;
use std::mem;
use std::net::{self, SocketAddr};
use std::panic::{AssertUnwindSafe, catch_unwind};
use std::rc::Rc;
use std::sync::{self, Arc, Mutex};
use std::thread::{self, JoinHandle};
use std::result::Result;

use tokio_core;
use tokio_core::reactor::Core;

use tokio_tls::{self, TlsAcceptorExt};

use thread_control;


enum WorkerMessage {
    Config(),
    Stream(net::TcpStream),
    Stop
}


pub struct WorkerPool {
    port_label: &'static str,
    num_threads: usize,
    tcp_backlog: u16,
    bind_address: Option<SocketAddr>,
    tls_acceptor: Option<Arc<TlsAcceptor>>,
    stream_sender: CbchanSender<WorkerMessage>,
    stream_receiver: CbchanReceiverStream<WorkerMessage>,
    srd_http_service: Mutex<SrdHttpService>,
    workers: Mutex<RefCell<Vec<Arc<Worker>>>>,
    stopped: Mutex<RefCell<bool>>,
}

impl WorkerPool {
    pub fn new(port_label: &'static str,
               num_threads: usize,
               tcp_backlog: u16,
               bind_address: Option<SocketAddr>,
               tls_acceptor: Option<Arc<TlsAcceptor>>,
               srd_http_service: SrdHttpService)
    -> WorkerPool {
        let (sender, receiver) = cbchan_bounded(num_threads * 3);
        WorkerPool {
            port_label: port_label,
            num_threads: num_threads,
            tcp_backlog: tcp_backlog,
            bind_address: bind_address,
            tls_acceptor: tls_acceptor,
            stream_sender: sender,
            stream_receiver: receiver,
            srd_http_service: Mutex::new(srd_http_service),
            workers: Mutex::new(RefCell::new(Vec::with_capacity(num_threads))),
            stopped: Mutex::new(RefCell::new(false)),
        }
    }

    pub fn spawn(self) -> Arc<WorkerPool> {
        let this = Arc::new(self);
        let this2 = Arc::clone(&this);
        let mut worker_count = 0;
        let guard = match this2.workers.lock() {
            Ok(g) => g,
            Err(_e) => panic!("Workers mutex poisoned")
        };
        for _ in 0..this.num_threads {
            worker_count += 1;
            let worker_id = format!("{}-{}", worker_count, this.port_label.clone());
            let worker = Worker::spawn(worker_id,
                                       this.bind_address.clone(),
                                       this.tcp_backlog,
                                       this.stream_receiver.clone(),
                                       this.tls_acceptor.clone(),
                                       Arc::downgrade(&this));
            (*guard).borrow_mut().push(worker);
        }
        this
    }

    pub fn stop(&self) {
        {
            let mut guard = match self.stopped.lock() {
                Ok(g) => g,
                Err(_e) => panic!("Stopped mutex poisoned")
            };
            *(*guard).borrow_mut() = true;
        }
        {
            let guard = match self.workers.lock() {
                Ok(g) => g,
                Err(_e) => panic!("Workers mutex poisoned")
            };
            for worker in &*(*guard).borrow() {
                worker.stop();
            }
        }
    }

    pub fn wait(&self) {
        let workers_copy: Vec<_>;
        {
            let guard = match self.workers.lock() {
                Ok(g) => g,
                Err(_e) => panic!("Workers mutex poisoned")
            };
            workers_copy = (&*(*guard).borrow()).iter()
                .map(|worker| Arc::clone(&worker))
                .collect();
        }

        for worker in workers_copy {
            worker.wait();
        }
    }

    pub fn send_stream(&self, stream: net::TcpStream) -> Option<net::TcpStream> {
        self.send_msg(WorkerMessage::Stream(stream))
    }

    pub fn send_service(&self, service: SrdHttpService) {
        let mut guard = match self.srd_http_service.lock() {
            Ok(g) => g,
            Err(_e) => panic!("Server configuration mutex poisoned")
        };
        // Update the cached value
        mem::replace(&mut *guard, service);
        self.send_msg(WorkerMessage::Config());
    }

    fn send_msg(&self, message: WorkerMessage) -> Option<net::TcpStream> {
        match message {
            WorkerMessage::Stream(_) => {
                if let Err(e) = self.stream_sender.send(message) {
                    error!("Error sending socket: {}", e);
                    let stream = match e.into_inner() {
                        WorkerMessage::Stream(stream) => stream,
                        _ => panic!("Illegal state error")
                    };
                    return Some(stream);
                }
                return None;
            },
            WorkerMessage::Config() => {
                inner_send_msg(self, || WorkerMessage::Config());
            },
            WorkerMessage::Stop =>
                inner_send_msg(self, || WorkerMessage::Stop)
        }

        fn inner_send_msg<F: Fn() -> WorkerMessage>(this: &WorkerPool, message_builder: F) {
            let guard = match this.workers.lock() {
                Ok(g) => g,
                Err(_e) => panic!("Workers mutex poisoned")
            };
            for worker in &*(*guard).borrow() {
                worker.send_msg(message_builder());
            }
        }

        None
    }

    fn srd_http_service(&self) -> SrdHttpService {
        let guard = match self.srd_http_service.lock() {
            Ok(g) => g,
            Err(_e) => panic!("Server configuration mutex poisoned")
        };
        let srd_http_service = &*guard;
        srd_http_service.clone()
    }

    fn worker_stopped(this: Arc<WorkerPool>, worker: &Worker) {
        let guard = match this.stopped.lock() {
            Ok(g) => g,
            Err(_e) => panic!("Stopped mutex poisoned")
        };
        if *(*guard).borrow() {
            return;
        }

        warn!("|{}| Worker unexpectedly stopped", worker.worker_id);

        let mut guard = match this.workers.lock() {
            Ok(g) => g,
            Err(_e) => panic!("Workers mutex poisoned")
        };
        let workers = &mut*(*guard).borrow_mut();
        for i in 0..workers.len() {
            let mut swap_worker = Default::default();
            {
                let old_worker = &mut workers[i];
                if &**old_worker as *const Worker == worker {
                    info!("|{}| Replacing worker", old_worker.worker_id);
                    let new_worker = Worker::spawn(old_worker.worker_id.clone(),
                                                   this.bind_address.clone(),
                                                   this.tcp_backlog,
                                                   this.stream_receiver.clone(),
                                                   this.tls_acceptor.clone(),
                                                   Arc::downgrade(&this));
                    swap_worker = Some((i, new_worker));
                }
            }
            if let Some((i, new_worker)) = swap_worker {
                trace!("Workers before: {:?}", workers);
                workers.swap_remove(i);
                trace!("Workers during: {:?}", workers);
                workers.push(new_worker);
                trace!("Workers after: {:?}", workers);
            }
        }
    }
}


pub struct Worker {
    worker_id: String,
    worker_pool: sync::Weak<WorkerPool>,
    bind_address: Option<SocketAddr>,
    tcp_backlog: u16,
    thread_status: thread_control::Flag,
    thread_controller: thread_control::Control,
    stream_receiver: CbchanReceiverStream<WorkerMessage>,
    msg_sender: CbchanSender<WorkerMessage>,
    msg_receiver: CbchanReceiverStream<WorkerMessage>,
    tls_acceptor: Option<Arc<TlsAcceptor>>,
    thread: Mutex<RefCell<Option<JoinHandle<()>>>>,
}

impl std::fmt::Debug for Worker {
    fn fmt(&self, f: &mut std::fmt::Formatter) -> Result<(), std::fmt::Error> {
        write!(f, "Worker {{ id: {}/{:p} }}", self.worker_id, self)
    }
}

impl Worker {
    fn spawn(worker_id:       String,
             bind_address:    Option<SocketAddr>,
             tcp_backlog:     u16,
             stream_receiver: CbchanReceiverStream<WorkerMessage>,
             tls_acceptor:    Option<Arc<TlsAcceptor>>,
             worker_pool:     sync::Weak<WorkerPool>)
    -> Arc<Worker> {
        let (thread_status, thread_controller) = thread_control::make_pair();
        let (msg_sender, msg_receiver) = cbchan_unbounded();

        let this = Worker {
            worker_id: worker_id,
            worker_pool: worker_pool,
            bind_address: bind_address,
            tcp_backlog: tcp_backlog,
            thread_status: thread_status,
            thread_controller: thread_controller,
            stream_receiver: stream_receiver,
            msg_sender: msg_sender,
            msg_receiver: msg_receiver,
            tls_acceptor: tls_acceptor,
            thread: Default::default(),
        };
        let this = Arc::new(this);

        let this2 = Arc::clone(&this);
        let thread = thread::spawn(move || {
            let this3 = Arc::clone(&this2);
            match catch_unwind(AssertUnwindSafe(|| Self::run(this2))) {
                _ => {
                    let worker_pool = match this3.worker_pool.upgrade() {
                        Some(pool) => pool,
                        None => panic!("Parent worker pool disappeared!")
                    };
                    WorkerPool::worker_stopped(worker_pool, &*this3);
                }
            }
        });

        {
            let guard = match this.thread.lock() {
                Ok(g) => g,
                Err(_e) => panic!("Workers mutex poisoned")
            };
            mem::replace(&mut *guard.borrow_mut(), Some(thread));
        }

        this
    }

    fn stop(&self) {
        debug!("|{}| Stopping worker", self.worker_id);
        self.thread_controller.stop();
        self.send_msg(WorkerMessage::Stop);
    }

    fn wait(&self) {
        let thread;
        {
            let mut guard = match self.thread.lock() {
                Ok(g) => g,
                Err(_e) => panic!("Workers mutex poisoned")
            };
            let t = &mut *(*guard).borrow_mut();
            thread = t.take();
        }
        if let Some(thread) = thread {
            debug!("|{}| Waiting for worker", self.worker_id);
            thread.join().ok();
            debug!("|{}| Worker stopped", self.worker_id);
        }
    }

    fn send_msg(&self, message: WorkerMessage) {
        if let Err(e) = self.msg_sender.send(message) {
            error!("|{}| Error sending message: {}", self.worker_id, e);
        }
    }

    fn run(this: Arc<Worker>) {
        enum Relaunch {
            Method,
            Thread,
        }

        while this.thread_status.alive() {
            match inner_run(Arc::clone(&this)) {
                Relaunch::Method => {},
                Relaunch::Thread => break
            }
        }

        enum StreamType {
            Net(net::TcpStream),
            Tokio(tokio_core::net::TcpStream),
            Tls(tokio_tls::TlsStream<tokio_core::net::TcpStream>),
        }

        enum ErrorType {
            Ignore,
            Channel(cbchan::RecvError),
            Io(std::io::Error),
            Tls(native_tls::Error),
            Stop,
        }

        fn inner_run(this: Arc<Worker>) -> Relaunch {
            debug!("|{}| Worker started", this.worker_id);

            let mut core = match Core::new() {
                Ok(c) => c,
                Err(e) => {
                    error!("Error creating event loop: {}", e);
                    return Relaunch::Method;
                }
            };
            let handle1 = core.handle();
            let handle2 = handle1.clone();

            let stream_to_enum = |stream_result| {
                match stream_result {
                    Ok(WorkerMessage::Stream(stream)) =>
                        Ok(StreamType::Net(stream)),
                    Err(e) =>
                        Err(ErrorType::Channel(e)),
                    _ =>
                        panic!("Illegal state error")
                }
            };

            let msg_to_enum = |channel_result| {
                match channel_result {
                    Ok(WorkerMessage::Config()) => {
                        Err(ErrorType::Ignore)
                    },
                    Ok(WorkerMessage::Stream(stream)) => {
                        Ok(StreamType::Net(stream))
                    },
                    Ok(WorkerMessage::Stop) =>
                        Err(ErrorType::Stop),
                    Err(e) =>
                        Err(ErrorType::Channel(e))
                }
            };

            // Connection counter
            let cx_counter = RefCell::new(0usize);
            let cx_counter = Rc::new(cx_counter);

            let increment_connection_counter = |_stream: &StreamType| {
                *cx_counter.borrow_mut() += 1;
                debug!("|{}| +Worker connection counter: {}", this.worker_id, cx_counter.borrow());
            };

            let convert_to_tokio_stream = |stream: StreamType| {
                match stream {
                    StreamType::Net(stream) => {
                        let stream = tokio_core::net::TcpStream::from_stream(stream, &handle1)
                            .expect("Failed to convert stream");
                        StreamType::Tokio(stream)
                    },
                    StreamType::Tokio(_) =>
                        stream,
                    _ =>
                        panic!("Illegal state error")
                }
            };

            let process_tls = |stream: StreamType| {
                match this.tls_acceptor {
                    Some(ref tls_acceptor) =>
                        match stream {
                            StreamType::Tokio(stream) =>
                                Either::A(
                                    tls_acceptor.accept_async(stream)
                                        .map(|tls_stream| StreamType::Tls(tls_stream))
                                        .map_err(|tls_error| ErrorType::Tls(tls_error))
                                ),
                            _ =>
                                panic!("Illegal state error")
                        },
                    None =>
                        Either::B(future::ok(stream))
                }
            };

            let http: Http<Chunk> = Http::new();

            let cx_counter = Rc::clone(&cx_counter);
            let worker_id = this.worker_id.clone();
            let decrement_connection_counter = move || {
                *cx_counter.borrow_mut() -= 1;
                debug!("|{}| -Worker connection counter: {}", worker_id, cx_counter.borrow());
            };
            let decrement_connection_counter = Rc::new(decrement_connection_counter);

            let srd_service = match this.worker_pool.upgrade() {
                Some(worker_pool) => worker_pool.srd_http_service(),
                None => panic!("Parent worker pool disappeared!")
            };
            let srd_service = Rc::new(srd_service);

            let service_stream = |stream| {
                let decrement_connection_counter = Rc::clone(&decrement_connection_counter);
                match stream {
                    StreamType::Tls(tls_stream) => {
                        let http_future = http.serve_connection(tls_stream, Rc::clone(&srd_service))
                            .then(move |http_result| {
                                decrement_connection_counter();
                                http_result
                            })
                            .map(|_| ())
                            .map_err(|_| ());
                        handle2.spawn(http_future);
                    },
                    StreamType::Tokio(tcp_stream) => {
                        let http_future = http.serve_connection(tcp_stream, Rc::clone(&srd_service))
                            .then(move |http_result| {
                                decrement_connection_counter();
                                http_result
                            })
                            .map(|_| ())
                            .map_err(|_| ());
                        handle2.spawn(http_future);
                    },
                    _ =>
                        panic!("Illegal state error")
                }
                Ok(())
            };

            let optional_tcp_listener = OptionalTcpListenerStream::new(&handle1, this.bind_address, this.tcp_backlog, this.worker_id.clone(),
                                                                       |(stream, _client_addr)| StreamType::Tokio(stream),
                                                                       |error| ErrorType::Io(error));

            let future = this.stream_receiver.clone()
                .then(stream_to_enum)
                .select(optional_tcp_listener)
                .select(this.msg_receiver.clone()
                            .then(msg_to_enum))
                .inspect(increment_connection_counter)
                .map(convert_to_tokio_stream)
                .and_then(process_tls)
                .or_else(|err| {
                    match err {
                        ErrorType::Ignore |
                        ErrorType::Stop => {},
                        _ => decrement_connection_counter()
                    }
                    Err(err)
                })
                .for_each(service_stream)
                .shared();

            debug!("|{}| Waiting for work", this.worker_id);
            let result = core.run(future.clone());
            match result {
                Ok(_) =>
                    Relaunch::Method,
                Err(e) =>
                    match *e {
                        ErrorType::Ignore =>
                            Relaunch::Method,
                        ErrorType::Channel(ref e) => {
                            error!("|{}| Channel error in worker: {}", this.worker_id, e);
                            Relaunch::Thread
                        },
                        ErrorType::Io(ref e) => {
                            error!("|{}| IO error in worker: {}", this.worker_id, e);
                            Relaunch::Method
                        },
                        ErrorType::Tls(ref e) => {
                            error!("|{}| TLS error in worker: {}", this.worker_id, e);
                            Relaunch::Method
                        },
                        ErrorType::Stop =>
                            Relaunch::Thread
                    }
            }
        }
    }
}
