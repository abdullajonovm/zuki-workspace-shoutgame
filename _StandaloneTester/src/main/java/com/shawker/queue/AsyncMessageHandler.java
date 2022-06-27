package com.shawker.queue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AsyncMessageHandler
{
    private BlockingQueue<String> workActionQueue = new LinkedBlockingQueue<>();
    private WorkActionQueueConsumer workActionQueueConsumer = new WorkActionQueueConsumer();
    private Thread workActionQueueConsumerThread = new Thread(workActionQueueConsumer);

    public static abstract class WorkRunnable implements Runnable { }

    private static class WorkAction
    {
        public enum TYPE { NEW, FINISHED, CHECK }
        public TYPE type;
        public int subscriberId;
        public Work newWork;

        public WorkAction(TYPE type, int subscriberId, Work newWork)
        {
            this.type = type;
            this.subscriberId = subscriberId;
            this.newWork = newWork;
        }
    }

    private class WorkActionQueueConsumer
    implements Runnable
    {
        @Override
        public void run()
        {
            try {
                while (true) {
                    System.out.println("Processing: " + workActionQueue.take()); //take will block until an item becomes available
                    Thread.sleep(1500L);
                }
            } catch (InterruptedException e) {
            }

            System.out.println("CONSUMER HAS SHUT DOWN");
        }

    }

    private void start()
    throws InterruptedException
    {
        workActionQueueConsumerThread = new Thread(workActionQueueConsumer);
        workActionQueueConsumerThread.start();

        String input = getConsoleInput("work: ");
        while (input != null && input.length() > 0) {
            workActionQueue.put(input);
            input = getConsoleInput("work: ");
        }

        workActionQueueConsumerThread.interrupt();
    }

    private void stop()
    {
        workActionQueueConsumerThread.interrupt();
    }

    public static String getConsoleInput(String message)
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(message);
        try {
            return br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args)
    {
        AsyncMessageHandler e = new AsyncMessageHandler();
        try {
            e.start();
        } catch (InterruptedException e1) {
        } finally {
            e.stop();
        }
    }
}
