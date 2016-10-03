/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client;

import java.util.Date;

/**
 * Use Event to send a Text or Image to a Conversation and handle activity receipts.
 * The following code example shows how to mark an Event as seen:
 * <pre>
 * text.markAsSeen(new TextMarkAsSeenListener() {
 *         &#64;Override
 *         public void onMarkedAsSeen(Conversation conversation) {
 *         }
 *
 *         &#64;Override
 *         public void onError(int errCode, String errMessage) {
 *         }
 *     });
 *</pre>
 * See Text or Image for more information on how to send an Event.
 */
public class Event extends Text {

    public Event(String payload) {
        super(payload);
    }

    public Event(String payload, String id, Date timestamp, Member member) {
        super(payload, id, timestamp, member);
    }

    public Event(Event message) {
        super(message);
    }
}
