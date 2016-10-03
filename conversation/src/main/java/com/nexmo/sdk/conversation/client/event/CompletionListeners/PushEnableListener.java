/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client.event.CompletionListeners;

import com.nexmo.sdk.conversation.client.event.ConversationGenericListener;

/**
 * Push settings listener.
 */
public interface PushEnableListener extends ConversationGenericListener {

    /**
     * Push enabling/disabling was successfully set.
     */
    void onSuccess();

}
