use chan::{cbchan_unbounded, CbchanSender, CbchanReceiverStream};

use core_affinity;

use crossbeam_channel as cbchan;

use futures::future::Future;
use futures::stream::Stream;

use listener;

use native_tls::TlsAcceptor;

use net2;

use service::SrdHttpService;

use std::cell::RefCell;
use std::io;
use std::mem;
use std::net::{self, SocketAddr};
use std::thread::{self, JoinHandle};
use std::sync::{Arc, Mutex};

use thread_control;

use tokio_core::reactor::Core;

use worker::WorkerPool;


enum DispatcherMessage {
    Stream((net::TcpStream, SocketAddr)),
    Stop,
}

enum DispatcherError {
    Stream(io::Error),
    Channel(cbchan::RecvError),
    Stop,
}

pub struct ConnectionDispatcher {
    port_label: &'static str,
    worker_pool: Arc<WorkerPool>,
    thread: Mutex<RefCell<Option<JoinHandle<()>>>>,
    thread_status: thread_control::Flag,
    thread_controller: thread_control::Control,
    msg_sender: CbchanSender<DispatcherMessage>,
    msg_receiver: CbchanReceiverStream<DispatcherMessage>,
}

impl ConnectionDispatcher {
    pub fn spawn(port_label:       &'static str,
                 bind_address:     SocketAddr,
                 reuse_port:       bool,
                 tls_acceptor:     Option<TlsAcceptor>,
                 num_threads:      usize,
                 tcp_backlog:      u16,
                 srd_http_service: SrdHttpService)
    -> Arc<ConnectionDispatcher> {
        let tls_acceptor = tls_acceptor.map(|ta| Arc::new(ta));
    
        info!("|{}| Launching {} workers", port_label, num_threads);
    
        let worker_pool = WorkerPool::new(port_label,
                                          num_threads,
                                          tcp_backlog,
                                          if reuse_port { Some(bind_address.clone()) } else { None },
                                          tls_acceptor,
                                          srd_http_service);
        let worker_pool = worker_pool.spawn();
        let worker_pool2 = Arc::clone(&worker_pool);
    
        let (msg_sender, msg_receiver) = cbchan_unbounded();
        let (thread_status, thread_controller) = thread_control::make_pair();

        let this = ConnectionDispatcher {
            port_label: port_label,
            worker_pool: worker_pool,
            thread: Default::default(),
            thread_status: thread_status,
            thread_controller: thread_controller,
            msg_sender: msg_sender,
            msg_receiver: msg_receiver,
        };
        let this = Arc::new(this);
        let this2 = Arc::clone(&this);

        if !reuse_port {
            let core_ids = core_affinity::get_core_ids().expect("Error reading cpu cores");
            let thread = thread::spawn(move || {
                core_affinity::set_for_current(core_ids[0]);

                let mut core = match Core::new() {
                    Ok(c) => c,
                    Err(e) =>
                        panic!("Error creating event loop: {}", e)
                };
        
                let tcp = net2::TcpBuilder::new_v4().unwrap();
                let listener = tcp
                    .reuse_address(true).expect("Failed to set SO_REUSEADDR")
                    .bind(bind_address).expect("Failed to bind")
                    .listen(tcp_backlog as i32).expect("Failed to listen");
                let listener = listener::TcpListener::new(listener, bind_address, &core.handle());
    
                let listener_to_enum = |listen_result| {
                    match listen_result {
                        Ok((stream, client_addr)) =>
                            Ok(DispatcherMessage::Stream((stream, client_addr))),
                        Err(e) =>
                            Err(DispatcherError::Stream(e))
                    }
                };

                let msg_to_enum = |msg_result| {
                    match msg_result {
                        Ok(msg) => Ok(msg),
                        Err(e) => Err(DispatcherError::Channel(e))
                    }
                };

                let send_stream = |message|
                        match message {
                            DispatcherMessage::Stream((stream, _client_addr)) => {
                                worker_pool2.send_stream(stream);
                                Ok(())
                            },
                            DispatcherMessage::Stop =>
                                Err(DispatcherError::Stop)
                        };

                let future = listener
                    .then(listener_to_enum)
                    .select(this2.msg_receiver.clone()
                                .then(msg_to_enum))
                    .for_each(send_stream);
                let future = future.shared();
        
                while this2.thread_status.alive() {
                    match core.run(future.clone()) {
                        Ok(_) => {},
                        Err(e) =>
                            match *e {
                                DispatcherError::Channel(ref e) =>
                                    error!("|{}| Channel error in dispatcher: {}", this2.port_label, e),
                                DispatcherError::Stream(ref e) =>
                                    error!("|{}| IO error in dispatcher: {}", this2.port_label, e),
                                DispatcherError::Stop => {
                                    debug!("|{}| Stopping dispatcher", this2.port_label);
                                    break;
                                }
                            }
                    }
                }
            });

            let guard = match this.thread.lock() {
                Ok(g) => g,
                Err(_e) => panic!("Workers mutex poisoned")
            };
            mem::replace(&mut *guard.borrow_mut(), Some(thread));
        }
    
        this
    }

    pub fn stop(&self) {
        info!("|{}| Stopping dispatcher", self.port_label);
        self.thread_controller.stop();
        self.send_msg(DispatcherMessage::Stop);

        self.worker_pool.stop();
    }

    pub fn wait(&self) {
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
            trace!("|{}| Waiting for dispatcher", self.port_label);
            thread.join().ok();
            debug!("|{}| Dispatcher stopped", self.port_label);
        }

        self.worker_pool.wait();
    }

    fn send_msg(&self, message: DispatcherMessage) {
        if let Err(e) = self.msg_sender.send(message) {
            error!("Error sending dispatcher message: {}", e);
        }
    }

    pub fn worker_pool(&self) -> &WorkerPool {
        &*self.worker_pool
    }
}