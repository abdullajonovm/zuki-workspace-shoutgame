package com.meinc.jdbc.effect;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class TransactionSideEffectManager {
    private static final Log log = LogFactory.getLog(TransactionSideEffectManager.class);
    
    // Stores side-effect lists for a thread. Normally a thread has only a single side-effect list because a connection
    // is shared between services and somewhat firewalled by JDBC savepoints. However it is possible that a service will
    // grab a new connection, so we use a queue to track all connections for a thread.
    private static ThreadLocal<Deque<TransactionSideEffectList>> sideEffects = new TransactionSideEffectThreadLocal<Deque<TransactionSideEffectList>>();
    
    public static void runAfterThisTransactionCommit(Runnable task) {
        getSideEffectList().addSideEffect(new ServiceTask<Object>(task));
    }
    
    public static void runAfterThisTransactionCommit(ServiceTask<?> task) {
        getSideEffectList().addSideEffect(task);
    }
    
    /**
     * @return false if no current connection exists, or if no transaction exists on the current transaction, or if an error occurs while
     *         checking, true otherwise
     */
    public static boolean currentConnectionHasTransaction() {
        Deque<TransactionSideEffectList> sideEffectQueue = getSideEffectQueue();
        if (sideEffectQueue.isEmpty())
            return false;
        try {
            return !sideEffectQueue.getLast().getConnection().getAutoCommit();
        } catch (SQLException e) {
            log.error("Error checking connection for transaction", e);
            return false;
        }
    }

    public static boolean currentConnectionHasActiveSavepoint() {
        return !getSideEffectList().isEmpty();
    }
    
    private static Deque<TransactionSideEffectList> getSideEffectQueue() {
        Deque<TransactionSideEffectList> sideEffectQueue = sideEffects.get();
        if (sideEffectQueue == null) {
            sideEffectQueue = new LinkedList<TransactionSideEffectList>();
            sideEffects.set(sideEffectQueue);
        }
        return sideEffectQueue;
    }

    private static TransactionSideEffectList getSideEffectList() {
        Deque<TransactionSideEffectList> sideEffectQueue = getSideEffectQueue();
        TransactionSideEffectList sideEffectList = sideEffectQueue.getLast();
        if (sideEffectList == null)
            throw new IllegalStateException("Missing connection side effect list");
        return sideEffectList;
    }

    static void newConnection(Connection connection) {
        Deque<TransactionSideEffectList> sideEffectQueue = getSideEffectQueue();
        TransactionSideEffectList sideEffectList = new TransactionSideEffectList(connection);
        sideEffectQueue.add(sideEffectList);
    }

    static void closeConnection(Connection connection) {
        Deque<TransactionSideEffectList> queue = getSideEffectQueue();

        while (!queue.isEmpty() && queue.getLast().getConnection() == connection) {
            TransactionSideEffectList sideEffectList = queue.getLast();
            if (!sideEffectList.isEmpty())
                log.warn("THROWING AWAY SIDE EFFECTS - SIDE EFFECTS ADDED TO CONNECTION WITHOUT TRANSACTION? OR CONNECTION CLOSED WITHOUT COMMIT?\n" + sideEffectList);
            queue.removeLast();
        }
    }

    static void addSavepoint() {
        getSideEffectList().addSavepoint();
    }
    
    static void releaseSavepoint() {
        getSideEffectList().releaseSavepoint();
    }
    
    static void rollbackToSavepoint() {
        getSideEffectList().rollbackToSavepoint();
    }
    
    static void commit() {
        getSideEffectList().commit();
        getSideEffectQueue().removeLast();
    }
    
    static void rollback() {
        getSideEffectQueue().removeLast();
    }
    
    private static class TransactionSideEffectThreadLocal<T> extends InheritableThreadLocal<T> {
        @Override
        protected T childValue(T parentValue) {
            // Thread doesn't inherit its parent's side effects
            return null;
        }
    }

    private static class TransactionSideEffectList {
        private Connection connection;
        // The inner list stores individual side effects, the outer list represents savepoints
        private List<List<ServiceTask<?>>> sideEffects = new ArrayList<List<ServiceTask<?>>>();
        private int index;
        
        public TransactionSideEffectList(Connection connection) {
            this.connection = connection;
            clear();
        }
        
        public Connection getConnection() {
            return connection;
        }

        private void growToIndex() {
            for (int i = sideEffects.size(); i <= index; i++)
                sideEffects.add(null);
        }

        public void addSideEffect(ServiceTask<?> sideEffect) {
            List<ServiceTask<?>> indexEffects = sideEffects.get(index);
            if (indexEffects == null) {
                indexEffects = new ArrayList<ServiceTask<?>>();
                sideEffects.set(index, indexEffects);
            }
            indexEffects.add(sideEffect);
        }
        
        public void addSavepoint() {
            index += 1;
            growToIndex();
        }
        
        public void releaseSavepoint() {
            if (--index < 0)
                throw new IllegalStateException("Released too many savepoints");
        }
        
        public void rollbackToSavepoint() {
            for (int i = sideEffects.size()-1; i >= index; i--)
                sideEffects.remove(i);
            releaseSavepoint();
        }
        
        public void commit() {
            ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                // Note that a side effect can produce its own side effects
                //for (List<ServiceTask<?>> indexEffects : sideEffects) {
                for (int i = 0; i < sideEffects.size(); i++) {
                    List<ServiceTask<?>> indexEffects = sideEffects.get(i);
                    if (indexEffects != null)
                        //for (ServiceTask<?> sideEffect : indexEffects) {
                        for (int j = 0; j < indexEffects.size(); j++) {
                            ServiceTask<?> sideEffect = indexEffects.get(j);
                            try {
                                Thread.currentThread().setContextClassLoader(sideEffect.getClassLoader());
                                sideEffect.run();
                                sideEffect.get();
                            } catch (Throwable t) {
                                if (t instanceof ExecutionException)
                                    t = t.getCause();
                                log.error("Error running transaction side effect: " + t.getMessage(), t);
                            }
                        }
                }
                clear();
            } finally {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }
        }
        
        public void clear() {
            index = 0;
            sideEffects.clear();
            growToIndex();
        }
        
        public boolean isEmpty() {
            return index == 0;
        }

        @Override
        public String toString() {
            StringBuffer s = new StringBuffer();
            s.append("SideEffectList(cx="+connection+")[");
            for (List<ServiceTask<?>> taskList : sideEffects) {
                if (taskList == null)
                    s.append("{null},");
                else if (!taskList.isEmpty()) {
                    s.append("{");
                    for (ServiceTask<?> task : taskList) {
                        s.append(task.getClass().getName());
                        s.append(",");
                    }
                    s.append("},");
                }
            }
            s.append("]");
            return s.toString();
        }
    }
}
