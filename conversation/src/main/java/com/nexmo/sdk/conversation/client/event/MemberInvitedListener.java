/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client.event;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.Member;

/**
 * Listener for members that get invited to a conversation.
 */

public interface MemberInvitedListener {

    /**
     * A member has received an invitation to join a conversation.
     * To extract the date at which the invitation has been sent, use {@link Member#getInvitedAt()}.
     * todo date ?
     *
     * @param conversation          The conversation for which the invitation was sent out.
     * @param invitedMember         The newly created member that has been invited to join.
     * @param invitedByMemberId     The id of the member that has sent the invitation.
     *
     * @param invitedByUsername     The username of the member that has sent the invitation.
     */
    void onMemberInvited(Conversation conversation, Member invitedMember, String invitedByMemberId, String invitedByUsername);

    //todo
    //void onInvitationReceived(Conversation conversation, Member invitedBy, Member invitedMember, Date invitationDate);
}
