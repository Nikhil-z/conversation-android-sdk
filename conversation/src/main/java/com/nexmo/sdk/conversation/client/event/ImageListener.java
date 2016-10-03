/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client.event;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.Image;
import com.nexmo.sdk.conversation.client.Member;

/**
 * Image event listener for notifying upon incoming image related events.
 */

public interface ImageListener {

    /**
     * Incoming {@link Image}.
     *
     * <p>
     * Once the image is downloaded, you will be able to access all 3 representations:
     * <ul>
     *     <li>Original at a 100% quality ratio via {@link Image#getOriginal()}</li>
     *     <li>Medium at a 50% quality ratio via {@link Image#getMedium()}</li>
     *     <li>Thumbnail at a 10% quality ratio via {@link Image#getThumbnail()} </li>
     * </ul></p>
     *
     * Get the associated bitmap via {@link com.nexmo.sdk.conversation.client.ImageRepresentation#bitmap}
     * for updating the UI.
     *
     * @param conversation The conversation.
     * @param image        The incoming image event.
     */
    void onImageReceived(Conversation conversation, Image image);

    // you can now call image.getOriginal().bitmap !!!!

    /**
     *
     * <p>
     * Once the image is downloaded, you will be able to access all 3 representations:
     * <ul>
     *     <li>Original at a 100% quality ratio via {@link Image#getOriginal()}</li>
     *     <li>Medium at a 50% quality ratio via {@link Image#getMedium()}</li>
     *     <li>Thumbnail at a 10% quality ratio via {@link Image#getThumbnail()} </li>
     * </ul></p>
     *
     * Get the associated bitmap via {@link com.nexmo.sdk.conversation.client.ImageRepresentation#bitmap}
     * for updating the UI.
     *
     * @param conversation The conversation
     * @param image        The incoming image event, updated with the bitmaps.
     */
    void onImageDownloaded(Conversation conversation, Image image);

    /**
     * Downloading the associated {@link com.nexmo.sdk.conversation.client.ImageRepresentation}
     * has failed.
     *
     * @param conversation The conversation.
     * @param image        The incoming image event.
     */
    void onDownloadFailed(Conversation conversation, Image image);

    /**
     * An Image was deleted.
     *
     * @param conversation The conversation object.
     * @param message      The message that got deleted.
     * @param member       The member that removed the message.
     */
    void onImageDeleted(Conversation conversation, Image message, Member member);

    /**
     * Image event was flagged as seen by other member.
     * The {@link SeenReceipt} contains:
     * <ul>
     *     <li>The event id of the event flagged as seen.</li>
     *     <li>The id of the member that has set the flag.</li>
     *     <li>The receipt timestamp.</li>
     * </ul>
     *
     * @param conversation      The conversation associated to the message.
     * @param seenReceipt       The seen receipt.
     */
    //void onImageSeen(Conversation conversation, SeenReceipt seenReceipt);

}
