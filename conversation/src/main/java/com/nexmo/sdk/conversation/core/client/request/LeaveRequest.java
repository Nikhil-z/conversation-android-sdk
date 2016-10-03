/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.core.client.request;

import com.nexmo.sdk.conversation.client.event.CompletionListeners.LeaveListener;

/**
 * Leave conversation request.
 */
public class LeaveRequest extends Request {
    public String cid;
    public String memberId;
    public LeaveListener leaveListener;

    public LeaveRequest(TYPE type, String tid) {
        super(type, tid);
    }

    public LeaveRequest(TYPE type, String tid, String cid, String memberId, LeaveListener leaveListener) {
        this(type, tid);
        this.cid = cid;
        this.memberId = memberId;
        this.leaveListener = leaveListener;
    }
}
