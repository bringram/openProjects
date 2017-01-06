package com.mediocredeveloper.cloud.work;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Will2 on 11/26/2016.
 */
public class CloudWorkerMgrTest {

    public static HazelcastInstance hcast1 = Hazelcast.newHazelcastInstance(new Config());
    public static HazelcastInstance hcast2 = Hazelcast.newHazelcastInstance(new Config());

    @Test
    public void verifyWorkQueue() throws InterruptedException {
        String group = "example";
        final List<String> work1 = new CopyOnWriteArrayList<>();
        final List<String> work2 = new CopyOnWriteArrayList<>();

        CloudWorkWrapper<String> mgr1 = new CloudWorkWrapper<>(group, "one", hcast1, new CloudWorker<String>() {
            @Override
            public void doWork(String obj) {
                work1.add(obj);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        CloudWorkWrapper<String> mgr2 = new CloudWorkWrapper<>(group, "one", hcast2, new CloudWorker<String>() {
            @Override
            public void doWork(String obj) {
                work2.add(obj);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        int counter = 0;
        while(counter < 200){
            counter = counter + 2;
            mgr1.post("some work");
            mgr2.post("some other work");
        }

        Thread.sleep(5000);

        System.out.println("work1: " + work1.size());
        System.out.println("work2: " + work2.size());

        assertEquals(counter, work1.size() + work2.size());
        assertTrue(work1.size() > 0);
        assertTrue(work2.size() > 0);
    }
}
