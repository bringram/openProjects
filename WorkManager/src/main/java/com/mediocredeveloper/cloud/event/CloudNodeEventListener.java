package com.mediocredeveloper.cloud.event;

/**
 * Created by Will2 on 12/3/2016.
 */
public interface CloudNodeEventListener {
    void handle(String group, String name, CloudNodeEvent event);
}
