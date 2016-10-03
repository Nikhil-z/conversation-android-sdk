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
 * Listener for {@link Text#markAsSeen(MarkedAsSeenListener)}.
 *
 */
public interface MarkedAsSeenListener extends ConversationGenericListener {

    /**
     * Marking an event as seen was successful.
     *
     * @param conversation The conversation in which the read receipt was sent.
     */
    void onMarkedAsSeen(Conversation conversation);

}
