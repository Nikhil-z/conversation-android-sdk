/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Use this class to retrive information about a Member and handle state. For example, when a
 * Member has joined a Conversation, or when he or she is typing.
 * 
 * The following code example shows how to see which Member sent a text message:
 * <pre>
 *  conversation.addTextListener(new TextListener() {
 *   &#64;Override
 *   public void onTextReceived(Conversation conversation, Text message) {
 *       Log.d(TAG, "onTextReceived " + message.getPayload() + " from : " + message.getMember.getMemberId());
 *   }
 *
 *   &#64;Override
 *   public void onTextDeleted(Conversation conversation, Text message, Member member) {
 *       Log.d(TAG, "onTextDeleted by: " + member.getName());
 *   }
 *
 *   //...other text events.
 *   });
 *</pre>
 */
public class Member implements Parcelable {
    private static final String TAG = Member.class.getSimpleName();

    public enum TYPING_INDICATOR {
        ON,
        OFF
    }

    public enum STATE {
        JOINED,
        INVITED,
        LEFT,
        UNKNOWN
    }

    @Expose
    @SerializedName("member_id")
    private String memberId;
    @Expose
    @SerializedName("user_id")
    private String user_id;
    @Expose
    @SerializedName("name")
    private String name;
    private STATE state = STATE.UNKNOWN;
    private TYPING_INDICATOR typingIndicator = TYPING_INDICATOR.OFF;
    // STATE timestamp is returned by get events history.
    private Date joinedAt;
    private Date invitedAt;
    private Date leftAt; //not done yet CSA-69

    public Member(String memberId) {
        this.memberId = memberId;
    }

    public Member(String user_id, String name, String memberId) {
        this(memberId);
        this.user_id = user_id;
        this.name = name;
    }

    public Member(String user_id, String name, String memberId, STATE state) {
        this(user_id, name, memberId);
        this.state = state;
    }

    public Member(String user_id, String name, String memberId, Date joinedAt, Date invitedAt, Date leftAt, STATE state) {
        this(user_id, name, memberId, state);
        this.invitedAt = invitedAt;
        this.joinedAt = joinedAt;
        this.leftAt = leftAt;
    }

    protected Member(User user) {
        this.user_id = user.getUserId();
        this.name = user.getName();
    }

    public Member(Member member) {
        this(member.getUser_id(), member.getName(), member.getMemberId(), member.getJoinedAt(), member.getInvitedAt(), member.getLeftAt(), member.getState());
        this.typingIndicator = member.getTypingIndicator();
    }

    protected Member(Parcel in) {
        this.user_id = in.readString();
        this.name = in.readString();
        this.memberId = in.readString();
        this.state = state(in.readString());
        this.joinedAt = (Date) in.readSerializable();
        this.leftAt = (Date) in.readSerializable();
        this.invitedAt = (Date) in.readSerializable();
    }

    public TYPING_INDICATOR getTypingIndicator() {
        return this.typingIndicator;
    }

    public void setTypingIndicator(TYPING_INDICATOR typingIndicator) {
        this.typingIndicator = typingIndicator;
    }

    public void setState(STATE state) {
        this.state = state;
    }

    public void updateState(STATE state, Date date) {
        switch(state) {
            case INVITED:{
                setState(STATE.INVITED);
                this.invitedAt = date;
                break;
            }
            case JOINED: {
                setState(STATE.JOINED);
                this.joinedAt = date;
                break;
            }
            case LEFT : {
                setState(STATE.LEFT);
                this.leftAt = date;
                break;
            }
        }
    }

    public Date getJoinedAt() {
        return this.joinedAt;
    }

    public Date getInvitedAt() {
        return this.invitedAt;
    }

    public Date getLeftAt() {
        return this.leftAt;
    }

    public static final Creator<Member> CREATOR = new Creator<Member>() {
        @Override
        public Member createFromParcel(Parcel in) {
            return new Member(in);
        }

        @Override
        public Member[] newArray(int size) {
            return new Member[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.user_id);
        dest.writeString(this.name);
        dest.writeString(this.memberId);
        dest.writeString(valueOf(this.state));
        dest.writeSerializable(this.joinedAt);
        dest.writeSerializable(this.leftAt);
        dest.writeSerializable(this.invitedAt);
    }

    public String getName() {
        return this.name;
    }

    public String getUser_id() {
        return this.user_id;
    }

    public String getMemberId() {
        return this.memberId;
    }

    public STATE getState() {
        return this.state;
    }

    @Override
    public String toString(){
        return TAG + " name: " + (this.name!= null ? this.name: "") + ". user_id: " + (this.user_id != null ? this.user_id : "") +
                " .memberId: " + (this.memberId != null ? this.memberId : "" +
                " .state: " + (this.state != STATE.UNKNOWN ? valueOf(this.state)  : "") +
                " .typing: " + (this.typingIndicator == TYPING_INDICATOR.ON ? "ON" : "OFF") +
                " joinedAt: " + (this.joinedAt != null ? this.joinedAt.toString() : ""));
    }

    public static STATE state(String state){
        switch(state) {
            case "INVITED":
                return STATE.INVITED;
            case "JOINED":
                return STATE.JOINED;
            case "LEFT" :
                return STATE.LEFT;
            default:
                return STATE.UNKNOWN;
        }
    }

    public static String valueOf(STATE state){
        switch(state) {
            case INVITED:
                return "INVITED";
            case JOINED:
                return "JOINED";
            case LEFT:
                return "LEFT";
            default:
                return "UNKNOWN";
        }
    }

}
