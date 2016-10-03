/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client.event;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.Member;

/**
 * Type event listener for notifying upon Members typing ON and OFF.
 */

public interface MemberTypingListener extends ConversationGenericListener {

    /**
     * A {@link Member} has started or stopped typing into a {@link Conversation}.
     *
     * @param conversation    The conversation for which we receive the type indicator.
     * @param member          The member that issued the type indicator.
     * @param typingIndicator {@link com.nexmo.sdk.conversation.client.Member.TYPING_INDICATOR#ON} or {@link com.nexmo.sdk.conversation.client.Member.TYPING_INDICATOR#OFF}.
     */
    void onTyping(Conversation conversation, Member member, Member.TYPING_INDICATOR typingIndicator);
}
