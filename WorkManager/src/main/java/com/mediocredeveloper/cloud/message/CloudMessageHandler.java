package com.mediocredeveloper.cloud.message;

import java.io.Serializable;

/**
 * Created by Will2 on 11/25/2016.
 */
public interface CloudMessageHandler <T extends Serializable, E extends Serializable> extends Serializable {
    E handle(CloudMessage<T> message);
}
