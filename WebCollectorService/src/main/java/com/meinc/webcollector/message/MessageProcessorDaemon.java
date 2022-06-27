package com.meinc.webcollector.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.launcher.serverprops.ServerPropertyHolder.Change;
import com.meinc.launcher.serverprops.ServerPropertyHolder.ChangeListener;
import com.meinc.mrsoa.service.ServiceCallStack;
import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.ServiceMessage;
import com.meinc.webcollector.message.handler.MessageTypeHandlerRegistry;

public class MessageProcessorDaemon {
    private static final Log log = LogFactory.getLog(MessageProcessorDaemon.class);

    private boolean MESSAGE_PROCESSOR_SHORT_CIRCUIT_FILE_DETECTION;
    private int MESSAGE_PROCESSOR_THREAD_COUNT;
    private int MESSAGE_PROCESSOR_INTERVAL_MS = 1000;
    private int MESSAGE_PROCESSOR_MESSAGE_THRESHOLD_MIN = 10;
    private int MESSAGE_PROCESSOR_MESSAGE_THRESHOLD_MAX = 100;

    @Autowired
    private WebCollectorMessageBuffer messageBuffer;
    private MessageBufferProxy messageBufferProxy;
    private Semaphore messageBufferAddedMessageCount;

    @Autowired
    private MessageTypeHandlerRegistry messageTypeHandlerRegistry;

    private List<Worker> workers = new ArrayList<Worker>();

    @PostConstruct
    public void onPostConstruct() {
        messageBufferProxy = new MessageBufferProxy(messageBuffer.getSecret());
        messageBufferAddedMessageCount = messageBuffer.getAddedMessageCount();

        ServerPropertyHolder.addPropertyChangeListener("message\\.processor\\..*", new ChangeListener() {
            @Override
            public void propertiesChanged(List<Change> properties) {
                for (Change change : properties) {
                    if ("message.processor.short.circuit.file.detection".equals(change.key)) {
                        MESSAGE_PROCESSOR_SHORT_CIRCUIT_FILE_DETECTION = Boolean.parseBoolean(change.newValue);
                        ArrayList<Worker> newWorkers = new ArrayList<Worker>(workers.size());
                        synchronized (MessageProcessorDaemon.this) {
                            for (Worker worker : workers) {
                                worker.interrupt();
                                newWorkers.add(new Worker());
                            }
                            workers = newWorkers;
                        }
                    }
                    if ("message.processor.interval.ms".equals(change.key))
                        MESSAGE_PROCESSOR_INTERVAL_MS = Integer.parseInt(change.newValue);
                    if ("message.processor.message.threshold.min".equals(change.key))
                        MESSAGE_PROCESSOR_MESSAGE_THRESHOLD_MIN = Integer.parseInt(change.newValue);
                    if ("message.processor.message.threshold.max".equals(change.key))
                        MESSAGE_PROCESSOR_MESSAGE_THRESHOLD_MAX = Integer.parseInt(change.newValue);
                    if ("message.processor.thread.count".equals(change.key)) {
                        int old = MESSAGE_PROCESSOR_THREAD_COUNT;
                        MESSAGE_PROCESSOR_THREAD_COUNT = Integer.parseInt(change.newValue);
                        log.info("Changing message processor thread count from " + old + " to " + MESSAGE_PROCESSOR_THREAD_COUNT);
                        synchronized (MessageProcessorDaemon.this) {
                            while (MESSAGE_PROCESSOR_THREAD_COUNT > workers.size())
                                workers.add(new Worker());
                            while (MESSAGE_PROCESSOR_THREAD_COUNT < workers.size()) {
                                workers.get(workers.size()-1).interrupt();
                                workers.remove(workers.size()-1);
                            }
                        }
                    }
                }
            }
        });
        MESSAGE_PROCESSOR_SHORT_CIRCUIT_FILE_DETECTION = Boolean.parseBoolean(ServerPropertyHolder.getProperty("message.processor.short.circuit.file.detection", "false"));
        String threadCountString = ServerPropertyHolder.getProperty("message.processor.thread.count");
        if (threadCountString != null)
            MESSAGE_PROCESSOR_THREAD_COUNT = Integer.parseInt(threadCountString);
        String intervalMsString = ServerPropertyHolder.getProperty("message.processor.interval.ms");
        if (intervalMsString != null)
            MESSAGE_PROCESSOR_INTERVAL_MS = Integer.parseInt(intervalMsString);
        String messageThresholdMinString = ServerPropertyHolder.getProperty("message.processor.message.threshold.min");
        if (messageThresholdMinString != null)
            MESSAGE_PROCESSOR_MESSAGE_THRESHOLD_MIN = Integer.parseInt(messageThresholdMinString);
        String messageThresholdMaxString = ServerPropertyHolder.getProperty("message.processor.message.threshold.max");
        if (messageThresholdMaxString != null)
            MESSAGE_PROCESSOR_MESSAGE_THRESHOLD_MAX = Integer.parseInt(messageThresholdMaxString);
    }

    private static AtomicInteger workerIdSource = new AtomicInteger();
    private class Worker extends Thread {
        private int workerId = workerIdSource.incrementAndGet();
        public Worker() {
            setDaemon(true);
            start();
        }

        @Override
        public void run() {
            try {
                Thread.currentThread().setContextClassLoader(Worker.class.getClassLoader());
                ServiceCallStack.orphanCurrentThread();
                long lastProcessTime = 0;
                while (!isInterrupted()) {
                    List<CollectorMessage> claimedMessages = null;
                    try {
                        long now = System.currentTimeMillis();
                        if (MESSAGE_PROCESSOR_SHORT_CIRCUIT_FILE_DETECTION) {
                            // If the server is idle and a single message is added then only a single thread is woken. If the server is busy and
                            // many messages are added then more threads will be woken.
                            messageBufferAddedMessageCount.acquire();
                            int acquiredMessagePermitCount = 1;
                            acquiredMessagePermitCount += messageBufferAddedMessageCount.drainPermits();
                            if (acquiredMessagePermitCount > MESSAGE_PROCESSOR_MESSAGE_THRESHOLD_MAX) {
                                messageBufferAddedMessageCount.release(acquiredMessagePermitCount - MESSAGE_PROCESSOR_MESSAGE_THRESHOLD_MAX);
                                acquiredMessagePermitCount = MESSAGE_PROCESSOR_MESSAGE_THRESHOLD_MAX;
                            }
                            claimedMessages = messageBuffer.claimFromBuffer(0, acquiredMessagePermitCount);
                            if (claimedMessages == null || acquiredMessagePermitCount > claimedMessages.size())
                                messageBufferAddedMessageCount.release((claimedMessages==null) ? acquiredMessagePermitCount : (acquiredMessagePermitCount-claimedMessages.size()));
                        } else {
                            claimedMessages = messageBuffer.claimFromBuffer(MESSAGE_PROCESSOR_MESSAGE_THRESHOLD_MIN,
                                                                            MESSAGE_PROCESSOR_MESSAGE_THRESHOLD_MAX);
                            if (claimedMessages == null && now - lastProcessTime > MESSAGE_PROCESSOR_INTERVAL_MS)
                                claimedMessages = messageBuffer.claimFromBuffer(0, MESSAGE_PROCESSOR_MESSAGE_THRESHOLD_MAX);
                        }

                        int claimedMessageCount = 0;
                        if (claimedMessages != null)
                            for (CollectorMessage claimedMessage : claimedMessages)
                                if (claimedMessage != null)
                                    claimedMessageCount += 1;

                        if (claimedMessageCount > 0) {
                            lastProcessTime = System.currentTimeMillis();

                            if (log.isDebugEnabled()) {
                                //exception: if there is an acra report, it's just too much to log. don't even try
                                String msg = claimedMessages.toString();
                                if (msg.contains("/acra/report")) {
                                    msg = " --- ACRA DATA (not displayed for brevity) ---";
                                } else if (msg.contains("/game/getSyncMessages")) {
                                    msg = "--- getSyncMessages ---";
                                }
                                log.debug("Worker " + workerId + " claimed " + claimedMessageCount + " messages: " + msg);
                            }

                            //TODO: this sorting into lists by message type is no longer necessary as claimFromBuffer above only returns homogeneous message lists
                            Map<String,List<CollectorMessage>> messageListByMessageType = new HashMap<String,List<CollectorMessage>>();
                            for (CollectorMessage claimedMessage : claimedMessages) {
                                if (claimedMessage != null) {
                                    List<CollectorMessage> messageList = messageListByMessageType.get(claimedMessage.getMessageType());
                                    if (messageList == null) {
                                        messageList = new ArrayList<CollectorMessage>();
                                        messageListByMessageType.put(claimedMessage.getMessageType(), messageList);
                                    }
                                    messageList.add(claimedMessage);
                                }
                            }

                            for (String messageType : messageListByMessageType.keySet()) {
                                List<CollectorMessage> messageList = messageListByMessageType.get(messageType);

                                ServiceEndpoint messageTypeHandler = messageTypeHandlerRegistry.getMessageTypeHandlerEndpoint(messageType);
                                if (messageTypeHandler == null) {
                                    log.warn("No message handler regisered for type " + messageType + " - waiting");
                                    continue;
                                }
                                ServiceMessage.sendFast(messageTypeHandler, "handleMessages", messageList, messageBufferProxy);
                            }
                        }

                        if (!MESSAGE_PROCESSOR_SHORT_CIRCUIT_FILE_DETECTION) {
                            Thread.sleep(MESSAGE_PROCESSOR_INTERVAL_MS / 3);
                        }

                    } catch (InterruptedException e) {
                        log.warn("Worker " + workerId + " was interrupted", e);
                        interrupt();
                        continue;
                    } catch (Throwable e) {
                        log.error(e.getMessage(), e);
                    } finally {
                        if (claimedMessages != null) {
                            try {
                                messageBuffer.unclaimMessages(claimedMessages);
                            } catch (Exception e) {
                                log.error("Error unclaiming messages " + claimedMessages, e);
                            }
                        }
                    }
                }
            } finally {
                log.info("Worker " + workerId + " finished");
            }
        }
    }

    public synchronized void start() {
        if (!workers.isEmpty())
            throw new IllegalStateException("daemon already started");

        for (int i = 0; i < MESSAGE_PROCESSOR_THREAD_COUNT; i++)
            workers.add(new Worker());
    }

    public synchronized void shutdown() {
        if (workers.isEmpty())
            throw new IllegalStateException("daemon not started");

        for (int i = 0; i < MESSAGE_PROCESSOR_THREAD_COUNT; i++)
            workers.get(i).interrupt();

        for (int i = 0; i < MESSAGE_PROCESSOR_THREAD_COUNT; i++)
            try {
                workers.get(i).join();
            } catch (InterruptedException e) { }

        workers.clear();
    }
}
