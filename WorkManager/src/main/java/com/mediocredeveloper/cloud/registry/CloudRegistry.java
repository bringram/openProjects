package com.mediocredeveloper.cloud.registry;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ReplicatedMap;
import com.mediocredeveloper.cloud.CloudContext;
import com.mediocredeveloper.cloud.event.CloudNodeEvent;
import com.mediocredeveloper.cloud.event.CloudNodeEventListener;
import com.mediocredeveloper.cloud.message.CloudMessageError;
import com.mediocredeveloper.cloud.message.CloudMessageServicer;
import com.mediocredeveloper.cloud.message.CloudResp;
import com.mediocredeveloper.cloud.util.CompletedFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Will2 on 11/27/2016.
 */
public class CloudRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudRegistry.class);

    private static final String REGISTRY = "%s_REGISTRY";

    private static final long UPDATE_INTERVAL = 5000;

    private final HazelcastInstance hcast;

    private final ReplicatedMap<String, Long> registry;

    private final CloudActionMsgHandler handler;

    private final CloudMessageServicer<CloudAction, CloudResp> msgService;

    private final String group;

    private final String name;

    private final CloudNodeEventListener listener;

    private final Timer timer;

    /**
     * Create a registry and add this node to it.
     * @param hcast
     * @param group The group name these nodes are part of.
     * @param name This nodes unique name.
     */
    public CloudRegistry(final HazelcastInstance hcast, final CloudNodeEventListener listener, final String group, final String name){
        this.group = group;
        this.name = name;
        this.handler = new CloudActionMsgHandler(group, name);
        this.listener = listener;
        this.registry = hcast.getReplicatedMap(String.format(REGISTRY, group));
        this.hcast = hcast;

        //make sure this node has its updated timestamp
        registry.put(name, System.currentTimeMillis());

        //just periodically update, if it is no longer in registry map, the timer will kill itself upon waking up.
        TimerTask timerTask = new RegularUpdate(this);
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(timerTask, UPDATE_INTERVAL, UPDATE_INTERVAL);

        CloudContext.register(group, name, CloudRegistry.class, this);

        hcast.getCluster().addMembershipListener(new CloudNodeListener(this, listener));

        registry.put(name, 0L);

        this.msgService = new CloudMessageServicer<>(group, name, hcast, handler);
    }

    /**
     * Get this elements name.
     * @return
     */
    public String getName() {
        return name;
    }

    public String getGroup(){
        return group;
    }

    /**
     * Message every node in the registry and wait for them to respond saying they are up.
     * @param time The total amount of time to allow for this check.
     * @param unit
     * @return All nodes which failed to responded within the provided timeout or responded with NO.
     * @throws CloudMessageError
     */
    public Set<String> findDownNodes(long time, TimeUnit unit) {
        Set<String> result = new HashSet<>();

        long current, start = System.currentTimeMillis();
        long totalTimeInMillis = unit.toMillis(time);

        List<Future<CloudResp>> messages = new ArrayList<>();

        //send the check message
        for(String name : registry.keySet()){
            try {
                //can fail when trying to send
                messages.add(check(name));
            }catch(Exception e){
                LOGGER.error("Failure when checking node: "+name,e);
                result.add(name);
            }
        }

        //wait on all messages to return.
        for(Future<CloudResp> msg : messages){
            current = System.currentTimeMillis();
            long remaining = totalTimeInMillis - (current-start);
            try{
                //should not really happen.  Basically if a node retured a resp, then it must
                //be up.
                if(msg.get(remaining, TimeUnit.MILLISECONDS) == CloudResp.NO){
                    result.add(name);
                }
            } catch (TimeoutException e) {
                result.add(name);
                LOGGER.warn("Failed to check if node \"" + name + "\" was up, timeout", e);
            } catch (ExecutionException|InterruptedException e) {
                result.add(name);
                LOGGER.error("Failed to check if node \""+name+"\" was up");
            }
        }

        return result;
    }

    /**
     * Check if the node is up UP.  The future returned should hold the response.
     * @param name The unique node to send message to.
     * @return
     */
    public Future<CloudResp> check(String name){
        if(!registry.containsKey(name)){
            throw new IllegalArgumentException("Node "+name+" is not registered.");
        }
        return send(name, CloudAction.UP);
    }

    /**
     * Iterate the registry verifying everyone is up, atleast within the time provided.
     * @param timeout
     * @param unit
     * @return
     */
    public boolean check(long timeout, TimeUnit unit){
        for(String name : registry.keySet()){
            try {
                CloudResp result = msgService.send(name, CloudAction.UP, timeout, unit);
                if(result != CloudResp.YES){
                    return false;
                }
                registry.put(name, System.currentTimeMillis());
            } catch (TimeoutException e) {
                LOGGER.error("Timeout on testing node is up: " + group + ":" + name, e);
                return false;
            } catch (Exception e) {
                LOGGER.error("Error on testing node is up: "+group+":"+name, e);
                return false;
            }
        }
        return true;
    }

    /**
     * Remove a registry entry.  This will send a request to the registry on requested node.  Upon receipt of this
     * message, the node will remove itself from the cluster registry.
     * @param name
     */
    public synchronized Future<CloudResp> remove(String name) {
        return send(name, CloudAction.REMOVE);
    }

    /**
     * This is really just a way to re-add a node.  If the node was told to {@link #remove()}, this just undoes that.
     * @param name
     * @return
     */
    public synchronized Future<CloudResp> add(String name){
        return send(name, CloudAction.ADD);
    }

    /**
     * This is intended to be called by the Event listener.  You should never call this other than
     * through the event listener
     */
    void remove(){
        registry.remove(name);
    }

    void add(){
        if(!this.registry.containsKey(name)){
           this.registry.put(name, System.currentTimeMillis());
        }
    }

    /**
     * Check the last known time this node updated its time stamp
     * @param name
     * @return
     */
    public Future<CloudResp> up(String name) throws CloudMessageError {
        if(!registry.containsKey(name)){
            throw new IllegalStateException("Node "+name+" is not registered.");
        }
        boolean quickTest = System.currentTimeMillis() - registry.get(name) > UPDATE_INTERVAL*1.10;
        if(!quickTest){
            return msgService.send(name, CloudAction.UP);
        }

        return new CompletedFuture(quickTest? CloudResp.YES : CloudResp.NO);
    }

    /**
     * Get all the registered nodes.
     * @return
     */
    public Set<String> getRegistered(){
        return this.registry.keySet();
    }

    /**
     * Allows the actions be mapped back to events.  This will notify the event listener
     * @param event
     */
    void notify(CloudNodeEvent event){
        listener.handle(group, name, event);
    }

    /**
     * send a action message.  If it fails, then the error is trapped, but will return null.
     * @param name
     * @param action
     * @return
     */
    private Future<CloudResp> send(String name, CloudAction action){
        try {
            return msgService.send(name, action);
        } catch (CloudMessageError e) {
            LOGGER.error("Error sending message - " + group + ":" + name + "->" + action.name());
            throw new IllegalStateException("Failed sending message.", e);
        }
    }

    /**
     * This is a timer task which just periodically will update the registry with when it ran.  This just gives us a quick check to verify
     * that a node is up.
     */
    private static class RegularUpdate extends TimerTask {
        private final CloudRegistry registry;
        public RegularUpdate(CloudRegistry registry){
            this.registry = registry;
        }
        @Override
        public void run() {
            //throws null pointer if the map is empty, seems wrong
            if (registry.registry.size() ==0 || !registry.registry.containsKey(registry.name)) {
                registry.timer.cancel();
                return;
            }
            registry.registry.put(registry.name, System.currentTimeMillis());
        }
    }
}
