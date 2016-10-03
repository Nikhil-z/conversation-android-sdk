/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client.event.CompletionListeners;

import com.nexmo.sdk.conversation.client.event.ConversationGenericListener;

/**
 * Invitation completion listener.
 */
public interface InviteSendListener extends ConversationGenericListener {

    /**
     * You have sent out an invitation.
     *
     * @param conversationId        The id of the conversation for which the invitation was sent out.
     * @param memberId              The id of the member that has sent the invitation.
     */
    void onInviteSent(String conversationId, String memberId);

}
