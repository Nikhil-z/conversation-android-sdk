/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client.event;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.Member;
import com.nexmo.sdk.conversation.client.User;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.JoinListener;

import java.util.List;

/**
 * Retrieve the list of conversations associated to a user.
 * Note: any {@link User} that joins or receives an invite to a {@link Conversation} becomes a {@link Member}.
 */
public interface ConversationListListener extends ConversationGenericListener {

    /**
     * List of active conversation. The retrieves list will only contain conversation names and ids, for more
     * detailed like for ex: the message list you must call {@link Conversation#update(ConversationListener)}
     * for each one.
     *
     * @param conversationList The list of active conversations.
     *                         A member is part of a conversation only after
     *                         a successful {@link Conversation#join(JoinListener)}
     */
    void onConversationList(List<Conversation> conversationList);
}
