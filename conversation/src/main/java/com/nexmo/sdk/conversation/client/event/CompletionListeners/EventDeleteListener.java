/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client.event.CompletionListeners;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.Text;
import com.nexmo.sdk.conversation.client.event.ConversationGenericListener;

/**
 * Listener for {@link com.nexmo.sdk.conversation.client.Conversation#deleteTextEvent(Text, EventDeleteListener)}
 *
 */
public interface EventDeleteListener extends ConversationGenericListener {

    /**
     * Text/Image event was successfully deleted.
     *
     * @param conversation The updated conversation object.
     */
    void onDeleted(Conversation conversation);
}
