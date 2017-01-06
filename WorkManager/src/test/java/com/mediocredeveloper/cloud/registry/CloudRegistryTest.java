package com.mediocredeveloper.cloud.registry;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.mediocredeveloper.cloud.event.CloudNodeEvent;
import com.mediocredeveloper.cloud.event.CloudNodeEventListener;
import com.mediocredeveloper.cloud.message.CloudMessageError;
import com.mediocredeveloper.cloud.message.CloudResp;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

/**
 * Created by Will2 on 12/4/2016.
 */
public class CloudRegistryTest {

    private static final String node1 = "node1", node2 = "node2";

    public static HazelcastInstance hcast1 = Hazelcast.newHazelcastInstance(new Config());
    public static HazelcastInstance hcast2 = Hazelcast.newHazelcastInstance(new Config());

    public final static Map<String, List<CloudNodeEvent>> map = new ConcurrentHashMap<>();
    static {
        map.put("node1", new ArrayList<CloudNodeEvent>());
        map.put("node2", new ArrayList<CloudNodeEvent>());
    }

    static CloudRegistry reg1 = new CloudRegistry(hcast1, new NodeEvtHandler(), "group", node1);
    static CloudRegistry reg2 = new CloudRegistry(hcast2,  new NodeEvtHandler(), "group", node2);

    @Test
    public void CloudRegistryUpTest() throws InterruptedException, CloudMessageError, ExecutionException {

        assertEquals(CloudResp.YES, reg1.up(node2).get());
        assertEquals(CloudResp.YES, reg2.up(node1).get());

    }

    @Test
    public void CloudRegistryEventRemoveTest() throws InterruptedException, ExecutionException {

        reg1.remove(node2).get();

        assertEquals(CloudNodeEvent.REMOVE, map.get(node2).get(0));

        reg1.add(node2).get();

        assertEquals(CloudNodeEvent.ADD, map.get(node2).get(1));

        map.get(node1).clear();
        map.get(node2).clear();
    }

    @Test
    public void CloudRegistryDroppedTest(){
        //HazelcastInstance hcast3 = Hazelcast.newHazelcastInstance(new Config());

    }

    public static class NodeEvtHandler implements CloudNodeEventListener {
        @Override
        public void handle(String group, String name, CloudNodeEvent event) {
            map.get(name).add(event);
            System.err.println("GOT EVENT: "+name+"-->"+event.name());
        }
    }
}
