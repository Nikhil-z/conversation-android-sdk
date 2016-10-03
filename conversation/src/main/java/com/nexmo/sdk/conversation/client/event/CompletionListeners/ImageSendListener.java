/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client.event.CompletionListeners;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.Image;
import com.nexmo.sdk.conversation.client.Member;
import com.nexmo.sdk.conversation.client.event.ConversationGenericListener;

/**
 * Image event completion listener.
 */
public interface ImageSendListener extends ConversationGenericListener {

    /**
     * Sending image message via {@link Conversation#sendImage(String, ImageSendListener)}  was successful.
     *
     * @param conversation The conversation in which the image message was sent.
     * @param message      The message itself. It is also containing {@link Member} information. In this case {@link Member} is current user.
     */
    void onImageSent(Conversation conversation, Image message);

    //todo onImageUploadInProgress
}
