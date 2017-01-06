package com.mediocredeveloper.cloud.message;

/**
 * Created by Will2 on 11/16/2016.
 */
public class CloudMessageError extends Exception {
    public CloudMessageError(String name){
        super(name);
    }
    public CloudMessageError(String error, Exception e){
        super(error);
    }
}
