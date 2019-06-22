package io.rnkit.sensor;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2019-06-19.
 *
 * @author 老潘
 */
public class TNPoolExecutor extends ThreadPoolExecutor {

    private static  final  int MAX_THREAD_COUNT=Runtime.getRuntime().availableProcessors()+1;
    private static final int INIT_THREAD_COUNT = MAX_THREAD_COUNT;
    private static final long SURPLUS_THREAD_LIFE = 30L;


    private static TNPoolExecutor instance=getInstance();


    //    private static JJPoolExecutor instance;

    public static TNPoolExecutor getInstance() {
        if (null == instance) {
            synchronized (TNPoolExecutor.class) {
                if (null == instance) {
                    instance = new TNPoolExecutor(
                            INIT_THREAD_COUNT,
                            MAX_THREAD_COUNT,
                            SURPLUS_THREAD_LIFE,
                            TimeUnit.SECONDS,
                            new LinkedBlockingQueue<Runnable>(),
                            new DefaultThreadFactory());
                }
            }
        }
        return instance;
    }

    private TNPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                Log.d("TouNa", "Task rejected, too many task!: ");
                //executor.execute(r);
            }
        });
    }
}
