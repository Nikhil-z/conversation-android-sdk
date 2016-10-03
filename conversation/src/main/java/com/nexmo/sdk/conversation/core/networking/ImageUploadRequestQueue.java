/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.core.networking;

import com.squareup.okhttp.OkHttpClient;

/**
 * Image upload queue for Image Processing service.
 */
public class ImageUploadRequestQueue {
    private static final String TAG = ImageUploadRequestQueue.class.getSimpleName();
    private static ImageUploadRequestQueue sInstance;
    OkHttpClient client ;

    private ImageUploadRequestQueue(){
        this.client = getClient();
    }

    public static synchronized ImageUploadRequestQueue getInstance() {
        if (sInstance == null)
            sInstance = new ImageUploadRequestQueue();

        return sInstance;
    }

    public OkHttpClient getClient() {
        if (this.client == null)
            this.client = new OkHttpClient();

        return this.client;
    }

}
