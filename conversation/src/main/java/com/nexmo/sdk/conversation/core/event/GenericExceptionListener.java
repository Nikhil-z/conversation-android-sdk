/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.core.event;

import java.io.IOException;

public interface GenericExceptionListener {

    /**
     * A request was timed out because of network connectivity exception.
     * Triggered in case of network error, such as UnknownHostException or SocketTimeout exception.
     * @param exception The exception.
     */
    public void onException(final IOException exception);

}