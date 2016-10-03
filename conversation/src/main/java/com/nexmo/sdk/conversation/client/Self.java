/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client;

/**
 * Represents the current User after he or she has logged in.
 */
public class Self extends User {

    protected Self(String user_id, String name) {
        super(user_id, name);
    }

}
