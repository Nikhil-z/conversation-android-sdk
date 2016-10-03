/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client.event.CompletionListeners;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.ConversationClient;
import com.nexmo.sdk.conversation.client.event.ConversationGenericListener;

/**
 * Conversation creation event listener.
 */
public interface ConversationCreateListener extends ConversationGenericListener {

    /**
     * {@link ConversationClient#newConversation(String, ConversationCreateListener)} returned a new {@link Conversation}.
     * Once a conversation has been created, members can be added or removed.
     *
     * @param conversation The newly created conversation.
     */
    void onConversationCreated(Conversation conversation);
}
