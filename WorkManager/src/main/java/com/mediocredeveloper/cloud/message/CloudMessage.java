package com.mediocredeveloper.cloud.message;

import java.io.Serializable;

/**
 * Just a real simple pojo for wrapping basic messages.
 */
public class CloudMessage <T extends Serializable> implements Serializable {
    private final String from;
    private final String to;
    private final T message;

    /**
     * Constructor.
     * @param from node this message is from.  This should be a unique name.  The cloud tools, rely on groups, so this name
     *             should be unique on a per group basis.
     * @param to node this message is to.  This should be a unique name.  The cloud tools, rely on groups, so this name
     *           should be unique on a per group basis.
     * @param message The message to be sent.
     */
    CloudMessage(String from, String to, T message){
        this.from = from;
        this.to = to;
        this.message = message;
    }

    /**
     * Who this message is "TO".
     * @return
     */
    public String getTo(){
        return to;
    }

    /**
     * The message body.
     * @return
     */
    public T getMessage(){
        return message;
    }

    /**
     * Who this message is from.
     * @return
     */
    public String getFrom(){
        return from;
    }
}
