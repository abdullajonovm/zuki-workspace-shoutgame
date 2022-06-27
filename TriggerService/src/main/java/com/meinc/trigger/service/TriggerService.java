package com.meinc.trigger.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.ServiceMessage;
import com.meinc.mrsoa.service.annotation.OnStart;
import com.meinc.mrsoa.service.annotation.OnStop;
import com.meinc.mrsoa.service.annotation.Service;
import com.meinc.mrsoa.service.annotation.ServiceMethod;
import com.meinc.trigger.domain.Trigger;

@Service(
    namespace =      TriggerService.MEINC_NAMESPACE, 
    name =           TriggerService.SERVICE_NAME, 
    interfaces =     TriggerService.SERVICE_INTERFACE, 
    version =        TriggerService.SERVICE_VERSION, 
    exposeAs =       ITriggerService.class)
public class TriggerService 
implements ITriggerService
{
    public static final String MEINC_NAMESPACE = "meinc-service";
    public static final String SERVICE_NAME = "TriggerService";
    public static final String SERVICE_INTERFACE = "ITriggerService";
    public static final String SERVICE_VERSION = "1.0";
    
    private static Logger _logger = Logger.getLogger(TriggerService.class);
    
    private Lock _lock = new ReentrantLock();
//    private Lock _processCountLock = new ReentrantLock();    
    private List<Callback> _callbacks = new ArrayList<Callback>();
//    private DistributedQueue<Trigger> _workQueue = DistributedQueue.getQueue("trigger_work_queue");
    private BlockingQueue<Trigger> _workQueue = new LinkedBlockingQueue<Trigger>();
    private TriggerConsumer _consumer;
//    private CounterThread _counterThread;
//    private int _numAdded;
//    private int _numRemoved;
    
    @Override
    @OnStart
    @ServiceMethod
    public void start()
    {
        _consumer = new TriggerConsumer();
        _consumer.setDaemon(true);
        _consumer.start();
        
//        _counterThread = new CounterThread();
//        _counterThread.setDaemon(true);
//        _counterThread.start();
    }

    @Override
    @OnStop
    @ServiceMethod
    public void stop()
    {
        _consumer.interrupt();
//        _counterThread.interrupt();
    }
    
    class Callback
    {
        public ServiceEndpoint endpoint;
        public String methodName;
        public String route;
        
        Callback(){
            this.route = "*";
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if (obj == null) return false;
            if (!(obj instanceof Callback)) return false;
            Callback c = (Callback) obj;
            if (! c.methodName.equals(methodName)) return false;
            if (! c.endpoint.equals(endpoint)) return false;
            return true;
        }
    }
    
    @Override
    @ServiceMethod
    public boolean registerCallback(ServiceEndpoint endpoint, String methodName)
    {
        return registerCallback(endpoint, methodName, "*");
    }

    @Override
    @ServiceMethod
    public boolean registerCallback(ServiceEndpoint endpoint, String methodName, String route)
    {
        if (endpoint == null || methodName == null) throw new IllegalArgumentException("null params not allowed");
        Callback callback = new Callback();
        callback.endpoint = endpoint;
        callback.methodName = methodName;
        callback.route = route;
        
        boolean duplicate = false;
        _lock.lock();
        try {
            for (Callback cb : _callbacks) {
                if (callback.equals(cb)) {
                    duplicate = true;
                    break;
                }
            }
            if (duplicate) return false;
    
            _callbacks.add(callback);
        } finally {
            _lock.unlock();
        }
        return true;
    }
    
    @Override
    @ServiceMethod
    public boolean unregisterCallback(ServiceEndpoint endpoint)
    {
        if (endpoint == null) return false;

        boolean found = false;
        _lock.lock();
        try {
            for (Callback cb : _callbacks) {
                if (cb.endpoint.equals(endpoint)) {
                    _callbacks.remove(cb);
                    found = true;
                    break;
                }
            }
        } finally {
            _lock.unlock();
        }
        
        return found;
    }
    
    @Override
    @ServiceMethod
    public void process(Trigger trigger)
    {
        //drop on the queue
//        _logger.debug(">>> ADD TO QUEUE BEGIN: " + trigger.getKey());
        boolean added = _workQueue.offer(trigger);
        if (!added)
            _logger.warn("failed to add trigger to queue: " + trigger);
//        if (added) {
//            _processCountLock.lock();
//            try {
//                _numAdded++;
//            } finally {
//                _processCountLock.unlock();
//            }
//        }
//        _logger.debug("<<< ADD TO QUEUE END: " + trigger.getKey() + ", success: " + added);
    }
    
    @Override
    @ServiceMethod
    public void enqueue(Trigger trigger)
    {
        this.process(trigger);
    }
    
    @Override
    @ServiceMethod
    public void enqueue(String triggerKey, Object serializedTriggerPayload, String route, String source, Set<String> bundleIds, int contextId)
    {
        Trigger trigger = new Trigger(triggerKey, serializedTriggerPayload, route, source, bundleIds, contextId);
        this.process(trigger);
    }
    
    private class TriggerConsumer
    extends Thread
    {
        @Override
        public void run()
        {
            _logger.info("TriggerConsumer launching with work queue of type " + _workQueue.getClass().getName());
            while (!isInterrupted()) {
                try {
                    Trigger trigger = null;
                    trigger = _workQueue.take();
                    
//                    _processCountLock.lock();
//                    try {
//                        _numRemoved++;
//                    } finally {
//                        _processCountLock.unlock();
//                    }
                    
                    if (trigger == null) {
                        _logger.debug("trigger is NULL");
                        continue;
                    }
                    _logger.info(MessageFormat.format("BEGIN processing trigger:{0} \t:{1} \t:{2}", trigger.getKey(), trigger.getPayload(), trigger.getRoute()));

                    // Routing is agnostic
                    if ("*".equals(trigger.getRoute())){
                        _lock.lock();
                        try {
                            for (Callback cb : _callbacks) {
                                Object result = ServiceMessage.send(cb.endpoint, cb.methodName, trigger);
                                if (result instanceof Boolean && !((Boolean)result).booleanValue())
                                    break;
                            }
                        } finally {
                            _lock.unlock();
                        }
                    }else{
                        // Routing is specific
                        _lock.lock();
                        try {
                            for (Callback cb : _callbacks) {
                                if (trigger.getRoute().equals(cb.route)){
                                    Object result = ServiceMessage.send(cb.endpoint, cb.methodName, trigger);
                                    if (result instanceof Boolean && !((Boolean)result).booleanValue())
                                        break;
                                }
                            }
                        } finally {
                            _lock.unlock();
                        }                        
                    }                    
                    _logger.info(MessageFormat.format("END processing trigger: {0}:\n\t{1}", trigger.getKey(), trigger.getPayload()));

                } catch (InterruptedException e) {
                    _logger.info("queue INTERRUPTED");
                    interrupt();
                } catch (Exception e) {
                    if (e != null && e instanceof IllegalStateException && e.getMessage() != null && "Hazelcast Instance is not active!".equals(e.getMessage()))
                        ; //ignore; just means the server is rebooting; get over it
                    else
                        _logger.error("error in trigger consumer thread", e);
                }
            }
            
            _logger.info("QUEUE completed");
        }
    }
    
//    private class CounterThread
//    extends Thread
//    {
//        @Override
//        public void run()
//        {
//            while (!isInterrupted()) {
//                try {
//                    
//                    _processCountLock.lock();
//                    try {
//                        _logger.debug(MessageFormat.format("#Added: {0}, #Removed: {1}, Qsize: {2}", _numAdded, _numRemoved, _workQueue.size()));
//                    } finally {
//                        _processCountLock.unlock();
//                    }
//                    
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    interrupt();
//                }
//            }
//        }
//    }
}
