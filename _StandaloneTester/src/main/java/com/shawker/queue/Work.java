package com.shawker.queue;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import com.shawker.queue.AsyncMessageHandler.WorkRunnable;


public class Work implements Serializable, Cloneable
{
    private static final long serialVersionUID = 1L;

    private static AtomicInteger globalId = new AtomicInteger();

    private int id;
    /**
     * A unique key that represents this piece of work.  This is different from the transactionId in that the same
     * work will result in the same key (i.e. subscriber 10 using provider 'a' updates note 'x')
     */
    private String key;

    private int subscriberId;

    /**
     * The work to execute
     */
    private WorkRunnable job;

    /**
     * In the event of a failure, this contains all the information needed by the client to track this async work.
     */
    private AsyncError transactionResponse;

    public Work(String key, int subscriberId, WorkRunnable job, AsyncError transactionResponse)
    {
        if (key == null) throw new IllegalArgumentException("key is required");
        if (job == null) throw new IllegalArgumentException("job is required");
        if (transactionResponse == null) throw new IllegalArgumentException("transactionResponse is required");
        this.id = globalId.incrementAndGet();
        this.key = key;
        this.subscriberId = subscriberId;
        this.job = job;
        this.transactionResponse = transactionResponse;
    }

    public int getId() {
        return id;
    }

    public String getKey()
    {
        return key;
    }

    void setJob(WorkRunnable job) {
        this.job = job;
    }

    public WorkRunnable getJob()
    {
        return job;
    }

    public AsyncError getTransactionResponse()
    {
        return transactionResponse;
    }

    public int getSubscriberId()
    {
        return subscriberId;
    }

    @Override
    protected Work clone() {
        try {
            return (Work) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
