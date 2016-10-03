/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.config;

/**
 * General configurations.
 */
public class Config {

    /** Environment endpoint: production or sandbox. */
    /** Used for applications deployed in production. */
    public static final String ENDPOINT_PRODUCTION = "https://ws.nexmo.com";

    public static final String ENDPOINT_SANDBOX = "";
    /** Media service endpoints **/
    public static final String IPS_ENDPOINT_PRODUCTION = "https://api.nexmo.com/v1/image";

    /** Current Nexmo Conversion SDK version. */
    public static final String SDK_REVISION_CODE = "0.0.1";

}