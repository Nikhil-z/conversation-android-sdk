/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.core.client.request;

import com.nexmo.sdk.conversation.client.event.ConversationListener;

/**
 * Get conversation details request.
 */
public class GetConversationRequest extends Request{
    public String cid;
    public String startId;
    public String endId;
    public ConversationListener conversationListener;

    public GetConversationRequest(TYPE type, String tid) {
        super(type, tid);
    }

    public GetConversationRequest(TYPE type, String tid, String cid, String startId, String endId, ConversationListener conversationListener) {
        super(type, tid);
        this.cid = cid;
        this.startId = startId;
        this.endId = endId;
        this.conversationListener = conversationListener;
    }
}
