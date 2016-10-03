/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.core.networking;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import com.nexmo.sdk.conversation.client.ConversationClient;
import com.nexmo.sdk.conversation.client.ImageRepresentation;
import com.squareup.okhttp.ResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Image downloader.
 */
public class ImageDownloader {
    static final String TAG = ImageDownloader.class.getSimpleName();
    //todo provide a cancel method

    public static void downloadImage(final ImageRepresentation imageRepresentation, com.squareup.okhttp.Callback callback) {
        Log.d(TAG, "downloadImage ");

        final com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                .url(imageRepresentation.url)
                .addHeader(Constants.CUSTOM_HEADER_AUTHORIZATION, "Bearer " + ConversationClient.get().getToken())
                .build();

        ImageUploadRequestQueue.getInstance().getClient().newCall(request).enqueue(callback); //execute
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);

        return resizedBitmap;
    }

    public static Bitmap decodeImage(com.squareup.okhttp.Response response) throws IOException {

        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        ResponseBody body = response.body();
        InputStream inputStream = response.body().byteStream();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n = 0;
        while (-1!=(n=inputStream.read(buf)))
        {
            out.write(buf, 0, n);
        }
        out.close();
        inputStream.close();

        byte[] responseByteArray = out.toByteArray();
        return BitmapFactory.decodeByteArray(responseByteArray, 0, responseByteArray.length);
    }

}
