/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.nexmo.sdk.conversation.client.event.CompletionListeners.MarkedAsSeenListener;
import com.nexmo.sdk.conversation.client.event.ConversationListener;
import com.nexmo.sdk.conversation.client.event.ImageListener;
import com.nexmo.sdk.conversation.client.event.SignalingChannelListener;

import java.util.Date;
import java.util.List;

/**
 * Send an image to a Conversation and handle activity receipts.
 *
 * The following code example shows how to send an image to a Conversation:
 * <pre>
 * conversation.sendImage("imagePath", new ImageSendListener() {
 *          &#64;Override
 *          public void onImageSent(Conversation conversation, Image image) {
 *          }
 *
 *          &#64;Override
 *          public void onError(int errCode, String errMessage) {
 *          }
 *      });
 * </pre>
 *
 * <p>
 * Once the image is uploaded, you will be able to access all 3 representations:
 * <ul>
 *     <li>Original at a 100% quality ratio via {@link Image#getOriginal()}</li>
 *     <li>Medium at a 50% quality ratio via {@link Image#getMedium()}</li>
 *     <li>Thumbnail at a 10% quality ratio via {@link Image#getThumbnail()} </li>
 * </ul></p>
 *
 * <p>Each {@link ImageRepresentation} contains a {@link ImageRepresentation#bitmap}
 * that can be used to update UI</p>
 *
 * <p> For listening to incoming/sent messages events, register using
 * {@link Conversation#addImageListener(ImageListener)} </p>
 *
 */
public class Image extends Text implements Parcelable {
    private static final String TAG = Image.class.getSimpleName();

    private String url;

    private String localPath;
    //seen receipts,sizes, thumbnails to be added;
    ImageRepresentation original;
    ImageRepresentation medium;
    ImageRepresentation thumbnail;

    //temp bitmap
    public Bitmap bitmap;
    public Image(String localPath) {
        super();
        this.localPath = localPath;
    }

    public Image(final String id, final Date timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    public Image(final String payload, final String id, final Date timestamp, final String url) {
        super(payload, id, timestamp);
        this.url = url;
    }

    public Image(final String payload, final String id, final Date timestamp, final String url, Member member){
        this(payload, id, timestamp, url);
        this.member = member;
    }

    public Image(final String payload, final String id, final Date timestamp, final String url, Member member, String deleteEventId){
        this(payload, id, timestamp, url, member);
        this.deleteEventId = deleteEventId;
    }

    public Image(final String payload, final String id, final Date timestamp, final String url, Member member, String deleteEventId, List<SeenReceipt> seenReceipts){
        this(payload, id, timestamp, url, member, deleteEventId);
        this.seenReceiptList = seenReceipts;
    }

    protected Image(Parcel in) {
        super(in);
        this.url = in.readString();
    }

    public Image(Image message) {
        this(message.getPayload(), message.getId(), message.getTimestamp(), message.getUrl(), message.getMember(), message.getDeleteEventId(), message.getSeenReceipts());
    }

    public String getUrl() {
        return this.url;
    }

    public void addRepresentations(ImageRepresentation original, ImageRepresentation medium, ImageRepresentation thumbnail) {
        this.original = original;
        this.medium = medium;
        this.thumbnail = thumbnail;
    }

    public ImageRepresentation getOriginal(){
        return this.original;
    }

    public ImageRepresentation getMedium() {
        return this.medium;
    }

    public ImageRepresentation getThumbnail(){
        return this.thumbnail;
    }

    /**
     * Marks an image event as seen.
     * Flag an {@link Image} as seen by the current member.
     * Message cannot be un-seen.
     *
     * @param listener     The listener in charge of dispatching the completion result.
     */
    public void markAsSeen(MarkedAsSeenListener listener) {
        if (this.conversation.getConversationId() == null)
            listener.onError(ConversationListener.MISSING_CONVERSATION, "Missing conversation");
        else if (ConversationClient.get().getSignallingChannel().isLoggedIn() != null)
            ConversationClient.get().getSignallingChannel().sendSeenEvent(
                    this,
                    listener);
        else
            listener.onError(SignalingChannelListener.MISSING_USER, "No user is logged in");
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeString(this.url);
    }

    @Override
    public String toString() {
        return TAG + " url: " + (this.url != null ? this.url : "");
    }
}
