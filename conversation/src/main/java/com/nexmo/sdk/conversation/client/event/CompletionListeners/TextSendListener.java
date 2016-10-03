/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client.event.CompletionListeners;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.Member;
import com.nexmo.sdk.conversation.client.Text;
import com.nexmo.sdk.conversation.client.event.ConversationGenericListener;

/**
 * Text event send completion listener.
 */
public interface TextSendListener extends ConversationGenericListener {

    /**
     * Sending text message via {@link com.nexmo.sdk.conversation.client.Conversation#sendText(String, TextSendListener)} was successful.
     *
     * @param conversation The conversation in which the text message was sent.
     * @param message      The message itself. It is also containing {@link Member} information. In this case {@link Member} is current user.
     */
    void onTextSent(Conversation conversation, Text message);
}
