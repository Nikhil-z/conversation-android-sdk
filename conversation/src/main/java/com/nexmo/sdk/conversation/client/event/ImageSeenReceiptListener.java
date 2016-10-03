/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client.event;

import com.nexmo.sdk.conversation.client.Image;
import com.nexmo.sdk.conversation.client.Member;
import com.nexmo.sdk.conversation.client.SeenReceipt;

/**
 *
 */
public interface ImageSeenReceiptListener {

    /**
     * Text event was flagged as seen by other member.
     * The {@link SeenReceipt} contains:
     * <ul>
     *     <li>The event id of the event flagged as seen.</li>
     *     <li>The id of the member that has set the flag.</li>
     *     <li>The receipt timestamp.</li>
     * </ul>
     *
     * @param image             The image event associated to the message.
     * @param member            The member that has seen the event.
     * @param seenReceipt       The seen receipt.
     */
    void onSeenReceipt(Image image, Member member, SeenReceipt seenReceipt);
    //receiptRecordChanged on iOS\
}
