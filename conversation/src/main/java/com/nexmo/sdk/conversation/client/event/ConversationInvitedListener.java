/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client.event;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.Member;
import com.nexmo.sdk.conversation.client.User;

/**
 * Listener for incoming conversation invites.
 * Once a {@link User} gets an invitation  he can accept or reject.
 * Accepting means joining the conversation via:
 *
 */

public interface ConversationInvitedListener extends ConversationGenericListener {

    /**
     * Current user has received an invitation to join a conversation.
     * To extract the date at which the invitation has been sent, use {@link Member#getInvitedAt()}.
     *
     * @param conversation          The conversation for which the invitation was sent out.
     * @param invitedMember         The newly created member that has been invited to join.
     * @param invitedByMemberId     The id of the member that has sent the invitation.
     *
     * @param invitedByUsername     The username of the member that has sent the invitation.
     */
    void onConversationInvited(Conversation conversation, Member invitedMember,
                               String invitedByMemberId, String invitedByUsername);

}
