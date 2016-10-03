/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.core.client.request;

import com.nexmo.sdk.conversation.client.Member;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.TypingSendListener;

/**
 *
 */
public class TypingIndicatorRequest extends Request {
    public String cid;
    public String memberId;
    public Member.TYPING_INDICATOR typingIndicator;
    public TypingSendListener typingSendListener;

    public TypingIndicatorRequest(TYPE type, String tid) {
        super(type, tid);
    }

    public TypingIndicatorRequest(TYPE type, String tid, String cid, String memberId, Member.TYPING_INDICATOR typingIndicator, TypingSendListener listener) {
        this(type, tid);
        this.cid = cid;
        this.memberId = memberId;
        this.typingIndicator = typingIndicator;
        this.typingSendListener = listener;
    }

}
