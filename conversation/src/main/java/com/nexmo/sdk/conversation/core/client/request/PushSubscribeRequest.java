/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.core.client.request;

import com.nexmo.sdk.conversation.client.event.CompletionListeners.PushEnableListener;

/**
 */
public class PushSubscribeRequest extends Request{
    public String cid;
    public PushEnableListener pushEnableListener;

    public PushSubscribeRequest(TYPE type, String tid) {
        super(type, tid);
    }

    public PushSubscribeRequest(TYPE type, String tid, String cid, PushEnableListener pushEnableListener){
        this(type,tid);
        this.cid = cid;
        this.pushEnableListener = pushEnableListener;
    }
}
