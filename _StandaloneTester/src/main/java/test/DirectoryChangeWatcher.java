package test;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.CountDownLatch;

//https://docs.oracle.com/javase/tutorial/essential/io/notification.html
public class DirectoryChangeWatcher
{
    private WatchService _watcher;

    public DirectoryChangeWatcher(String watchDir)
    throws IOException
    {
        _watcher = FileSystems.getDefault().newWatchService();

        Path dir = Paths.get(watchDir);
        /*WatchKey key = */dir.register(_watcher, StandardWatchEventKinds.ENTRY_CREATE);

        CountDownLatch cdl = new CountDownLatch(1);
        ChangeWatchListener listener = new ChangeWatchListener(cdl);
        listener.start();

        try {
            cdl.await();
        } catch (InterruptedException e) {
        }
    }

    private class ChangeWatchListener
    extends Thread
    {
        private boolean _running;
        private CountDownLatch _cdl;

        private ChangeWatchListener(CountDownLatch cdl)
        {
            _running = true;
            _cdl = cdl;
        }

        @Override
        public void run()
        {
            while (_running) {
                WatchKey key;
                try {
                    //wait for an event to show up
                    key = _watcher.take();

                    //loop each event
                    for (WatchEvent<?> event: key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        //overflow can always be sent, even if not explicitly listened for
                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;

                        } else {
                            //since this is only listening for StandardWatchEventKinds.ENTRY_CREATE, we know what the type is
                            @SuppressWarnings("unchecked")
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path filename = ev.context();

                            //TODO: do something interesting
                            System.out.println("new file: " + filename);
                        }
                    }

                    //the key must be reset or no further events will be sent
                    boolean valid = key.reset();
                    if (!valid) {
                        //not valid means the directory being watched is no longer accessible
                        _running = false;
                    }

                } catch (InterruptedException e) {
                    _running = false;
                }
            }

            _cdl.countDown();
        }
    }

    public static void main(String[] args)
    throws IOException
    {
        new DirectoryChangeWatcher("/Users/shawker/temp");
    }

}
