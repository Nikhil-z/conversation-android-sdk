/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.core.networking;

import android.util.Log;

import com.nexmo.sdk.conversation.client.ConversationClient;
import com.nexmo.sdk.conversation.core.client.request.SendMessageRequest;
import com.nexmo.sdk.conversation.config.Config;
import com.squareup.okhttp.MultipartBuilder;

import java.io.File;

/**
 * Image uploader.
 */
public class ImageUploader {
    static final String TAG = ImageUploader.class.getSimpleName();
    //todo provide a cancel method for the Call
    //Call uploadCall

    public static void uploadImage(final SendMessageRequest sendImageRequest, com.squareup.okhttp.Callback callback) {
        Log.d(TAG, "uploadImage ");
        File file = new File(sendImageRequest.message);
        String fileName = file.getName();

        com.squareup.okhttp.RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart(Constants.FORM_KEY_FILE, fileName,
                        com.squareup.okhttp.RequestBody.create(com.squareup.okhttp.MediaType.parse("image/jpeg"), file))
                .addFormDataPart(Constants.FORM_KEY_QUALITY_RATIO, Constants.FORM_VALUE_QUALITY_RATIO)
                .addFormDataPart(Constants.FORM_KEY_MEDIUM_RATIO, Constants.FORM_VALUE_MEDIUM_RATIO)
                .addFormDataPart(Constants.FORM_KEY_THUMBNAIL_RATIO, Constants.FORM_VALUE_THUMBNAIL_RATIO)
                .build();

        final com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                .url(Config.IPS_ENDPOINT_PRODUCTION)
                .addHeader(Constants.CUSTOM_HEADER_AUTHORIZATION, Constants.CUSTOM_HEADER_VALUE + ConversationClient.get().getToken())
                .post(requestBody)
                .build();

        ImageUploadRequestQueue.getInstance().getClient().newCall(request).enqueue(callback);
    }

}
