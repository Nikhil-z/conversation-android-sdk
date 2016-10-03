/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */

package com.nexmo.sdk.conversation.client.event;

/**
 * Generic error listener.
 */
public interface ConversationGenericListener {
    /** Generic internal error.**/
    int GENERIC_ERR = -1;
    /** This error generally occurs while trying to perform an action without a prior login.**/
    int MISSING_USER = -2;
    /** This error generally occurs while mandatory params are missing.**/
    int MISSING_PARAMS = -3;

    /**
     * A Conversation related request has encountered an error.
     *
     * @param errCode    The error code.
     * @param errMessage The reason describing the error. A human readable message describing the error.
     */
    void onError(final int errCode, final String errMessage);
}
