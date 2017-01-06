package com.mediocredeveloper.cloud.resource;

import com.hazelcast.core.HazelcastInstance;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

/**
 * Create a Datasource like instance that shares a resouce pool across the hazelcast cluster. Uses the ISemephore as
 * the conneciton pool.
 *
 */
public class CloudDatasource {

    private final String dsClass;
    private final String group;
    private final String url;
    private final String user;
    private final String password;

    private final CloudBuffer buffer;

    /**
     * Create the Cloud Datasource.  This is not really a datasource, but will create connections on request and limit
     * number of connections across cluster.
     * @param group Group name this datasource is associated with.
     * @param hcast The hazelcast instance.
     * @param dsClass The class to user for creating connections. Ex: "oracle.jdbc.driver.OracleDriver"
     * @param url The URL to connect to.
     * @param user The username to user when making connections.
     * @param password The password to use when making connections.
     * @param size The size of the Connection pool.
     */
    public CloudDatasource(String group, HazelcastInstance hcast, String dsClass, String url, String user, String password, int size) {
        this.dsClass = dsClass;
        this.group = group;
        this.url = url;
        this.user = user;
        this.password = password;
        this.buffer = new CloudBuffer(group, hcast, size);
    }

    /**
     * Create a single connection.  This is slow as a connection is created for each request.  These are not actually
     * pooled and reused.  The connection must have close() called on it to ensure it is "returned" to the pool.
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    public Connection getConnection() throws SQLException, ClassNotFoundException, InterruptedException {

        Class.forName(dsClass);

        buffer.get();
        Connection conn = DriverManager.getConnection(url, user, password);

        return proxy(conn);
    }

    /**
     * See {@link #getConnection()}.  The only difference is this has a timeout.
     * @param time The time to wait.
     * @param unit The time unit time is in.
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    public Connection getConnection(long time, TimeUnit unit) throws SQLException, ClassNotFoundException, InterruptedException {
        Class.forName(dsClass);

        buffer.get(time, unit);
        Connection conn = DriverManager.getConnection(url, user, password);

        return proxy(conn);
    }

    /**
     * Get number of available "connections" in pool.
     * @return
     */
    public int getAvailable(){
        return buffer.getAvailableLocks();
    }

    /**
     * Wrap a connection in a proxy, so we can listen for the {@link Connection#close()} call.
     * @return
     */
    private Connection proxy(final Connection conn){
        return (Connection) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{Connection.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if("close".equals(method.getName())){
                    buffer.done();
                }
                return method.invoke(conn, args);
            }
        });
    }
}
