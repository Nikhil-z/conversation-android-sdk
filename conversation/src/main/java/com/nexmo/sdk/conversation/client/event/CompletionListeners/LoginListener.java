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
 * Login Listener.
 */
public interface LoginListener extends ConversationGenericListener {
    int CONNECT_ALREADY_IN_PROGRESS = -1;

    /**
     * Login successful.
     * Once logged-in, a user may newConversation and join conversations, and send and receive messages.
     *
     * @param user The current user.
     */
    void onLogin(final User user);

    /**
     * One user is already logged in. Must perform explicit {@link ConversationClient#logout} beforehand.
     *
     * @param user
     */
    void onUserAlreadyLoggedIn(final User user);

    /**
     * The supplied token for {@link ConversationClient#login(String, LoginListener)} is not valid,
     * your application needs to retrieve another one in order to complete login.
     */
    void onTokenInvalid();

    /**
     * The supplied token for {@link ConversationClient#login(String, LoginListener)} has expired,
     * your application needs to retrieve a new one in order to complete login.
     */
    void onTokenExpired();
}
