package com.meinc.jdbc.effect;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestSideEffects {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCommitSingleThread() {
        @SuppressWarnings({ "resource", "unused" })
        SideEffectConnection sec = new SideEffectConnection(null);
        int iterations = 100;
        final AtomicInteger ai = new AtomicInteger();
        for (int i = 0; i < iterations; i++) {
            TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
                @Override
                public void run() {
                    ai.incrementAndGet();
                }
            });
            TransactionSideEffectManager.addSavepoint();
        }

        assertEquals(0, ai.get());

        for (int i = iterations; i > 0; i--) {
            TransactionSideEffectManager.releaseSavepoint();
            assertEquals(0, ai.get());
        }

        TransactionSideEffectManager.commit();
        assertEquals(iterations, ai.get());
    }

    @Test
    public void testRollbackSingleThread() {
        @SuppressWarnings({ "resource", "unused" })
        SideEffectConnection sec = new SideEffectConnection(null);
        int iterations = 100;
        final AtomicInteger ai = new AtomicInteger();
        for (int i = 0; i < iterations; i++) {
            TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
                @Override
                public void run() {
                    ai.incrementAndGet();
                }
            });
            TransactionSideEffectManager.addSavepoint();
        }

        assertEquals(0, ai.get());

        for (int i = iterations; i > 0; i--) {
            TransactionSideEffectManager.rollbackToSavepoint();
            assertEquals(0, ai.get());
        }

        TransactionSideEffectManager.rollback();
        assertEquals(0, ai.get());
    }

    @Test
    public void testMixedCommitSingleThread() {
        @SuppressWarnings({ "resource", "unused" })
        SideEffectConnection sec = new SideEffectConnection(null);
        int iterations = 6;
        final AtomicInteger ai = new AtomicInteger();
        for (int i = 0; i < iterations; i++) {
            TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
                @Override
                public void run() {
                    ai.incrementAndGet();
                }
            });
            TransactionSideEffectManager.addSavepoint();
        }

        assertEquals(0, ai.get());

        for (int i = iterations; i > 0; i--) {
            if (i % 2 == 0)
                TransactionSideEffectManager.rollbackToSavepoint();
            else
                TransactionSideEffectManager.releaseSavepoint();
            assertEquals(0, ai.get());
        }

        TransactionSideEffectManager.commit();
        assertEquals(2, ai.get());
    }

    @Test
    public void testMixedRollbackSingleThread() {
        @SuppressWarnings({ "resource", "unused" })
        SideEffectConnection sec = new SideEffectConnection(null);
        int iterations = 100;
        final AtomicInteger ai = new AtomicInteger();
        for (int i = 0; i < iterations; i++) {
            TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
                @Override
                public void run() {
                    ai.incrementAndGet();
                }
            });
            TransactionSideEffectManager.addSavepoint();
        }

        assertEquals(0, ai.get());

        for (int i = iterations; i > 0; i--) {
            if (i % 2 == 0)
                TransactionSideEffectManager.rollbackToSavepoint();
            else
                TransactionSideEffectManager.releaseSavepoint();
            assertEquals(0, ai.get());
        }

        TransactionSideEffectManager.rollback();
        assertEquals(0, ai.get());
    }

    @Test
    public void testRollbackFinalSingleThread() {
        @SuppressWarnings({ "resource", "unused" })
        SideEffectConnection sec = new SideEffectConnection(null);
        int iterations = 100;
        final AtomicInteger ai = new AtomicInteger();
        for (int i = 0; i < iterations; i++) {
            TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
                @Override
                public void run() {
                    ai.incrementAndGet();
                }
            });
            TransactionSideEffectManager.addSavepoint();
        }
        TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
            @Override
            public void run() {
                ai.incrementAndGet();
            }
        });

        assertEquals(0, ai.get());
        TransactionSideEffectManager.rollbackToSavepoint();

        for (int i = iterations-1; i > 0; i--) {
            TransactionSideEffectManager.releaseSavepoint();
            assertEquals(0, ai.get());
        }

        TransactionSideEffectManager.commit();
        assertEquals(iterations, ai.get());
    }

    @Test
    public void testRollbackMiddleSingleThread() {
        @SuppressWarnings({ "resource", "unused" })
        SideEffectConnection sec = new SideEffectConnection(null);
        int iterations = 100;
        final AtomicInteger ai = new AtomicInteger();
        for (int i = 0; i < iterations; i++) {
            TransactionSideEffectManager.runAfterThisTransactionCommit(new Runnable() {
                @Override
                public void run() {
                    ai.incrementAndGet();
                }
            });
            TransactionSideEffectManager.addSavepoint();
        }

        assertEquals(0, ai.get());

        for (int i = iterations; i > 0; i--) {
            if (i == iterations / 2)
                TransactionSideEffectManager.rollbackToSavepoint();
            else
                TransactionSideEffectManager.releaseSavepoint();
            assertEquals(0, ai.get());
        }

        TransactionSideEffectManager.commit();
        assertEquals(iterations/2, ai.get());
    }
}
