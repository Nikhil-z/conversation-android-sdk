/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A user is a person associated with your Application.
 * You populate the application users from your customer database using Application API.
 * You use this SDK to add and remove users to a Conversation. A member is a User who has joined a Conversation.
 * The following code example shows how to add a User to a Conversation:
 * <pre>
 * client.login(token, new LoginListener() {
 *         &#64;Override
 *         public void onLogin(User user) {
 *             Log.d(TAG, "onLogin " + user.toString());
 *             self = user;
 *             // can update UI user info for ex.
 *         }
 *
 *         &#64;Override
 *         public void onUserAlreadyLoggedIn(User user) {
 *         }
 *
 *         &#64;Override
 *         public void onError(int errCode, String errMessage) {
 *         }
 *     });
 * </pre>
 * The login method accepts any unique String as a User ID. You need to validate each user against your customer databse before adding them to a Conversation.
 */
public class User implements Parcelable {

    private static final String TAG = User.class.getSimpleName();
    @Expose
    @SerializedName("user_id")
    private String userId;
    @Expose
    @SerializedName("name")
    private String name;

    public User(final String userId, final String name) {
        this.userId = userId;
        this.name = name;
    }

    public User(User user) {
        this(user.getUserId(), user.getName());
    }

    protected User(Parcel in) {
        this.userId = in.readString();
        this.name = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    protected User() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(name);
    }

    @Override
    public boolean equals(Object user) {
        if(this == user) return true;//if both of them points the same address in memory

        if(!(user instanceof User)) return false;

        User userCast = (User)user;

        return this.name.equals(userCast.name) && this.userId == userCast.userId;
    }

    public String getName() {
        return this.name;
    }

    public String getUserId() {
        return this.userId;
    }

    @Override
    public String toString(){
        return TAG + " name: " + (this.name!= null ? this.name: "") + ". userId: " + (this.userId != null ? this.userId : "");
    }
}
