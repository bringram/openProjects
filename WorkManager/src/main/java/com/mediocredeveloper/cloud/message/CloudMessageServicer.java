package com.mediocredeveloper.cloud.message;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.ReplicatedMap;

import java.io.Serializable;
import java.util.concurrent.*;

/**
 * This sets up a message sevicer using hazel cast services.  This allows for synchronous messaging.  This nice thing is you do not
 * have to wait on responses either.  This uses a distributed single thread pool executor, so messages are in effect queues as FIFO.
 * @param <T> The type that messages wrap.
 * @param <E> The type that messages return
 */
public class CloudMessageServicer<T extends Serializable, E extends Serializable> {
    private static final String MSG_EXEC_SERVICE = "%s_%s_MSG_SERVICE";

    /**
     * We need to lookup our handlers from the node we are sending message to, so
     * all nodes register their handler to this map.
     */
    private static final String MSG_HANDLERS = "%s_MSG_HANDLERS";
    private final String name;
    private final String group;
    private final HazelcastInstance hcast;
    private final IExecutorService execService;
    private final ReplicatedMap<String, CloudMessageHandler<T, E>> handlers;

    /**
     * Setups a messaging service that allows for synchronous message passing.
     * @param group The "group" that this messaging service is associated with.
     * @param name A unique name for this messaging service.  This is the same "TO" name when you want to send a message to this service.
     * @param hcast A hazelcast instance to use for setting up this service.
     * @param handler The handler to use for received messages.
     */
    public CloudMessageServicer(final String group, final String name, final HazelcastInstance hcast, final CloudMessageHandler<T,E> handler){
        this.name = name;
        this.group = group;
        this.hcast = hcast;
        this.execService = hcast.getExecutorService(String.format(MSG_EXEC_SERVICE, group, name));
        this.handlers = hcast.getReplicatedMap(String.format(MSG_HANDLERS, group));
        this.handlers.put(name, handler);
    }

    /**
     * Send a mesage, getting back a future.  The message will be sent to the "TO" member of the provided message.
     * @param message The message to send
     * @return
     */
    public Future<E> send(String to, T message) throws CloudMessageError {

        //get the cloud executor service
        IExecutorService executor = hcast.getExecutorService(String.format(MSG_EXEC_SERVICE, group, to));

        //wrap the message
        CloudMessageHandler<T, E> handler = handlers.get(to);

        //possible there is no handler
        if(handler == null){
            throw new CloudMessageError("No handler found for '"+to+"'.  Please verify a message service has been created.");
        }

        CloudMessage<T> cMessage = new CloudMessage<>(name, to, message);
        CloudMessageTask<T,E> task = new CloudMessageTask<>(cMessage, handler);

        //submit the message
        return executor.submit(task);
    }

    public E send(String to, T message, long timeout, TimeUnit unit) throws CloudMessageError, TimeoutException {
        Future<E> future = send(to, message);
        try {
            return future.get(timeout, unit);
        } catch (InterruptedException | ExecutionException e) {
            throw new CloudMessageError("Error sending message,", e);
        }
    }

    /**
     * Just a simple Callable wrapper for the message servicer.
     */
    private static class CloudMessageTask <T extends Serializable, E extends Serializable> implements Callable<E>, Serializable {

        private final CloudMessage<T> message;

        private final CloudMessageHandler<T,E> handler;

        public CloudMessageTask(CloudMessage<T> message, CloudMessageHandler<T,E> handler){
            this.message = message;
            this.handler = handler;
        }

        @Override
        public E call() {
            return handler.handle(message);
        }
    }
}
