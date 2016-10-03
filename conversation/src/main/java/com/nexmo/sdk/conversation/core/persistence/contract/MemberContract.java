/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.core.persistence.contract;

import android.provider.BaseColumns;

/**
 *  Contract for {@link com.nexmo.sdk.conversation.client.Member} object.
 */
public class MemberContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public MemberContract() {}

    public static abstract class MemberEntry implements BaseColumns {
        public static final String TABLE_NAME = "member";
        public static final String COLUMN_MEMBER_ID = "id";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_STATE = "state";
        public static final String COLUMN_INVITEDAT = "invited_at";
        public static final String COLUMN_JOINEDAT = "joined_at";
        public static final String COLUMN_LEFTAT = "left_at";
    }

}
