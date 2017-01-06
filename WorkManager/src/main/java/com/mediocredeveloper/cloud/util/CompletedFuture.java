package com.mediocredeveloper.cloud.util;

import com.mediocredeveloper.cloud.message.CloudResp;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Will2 on 12/3/2016.
 */
public class CompletedFuture implements Future<CloudResp> {
    private final CloudResp resp;
    public CompletedFuture(CloudResp resp){
        this.resp = resp;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public CloudResp get() throws InterruptedException, ExecutionException {
        return resp;
    }

    @Override
    public CloudResp get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return resp;
    }
}
