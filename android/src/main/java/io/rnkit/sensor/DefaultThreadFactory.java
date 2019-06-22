package io.rnkit.sensor;

import android.util.Log;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 2019-06-19.
 *
 * @author 老潘
 */
public class DefaultThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);

    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final String namePrefix;

    public DefaultThreadFactory() {
        group = Thread.currentThread().getThreadGroup();
        namePrefix = "task pool No." + poolNumber.getAndIncrement() + ", thread No.";
    }

    @Override
    public Thread newThread(Runnable runnable) {
        String threadName = namePrefix + threadNumber.getAndIncrement();
        Log.d("TouNa", "Thread production, name is [" + threadName + "]");
        Thread thread = new Thread(group, runnable, threadName, 0);
        //设为非后台线程
        if (thread.isDaemon()) {
            thread.setDaemon(false);
        }
        //优先级为normal
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        // 捕获多线程处理中的异常
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Log.d("TouNa", "Running task appeared exception! Thread [" + thread.getName() + "], because [" + ex.getMessage() + "]");
            }
        });
        return thread;
    }
}
