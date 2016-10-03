# The Nexmo Android Conversation SDK
=======


Usage
=======

The ConversationClient is the Nexmo Conversation SDK entry point.
First step is to acquire a ConversationClient instance based on a context.
``` java
        try {
            ConversationClient client = new ConversationClient.ConversationClientBuilder()
                    .context(this)
                    .build();
        } catch (ConversationClientException e) {
            //  deny app use!
        }
```

Listen to connection events, as the socket might get disconnected at any point without prior notice.
Make sure to update the UI when needed.
```java
        client.listenToConnectionEvents(new NetworkingStateListener() {
            @Override
            public void onNetworkingState(NETWORK_STATE networkingState) {
                switch (networkingState) {
                    case CONNECTED:{
                        Log.d(TAG, "onConnected");
                        break;
                    }
                    case DISCONNECTED:{
                        Log.d(TAG, "onDisconnected");
                        break;
                    }
                    case SESSION_TERMINATED:{
                        Log.d(TAG, "onSessionTerminated");
                        break;
                    }
                    case CONNECT_ERROR: {
                        Log.d(TAG, "Connect err");
                        break;
                    }
                    case CONNECT_TIMEOUT:{
                        Log.d(TAG, "Connect timeout");
                        break;
                    }
                    case RECONNECT:{
                        Log.d(TAG, "onReconnect");
                        break;
                    }
                    case RECONNECT_ERROR:{
                        Log.d(TAG, "reconnn err");
                        break;
                    }
                }
            }
        });
````

Login

Login method will accept any unique String as a User ID ( email addresses, phone numbers, usernames, etc), so you can use any new or existing User Management system. 

As part of the Authentication process, you will need to set up a Web Service which generates a unique Identity Token for each user on request.
```java
        client.login(token, new LoginListener() {
        @Override
        public void onLogin(User user) {
            Log.d(TAG, "onLogin " + user.toString());
            self = user;
            // can update UI user info for ex.
        }

        @Override
        public void onUserAlreadyLoggedIn(User user) {
        }

        @Override
        public void onError(int errCode, String errMessage) {
        }
    });
```

Logout
```java
        client.logout(new LogoutListener() {
        @Override
        public void onLogout(User user) {
        }

        @Override
        public void onError(int errCode, String errMessage) {
        }
    });
```

Conversations

Create a conversation
```java
        client.newConversation(conversation_name, new ConversationCreateListener() {
        @Override
        public void onConversationCreated(Conversation conversation) {
        }

        @Override
        public void onError(int errCode, String errMessage) {
        }
    });
```

Joining a conversation will get back a Member.
```java
        conversation.join(new JoinListener() {
        @Override
        public void onConversationJoined(Conversation conversation, Member member) {
        }

        @Override
        public void onError(int errCode, String errMessage) {
        }
    });
```

Events

Sending a text
```java
        conversation.sendText("payload", new TextSendListener() {
        @Override
        public void onTextSent(Conversation conversation, Text text) {
        }

        @Override
        public void onError(int errCode, String errMessage) {
        }
    });
```

Sending an image
```java
        onversation.sendImage(“image_path”, new ImageSendListener() {
        @Override
        public void onImageSent(Conversation conversation, Image image) {
        }

        @Override
        public void onError(int errCode, String errMessage) {
        }
    });
```

Marking an event as seen:
```java
        text.markAsSeen(new TextMarkAsSeenListener() {
        @Override
        public void onMarkedAsSeen(Conversation conversation) {
        }

        @Override
        public void onError(int errCode, String errMessage) {
        }
    });
```
Seen receipts
For one on one Conversations, or small group Conversations, it makes sense to show the current state of a given message.
You can then choose to mark any Message as Read once it is actually displayed in the UI.

Listening for text events, including typing indicator and seen receipts from all members:
```java
        conversation.addTextListener(new TextListener() {
        @Override
        public void onTextReceived(Conversation conversation, Text message) {
            Log.d(TAG, "onTextReceived " + message.getPayload() + " from : " + message.getMember.getMemberId());
        }

        @Override
        public void onTextDeleted(Conversation conversation, Text message, Member member) {
            Log.d(TAG, "onTextDeleted by: " + member.getName());
        }

        //...other text events.
        });
```
Typing indicator
Members can get real-time events of someone else typing.

Sending startTyping/stopTyping for current member:
```java
        conversation.startTyping(new TypingSendListener() {
        @Override
        public void onTypingSent(Conversation conversation, Member.TYPING_INDICATOR typingIndicator) {
            Log.d(TAG, "onTypingIndicatorSent");
        }
    });
```

Invites

Sending an invite
```java
        conversation.invite(user, new InviteSendListener() {
        @Override
        public void onInviteSent(String conversationId, String memberId) {
            Log.d(TAG, user + " was invited");
        }

        @Override
        public void onError(int errCode, String errMessage) {
        }
    });
```

Listening for member events, including invites addMemberInvitedListener, addMemberLeftListener, addMemberJoinedListener
```java
        conversation.addMemberInvitedListener(new MemberInvitedListener() {
        @Override
        public void onMemberInvited(Conversation conversation, Member invitedMember, String invitedByMemberId, String invitedByUsername) {
            Log.d(TAG,  "member invited");
            }
        });
```

Accept an invite
```java
        client.acceptInvite(member_id, new JoinListener() {
        @Override
        public void onConversationJoined(Conversation conversation, Member member);
            Log.d(TAG, "Joined as " + member.getMember_id());
        }
        @Override
        public void onError(int errCode, String errMessage) {
        }
    });
```

Leave conversation
```java
        conversation.leave(new LeaveListener() {
        @Override
        public void onConversationLeft();
        }
        @Override
        public void onError(int errCode, String errMessage) {
        }
    });
```

Kick members
```java
        conversation.kick(Member member, new LeaveListener() {
        @Override
        public void onConversationLeft();
        }
        @Override
        public void onError(int errCode, String errMessage) {
        }
    });
```


License
=======

Copyright (c) 2016 Nexmo, Inc.
All rights reserved.

By downloading or otherwise using our software or services, you acknowledge
that you have read, understand and agree to be bound by the
[Privacy Policy][1].

You may not use, exercise any rights with respect to or exploit this SDK,
or any modifications or derivative works thereof, except in accordance with the License.

 [1]: https://www.nexmo.com/privacy-policy/

Author
=======

Emma Tresanszki, oss@nexmo.com



Contributing
=======

If you would like to contribute code you can do so through GitHub by forking the repository and sending a pull request.
