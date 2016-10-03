/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Seen receipts for events: of type Text for now.
 */
public class SeenReceipt implements Parcelable {
    private static final String TAG = SeenReceipt.class.getSimpleName();
    //TODO event itself: Text/Image
    private String event_id;
    private String member_id;
    private Date timestamp;

    public SeenReceipt(String event_id, String member_id, Date timestamp) {
        this.event_id = event_id;
        this.member_id = member_id;
        this.timestamp = timestamp;
    }

    public String getMember_id() {
        return this.member_id;
    }

    public String getEvent_id() {
        return this.event_id;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    protected SeenReceipt(Parcel in) {
        this.event_id = in.readString();
        this.member_id = in.readString();
        this.timestamp = (Date) in.readSerializable();
    }

    public static final Creator<SeenReceipt> CREATOR = new Creator<SeenReceipt>() {
        @Override
        public SeenReceipt createFromParcel(Parcel in) {
            return new SeenReceipt(in);
        }

        @Override
        public SeenReceipt[] newArray(int size) {
            return new SeenReceipt[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.event_id);
        dest.writeString(this.member_id);
        dest.writeSerializable(this.timestamp);
    }

    @Override
    public String toString() {
        return TAG + ".event id: " + (this.event_id != null ? this.event_id : "") +
                ".memberId: " + (this.member_id != null ? this.member_id : "") +
                ".timestamnp: " + (this.timestamp != null ? this.timestamp : "");
    }

}
