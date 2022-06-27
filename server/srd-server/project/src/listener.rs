use futures::{Async, Poll};
use futures::stream::Stream;

use mio;

use net2;
use net2::unix::UnixTcpBuilderExt;

use std::io;
use std::net::{self, SocketAddr};

use tokio_core;
use tokio_core::reactor::Handle;
use tokio_core::reactor::PollEvented;


pub struct TcpListener {
    poll_evented: PollEvented<mio::net::TcpListener>,
}

impl TcpListener {
    pub fn new(listener: net::TcpListener, bound_addr: net::SocketAddr, handle: &Handle) -> TcpListener {
        let listener = match mio::net::TcpListener::from_listener(listener, &bound_addr) {
            Ok(listener) =>
                listener,
            Err(e) =>
                panic!("Error opening async listener: {}", e)
        };
        let poll_evented = PollEvented::new(listener, handle)
            .expect("Error initializing async listener");
        TcpListener {
            poll_evented: poll_evented,
        }
    }
}

impl Stream for TcpListener {
    type Item = (net::TcpStream, net::SocketAddr);
    type Error = io::Error;

    fn poll(&mut self) -> Poll<Option<Self::Item>, Self::Error> {
        match self.poll_evented.poll_read() {
            Async::NotReady =>
                Ok(Async::NotReady),
            Async::Ready(_) =>
                match self.poll_evented.get_ref().accept_std() {
                    Err(e) =>
                        match e {
                            _ if e.kind() == io::ErrorKind::WouldBlock => {
                                self.poll_evented.need_read();
                                Ok(Async::NotReady)
                            },
                            _ =>
                                Err(e)
                        },
                    Ok((stream, client_address)) =>
                        Ok(Async::Ready(Some((stream, client_address))))
                }
        }
    }
}


pub struct OptionalTcpListenerStream<ItemT, ErrT, ItemFactoryT, ErrFactoryT>
        where ItemFactoryT: FnMut(<tokio_core::net::Incoming as Stream>::Item) -> ItemT,
              ErrFactoryT: FnMut(<tokio_core::net::Incoming as Stream>::Error) -> ErrT {
    incoming: Option<tokio_core::net::Incoming>,
    item_factory: ItemFactoryT,
    err_factory: ErrFactoryT,
}

impl<ItemT, ErrT, ItemFactoryT, ErrFactoryT> OptionalTcpListenerStream<ItemT, ErrT, ItemFactoryT, ErrFactoryT>
        where ItemFactoryT: FnMut(<tokio_core::net::Incoming as Stream>::Item) -> ItemT,
              ErrFactoryT: FnMut(<tokio_core::net::Incoming as Stream>::Error) -> ErrT {
    pub fn new(handle: &Handle, bind_address: Option<SocketAddr>, tcp_backlog: u16, worker_id: String, item_factory: ItemFactoryT, err_factory: ErrFactoryT)
    -> OptionalTcpListenerStream<ItemT, ErrT, ItemFactoryT, ErrFactoryT> {
        let mut incoming = None;
        if let Some(bind_address) = bind_address {
            let tcp = net2::TcpBuilder::new_v4().unwrap();
            let tcp_listener = tcp
                .reuse_address(true).expect("Failed to set SO_REUSEADDR")
                .reuse_port(true).expect("Failed to set SO_REUSEPORT")
                .bind(bind_address).expect("Failed to bind")
                .listen(tcp_backlog as i32).expect("Failed to listen");
            incoming = Some(tokio_core::net::TcpListener::from_listener(tcp_listener,
                                                                        &bind_address,
                                                                        handle)
                .expect("Failed to create listener stream")
                .incoming());
            info!("|{}| Listening on: {}", worker_id, bind_address);
        }
        OptionalTcpListenerStream {
            incoming: incoming,
            item_factory: item_factory,
            err_factory: err_factory,
        }
    }
}

impl<ItemT, ErrT, ItemFactoryT, ErrFactoryT> Stream for OptionalTcpListenerStream<ItemT, ErrT, ItemFactoryT, ErrFactoryT>
        where ItemFactoryT: FnMut(<tokio_core::net::Incoming as Stream>::Item) -> ItemT,
              ErrFactoryT: FnMut(<tokio_core::net::Incoming as Stream>::Error) -> ErrT {
    type Item = ItemT;
    type Error = ErrT;
    fn poll(&mut self) -> Poll<Option<Self::Item>, Self::Error> {
        match self.incoming {
            Some(ref mut incoming) => {
                match incoming.poll() {
                    Ok(Async::Ready(Some(item))) => {
                        let item = (self.item_factory)(item);
                        Ok(Async::Ready(Some(item)))
                    },
                    Ok(Async::Ready(None)) =>
                        Ok(Async::Ready(None)),
                    Ok(Async::NotReady) =>
                        Ok(Async::NotReady),
                    Err(e) => {
                        let err = (self.err_factory)(e);
                        Err(err)
                    }
                }
            },
            None =>
                Ok(Async::NotReady),
        }
    }
}