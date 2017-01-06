package com.mediocredeveloper.cloud.master;

/**
 * Events that master listens for.
 */
public enum CloudMasterEvent {
    ELECTED, //when elected a master
    DEMOTED  //when going from master to not master
}
