/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client.event.CompletionListeners;

import com.nexmo.sdk.conversation.client.event.ConversationGenericListener;

/**
 * Conversation leave event listener.
 */
public interface LeaveListener extends ConversationGenericListener {
    int MEMBER_NOT_PART_OF_THIS_CONVERSATION = -1;

    /**
     * {@link com.nexmo.sdk.conversation.client.Conversation#leave(LeaveListener)} has completed.
     */
    void onConversationLeft();
}
