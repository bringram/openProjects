package com.mediocredeveloper.cloud.resource;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import junit.framework.Assert;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by Will2 on 12/18/2016.
 */
public class CloudBufferTest {
    public static HazelcastInstance hcast1 = Hazelcast.newHazelcastInstance(new Config());
    //public static HazelcastInstance hcast2 = Hazelcast.newHazelcastInstance(new Config());

    @Test
    public void verify() throws InterruptedException {
        final CloudBuffer buffer = new CloudBuffer("test",hcast1, 2);
        //CloudBuffer buffer2 = new CloudBuffer("test",hcast2, 2);

        assertEquals(2, buffer.getAvailableLocks());

        buffer.get();
        assertEquals(1, buffer.getAvailableLocks());

        buffer.get();
        assertEquals(0, buffer.getAvailableLocks());

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    buffer.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();

        Thread.sleep(1000);

        assertEquals(Thread.State.WAITING, t.getState());

        buffer.done();

        Thread.sleep(1000);

        assertEquals(Thread.State.TERMINATED, t.getState());

        assertEquals(2, buffer.getLockCount());
        assertEquals(0, buffer.getAvailableLocks());
        buffer.done();

        assertEquals(1, buffer.getLockCount());
        assertEquals(1, buffer.getAvailableLocks());
        buffer.done();

        assertEquals(0, buffer.getLockCount());
        assertEquals(2, buffer.getAvailableLocks());



    }

}
