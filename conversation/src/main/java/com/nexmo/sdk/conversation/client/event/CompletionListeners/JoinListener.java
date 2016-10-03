/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client.event.CompletionListeners;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.Member;
import com.nexmo.sdk.conversation.client.event.ConversationGenericListener;

/**
 * Conversation join event listener.
 */
public interface JoinListener extends ConversationGenericListener {

    /**
     * Conversation joined successfully.
     * {@link com.nexmo.sdk.conversation.client.Conversation#join(JoinListener)} returned a new {@link Member}.
     * @param conversation The conversation the user has joined.
     * @param member       Contains member information.
     */
    void onConversationJoined(Conversation conversation, Member member);
}
