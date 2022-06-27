package com.meinc.commons.encryption;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meinc.launcher.serverprops.ServerPropertyHolder;

public class RandomUuidPool extends Thread {
    private static final Log log = LogFactory.getLog(RandomUuidPool.class);
    
    private static RandomUuidPool singleton = new RandomUuidPool();
    
    public static RandomUuidPool getInstance() {
        return singleton;
    }

    private ArrayBlockingQueue<long[]> uuidPool;
    private AtomicLong logCounter = new AtomicLong();
    
    private RandomUuidPool() {
        super("Global Random UUID Pool");
        Integer poolSizeMb = Integer.parseInt(ServerPropertyHolder.getProperty("uuid.pool.size.mb", "100"));
        int poolSize = poolSizeMb * (1000*1000) / (128/8);  //(MB -> B) / size-of-uuid
        log.info(String.format("Initializing Random UUID pool to size %dMB", poolSizeMb));
        uuidPool = new ArrayBlockingQueue<long[]>(poolSize, true);

        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                UUID randomUuid = UUID.randomUUID();
                long[] randomBitsArray = new long[] { randomUuid.getMostSignificantBits(),
                                                      randomUuid.getLeastSignificantBits() };
                uuidPool.put(randomBitsArray);
            } catch (InterruptedException e) {
                interrupt();
                continue;
            }
        }
    }
    
    public String getRandomUuid() throws InterruptedException {
        long[] randomBitsArray = uuidPool.take();

        if (logCounter.incrementAndGet() % 10000 == 0) {
            int currentPoolSize = uuidPool.size();
            int currentPoolUtilization = 100 * currentPoolSize / (currentPoolSize + uuidPool.remainingCapacity());
            if (currentPoolUtilization < 90)
                log.warn(String.format("Random UUID Pool size at %d%%!", currentPoolUtilization));
        }

        UUID randomUuid = new UUID(randomBitsArray[0], randomBitsArray[1]);
        return randomUuid.toString();
    }
}
