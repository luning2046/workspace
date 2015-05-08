package io.rong.imkit.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Process;

/**
 * Created by zhjchen on 14-3-20.
 */
public class PriorityThreadFactory implements ThreadFactory {

    private static final String TAG = PriorityThreadFactory.class.getSimpleName();

    public static final int THREAD_PRORITY_DEFAULT_LESS = Process.THREAD_PRIORITY_DEFAULT + Process.THREAD_PRIORITY_MORE_FAVORABLE * 3;

    private final AtomicInteger mNumber = new AtomicInteger();
    private final String mName;

    public PriorityThreadFactory(String name) {
        this.mName = name;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        return new Thread(runnable, TAG + "-" + mName + "-" + mNumber.getAndIncrement()) {

            @Override
            public void run() {
                //TODO
                super.run();
            }
        };
    }
}
