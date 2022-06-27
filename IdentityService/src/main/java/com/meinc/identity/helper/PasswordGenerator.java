package com.meinc.identity.helper;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;

import com.meinc.commons.encryption.IEncryption;

public class PasswordGenerator
{
    private static final int MAX_QUEUE_SIZE = 1000;
    //private static final Logger _logger = Logger.getLogger(PasswordGenerator.class);
    private BlockingQueue<String> _queue = new ArrayBlockingQueue<String>(MAX_QUEUE_SIZE);
    private Thread _producerThread = new Thread(new Producer());
    private boolean _interrupted = false;
    
    @Autowired
    private IEncryption encryptionService;
    
    public PasswordGenerator()
    {
    }
    
    public String getRandomPassword()
    {
        try {
            //_logger.debug("taking item from queue");
            String randomPassword = _queue.take();
            return randomPassword;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void start()
    {
        _producerThread.start();
    }
    
    public void stop()
    {
        _producerThread.interrupt();
    }
    
    class Producer
    implements Runnable
    {
        @Override
        public void run()
        {
            while (!_interrupted) {
                String randomPassword = encryptionService.generateRandomPassword();
                try {
                    _queue.put(randomPassword);
                    //_logger.debug("added item to queue");
                } catch (InterruptedException e) {
                    _interrupted = true;
                }
            }
        }
    }
}
