package com.meinc.webcollector.message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.meinc.launcher.serverprops.ServerPropertyHolder;
import com.meinc.launcher.serverprops.ServerPropertyHolder.Change;
import com.meinc.launcher.serverprops.ServerPropertyHolder.ChangeListener;
import com.meinc.mrsoa.service.annotation.Service;
import com.meinc.mrsoa.service.annotation.ServiceMethod;
import com.meinc.webcollector.service.WebCollectorJettyHandler;

@Service(name=IWebCollectorMessageBuffer.SERVICE_NAME, interfaces=IWebCollectorMessageBuffer.SERVICE_INTERFACES)
public class WebCollectorMessageBuffer implements IWebCollectorMessageBuffer {
    private static final String RETRY_COUNTER_PROPERTY = "X-RETRY-COUNTER";
    private static final Log log = LogFactory.getLog(WebCollectorMessageBuffer.class);
    private static boolean initted;

    private boolean MESSAGE_PROCESSOR_SHORT_CIRCUIT_FILE_DETECTION;
    private Semaphore addedMessageCount;

    @Value("${web.collector.message.buffer.path}")
    private String messageBufferPathString;
    private File messageBufferPath;
    private File storageBufferPath;

    private Random random = new Random();

    /** Used to quickly determine whether a new message subdirectory needs to be created. These
        subdirectories are used lest too many files exist in a single directory. Note that the
        counts do not need to be perfectly accurate. Thus for performance, no locking is performed
        around accesses to this map. The map is however thread-safe. */
    private ConcurrentHashMap<String,AtomicInteger> messagePathsFileCounts = new ConcurrentHashMap<String,AtomicInteger>();

    private final int MAX_DIRECTORY_SIZE = 3000;

    private ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>();

    public WebCollectorMessageBuffer() {
        ServerPropertyHolder.addPropertyChangeListener("message\\.processor\\.short\\.circuit\\.file\\.detection", new ChangeListener() {
            @Override
            public void propertiesChanged(List<Change> properties) {
                MESSAGE_PROCESSOR_SHORT_CIRCUIT_FILE_DETECTION = Boolean.parseBoolean(ServerPropertyHolder.getProperty("message.processor.short.circuit.file.detection", "false"));
            }
        });
        MESSAGE_PROCESSOR_SHORT_CIRCUIT_FILE_DETECTION = Boolean.parseBoolean(ServerPropertyHolder.getProperty("message.processor.short.circuit.file.detection", "false"));
    }

    @PostConstruct
    public void onPostConstruct() {
        messageBufferPath = new File(messageBufferPathString);
        storageBufferPath = new File("/opt/meinc/collector_storage");
        initMessageBuffer();
    }

    public List<File> getClaimedMessages() {
        final List<File> claimedFiles = new ArrayList<File>();
        visitMessageFiles(new MessageFileVisitor() {
            @Override
            public void onFile(File dir, String name) {
                if (name.endsWith(".claimed") || name.endsWith(".claimed.old"))
                    claimedFiles.add(new File(dir, name));
            }
        });
        return claimedFiles;
    }

    public List<File> getNewMessages() {
        final List<File> newFiles = new ArrayList<File>();
        visitMessageFiles(new MessageFileVisitor() {
            @Override
            public void onFile(File dir, String name) {
                if (name.endsWith(".new"))
                    newFiles.add(new File(dir, name));
            }
        });
        return newFiles;
    }

    public List<File> getErrorMessages() {
        final List<File> errorFiles = new ArrayList<File>();
        visitMessageFiles(new MessageFileVisitor() {
            @Override
            public void onFile(File dir, String name) {
                if (name.endsWith(".error"))
                    errorFiles.add(new File(dir, name));
            }
        });
        return errorFiles;
    }

    private synchronized void initMessageBuffer() {
        if (initted) return;

        log.info("Initializing Message Buffer at " + messageBufferPath.getAbsolutePath());

        // Error message files require human processing, so let a human know they exist
        List<File> errorFiles = getErrorMessages();
        for (File errorFile : errorFiles) {
            log.error("Errored-out message file stuck at " + errorFile.getPath());
        }

        // Claimed message files may or may not have been processed already. We risk processing a message again in order
        // to ensure we never skip a message.
        List<File> claimedFiles = getClaimedMessages();
        for (File claimedFile : claimedFiles) {
            File restoredFile = new File(claimedFile.getAbsolutePath().replaceFirst("\\.claimed(\\.old)?$", ""));
            if (!claimedFile.renameTo(restoredFile))
                log.error("Failed to restore message file " + claimedFile.getPath() + " - skipping");
        }

        // New message files might not be completely written to disk
        List<File> newFiles = getNewMessages();
        //for (File newFile : newFiles) {
        //    if (!newFile.delete())
        //        log.error("Failed to delete message file " + newFile.getPath());
        //}
        for (File newFile : newFiles) {
            File restoredFile = new File(newFile.getAbsolutePath().replaceFirst("\\.new$", ""));
            if (!newFile.renameTo(restoredFile))
                log.error("Failed to restore message file " + newFile.getPath() + " - skipping");
        }

        deleteEmptyMessagePaths();

        initMessagePathsFileCounts();

        initted = true;
    }

    private void deleteEmptyMessagePaths() {
        List<File> existingMessagePaths = getExistingMessagePaths();
        //reverse numeric sort
        Collections.sort(existingMessagePaths, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                Integer i2 = Integer.parseInt(o2.getName());
                Integer i1 = Integer.parseInt(o1.getName());
                return i2.compareTo(i1);
            }
        });
        for (File existingMessagePath : existingMessagePaths)
            if (!existingMessagePath.delete())
                break;
    }

    private void initMessagePathsFileCounts() {
        int totalMessageCount = 0;
        List<File> existingMessagePaths = getExistingMessagePaths();
        for (File existingMessagePath : existingMessagePaths) {
            int fileCount = 0;
            String[] files = existingMessagePath.list();
            for (String file : files)
                if (!file.endsWith(".error") && !file.endsWith(".new") && !file.endsWith(".old") && !file.endsWith(".claimed"))
                    fileCount += 1;
            messagePathsFileCounts.put(existingMessagePath.getName(), new AtomicInteger(fileCount));
            totalMessageCount += fileCount;
            log.info("Found " + fileCount + " messages in collector message path " + existingMessagePath.getName());
        }
        addedMessageCount = new Semaphore(totalMessageCount);
    }

    private Kryo getKryo() {
        Kryo kryo = kryoThreadLocal.get();
        if (kryo == null) {
            kryo = new Kryo();
            kryo.setReferences(false);
            kryo.register(CollectorMessage.class, 1024);
            kryoThreadLocal.set(kryo);
        }
        return kryo;
    }

    public boolean addToBuffer(CollectorMessage newMessage) {
        if (newMessage.getClass() != CollectorMessage.class)
            throw new IllegalArgumentException("Only genuine "+CollectorMessage.class.getName()+" classes may be saved to the buffer");
        String newMessageFilename = newMessage.getMessageId()+"."+newMessage.getMessageType()+".msg.new";

//unless this becomes a bug, don't bother printing. it's just duplicated in a moment by MessageProcessorDaemon
//        if (log.isDebugEnabled())
//            log.debug(String.format("Saving message %s: %s", newMessageFilename, newMessage));

        File newMessagePath = getNewMessagePath(newMessage.getMessageType());
        if (newMessagePath == null)
            return false;

        File newMessageFile = new File(newMessagePath, newMessageFilename);
        FileOutputStream newMessageFos;
        try {
            newMessageFos = new FileOutputStream(newMessageFile);
        } catch (FileNotFoundException e) {
            log.error("Could not write message to buffer", e);
            return false;
        }

        Kryo kryo = kryoThreadLocal.get();
        if (kryo == null) {
            kryo = new Kryo();
            kryo.setReferences(false);
            kryo.register(CollectorMessage.class, 1024);
            kryoThreadLocal.set(kryo);
        }

        Output kryoOutput = new Output(newMessageFos);
        kryo.writeObject(kryoOutput, newMessage);
        kryoOutput.close();

        File readyMessageFile = new File(newMessageFile.getAbsolutePath().replaceFirst("\\.new$", ""));
        if (!newMessageFile.renameTo(readyMessageFile))
            log.warn("Could not rename " + newMessageFile.getAbsolutePath());

        AtomicInteger messagePathFileCount = messagePathsFileCounts.get(newMessageFile.getParentFile().getName());
        if (messagePathFileCount == null)
            log.warn("Collector message path " + newMessageFile.getParentFile().getName() + " is missing file count");
        else
            messagePathFileCount.incrementAndGet();

        if (MESSAGE_PROCESSOR_SHORT_CIRCUIT_FILE_DETECTION)
            addedMessageCount.release();

        return true;
    }

    private File getRootBufferPath(String messageType) {
        return (messageType != null && "NO_VIRTUAL_SEAT".equals(messageType)) ? storageBufferPath : messageBufferPath;
    }

    private File getNewMessagePath(String messageType) {
        List<File> existingMessagePaths = getExistingMessagePaths();
        for (File existingMessagePath : existingMessagePaths) {
            AtomicInteger existingMessagePathFileCount = messagePathsFileCounts.get(existingMessagePath.getName());
            if (existingMessagePathFileCount == null) {
                log.warn("Collector message path " + existingMessagePath.getName() + " is missing file count");
                continue;
            }
            if (existingMessagePathFileCount.get() < MAX_DIRECTORY_SIZE)
                return existingMessagePath;
        }

        File rootPath = getRootBufferPath(messageType);
        File newMessagePath = new File(rootPath, ""+(existingMessagePaths.size()+1));
        messagePathsFileCounts.put(newMessagePath.getName(), new AtomicInteger());
        newMessagePath.mkdirs();
        if (!newMessagePath.exists()) {
            log.error("Could not create message buffer directory " + newMessagePath.getAbsolutePath());
            messagePathsFileCounts.remove(newMessagePath.getName());
            return null;
        }
        return newMessagePath;
    }

    private List<File> getExistingMessagePaths() {
        List<File> existingMessagePaths = new ArrayList<File>();
        File rootPath = getRootBufferPath(null);
        Integer directoryCounter = 1;
        File messagePath = new File(rootPath, directoryCounter.toString());
        while (messagePath.exists()) {
            if (!messagePath.isDirectory()) {
                log.error("Path must be a directory: " + messagePath.getAbsolutePath());
                return null;
            }
            existingMessagePaths.add(messagePath);
            messagePath = new File(rootPath, (++directoryCounter).toString());
        }

        if (existingMessagePaths.size() > 1) {
            // Rotate order so that message paths are accessed to prevent orphaned messages
            int randomIndex = random.nextInt(existingMessagePaths.size());
            List<File> rotatedMessagePaths = new ArrayList<File>(existingMessagePaths.size());
            rotatedMessagePaths.addAll(existingMessagePaths.subList(randomIndex, existingMessagePaths.size()));
            rotatedMessagePaths.addAll(existingMessagePaths.subList(0, randomIndex));
        }

        return existingMessagePaths;
    }

    private static abstract class MessageFileVisitor implements FilenameFilter {
        protected int filterClaimedCount;
        protected int filterMaxClaimCount;
        protected boolean isFinished;

        public MessageFileVisitor() { }

        public MessageFileVisitor(int filterMaxClaimCount) {
            this.filterMaxClaimCount = filterMaxClaimCount;
        }

        @Override
        public final boolean accept(File dir, String name) {
            if (!isFinished)
                onFile(dir, name);
            return false;
        }

        public abstract void onFile(File dir, String name);
    }

    public void visitMessageFiles(MessageFileVisitor messageFilenameFilter) {
        List<File> existingMessagePaths = getExistingMessagePaths();
        if (existingMessagePaths != null)
            for (File existingMessagePath : existingMessagePaths) {
                existingMessagePath.listFiles(messageFilenameFilter);
                if (messageFilenameFilter.isFinished)
                    break;
            }
    }

    /**
     * @param minCount
     * @param maxCount
     * @return any returned list item may be null if there was a problem with the corresponding message and it was never claimed
     */
    public List<CollectorMessage> claimFromBuffer(int minCount, final int maxCount) {
        final List<File> totalClaimedMessageFiles = new ArrayList<File>();
        visitMessageFiles(new MessageFileVisitor(maxCount-totalClaimedMessageFiles.size()) {
            private String filterTypeString;
            @Override
            public void onFile(File dir, String name) {
                if (!name.endsWith(".msg"))
                    return;
                String[] nameParts = name.split("\\.");
                if (nameParts.length < 3) {
                    log.error("Illegal message filename: " + new File(dir,name));
                    return;
                }
                String messageTypeString = nameParts[nameParts.length-2];
                if (filterTypeString == null)
                    filterTypeString = messageTypeString;
                if (filterClaimedCount >= filterMaxClaimCount)
                    isFinished = true;
                else if (messageTypeString.equals(filterTypeString)) {
                    File readyFile = new File(dir, name);
                    File claimedFile = new File(dir, name+".claimed");
                    if (readyFile.renameTo(claimedFile)) {
                        filterClaimedCount += 1;
                        totalClaimedMessageFiles.add(claimedFile);
                    }
                }
            }
        });

        if (totalClaimedMessageFiles.size() < minCount) {
            for (File claimedFile : totalClaimedMessageFiles) {
                try {
                    unclaimFile(claimedFile);
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
            return null;
        }

        Kryo kryo = getKryo();
        List<CollectorMessage> claimedMessages = new ArrayList<CollectorMessage>();
        for (File claimedMessageFile : totalClaimedMessageFiles) {
            FileInputStream claimedMessageFis = null;
            try {
                claimedMessageFis = new FileInputStream(claimedMessageFile);
            } catch (FileNotFoundException e) {
                log.error(String.format("Claimed message file %s disappeared - skipping", claimedMessageFile.getAbsolutePath()));
            }
            CollectorMessage message = null;
            if (claimedMessageFile != null) {
                Input input = new Input(claimedMessageFis);
                try {
                    message = kryo.readObject(input, CollectorMessage.class);
                } catch (KryoException e) {
                    log.error("Error reading message " + claimedMessageFile.getAbsolutePath() + " - marking message as error: " + e.getMessage());
                    try {
                        errorOutFile(claimedMessageFile);
                    } catch (IOException e2) {
                        log.error(e2.getMessage());
                    }
                } finally {
                    input.close();
                }
            }

            InternalMessage internalMessage = null;
            if (message != null) {
                internalMessage = new InternalMessage(message);
                internalMessage.messageFile = claimedMessageFile;
            }

            claimedMessages.add(internalMessage);
        }

        return claimedMessages;
    }

    public void unclaimMessages(List<CollectorMessage> messageList) {
        for (CollectorMessage message : messageList) {
            if (message != null) {
                if (!(message instanceof InternalMessage))
                    // should normally never happen
                    throw new IllegalArgumentException("Message list must originate from claimFromBuffer(int, int)");
                InternalMessage internalMessage = (InternalMessage) message;
                if (internalMessage.messageFile != null && internalMessage.messageFile.exists()) {
                    try {
                        log.info("--] Unclaiming " + message.getMessageType() + " message " + message.getMessageId());
                        unclaimMessage(message);
                    } catch (IOException e) {
                        log.error("Could not unclaim message: " + e.getMessage(), e);
                    }
                }
            }
        }
    }

    public void unclaimMessage(CollectorMessage message) throws IOException {
        if (!(message instanceof InternalMessage))
            throw new IllegalArgumentException("Message list must originate from claimFromBuffer(int, int)");

        InternalMessage internalMessage = (InternalMessage) message;
        File claimedFile = internalMessage.messageFile;
        unclaimFile(claimedFile);
    }

    public void unclaimFile(File claimedFile) throws IOException {
        if (claimedFile == null || !claimedFile.exists() || !claimedFile.getPath().endsWith(".claimed"))
            throw new IllegalArgumentException("Cannot unclaim a message that is not claimed");

        File unclaimedFile = new File(claimedFile.getPath().replaceFirst("\\.claimed$", ""));
        if (!claimedFile.renameTo(unclaimedFile))
            throw new IOException("Could not unclaim message file " + claimedFile.getAbsolutePath());

        if (MESSAGE_PROCESSOR_SHORT_CIRCUIT_FILE_DETECTION)
            addedMessageCount.release();
    }

    private void errorOutFile(File problemFile) throws IOException {
        if (problemFile == null || !problemFile.exists() || problemFile.getPath().endsWith(".error"))
            throw new IllegalArgumentException("Cannot error-out message " + problemFile.getAbsolutePath());

        File errorFile = new File(problemFile.getPath() + ".error");
        if (!problemFile.renameTo(errorFile))
            throw new IOException("Could not error-out message file " + problemFile.getAbsolutePath());
    }

    /* (non-Javadoc)
     * @see com.meinc.webcollector.message.IMessageRemover#flushFromBuffer(java.util.List)
     */
    @Override
    public void removeMessages(List<CollectorMessage> messages, boolean storeProcessingTime) {
        for (CollectorMessage message : messages)
            removeMessage(message, storeProcessingTime);
    }

    /* (non-Javadoc)
     * @see com.meinc.webcollector.message.IMessageRemover#flushFromBuffer(com.meinc.webcollector.message.Message)
     */
    @Override
    public void removeMessage(CollectorMessage message, boolean storeProcessingTime) {
        if (!(message instanceof InternalMessage))
            throw new IllegalArgumentException("Message list must originate from claimFromBuffer(int, int)");
        InternalMessage internalMessage = (InternalMessage) message;
        if (!internalMessage.messageFile.delete()) {
            // Not an error if the message was retried as that operation removes the message
            if (internalMessage.messageFile.exists() || internalMessage.getProperty(RETRY_COUNTER_PROPERTY) == null)
                log.error("Could not delete message " + internalMessage.messageFile.getAbsolutePath());
        }

        AtomicInteger messagePathFileCount = messagePathsFileCounts.get(internalMessage.messageFile.getParentFile().getName());
        if (messagePathFileCount == null)
            log.warn("Collector message path " + internalMessage.messageFile.getParentFile().getName() + " is missing file count - ignoring");
        else
            messagePathFileCount.decrementAndGet();

        //dump total processing time for this message into the data collector
        if (storeProcessingTime) {
            long duration = System.currentTimeMillis() - message.getTimestamp();
            WebCollectorJettyHandler.addMessageProcessingTime(message, duration);
        }
    }

    @Override
    public boolean retryMessage(CollectorMessage message, int maxRetries) {
        if (!(message instanceof InternalMessage))
            throw new IllegalArgumentException("Message must originate from claimFromBuffer(int, int)");
        InternalMessage internalMessage = (InternalMessage) message;
        if (!internalMessage.messageFile.getName().endsWith(".claimed"))
            throw new IllegalArgumentException("Message must be claimed before retrying");
        String retryCounterString = internalMessage.getProperty(RETRY_COUNTER_PROPERTY);
        if (retryCounterString == null || retryCounterString.trim().isEmpty()) {
            log.info("Resetting retry counter for message " + message.getMessageId());
            retryCounterString = "0";
        }
        int retryCounter;
        try {
            retryCounter = Integer.parseInt(retryCounterString);
        } catch (NumberFormatException e) {
            log.error(String.format("Invalid retry counter '%s' for message %s", retryCounterString, message.getMessageId()));
            retryCounter = 0;
        }
        if (retryCounter >= maxRetries) {
            log.info(String.format("Reached max retries of %d for message %s", maxRetries, message.getMessageId()));
            return false;
        } else {
            log.info(String.format("Saving message %s for retry %d of %d", message.getMessageId(), retryCounter+1, maxRetries));
            File oldMessageFile = new File(internalMessage.messageFile.getPath() + ".old");
            if (!internalMessage.messageFile.renameTo(oldMessageFile)) {
                log.error(String.format("Failed to rename old claimed message %s", oldMessageFile.getName()));
                return false;
            } else {
                message.addProperty(RETRY_COUNTER_PROPERTY, (retryCounter+1)+"");
                CollectorMessage collectorMessage = new CollectorMessage(internalMessage);
                if (!addToBuffer(collectorMessage)) {
                    log.error(String.format("Failed to add retry message %s back to buffer", message.getMessageId()));
                    return false;
                }
                if (!oldMessageFile.delete())
                    log.error(String.format("Failed to delete old claimed message %s", oldMessageFile.getName()));
            }
        }
        return true;
    }

    private final int messageBufferSecret = 829761505;

    /**
     * @param secret The secret is meant to keep well-intentioned developers from using this service method
     */
    @ServiceMethod
    public void removeMessages(int secret, List<CollectorMessage> messages, boolean storeProcessingTime) {
        if (secret != messageBufferSecret)
            throw new IllegalArgumentException();
        removeMessages(messages, storeProcessingTime);
    }

    /**
     * @param secret The secret is meant to keep well-intentioned developers from using this service method
     */
    @ServiceMethod
    public void removeMessage(int secret, CollectorMessage message, boolean storeProcessingTime) {
        if (secret != messageBufferSecret)
            throw new IllegalArgumentException();
        removeMessage(message, storeProcessingTime);
    }

    /**
     * @param secret The secret is meant to keep well-intentioned developers from using this service method
     * @return
     */
    @ServiceMethod
    public boolean retryMessage(int secret, CollectorMessage message, int maxRetries) {
        if (secret != messageBufferSecret)
            throw new IllegalArgumentException();
        return retryMessage(message, maxRetries);
    }

    public int getSecret() {
        return messageBufferSecret;
    }

    public Semaphore getAddedMessageCount() {
        return addedMessageCount;
    }
}
