/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client.event;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.Member;
import com.nexmo.sdk.conversation.client.Text;

/**
 * Text event listener for notifying upon incoming text related events.
 */
public interface TextListener {

    /**
     * Incoming text message from a conversation.
     * Might be a good idea to customize UI for own messages.
     *
     * @param conversation The conversation from which we received a text message.
     * @param message      The message itself, also containing the {@link Member} information.
     */
    void onTextReceived(Conversation conversation, Text message);

    /**
     * A Text was deleted.
     *
     * @param conversation The conversation object.
     * @param message      The message that got deleted.
     * @param member       The member that removed the message.
     */
    void onTextDeleted(Conversation conversation, Text message, Member member);

    /**
     * Text event was flagged as seen by other member.
     * The {@link SeenReceipt} contains:
     * <ul>
     *     <li>The event id of the event flagged as seen.</li>
     *     <li>The id of the member that has set the flag.</li>
     *     <li>The receipt timestamp.</li>
     * </ul>
     *
     * @param conversation      The conversation associated to the message.
     * @param seenReceipt       The seen receipt.
     */
    //void onTextSeen(Conversation conversation, SeenReceipt seenReceipt);

}
