package com.mediocredeveloper.cloud.resource;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ISemaphore;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Created by Will2 on 12/18/2016.
 */
public class CloudBuffer {

    private static final String BUFFER_NAME = "%s_RESOURCE_BUFFER_%d";

    private final ISemaphore pool;

    private final int poolSize;

    public CloudBuffer(String group, HazelcastInstance hcast, int resourceCount){
        pool = hcast.getSemaphore(String.format(BUFFER_NAME, group, resourceCount));
        pool.init(resourceCount);
        poolSize = resourceCount;
    }

    /**
     * Get a lock on a "resource".
     */
    public void get() throws InterruptedException {
        pool.acquire();
    }

    /**
     * Same as {@link #get()} but with a timeout.
     * @param time The time length
     * @param unit The unit time is in.
     * @throws InterruptedException
     */
    public void get(long time, TimeUnit unit) throws InterruptedException {
        pool.tryAcquire(time, unit);
    }

    /**
     * Return the locked "resource" to the pool.
     */
    public void done(){
        pool.release();
    }

    /**
     * Get the number of available locks.  This uses {@link Lock#tryLock()} on each lock to count.
     * @return
     */
    public int getAvailableLocks(){
        return pool.availablePermits();
    }

    /**
     * The total number of locks.
     * @return
     */
    public int getLockCount(){
        return poolSize - pool.availablePermits();
    }
}
