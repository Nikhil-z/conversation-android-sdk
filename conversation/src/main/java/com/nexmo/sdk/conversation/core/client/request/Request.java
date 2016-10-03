/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.core.client.request;

/**
 * Base request class.
 */
public class Request {

    public enum TYPE {
        CREATE,
        JOIN,
        INVITE,
        LEAVE,
        GET,
        SEND_TEXT,
        SEND_IMAGE,
        DELETE_EVENT,
        MARK_TEXT_SEEN,
        MARK_IMAGE_SEEN,//initial image, no thumbnail
        TYPING,
        PUSH_SUBSCRIBE

    }

    public TYPE type;
    public String tid;

    public Request(TYPE type, String tid) {
        this.type = type;
        this.tid = tid;
    }

}
