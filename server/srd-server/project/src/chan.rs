use crossbeam_channel as cbchan;

use futures::{Async, Poll};
use futures::stream::Stream;
use futures::task::{self, Task};


pub fn cbchan_bounded<ItemT>(cap: usize) -> (CbchanSender<ItemT>, CbchanReceiverStream<ItemT>) {
    let (item_sender, item_receiver) = cbchan::bounded(cap);
    let (task_sender, task_receiver) = cbchan::unbounded();
    (CbchanSender::new(item_sender, task_receiver), CbchanReceiverStream::new(item_receiver, task_sender))
}

pub fn cbchan_unbounded<ItemT>() -> (CbchanSender<ItemT>, CbchanReceiverStream<ItemT>) {
    let (item_sender, item_receiver) = cbchan::unbounded();
    let (task_sender, task_receiver) = cbchan::unbounded();
    (CbchanSender::new(item_sender, task_receiver), CbchanReceiverStream::new(item_receiver, task_sender))
}

pub struct CbchanSender<ItemT> {
    item_sender:   cbchan::Sender<ItemT>,
    task_receiver: cbchan::Receiver<Task>,
}

impl<ItemT> CbchanSender<ItemT> {
    pub fn new(item_sender: cbchan::Sender<ItemT>, task_receiver: cbchan::Receiver<Task>) -> CbchanSender<ItemT> {
        CbchanSender {
            item_sender:   item_sender,
            task_receiver: task_receiver,
        }
    }

    pub fn send(&self, msg: ItemT) -> Result<(), cbchan::SendError<ItemT>> {
        let result = self.item_sender.send(msg);
        if self.task_receiver.is_disconnected() {
            panic!("Task sender disconnected");
        }
        for task in self.task_receiver.try_iter() {
            task.notify();
        }
        result
    }
}

impl<ItemT> Clone for CbchanSender<ItemT> {
    fn clone(&self) -> Self {
        CbchanSender {
            item_sender:   self.item_sender.clone(),
            task_receiver: self.task_receiver.clone(),
        }
    }
}


pub struct CbchanReceiverStream<ItemT> {
    item_receiver: cbchan::Receiver<ItemT>,
    task_sender:   cbchan::Sender<Task>,
}

impl<ItemT> CbchanReceiverStream<ItemT> {
    pub fn new(item_receiver: cbchan::Receiver<ItemT>, task_sender: cbchan::Sender<Task>) -> CbchanReceiverStream<ItemT> {
        CbchanReceiverStream {
            item_receiver: item_receiver,
            task_sender:   task_sender,
        }
    }
}

impl<ItemT> Stream for CbchanReceiverStream<ItemT> {
    type Item = ItemT;
    type Error = cbchan::RecvError;

    fn poll(&mut self) -> Poll<Option<Self::Item>, Self::Error> {
        if let Err(_e) = self.task_sender.send(task::current()) {
            panic!("Task receiver disconnected");
        }
        match self.item_receiver.try_recv() {
            Ok(item) =>
                Ok(Async::Ready(Some(item))),
            Err(cbchan::TryRecvError::Empty) =>
                Ok(Async::NotReady),
            _ =>
                Ok(Async::Ready(None))
        }
    }
}

impl<ItemT> Clone for CbchanReceiverStream<ItemT> {
    fn clone(&self) -> Self {
        CbchanReceiverStream {
            item_receiver: self.item_receiver.clone(),
            task_sender:   self.task_sender.clone(),
        }
    }
}

