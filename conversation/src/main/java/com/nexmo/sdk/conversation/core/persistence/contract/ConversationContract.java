/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.core.persistence.contract;

import android.provider.BaseColumns;

/**
 * Contract for {@link com.nexmo.sdk.conversation.client.Conversation} object.
 */
public final class ConversationContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public ConversationContract() {}

    /* Inner class that defines the table contents */
    public static abstract class ConversationEntry implements BaseColumns {
        public static final String TABLE_NAME = "conversation";
        public static final String COLUMN_CID = "cid";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_CREATED = "created";
        public static final String COLUMN_LAST_EVENT_ID = "sequence_number";
        public static final String COLUMN_MEMBER_ID = "member_id";

    }

}