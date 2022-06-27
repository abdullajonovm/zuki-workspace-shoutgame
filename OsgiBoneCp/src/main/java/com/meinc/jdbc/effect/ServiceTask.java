package com.meinc.jdbc.effect;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ServiceTask<V> extends FutureTask<V> {
    private ClassLoader taskClassLoader;

    public ServiceTask(Callable<V> callable) {
        super(callable);
        taskClassLoader = Thread.currentThread().getContextClassLoader();
    }

    public ServiceTask(Runnable runnable) {
        this(runnable, null);
    }

    public ServiceTask(Runnable runnable, V result) {
        super(runnable, result);
        taskClassLoader = Thread.currentThread().getContextClassLoader();
    }

    public ClassLoader getClassLoader() {
        return taskClassLoader;
    }

    public void setClassLoader(ClassLoader taskClassLoader) {
        this.taskClassLoader = taskClassLoader;
    }
}