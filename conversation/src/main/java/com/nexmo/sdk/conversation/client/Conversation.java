/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

import com.nexmo.sdk.conversation.client.event.CompletionListeners.EventDeleteListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.ImageSendListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.InviteSendListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.TextSendListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.TypingSendListener;
import com.nexmo.sdk.conversation.client.event.ConversationGenericListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.JoinListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.LeaveListener;
import com.nexmo.sdk.conversation.client.event.ConversationListener;
import com.nexmo.sdk.conversation.client.event.ImageListener;
import com.nexmo.sdk.conversation.client.event.ImageSeenReceiptListener;
import com.nexmo.sdk.conversation.client.event.MemberInvitedListener;
import com.nexmo.sdk.conversation.client.event.MemberJoinedListener;
import com.nexmo.sdk.conversation.client.event.MemberLeftListener;
import com.nexmo.sdk.conversation.client.event.TextSeenReceiptListener;
import com.nexmo.sdk.conversation.client.event.SignalingChannelListener;
import com.nexmo.sdk.conversation.client.event.TextListener;
import com.nexmo.sdk.conversation.client.event.MemberTypingListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A container that you use to manage communications between Members,
 * the conversation history and context.
 *
 * <p>Listen for incoming text events {@link Conversation#addTextListener(TextListener)}</p>
 * <p>Example usage:</p>
 * <pre>
 *     myConversation.addTextListener(new ConversationTextListener() {
 *         &#64;Override
 *         public void onTextReceived(Conversation conversation, Message message) {
 *              // Update the application UI here if needed.
 *         }
 *         &#64;Override
 *         public void onTextDeleted(Conversation conversation, Message message) {
 *              // Update the application UI here if needed.
 *         }
 *     });
 * </pre>
 *
 * <p>Sending a text message to a specific conversation is achieved using
 * {@link Conversation#sendText(String, TextSendListener)}</p>
 * <p>Example usage:</p>
 * <pre>
 *     myConversation.sendText("message payload...", new TextSendListener() {
 *         &#64;Override
 *         public void onTextSent(Conversation conversation, Text message) {
 *              // Update the application UI here if needed.
 *         }
 *         &#64;Override
 *         public void onError(int errorCode, String message) {
 *              // Update the application UI here if needed.
 *         }
 *    });
 * </pre>
 *
 * <p>Listen for typing indicator events {@link Conversation#addTypingListener(MemberTypingListener)} </p>
 * <p>Example usage:</p>
 * <pre>
 *     myConversation.addTypingListener(new MemberTypingListener() {
 *         &#64;Override
 *         public void onTyping(Conversation conversation, Member member, Member.TYPING_INDICATOR typingIndicator);
 *              // Update the application UI to highlight when someone else is typing.
 *         }
 *     });
 * </pre>
 *
 */
public class Conversation implements Parcelable {
    private static final String TAG = Conversation.class.getSimpleName();

    //TODO v2.0 key value map based on memberId to fast up the search
    private List<Member> members = new ArrayList<>();
    private List<Text> messages = new ArrayList<>();
    private List<Image> images = new ArrayList<>();
    //self as member of the conversation
    private Member self;
    @Expose
    @SerializedName("name")
    private String name;
    @Expose
    @SerializedName("id")
    private String conversationId;
    @Expose
    @SerializedName("member_id")
    private String memberId; //if memberId is not set, the conversation is not joined
    @Expose
    @SerializedName("sequence_number")
    private String lastEventId; // last known event conversationId, used for paginated access.
    private Date creationDate;

    protected Conversation() {
    }

    public Conversation(final String name) {
        this.name = name;
    }

    public Conversation(final String name, final String conversationId) {
        this(name);
        this.conversationId = conversationId;
    }

    public Conversation(final String name, final String conversationId, final String memberId) {
        this(name, conversationId);
        this.conversationId = conversationId;
        this.memberId = memberId;
        if (!TextUtils.isEmpty(memberId))
            this.self = new Member(this.memberId);
    }

    public Conversation(final String name, final String conversationId, final String memberId, final Date creationDate) {
        this(name, conversationId, memberId);
        this.creationDate = creationDate;
    }

    public Conversation(final String name, final String conversationId, final String memberId, final Date creationDate, String lastEventId) {
        this(name, conversationId, memberId, creationDate);
        this.lastEventId = lastEventId;
    }

    public Conversation(final String name, final String conversationId, final String memberId, final List<Text> messages, final List<Member> members, final Date creation_time, final String lastEventId) {
        this(name, conversationId, memberId, creation_time, lastEventId);
        this.messages = messages;
        this.members = members;
    }

    public Conversation(final String name, final String conversationId, final Member member, final List<Text> messages,
                        final List<Image> images, final List<Member> members, final Date creation_time, final String lastEventId) {
        this(name, conversationId);
        this.messages = messages;
        this.images = images;
        this.members = members;
        this.creationDate = creation_time;
        if (member != null){
            this.memberId = member.getMemberId();
            this.self = member;
        }
        this.lastEventId = lastEventId;
    }

    public Conversation(Conversation conversation) {
        this(conversation.getName(), conversation.getConversationId(), conversation.getSelf(),
                conversation.getMessages(), conversation.getImages(), conversation.getMembers(),
                conversation.getCreationDate(), conversation.getLastEventId());
    }

    /**
     * Retrieve full conversation information, like members and conversation details.
     *
     * <p>For retrieving the text/image events use {@link Conversation#updateEvents(String, String, ConversationListener)} (String, String, ConversationListener)}.
     *
     * <p>Please bear in mind to refresh the members list on a regular basis to avoid messages from 'unknown' members.
     *
     * <p>For the first time launching the app, the local cache will not present any results.
     *
     * @param conversationListener The listener in charge of dispatching the result.
     */
    public void update(ConversationListener conversationListener) {
        if (conversationListener == null)
            Log.d(TAG, "ConversationListener is mandatory");
        else if (TextUtils.isEmpty(this.getConversationId()))
                conversationListener.onError(ConversationListener.MISSING_CONVERSATION, "Missing conversation");
        else //if (!forceCache || (forceCache && this.signalingChannel.hasCachedConversation(cid)))
         if (ConversationClient.get().getSignallingChannel().isLoggedIn() == null)
             conversationListener.onError(SignalingChannelListener.MISSING_USER, "No user is logged in");
        else
             ConversationClient.get().getSignallingChannel().getConversation(this.conversationId, conversationListener);
    }

    /**
     * Retrieve the event history of a {@link Conversation}.
     * In the next release this will be incorporated in the update method, and will be done silently.
     *
     * <p>Note: Make sure to call {@link Conversation#update(ConversationListener)} before this,
     * in order to retrieve the already existing members inside this conversation.</p>
     *
     * @param startId              Optional, the first event id to get.
     * @param endId                Optional, the last event id to get.
     * @param conversationListener The listener in charge of dispatching the result.
     */
    public void updateEvents(String startId, String endId, ConversationListener conversationListener) {
        if (conversationListener == null)
            Log.d(TAG, "ConversationListener is mandatory");
        else if (TextUtils.isEmpty(this.getConversationId()))
            conversationListener.onError(ConversationListener.MISSING_CONVERSATION, "Missing conversation");
        else //if (!forceCache || (forceCache && this.signalingChannel.hasCachedConversation(cid)))
            if (ConversationClient.get().getSignallingChannel().isLoggedIn() == null)
                conversationListener.onError(SignalingChannelListener.MISSING_USER, "No user is logged in");
            else
                ConversationClient.get().getSignallingChannel().getMessages(
                        this.conversationId,
                        startId,
                        endId,
                        conversationListener);
    }

    /**
     * Current {@link User} joins a {@link Conversation}.
     *
     * <p>Note: any {@link User} that joins a {@link Conversation} becomes a {@link Member}.</p>
     * <p>The {@link User} will always be current user that has successfully logged-in.</p>
     *
     * @param joinListener             The completion listener in charge of dispatching the result.
     */
    public void join(JoinListener joinListener) {
        if (joinListener != null) {
            if (TextUtils.isEmpty(this.conversationId))
                joinListener.onError(ConversationGenericListener.MISSING_PARAMS, "This conversation does not have an id.");
            else if (ConversationClient.get().getSignallingChannel().isLoggedIn() == null)
                joinListener.onError(SignalingChannelListener.MISSING_USER, "No user is logged in");
            else ConversationClient.get().getSignallingChannel().
                        joinConversation(this.conversationId, joinListener);

        }
        else
            Log.d(TAG, "JoinListener is mandatory");
    }

    /**
     * Invite a user to a certain conversation.
     *
     * @param username            The user id or name.
     * @param inviteSendListener  Teh completion listener.
     */
    public void invite(String username, InviteSendListener inviteSendListener) {
        if (inviteSendListener == null)
            Log.d(TAG, "InviteListener is mandatory");
        else if (TextUtils.isEmpty(this.conversationId))
                inviteSendListener.onError(ConversationGenericListener.MISSING_PARAMS, "Invalid input id.");
            else if (ConversationClient.get().getSignallingChannel().isLoggedIn() == null)
                inviteSendListener.onError(SignalingChannelListener.MISSING_USER, "No user is logged in");
            else ConversationClient.get().getSignallingChannel().
                    invite(this.conversationId, username, inviteSendListener);
    }

    /**
     * in v2.0 we should create an Invitation object.
     * Accept an invitation to join a conversation.
     * @param member
     * @param joinListener
     */
    public void acceptInvite(Member member, JoinListener joinListener) {
        if (joinListener != null) {
            if (TextUtils.isEmpty(this.conversationId))
                joinListener.onError(ConversationGenericListener.MISSING_PARAMS, "This conversation does not have an id.");
            else if (ConversationClient.get().getSignallingChannel().isLoggedIn() == null)
                joinListener.onError(SignalingChannelListener.MISSING_USER, "No user is logged in");
            else ConversationClient.get().getSignallingChannel().
                        acceptInvitation(this.conversationId, this.name, member.getMemberId(), joinListener);

        }
        else
            Log.d(TAG, "JoinListener is mandatory");
    }

    /**
     * Leave a conversation the {@link User} has joined, or is invited to.
     * No ownership is enforced on conversation, so any member may invite or remove others.
     *
     * @param leaveListener            The completion listener in charge of dispatching the result.
     */
    public void leave(LeaveListener leaveListener) {
        if (leaveListener == null)
            Log.d(TAG, "LeaveListener is mandatory");
        else if (TextUtils.isEmpty(this.conversationId))
                leaveListener.onError(ConversationGenericListener.MISSING_PARAMS, "This conversation does not have an id.");
        else if (this.getSelf() == null)
            leaveListener.onError(LeaveListener.MEMBER_NOT_PART_OF_THIS_CONVERSATION, "You are currently not part of this conversation");
        //this needs more tests, maybe the self memberId is not yet retrieved.
        else if (ConversationClient.get().getSignallingChannel().isLoggedIn() == null)
            leaveListener.onError(SignalingChannelListener.MISSING_USER, "No user is logged in");
        else ConversationClient.get().getSignallingChannel().leaveConversation(
                    this.conversationId,
                    this.self.getMemberId(),
                    leaveListener);
    }

    /**
     * Register for receiving member joined events.
     * @param memberJoinedListener The completion listener in charge of dispatching the result.
     */
    public void addMemberJoinedListener(MemberJoinedListener memberJoinedListener) {
        ConversationClient.get().getSignallingChannel().
                addMemberJoinedListener(this.conversationId, memberJoinedListener);
    }

    /**
     * Register for receiving member invited events.
     * This applies for other members being invited to this conversation.
     *
     * @param memberInvitedListener The completion listener in charge of dispatching the result.
     */
    public void addMemberInvitedListener(MemberInvitedListener memberInvitedListener) {
        ConversationClient.get().getSignallingChannel().
                addMemberInvitedListener(this.conversationId, memberInvitedListener);
    }

    /**
     * Register for receiving member left events.
     *
     * @param memberLeftListener The completion listener in charge of dispatching the result.
     */
    public void addMemberLeftListener(MemberLeftListener memberLeftListener) {
        ConversationClient.get().getSignallingChannel().
                addMemberLeftListener(this.conversationId, memberLeftListener);
    }

    /**
     * Upon disconnect remove all listeners.
     *
     * @param memberJoinedListener The completion listener in charge of dispatching the result.
     */
    public void removeMemberJoinedListener(MemberJoinedListener memberJoinedListener) {
        ConversationClient.get().getSignallingChannel().
                removeMemberJoinedListener(this.conversationId, memberJoinedListener);
    }

    /**
     * Upon disconnect remove all listeners.
     *
     * @param memberLeftListener The completion listener in charge of dispatching the result.
     */
    public void removeMemberLeftListener(MemberLeftListener memberLeftListener) {
        ConversationClient.get().getSignallingChannel().
                removeMemberLeftListener(this.conversationId, memberLeftListener);
    }

    /**
     * Upon disconnect remove all listeners.
     *
     * @param memberInvitedListener The completion listener in charge of dispatching the result.
     */
    public void removeMemberInvitedListener(MemberInvitedListener memberInvitedListener) {
        ConversationClient.get().getSignallingChannel().
                removeMemberInvitedListener(this.conversationId, memberInvitedListener);
    }

    /**
     * Remove a member from a conversation.
     * No ownership is enforced on conversation, so any member may invite or remove others.
     *
     * @param member        The member that is kicked out of this Conversation.
     * @param leaveListener The completion listener in charge of dispatching the result.
     */
    public void kick(Member member, LeaveListener leaveListener) {
        if (leaveListener == null)
            Log.d(TAG, "LeaveListener is mandatory");
        else if (member == null)
            leaveListener.onError(ConversationGenericListener.MISSING_PARAMS, "The member is mandatory");
        else if (TextUtils.isEmpty(this.conversationId))
            leaveListener.onError(ConversationGenericListener.MISSING_PARAMS, "This conversation does not have an id.");
        else if (ConversationClient.get().getSignallingChannel().isLoggedIn() == null)
            leaveListener.onError(SignalingChannelListener.MISSING_USER, "No user is logged in");
        else ConversationClient.get().getSignallingChannel().leaveConversation(
                    this.conversationId,
                    member.getMemberId(),
                    leaveListener);
    }

    /**
     * Send a typing indicator {{@link com.nexmo.sdk.conversation.client.Member.TYPING_INDICATOR#ON} event for the
     * current member of a conversation.
     *
     * @param listener  The listener in charge of dispatching the completion result.
     */
    public void startTyping(TypingSendListener listener) {
        sendTyping(Member.TYPING_INDICATOR.ON, listener);
    }

    /**
     * Send a typing indicator {@link com.nexmo.sdk.conversation.client.Member.TYPING_INDICATOR#OFF} event for the
     * current member of a conversation.
     *
     * @param listener  The listener in charge of dispatching the completion result.
     */
    public void stopTyping(TypingSendListener listener) {
        sendTyping(Member.TYPING_INDICATOR.OFF, listener);
    }

    /**
     * Listen for incoming text messages from this conversation.
     *
     * @param textListener Text event listener for notifying upon incoming text related events.
     */
    public void addTextListener(TextListener textListener) {
        ConversationClient.get().getSignallingChannel().addTextListener(this.conversationId, textListener);
    }

    /**
     * Stop listening for incoming text messages in this conversation.
     *
     * @param textListener Text event listener for notifying upon incoming or deleted text events.
     */
    public void removeTextListener(TextListener textListener) {
        ConversationClient.get().getSignallingChannel().removeTextListener(this.conversationId, textListener);
    }

    /**
     * Listen for incoming image messages from this conversation.
     *
     * @param imageListener Image event listener for notifying upon incoming image related events.
     */
    public void addImageListener(ImageListener imageListener) {
        ConversationClient.get().getSignallingChannel().addImageListener(this.conversationId, imageListener);
    }

    /**
     * what about all the listeners? make a clearListeners method
     * Stop listening for incoming image related events.
     *
     * @param imageListener  Image event listener to be removed.
     */
    public void removeImageListener(ImageListener imageListener) {
        ConversationClient.get().getSignallingChannel().removeImageListener(this.conversationId, imageListener);
    }

    /**
     * Send a Text event to a conversation.
     *
     * tofo addTextListener, removeTextListener
     * <p> For listening to incoming/sent messages events, register using {@link Conversation#addTextListener(TextListener)} </p>
     *
     * @param message          The payload.
     * @param textSendListener The listener in charge of dispatching the completion result.
     */
    public void sendText(String message, TextSendListener textSendListener) {
        if (textSendListener != null) {
            if (this.getConversationId() == null)
                textSendListener.onError(ConversationListener.MISSING_CONVERSATION, "Missing conversation");
            else if (ConversationClient.get().getSignallingChannel().isLoggedIn() != null)
                ConversationClient.get().getSignallingChannel().sendText(this, message, textSendListener);
            else
                textSendListener.onError(SignalingChannelListener.MISSING_USER, "No user is logged in");
        }
        else
            Log.d(TAG, "TextSendListener is mandatory");
    }

    /**
     * Send/Upload an Image event to a conversation.
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
     * Note: at this only the image path is allowed, but bitmap or file will soon be added.
     * @param imagePath         The image location.
     * @param imageSendListener The completion listener.
     */
    public void sendImage(String imagePath, ImageSendListener imageSendListener) {
        if (imageSendListener == null)
            Log.d(TAG, "ImageSendListener is mandatory");
        else if (this.conversationId == null)
            imageSendListener.onError(ConversationListener.MISSING_CONVERSATION, "Missing conversation");
        else if (ConversationClient.get().getSignallingChannel().isLoggedIn() != null)
            ConversationClient.get().getSignallingChannel().sendImage(this, imagePath, imageSendListener);
        else
            imageSendListener.onError(SignalingChannelListener.MISSING_USER, "No user is logged in");
    }

    /**
     * Delete a text event.
     *
     * @param textEvent    The text message that needs to be deleted.
     */
    public void deleteTextEvent(Text textEvent, EventDeleteListener eventDeleteListener) {
        attemptDeleteEvent(textEvent.getId(), eventDeleteListener);
    }

    /**
     * Delete an image event.
     *
     * @param imageEvent    The image message that needs to be deleted.
     */
    public void deleteImageEvent(Image imageEvent, EventDeleteListener eventDeleteListener) {
        attemptDeleteEvent(imageEvent.getId(), eventDeleteListener);
    }

    /**
     * Register for receiving typing indicator events from other members.
     *
     * @param typingListener The event listener.
     */
    public void addTypingListener(MemberTypingListener typingListener) {
        if (typingListener == null)
            Log.d(TAG, "ImageSendListener is mandatory");
        else if (this.conversationId == null)
            typingListener.onError(ConversationListener.MISSING_CONVERSATION, "Missing conversation");
        else if (ConversationClient.get().getSignallingChannel().isLoggedIn() != null)
            ConversationClient.get().getSignallingChannel().addTypeListener(this.conversationId, typingListener);
        else
            typingListener.onError(SignalingChannelListener.MISSING_USER, "No user is logged in");
    }

    /**
     * Unregister from receiving typing indicator events on this conversation.
     *
     * @param typingListener The typing event listener to be removed.
     */
    public void removeTypingListener(MemberTypingListener typingListener) {
        ConversationClient.get().getSignallingChannel().removeTypeListener(this.conversationId, typingListener);
    }

    public void addTextSeenReceiptListener(TextSeenReceiptListener textSeenReceiptListener) {
        ConversationClient.get().getSignallingChannel().addSeenListener(this.conversationId, textSeenReceiptListener);
    }

    public void removeTextSeenReceiptListener(TextSeenReceiptListener textSeenReceiptListener) {
        ConversationClient.get().getSignallingChannel().removeSeenListener(this.conversationId, textSeenReceiptListener);
    }

    public void addImageSeenReceiptListener(ImageSeenReceiptListener imageSeenReceiptListener) {
        ConversationClient.get().getSignallingChannel().addSeenListener(this.conversationId, imageSeenReceiptListener);
    }

    public void removeImageSeenReceiptListener(ImageSeenReceiptListener imageSeenReceiptListener) {
        ConversationClient.get().getSignallingChannel().removeSeenListener(this.conversationId, imageSeenReceiptListener);
    }

    public String getName() {
        return name;
    }

    public String getMemberId() {
        if (this.self != null)
            return this.self.getMemberId();
        else if (this.memberId != null)
            return this.memberId;
        else
            return null;
    }

    public Member getMember(final String member_id) {
        for (Member member : this.members) {
            if (member.getMemberId().equals(member_id))
                return member;
        }
        return null;
    }

    public Member getMember(final User user) {
        for (Member member : this.members) {
            if (member.getUser_id().equals(user.getUserId()))
                return member;
        }
        return null;
    }

    public Text getMessageByIndex(int index) {
        if (this.messages.size() >= index)
            return this.messages.get(index);
        return null;
    }

    public Text getMessage(final String message_id) {
        for (Text message : this.messages) {
            if (message.getId().equals(message_id))
                return message;
        }
        return null;
    }

    public String getConversationId() {
        return this.conversationId;
    }

    public void addMember(Member member) {
        synchronized(this) {
            this.members.add(member);
        }
    }

    public void setMembers(List<Member> members) {
        synchronized(this) {
            this.members = members;
        }
    }

    public void setMessages(List<Text> messages) {
        synchronized(this) {
            clearMessages();
            this.messages = messages;
        }
    }

    public void setImages(List<Image> images) {
        synchronized(this) {
            clearImages();
            this.images = images;
        }
    }

    public void addMessage(Text message) {
        synchronized(this) {
            this.messages.add(message);
        }
    }

    public void addImageEvent(Image image) {
        synchronized(this) {
            this.images.add(image);
        }
    }

    public List<Text> getMessages() {
        return this.messages;
    }

    public Text findText(final String id) {
        for (Text text : this.messages)
            if (text.getId().equals(id))
                return text;

        return null;
    }

    public Image findImage(final String id) {
        for (Image image : this.images)
            if (image.getId().equals(id))
                return image;

        return null;
    }

    public List<Image> getImages() {
        return this.images;
    }

    public List<Member> getMembers() {
        return this.members;
    }

    public Member getSelf() {
        return this.self;
    }

    public void setSelf(Member self) {
        synchronized(this) {
            this.self = self;
            this.memberId = self.getMemberId();
        }
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getLastEventId() {
        return this.lastEventId;
    }

    public void updateLastEventId(String lastEventId) {
        this.lastEventId = lastEventId;
    }

    protected  void clearMessages() {
        synchronized(this) {
            if (this.messages != null) {
                this.messages.clear();
                this.messages = new ArrayList<>();
                //clear images as well
            }
        }
    }

    protected void clearImages() {
        synchronized(this) {
            if (this.images != null) {
                this.images.clear();
                this.images = new ArrayList<>();
            }
        }
    }

    protected Conversation(Parcel in) {
        this.name = in.readString();
        this.conversationId = in.readString();
        this.memberId = in.readString();
        in.readTypedList(this.members, Member.CREATOR);
        in.readTypedList(this.messages, Text.CREATOR);
        //in.readTypedList(this.images, Image.CREATOR);
        //this.self = in.readParcelable(Member.class.getClassLoader());
        this.creationDate = (Date) in.readSerializable();
        this.lastEventId = in.readString();
    }

    public static final Creator<Conversation> CREATOR = new Creator<Conversation>() {
        @Override
        public Conversation createFromParcel(Parcel in) {
            return new Conversation(in);
        }

        @Override
        public Conversation[] newArray(int size) {
            return new Conversation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.conversationId);
        dest.writeString(this.getMemberId());
        dest.writeTypedList(this.members);
        dest.writeTypedList(this.messages);
        //dest.writeTypedList(this.images);
        //dest.writeParcelable(this.self, 0);
        dest.writeSerializable(this.creationDate);
        dest.writeString(this.lastEventId);
    }

    @Override
    public String toString() {
        return TAG + " name: " + (this.name != null ? this.name : "") + ". conversationId: " + (this.conversationId != null ? this.conversationId : "") +
                ".memberId: " + (this.getMemberId() != null ? this.getMemberId() : "") +
                ".creation_time: " + (this.creationDate != null ? this.creationDate : "") +
                ".lastEventId: " + (this.lastEventId != null ? this.lastEventId : "");
    }

    private void attemptDeleteEvent(String eventId, EventDeleteListener eventDeleteListener) {
        if (eventDeleteListener == null)
            Log.d(TAG, "EventDeleteListener is mandatory");
        else if (this.conversationId == null)
            eventDeleteListener.onError(ConversationListener.MISSING_CONVERSATION, "Missing conversation");
        else if (ConversationClient.get().getSignallingChannel().isLoggedIn() != null)
            ConversationClient.get().getSignallingChannel().deleteMessage(this, eventId, eventDeleteListener);
        else
            eventDeleteListener.onError(SignalingChannelListener.MISSING_USER, "No user is logged in");
    }

    private void sendTyping(Member.TYPING_INDICATOR indicator, TypingSendListener listener) {
        if (TextUtils.isEmpty(this.conversationId))
            listener.onError(ConversationGenericListener.MISSING_PARAMS, "This conversation does not have an id.");
        else if (ConversationClient.get().getSignallingChannel().isLoggedIn() == null)
            listener.onError(SignalingChannelListener.MISSING_USER, "No user is logged in");
        else ConversationClient.get().getSignallingChannel().sendTypingIndicator(
                    this,
                    indicator,
                    listener);
    }
}
