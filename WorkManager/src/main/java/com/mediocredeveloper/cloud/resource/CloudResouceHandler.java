package com.mediocredeveloper.cloud.resource;

import java.sql.SQLException;

/**
 * Created by Will2 on 12/18/2016.
 */
public interface CloudResouceHandler <T> {
    T get() throws Exception;
    void done(T resource) throws Exception;
}
