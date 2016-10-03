/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.core.client.request;

import com.nexmo.sdk.conversation.client.event.CompletionListeners.ImageSendListener;

/**
 * Send image request.
 */
public class SendImageRequest  extends Request {
    public String cid;
    public String message;
    public String filePath; // byte[] or multipart?
    public String memberId;
    public ImageSendListener imageSendListener;

    public SendImageRequest(TYPE type, String tid) {
        super(type, tid);
    }

    public SendImageRequest(TYPE type, String tid, String cid, String memberId, String message, ImageSendListener listener) {
        this(type,tid);
        this.cid = cid;
        this.memberId = memberId;
        this.message = message;
        this.imageSendListener = listener;
    }
}