/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.core.client;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.ConversationClient;
import com.nexmo.sdk.conversation.client.Image;
import com.nexmo.sdk.conversation.client.ImageRepresentation;
import com.nexmo.sdk.conversation.client.SeenReceipt;
import com.nexmo.sdk.conversation.client.event.network.NetworkingStateListener;
import com.nexmo.sdk.conversation.client.Member;
import com.nexmo.sdk.conversation.client.Text;
import com.nexmo.sdk.conversation.client.User;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.LoginListener;
import com.nexmo.sdk.conversation.client.event.SignalingChannelListener;
import com.nexmo.sdk.conversation.common.util.DateUtil;
import com.nexmo.sdk.conversation.config.Config;
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
import com.nexmo.sdk.conversation.device.DeviceProperties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.nexmo.sdk.conversation.client.event.ConversationGenericListener.GENERIC_ERR;

/**
 * SocketClient responsible for signaling events.
 */
class SocketClient {
    private static final String TAG = SocketClient.class.getSimpleName();

    /**
     * event names.
     */
    static final String LOGIN_REQUEST = "session:login";
    static final String LOGIN_SUCCESS = "session:success";
    static final String LOGOUT_REQUEST = "session:logout";
    static final String LOGOUT_SUCCESS = "session:logged-out";
    /**
     * conversation events
     */
    static final String CONVERSATION_NEW_REQUEST = "new:conversation";
    static final String CONVERSATION_NEW_SUCCESS = "new:conversation:success";
    static final String CONVERSATION_NEW_ERROR = "new:conversation:error";
    static final String CONVERSATION_MEMBER_JOINED = "member:joined";
    static final String CONVERSATIONS_GET_REQUEST = "user:conversations";
    static final String CONVERSATIONS_GET_SUCCESS = "user:conversations:success";
    static final String CONVERSATION_GET_REQUEST = "conversation:get";
    static final String CONVERSATION_GET_SUCCESS = "conversation:get:success";
    static final String CONVERSATION_LEAVE = "conversation:member:delete";
    static final String CONVERSATION_LEAVE_SUCCESS = "conversation:member:delete:success";
    /**
     * invites
     */
    static final String CONVERSATION_JOIN_REQUEST = "conversation:join";
    static final String CONVERSATION_JOIN_SUCCESS = "conversation:join:success";
    static final String INVITE_REQUEST = "conversation:invite";
    static final String INVITE_SUCCESS = "conversation:invite:success";
    static final String MEMBER_INVITED = "member:invited";
    static final String MEMBER_LEFT = "member:left";  //invitation decline, or leave joined conversation.
    /**
     * generic events
     */
    static final String EVENT_DELETE = "event:delete";
    static final String EVENT_DELETE_SUCCESS = "event:delete:success";
    /**
     * text events
     */
    static final String TEXT_MESSAGE = "text";
    static final String TEXT_MESSAGE_SUCCESS = "text:success";
    static final String CONVERSATION_GET_MESSAGES = "conversation:events";
    static final String CONVERSATION_GET_MESSAGES_SUCCESS = "conversation:events:success";
    static final String TEXT_SEEN = "text:seen";
    static final String TEXT_SEEN_SUCCESS = "text:seen:success";
    static final String TEXT_TYPE_ON = "text:typing:on";
    static final String TEXT_TYPE_ON_SUCCESS = "text:typing:on:success";
    static final String TEXT_TYPE_OFF = "text:typing:off";
    static final String TEXT_TYPE_OFF_SUCCESS = "text:typing:off:success";
    /**
     * image events
     */
    static final String IMAGE_MESSAGE = "image";
    static final String IMAGE_MESSAGE_SUCCESS = "image:success";
    static final String IMAGE_SEEN = "image:seen";
    static final String IMAGE_SEEN_SUCCESS = "image:seen:success";
    /**
     * push events
     */
    static final String PUSH_REGISTER = "push:register";
    static final String PUSH_REGISTER_SUCCESS = "push:register:success";
    static final String PUSH_UNREGISTER = "push:unregister";
    static final String PUSH_UNREGISTER_SUCCESS = "push:unregister:success";
    static final String PUSH_SUBSCRIBE = "push:subscribe";
    static final String PUSH_SUBSCRIBE_SUCCESS = "push:subscribe:success";
    static final String PUSH_UNSUBSCRIBE = "push:unsubscribe";
    static final String PUSH_UNSUBSCRIBE_SUCCESS = "push:unsubscribe:success";

    /**
     * error events
     */
    static final String MESSAGE_ERROR = "message:error:invalid";
    static final String INVALID_TOKEN = "session:error:invalid-token";
    static final String EXPIRED_TOKEN = "session:error:expired-token";
    static final String SESSION_INVALID = "session:invalid";
    static final String SESSION_ERROR = "session:error";
    static final String SESSION_TERMINATED = "session:terminated";
    static final String CONVERSATION_ERROR = "conversation:error";
    static final String EVENT_ERROR = "event:error";
    static final String PUSH_REGISTER_ERROR = "push:register:error";
    static final String PUSH_UNREGISTER_ERROR = "push:unregister:error";

    private static GsonBuilder gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();
    private static final Gson gson = gsonBuilder.create();
    /**
     * There is one connect/login attempt in progress that blocks.
     **/
    private NetworkingStateListener.NETWORK_STATE connectionStatus = NetworkingStateListener.NETWORK_STATE.DISCONNECTED;
    private Socket socket;
    private ConversationClient conversationClient;
    private SignalingChannelListener signalingChannelListener;

    User self;
    List<Conversation> conversationList = Collections.synchronizedList(new ArrayList<Conversation>());

    SocketClient() {
    }

     NetworkingStateListener.NETWORK_STATE getConnectionStatus() {
        return this.connectionStatus;
    }

     void registerSignalChannelListener(SignalingChannelListener listener) {
        this.signalingChannelListener = listener;
    }

    void connect(final String connectionUrl, final ConversationClient conversationClient) throws URISyntaxException {
       IO.Options options = new IO.Options();
       options.forceNew = true;
       options.path = "/rtc/";
       options.reconnection = false;
       this.conversationClient = conversationClient;

        Log.d(TAG, "Connect to " + connectionUrl);
       this.socket = IO.socket(connectionUrl, options);
       this.socket.connect();

       this.socket.on(Socket.EVENT_CONNECT, onConnected);
       this.socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
       this.socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectTimeout);
       this.socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
       this.socket.on(Socket.EVENT_RECONNECT, onReconnect);
       this.socket.on(Socket.EVENT_RECONNECTING, onReconnecting);
       this.socket.on(Socket.EVENT_RECONNECT_ERROR, onReconnectError);
       this.socket.on(Socket.EVENT_RECONNECT_FAILED, onReconnectFailed);
       this.socket.on(Socket.EVENT_RECONNECT_ATTEMPT, onReconnectAttempt);
   }

     private void login() {
        JSONObject loginObj = new JSONObject();
        try {
            attachRandomUUID(loginObj);

            JSONObject bodyObj = new JSONObject();
            bodyObj.put("token", conversationClient.getToken());
            bodyObj.put("sdk", Config.SDK_REVISION_CODE);
            // push fields
            bodyObj.put("device_id", DeviceProperties.getAndroid_ID(this.conversationClient.getContext()));
            bodyObj.put("device_type", "android");//fcm
            loginObj.put("body", bodyObj);
            Log.d(LOGIN_REQUEST, loginObj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.socket.on(LOGIN_SUCCESS, onLogin);
        this.socket.once(INVALID_TOKEN, onInvalidToken);
        this.socket.once(EXPIRED_TOKEN, onExpiredToken);
        this.socket.once(SESSION_INVALID, onInvalidSession);
        this.socket.once(SESSION_TERMINATED, onSessionTerminated);
        this.socket.once(SESSION_ERROR, onSessionError);
        this.socket.once(MESSAGE_ERROR, onMessageError);
        this.socket.emit(LOGIN_REQUEST, loginObj);
    }

    void pushRegister() {
        JSONObject registerObj = new JSONObject();
        try {
            attachRandomUUID(registerObj);

            JSONObject bodyObj = new JSONObject();
            bodyObj.put("device_token", conversationClient.getPushDeviceToken());
            registerObj.put("body", bodyObj);
            Log.d(PUSH_REGISTER, registerObj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.socket.on(PUSH_REGISTER_SUCCESS, onPushRegistered);
        this.socket.on(PUSH_REGISTER_ERROR, onPushRegisterErr);
        this.socket.emit(PUSH_REGISTER, registerObj);
    }

    void pushUnregister() {
        JSONObject unregisterObj = new JSONObject();
        try {
            attachRandomUUID(unregisterObj);

            JSONObject bodyObj = new JSONObject();
            bodyObj.put("device_id", DeviceProperties.getAndroid_ID(this.conversationClient.getContext()));
            unregisterObj.put("body", bodyObj);
            Log.d(PUSH_UNREGISTER, unregisterObj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.socket.once(PUSH_UNREGISTER_SUCCESS, onPushUnregistered);
        this.socket.once(PUSH_UNREGISTER_ERROR, onPushUnregisterErr);
        this.socket.emit(PUSH_UNREGISTER, unregisterObj);
    }

    void pushSubscribeToConversation(boolean subscribe, PushSubscribeRequest pushSubscribeRequest) {
        JSONObject subscribeObj = new JSONObject();
        try {
            attachTID(pushSubscribeRequest.tid, subscribeObj);

            JSONObject bodyObj = new JSONObject();
            bodyObj.put("cid", pushSubscribeRequest.cid);
            subscribeObj.put("body", bodyObj);
            Log.d("Subscribe push ", subscribeObj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (subscribe)
            this.socket.emit(PUSH_SUBSCRIBE, subscribeObj);
        else
            this.socket.emit(PUSH_UNSUBSCRIBE, subscribeObj);

        this.socket.once(PUSH_SUBSCRIBE_SUCCESS, onPushSubscribed);
        this.socket.once(PUSH_UNSUBSCRIBE_SUCCESS, onPushUnsubscribed);
    }

    void logout() {
        JSONObject logoutObj = new JSONObject();
        try {
            attachRandomUUID(logoutObj);
            Log.d(LOGOUT_REQUEST, logoutObj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.socket.once(LOGOUT_SUCCESS, onLogout);
        this.socket.emit(LOGOUT_REQUEST, logoutObj);
    }

    void newConversation(CreateRequest createRequest) {
        JSONObject convObj = new JSONObject();
        try {
            attachTID(createRequest.tid, convObj);

            JSONObject bodyObj = new JSONObject();
            bodyObj.put("name", createRequest.name);
            convObj.put("body", bodyObj);
            Log.d(CONVERSATION_NEW_REQUEST, convObj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.socket.once(CONVERSATION_NEW_SUCCESS, onNewConversation);
        this.socket.emit(CONVERSATION_NEW_REQUEST, convObj);
    }

    void joinConversation(JoinRequest joinRequest) {
        JSONObject convObj = new JSONObject();
        try {
            attachTID(joinRequest.tid, convObj);
            convObj.put("cid", joinRequest.cid);
            JSONObject bodyObj = new JSONObject();
            bodyObj.put("user_id", self.getUserId());
            if (!TextUtils.isEmpty(joinRequest.memberId))
                bodyObj.put("member_id", joinRequest.memberId);

            convObj.put("body", bodyObj);
            Log.d(CONVERSATION_JOIN_REQUEST, convObj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.socket.once(CONVERSATION_JOIN_SUCCESS, onJoinConversation);
        this.socket.emit(CONVERSATION_JOIN_REQUEST, convObj);
    }

    void leaveConversation(LeaveRequest leaveRequest) {
        JSONObject convObj = new JSONObject();
        try {
            attachTID(leaveRequest.tid, convObj);
            convObj.put("cid", leaveRequest.cid);

            JSONObject bodyObj = new JSONObject();
            bodyObj.put("member_id", leaveRequest.memberId);
            convObj.put("body", bodyObj);
            Log.d("Leave conversation: ", convObj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.socket.once(CONVERSATION_LEAVE_SUCCESS, onKickedSuccess);
        this.socket.emit(CONVERSATION_LEAVE, convObj);
    }

    void invite(InviteRequest inviteRequest) {
        JSONObject inviteObject = new JSONObject();
        try {
            attachTID(inviteRequest.tid, inviteObject);
            inviteObject.put("cid", inviteRequest.cid);

            JSONObject bodyObj = new JSONObject();
            bodyObj.put("user_name", inviteRequest.user);
            inviteObject.put("body", bodyObj);
            Log.d(INVITE_REQUEST, inviteObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.socket.once(INVITE_SUCCESS, onInvite);
        this.socket.emit(INVITE_REQUEST, inviteObject);
    }

    void getConversation(GetConversationRequest getRequest) {
        JSONObject getObject = new JSONObject();
        try {
            attachTID(getRequest.tid, getObject);
            getObject.put("cid", getRequest.cid);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("getConversation ", getObject.toString());
        this.socket.once(CONVERSATION_GET_SUCCESS, onConversation);
        this.socket.emit(CONVERSATION_GET_REQUEST, getObject);
    }

    void getConversations(String tid) {
        JSONObject getObject = new JSONObject();
        try {
            attachTID(tid, getObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("getConversations ", getObject.toString());
        this.socket.once(CONVERSATIONS_GET_SUCCESS, onConversations);
        this.socket.emit(CONVERSATIONS_GET_REQUEST, getObject);
    }

    // declare that self has seen the message
    void sendSeenEvent(MarkSeenRequest markSeenRequest) {
        JSONObject convObj = new JSONObject();
        try {
            attachTID(markSeenRequest.tid, convObj);
            convObj.put("cid", markSeenRequest.cid);
            convObj.put("from", markSeenRequest.memberId);

            JSONObject bodyObj = new JSONObject();
            bodyObj.put("event_id", markSeenRequest.eventId);
            convObj.put("body", bodyObj);
            Log.d("SENDING SEEN : ", convObj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (markSeenRequest.type.equals(Request.TYPE.MARK_TEXT_SEEN)) {
            this.socket.once(TEXT_SEEN_SUCCESS, onSeenSuccess);
            this.socket.emit(TEXT_SEEN, convObj);
        } else if(markSeenRequest.type.equals(Request.TYPE.MARK_IMAGE_SEEN)) {
            this.socket.once(IMAGE_SEEN_SUCCESS, onSeenSuccess);
            this.socket.emit(IMAGE_SEEN, convObj);
        }
    }

    void sendTypingIndicator(TypingIndicatorRequest typingIndicatorRequest) {
        JSONObject typeObject = new JSONObject();
        try {
            attachTID(typingIndicatorRequest.tid, typeObject);
            typeObject.put("cid", typingIndicatorRequest.cid);
            typeObject.put("from", typingIndicatorRequest.memberId);

            JSONObject bodyObj = new JSONObject();
            bodyObj.put("activity", typingIndicatorRequest.typingIndicator == Member.TYPING_INDICATOR.ON ? 1 : 0);
            typeObject.put("body", bodyObj);
            Log.d("SENDING Type indicator " +(typingIndicatorRequest.typingIndicator == Member.TYPING_INDICATOR.ON ? "ON" : "OFF"), typeObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.socket.once(TEXT_TYPE_ON_SUCCESS, onTypeOnSuccess);
        this.socket.once(TEXT_TYPE_OFF_SUCCESS, onTypeOffSuccess);
        this.socket.emit(typingIndicatorRequest.typingIndicator == Member.TYPING_INDICATOR.ON ? TEXT_TYPE_ON : TEXT_TYPE_OFF, typeObject);
    }

    void deleteEvent(DeleteEventRequest deleteEventRequest) {
        JSONObject requestObj = new JSONObject();
        try {
            attachTID(deleteEventRequest.tid, requestObj);
            requestObj.put("cid", deleteEventRequest.cid);
            requestObj.put("from", deleteEventRequest.memberId);
            JSONObject bodyObj = new JSONObject();
            bodyObj.put("event_id", deleteEventRequest.messageId);
            requestObj.put("body", bodyObj);
            Log.d("Deleting message: ", requestObj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.socket.once(EVENT_DELETE_SUCCESS, onTextDeletedSuccess);
        this.socket.emit(EVENT_DELETE, requestObj);
    }

    void sendText(SendMessageRequest sendMessageRequest) {
        JSONObject textObject = new JSONObject();
        try {
            attachTID(sendMessageRequest.tid, textObject);
            textObject.put("cid", sendMessageRequest.cid);
            textObject.put("from", sendMessageRequest.memberId);

            JSONObject bodyObj = new JSONObject();
            bodyObj.put("text", sendMessageRequest.message);
            textObject.put("body", bodyObj);
            Log.d("SENDING TEXT : ", textObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.socket.once(TEXT_MESSAGE_SUCCESS, onTextSent);
        this.socket.emit(TEXT_MESSAGE, textObject);
    }

    void sendImage(SendMessageRequest sendMessageRequest, JSONObject jsonObject) {
        JSONObject imageObject = new JSONObject();
        try {
            attachTID(sendMessageRequest.tid, imageObject);
            imageObject.put("cid", sendMessageRequest.cid);
            imageObject.put("from",sendMessageRequest.memberId);

            JSONObject bodyObj = new JSONObject();
            bodyObj.put("representations", jsonObject);
            imageObject.put("body", bodyObj);

            Log.d("SENDING IMAGE : ", imageObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.socket.once(IMAGE_MESSAGE_SUCCESS, onImageSent);
        this.socket.emit(IMAGE_MESSAGE, imageObject);
    }

    void getMessages(GetConversationRequest getRequest){
        JSONObject getObject = new JSONObject();
        try {
            attachTID(getRequest.tid, getObject);
            getObject.put("cid", getRequest.cid);

            JSONObject bodyObj = new JSONObject();
            String startId = getRequest.startId;
            String endId = getRequest.endId;
            if (!TextUtils.isEmpty(startId))
                bodyObj.put("start_id", startId);
            if (!TextUtils.isEmpty(endId))
                bodyObj.put("end_id", endId);
            getObject.put("body", bodyObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("SENDING get texts : ", getObject.toString());
        this.socket.once(CONVERSATION_GET_MESSAGES_SUCCESS, onConversationMessages);
        this.socket.emit(CONVERSATION_GET_MESSAGES, getObject);
    }

    void release(){
        if (this.socket != null && this.socket.connected()){
            this.socket.disconnect();
            this.socket.off();
            updateConnectStatus(NetworkingStateListener.NETWORK_STATE.DISCONNECTED);
        }
    }

    private void updateConnectStatus(NetworkingStateListener.NETWORK_STATE status){
        synchronized(this) {
            this.connectionStatus = status;
        }
    }

    private void attachTID(String tid, JSONObject json) throws JSONException {
        json.put("tid", tid);
    }

    private String attachRandomUUID(JSONObject json) throws JSONException {
        String tid = UUID.randomUUID().toString();
        json.put("tid", tid);

        return tid;
    }

    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onLogin ", data.toString());
            try {
                JSONObject body = data.getJSONObject("body");
                self = gson.fromJson(body.toString(), User.class);
                Log.d(TAG, "USER: " + self.toString());

                LoginListener loginListener = conversationClient.getLoginListener();
                if (loginListener != null) {
                    loginListener.onLogin(new User(self));
                    conversationClient.setMyUser(new User(self));
                    conversationClient.cleanLoginListener();
                }
            } catch (JSONException e) {
                Log.d(TAG, "onLogin exception: " + e.toString());
            }
            //listen for new members once joined.
            socket.on(CONVERSATION_MEMBER_JOINED, onMemberJoined);

            //listen for kicks
            socket.on(MEMBER_LEFT, onMemberLeft);

            //listen for invites
            socket.on(MEMBER_INVITED, onMemberInvited);

            //listen for text messages once joined.
            socket.on(EVENT_DELETE, onTextDeleted);
            socket.on(TEXT_MESSAGE, onText);

            //listen for text seen events.
            socket.on(TEXT_SEEN, onTextSeen);

            //listen for image events
            socket.on(IMAGE_MESSAGE, onImage);
            socket.on(IMAGE_SEEN, onImageSeen);

            //listen to typing
            socket.on(TEXT_TYPE_ON, onTypeOn);
            socket.on(TEXT_TYPE_OFF, onTypeOff);

            //listen for generic conversation errors.
            socket.on(CONVERSATION_ERROR, onConversationError);
            socket.on(CONVERSATION_NEW_ERROR, onNewConversationError);
            socket.on(EVENT_ERROR, onEventError);
        }
    };

    private Emitter.Listener onLogout = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onLogout ", data.toString());

            conversationClient.setMyUser(null);
            conversationClient.getLogoutListener().onLogout(new User(self));
            conversationClient.detachAllListeners();
            release();
        }
    };

    private Emitter.Listener onPushRegistered = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onPushRegistered ", data.toString());

            conversationClient.getPushEnableListener().onSuccess();
        }
    };

    private Emitter.Listener onPushRegisterErr = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onPushRegisterErr ", data.toString());

            conversationClient.getPushEnableListener().onError(GENERIC_ERR, "Generic err");
        }
    };

    private Emitter.Listener onPushUnregisterErr = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onPushUnregisterErr ", data.toString());

            conversationClient.getPushEnableListener().onError(GENERIC_ERR, "Generic err");
        }
    };

    private Emitter.Listener onPushUnregistered = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onPushUnregistered ", data.toString());

            conversationClient.getPushEnableListener().onSuccess();
        }
    };

    private Emitter.Listener onPushSubscribed = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onPushSubscribed ", data.toString());

            try {
                String rid = data.getString("rid");
                signalingChannelListener.onPushSubscribedToConversation(rid);
            } catch (JSONException e) {
            }
        }
    };

    private Emitter.Listener onPushUnsubscribed = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onPushUnsubscribed ", data.toString());

            try {
                String rid = data.getString("rid");
                signalingChannelListener.onPushSubscribedToConversation(rid);
            } catch (JSONException e) {
            }
        }
    };

    private Emitter.Listener onTextDeleted = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onDeleted ", data.toString());
            try {
                String from = data.getString("from");
                String cid = data.getString("cid");
                Date timestamp = null;
                try {
                    timestamp = DateUtil.formatIso8601DateString(data.getString("timestamp"));
                } catch (ParseException e) {
                }

                JSONObject body = data.getJSONObject("body");
                signalingChannelListener.onTextDeleted(cid, from, body.getString("event_id"), timestamp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onText = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onText ", data.toString());
            try {
                String cid = data.getString("cid");

                JSONObject body = data.getJSONObject("body");
                Text incomingMessage = gson.fromJson(body.toString(), Text.class);
                Date timestamp = null;
                try {
                    timestamp = DateUtil.formatIso8601DateString(data.getString("timestamp"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String senderId = data.getString("from");
                incomingMessage = new Text(incomingMessage.getPayload(), data.getString("id"), timestamp);
                signalingChannelListener.onTextReceived(cid, senderId, incomingMessage);
            } catch (JSONException e) {
            }
        }
    };

    private Emitter.Listener onImage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onImage ", data.toString());
            try {
                String cid = data.getString("cid");
                String senderId = data.getString("from");
                String id = data.getString("id");
                Date timestamp = null;
                try {
                    timestamp = DateUtil.formatIso8601DateString(data.getString("timestamp"));
                } catch (ParseException e) {
                }

                JSONObject body = data.getJSONObject("body");
                JSONObject representations = body.getJSONObject("representations");
                JSONObject originalJson=null, mediumJson=null, thumbnailJson= null;
                ImageRepresentation original=null, medium=null, thumbnail=null;

                originalJson = representations.getJSONObject("original");
                original = gson.fromJson(originalJson.toString(), ImageRepresentation.class);
                original.type = ImageRepresentation.TYPE.ORIGINAL;
                Log.d(TAG, original.toString());

                mediumJson = representations.getJSONObject("medium");
                medium = gson.fromJson(mediumJson.toString(), ImageRepresentation.class);
                medium.type = ImageRepresentation.TYPE.MEDIUM;
                Log.d(TAG, medium.toString());

                thumbnailJson = representations.getJSONObject("thumbnail");
                thumbnail = gson.fromJson(thumbnailJson.toString(), ImageRepresentation.class);
                thumbnail.type = ImageRepresentation.TYPE.THUMBNAIL;
                Log.d(TAG, thumbnail.toString());

                // retrieve 3 Images, dev can download any of them.: Image.downloadBitmap()
                Image incomingMessage = new Image(id, timestamp);
                incomingMessage.addRepresentations(original, medium, thumbnail);

                Log.d(TAG, "onImageReceived " + cid);
                signalingChannelListener.onImageReceived(cid, senderId, incomingMessage);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onTypeOnSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onTypeOnSuccess ", data.toString());
            try {
                String rid = data.getString("rid");
                if (!TextUtils.isEmpty(rid)) {
                    signalingChannelListener.onTypingOn(rid);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onTypeOffSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onTypeOffSuccess ", data.toString());
            try {
                String rid = data.getString("rid");
                if (!TextUtils.isEmpty(rid)) {
                    signalingChannelListener.onTypingOff(rid);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    // dispatching seen event was handled by the service.
    private Emitter.Listener onSeenSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onSeenSuccess ", data.toString());
            try {
                String rid = data.getString("rid");
                if(!TextUtils.isEmpty(rid)) {
                    signalingChannelListener.onMarkedAsSeen(rid);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onTypeOn = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onTypeOn received ", data.toString());
            try {
                String cid = data.getString("cid");
                String memberId = data.getString("from");

                signalingChannelListener.onTypingOnReceived(cid, memberId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onTypeOff = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onTypeOn received ", data.toString());
            try {
                String cid = data.getString("cid");
                String memberId = data.getString("from");

                signalingChannelListener.onTypingOffReceived(cid, memberId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onImageSeen = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onImageSeen", data.toString());

            parseSeenReceipt(data, Request.TYPE.MARK_IMAGE_SEEN);
        }
    };

    private Emitter.Listener onTextSeen = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onTextSeen", data.toString());

            parseSeenReceipt(data, Request.TYPE.MARK_TEXT_SEEN);
        }
    };

    private void parseSeenReceipt(JSONObject data, Request.TYPE eventType) {
        try {
            String cid = data.getString("cid");
            String memberId = data.getString("from");
            Date timestamp = null;
            try {
                timestamp = DateUtil.formatIso8601DateString(data.getString("timestamp"));
            } catch (ParseException e) {
            }

            JSONObject body = data.getJSONObject("body");
            String eventId = null;
            if (body.has("event_id"))
                eventId = body.getString("event_id");
            else if (body.has("message_id"))
                body.getString("message_id");

            signalingChannelListener.onEventSeen(cid, memberId, eventId, eventType, timestamp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Emitter.Listener onTextDeletedSuccess= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onEventDeletedSuccess ", data.toString());
            try {
                String rid = data.getString("rid");
                if (!TextUtils.isEmpty(rid)) {
                    JSONObject body = data.getJSONObject("body");
                    String eventId = body.getString("id");

                    signalingChannelListener.onTextRemoved(rid, eventId);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //onTextSent just notifies upon success, does not get back the payload.
    private Emitter.Listener onTextSent = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onTextSent ", data.toString());
            try {
                String rid = data.getString("rid");
                if (!TextUtils.isEmpty(rid)) {
                    JSONObject body = data.getJSONObject("body");
                    Text incomingMessage = gson.fromJson(body.toString(), Text.class);
                    Date timestamp = null;
                    try {
                        timestamp = DateUtil.formatIso8601DateString(body.getString("timestamp"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    signalingChannelListener.onTextSent(rid, incomingMessage.getId(), timestamp);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onImageSent = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onImageSent ", data.toString());
            try {
                String rid = data.getString("rid");
                if (!TextUtils.isEmpty(rid)) {
                    JSONObject body = data.getJSONObject("body");
                    //Text incomingMessage = gson.fromJson(body.toString(), Text.class);
                    Date timestamp = null;
                    try {
                        timestamp = DateUtil.formatIso8601DateString(body.getString("timestamp"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    signalingChannelListener.onImageSent(rid, body.getString("id"), timestamp);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onNewConversationError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "onNewConversationError: error");
            //todo trigger generic errors
        }
    };

    private Emitter.Listener onNewConversation = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onNewConversation ", data.toString());
            try {
                String rid = data.getString("rid");
                if (!TextUtils.isEmpty(rid)) {
                    JSONObject body = data.getJSONObject("body");
                    String cid = body.getString("id");
                    signalingChannelListener.onCreate(rid, body.getString("id"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onMemberInvited = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onInvitation received ", data.toString());
            try {
                String cid = data.getString("cid");
                String senderMemberId = data.getString("from");

                JSONObject body = data.getJSONObject("body");
                String cName = body.getString("cname");
                String senderUsername = body.getString("invited_by"); //username

                JSONObject time = body.getJSONObject("timestamp");
                Date timestamp = null;
                try {
                    timestamp = DateUtil.formatIso8601DateString(time.getString("invited"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                JSONObject user = body.getJSONObject("user");
                Member invitedMember = new Member(user.getString("user_id"), user.getString("user_name"), user.getString("member_id"), null, null, timestamp, Member.STATE.INVITED);

                signalingChannelListener.onMemberInvited(cid, cName, invitedMember, senderMemberId, senderUsername);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onMemberLeft = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onMemberLeft ", data.toString());
            try {
                String cid = data.getString("cid");
                String memberId = data.getString("from");

                JSONObject body = data.getJSONObject("body");
                JSONObject timestamp = body.getJSONObject("timestamp");
                Date joinedTimestamp = null, leftTimestamp = null, invitedTimestamp = null; //optional
                try {
                    if (timestamp.has("joined"))
                        joinedTimestamp = DateUtil.formatIso8601DateString(timestamp.getString("joined"));
                    if (timestamp.has("left"))
                        leftTimestamp = DateUtil.formatIso8601DateString(timestamp.getString("left"));
                    if (timestamp.has("invited"))
                        invitedTimestamp = DateUtil.formatIso8601DateString(timestamp.getString("invited"));
                } catch (ParseException e) {
                }
                JSONObject userObject = body.getJSONObject("user");
                User user = new User(userObject.getString("id"), userObject.getString("name"));

                signalingChannelListener.onMemberLeft(cid, memberId, user, invitedTimestamp, joinedTimestamp, leftTimestamp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onMemberJoined = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onMemberJoined ", data.toString());
            try {
                String from = data.getString("from");
                String cid = data.getString("cid");

                JSONObject body = data.getJSONObject("body");
                JSONObject userObject = body.getJSONObject("user");
                User user = new User(userObject.getString("id"), userObject.getString("name"));
                JSONObject timestamp = body.getJSONObject("timestamp");

                Date joinedTimestamp = null;
                try {
                    joinedTimestamp = DateUtil.formatIso8601DateString(timestamp.getString("joined"));
                } catch (ParseException e) {
                }

                signalingChannelListener.onMemberJoined(cid, from, user, joinedTimestamp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onConversationError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //unknown conversation id
            JSONObject data = (JSONObject) args[0];
            Log.d("onConversationError ", data.toString());
        }
    };

    private Emitter.Listener onConversation = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onConversationUpdated ", data.toString());
            try {
                String rid = data.getString("rid");
                if (!TextUtils.isEmpty(rid)) {
                    JSONObject body = data.getJSONObject("body");

                    Conversation conversation = gson.fromJson(body.toString(), Conversation.class);
                    Date timestamp = null;
                    try {
                        timestamp = DateUtil.formatIso8601DateString(body.getJSONObject("timestamp").getString("created"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    conversation = new Conversation(conversation.getName(), conversation.getConversationId(), conversation.getMemberId(), timestamp, conversation.getLastEventId());

                    JSONArray membersArray = body.getJSONArray("members");
                    for (int index=0 ; index < membersArray.length(); index++) {
                        JSONObject m = membersArray.getJSONObject(index);
                        Member member = gson.fromJson(m.toString(), Member.class);
                        member.setState(Member.state(m.getString("state")));
                        //todo the service will have to return the related date as well, and senderId for INVITED state.
                        conversation.addMember(member);
                        //search for self.user_id among all the members
                        if (member.getUser_id().equals(self.getUserId()))
                            conversation.setSelf(member);
                    }
                    signalingChannelListener.onConversation(rid, conversation);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onConversationMessages = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onConversationMessages ", data.toString());
            List<Text> messages = new ArrayList<>();
            List<Image> images = new ArrayList<>();
            List<SeenReceipt> seenReceipts = new ArrayList<>();

            try {
                String rid = data.getString("rid");
                if (!TextUtils.isEmpty(rid)) {
                    JSONArray messagesArray = data.getJSONArray("body");
                    for(int index = 0; index < messagesArray.length(); index++) {
                        JSONObject messageObject = messagesArray.getJSONObject(index);

                        if(messageObject.getString("type").equals("text")) {
                            Log.d(TAG, "Text event " + messageObject);
                            String textId = messageObject.getString("id");

                            JSONObject body = messageObject.getJSONObject("body");
                            String payload = null;
                            if (body.has("text")) {
                                payload = body.getString("text");
                            }
                            Date timestamp = null;
                            Date timestampDeleted=null;
                            try {
                                timestamp = DateUtil.formatIso8601DateString(messageObject.getString("timestamp"));
                                //timestampDeleted = Defaults.DATE_FORMAT.parse(messageObject.getString("timestamp_deleted"));
                            } catch (ParseException e) {
                            }

                            if (timestampDeleted != null)
                                Log.d(TAG, "history - text deleted at time: " + timestampDeleted.toString());
                            Member member = new Member(messageObject.getString("from"));
                            Text message = new Text(payload, textId, timestamp, member);
                            messages.add(message);
                        } else if(messageObject.getString("type").equals("image")) {
                            Log.d(TAG, "Image event " + messageObject);
                            String id = messageObject.getString("id");
                            JSONObject body = messageObject.getJSONObject("body");

                            try {
                                Date timestamp = null; //or deleted
                                try {
                                    timestamp = DateUtil.formatIso8601DateString(messageObject.getString("timestamp"));
                                } catch (ParseException exc) {
                                }

                                JSONObject representations = body.getJSONObject("representations");
                                JSONObject originalJson=null, mediumJson=null, thumbnailJson= null;
                                ImageRepresentation original=null, medium=null, thumbnail=null;

                                originalJson = representations.getJSONObject("original");
                                original = gson.fromJson(originalJson.toString(), ImageRepresentation.class);
                                original.type = ImageRepresentation.TYPE.ORIGINAL;
                                Log.d(TAG, original.toString());

                                mediumJson = representations.getJSONObject("medium");
                                medium = gson.fromJson(mediumJson.toString(), ImageRepresentation.class);
                                medium.type = ImageRepresentation.TYPE.MEDIUM;
                                Log.d(TAG, medium.toString());

                                thumbnailJson = representations.getJSONObject("thumbnail");
                                thumbnail = gson.fromJson(thumbnailJson.toString(), ImageRepresentation.class);
                                thumbnail.type = ImageRepresentation.TYPE.THUMBNAIL;
                                Log.d(TAG, thumbnail.toString());

                                Member member = new Member(messageObject.getString("from"));
                                Image incomingMessage = new Image(null, id, timestamp, null, member);
                                incomingMessage.addRepresentations(original, medium, thumbnail);

                                images.add(incomingMessage);
                            } catch (JSONException e) {
                                Log.d(TAG, "Ignore old images with wrong format.");
                                // or deleted images
                            }
                        }
                        else if(messageObject.getString("type").equals("text:seen") ||
                                messageObject.getString("type").equals("image:seen")) {
                            Log.d(TAG, "Seen event " + messageObject);

                            String senderId = messageObject.getString("from");
                            Date timestamp = null;
                            try {
                                timestamp = DateUtil.formatIso8601DateString(messageObject.getString("timestamp"));
                            } catch (ParseException e) {
                            }

                            JSONObject body = messageObject.getJSONObject("body");
                            String eventId = null;
                            if (body.has("event_id"))
                                eventId = body.getString("event_id");
                            else if (body.has("message_id"))
                                eventId = body.getString("message_id");

                            SeenReceipt seenReceipt = new SeenReceipt(eventId, senderId, timestamp);
                            seenReceipts.add(seenReceipt);
                        }
                        else if(messageObject.getString("type").equals("event:delete"))
                            Log.d(TAG, "event:deleted " + messageObject);
                    }
                    Log.d(TAG, "onMessages.conversation: " + messages.toString());
                    signalingChannelListener.onEventsHistory(rid, messages, images, seenReceipts);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onInvite = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onInviteSent ", data.toString());
            try {
                String rid = data.getString("rid");
                if (!TextUtils.isEmpty(rid)) {
                    signalingChannelListener.onInvitationSent(rid);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onJoinConversation = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onJoinConversation ", data.toString());
            try {
                String rid = data.getString("rid");
                if (!TextUtils.isEmpty(rid)) {
                    JSONObject body = data.getJSONObject("body");

                    //ignore invalid signals from unknown users.
                    String user_id = body.getString("user_id");
                    if (!self.getUserId().equals(user_id))
                        return;

                    String member_id = body.getString("id");
                    Date timestamp = null;
                    try {
                        timestamp = DateUtil.formatIso8601DateString(body.getJSONObject("timestamp").getString("joined"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    //signal back a new Member
                    Member member = new Member(self.getUserId(), self.getName(), member_id, timestamp, null, null, Member.STATE.JOINED);
                    signalingChannelListener.onJoin(rid, member);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onKickedSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onKickedSuccess ", data.toString());
            try {
                String rid = data.getString("rid");
                if (!TextUtils.isEmpty(rid)) {

                    JSONObject body = data.getJSONObject("body");
                    JSONObject timestamp = body.getJSONObject("timestamp");
                    Date joinedTimestamp = null, leftTimestamp = null, invitedTimestamp = null;
                    try {
                        if (timestamp.has("joined"))
                            joinedTimestamp = DateUtil.formatIso8601DateString(timestamp.getString("joined"));
                        if (timestamp.has("left"))
                            leftTimestamp = DateUtil.formatIso8601DateString(timestamp.getString("left"));
                        if (timestamp.has("invited"))
                            invitedTimestamp = DateUtil.formatIso8601DateString(timestamp.getString("invited"));
                    } catch (ParseException e) {
                    }

                    signalingChannelListener.onLeft(rid, invitedTimestamp, joinedTimestamp, leftTimestamp);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onConversations = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onConversations ", data.toString());
            try {
                JSONArray conversations = data.getJSONArray("body");
                List<Conversation> conversationList = new ArrayList<>();

                for (int index=0 ; index < conversations.length(); index++) {
                    JSONObject c = conversations.getJSONObject(index);
                    Conversation conversation = gson.fromJson(c.toString(), Conversation.class);
                    Member member = new Member(self.getUserId(), self.getName(), conversation.getMemberId(), Member.state(c.getString("state")));
                    conversation.addMember(member);
                    conversation.setSelf(member);
                    Log.d("conversation: ", conversation.toString() + ".for member_id " + conversation.getSelf().toString());
                    conversationList.add(conversation);
                }
                signalingChannelListener.onConversations(conversationList);
            } catch (JSONException e) {

                e.printStackTrace();
            }
        }
    };

    /** All error signals. **/
    private Emitter.Listener onInvalidToken = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            updateConnectStatus(NetworkingStateListener.NETWORK_STATE.DISCONNECTED);

            conversationClient.getLoginListener().onTokenInvalid();
        }
    };

    private Emitter.Listener onSessionError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(SocketClient.TAG, "onSessionError: " + args.toString());
            release();

            notifyConnectionListeners(NetworkingStateListener.NETWORK_STATE.SESSION_TERMINATED);
        }
    };

    private Emitter.Listener onSessionTerminated = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(SocketClient.TAG, "onSessionTerminated: " + args.toString());
            updateConnectStatus(NetworkingStateListener.NETWORK_STATE.DISCONNECTED);

            notifyConnectionListeners(NetworkingStateListener.NETWORK_STATE.SESSION_TERMINATED);
        }
    };

    private void notifyConnectionListeners(NetworkingStateListener.NETWORK_STATE state) {
        for(NetworkingStateListener networkingStateListener : conversationClient.getNetworkingStateListeners())
            networkingStateListener.onNetworkingState(state);
    }

    private Emitter.Listener onInvalidSession = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(SocketClient.TAG, "onInvalidSession: " + args.toString());
            updateConnectStatus(NetworkingStateListener.NETWORK_STATE.DISCONNECTED);

            notifyConnectionListeners(NetworkingStateListener.NETWORK_STATE.SESSION_TERMINATED);
        }
    };
    private Emitter.Listener onExpiredToken = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(SocketClient.TAG, "onExpiredToken: " + args.toString());
            updateConnectStatus(NetworkingStateListener.NETWORK_STATE.DISCONNECTED);

            conversationClient.getLoginListener().onTokenExpired();
        }
    };

    private Emitter.Listener onEventError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onEventError ", data.toString());
        }
    };

    private Emitter.Listener onMessageError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onMessageError ", data.toString());
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(SocketClient.TAG, "onConnectError.");
            updateConnectStatus(NetworkingStateListener.NETWORK_STATE.DISCONNECTED);

            notifyConnectionListeners(NetworkingStateListener.NETWORK_STATE.CONNECT_ERROR);
        }
    };

    private Emitter.Listener onConnectTimeout = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(SocketClient.TAG, "onConnectTimeout: " + args.toString());
            updateConnectStatus(NetworkingStateListener.NETWORK_STATE.CONNECT_TIMEOUT);

            notifyConnectionListeners(NetworkingStateListener.NETWORK_STATE.CONNECT_TIMEOUT);
        }
    };

    private Emitter.Listener onReconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(SocketClient.TAG, "onReconnect: " + args.toString());
            updateConnectStatus(NetworkingStateListener.NETWORK_STATE.RECONNECT);

            notifyConnectionListeners(NetworkingStateListener.NETWORK_STATE.RECONNECT);
        }
    };
    private Emitter.Listener onReconnecting = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(SocketClient.TAG, "onReconnecting: " + args.toString());
            updateConnectStatus(NetworkingStateListener.NETWORK_STATE.RECONNECT);

            notifyConnectionListeners(NetworkingStateListener.NETWORK_STATE.RECONNECT);
        }
    };
    private Emitter.Listener onReconnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(SocketClient.TAG, "onReconnectError: " + args.toString());
            updateConnectStatus(NetworkingStateListener.NETWORK_STATE.DISCONNECTED);

            notifyConnectionListeners(NetworkingStateListener.NETWORK_STATE.RECONNECT_ERROR);
        }
    };
    private Emitter.Listener onReconnectFailed = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(SocketClient.TAG, "onReconnectFailed: " + args.toString());
            updateConnectStatus(NetworkingStateListener.NETWORK_STATE.DISCONNECTED);

            notifyConnectionListeners(NetworkingStateListener.NETWORK_STATE.RECONNECT_ERROR);
        }
    };
    private Emitter.Listener onReconnectAttempt = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(SocketClient.TAG, "onReconnectAttempt: " + args.toString());
            updateConnectStatus(NetworkingStateListener.NETWORK_STATE.RECONNECT);

            notifyConnectionListeners(NetworkingStateListener.NETWORK_STATE.RECONNECT);
        }
    };

    private Emitter.Listener onConnected = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(SocketClient.TAG, "on Connected");
            updateConnectStatus(NetworkingStateListener.NETWORK_STATE.CONNECTED);

            login();

            notifyConnectionListeners(NetworkingStateListener.NETWORK_STATE.CONNECTED);
        }
    };

    // disconnect may have been explicitly called! TODO handle this case
    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(SocketClient.TAG, "on Disconnected");
            updateConnectStatus(NetworkingStateListener.NETWORK_STATE.DISCONNECTED);
            conversationList.clear();

            notifyConnectionListeners(NetworkingStateListener.NETWORK_STATE.DISCONNECTED);
            //remove user info
            self = null;
        }
    };

}
