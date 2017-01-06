package com.mediocredeveloper.cloud.master;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.mediocredeveloper.cloud.event.CloudNodeEvent;
import com.mediocredeveloper.cloud.event.CloudNodeEventListener;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Will2 on 12/10/2016.
 */
public class CloudMasterTest {
    public static HazelcastInstance hcast1 = Hazelcast.newHazelcastInstance(new Config());
    public static HazelcastInstance hcast2 = Hazelcast.newHazelcastInstance(new Config());
    public static HazelcastInstance hcast3 = Hazelcast.newHazelcastInstance(new Config());
    //public static HazelcastInstance hcast4 = Hazelcast.newHazelcastInstance(new Config());
    //public static HazelcastInstance hcast5 = Hazelcast.newHazelcastInstance(new Config());

    public static final String GROUP = "group1";

    @Test
    public void verifyMasterFailurePickedUp() throws InterruptedException {
        CloudMaster node1 = new CloudMaster(GROUP,"node1", hcast1, new MasterEvtHandler(GROUP,"node1"));
        CloudMaster node2 = new CloudMaster(GROUP,"node2", hcast2, new MasterEvtHandler(GROUP,"node2"));
        CloudMaster node3 = new CloudMaster(GROUP,"node3", hcast3, new MasterEvtHandler(GROUP,"node3"));

        node1.elect();

        CloudMaster master = null;
        HazelcastInstance masterHcast = null;
        if(node1.isMaster()){
            master = node1;
            masterHcast = hcast1;
        }else if(node2.isMaster()){
            master = node2;
            masterHcast = hcast2;
        }else if(node1.isMaster()){
            master = node2;
            masterHcast = hcast2;
        }

        System.err.println(">>>>>NODE1: " + node1.isMaster());
        System.err.println(">>>>>NODE2: "+node2.isMaster());
        System.err.println(">>>>>NODE3: " + node3.isMaster());

        //someone should be a master
        assertNotNull(master);

        masterHcast.shutdown();

        Thread.sleep(3000);

        assertFalse(master.isMaster());

        System.err.println(">>>>>NODE1: " + node1.isMaster());
        System.err.println(">>>>>NODE2: " + node2.isMaster());
        System.err.println(">>>>>NODE3: " + node3.isMaster());

        boolean thereIsAMaster = (node1.isMaster() || node2.isMaster() || node3.isMaster());
        assertTrue(thereIsAMaster);




    }

    public static class MasterEvtHandler implements CloudMasterEventHandler {
        String group = "??";
        String name = "??";
        public MasterEvtHandler(String group, String name){
            this.group = group;
            this.name = name;
        }
        public MasterEvtHandler(){}
        @Override
        public void handle(CloudMasterEvent event) {
            System.out.println("Master Evt Handler-->"+group+":"+name+"-->"+event.name());
        }
    }
    public static class NodeEvtHandler implements CloudNodeEventListener {
        @Override
        public void handle(String group, String name, CloudNodeEvent event) {
            System.out.println("Node Evt Handler-->"+group+":"+name+"-->"+event.name());
        }
    }

}