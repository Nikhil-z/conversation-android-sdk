/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.core.client;

import android.database.SQLException;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.ConversationClient;
import com.nexmo.sdk.conversation.client.Image;
import com.nexmo.sdk.conversation.client.ImageRepresentation;
import com.nexmo.sdk.conversation.client.Member;
import com.nexmo.sdk.conversation.client.SeenReceipt;
import com.nexmo.sdk.conversation.client.Text;
import com.nexmo.sdk.conversation.client.User;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.ConversationCreateListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.EventDeleteListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.ImageSendListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.InviteSendListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.JoinListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.LeaveListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.MarkedAsSeenListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.PushEnableListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.TextSendListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.TypingSendListener;
import com.nexmo.sdk.conversation.client.event.ConversationGenericListener;
import com.nexmo.sdk.conversation.client.event.ConversationInvitedListener;
import com.nexmo.sdk.conversation.client.event.ConversationListListener;
import com.nexmo.sdk.conversation.client.event.ConversationListener;
import com.nexmo.sdk.conversation.client.event.ImageListener;
import com.nexmo.sdk.conversation.client.event.ImageSeenReceiptListener;
import com.nexmo.sdk.conversation.client.event.MemberInvitedListener;
import com.nexmo.sdk.conversation.client.event.MemberJoinedListener;
import com.nexmo.sdk.conversation.client.event.MemberLeftListener;
import com.nexmo.sdk.conversation.client.event.MemberTypingListener;
import com.nexmo.sdk.conversation.client.event.SignalingChannelListener;
import com.nexmo.sdk.conversation.client.event.TextListener;
import com.nexmo.sdk.conversation.client.event.TextSeenReceiptListener;
import com.nexmo.sdk.conversation.client.event.network.NetworkingStateListener;
import com.nexmo.sdk.conversation.core.client.request.CreateRequest;
import com.nexmo.sdk.conversation.core.client.request.DeleteEventRequest;
import com.nexmo.sdk.conversation.core.client.request.GetConversationRequest;
import com.nexmo.sdk.conversation.core.client.request.InviteRequest;
import com.nexmo.sdk.conversation.core.client.request.JoinRequest;
import com.nexmo.sdk.conversation.core.client.request.LeaveRequest;
import com.nexmo.sdk.conversation.core.client.request.MarkSeenRequest;
import com.nexmo.sdk.conversation.core.client.request.PushSubscribeRequest;
import com.nexmo.sdk.conversation.core.client.request.Request;
import com.nexmo.sdk.conversation.core.client.request.SendMessageRequest;
import com.nexmo.sdk.conversation.core.client.request.TypingIndicatorRequest;
import com.nexmo.sdk.conversation.core.networking.ImageDownloader;
import com.nexmo.sdk.conversation.core.networking.ImageUploader;
import com.nexmo.sdk.conversation.core.persistence.CacheDB;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.nexmo.sdk.conversation.core.client.request.Request.TYPE.GET;
import static com.nexmo.sdk.conversation.core.client.request.Request.TYPE.MARK_IMAGE_SEEN;

/**will
 * ConversationSignalingChannel.
 */
public class ConversationSignalingChannel implements SignalingChannelListener {

    private static final String TAG = ConversationSignalingChannel.class.getSimpleName();
    private ConversationClient conversationClient;
    private SocketClient socketClient  = new SocketClient();

    // pending requests that await for 'success' or 'error' signals.
    private Map<String, CreateRequest> createRequestMap = new ConcurrentHashMap<>();
    private Map<String, PushSubscribeRequest> pushSubscribeRequestMap = new ConcurrentHashMap<>();
    private Map<String, JoinRequest> joinRequestMap = new ConcurrentHashMap<>();
    private Map<String, InviteRequest> inviteRequestMap = new ConcurrentHashMap<>();
    private Map<String, LeaveRequest> leaveRequestMap = new ConcurrentHashMap<>();
    private Map<String, GetConversationRequest> getConversationRequestMap = new ConcurrentHashMap<>();
    private Map<String, GetConversationRequest> getTextEventRequestMap = new ConcurrentHashMap<>();
    private ConversationListListener conversationListListener;
    private Map<String, SendMessageRequest> sendMessageRequestMap = new ConcurrentHashMap<>();
    //private Map<String, Imag> sendImageRequestMap = new ConcurrentHashMap<>();
    private Map<String, MarkSeenRequest> markSeenRequestMap = new ConcurrentHashMap<>();
    private Map<String, DeleteEventRequest> deleteTextRequestMap = new ConcurrentHashMap<>();
    private Map<String, TypingIndicatorRequest> typingIndicatorRequestMap = new ConcurrentHashMap<>();

    // list of joined/invited conversations.
    private List<Conversation> conversationList = Collections.synchronizedList(new ArrayList<Conversation>());
    // array of member events /per conversation
    private Map<String, ArrayList<MemberJoinedListener>> memberJoinedListenerMap = new ConcurrentHashMap<>();
    private Map<String, ArrayList<MemberLeftListener>> memberLeftListenerMap = new ConcurrentHashMap<>();
    private Map<String, ArrayList<MemberInvitedListener>> memberInvitedListenerMap = new ConcurrentHashMap<>();
    private List<ConversationInvitedListener> conversationInvitesListeners = Collections.synchronizedList(new ArrayList<ConversationInvitedListener>());
    private Map<String, ArrayList<TextListener>> textListenerMap = new ConcurrentHashMap<>();
    private Map<String, ArrayList<ImageListener>> imageListenerMap = new ConcurrentHashMap<>();
    private Map<String, ArrayList<MemberTypingListener>> textTypeListenerMap = new ConcurrentHashMap<>();
    private Map<String, ArrayList<TextSeenReceiptListener>> seenReceiptListenerMap = new ConcurrentHashMap<>();
    private Map<String, ArrayList<ImageSeenReceiptListener>> imageSeenReceiptListenerMap = new ConcurrentHashMap<>();


    private CacheDB dbHelper;
    private static GsonBuilder gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();
    private static final Gson gson = gsonBuilder.create();

    public ConversationSignalingChannel(ConversationClient conversationClient) {
        this.conversationClient = conversationClient;
        // disable caching for beta 0.0.1
        //this.dbHelper = CacheDB.getInstance(conversationClient.getContext());
    }

    public void connect() {
        try {
            this.socketClient.connect(this.conversationClient.getEnvironmentHost(), this.conversationClient);
            this.socketClient.registerSignalChannelListener(this);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnecting() {
        return this.socketClient.getConnectionStatus() == NetworkingStateListener.NETWORK_STATE.CONNECTED;
    }

    public NetworkingStateListener.NETWORK_STATE getConnectionStatus() {
        return this.socketClient.getConnectionStatus();
    }

    // Retrieve the current user, if any.
    public User isLoggedIn() {
        User self = this.socketClient.self;
        if (self == null)
            Log.d(TAG, "No user logged in");

        return self;
    }

    public void enableAllPushNotifications(boolean enable) {
        if (enable)
            this.socketClient.pushRegister();
        else
            this.socketClient.pushUnregister();
    }

    public void enablePushNotifications(boolean enable, String cid, PushEnableListener pushEnableListener) {
        String tid = newTID();
        PushSubscribeRequest pushSubscribeRequest = new PushSubscribeRequest(Request.TYPE.PUSH_SUBSCRIBE, tid, cid, pushEnableListener);
        this.pushSubscribeRequestMap.put(tid, pushSubscribeRequest);
        this.socketClient.pushSubscribeToConversation(enable, pushSubscribeRequest);
    }

    public void logout() {
            this.socketClient.logout();
    }

    private static String newTID() {
       return UUID.randomUUID().toString();
    }

    public void newConversation(final String name, ConversationCreateListener listener) {
        String tid = newTID();
        CreateRequest createRequest = new CreateRequest(Request.TYPE.CREATE, tid, name, listener);
        this.createRequestMap.put(tid, createRequest);
        this.socketClient.newConversation(createRequest);
    }

    public void joinConversation(String conversationId, JoinListener joinListener) {
        if (isLoggedIn() != null) {
            String tid = newTID();
            JoinRequest joinRequest = new JoinRequest(
                    Request.TYPE.JOIN,
                    tid,
                    conversationId,
                    joinListener);

            this.joinRequestMap.put(tid, joinRequest);
            this.socketClient.joinConversation(joinRequest);
        }
        else
            joinListener.onError(ConversationGenericListener.MISSING_USER, "No user is logged in");
    }

    public void acceptInvitation(String conversationId, String cName, String memberId, JoinListener joinListener) {
        if (isLoggedIn() != null) {
            String tid = newTID();
            JoinRequest joinRequest = new JoinRequest(
                    Request.TYPE.JOIN,
                    tid,
                    conversationId,
                    cName,
                    memberId,
                    joinListener);

            this.joinRequestMap.put(tid, joinRequest);
            this.socketClient.joinConversation(joinRequest);
        }
        else
            joinListener.onError(ConversationGenericListener.MISSING_USER, "No user is logged in");
    }

    public void addConversationInvitedListener(ConversationInvitedListener conversationInvitedListener) {
        this.conversationInvitesListeners.add(conversationInvitedListener);
    }

    public void removeConversationInvitedListener(ConversationInvitedListener conversationInvitedListener) {
        if (this.conversationInvitesListeners.contains(conversationInvitedListener))
            this.conversationInvitesListeners.remove(conversationInvitedListener);
    }

    public void addMemberJoinedListener(String cid, MemberJoinedListener memberJoinedListener){
        if (this.memberJoinedListenerMap.containsKey(cid))
            this.memberJoinedListenerMap.get(cid).add(memberJoinedListener);
        else {
            ArrayList<MemberJoinedListener> listeners = new ArrayList<>();
            listeners.add(memberJoinedListener);
            this.memberJoinedListenerMap.put(cid, listeners);
        }
    }

    public void removeMemberJoinedListener(String cid, MemberJoinedListener memberJoinedListener) {
        if (this.memberJoinedListenerMap.containsKey(cid))
            this.memberJoinedListenerMap.get(cid).remove(memberJoinedListener); //make sure the array is not emptied
    }

    public void addMemberLeftListener(String cid, MemberLeftListener memberLeftListener) {
        if (this.memberLeftListenerMap.containsKey(cid))
            this.memberLeftListenerMap.get(cid).add(memberLeftListener);
        else {
            ArrayList<MemberLeftListener> listeners = new ArrayList<>();
            listeners.add(memberLeftListener);
            this.memberLeftListenerMap.put(cid, listeners);
        }
    }

    public void removeMemberLeftListener(String cid, MemberLeftListener memberLeftListener) {
        if (this.memberLeftListenerMap.containsKey(cid))
            this.memberLeftListenerMap.get(cid).remove(memberLeftListener);
    }

    public void addMemberInvitedListener(String cid, MemberInvitedListener memberInvitedListener) {
        if (this.memberInvitedListenerMap.containsKey(cid))
            this.memberInvitedListenerMap.get(cid).add(memberInvitedListener);
        else {
            ArrayList<MemberInvitedListener> listeners = new ArrayList<>();
            listeners.add(memberInvitedListener);
            this.memberInvitedListenerMap.put(cid, listeners);
        }
    }

    public void removeMemberInvitedListener(String cid, MemberInvitedListener memberInvitedListener) {
        if (this.memberInvitedListenerMap.containsKey(cid))
            this.memberInvitedListenerMap.get(cid).remove(memberInvitedListener);
    }

    public void addTextListener(String cid, TextListener textListener) {
        if (this.textListenerMap.containsKey(cid))
            this.textListenerMap.get(cid).add(textListener);
        else {
            ArrayList<TextListener> listeners = new ArrayList<>();
            listeners.add(textListener);
            this.textListenerMap.put(cid, listeners);
        }
    }

    public void removeTextListener(String cid, TextListener textListener) {
        if (this.textListenerMap.containsKey(cid))
            this.textListenerMap.get(cid).remove(textListener); //make sure the array is not emptied
    }

    public void addImageListener(String cid, ImageListener imageListener) {
        if (this.imageListenerMap.containsKey(cid))
            this.imageListenerMap.get(cid).add(imageListener);
        else {
            ArrayList<ImageListener> listeners = new ArrayList<>();
            listeners.add(imageListener);
            this.imageListenerMap.put(cid, listeners);
        }
    }

    public void removeImageListener(String cid, ImageListener imageListener) {
        if (this.imageListenerMap.containsKey(cid))
            this.imageListenerMap.get(cid).remove(imageListener); //make sure the array is not emptied
    }

    public void addTypeListener(String cid, MemberTypingListener textTypingListener) {
        if (this.textTypeListenerMap.containsKey(cid))
            this.textTypeListenerMap.get(cid).add(textTypingListener);
        else {
            ArrayList<MemberTypingListener> listeners = new ArrayList<>();
            listeners.add(textTypingListener);
            this.textTypeListenerMap.put(cid, listeners);
        }
    }

    public void removeTypeListener(String cid, MemberTypingListener textTypingListener) {
        if (this.textTypeListenerMap.containsKey(cid))
            this.textTypeListenerMap.get(cid).remove(textTypingListener); //make sure the array is not emptied
    }

    public void addSeenListener(String cid, TextSeenReceiptListener textSeenReceiptListener) {
        if (this.seenReceiptListenerMap.containsKey(cid))
            this.seenReceiptListenerMap.get(cid).add(textSeenReceiptListener);
        else {
            ArrayList<TextSeenReceiptListener> listeners = new ArrayList<>();
            listeners.add(textSeenReceiptListener);
            this.seenReceiptListenerMap.put(cid, listeners);
        }
    }

    public void removeSeenListener(String cid, TextSeenReceiptListener textSeenReceiptListener) {
        if (this.seenReceiptListenerMap.containsKey(cid))
            this.seenReceiptListenerMap.get(cid).remove(textSeenReceiptListener);
    }

    public void addSeenListener(String cid, ImageSeenReceiptListener imageSeenReceiptListener) {
        if (this.imageSeenReceiptListenerMap.containsKey(cid))
            this.imageSeenReceiptListenerMap.get(cid).add(imageSeenReceiptListener);
        else {
            ArrayList<ImageSeenReceiptListener> listeners = new ArrayList<>();
            listeners.add(imageSeenReceiptListener);
            this.imageSeenReceiptListenerMap.put(cid, listeners);
        }
    }

    public void removeSeenListener(String cid, ImageSeenReceiptListener imageSeenReceiptListener) {
        if (this.imageSeenReceiptListenerMap.containsKey(cid))
            this.imageSeenReceiptListenerMap.get(cid).remove(imageSeenReceiptListener);
    }

    public void leaveConversation(String cid, String member_id, LeaveListener leaveListener) {
        String tid = newTID();
        Conversation pendingConversation = findConversation(cid);
        if (pendingConversation != null) {
            LeaveRequest leaveRequest = new LeaveRequest(Request.TYPE.LEAVE, tid, cid, member_id, leaveListener);
            this.leaveRequestMap.put(tid, leaveRequest);
            this.socketClient.leaveConversation(leaveRequest);
        } else
            Log.d(TAG, "missing conversation");
    }

    public void invite(String conversationId, String username, InviteSendListener inviteSendListener) {
        String tid = newTID();
        InviteRequest inviteRequest = new InviteRequest(Request.TYPE.INVITE, tid, conversationId, username, inviteSendListener);
        this.inviteRequestMap.put(tid, inviteRequest);
        this.socketClient.invite(inviteRequest);
    }

    // don't allow multiple retrievals just yet.
    public void getConversations(ConversationListListener listListener) {
        String tid = newTID();
        this.conversationListListener = listListener;
        this.conversationList.clear();
        this.socketClient.getConversations(tid);
    }

    public void openCacheDb() throws SQLException {
        if (dbHelper != null)
            dbHelper.getWritableDatabase();
    }

    public void getCachedConversations(ConversationListListener listListener) {
        this.conversationListListener = listListener;
        //readDb
    }

    public void getCachedConversation(final String cid, ConversationListener conversationListener) {
//        String tid = newTID();
//        GetConversationRequest getConversationRequest = new GetConversationRequest(GET, tid, conversationId, null, null, conversationListener);
//        this.getConversationRequestMap.put(tid, getConversationRequest);
//        this.socketClient.getConversation(getConversationRequest);
    }

    public boolean hasCachedConversations() {
        if (dbHelper != null)
            return dbHelper.hasConversations();

        return false;
    }

    public boolean hasCachedConversation(final String cid) {
        if (dbHelper != null)
            return dbHelper.hasConversation(cid);

        return false;
    }

    public void clearCache() {
        //async or not?
    }

    public void getConversation(final String conversationId, ConversationListener conversationListener) {
        String tid = newTID();
        GetConversationRequest getConversationRequest = new GetConversationRequest(GET, tid, conversationId, null, null, conversationListener);
        this.getConversationRequestMap.put(tid, getConversationRequest);
        this.socketClient.getConversation(getConversationRequest);
    }

    public void getMessages(final String cid, String startId, String endId, ConversationListener conversationListener) {
        String tid = newTID();
        GetConversationRequest getConversationRequest = new GetConversationRequest(GET, tid, cid, startId, endId, conversationListener);
        this.getTextEventRequestMap.put(tid, getConversationRequest);
        this.socketClient.getMessages(getConversationRequest);
    }

    public void sendText(Conversation conversation, final String message, TextSendListener listener) {
        String tid = newTID();
        SendMessageRequest sendMessageRequest = new SendMessageRequest(
                Request.TYPE.SEND_TEXT,
                tid,
                conversation.getConversationId(),
                conversation.getMemberId(),
                message,
                listener);
        this.sendMessageRequestMap.put(tid, sendMessageRequest);
        this.socketClient.sendText(sendMessageRequest);
    }

    // upload image to IPS, send image 'representations' to CAPI. download from media service once done.
    public void sendImage(final Conversation conversation, String imagePath, final ImageSendListener listener) {
        final String tid = newTID();

        final SendMessageRequest sendMessageRequest = new SendMessageRequest(
                Request.TYPE.SEND_IMAGE,
                tid,
                conversation.getConversationId(),
                conversation.getMemberId(),
                imagePath,
                listener);
        this.sendMessageRequestMap.put(tid, sendMessageRequest);

        final com.squareup.okhttp.Callback uploadCallback = new com.squareup.okhttp.Callback() {

            @Override
            public void onFailure(com.squareup.okhttp.Request request, IOException e) {
                Log.d(TAG, "onFailure upload " + e.toString());
                listener.onError(GENERIC_ERR, e.toString());
            }

            @Override
            public void onResponse(com.squareup.okhttp.Response response) throws IOException {
                Log.d(TAG, "onResponse upload" + response.toString());

                if (!response.isSuccessful()) {
                    listener.onError(GENERIC_ERR, "Unexpected code " + response);
                }

                String jsonData = response.body().string();
                JSONObject jsonBody=null, originalJson=null, mediumJson=null, thumbnailJson= null;
                ImageRepresentation original=null, medium=null, thumbnail=null;
                try {
                    jsonBody = new JSONObject(jsonData);
                    originalJson = jsonBody.getJSONObject("original");
                    original = gson.fromJson(originalJson.toString(), ImageRepresentation.class);
                    original.type = ImageRepresentation.TYPE.ORIGINAL;
                    Log.d(TAG, original.toString());

                    mediumJson = jsonBody.getJSONObject("medium");
                    medium = gson.fromJson(mediumJson.toString(), ImageRepresentation.class);
                    medium.type = ImageRepresentation.TYPE.MEDIUM;
                    Log.d(TAG, medium.toString());

                    thumbnailJson = jsonBody.getJSONObject("thumbnail");
                    thumbnail = gson.fromJson(thumbnailJson.toString(), ImageRepresentation.class);
                    thumbnail.type = ImageRepresentation.TYPE.THUMBNAIL;
                    Log.d(TAG, thumbnail.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                    listener.onError(GENERIC_ERR, e.toString());
                }
                updateImageRepresentations(
                        tid,
                        original,
                        medium,
                        thumbnail);

                socketClient.sendImage(sendMessageRequest, jsonBody);
                response.body().close();
            }};

        ImageUploader.uploadImage(sendMessageRequest, uploadCallback);
    }

    private void updateImageRepresentations(String tid,
                                            ImageRepresentation original,
                                            ImageRepresentation medium,
                                            ImageRepresentation thumbnail) {
        SendMessageRequest sendMessageRequest = this.sendMessageRequestMap.get(tid);
        sendMessageRequest.original = original;
        sendMessageRequest.medium = medium;
        sendMessageRequest.thumbnail = thumbnail;
    }

    public void sendSeenEvent(Text text, MarkedAsSeenListener listener) {
        String tid = newTID();
        MarkSeenRequest markSeenRequest = new MarkSeenRequest(Request.TYPE.MARK_TEXT_SEEN, tid,
                text.getConversation().getConversationId(),
                text.getConversation().getMemberId(),
                text.getId(),
                listener);
        this.markSeenRequestMap.put(tid, markSeenRequest);
        this.socketClient.sendSeenEvent(markSeenRequest);
    }

    public void sendSeenEvent(Image image, MarkedAsSeenListener listener) {
        String tid = newTID();
        MarkSeenRequest markSeenRequest = new MarkSeenRequest(MARK_IMAGE_SEEN, tid,
                image.getConversation().getConversationId(),
                image.getConversation().getMemberId(),
                image.getId(),
                listener);
        this.markSeenRequestMap.put(tid, markSeenRequest);
        this.socketClient.sendSeenEvent(markSeenRequest);
    }

    public void sendTypingIndicator(Conversation conversation, Member.TYPING_INDICATOR typingIndicator, TypingSendListener listener){
        String tid = newTID();
        TypingIndicatorRequest typingIndicatorRequest = new TypingIndicatorRequest(Request.TYPE.TYPING, tid, conversation.getConversationId(), conversation.getMemberId(), typingIndicator, listener);
        this.typingIndicatorRequestMap.put(tid, typingIndicatorRequest);
        this.socketClient.sendTypingIndicator(typingIndicatorRequest);
    }

    public void deleteMessage(Conversation conversation, String messageId, EventDeleteListener listener) {
        String tid = newTID();
        DeleteEventRequest deleteEventRequest = new DeleteEventRequest(Request.TYPE.DELETE_EVENT, tid, conversation.getConversationId(), conversation.getMemberId(), messageId, listener);
        this.deleteTextRequestMap.put(tid, deleteEventRequest);
        this.socketClient.deleteEvent(deleteEventRequest);
    }

    public void release() {
        this.socketClient.conversationList.clear();
        this.createRequestMap.clear();
        this.joinRequestMap.clear();
        this.socketClient.release();
    }

    @Override
    public void onCreate(String tid, String conversationId) {
        if (this.createRequestMap.containsKey(tid)) {
            CreateRequest request = this.createRequestMap.get(tid);
            request.conversationCreateListener.onConversationCreated(new Conversation(request.name, conversationId));
            this.createRequestMap.remove(tid);
        }
    }

    @Override
    public void onJoin(String tid, Member member) {
        if (this.joinRequestMap.containsKey(tid)){
            JoinRequest request = this.joinRequestMap.get(tid);
            //at this point we do not store the pending invitations until we actually join. TODO
            Conversation joinedConversation;

            if (TextUtils.isEmpty(request.cName))
                joinedConversation = new Conversation("", request.cid);
            else
                joinedConversation = new Conversation(request.cName, request.cid);
            joinedConversation.setSelf(member);
            joinedConversation.addMember(member);
            //pass a clone
            request.joinListener.onConversationJoined(new Conversation(joinedConversation), new Member(member));

            this.conversationList.add(joinedConversation);
            this.joinRequestMap.remove(tid);
        }

//        Log.d(TAG, "onMemberJoined ");
//        Conversation pendingConversation = findConversation(cid);
//        if(pendingConversation != null) {
//            //update member state and join date.
//            Member member = pendingConversation.getMember(memberId);
//            if(member != null)
//                synchronized(this) {
//                    member.updateState(Member.STATE.JOINED, joinedTimestamp);
//                }
//            else {
//                member = new Member(user.getUserId(), user.getName(), memberId, joinedTimestamp, null, null, Member.STATE.JOINED);
//                pendingConversation.addMember(member);
//            }
//
//            Log.d(TAG, "onMemberJoined joinedListener.size " + this.memberJoinedListenerMap.size());
//
//            if (this.memberJoinedListenerMap.containsKey(pendingConversation.getConversationId())) {
//                List<MemberJoinedListener> listeners = this.memberJoinedListenerMap.get(cid);
//                for (MemberJoinedListener listener : listeners)
//                    listener.onJoined(pendingConversation, new Member(member));
//            }
//        }
    }

    @Override
    public void onLeft(String tid,  Date invited, Date joined, Date left) {
        if (this.leaveRequestMap.containsKey(tid)) {
            LeaveRequest request = this.leaveRequestMap.get(tid);
            Conversation pendingConversation = findConversation(request.cid);
            if (pendingConversation != null) {
                Member member = pendingConversation.getMember(request.memberId);
                //update Member with left timestamp
                if (member != null)
                    synchronized(this) {
                        member.updateState(Member.STATE.LEFT, left);
                    }
            }
            request.leaveListener.onConversationLeft();
            this.leaveRequestMap.remove(tid);
        }
    }

    @Override
    public void onConversations(List<Conversation> conversations) {
        this.conversationList = conversations;
        this.conversationListListener.onConversationList(new ArrayList<>(conversations));
        // update DB after dispatching events
        //CacheDB.getInstance(this.conversationClient.getContext()).insertConversations(conversations);
    }

    @Override
    public void onConversation(String tid, Conversation conversation) {
        if (this.getConversationRequestMap.containsKey(tid)) {
            //update list instead, make sure does not overlap with onConversations;

            Conversation pendingConversation = findConversation(conversation.getConversationId());
            if (pendingConversation != null)
                pendingConversation.setMembers(conversation.getMembers());
            GetConversationRequest request = this.getConversationRequestMap.get(tid);
            request.conversationListener.onConversationUpdated(new Conversation(conversation));
            this.getConversationRequestMap.remove(tid);
//            this.dbHelper.updateConversation(conversation);
        }
    }

    //todo optimize this
    @Override
    public void onEventsHistory(String tid, List<Text> messages, List<Image> images, List<SeenReceipt> seenReceipts) {
        if (this.getTextEventRequestMap.containsKey(tid)) {
            GetConversationRequest request = this.getTextEventRequestMap.get(tid);
            //update list, make sure does not overlap with onConversations;
            //update conversation entry, don't remove the members
            Conversation pendingConversation = findConversation(request.cid);
            if (pendingConversation != null) {
                for(Text message : messages) {
                    message.setConversation(pendingConversation);
                    // TODO  else if member not found we need to sync the member list!
                    //workaround: only send the member id and let the dev figure it out
                    String memberId = message.getMember().getMemberId();
                    Member sender = pendingConversation.getMember(memberId);
                    if (sender != null)
                        message.setMember(sender);
                }
                for(Image image : images) {
                    image.setConversation(pendingConversation);
                    String memberId = image.getMember().getMemberId();
                    Member sender = pendingConversation.getMember(memberId);
                    if (sender != null)
                        image.setMember(sender);

                    //download representations
                    downloadImageRepresentation(pendingConversation, image, image.getOriginal());
                    downloadImageRepresentation(pendingConversation, image, image.getMedium());
                    downloadImageRepresentation(pendingConversation, image, image.getThumbnail());
                }
                for (SeenReceipt seenReceipt : seenReceipts) {
                    for (Text message : messages) {
                        if (message.getId().equals(seenReceipt.getEvent_id())) {
                            message.addSeenReceipt(seenReceipt);
                            break;
                        }
                    }
                    for (Image image : images) {
                        if (image.getId().equals(seenReceipt.getEvent_id())) {
                            image.addSeenReceipt(seenReceipt);
                            break;
                        }
                    }
                }

                synchronized(this) {
                    pendingConversation.setMessages(messages);
                    pendingConversation.setImages(images);
                }
                request.conversationListener.onConversationUpdated(new Conversation(pendingConversation));
            }
            this.getTextEventRequestMap.remove(tid);
        }
    }

    @Override
    public void onTextSent(String tid, String textId, Date timestamp) {
        Log.d(TAG, "onTextSent");
        if (this.sendMessageRequestMap.containsKey(tid)) {
            SendMessageRequest request = this.sendMessageRequestMap.get(tid);
            Conversation pendingConversation = findConversation(request.cid);
            if (pendingConversation != null) {
                Text incomingText = new Text(request.message, textId, timestamp, pendingConversation.getSelf());
                synchronized(this){
                    incomingText.setConversation(pendingConversation);
                    pendingConversation.addMessage(incomingText);
                    incomingText.getMember();
                    pendingConversation.updateLastEventId(textId);
                    //debug check the memberId
                }
                request.textSendListener.onTextSent(pendingConversation, incomingText);
            }
            this.sendMessageRequestMap.remove(tid);
        }
    }

    @Override
    public void onImageSent(String tid, String imageId, Date timestamp) {
        if (this.sendMessageRequestMap.containsKey(tid)) {
            SendMessageRequest request = this.sendMessageRequestMap.get(tid);
            Conversation pendingConversation = findConversation(request.cid);
            if (pendingConversation != null) {
                //Image(final String payload, final String id, final Date timestamp, final String url) {
                Image image = new Image(request.message, imageId, timestamp, request.message);
                synchronized(this){
                    image.setMember(pendingConversation.getSelf());
                    image.setConversation(pendingConversation);
                    image.addRepresentations(request.original, request.medium, request.thumbnail);
                    pendingConversation.addImageEvent(image);

                    pendingConversation.updateLastEventId(imageId);
                }
                request.imageSendListener.onImageSent(pendingConversation, image);
            }
            this.sendMessageRequestMap.remove(tid);
        }
    }

    @Override
    public void onTextRemoved(String tid, String eventId) {
        //remove payload from the text and upload conversation list.
        if (this.deleteTextRequestMap.containsKey(tid)) {
            DeleteEventRequest request = this.deleteTextRequestMap.get(tid);
            Conversation pendingConversation = findConversation(request.cid);
            if (pendingConversation != null) {
                synchronized(this){
                    pendingConversation.getMessage(request.messageId).setDeleteEventId(eventId);
                }
                request.eventDeleteListener.onDeleted(pendingConversation);
            }
            this.sendMessageRequestMap.remove(tid);
        }
    }

    @Override
    public void onMarkedAsSeen(String tid) {
        if (this.markSeenRequestMap.containsKey(tid)) {
            MarkSeenRequest request = this.markSeenRequestMap.get(tid);
            Conversation pendingConversation = findConversation(request.cid);
            if (pendingConversation != null) {
                // update internal conversation object.setMessage(seen boolean)
                synchronized(this){
                    //pendingConversation.addMessage(incomingText);
                }
                request.listener.onMarkedAsSeen(pendingConversation);
            }
            this.markSeenRequestMap.remove(tid);
        }
    }

    @Override
    public void onTypingOn(String tid) {
        if (this.typingIndicatorRequestMap.containsKey(tid)) {
            TypingIndicatorRequest request = this.typingIndicatorRequestMap.get(tid);
            Conversation pendingConversation = findConversation(request.cid);
            if (pendingConversation != null) {
                synchronized(this) {
                    pendingConversation.getMember(request.memberId).setTypingIndicator(Member.TYPING_INDICATOR.ON);
                }
                request.typingSendListener.onTypingSent(pendingConversation, Member.TYPING_INDICATOR.ON);
            }
            this.typingIndicatorRequestMap.remove(tid);
        }
    }

    @Override
    public void onTypingOff(String tid) {
        if (this.typingIndicatorRequestMap.containsKey(tid)) {
            TypingIndicatorRequest request = this.typingIndicatorRequestMap.get(tid);
            Conversation pendingConversation = findConversation(request.cid);
            if (pendingConversation != null) {
                synchronized(this) {
                    pendingConversation.getMember(request.memberId).setTypingIndicator(Member.TYPING_INDICATOR.OFF);
                }
                request.typingSendListener.onTypingSent(pendingConversation, Member.TYPING_INDICATOR.OFF);
            }
            this.typingIndicatorRequestMap.remove(tid);
        }
    }

    @Override
    public void onInvitationSent(String tid) {
        if (this.inviteRequestMap.containsKey(tid)) {
            InviteRequest request = this.inviteRequestMap.get(tid);
            Conversation pendingConversation = findConversation(request.cid);
            if (pendingConversation != null) {
                //todo set member invited.
                request.inviteSendListener.onInviteSent(request.cid, request.user);
                }
            this.inviteRequestMap.remove(tid);
        }
    }

    // update internal state regardless of listeners being set
    @Override
    public void onMemberJoined(String cid, String memberId, User user, Date joinedTimestamp) {
        Log.d(TAG, "onMemberJoined ");
        Conversation pendingConversation = findConversation(cid);
        if(pendingConversation != null) {
            //update member state and join date.
            Member member = pendingConversation.getMember(memberId);
            if(member != null)
                synchronized(this) {
                    member.updateState(Member.STATE.JOINED, joinedTimestamp);
                }
            else {
                member = new Member(user.getUserId(), user.getName(), memberId, joinedTimestamp, null, null, Member.STATE.JOINED);
                pendingConversation.addMember(member);
            }

            if (this.memberJoinedListenerMap.containsKey(pendingConversation.getConversationId())) {
                List<MemberJoinedListener> listeners = this.memberJoinedListenerMap.get(cid);
                for (MemberJoinedListener listener : listeners)
                    listener.onJoined(pendingConversation, new Member(member));
            }
        }
    }

    @Override
    public void onMemberInvited(String cid, String cName, Member invitedMember, String invitedByMemberId, String invitedByUsername) {
        Log.d(TAG, "onMemberInvited");
        Conversation pendingConversation = findConversation(cid);
        List<MemberInvitedListener> listeners = this.memberInvitedListenerMap.get(cid);

        if (pendingConversation != null) {
            Log.d(TAG, "onMemberInvited to one of the conversations");
            //add new member
            pendingConversation.addMember(invitedMember);

            if (this.memberInvitedListenerMap.containsKey(pendingConversation.getConversationId()))
                for (MemberInvitedListener listener : listeners)
                    listener.onMemberInvited(pendingConversation, new Member(invitedMember), invitedByMemberId, invitedByUsername);
        } else {
            Log.d(TAG, "User received an invitation");
            Conversation invitedConversation = new Conversation(cName, cid);
            invitedConversation.addMember(invitedMember);
            conversationList.add(invitedConversation);

            for (ConversationInvitedListener listener : this.conversationInvitesListeners)
                listener.onConversationInvited(invitedConversation, new Member(invitedMember), invitedByMemberId, invitedByUsername);
        }
    }

    @Override
    public void onMemberLeft(String cid, String memberId, User user, Date invited, Date joined, Date left) {
        Log.d(TAG, "onMemberLeft");
        Conversation pendingConversation = findConversation(cid);
        List<MemberLeftListener> listeners = this.memberLeftListenerMap.get(cid);

        if (pendingConversation != null) {
            if (conversationClient != null) {
                //update member state.
                Member member;
                if (TextUtils.isEmpty(memberId)) {
                    member = pendingConversation.getMember(user);
                    if (member != null)
                        synchronized(this) {
                            member.updateState(Member.STATE.LEFT, left);
                        }
                }
                else {
                    member = pendingConversation.getMember(memberId);
                    if (member != null)
                        synchronized(this) {
                            member.updateState(Member.STATE.LEFT, left);
                        }
                    else
                        member = new Member(user.getUserId(), user.getName(), memberId, joined, invited, left, Member.STATE.LEFT);
                }

                if (this.memberLeftListenerMap.containsKey(pendingConversation.getConversationId()))
                    for (MemberLeftListener listener : listeners)
                        listener.onMemberLeft(pendingConversation, new Member(member));
            }
        }
    }

    @Override
    public void onTypingOnReceived(String cid, String memberId) {
        dispatchMemberTypeEvent(cid, memberId, Member.TYPING_INDICATOR.ON);
    }

    @Override
    public void onTypingOffReceived(String cid, String memberId) {
        dispatchMemberTypeEvent(cid, memberId, Member.TYPING_INDICATOR.OFF);
    }

    @Override
    public void onTextReceived(String cid, String memberId, Text text) {
        Conversation pendingConversation = findConversation(cid);
        //avoid duplicate texts on same id.
        if (pendingConversation != null) {
            if (!containsMessage(pendingConversation, text)) {
                // add the message
                synchronized(this) {
                    text.setMember(pendingConversation.getMember(memberId));
                    pendingConversation.addMessage(text);
                    text.setConversation(pendingConversation);
                    pendingConversation.updateLastEventId(text.getId());
                }
                if (this.textListenerMap.containsKey(pendingConversation.getConversationId())) {
                    List<TextListener> listeners = this.textListenerMap.get(cid);
                    for (TextListener listener : listeners)
                        listener.onTextReceived(pendingConversation, text);
                }
            }
            else {
                Log.d(TAG, "onTextReceived for relayed-own text");
                for (Text message: pendingConversation.getMessages()) {
                    if (message.getId().equals(text.getId())) {
                        //update fields
                        if (this.textListenerMap.containsKey(pendingConversation.getConversationId())) {
                            List<TextListener> listeners = this.textListenerMap.get(cid);
                            for(TextListener listener : listeners)
                                listener.onTextReceived(pendingConversation, message);
                        }
                        break;
                    }
                }
            }
        }
        // else completely new event, conversations not synced yet.
        //keep the event until sync is complete.
    }

    @Override
    public void onTextDeleted(String cid, String memberId, String eventId, Date timestamp) {
        // remove payload from the text and refresh conversation list.
        Conversation pendingConversation = findConversation(cid);
        if (pendingConversation != null) {
            Text deletedMessage =  pendingConversation.getMessage(eventId);
            if (deletedMessage != null)
                synchronized(this) {
                    deletedMessage.setPayload(null);
                    //set date deleted. todo see if date created has to be kept.
                }

            // notify all listeners
            if (this.textListenerMap.containsKey(cid)) {
                List<TextListener> listeners = this.textListenerMap.get(cid);
                for (TextListener listener : listeners)
                    listener.onTextDeleted(
                            pendingConversation,
                            deletedMessage,
                            pendingConversation.getMember(memberId));
            }
        }
    }

    @Override
    public void onEventSeen(String cid, String memberId, String eventId, Request.TYPE type, Date timestamp) {
        //add seen receipt
        Conversation pendingConversation = findConversation(cid);
        if (pendingConversation != null) {
            SeenReceipt seenReceipt = new SeenReceipt(eventId, memberId, timestamp);

            switch(type) {
                case MARK_TEXT_SEEN: {
                    Text seenMessage = pendingConversation.findText(eventId);

                    if(seenMessage != null) {
                        synchronized(this) {
                            seenMessage.addSeenReceipt(seenReceipt);
                        }

                        if (this.seenReceiptListenerMap.containsKey(cid)) {
                            List<TextSeenReceiptListener> listeners = this.seenReceiptListenerMap.get(cid);
                            for (TextSeenReceiptListener listener : listeners)
                                listener.onSeenReceipt(seenMessage, pendingConversation.getMember(memberId), seenReceipt);
                        }
                    }
                    break;
                }
                case MARK_IMAGE_SEEN:{
                    Image seenMessage = pendingConversation.findImage(eventId);

                    if(seenMessage != null) {
                        synchronized(this) {
                            seenMessage.addSeenReceipt(seenReceipt);
                        }

                        if (this.imageSeenReceiptListenerMap.containsKey(cid)) {
                            List<ImageSeenReceiptListener> listeners = this.imageSeenReceiptListenerMap.get(cid);
                            for (ImageSeenReceiptListener listener : listeners)
                                listener.onSeenReceipt(seenMessage, pendingConversation.getMember(memberId), seenReceipt);
                        }
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onImageReceived(String cid, String memberId, Image image) {
        Log.d(TAG, "onImageReceived");
        Conversation pendingConversation = findConversation(cid);
        //avoid duplicate texts on same id.
        if (pendingConversation != null) {
            //if (!containsImage(pendingConversation, image)) {
                Log.d(TAG, "onImageReceived from someone else,add it");
                //download representations
                downloadImageRepresentation(pendingConversation, image, image.getOriginal());
                downloadImageRepresentation(pendingConversation, image, image.getMedium());
                downloadImageRepresentation(pendingConversation, image, image.getThumbnail());
                // add the image message
                synchronized(this) {
                    image.setMember(pendingConversation.getMember(memberId));
                    image.setConversation(pendingConversation);

                    pendingConversation.addImageEvent(image);
                    pendingConversation.updateLastEventId(image.getId());
                }
            //} else
                //Log.d(TAG, "Received own relayed image");
            if (this.imageListenerMap.containsKey(cid)) {
                List<ImageListener> listeners = this.imageListenerMap.get(cid);
                for (ImageListener listener : listeners)
                    listener.onImageReceived(pendingConversation, image);
            }
        }
        else Log.d(TAG, "onImageReceived for not-sync conversation");
    }

    //download from media service
    //if this is for the history download, we need different callback

    private void downloadImageRepresentation(final Conversation conversation, final Image image, final ImageRepresentation imageRepresentation) {
        final com.squareup.okhttp.Callback downloadCallback = new com.squareup.okhttp.Callback() {

            @Override
            public void onFailure(com.squareup.okhttp.Request request, IOException e) {
                Log.d(TAG, "onFailure download " + e.toString());
                if (imageListenerMap.containsKey(conversation.getConversationId())) {
                    List<ImageListener> listeners = imageListenerMap.get(conversation.getConversationId());
                    for (ImageListener listener : listeners)
                        listener.onDownloadFailed(null, null);
                    //Conversation conversation, Image image
                }
            }

            @Override
            public void onResponse(com.squareup.okhttp.Response response) throws IOException {
                Log.d(TAG, "onResponse download");

                if (!response.isSuccessful()) {
                    //recoverable error.
                    if (imageListenerMap.containsKey(conversation.getConversationId())) {
                        List<ImageListener> listeners = imageListenerMap.get(conversation.getConversationId());
                        for (ImageListener listener : listeners)
                            listener.onDownloadFailed(null, null);
                    }
                }

                Bitmap bitmap = ImageDownloader.decodeImage(response);
                synchronized (this){
                    switch(imageRepresentation.type) {
                        case ORIGINAL: {
                            image.getOriginal().bitmap = bitmap;
                            break;
                        }
                        case MEDIUM: {
                            image.getMedium().bitmap = bitmap;
                            break;
                        }
                        case THUMBNAIL:{
                            image.getThumbnail().bitmap = bitmap;
                            break;
                        }
                    }
                }

                if (imageListenerMap.containsKey(conversation.getConversationId())) {
                    List<ImageListener> listeners = imageListenerMap.get(conversation.getConversationId());
                    for (ImageListener listener : listeners)
                        listener.onImageDownloaded(conversation, image);
                }
                response.body().close();
            }};

        ImageDownloader.downloadImage(imageRepresentation, downloadCallback);
    }

    @Override
    public void onPushSubscribedToConversation(String tid) {
        if (this.pushSubscribeRequestMap.containsKey(tid)) {
            PushSubscribeRequest request = this.pushSubscribeRequestMap.get(tid);
            request.pushEnableListener.onSuccess();

            this.pushSubscribeRequestMap.remove(tid);
        }
    }

    private boolean containsMessage(Conversation conversation, Text text) {
        for (Text message: conversation.getMessages()) {
            if (message.getId().equals(text.getId()))
                return true;
        }
        return false;
    }

    private boolean containsImage(Conversation conversation, Image image) {
        for (Image message: conversation.getImages()) {
            if (message.getId().equals(image.getId()))
                return true;
        }
        return false;
    }

    @Override
    public void onError(int errCode, String errMessage) {

    }

    private Conversation findConversation(final String cid) {
        synchronized(this.conversationList) {
            Iterator i = this.conversationList.iterator();
            while (i.hasNext()){
                Conversation conversation = (Conversation) i.next();
                if (conversation.getConversationId().equals(cid))
                    return conversation;
            }
        }

        return null;
    }

    private void dispatchMemberTypeEvent(String cid, String memberId, Member.TYPING_INDICATOR typing_indicator){
        Conversation pendingConversation = findConversation(cid);
        if (pendingConversation != null) {
            //set member
            Member typingMember = pendingConversation.getMember(memberId);
            if(typingMember != null)
                typingMember.setTypingIndicator(typing_indicator);


            if (this.textTypeListenerMap.containsKey(cid)) {
                List<MemberTypingListener> listeners = this.textTypeListenerMap.get(cid);
                for(MemberTypingListener listener : listeners)
                    listener.onTyping(pendingConversation, new Member(typingMember), typing_indicator);
            }
        } //else dispatch an internal error report to bugsnag
    }

    public void removeAllListeners() {
        this.memberJoinedListenerMap.clear();
        this.memberLeftListenerMap.clear();
        this.memberInvitedListenerMap.clear();
        this.conversationInvitesListeners.clear();
        this.textListenerMap.clear();
        this.imageListenerMap.clear();
        this.textTypeListenerMap.clear();
        this.seenReceiptListenerMap.clear();
        this.imageSeenReceiptListenerMap.clear();
    }
}
