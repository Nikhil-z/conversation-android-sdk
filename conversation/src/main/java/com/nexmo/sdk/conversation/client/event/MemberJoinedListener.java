/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client.event;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.Member;

/**
 * Listener for new members that join a conversation.
 */

public interface MemberJoinedListener {

    /**
     * A member has joined a conversation.
     *
     * @param conversation          The conversation in which a new member has joined.
     * @param member                The member that has joined.
     */
    void onJoined(Conversation conversation, Member member);
}
