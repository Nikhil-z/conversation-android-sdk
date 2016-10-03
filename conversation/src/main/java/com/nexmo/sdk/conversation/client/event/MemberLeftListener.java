/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client.event;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.Member;

/**
 * Listener for members that left the conversation.
 */

public interface MemberLeftListener {

    /**
     * A member has either left a joined conversation, either rejected an invite.
     *
     * @param conversation          The conversation from which a member has left.
     * @param leftMember            The member that has left.
     */
    void onMemberLeft(Conversation conversation, Member leftMember);
}
