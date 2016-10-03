/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.core.client.request;

import com.nexmo.sdk.conversation.client.event.CompletionListeners.EventDeleteListener;

/**
 *
 */
public class DeleteEventRequest extends Request {
    public String cid;
    public String messageId;
    public String memberId;
    public EventDeleteListener eventDeleteListener;

    public DeleteEventRequest(TYPE type, String tid) {
        super(type, tid);
    }

    public DeleteEventRequest(TYPE type, String tid, String cid, String memberId, String messageId, EventDeleteListener listener) {
        this(type,tid);
        this.cid = cid;
        this.memberId = memberId;
        this.messageId = messageId;
        this.eventDeleteListener = listener;
    }
}
