/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.core.client.request;

import com.nexmo.sdk.conversation.client.event.CompletionListeners.JoinListener;

/**
 * Join conversation request.
 */
public class JoinRequest extends Request {
    public String cid;
    public String cName;
    public String memberId;
    public JoinListener joinListener;

    public JoinRequest(TYPE type, String tid) {
        super(type, tid);
    }

    public JoinRequest(TYPE type, String tid, String cid, JoinListener joinListener) {
        this(type, tid);
        this.cid = cid;
        this.joinListener = joinListener;
    }

    public JoinRequest(TYPE type, String tid, String cid, String cName, JoinListener joinListener) {
        this(type, tid, cid, joinListener);
        this.cName = cName;
    }

    public JoinRequest(TYPE type, String tid, String cid, String cName, String memberId, JoinListener joinListener) {
        this(type, tid, cid, cName, joinListener);
        this.memberId = memberId;
    }
}
