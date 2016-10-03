/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.bugsnag.android.Bugsnag;

import com.nexmo.sdk.conversation.client.event.CompletionListeners.ConversationCreateListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.JoinListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.LoginListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.LogoutListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.PushEnableListener;

import com.nexmo.sdk.conversation.client.event.ConversationClientException;
import com.nexmo.sdk.conversation.client.event.ConversationInvitedListener;
import com.nexmo.sdk.conversation.client.event.ConversationListListener;
import com.nexmo.sdk.conversation.client.event.MemberInvitedListener;
import com.nexmo.sdk.conversation.client.event.SignalingChannelListener;
import com.nexmo.sdk.conversation.client.event.network.NetworkingStateListener;
import com.nexmo.sdk.conversation.client.event.network.NetworkingStateListener.NETWORK_STATE;

import com.nexmo.sdk.conversation.config.Config;
import com.nexmo.sdk.conversation.core.client.ConversationSignalingChannel;

import java.util.ArrayList;

/**
 * You use a <i>ConversationClient</i> instance to utilise the services provided by Conversation API in your app.
 *
 * A session is the period during which your app is connected to Conversation API.
 * Sessions are established for the length of time given when the token was created.
 *
 * Tokens also have a lifetime and can optionally be one-shot which will allow a single login only, before
 * the token becomes invalid for another login attempt. If the token is revoked while a session is active the
 * session may be terminated by the server.
 * It is only possible to have a single session active over a socket.io connection at a time.
 * Session multiplexing is not supported.</p>
 *
 * <strong>Note</strong>: The connection uses socket.io for both web and mobile clients.
 * Upon a successful socket.io connection the client needs to authenticate itself.
 * This is achieved by sending a login request via {@link ConversationClient#login(String, LoginListener)}</p>
 *
 * <p>Unless otherwise specified, all the methods invoked by this client are executed asynchronously.</p>
 *
 * <p>For the security of your Nexmo account, you should not embed directly your Conversation credential token as strings in the app you submit to the Google Play Store.</p>
 *
 * First step is to acquire a {@link ConversationClient} instance based on user credentials.
 * <p>To construct a {@link ConversationClient} the required parameters are:</p>
 * <ul>
 *     <li>applicationContext:  The application context.</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 *     ConversationClient myClient = new ConversationClient.ConversationClientBuilder()
 *                     .context(applicationContext)
 *                     .build();
 *
 *     myClient.login("token", new LoginListener() {
 *         &#64;Override
 *         public void onUserAlreadyLoggedIn(User user) {
 *              // Update the application UI here if needed.
 *         }
 *         &#64;Override
 *         public void onLogin(User user) {
 *              // Update the application UI here if needed.
 *         }
 *          &#64;Override
 *         public void onTokenInvalid() {
 *              // Update the application UI here if needed.
 *         }
 *         &#64;Override
 *         public void onTokenExpired() {
 *              // Update the application UI here if needed.
 *         }
 *         &#64;Override
 *         public void onError(int errorCode, String message) {
 *              // Update the application UI here if needed.
 *         }
 *    });
 * </pre>
 *
 * <p>The list of active conversations can be retrieved via
 * {@link ConversationClient#getConversations(ConversationListListener)}.</p>
 *
 * <p> Example usage, performing a {@link ConversationClient#getConversations(ConversationListListener)}
 * for the logged in user:</p>
 *
 * <pre>
 *     myClient.getConversations(new ConversationListListener() {
 *         &#64;Override
 *         public void onConversationList(List<Conversation> conversations) {
 *              // Update the application UI here if needed.
 *         }
 *         &#64;Override
 *         public void onError(int errorCode, String message) {
 *              // Update the application UI here if needed.
 *         }
 *    });
 * </pre>
 *
 * <p>Joining a conversation is done via {@link Conversation#join(JoinListener)}</p>
 * Note: any {@link User} that joins a {@link Conversation} becomes a {@link Member}.
 * <p> Example usage:</p>
 *
 * <pre>
 *     myConversation.join(new ConversationJoinListener() {
 *         &#64;Override
 *         public void onConversationJoined(Conversation conversation, Member member) {
 *              // Update the application UI here if needed. From now on the member may send messages in this conversation.
 *         }
 *         &#64;Override
 *         public void onError(int errorCode, String message) {
 *              // Update the application UI here if needed.
 *         }
 *    });
 * </pre>
 *
 * Remember to logout when needed in order to disconnect the underlying socket.
 * <p>Example usage:</p>
 * <pre>
 *     myClient.logout(new LogoutListener() {
 *         &#64;Override
 *         public void onLogout(User user) {
 *              // Update the application UI here if needed.
 *         }
 *         &#64;Override
 *         public void onError(int errorCode, String message) {
 *              // Update the application UI here if needed.
 *         }
 *    });
 * </pre>
 *
 */
public class ConversationClient implements Parcelable {

    private static final String TAG = ConversationClient.class.getSimpleName();
    public static volatile ConversationClient mInstance = null;
    private ConversationSignalingChannel signalingChannel;
    //private SignalingChannelListener clientListener;
    // map, array of active conversations/calls
//    private ConversationCall
    private Context context;
    private String token;
    private String environmentHost = Config.ENDPOINT_PRODUCTION;
    private boolean enableCrashReporting;


    private LoginListener loginListener;
    private LogoutListener logoutListener;
    private User myUser;
    //explicit disconnect issued by logout. otherwise socket gets disconnected for another reason.
    private boolean explicitDisconnect = false;
    private ArrayList<NetworkingStateListener> networkingStateListeners = new ArrayList<>();
    // Indicates if the client should try to reconnect after the socket gets disconnected.
    private boolean reconnectEnabled;

    //push setting persisted
    private boolean pushEnabledForAllConversations;
    private String pushDeviceToken;
    private PushEnableListener pushEnableListener;

    private ArrayList<Conversation> conversations = new ArrayList<>();

    private ConversationClient(final Context context) {
        this.context = context;
        this.signalingChannel = new ConversationSignalingChannel(this);
    }

    private ConversationClient(final Context context, final String environmentHost) {
        this(context);
        this.environmentHost = environmentHost;
    }

    private ConversationClient(final Context context, final String environmentHost, final boolean enableCrashReporting) {
        this(context, environmentHost);
        if (enableCrashReporting) {
            this.enableCrashReporting = true;
            Bugsnag.init(context);
            Bugsnag.setAppVersion(Config.SDK_REVISION_CODE);
        }
    }

    /**
     * Check whether the {@link ConversationClient} is trying to connect to the backend socket or not.
     * Use this method whenever you want a single fast check of the connection status.
     * Nevertheless, {@link NetworkingStateListener} does notify upon all of the different connection statuses as well.
     *
     * @return The socket connected status. One of the {@link NETWORK_STATE}
     */
    public NETWORK_STATE getNetworkingState() {
        return this.signalingChannel.getConnectionStatus();
    }

    public static ConversationClient get() {
        return mInstance;
    }

    ConversationSignalingChannel getSignallingChannel() {
        return this.signalingChannel;
    }

    public String getEnvironmentHost() {
        return this.environmentHost;
    }

    /**
     * Check whether a User has been successfully logged in.
     *
     * @return boolean.
     */
    public boolean isLoggedIn() {
        return this.signalingChannel.isLoggedIn() != null;
    }

    /**
     * Get the current logged in User if available.
     *
     * @return The current user.
     */
    public User getLoggedInUser() {
        return this.signalingChannel.isLoggedIn();
    }


    public void applicationWillResignActive(){

    }

    /**
     * Attach a listener for connection events.
     * Any application should listen for those events as the socket might get disconnected/reconnecting at any stage
     * without prior notice.
     *
     * @param listener The {@link NetworkingStateListener}.
     */
    public void listenToConnectionEvents(NetworkingStateListener listener) {
        if (!this.networkingStateListeners.contains(listener))
            synchronized(this) {
                this.networkingStateListeners.add(listener);
            }
    }

    public ArrayList<NetworkingStateListener> getNetworkingStateListeners() {
        return this.networkingStateListeners;
    }

    public void removeConnectionListenerOnceUsed(NetworkingStateListener listener) {
        if (this.networkingStateListeners != null && this.networkingStateListeners.contains(listener))
            this.networkingStateListeners.remove(listener);
    }

    public void cleanLoginListener() {
        if (this.loginListener != null)
            this.loginListener = null;
    }

    /**
     * Updates user after login/logout
     * @param user
     */
    public void setMyUser(final User user) {
        this.myUser = user;
    }

    public User getMyUser() {
        return this.myUser;
    }

    public LoginListener getLoginListener() {
        return this.loginListener;
    }

    public LogoutListener getLogoutListener() {
        return this.logoutListener;
    }

    public PushEnableListener getPushEnableListener() {
        return this.pushEnableListener;
    }

    /**
     * Login this {@link ConversationClient} instance based on the builder information and login details.
     * <p> Only one {@link ConversationClient#login(String, LoginListener)} action can be performed at a time. </p>
     *
     * <p>A network connection will be established with the Conversation service, make sure you listen to connection events
     *  {@link ConversationClient#listenToConnectionEvents(NetworkingStateListener)}</p>
     *
     * Required parameters are:
     * <ul>
     *      <li>token:              The backend authorization token.</li>
     * </ul>
     *
     * @param token    The backend authorization jwt.
     * @param listener The {@link LoginListener}.
     */
    public void login(final String token, final LoginListener listener) {
        if (listener != null) {
            this.loginListener = listener;

            if (this.signalingChannel.isLoggedIn() != null)
                this.loginListener.onUserAlreadyLoggedIn(new User(this.signalingChannel.isLoggedIn()));
            else if (this.signalingChannel.isConnecting()) //todo add CONNECTING state
                this.loginListener.onError(LoginListener.CONNECT_ALREADY_IN_PROGRESS, "Already connecting");
            else if (TextUtils.isEmpty(token))
                this.loginListener.onError(LoginListener.GENERIC_ERR, TAG + " onError: Missing params");
            else {
                this.token = token;

                /**
                 * Ask to establish a network connection with the Conversation service.
                 * If another connection is attempted already, an error is received.
                 */
                this.signalingChannel.connect();
            }
        } else
            Log.d(TAG, "LoginListener is mandatory!");
    }

    /**
     * Internal method for disconnect from the client, releasing the socket connection.
     */
    public void disconnect() {
        synchronized(this) {
            this.networkingStateListeners.clear();
        }
        this.signalingChannel.release();
    }

    /**
     * Logout current user.
     * Logout also disconnects from the Conversation socket, removes all event listeners and deletes any locally stored data.
     *
     * <p> Only one {ConversationClient#logout(LogoutListener)} can be performed at a time. </p>
     *
     * @param logoutListener The {@link LogoutListener}.
     */
    public void logout(LogoutListener logoutListener) {
        if (logoutListener != null) {
            this.logoutListener = logoutListener;

            if (this.signalingChannel.isLoggedIn() != null)
                this.signalingChannel.logout();
            else
                this.logoutListener.onError(LogoutListener.MISSING_USER, "No user is logged in");
        }
        else
            Log.d(TAG, "LogoutListener is mandatory!");
    }

    /**
     * Set/Reset the push registration token whenever there is a new one available, via FirebaseInstanceIdService, prior to
     * {@link ConversationClient#login(String, LoginListener)}.
     *
     * <p>User must perform another login if a new push token is issued.</p>
     *
     * @param pushDeviceToken The instance ID that uniquely identifies an app/device pairing for push purposes. Remember to update the
     *                        token provided by a FirebaseInstanceIdService each time  the InstanceID token is updated.
     */
    public void setPushDeviceToken(final String pushDeviceToken) {
        //cache it internally until login.
        synchronized(this){
            this.pushDeviceToken = pushDeviceToken;
        }
    }

    /**
     * Enable or Disable any push notifications triggered by any of the available conversations, using Firebase Cloud Messaging.
     *
     * <p></p>User must be logged in and set {@link ConversationClient#setPushDeviceToken} obtained by extending a FirebaseInstanceIdService.</p>
     *
     * <p>By default push notifications are disabled, until {@link ConversationClient#setPushDeviceToken(String)} and
     * any of the {@link ConversationClient#enableAllPushNotifications(boolean, PushEnableListener)} or
     *{@link ConversationClient#enablePushNotificationsForConversation(boolean, String, PushEnableListener)} are set.</p>
     *
     * @param enable             True or False.
     * @param pushEnableListener Completion listener.
     */
    public void enableAllPushNotifications(boolean enable, PushEnableListener pushEnableListener) {
        if (pushEnableListener != null) {
            this.pushEnableListener = pushEnableListener;

            if (this.signalingChannel.isLoggedIn() != null) {
                //enable/disable push regardless of logged in state.
                this.pushEnabledForAllConversations = enable;
                this.signalingChannel.enableAllPushNotifications(enable);
            }
            else
                this.pushEnableListener.onError(LogoutListener.MISSING_USER, "No user is logged in");

        }
        else
            Log.d(TAG, "PushEnableListener is mandatory!");
    }

    /**
     * NOTE This is not yet implemented service-side!
     *
     * Enable or Disable push notifications for certain conversations.
     * Push notifications must be enabled first via {@link ConversationClient#enableAllPushNotifications(boolean, PushEnableListener)}.
     *
     * @param enable             True or False.
     * @param conversationId     The conversation id.
     * @param pushEnableListener Completion listener.
     */
    public void enablePushNotificationsForConversation(boolean enable, String conversationId, PushEnableListener pushEnableListener) {
        if (pushEnableListener != null) {
            this.pushEnableListener = pushEnableListener;
            //enable/disable push regardless of logged in state.
            this.pushEnabledForAllConversations = (enable && this.pushEnabledForAllConversations);
            this.signalingChannel.enablePushNotifications(enable, conversationId, pushEnableListener);
        }
        else
            Log.d(TAG, "PushEnableListener is mandatory!");
    }

    /**
     * Retrieve the push registration token that was once set.
     *
     * @return The Firebase push registration token.
     */
    public String getPushDeviceToken() {
        return this.pushDeviceToken;
    }

    /**
     * Register for receiving conversation invitations.
     *
     * @param conversationInvitedListener
     * Text event listener for notifying upon incoming text related events.
     */
    public void addConversationInvitedListener(ConversationInvitedListener conversationInvitedListener) {
        if (conversationInvitedListener != null) {
            if (this.signalingChannel.isLoggedIn() != null)
                this.signalingChannel.addConversationInvitedListener(conversationInvitedListener);
            else
                conversationInvitedListener.onError(SignalingChannelListener.MISSING_USER, "No user is logged in");
        }
        else
            Log.d(TAG, "ConversationInvitedListener is mandatory!");
    }

    public void removeConversationInvitedListener(ConversationInvitedListener conversationInvitedListener) {
        if (conversationInvitedListener != null)
            this.signalingChannel.removeConversationInvitedListener(conversationInvitedListener);
    }

    /**
     * Create a new {@link Conversation} for a given name.
     *
     * <p>  In order for a {@link User} to be able to send and receive events from a {@link Conversation}, he must either:
     * <ul>
     *     <li>explicitly {@link Conversation#join(JoinListener)} and receive a {@link Member} instance.</li>
     *     <li>accept an invitation.
     * Listen for incoming invitations using {@link Conversation#addMemberInvitedListener(MemberInvitedListener)} </li>
     * </ul> </p>
     *
     * @param conversationName           The optional conversation name. Must be unique, but it's case-insensitive.
     * @param conversationCreateListener The request completion listener.
     */
    public void newConversation(final String conversationName, ConversationCreateListener conversationCreateListener) {
        if (conversationCreateListener != null) {
            if (this.signalingChannel.isLoggedIn() != null)
                this.signalingChannel.newConversation(conversationName, conversationCreateListener);
            else
                conversationCreateListener.onError(SignalingChannelListener.MISSING_USER, "No user is logged in");
        }
        else
            Log.d(TAG, "ConversationCreateListener is mandatory!");
    }

    /**
     * Retrieve a full list of conversations the logged in user is a Member of.
     *
     * <p>A {@link Member} is part of a {@link Conversation} if:
     *  <li>
     *      <ul> he gets invited by some other Member. </ul>
     *      <ul> simply joins because he knows the conversation id. </ul>
     *  </li>
     * TODO:
     * When available local cache is enforced while reading through the conversations.
     * All the conversations are updated while synchronization is made whenever a new event occurs.
     *
     * @param conversationListListener The listener in charge of dispatching the result.
     */
    public void getConversations(ConversationListListener conversationListListener) {
        //if cache is empty-first run, trigger server request.
        if (conversationListListener != null) {
                if (this.signalingChannel.isLoggedIn() != null)
                    this.signalingChannel.getConversations(conversationListListener);
                else
                    conversationListListener.onError(SignalingChannelListener.MISSING_USER, "No user is logged in");
            }
        else
            Log.d(TAG, "ConversationListListener is mandatory");
    }

    /**
     * Detach all attached listeners.
     * <p>Note: upon logout, this is done by default.</p>
     */
    public void detachAllListeners() {
        this.signalingChannel.removeAllListeners();
    }

    /**
     * Asynchronous operation for force clearing the cache manually.
     * There is no need to clear the cache if the user logs out.
     */
    private void clearCache(){
        this.signalingChannel.clearCache();
    }

    public Context getContext() {
        return this.context;
    }

    public String getToken() { return this.token;}

    /**
     * Returns the current version of the Nexmo Conversation SDK.
     *
     *  @return The current version of the Nexmo Conversation SDK.
     */
    public static String getSDKversion() {
        return Config.SDK_REVISION_CODE;
    }

    @Override
    public String toString() {
        return TAG + " Token: " + (this.token != null ? this.token : "");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.token);
        dest.writeString(this.environmentHost);
    }

    public static final Parcelable.Creator<ConversationClient> CREATOR
            = new Parcelable.Creator<ConversationClient>() {
        public ConversationClient createFromParcel(Parcel in) {
            return new ConversationClient(in);
        }

        public ConversationClient[] newArray(int size) {
            return new ConversationClient[size];
        }
    };

    private ConversationClient(Parcel input) {
        this.token = input.readString();
        this.environmentHost = input.readString();
    }

    /**
     * API for creating {@link ConversationClient} instances.
     *
     * Acquire a ConversationClient, based on the following mandatory params:
     * <ul>
     *      <li>applicationContext: The application context.</li>
     * </ul>
     * <p> Example usage:</p>
     * <pre>
     *     try{
     *         // Create a ConversationClient using the ConversationClientBuilder.
     *         ConversationClient myConversationClient = new NexmoClient.ConversationClientBuilder()
     *                                      .context(myAppContext)
     *                                      .build();
     *     } catch (ClientBuilderException e) {
     *         e.printStackTrace();
     *     }
     * </pre>
     */
    public static class ConversationClientBuilder {

        private Context context;
        private String environmentHost = Config.ENDPOINT_PRODUCTION;
        private boolean enableCrashReporting; // by default false.
        //private boolean reconectAuto; // automatically-reconnect policy when the socket gets disconnected.

        /**
         * Build a {@link ConversationClient}, based on the following mandatory params:
         * <ul>
         * <li>applicationContext: The application context.</li>
         * </ul>
         *
         * @return an instance of {@link ConversationClient}.
         * @throws ConversationClientException A {@link ConversationClientException} if any of the mandatory params are not supplied.
         */
        public ConversationClient build() throws ConversationClientException {
            StringBuilder builder = new StringBuilder();
            if (this.context == null) {
                ConversationClientException.appendExceptionCause(builder, "Context");
                throw new ConversationClientException("Building a ConversationClient instance has failed due to missing parameters: "
                        + builder.toString());
            }

            if (mInstance == null)
                mInstance = new ConversationClient(this.context, this.environmentHost, this.enableCrashReporting);

            return mInstance;
        }

        public ConversationClientBuilder context(final Context context) {
            this.context = context;
            return this;
        }

        public ConversationClientBuilder environmentHost(final String environmentHost) {
            this.environmentHost = environmentHost;
            return this;
        }

        public ConversationClientBuilder enableCrashReporting(boolean enableCrashReporting) {
            this.enableCrashReporting = enableCrashReporting;
            return this;
        }

    }

}
