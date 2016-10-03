/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.MarkedAsSeenListener;
import com.nexmo.sdk.conversation.client.event.ConversationListener;
import com.nexmo.sdk.conversation.client.event.SignalingChannelListener;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Send a message to a Conversation and handle activity receipts.
 *
 * The following code example shows how to send a text message to a Conversation:
 * <pre>
 * conversation.sendText("payload", new TextSendListener() {
 *       &#64;Override
 *       public void onTextSent(Conversation conversation, Text text) {
 *       }
 *
 *       &#64;Override
 *       public void onError(int errCode, String errMessage) {
 *       }
 *   });
 * </pre>
 */
public class Text implements Parcelable {
    private static final String TAG = Text.class.getSimpleName();
    @Expose
    @SerializedName("text")
    private String payload;
    @Expose
    @SerializedName("id")
    protected String id;
    protected String deleteEventId;
    protected Date timestamp;
    protected Member member;
    protected List<SeenReceipt> seenReceiptList = new ArrayList<>();
    private Map<Member, SeenReceipt> seenReceiptMap = new ConcurrentHashMap<>();

    protected Conversation conversation;

    public Text(){}

    public Text(final String payload) {
        this.payload = payload;
    }

    public Text(final String payload, final Conversation conversation) {
        this(payload);
        this.conversation = conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public Text(final String payload, final String id, final Date timestamp){
        this(payload);
        this.id = id;
        this.timestamp = timestamp;
    }

    public Text(final String payload, final String id, final Date timestamp, Member member){
        this(payload, id, timestamp);
        this.member = member;
    }

    public Text(final String payload, final String id, final Date timestamp, Member member, String deleteEventId){
        this(payload, id, timestamp, member);
        this.deleteEventId = deleteEventId;
    }

    public Text(final String payload, final String id, final Date timestamp, Member member, String deleteEventId, List<SeenReceipt> seenReceipts){
        this(payload, id, timestamp, member, deleteEventId);
        this.seenReceiptList = seenReceipts;
    }

    public Text(Text message) {
        this(message.getPayload(), message.getId(), message.getTimestamp(), message.getMember(), message.getDeleteEventId(), message.getSeenReceipts());
    }

    protected Text(Parcel in) {
        this.payload = in.readString();
        this.id = in.readString();
        this.timestamp = (Date) in.readSerializable();
        this.member = in.readParcelable(Member.class.getClassLoader());
        this.deleteEventId = in.readString();
        if (this.seenReceiptList != null)
            in.readTypedList(this.seenReceiptList, SeenReceipt.CREATOR);
//        if (this.seenReceiptMap != null)
//            in.readArrayMapInternal(this.seenReceiptMap, SeenReceipt.CREATOR);
    }

    public String getDeleteEventId() {
        return deleteEventId;
    }

    public void setDeleteEventId(String deleteEventId) {
        this.deleteEventId = deleteEventId;
    }

    public static final Creator<Text> CREATOR = new Creator<Text>() {
        @Override
        public Text createFromParcel(Parcel in) {
            return new Text(in);
        }

        @Override
        public Text[] newArray(int size) {
            return new Text[size];
        }
    };

    /**
     * Get the parent conversation of this event.
     *
     * @return The conversation.
     */
    public Conversation getConversation() {
        return this.conversation;
    }

    /**
     * Marks a text event as seen.
     * Flag a {@link Text} as seen by the current member.
     * Message cannot be un-seen.
     *
     * @param listener     The listener in charge of dispatching the completion result.
     */
    public void markAsSeen(MarkedAsSeenListener listener) {
        // invalid conv object
        if (this.conversation.getConversationId() == null)
            listener.onError(ConversationListener.MISSING_CONVERSATION, "Missing conversation");
        else if (ConversationClient.get().getSignallingChannel().isLoggedIn() != null)
            ConversationClient.get().getSignallingChannel().sendSeenEvent(
                    this,
                    listener);
        else
            listener.onError(SignalingChannelListener.MISSING_USER, "No user is logged in");
    }

    public void addSeenReceipt(SeenReceipt seenReceipt) {
        this.seenReceiptList.add(seenReceipt);
    }

    /**
     * Get the list of seen receipts for this event.
     * Search the list for certain {@link SeenReceipt#getMember_id()} if needed.
     *
     * @return A list of seen receipts.
     */
    public List<SeenReceipt> getSeenReceipts() {
        return this.seenReceiptList;
    }

    /**
     * Get the payload of this event. For images payload is not provided.
     *
     * @return The text payload.
     */
    public String getPayload() {
        return this.payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getId() {
        return this.id;
    }

    /**
     * Get the timestamp at which this image was received.
     *
     * @return The received timestamp.
     */
    public Date getTimestamp() {
        return this.timestamp;
    }

    /**
     * Get the sender of this image.
     *
     * @return The member that has sent this image.
     */
    public Member getMember() {
        return this.member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.payload);
        dest.writeString(this.id);
        dest.writeSerializable(this.timestamp);
        dest.writeParcelable(this.member, 0);
        dest.writeString(this.deleteEventId);
        dest.writeTypedList(this.seenReceiptList);
    }

    @Override
    public String toString(){
        return TAG + " payload: " + (this.payload != null ? this.payload : "") + " .id: " + (this.id != null ? this.id : "") +
                " .timestamp: " + (this.timestamp != null ? this.timestamp.toString() : "") +
                " .member: " + (this.member != null ? this.member.toString() : "");
    }
}
