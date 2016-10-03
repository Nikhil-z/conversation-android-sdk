/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Each image representation can be defined by type: {@link TYPE}
 * //todo
 */
public class ImageRepresentation implements Parcelable {
    private static final String TAG = ImageRepresentation.class.getSimpleName();

    public enum TYPE {
        ORIGINAL,
        MEDIUM,
        THUMBNAIL
    }
    public TYPE type;
    @Expose
    @SerializedName("id")
    public String id;
    @Expose
    @SerializedName("url")
    public String url;
    @Expose
    @SerializedName("size")
    public long size;
    public Bitmap bitmap;

    public ImageRepresentation(TYPE type, String id, String url, long size) {
        this.type = type;
        this.id = id;
        this.url = url;
        this.size = size;
    }

    public ImageRepresentation(String typeString, String id, String url, long size) {
        this.type = TYPE.valueOf(typeString);
        this.id = id;
        this.url = url;
        this.size = size;
    }

    protected ImageRepresentation(Parcel in) {
        this.type = TYPE.valueOf(in.readString());
        this.id = in.readString();
        this.url = in.readString();
        this.size = in.readLong();
    }

    public static final Creator<ImageRepresentation> CREATOR = new Creator<ImageRepresentation>() {
        @Override
        public ImageRepresentation createFromParcel(Parcel in) {
            return new ImageRepresentation(in);
        }

        @Override
        public ImageRepresentation[] newArray(int size) {
            return new ImageRepresentation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type.toString());
        dest.writeString(this.id);
        dest.writeString(this.url);
        dest.writeLong(this.size);
    }

    @Override
    public String toString() {
        return TAG + " type: " + this.type.toString() + ". id: " + (this.id != null ? this.id : "") +
                ".url: " + (this.url != null ? this.url : "") +
                ".size: " + this.size;
    }

}
