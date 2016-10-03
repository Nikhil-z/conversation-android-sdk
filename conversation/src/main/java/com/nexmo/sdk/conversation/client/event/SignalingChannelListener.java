/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.client.event;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.Image;
import com.nexmo.sdk.conversation.client.Member;
import com.nexmo.sdk.conversation.client.SeenReceipt;
import com.nexmo.sdk.conversation.client.Text;
import com.nexmo.sdk.conversation.client.User;
import com.nexmo.sdk.conversation.core.client.request.Request;

import java.util.Date;
import java.util.List;

/**
 * SignalingChannel listener
 */
public interface SignalingChannelListener extends ConversationGenericListener {

    // on:success events
    void onCreate(String tid, String conversationID);
    void onJoin(String tid, Member member);
    void onLeft(String tid,  Date invited, Date joined, Date left);
    void onConversations(List<Conversation> conversations);
    void onConversation(String tid, Conversation conversation);
    void onEventsHistory(String tid, List<Text> messages, List<Image> images, List<SeenReceipt> seenReceipts);
    void onTextSent(String tid, String textId, Date timestamp);
    void onImageSent(String tid, String imageId, Date timestamp);
    void onTextRemoved(String tid, String eventId);
    void onMarkedAsSeen(String tid);
    void onTypingOn(String tid);
    void onTypingOff(String tid);
    void onInvitationSent(String tid);

    //incoming member events
    void onMemberJoined(String cid, String memberId, User user, Date joinedTimestamp);
    void onMemberInvited(String cid, String cname, Member invitedMember, String invitedByMemberId, String invitedByUsername);
    void onMemberLeft(String cid, String memberId, User user, Date invited, Date joined, Date left);
                      //String member_id, Date timestamp);
    //incoming text events
    void onTypingOnReceived(String cid, String memberId);
    void onTypingOffReceived(String cid, String memberId);
    void onTextReceived(String cid, String memberId, Text text);
    void onTextDeleted(String cid, String memberId, String eventId, Date timestamp);

    // generic events
    void onEventSeen(String cid, String memberId, String eventId, Request.TYPE type, Date timestamp);

    //incoming image events
    void onImageReceived(String cid, String memberId, Image image);//todo add timestamp

    //push subscribers
    void onPushSubscribedToConversation(String tid);
}
