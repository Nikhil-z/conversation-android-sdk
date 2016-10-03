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
public final class ImageEventContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public ImageEventContract() {}

    /* Inner class that defines the table contents */
    public static abstract class ImageEntry implements BaseColumns {
        public static final String TABLE_NAME = "image";
        public static final String COLUMN_EVENT_ID = "event_id";
        public static final String COLUMN_CID = "conversation_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_NAME_SUBTITLE = "subtitle";

    }

}