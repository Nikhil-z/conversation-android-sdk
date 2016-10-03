/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client.event.CompletionListeners;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.Member;
import com.nexmo.sdk.conversation.client.event.ConversationGenericListener;

/**
 *
 */
public interface TypingSendListener extends ConversationGenericListener {

    /**
     * Sending a {@link com.nexmo.sdk.conversation.client.Member.TYPING_INDICATOR } via
     * {@link Conversation#startTyping(TypingSendListener)}
     * or {@link Conversation#stopTyping(TypingSendListener)} was successful.
     *
     * @param conversation    The conversation in which the typing indicator was sent.
     * @param typingIndicator The typing indicator.
     */
    void onTypingSent(Conversation conversation, Member.TYPING_INDICATOR typingIndicator);

}
