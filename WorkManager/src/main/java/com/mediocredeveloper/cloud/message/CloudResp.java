package com.mediocredeveloper.cloud.message;

import java.io.Serializable;

/**
 * Just used for genernal messaging response.  But a little silly, since if a node does respond saying, then it
 * most likely would always respond with {@link #YES}.  The only time a {@link #NO} would happen is if the node
 * cannot be contacted.
 */
public enum CloudResp implements Serializable {
    YES, NO
}
