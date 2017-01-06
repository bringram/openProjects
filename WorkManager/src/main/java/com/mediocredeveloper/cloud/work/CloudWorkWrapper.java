package com.mediocredeveloper.cloud.work;

import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Will2 on 11/26/2016.
 */
public class CloudWorkWrapper<T extends Serializable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudWorkWrapper.class);

    private static final String WORK_QUEUE = "%s_%s_WORK_QUEUE";

    private final String group;
    private final String name;

    private final HazelcastInstance hcast;

    private final ExecutorService workerPool =  Executors.newSingleThreadExecutor();

    /**
     * The shared work queue.  Only one member will perform work on this queue.
     */
    private final BlockingQueue<T> workQueue;

    /**
     * The provided worker for work queue
     */
    private final CloudWorker<T> worker;


    public CloudWorkWrapper(final String group, final String name, final HazelcastInstance hcast, final CloudWorker<T> worker){
        this.group = group;
        this.name = name;
        this.hcast = hcast;
        this.worker = worker;
        this.workQueue = hcast.getQueue(String.format(WORK_QUEUE,name,group));

        //listen for work on the work queue
        listenForWork();
    }

    /**
     * Post work to be done
     * @param work
     */
    public final void post(T work){
        workQueue.add(work);
    }

    private final void listenForWork(){
        Runnable runner = new Runnable() {
            @Override
            public void run() {
                try {
                    T work = workQueue.take();
                    worker.doWork(work);
                }catch(InterruptedException e){
                    LOGGER.error("Failed while waiting on work queue.", e);
                }finally {
                    listenForWork();
                }
            }
        };
        workerPool.submit(runner);
    }
}
