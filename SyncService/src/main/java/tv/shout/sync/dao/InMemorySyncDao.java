package tv.shout.sync.dao;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import tv.shout.sync.domain.SyncMessage;

public class InMemorySyncDao
implements ISyncServiceDao
{
    private static Logger _logger = Logger.getLogger(InMemorySyncDao.class);
    private static final String DB_NAME = "db_mock_sync";

    private List<SyncMessage> _syncMessagesList;

    private Lock _syncMessagesListLock = new ReentrantLock();

    public InMemorySyncDao()
    {
_logger.info("SyncService: creating the in memory sync dao");
        //see if there is already an instance of the db saved to disk. if so, use it
        String mrSoaHomeDir = System.getProperty("mrsoa.home");
        if (mrSoaHomeDir != null) {
            File dbParentDir = new File(mrSoaHomeDir, DB_NAME);
            if (dbParentDir.exists()) {
                File sync = new File(dbParentDir, "sync.db");

                if (sync.exists()) {
                    try {
                        _syncMessagesList = readFromFile(sync);

                    } catch (ClassNotFoundException | IOException e) {
                        _logger.debug("using default "+DB_NAME+": exception wile loading mockdb file", e);
                        initNewDb();
                    }
                } else {
                    _logger.debug("using default "+DB_NAME+": missing one or more mockdb files");
                    initNewDb();
                }
            } else {
                _logger.debug("using default "+DB_NAME+": "+DB_NAME+" subdir doesn't exist");
                initNewDb();
            }
        } else {
            _logger.debug("using default "+DB_NAME+": mrsoa.home system property not found");
            initNewDb();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> readFromFile(File file)
    throws IOException, ClassNotFoundException
    {
        FileInputStream fis = new FileInputStream(file);
        try (ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (List<T>) ois.readObject();
        }
    }

    private void persistDb()
    {
        //do we know where to store the files
        String mrSoaHomeDir = System.getProperty("mrsoa.home");
        if (mrSoaHomeDir == null) {
            _logger.debug("unable to persist "+DB_NAME+": mrsoa.home system property not found");
            return;
        }

        //does the subdir exist (or if not, can we create it)
        File dbParentDir = new File(mrSoaHomeDir, DB_NAME);
        if (!dbParentDir.exists()) {
            boolean parentCreated = dbParentDir.mkdirs();
            if (!parentCreated) {
                _logger.debug("unable to persist "+DB_NAME+": unable to create syncmockdb subdir");
                return;
            }
        }

        File sync = new File(dbParentDir, "sync.db");

        try {
            _syncMessagesListLock.lock();

            writeToFile(sync, _syncMessagesList);

        } catch (IOException e) {
            _logger.debug("unable to persist "+DB_NAME+": unable to write one or more files", e);

        } finally {
            _syncMessagesListLock.unlock();
        }
    }

    private <T> void writeToFile(File file, List<T> list)
    throws IOException
    {
        FileOutputStream fos = new FileOutputStream(file);
        try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(list);
        }
    }

    private void initNewDb()
    {
        _syncMessagesList = new ArrayList<>();
    }

    @Override
    public void insertSyncMessage(SyncMessage message)
    {
        _syncMessagesListLock.lock();
        try {
_logger.info(MessageFormat.format("*** writing {0} to: {1,number,#}", message.getMessageType(), message.getSubscriberId()));
            _syncMessagesList.add(message);
        } catch (Throwable t) {
_logger.error("error while inserting sync message", t);
        } finally {
            _syncMessagesListLock.unlock();
        }

        persistDb();

        //return message;
    }

    @Override
    public List<SyncMessage> getSyncMessagesWithContext(String contextualId, long subscriberId, Date since)
    {
        _syncMessagesListLock.lock();
        try {
            return _syncMessagesList.stream()
                    .filter(m -> m.getContextualId().equals(contextualId))
                    .filter(m -> m.getSubscriberId() == subscriberId)
                    .filter(m -> m.getCreateDate().after(since))
                    .sorted(Comparator.comparing( SyncMessage::getCreateDate, Comparator.naturalOrder() ))
                    .collect(Collectors.toList());
        } finally {
            _syncMessagesListLock.unlock();
        }
    }

    @Override
    public List<SyncMessage> getSyncMessages(long subscriberId, Date since)
    {
        _syncMessagesListLock.lock();
        try {
            return _syncMessagesList.stream()
                    .filter(m -> m.getSubscriberId() == subscriberId)
                    .filter(m -> m.getCreateDate().after(since))
                    .sorted(Comparator.comparing( SyncMessage::getCreateDate, Comparator.naturalOrder() ))
                    .collect(Collectors.toList());
        } finally {
            _syncMessagesListLock.unlock();
        }
    }

}
