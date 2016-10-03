/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.core.client.request;

import com.nexmo.sdk.conversation.client.event.CompletionListeners.ConversationCreateListener;

/**
 * Conversation create request.
 */
public class CreateRequest extends Request {
    public String name;
    public ConversationCreateListener conversationCreateListener;

    public CreateRequest(TYPE type, String tid) {
        super(type, tid);
    }

    public CreateRequest(TYPE type, String tid, String name, ConversationCreateListener listener) {
        this(type, tid);
        this.name = name;
        this.conversationCreateListener = listener;
    }
}
