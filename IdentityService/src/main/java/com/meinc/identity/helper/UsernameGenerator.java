package com.meinc.identity.helper;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;

import com.meinc.commons.encryption.IEncryption;

public class UsernameGenerator
{
    private static final int MAX_QUEUE_SIZE = 1000;
    //private static final Logger _logger = Logger.getLogger(UsernameGenerator.class);
    private BlockingQueue<String> _queueEn = new ArrayBlockingQueue<String>(MAX_QUEUE_SIZE);
    private BlockingQueue<String> _queueEs = new ArrayBlockingQueue<String>(MAX_QUEUE_SIZE);
    private Thread _producerEnThread = new Thread(new ProducerEn());
    private Thread _producerEsThread = new Thread(new ProducerEs());
    private boolean _interrupted = false;
    
    @Autowired
    private IEncryption encryptionService;
    
    public UsernameGenerator()
    {
    }
    
    public String getRandomUsername(String languageCode)
    {
        try {
            //_logger.debug("taking item from queue");
            String randomUsername;
            if ("es".equals(languageCode)) {
                randomUsername = _queueEs.take();
            } else {
                randomUsername = _queueEn.take();
            }
            return randomUsername;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void start()
    {
        _producerEnThread.start();
        _producerEsThread.start();
    }
    
    public void stop()
    {
        _producerEnThread.interrupt();
        _producerEsThread.interrupt();
    }
    
    class ProducerEn
    implements Runnable
    {
        @Override
        public void run()
        {
            while (!_interrupted) {
                String randomUsername = encryptionService.generateRandomUsername("en");
                try {
                    _queueEn.put(randomUsername);
                    //_logger.debug("added item to queue");
                } catch (InterruptedException e) {
                    _interrupted = true;
                }
            }
        }
    }

    class ProducerEs
    implements Runnable
    {
        @Override
        public void run()
        {
            while (!_interrupted) {
                String randomUsername = encryptionService.generateRandomUsername("es");
                try {
                    _queueEs.put(randomUsername);
                    //_logger.debug("added item to queue");
                } catch (InterruptedException e) {
                    _interrupted = true;
                }
            }
        }
    }
}
