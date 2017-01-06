package com.mediocredeveloper.cloud.master;

/**
 * Interface for listening for {@link CloudMaster} events.
 */
public interface CloudMasterEventHandler {
    /**
     * Called when an event is detected by master.  Since the implementation polls internally to detect some
     * state changes this cannot be relied upon to be called when event happened, just when it was detected.
     * Look at the {@link CloudMaster#STATE_CHANGE_POLL_INTERVAL} for how often this state is checked.
     * @param event
     */
    void handle(CloudMasterEvent event);
}
