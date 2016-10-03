/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client.event.network;

import com.nexmo.sdk.conversation.client.ConversationClient;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.LogoutListener;

/**
 * Socket Connection listener.
 * Any application should listen for those events as the socket might get disconnected at any stage
 * without prior notice.
 */
public interface NetworkingStateListener {

    enum NETWORK_STATE {
        /**
         * Connection completion.
         */
        CONNECTED,
        /**
         * User has been disconnected. Possible cause may be connectivity issues.
         * Also occurs after an explicit {@link ConversationClient#logout(LogoutListener)}.
         */
        DISCONNECTED,
        RECONNECT,
        CONNECT_TIMEOUT,
        CONNECT_ERROR,
        /**
         * Reconnection error.
         */
        RECONNECT_ERROR,
        /**
         * The Nexmo backend has terminated this session.
         * Please try again later.
         */
        SESSION_TERMINATED
    }

    /**
     * User connection has been changed.
     * User has been disconnected. Possible cause may be connectivity issues.
     * onDisconnected also occurs after an explicit {@link ConversationClient#logout(LogoutListener)}.
     *
     * @param networkingState Session networking state has been changed.
     */
    void onNetworkingState(NETWORK_STATE networkingState);

}
