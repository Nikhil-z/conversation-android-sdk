/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.device;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;

/**
 * Utility class for accessing device properties.
 */
public class DeviceProperties {

    private static final String TAG = DeviceProperties.class.getSimpleName();

    /**
     * Get the Android API version.
     *
     * @return The Android API version.
     */
    public static String getApiLevel() {
        return Build.VERSION.RELEASE;
    }

    /**
     * A randomly generated 64-bit number on the device's first boot that remains constant
     * for the lifetime of the device.
     *
     * @return The device ANDROID_ID, or null if the context is not supplied.
     */
    public static String getAndroid_ID(Context context) {
        if (context != null)
            return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return null;
    }

    /**
     * Get the IP address of the current device.
     * @param context The context of the sender activity.
     *
     * @return The IP address of the current device.
     * TODO add runtime permission check for WIFI_state
     */
    public static String getIPAddress(Context context) {
        if (context != null) {
            // Use the Application context to prevent memory leaks when referencing activities that are being killed.
            Context appContext = context.getApplicationContext();

            WifiManager manager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
            // Get the WiFi or cellular network IP address.
            if (manager.isWifiEnabled()) {
                int ipAddress = manager.getConnectionInfo().getIpAddress();
                // Format the integer IP address to the numeric representation.
                return ((ipAddress>>24) & 0xFF) + "." + ((ipAddress>>16) & 0xFF) + "." + ((ipAddress>>8) & 0xFF) + "." + ((ipAddress & 0xFF));
            } else {
                try {
                    for (Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces(); networks.hasMoreElements();) {
                        NetworkInterface networkInterface = networks.nextElement();
                        for (Enumeration<InetAddress> ipAddresses = networkInterface.getInetAddresses(); ipAddresses.hasMoreElements();) {
                            InetAddress inetAddress = ipAddresses.nextElement();
                            // Ignore the loopback address.
                            // // If only the loopback is available, it is not possible to do any requests to the service.
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    Log.e(TAG, "Error getting the IP address." + e.getMessage());
                }
            }
        }
        return  null;
    }

    /**
     * Get the user's preferred locale language. The format is language code and country code, separated by dash.
     * <p> Since the user's locale changes dynamically, avoid caching this value.
     *
     * @return The user's preferred language.
     */
    public static String getLanguage() {
        String language = Locale.getDefault().toString();
        if (!TextUtils.isEmpty(language) && language.indexOf("_") > 1) {
            return language.replace("_", "-");
        }
        return null;
    }

}
