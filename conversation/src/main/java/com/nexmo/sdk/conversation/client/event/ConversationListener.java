/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client.event;

import com.nexmo.sdk.conversation.client.Conversation;

/**
 * Conversation listener.
 */
public interface ConversationListener extends ConversationGenericListener {
    int MISSING_CONVERSATION = -1;

    /**
     * Returns the detailed information for a {@link Conversation} after calling
     * {@link Conversation#update(ConversationListener)}
     * @param conversation The conversation object.
     */
    void onConversationUpdated(Conversation conversation);


}
