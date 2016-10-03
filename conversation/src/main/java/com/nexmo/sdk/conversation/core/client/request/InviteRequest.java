/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.core.client.request;

import com.nexmo.sdk.conversation.client.event.CompletionListeners.InviteSendListener;

/**
 * Invite request.
 */
public class InviteRequest extends Request{
    public String cid;
    public String user;
    public InviteSendListener inviteSendListener;

    public InviteRequest(TYPE type, String tid) {
        super(type, tid);
    }

    public InviteRequest(TYPE type, String tid, String cid, String user, InviteSendListener inviteSendListener){
        this(type, tid);
        this.cid = cid;
        this.user = user;
        this.inviteSendListener = inviteSendListener;
    }

}
