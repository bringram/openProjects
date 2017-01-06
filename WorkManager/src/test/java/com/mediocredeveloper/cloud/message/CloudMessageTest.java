package com.mediocredeveloper.cloud.message;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

/**
 * Created by Will2 on 11/25/2016.
 */
public class CloudMessageTest {

    public static class MyHandler implements CloudMessageHandler<String,String>{
        private final String response;
        public MyHandler(String response){
            this.response = response;
        }

        @Override
        public String handle(CloudMessage<String> message) {
            return response;
        }
    }

    public static HazelcastInstance hcast1 = Hazelcast.newHazelcastInstance(new Config());
    public static HazelcastInstance hcast2 = Hazelcast.newHazelcastInstance(new Config());


    @Test
    public void verifyMessage() throws CloudMessageError, TimeoutException {

        final String msg1 = "hello";
        final String msg2 = "world";

        CloudMessageHandler<String,String> handler1 = new MyHandler(msg1);
        CloudMessageHandler<String,String> handler2 = new MyHandler(msg2);

        CloudMessageServicer<String,String> msgSvc1 = new CloudMessageServicer<>("example", "one", hcast1, handler1);
        CloudMessageServicer<String,String> msgSvc2 = new CloudMessageServicer<>("example", "two", hcast2, handler2);

        //verify the messaging services return what we expect
        assertEquals(msg1, msgSvc1.send("one", "whatever", 100, TimeUnit.MILLISECONDS)); //send to self
        assertEquals(msg1, msgSvc2.send("one", "whatever", 100, TimeUnit.MILLISECONDS));
        assertEquals(msg2, msgSvc1.send("two", "whatever", 100, TimeUnit.MILLISECONDS));
        assertEquals(msg2, msgSvc2.send("two", "whatever", 100, TimeUnit.MILLISECONDS));
    }

    @Test
    public void verifyMessageABunch() throws ExecutionException, InterruptedException, CloudMessageError {

        final String msg1 = "hello";
        final String msg2 = "world";

        CloudMessageHandler<String,String> handler1 = new MyHandler(msg1);
        CloudMessageHandler<String,String> handler2 = new MyHandler(msg2);

        CloudMessageServicer<String,String> msgSvc1 = new CloudMessageServicer<>("example", "one", hcast1, handler1);
        CloudMessageServicer<String,String> msgSvc2 = new CloudMessageServicer<>("example", "two", hcast2, handler2);

        for(int i=0; i<1000; i++){
            assertEquals(msg1, msgSvc1.send("one", "whatever").get());
            assertEquals(msg2, msgSvc2.send("two", "whatever").get());
            assertEquals(msg2, msgSvc1.send("two", "whatever").get());
            assertEquals(msg1, msgSvc2.send("one", "whatever").get());
        }
    }

    public static class MySlowHandler implements CloudMessageHandler<String,String>{
        private final String response;
        public MySlowHandler(String response){
            this.response = response;
        }

        @Override
        public String handle(CloudMessage<String> message) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return response;
        }
    }

    @Test(expected = TimeoutException.class)
    public void verifyTimeout() throws TimeoutException, CloudMessageError {
        final String msg = "world";

        CloudMessageHandler<String,String> handler = new MySlowHandler(msg);

        CloudMessageServicer<String,String> msgSvc = new CloudMessageServicer<>("example", "one", hcast1, handler);

        String resp = msgSvc.send("one", "whatever", 1, TimeUnit.MICROSECONDS);

        System.err.println("Got response: "+resp);
    }
}
