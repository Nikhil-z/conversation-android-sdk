/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client.event.CompletionListeners;

import com.nexmo.sdk.conversation.client.ConversationClient;
import com.nexmo.sdk.conversation.client.User;
import com.nexmo.sdk.conversation.client.event.ConversationGenericListener;

/**
 * Logout listener, responsible for {@link ConversationClient#logout(LogoutListener)} events.
 */
public interface LogoutListener extends ConversationGenericListener {

    /**
     * Logout complete.
     *
     * @param user The user that just logged out.
     */
    void onLogout(User user);

}
