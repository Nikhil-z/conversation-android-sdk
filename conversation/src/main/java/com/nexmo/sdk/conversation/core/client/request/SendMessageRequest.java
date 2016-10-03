/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.core.client.request;

import com.nexmo.sdk.conversation.client.ImageRepresentation;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.ImageSendListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.TextSendListener;

/**
 * Send text request.
 */
public class SendMessageRequest extends Request {
    public String cid;
    public String message;
    public String memberId;
    public byte[] imageData;
    public TextSendListener textSendListener;
    public ImageSendListener imageSendListener;

    public ImageRepresentation original,medium,thumbnail;

    public SendMessageRequest(TYPE type, String tid) {
        super(type, tid);
    }

    public SendMessageRequest(TYPE type, String tid, String cid, String memberId, String message, TextSendListener listener) {
        this(type,tid);
        this.cid = cid;
        this.memberId = memberId;
        this.message = message;
        this.textSendListener = listener;
    }

    public SendMessageRequest(TYPE type, String tid, String cid, String memberId, String imagePath, ImageSendListener listener) {
        this(type,tid);
        this.cid = cid;
        this.memberId = memberId;
        this.message = imagePath;
        this.imageSendListener = listener;
    }

    public SendMessageRequest(TYPE type, String tid, String cid, String memberId,byte[] imageData, String imagePath, ImageSendListener listener) {
        this(type,tid);
        this.cid = cid;
        this.memberId = memberId;
        this.message = imagePath;
        this.imageData = imageData;
        this.imageSendListener = listener;
    }
}
