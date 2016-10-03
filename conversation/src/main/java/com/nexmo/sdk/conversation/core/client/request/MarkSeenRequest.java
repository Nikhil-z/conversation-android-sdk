/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.core.client.request;

import com.nexmo.sdk.conversation.client.event.CompletionListeners.MarkedAsSeenListener;

/**
 * Mark as seen request,
 */
public class MarkSeenRequest extends Request {
    public String cid;
    public String memberId;
    public String eventId;
    public MarkedAsSeenListener listener;

    public MarkSeenRequest(TYPE type, String tid) {
        super(type, tid);
    }

    public MarkSeenRequest(TYPE type, String tid, String cid, String memberId, String eventId, MarkedAsSeenListener listener) {
        super(type, tid);
        this.cid = cid;
        this.memberId = memberId;
        this.eventId = eventId;
        this.listener = listener;
    }

}
