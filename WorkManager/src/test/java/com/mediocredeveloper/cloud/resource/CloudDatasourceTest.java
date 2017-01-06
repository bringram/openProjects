package com.mediocredeveloper.cloud.resource;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Will2 on 12/19/2016.
 */
public class CloudDatasourceTest {

    public static HazelcastInstance hcast1 = Hazelcast.newHazelcastInstance(new Config());
    public static HazelcastInstance hcast2 = Hazelcast.newHazelcastInstance(new Config());

    @Test
    public void verifyPooling() throws ClassNotFoundException, SQLException, InterruptedException, ExecutionException {
        final CloudDatasource ds = new CloudDatasource("group",hcast1, "org.h2.Driver", "jdbc:h2:~/test", "sa", "", 2);

        assertEquals(2, ds.getAvailable());
        Connection conn = ds.getConnection();

        assertEquals(1, ds.getAvailable());
        conn.close();
        assertEquals(2, ds.getAvailable());

        conn = ds.getConnection();
        Connection conn2 = ds.getConnection();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Connection conn = null;
                try {
                    conn = ds.getConnection();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();

        Thread.sleep(1000);

        assertEquals(Thread.State.WAITING, t.getState());

        conn.close();

        Thread.sleep(1000);

        assertEquals(Thread.State.TERMINATED, t.getState());
    }

    @Test
    public void verify2HcastInstance() throws Exception{
        final CloudDatasource ds1 = new CloudDatasource("group",hcast1, "org.h2.Driver", "jdbc:h2:~/test", "sa", "", 2);
        final CloudDatasource ds2 = new CloudDatasource("group",hcast2, "org.h2.Driver", "jdbc:h2:~/test", "sa", "", 2);

        assertEquals(2, ds1.getAvailable());

        Connection conn = ds1.getConnection();

        assertEquals(1, ds1.getAvailable());
        assertEquals(1, ds2.getAvailable());
        conn.close();
        assertEquals(2, ds1.getAvailable());
        assertEquals(2, ds1.getAvailable());

        conn = ds2.getConnection();
        assertEquals(1, ds1.getAvailable());
        assertEquals(1, ds2.getAvailable());
        conn.close();
        assertEquals(2, ds1.getAvailable());
        assertEquals(2, ds1.getAvailable());

        conn = ds2.getConnection();
        conn = ds1.getConnection();

        //0 connections available
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Connection conn = null;
                try {
                    conn = ds1.getConnection();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();

        Thread.sleep(1000);

        assertEquals(Thread.State.WAITING, t.getState());

        conn.close();

        Thread.sleep(1000);

        assertEquals(Thread.State.TERMINATED, t.getState());
    }
}
