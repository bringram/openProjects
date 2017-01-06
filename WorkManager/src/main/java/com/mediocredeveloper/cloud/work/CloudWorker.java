package com.mediocredeveloper.cloud.work;

/**
 * Created by Will2 on 11/16/2016.
 */
public interface CloudWorker<T> {
    public void doWork(T obj);
}
