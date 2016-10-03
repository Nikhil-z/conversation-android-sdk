/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.core.persistence.contract;

import android.provider.BaseColumns;

/**
 * Contract for {@link com.nexmo.sdk.conversation.client.Text} object.
 */
public final class TextEventContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public TextEventContract() {}

    /* Inner class that defines the table contents */
    public static abstract class TextEntry implements BaseColumns {
        public static final String TABLE_NAME = "text";
        public static final String COLUMN_EVENT_ID = "event_id";
        public static final String COLUMN_CID = "conversation_id";
        public static final String COLUMN_PAYLOAD = "payload";
        public static final String COLUMN_MEMBER = "member_id";
        public static final String COLUMN_TIMESTAMP = "timestamp";

    }

}