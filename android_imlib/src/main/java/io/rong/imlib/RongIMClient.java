package io.rong.imlib;

import io.rong.imlib.NativeObject.ConnectAckCallback;
import io.rong.imlib.NativeObject.DownFileCallback;
import io.rong.imlib.NativeObject.ExceptionListener;
import io.rong.imlib.NativeObject.PublishAckListener;
import io.rong.imlib.NativeObject.SendFileCallback;
import io.rong.imlib.NativeObject.UserInfoOutputListener;
import io.rong.imlib.version.Version;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

/**
 * IM 客户端核心类。
 * <p/>
 * 所有 IM 相关方法、监听器都由此调用和设置。
 */
public final class RongIMClient {
    private static RongIMClient client = null;
    private static String appKey;
    private static String resourcePath;
    private static Context sContext = null;
    private static RongWakeLock mWakeLock;
    private static NativeObject nativeObj;
    private String token;
    private String currentUserId;
private UserInfo mUserInfo = new UserInfo();//=====================登录用户自己的信息=============================================================
private static HashMap<String, Constructor<? extends MessageContent>> constructorMap =
					new HashMap<String, Constructor<? extends MessageContent>>();//======value为消息类型名如：RC:TxtMsg=======value为消息类型的构造方法=========
    private static ConnectionStatusListener mListener;
    private static boolean isConnecting = false;

    private RongIMClient() {}
    
    protected static NativeObject getLastNativeInstance() {
        if (client == null)
            return null;
        return nativeObj;
    }

    protected static RongIMClient getLastClientInstance() {
        return client;
    }

    /**
     * 会话类型枚举。
     */
    public static enum ConversationType {
        /**
         * 私聊。
         */
        PRIVATE(1, "private"),

        /**
         * 讨论组。
         */
        DISCUSSION(2, "discussion"),

        /**
         * 群组。
         */
        GROUP(3, "group"),

        /**
         * 聊天室。
         */
        CHATROOM(4, "chatroom"),

        /**
         * 客服
         */
        CUSTOMERSERVICE(5, "customerservice");

        private int value = 1;
        private String name = "";

        /**
         * 构造函数。
         *
         * @param value 会话类型的值。
         */
        ConversationType(int value, String name) {
            this.value = value;
            this.name = name;
        }

        /**
         * 获取会话类型的值。
         *
         * @return 会话类型的值。
         */
        public int getValue() {
            return this.value;
        }

        /**
         * 获取会话类型名称。
         *
         * @return 会话类型名称。
         */
        public String getName() {
            return this.name;
        }

        /**
         * 设置会话类型。
         *
         * @param code 会话类型的值。
         * @return 会话类型枚举。
         */
        public static ConversationType setValue(int code) {
            for (ConversationType c : ConversationType.values()) {
                if (code == c.getValue()) {
                    return c;
                }
            }
            return PRIVATE;
        }
    }

    /**
     * 媒体文件类型枚举。
     */
    public static enum MediaType {
        /**
         * 图片。
         */
        IMAGE(1),

        /**
         * 声音。
         */
        AUDIO(2),

        /**
         * 视频。
         */
        VIDEO(3),

        /**
         * 通用文件。
         */
        FILE(100);


        private int value = 1;

        /**
         * 构造函数。
         *
         * @param value 媒体文件类型的值。
         */
        MediaType(int value) {
            this.value = value;
        }

        /**
         * 获取媒体文件类型的值。
         *
         * @return 媒体文件类型的值。
         */
        public int getValue() {
            return this.value;
        }

        /**
         * 设置媒体文件类型。
         *
         * @param code 媒体文件类型的值。
         * @return 媒体文件类型枚举。
         */
        public static MediaType setValue(int code) {
            for (MediaType c : MediaType.values()) {
                if (code == c.getValue()) {
                    return c;
                }
            }
            return IMAGE;
        }
    }

    /**
     * 消息方向枚举。
     */
    public static enum MessageDirection {
        /**
         * 发送消息。
         */
        SEND(1),

        /**
         * 接收消息。
         */
        RECEIVE(2);

        private int value = 1;

        /**
         * 构造函数。
         *
         * @param value 消息方向的值。
         */
        MessageDirection(int value) {
            this.value = value;
        }

        /**
         * 获取消息方向的值。
         *
         * @return 消息方向的值。
         */
        public int getValue() {
            return this.value;
        }

        /**
         * 设置消息方向。
         *
         * @param code 消息方向的值。
         * @return 消息方向枚举。
         */
        public static MessageDirection setValue(int code) {
            for (MessageDirection c : MessageDirection.values()) {
                if (code == c.getValue()) {
                    return c;
                }
            }
            return SEND;
        }
    }

    /**
     * 发送出的消息的状态。
     */
    public static enum SentStatus {

        /**
         * 发送中。
         */
        SENDING(10),

        /**
         * 发送失败。
         */
        FAILED(20),

        /**
         * 已发送。
         */
        SENT(30),

        /**
         * 对方已接收。
         */
        RECEIVED(40),

        /**
         * 对方已读。
         */
        READ(50),

        /**
         * 对方已销毁。
         */
        DESTROYED(60);

        private int value = 1;

        /**
         * 构造函数。
         *
         * @param value 消息发送状态的值。
         */
        SentStatus(int value) {
            this.value = value;
        }

        /**
         * 获取消息发送状态的值。
         *
         * @return 消息发送状态的值。
         */
        public int getValue() {
            return this.value;
        }

        /**
         * 设置消息发送状态。
         *
         * @param code 消息发送状态的值。
         * @return 消息发送状态枚举。
         */
        public static SentStatus setValue(int code) {
            for (SentStatus c : SentStatus.values()) {
                if (code == c.getValue()) {
                    return c;
                }
            }
            return SENDING;
        }
    }


    /**
     * 接收到的消息的状态。
     * <p/>
     * 是一个按位标记的枚举，可以进行位运算。
     */
    public static class ReceivedStatus {
        private static final int READ = 0x1;
        private static final int LISTENED = 0x2;
        private static final int DOWNLOAD = 0x4;
        private int flag = 0;
        private boolean isRead = false;
        private boolean isListened = false;
        private boolean isDownload = false;
        private Long messageId = 0L;

        /**
         * 构造函数。
         *
         * @param flag      状态标识。
         * @param messageId 消息。
         */
        public ReceivedStatus(int flag, long messageId) {
            this.flag = flag;
            isRead = (flag & READ) == READ;
            isListened = (flag & LISTENED) == LISTENED;
            isDownload = (flag & DOWNLOAD) == DOWNLOAD;
            this.messageId = messageId;
        }

        /**
         * 获取状态标识。
         *
         * @return 状态标识。
         */
        public int getFlag() {
            return flag;
        }

        /**
         * 获取是否已读取的状态。
         *
         * @return 是否已读取的状态。
         */
        public boolean isRead() {
            return isRead;
        }

        /**
         * 获取是否已被收听的状态。
         *
         * @return 是否已被收听的状态。
         */
        public boolean isListened() {
            return isListened;
        }

        /**
         * 设置是否已被收听的状态。
         */
        public void setListened() {
            this.flag = (this.flag | LISTENED);
            this.isListened = true;
            nativeObj.SetReadStatus(messageId, flag);
        }

        /**
         * 获取媒体文件是否已经下载的状态。
         *
         * @return 媒体文件是否已经下载的状态。
         */
        public boolean isDownload() {
            return isDownload;
        }

        /**
         * 设置媒体文件是否已经下载的状态。
         */
        public void setDownload() {
            this.flag = (this.flag | DOWNLOAD);
            this.isDownload = true;
            nativeObj.SetReadStatus(messageId, flag);
        }
    }

    /**
     * 用户信息实体类，用来容纳和存储用户信息。
     */
    public static class UserInfo implements Parcelable {
        private String userId;
        private String name;
        private String portraitUri;

        /**
         * 默认构造函数。
         */
        public UserInfo() {
        }

        public UserInfo(Parcel in) {
            setUserId(in.readString());
            setName(in.readString());
            setPortraitUri(in.readString());
        }

        /**
         * 构造函数。
         *
         * @param userId      用户 Id。
         * @param name        用户名称（昵称）。
         * @param portraitUri 用户头像。
         */
        public UserInfo(String userId, String name, String portraitUri) {
            this.userId = userId;
            this.name = name;
            this.portraitUri = portraitUri;
        }

        /**
         * 获取用户 Id。
         *
         * @return 用户 Id。
         */
        public String getUserId() {
            return userId;
        }

        /**
         * 设置用户 Id。
         *
         * @param userId 用户 Id。
         */
        public void setUserId(String userId) {
            this.userId = userId;
        }

        /**
         * 获取用户名称（昵称）。
         *
         * @return 名称（昵称）。
         */
        public String getName() {
            return name;
        }

        /**
         * 设置名称（昵称）。
         *
         * @param name 名称（昵称）。
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * 获取用户头像。
         *
         * @return 用户头像地址。
         */
        public String getPortraitUri() {
            return portraitUri;
        }

        /**
         * 设置用户头像。
         *
         * @param portraitUri 用户头像地址。
         */
        public void setPortraitUri(String portraitUri) {
            this.portraitUri = portraitUri;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

            if (userId != null) {
                dest.writeString(userId);
            } else {
                dest.writeString("");
            }

            if (name != null) {
                dest.writeString(name);
            } else {
                dest.writeString("");
            }

            if (portraitUri != null) {
                dest.writeString(portraitUri);
            } else {
                dest.writeString("");
            }
        }

        public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {

            @Override
            public UserInfo createFromParcel(Parcel source) {
                return new UserInfo(source);
            }

            @Override
            public UserInfo[] newArray(int size) {
                return new UserInfo[size];
            }
        };
    }

    /**
     * 讨论组实体，用来容纳和存储讨论组的信息和设置。
     */
    public static class Discussion {
        private String id;
        private String name;
        private String creatorId;
        private boolean isOpen = true;
        private List<String> memberIdList;

        /**
         * 默认构造函数。
         */
        protected Discussion(NativeObject.DiscussionInfo info) {
            this.id = info.getDiscussionId();
            this.name = info.getDiscussionName();
            this.creatorId = info.getAdminId();
            this.memberIdList = Arrays.asList(info.getUserIds().split("\n"));
            Log.d("Discussion","info.getInviteStatus():"+info.getInviteStatus());
            this.isOpen = info.getInviteStatus() == 0 ? true : false;
        }


        public boolean isOpen() {
            return isOpen;
        }

        public void setOpen(boolean isOpen) {
            this.isOpen = isOpen;
        }

        /**
         * 获取讨论组名称。
         *
         * @return 讨论组名称。
         */
        public String getName() {
            return name;
        }

        /**
         * 设置讨论组名称。
         *
         * @param name 讨论组名称。
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * 获取讨论组 Id。
         *
         * @return 讨论组 Id。
         */
        public String getId() {
            return id;
        }

        /**
         * 设置讨论组 Id。
         *
         * @param id 讨论组 Id。
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * 获取讨论组创建者 Id。
         *
         * @return 讨论组创建者 Id。
         */
        public String getCreatorId() {
            return creatorId;
        }

        /**
         * 设置讨论组创建者 Id。
         *
         * @param creatorId 讨论组创建者 Id。
         */
        public void setCreatorId(String creatorId) {
            this.creatorId = creatorId;
        }

        /**
         * 获取讨论组成员 Id 列表。
         *
         * @return 讨论组成员 Id 列表。
         */
        public List<String> getMemberIdList() {
            return memberIdList;
        }

        /**
         * 设置 讨论组成员 Id 列表。
         *
         * @param memberIdList 讨论组成员 Id 列表。
         */
        public void setMemberIdList(List<String> memberIdList) {
            this.memberIdList = memberIdList;
        }
    }

    /**
     * 消息实体，用来容纳和存储客户端收到的消息信息，对应聊天界面中的消息。
     */
    public static class Message {
        private ConversationType conversationType;
        private String targetId;
        private long messageId;
        private MessageDirection messageDirection;
        private String senderUserId;
        private ReceivedStatus receivedStatus;
        private SentStatus sentStatus;
        private long receivedTime;
        private long sentTime;
        private String objectName;
        private MessageContent content;
        private String extra;

        /**
         * 默认构造函数。
         */
        public Message() {
        }

        protected Message(NativeObject.Message msg) {
            conversationType = ConversationType.setValue(msg.getConversationType());
            targetId = msg.getTargetId();
            messageId = msg.getMessageId();
            messageDirection = !msg.getMessageDirection() ? MessageDirection.SEND : MessageDirection.RECEIVE;
            senderUserId = msg.getSenderUserId();
            receivedStatus = new ReceivedStatus(msg.getReadStatus(), messageId);
            sentStatus = SentStatus.setValue(msg.getSentStatus());
            receivedTime = msg.getReceivedTime();
            sentTime = msg.getSentTime();
            objectName = msg.getObjectName();
            content = getMessageContent(objectName, msg.getContent());

            if (content != null) {
                content.conversationType = conversationType;
                content.targetId = targetId;
            }

            extra = msg.getExtra();
        }

        /**
         * 获取会话类型。
         *
         * @return 会话类型。
         */
        public ConversationType getConversationType() {
            return conversationType;
        }

        /**
         * 设置会话类型。
         *
         * @param conversationType 会话类型。
         */
        public void setConversationType(ConversationType conversationType) {
            this.conversationType = conversationType;
        }

        /**
         * 获取目标 Id。
         * <p/>
         * 根据不同的 conversationType，可能是聊天 Id、讨论组 Id、群组 Id 或聊天室 Id。
         *
         * @return 目标 Id 的值。
         */
        public String getTargetId() {
            return targetId;
        }

        /**
         * 设置目标 Id。
         * <p/>
         * 根据不同的 conversationType，可能是聊天 Id、讨论组 Id、群组 Id 或聊天室 Id。
         *
         * @param targetId 目标 Id 的值。
         * @see io.rong.imlib.RongIMClient.ConversationType
         */
        public void setTargetId(String targetId) {
            this.targetId = targetId;
        }

        /**
         * 获取消息 Id。
         *
         * @return 消息 Id。
         */
        public long getMessageId() {
            return messageId;
        }

        /**
         * 设置消息 Id。
         *
         * @param messageId 消息 Id。
         */
        public void setMessageId(long messageId) {
            this.messageId = messageId;
        }

        /**
         * 获取消息方向。
         *
         * @return 消息方向。
         */
        public MessageDirection getMessageDirection() {
            return messageDirection;
        }

        /**
         * 设置消息方向。
         *
         * @param messageDirection 消息方向。
         */
        public void setMessageDirection(MessageDirection messageDirection) {
            this.messageDirection = messageDirection;
        }

        /**
         * 获取接收到的消息的状态。
         *
         * @return 接收到的消息的状态。
         */
        public ReceivedStatus getReceivedStatus() {
            return receivedStatus;
        }

        /**
         * 设置接收到的消息的状态。
         *
         * @param receivedStatus 接收到的消息的状态。
         */
        public void setReceivedStatus(ReceivedStatus receivedStatus) {
            this.receivedStatus = receivedStatus;
        }

        /**
         * 获取发送出的消息的状态。
         *
         * @return 发送出的消息的状态。
         */
        public SentStatus getSentStatus() {
            return sentStatus;
        }

        /**
         * 设置发送出的消息的状态。
         *
         * @param sentStatus 发送出的消息的状态。
         */
        public void setSentStatus(SentStatus sentStatus) {
            this.sentStatus = sentStatus;
        }

        /**
         * 获取消息接收时间。
         * <p/>
         * 接收时间为消息到达接收客户端时客户端的本地时间。
         *
         * @return 消息接收时间。
         */
        public long getReceivedTime() {
            return receivedTime;
        }

        /**
         * 设置消息接收时间。
         * <p/>
         * 接收时间为消息到达接收客户端时客户端的本地时间。
         *
         * @param receivedTime 消息接收时间。
         */
        public void setReceivedTime(long receivedTime) {
            this.receivedTime = receivedTime;
        }

        /**
         * 获取消息发送时间。
         * <p/>
         * 发送时间为消息从发送客户端到达服务器时服务器的本地时间。
         *
         * @return 消息发送时间。
         */
        public long getSentTime() {
            return sentTime;
        }

        /**
         * 设置消息发送时间。
         * <p/>
         * 发送时间为消息从发送客户端到达服务器时服务器的本地时间。
         *
         * @param sentTime 消息发送时间。
         */
        public void setSentTime(long sentTime) {
            this.sentTime = sentTime;
        }

        /**
         * 获取消息对象名称。
         * <p/>
         * 消息对象名称即 {@link io.rong.imlib.MessageTag } 注解中的 value 值。
         *
         * @return 消息对象名称。
         * @see io.rong.imlib.MessageTag#value()
         */
        public String getObjectName() {
            return objectName;
        }

        /**
         * 设置消息对象名称。
         * <p/>
         * 消息对象名称即 {@link io.rong.imlib.MessageTag } 注解中的 value 值。
         *
         * @param objectName 消息对象名称。
         * @see io.rong.imlib.MessageTag#value()
         */
        public void setObjectName(String objectName) {
            this.objectName = objectName;
        }

        /**
         * 获取消息内容。
         *
         * @return 消息内容。
         */
        public MessageContent getContent() {
            return content;
        }

        /**
         * 设置消息内容。
         *
         * @param content 消息内容。
         */
        public void setContent(MessageContent content) {
            this.content = content;
        }

        /**
         * 获取消息的附加信息。
         *
         * @return 消息的附加信息。
         */
        public String getExtra() {
            return extra;
        }

        /**
         * 设置消息的附加信息。
         *
         * @param extra 消息的附加信息。
         */
        public void setExtra(String extra) {
            this.extra = extra;
        }

        /**
         * 获取发送消息的用户 Id。
         *
         * @return 发送消息的用户 Id。
         */
        public String getSenderUserId() {
            return senderUserId;
        }

        /**
         * 设置发送消息的用户 Id。
         *
         * @param senderUserId 发送消息的用户 Id。
         */
        public void setSenderUserId(String senderUserId) {
            this.senderUserId = senderUserId;
        }
    }

    /**
     * 会话的实体，用来容纳和存储客户端的会话信息，对应会话列表中的会话。
     */
    public static class Conversation {
        /**
         * 默认构造函数。
         */
        public Conversation() {
        }

        protected Conversation(JSONObject jsonObj) {
            targetId = jsonObj.optString("target_id");
            latestMessageId = jsonObj.optLong("last_message_id");
            conversationTitle = jsonObj.optString("conversation_title");
            unreadMessageCount = jsonObj.optInt("unread_count");
            conversationType = ConversationType.setValue(jsonObj.optInt("conversation_category"));
            isTop = jsonObj.optInt("is_top") == 1;
            objectName = jsonObj.optString("object_name");

            if (latestMessageId == 0 || latestMessageId == -1) {//最后一条消息为null
                latestMessage = null;
            } else {
                latestMessage = getMessageContent(objectName, jsonObj.optString("content").getBytes());
            }
            receivedStatus = new ReceivedStatus(jsonObj.optInt("read_status"), latestMessageId);
            receivedTime = jsonObj.optLong("receive_time");
            sentTime = jsonObj.optLong("send_time");
            sentStatus = SentStatus.setValue(jsonObj.optInt("send_status"));
            senderUserId = jsonObj.optString("sender_user_id");
            senderUserName = jsonObj.optString("sender_user_name");
            draft = jsonObj.optString("draft_message");

        }

        private ConversationType conversationType;
        private String targetId;
        private String conversationTitle;
        private int unreadMessageCount;
        private boolean isTop;
        private ReceivedStatus receivedStatus;
        private SentStatus sentStatus;
        private long receivedTime;
        private long sentTime;
        private String objectName;
        private String senderUserId;
        private String senderUserName;
        private long latestMessageId;
        private MessageContent latestMessage;
        private String draft;

        /**
         * 获取会话类型。
         * @return 会话类型。
         */
        public ConversationType getConversationType() {
            return conversationType;
        }
        /**
         * 设置会话类型。
         *
         * @param conversationType 会话类型。
         */
        public void setConversationType(ConversationType conversationType) {
            this.conversationType = conversationType;
        }
        /**
         * 获取目标 Id。
         * <p/>
         * 根据不同的 conversationType，可能是聊天 Id、讨论组 Id、群组 Id 或聊天室 Id。
         *
         * @return 目标 Id。
         */
        public String getTargetId() {
            return targetId;
        }
        /**
         * 设置目标 Id。
         * <p/>
         * 根据不同的 conversationType，可能是聊天 Id、讨论组 Id、群组 Id 或聊天室 Id。
         *
         * @param targetId 目标 Id。
         */
        public void setTargetId(String targetId) {
            this.targetId = targetId;
        }
        /**
         * 获取会话标题。
         *
         * @return 会话标题。
         */
        public String getConversationTitle() {
            return conversationTitle;
        }
        /**
         * 设置会话标题。
         *
         * @param conversationTitle 会话标题。
         */
        public void setConversationTitle(String conversationTitle) {
            this.conversationTitle = conversationTitle;
        }
        /**
         * 获取未读消息数。
         * @return 未读消息数。
         */
        public int getUnreadMessageCount() {
            return unreadMessageCount;
        }
        /**
         * 设置未读消息数。
         * @param unreadMessageCount 未读消息数。
         */
        public void setUnreadMessageCount(int unreadMessageCount) {
            this.unreadMessageCount = unreadMessageCount;
        }
        /**
         * 获取置顶状态。
         * @return 置顶状态。
         */
        public boolean isTop() {
            return isTop;
        }
        /**
         * 设置置顶状态。
         * @param isTop 置顶状态。
         */
        public void setTop(boolean isTop) {
            this.isTop = isTop;
        }
        /**
         * 获取接收到的消息的状态。
         * @return 接收到的消息的状态。
         */
        public ReceivedStatus getReceivedStatus() {
            return receivedStatus;
        }
        /**
         * 设置接收到的消息的状态。
         * @param receivedStatus 接收到的消息的状态。
         */
        public void setReceivedStatus(ReceivedStatus receivedStatus) {
            this.receivedStatus = receivedStatus;
        }
        /**
         * 获取发送出的消息的状态。
         * @return 发送出的消息的状态。
         */
        public SentStatus getSentStatus() {
            return sentStatus;
        }
        /**
         * 设置发送出的消息的状态。
         *
         * @param sentStatus 发送出的消息的状态。
         */
        public void setSentStatus(SentStatus sentStatus) {
            this.sentStatus = sentStatus;
        }
        /**
         * 获取消息接收时间。
         * @return 消息接收时间。
         */
        public long getReceivedTime() {
            return receivedTime;
        }
        /**
         * 设置消息接收时间。
         * @param receivedTime 消息接收时间。
         */
        public void setReceivedTime(long receivedTime) {
            this.receivedTime = receivedTime;
        }
        /**
         * 获取最后消息发送时间
         * @return 。
         */
        public long getSentTime() {
            return sentTime;
        }
        /**
         * 设置最后消息发送时间
         * @param sentTime 。
         */
        public void setSentTime(long sentTime) {
            this.sentTime = sentTime;
        }
        /**
         * 获取文字消息草稿。
         * @return 文字消息草稿。
         */
        public String getDraft() {
            return draft;
        }
        /**
         * 设置文字消息草稿。
         * @param draft 文字消息草稿。
         */
        public void setDraft(String draft) {
            this.draft = draft;
        }
        /**
         * 获取消息对象名称。
         * <p/>
         * 消息对象名称即 {@link io.rong.imlib.MessageTag } 注解中的 value 值。
         * @return 消息对象名称。
         * @see io.rong.imlib.MessageTag#value()
         */
        public String getObjectName() {
            return objectName;
        }
        /**
         * 设置消息对象名称。
         * <p/>
         * 消息对象名称即 {@link io.rong.imlib.MessageTag } 注解中的 value 值。
         * @param objectName 消息对象名称。
         * @see io.rong.imlib.MessageTag#value()
         */
        public void setObjectName(String objectName) {
            this.objectName = objectName;
        }
        /**
         * 获取本会话最后一条消息 Id。
         * @return 本会话最后一条消息 Id。
         */
        public long getLatestMessageId() {
            return latestMessageId;
        }
        /**
         * 设置本会话最后一条消息 Id。
         * @param latestMessageId 本会话最后一条消息 Id。
         */
        public void setLatestMessageId(long latestMessageId) {
            this.latestMessageId = latestMessageId;
        }
        /**
         * 获取本会话最后一条消息。
         * @return 消息内容。
         */
        public MessageContent getLatestMessage() {
            return latestMessage;
        }
        /**
         * 设置本会话最后一条消息。
         * @param latestMessage 消息内容。
         */
        public void setLatestMessage(MessageContent latestMessage) {
            this.latestMessage = latestMessage;
        }
        /**
         * 获取发送消息的用户 Id。
         *
         * @return 发送消息的用户 Id。
         */
        public String getSenderUserId() {
            return senderUserId;
        }
        /**
         * 设置发送消息的用户 Id。
         *
         * @param senderUserId 发送消息的用户 Id。
         */
        public void setSenderUserId(String senderUserId) {
            this.senderUserId = senderUserId;
        }
        /**
         * 获取发送消息的用户名称。
         *
         * @return 发送消息的用户名称。
         */
        public String getSenderUserName() {
            return senderUserName;
        }
        /**
         * 设置发送消息的用户名称。
         * @param senderUserName 发送消息的用户名称。
         */
        public void setSenderUserName(String senderUserName) {
            this.senderUserName = senderUserName;
        }
    }

    public static class UnknowMessage extends MessageContent {

        public UnknowMessage(byte[] bytes) {

        }

        public UnknowMessage() {

        }

        @Override
        public byte[] encode() {
            return new byte[0];
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }
    }

    /**
     * 语音消息，会存入消息历史记录。
     */
    @MessageTag(value = "RC:VcMsg", flag = MessageTag.ISCOUNTED | MessageTag.ISPERSISTED)
    public static class VoiceMessage extends MessageContent {

        private Uri uri;

        private int duration;

        /**
         * 将本地消息对象序列化为消息数据。
         */
        @Override
        public byte[] encode() {

            byte[] voiceData = FileUtil.getByteFromUri(uri);

            if (voiceData == null) {
                Log.d("publishVoide--", "voiceData is null");
                return null;
            }

            String voiceStr = Base64.encodeToString(voiceData, Base64.NO_WRAP);
            JSONObject jsonObj = new JSONObject();

            try {
                jsonObj.put("content", voiceStr);
                jsonObj.put("duration", duration);
            } catch (JSONException e) {
                Log.d("JSONException", e.getMessage());
            }

            return jsonObj.toString().getBytes();
        }

        protected VoiceMessage() {

        }

        protected VoiceMessage(byte[] data) {
            String jsonStr = new String(data);

            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                setDuration(jsonObj.getInt("duration"));
                String uniqueId = UUID.randomUUID().toString();
                // String path =
                // String.format("%1$s/%2$s/cache/%3$s/%4$s/audio/%5$s.amr",
                // RongIMClient.resourcePath, client.currentUserId,
                // this.conversationType.getName(), this.targetId,
                // uniqueId);
                String path = String.format("%1$s/%2$s/cache/%3$s/audio/%4$s.amr", RongIMClient.resourcePath, client.currentUserId, this.conversationType.getName(), uniqueId);
                Log.d("audioPath", path);
                Uri uri = Uri.parse(path);
                byte[] audio = Base64.decode(jsonObj.getString("content"), Base64.NO_WRAP);
                FileUtil.writeByte(uri, audio);
                this.uri = uri;

            } catch (JSONException e) {
                Log.d("JSONException", e.getMessage());
            }
        }

        /**
         * 构造函数。
         *
         * @param uri      音频文件的 Uri。
         * @param duration 音频片段时长，单位为秒。
         */
        public VoiceMessage(Uri uri, int duration) {
            this.uri = uri;
            this.duration = duration;
        }

        /**
         * 获取音频文件的 Uri。
         *
         * @return 音频文件的 Uri。
         */
        public Uri getUri() {
            return uri;
        }

        /**
         * 设置音频文件的 Uri。
         *
         * @param uri 音频文件的 Uri。
         */
        public void setUri(Uri uri) {
            this.uri = uri;
        }

        /**
         * 获取音频片段的时长。
         *
         * @return 音频片段的时长。
         */
        public int getDuration() {
            return duration;
        }

        /**
         * 设置音频片段的时长。
         *
         * @param duration 音频片段的时长。
         */
        public void setDuration(int duration) {
            this.duration = duration;
        }

        /**
         * 描述了包含在 Parcelable 对象排列信息中的特殊对象的类型。
         *
         * @return 一个标志位，表明Parcelable对象特殊对象类型集合的排列。
         */
        @Override
        public int describeContents() {
            return 0;
        }

        /**
         * 将类的数据写入外部提供的 Parcel 中。
         *
         * @param dest  对象被写入的 Parcel。
         * @param flags 对象如何被写入的附加标志，可能是 0 或 PARCELABLE_WRITE_RETURN_VALUE。
         */
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            if (uri == null || uri.getPath() == null) {
                dest.writeString("");
            } else {
                dest.writeString(uri.getPath());
            }

            dest.writeInt(duration);
        }

        /**
         * 构造函数。
         *
         * @param in 初始化传入的 Parcel。
         */
        public VoiceMessage(Parcel in) {
            uri = Uri.parse(in.readString());
            duration = in.readInt();
        }

        /**
         * 读取接口，目的是要从Parcel中构造一个实现了Parcelable的类的实例处理。
         */
        public static final Creator<VoiceMessage> CREATOR = new Creator<VoiceMessage>() {

            @Override
            public VoiceMessage createFromParcel(Parcel source) {
                return new VoiceMessage(source);
            }

            @Override
            public VoiceMessage[] newArray(int size) {
                return new VoiceMessage[size];
            }
        };
    }
  
    /**
     * 位置消息，定义了经纬度和位置缩略图片，会存入消息历史记录。
     */
    @MessageTag(value = "RC:LocMsg", flag = MessageTag.ISCOUNTED | MessageTag.ISPERSISTED)
    private static class LocationMessage extends MessageContent {
        private float longitude;
        private float latitude;

        /**
         * 将本地消息对象序列化成为消息数据。
         */
        @Override
        public byte[] encode() {
            return null;
        }

        protected LocationMessage() {

        }

        protected LocationMessage(byte[] data) {

        }

        /**
         * 构造函数。
         *
         * @param longitude    经度。
         * @param latitude     纬度。
         * @param mapThumbnail 地图的缩略图。
         */
        public LocationMessage(float longitude, float latitude, Bitmap mapThumbnail) {

        }

        /**
         * 获取经度值。
         *
         * @return 经度值。
         */
        public float getLongitude() {
            return longitude;
        }

        /**
         * 设置经度值。
         *
         * @param longitude 经度值。
         */
        public void setLongitude(float longitude) {
            this.longitude = longitude;
        }

        /**
         * 获取纬度值。
         *
         * @return 纬度值。
         */
        public float getLatitude() {
            return latitude;
        }

        /**
         * 设置纬度值。
         *
         * @param latitude 纬度值。
         */
        public void setLatitude(float latitude) {
            this.latitude = latitude;
        }

        /**
         * TODO: 统一说法是？
         *
         * @return
         */
        @Override
        public int describeContents() {
            return 0;
        }

        /**
         * TODO: 统一说法是？
         *
         * @param dest
         * @param flags
         */
        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }
    }

    /**
     * 表情贴纸消息，定义了表情图片，会存入消息历史记录。
     */
    @MessageTag("RC:StkMsg")
    private static class StickerMessage extends MessageContent {
        @Override
        public byte[] encode() {
            return null;
        }

        public StickerMessage() {

        }

        public StickerMessage(byte[] data) {

        }

        public StickerMessage(String stickerCategory, String key, Bitmap stickerImage) {

        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }
    }

    /**
     * 通知消息的抽象基类，用来表示聊天中的各种通知，某些消息会存入消息历史记录，某些不会。
     */
    private static abstract class NotificationMessage extends MessageContent {
    }

    /**
     * 通知消息，表示消息已经被收到，不会存入消息历史记录。
     */
    @MessageTag("RC:RecNtf")
    private static class HasReceivedNotificationMessage extends NotificationMessage {

        /**
         * 获取消息已收到的状态。
         *
         * @return 消息已收到的状态
         */
        public boolean isHasReceived() {
            return hasReceived;
        }

        /**
         * 设置消息已收到的状态。
         *
         * @param hasReceived 消息已收到的状态
         */
        public void setHasReceived(boolean hasReceived) {
            this.hasReceived = hasReceived;
        }

        private boolean hasReceived;

        /**
         * TODO: 统一说法是？
         *
         * @return
         */
        @Override
        public byte[] encode() {
            return new byte[0];
        }

        /**
         * TODO: 统一说法是？
         *
         * @return
         */
        @Override
        public int describeContents() {
            return 0;
        }

        /**
         * TODO: 统一说法是？
         *
         * @param dest
         * @param flags
         */
        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }
    }

    /**
     * 通知消息，表示消息已经被阅读，不会存入消息历史记录。
     */
    @MessageTag("RC:ReadNtf")
    private static class HasReadNotificationMessage extends NotificationMessage {
        /**
         * 获取消息已读的状态。
         *
         * @return 消息已读的状态。
         */
        public boolean hasRead() {
            return hasRead;
        }

        /**
         * 设置消息已读的状态。
         *
         * @param hasRead 消息已读的状态。
         */
        public void hasRead(boolean hasRead) {
            this.hasRead = hasRead;
        }

        private boolean hasRead;

        /**
         * TODO: 统一说法是？
         *
         * @return
         */
        @Override
        public byte[] encode() {
            return new byte[0];
        }

        /**
         * TODO: 统一说法是？
         *
         * @return
         */
        @Override
        public int describeContents() {
            return 0;
        }

        /**
         * TODO: 统一说法是？
         *
         * @param dest
         * @param flags
         */
        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }
    }

    /**
     * 状态消息的抽象基类，表示某种状态，不会存入消息历史记录。
     * <p/>
     * 此类消息不保证一定到达接收方（但只是理论上存在丢失的可能），但是速度最快，所以通常用来传递状态信息。
     */
    private static abstract class StatusMessage extends MessageContent {
    }

    /**
     * 状态消息，表示对方正在输入，不会存入消息历史记录。
     */
    @MessageTag("RC:TypNtf")
    private static class IsTypingStatusMessage extends StatusMessage {
        /**
         * 获取正在输入的状态。
         *
         * @return 正在输入的状态。
         */
        public boolean isTyping() {
            return isTyping;
        }

        /**
         * 设置正在输入的状态。
         *
         * @param isTyping 正在输入的状态。
         */
        public void setTyping(boolean isTyping) {
            this.isTyping = isTyping;
        }

        private boolean isTyping;

        /**
         * TODO: 统一说法是？
         *
         * @return
         */
        @Override
        public byte[] encode() {
            return new byte[0];
        }

        /**
         * TODO: 统一说法是？
         *
         * @return
         */
        @Override
        public int describeContents() {

            return 0;
        }

        /**
         * TODO: 统一说法是？
         *
         * @param dest
         * @param flags
         */
        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }
    }

    /**
     * 连接服务器的回调。
     */
    public static interface ConnectCallback {
        /**
         * 连接错误代码枚举。
         */
        public static enum ErrorCode {
            /**
             * 未知错误。
             */
            UNKNOWN(-1, "Unknown error."),

            /**
             * 数据包不完整。 请求数据包有缺失。
             */
            PACKAGE_BROKEN(2002, "Package is broken."),

            /**
             * 服务器不可用。
             */
            SERVER_UNAVAILABLE(2003, "Server is unavailable."),

            /**
             * 错误的令牌（Token），Token 解析失败，请重新向身份认证服务器获取 Token。
             */
            TOKEN_INCORRECT(2004, "Token is incorrect."),

            /**
             * App Key 不可用。
             * <p/>
             * 可能是错误的 App Key，或者 App Key 被服务器积极拒绝。
             */
            APP_KEY_UNAVAILABLE(2005, "App key is unavailable."),

            /**
             * 数据库错误。
             */
            DATABASE_ERROR(2006, "Database is error"),

            /**
             * 服务器超时。
             */
            TIMEOUT(5004, "Server is timed out.");

            private int code;
            private String msg;

            /**
             * 构造函数。
             *
             * @param code 错误代码。
             * @param msg  错误消息。
             */
            ErrorCode(int code, String msg) {
                this.code = code;
                this.msg = msg;
            }

            /**
             * 获取错误代码值。
             *
             * @return 错误代码值。
             */
            public int getValue() {
                return this.code;
            }

            /**
             * 获取错误消息。
             *
             * @return 错误消息。
             */
            public String getMessage() {
                return this.msg;
            }

            /**
             * 设置错误代码值。
             *
             * @param code 错误代码。
             * @return 错误代码枚举。
             */
            public static ErrorCode setValue(int code) {
                for (ErrorCode c : ErrorCode.values()) {
                    if (code == c.getValue()) {
                        return c;
                    }
                }

                return UNKNOWN;
            }
        }

        /**
         * 回调成功。
         *
         * @param userId 当前登录的用户 Id，既换取登录 Token 时，App 服务器传递给融云服务器的用户 Id。
         */
        void onSuccess(String userId);

        /**
         * 回调出错。
         *
         * @param errorCode 连接错误代码。
         */
        void onError(ErrorCode errorCode);
    }

    public static interface ConnectionStatusListener {
        public static enum ConnectionStatus {
            /**
             * 未知错误。
             */
            UNKNOWN(-1, "Unknown error."),

            /**
             * 网络不可用。
             */
            NETWORK_UNAVAILABLE(1, "Network is unavailable."),

            /**
             * 设备处于飞行模式。
             */
            AIRPLANE_MODE(2, "Switch to airplane mode."),

            /**
             * 设备处于 2G（GPRS、EDGE）低速网络下。
             */
            Cellular_2G(3, "Switch to 2G cellular network."),

            /**
             * 设备处于 3G 或 4G（LTE）高速网络下。
             */
            Cellular_3G_4G(4, "Switch to 3G or 4G cellular network."),

            /**
             * 设备网络切换到 WIFI 网络。
             */
            WIFI(5, "Switch to WIFI network."),

            /**
             * 用户账户在其他设备登录，本机会被踢掉线。
             */
            KICKED_OFFLINE_BY_OTHER_CLIENT(6, "Login on the other device, and be kicked offline."),

            /**
             * 用户账户在 Web 端登录。
             */
            LOGIN_ON_WEB(7, "Login on web client."),

            /**
             * 服务器异常或无法连接。
             */
            SERVER_INVALID(8, "Protocol version invalid"),

            /**
             * 验证异常(可能由于user验证、版本验证、auth验证)。
             */
            VALIDATE_INVALID(9, "validate invalid");

            private int code;
            private String msg;

            /**
             * 构造函数。
             *
             * @param code 状态代码。
             * @param msg  状态消息。
             */
            ConnectionStatus(int code, String msg) {
                this.code = code;
                this.msg = msg;
            }

            /**
             * 获取状态代码值。
             *
             * @return 状态代码值。
             */
            public int getValue() {
                return this.code;
            }

            /**
             * 获取状态消息。
             *
             * @return 状态消息。
             */
            public String getMessage() {
                return this.msg;
            }

            /**
             * 设置状态代码值。
             *
             * @param code 状态代码。
             * @return 状态代码枚举。
             */
            public static ConnectionStatus setValue(int code) {
                for (ConnectionStatus c : ConnectionStatus.values()) {
                    if (code == c.getValue()) {
                        return c;
                    }
                }

                return UNKNOWN;
            }

        }

        /**
         * 网络状态变化。
         *
         * @param status 网络状态。
         */
        void onChanged(ConnectionStatus status);
    }

    /**
     * 执行操作的回调。
     */
    public static interface OperationCallback {
        /**
         * 执行操作错误代码枚举。
         */
        public static enum ErrorCode {
            /**
             * 未知错误。
             */
            UNKNOWN(-1, "Unknown error."),

            /**
             * 服务器超时。
             */
            TIMEOUT(5004, "Server is timed out.");

            private int code;
            private String msg;

            /**
             * 构造函数。
             *
             * @param code 错误代码。
             * @param msg  错误消息。
             */
            ErrorCode(int code, String msg) {
                this.code = code;
                this.msg = msg;
            }

            /**
             * 获取错误代码值。
             *
             * @return 错误代码值。
             */
            public int getValue() {
                return this.code;
            }

            /**
             * 获取错误消息。
             *
             * @return 错误消息。
             */
            public String getMessage() {
                return this.msg;
            }

            /**
             * 设置错误代码值。
             *
             * @param code 错误代码。
             * @return 错误代码枚举。
             */
            public static ErrorCode setValue(int code) {
                for (ErrorCode c : ErrorCode.values()) {
                    if (code == c.getValue()) {
                        return c;
                    }
                }

                return UNKNOWN;
            }
        }

        /**
         * 执行成功。
         */
        public void onSuccess();

        /**
         * 执行出错。
         *
         * @param errorCode 执行错误代码。
         */
        public void onError(ErrorCode errorCode);
    }

    /**
     * 发送消息的回调。
     */
    public static interface SendMessageCallback {
        /**
         * 发送消息错误代码枚举。
         */
        public static enum ErrorCode {
            /**
             * 未知错误。
             */
            UNKNOWN(-1, "Unknown error."),

            /**
             * 服务器超时。
             */
            TIMEOUT(5004, "Server is timed out.");

            private int code;
            private String msg;

            /**
             * 构造函数。
             *
             * @param code 错误代码。
             * @param msg  错误消息。
             */
            ErrorCode(int code, String msg) {
                this.code = code;
                this.msg = msg;
            }

            /**
             * 获取错误代码值。
             *
             * @return 错误代码值。
             */
            public int getValue() {
                return this.code;
            }

            /**
             * 获取错误消息。
             *
             * @return 错误消息。
             */
            public String getMessage() {
                return this.msg;
            }

            /**
             * 设置错误代码值。
             *
             * @param code 错误代码。
             * @return 错误代码枚举。
             */
            public static ErrorCode setValue(int code) {
                for (ErrorCode c : ErrorCode.values()) {
                    if (code == c.getValue()) {
                        return c;
                    }
                }

                return UNKNOWN;
            }
        }

        /**
         * 发送消息成功。
         */
        public void onSuccess();

        /**
         * 发送消息的进度。
         *
         * @param progress 进度值，范围为 0 - 100。
         */
        public void onProgress(int progress);

        /**
         * 发送消息出错。
         *
         * @param errorCode 发送消息错误代码。
         */
        public void onError(ErrorCode errorCode);
    }

    /**
     * 创建讨论组的回调。
     */
    public static interface CreateDiscussionCallback {
        /**
         * 创建讨论组错误代码枚举。
         */
        public static enum ErrorCode {
            /**
             * 未知错误。
             */
            UNKNOWN(-1, "Unknown error."),

            /**
             * 服务器超时。
             */
            TIMEOUT(5004, "Server is timed out.");

            private int code;
            private String msg;

            /**
             * 构造函数。
             *
             * @param code 错误代码。
             * @param msg  错误消息。
             */
            ErrorCode(int code, String msg) {
                this.code = code;
                this.msg = msg;
            }

            /**
             * 获取错误代码值。
             *
             * @return 错误代码值。
             */
            public int getValue() {
                return this.code;
            }

            /**
             * 获取错误消息。
             *
             * @return 错误消息。
             */
            public String getMessage() {
                return this.msg;
            }

            /**
             * 设置错误代码值。
             *
             * @param code 错误代码。
             * @return 错误代码枚举。
             */
            public static ErrorCode setValue(int code) {
                for (ErrorCode c : ErrorCode.values()) {
                    if (code == c.getValue()) {
                        return c;
                    }
                }

                return UNKNOWN;
            }
        }

        /**
         * 创建讨论组成功。
         *
         * @param discussionId 创建的讨论组 Id。
         */
        public void onSuccess(String discussionId);

        /**
         * 创建讨论组出错。
         *
         * @param errorCode 创建讨论组错误代码。
         */
        public void onError(ErrorCode errorCode);
    }

    /**
     * 下载文件的回调。
     */
    public static interface DownloadMediaCallback {
        /**
         * 下载媒体文件错误代码枚举。
         */
        public static enum ErrorCode {
            /**
             * 未知错误。
             */
            UNKNOWN(-1, "Unknown error."),

            /**
             * 服务器超时。
             */
            TIMEOUT(5004, "Server is timed out.");

            private int code;
            private String msg;

            /**
             * 构造函数。
             *
             * @param code 错误代码。
             * @param msg  错误消息。
             */
            ErrorCode(int code, String msg) {
                this.code = code;
                this.msg = msg;
            }

            /**
             * 获取错误代码值。
             *
             * @return 错误代码值。
             */
            public int getValue() {
                return this.code;
            }

            /**
             * 获取错误消息。
             *
             * @return 错误消息。
             */
            public String getMessage() {
                return this.msg;
            }

            /**
             * 设置错误代码值。
             *
             * @param code 错误代码。
             * @return 错误代码枚举。
             */
            public static ErrorCode setValue(int code) {
                for (ErrorCode c : ErrorCode.values()) {
                    if (code == c.getValue()) {
                        return c;
                    }
                }

                return UNKNOWN;
            }
        }

        /**
         * 下载进度。
         *
         * @param progress 进度值，范围为 0 - 100。
         */
        public void onProgress(int progress);

        /**
         * 下载媒体文件成功。
         *
         * @param localMediaPath 下载的媒体文件的本地路径。
         */
        public void onSuccess(String localMediaPath);

        /**
         * 下载媒体文件出错。
         *
         * @param errorCode 下载媒体文件错误代码。
         */
        public void onError(ErrorCode errorCode);
    }

    /**
     * 获取用户信息的回调。
     */
    public static interface GetUserInfoCallback {
        /**
         * 获取用户信息错误代码枚举。
         */
        public static enum ErrorCode {
            /**
             * 未知错误。
             */
            UNKNOWN(-1, "Unknown error."),

            /**
             * 服务器超时。
             */
            TIMEOUT(5004, "Server is timed out.");

            private int code;
            private String msg;

            /**
             * 构造函数。
             *
             * @param code 错误代码。
             * @param msg  错误消息。
             */
            ErrorCode(int code, String msg) {
                this.code = code;
                this.msg = msg;
            }

            /**
             * 获取错误代码值。
             *
             * @return 错误代码值。
             */
            public int getValue() {
                return this.code;
            }

            /**
             * 获取错误消息。
             *
             * @return 错误消息。
             */
            public String getMessage() {
                return this.msg;
            }

            /**
             * 设置错误代码值。
             *
             * @param code 错误代码。
             * @return 错误代码枚举。
             */
            public static ErrorCode setValue(int code) {
                for (ErrorCode c : ErrorCode.values()) {
                    if (code == c.getValue()) {
                        return c;
                    }
                }

                return UNKNOWN;
            }
        }

        /**
         * 获取用户信息成功。
         *
         * @param user 用户信息。
         */
        public void onSuccess(UserInfo user);

        /**
         * 获取用户信息出错。
         *
         * @param errorCode 获取用户信息错误代码。
         */
        public void onError(ErrorCode errorCode);
    }

    /**
     * 获取讨论组的回调。
     */
    public static interface GetDiscussionCallback {
        /**
         * 获取讨论组错误代码枚举。
         */
        public static enum ErrorCode {
            /**
             * 未知错误。
             */
            UNKNOWN(-1, "Unknown error."),

            /**
             * 服务器超时。
             */
            TIMEOUT(5004, "Server is timed out.");

            private int code;
            private String msg;

            /**
             * 构造函数。
             *
             * @param code 错误代码。
             * @param msg  错误消息。
             */
            ErrorCode(int code, String msg) {
                this.code = code;
                this.msg = msg;
            }

            /**
             * 获取错误代码值。
             *
             * @return 错误代码值。
             */
            public int getValue() {
                return this.code;
            }

            /**
             * 获取错误消息。
             *
             * @return 错误消息。
             */
            public String getMessage() {
                return this.msg;
            }

            /**
             * 设置错误代码值。
             *
             * @param code 错误代码。
             * @return 错误代码枚举。
             */
            public static ErrorCode setValue(int code) {
                for (ErrorCode c : ErrorCode.values()) {
                    if (code == c.getValue()) {
                        return c;
                    }
                }

                return UNKNOWN;
            }
        }

        /**
         * 获取讨论组信息成功。
         *
         * @param discussion 讨论组信息。
         */
        public void onSuccess(Discussion discussion);

        /**
         * 获取用户信息出错。
         *
         * @param errorCode 获取用户信息错误代码。
         */
        public void onError(ErrorCode errorCode);
    }

    /**
     * 更新讨论组名字的回调。
     */
    public static interface UpdateDiscussionNameCallback {
        /**
         * 更新讨论组名字错误代码枚举。
         */
        public static enum ErrorCode {
            /**
             * 未知错误。
             */
            UNKNOWN(-1, "Unknown error."),

            /**
             * 服务器超时。
             */
            TIMEOUT(5004, "Server is timed out.");

            private int code;
            private String msg;

            /**
             * 构造函数。
             *
             * @param code 错误代码。
             * @param msg  错误消息。
             */
            ErrorCode(int code, String msg) {
                this.code = code;
                this.msg = msg;
            }

            /**
             * 更新错误代码值。
             *
             * @return 错误代码值。
             */
            public int getValue() {
                return this.code;
            }

            /**
             * 更新错误消息。
             *
             * @return 错误消息。
             */
            public String getMessage() {
                return this.msg;
            }

            /**
             * 设置错误代码值。
             *
             * @param code 错误代码。
             * @return 错误代码枚举。
             */
            public static ErrorCode setValue(int code) {
                for (ErrorCode c : ErrorCode.values()) {
                    if (code == c.getValue()) {
                        return c;
                    }
                }

                return UNKNOWN;
            }
        }


        /**
         * 更新讨论组名字成功。
         */
        public void onSuccess();

        /**
         * 更新讨论组名字出错。
         *
         * @param errorCode 更新讨论组名字错误代码。
         */
        public void onError(ErrorCode errorCode);
    }

    /**
     * 设置状态的回调。
     */
    public static interface SetBlockStatusCallback {

        /**
         * 设置状态成功。
         */
        public void onSuccess(int status);

        /**
         * 设置状态失败。
         */
        public void onError();
    }

    /**
     * 设置开放邀请状态的回调。SetInviteStatus
     */
    public static interface SetInviteStatusCallback {

        public void onComplete(int status);

//
//        /**
//         * 设置开放邀请状态成功。
//         */
//        public void onSuccess();
//
//        /**
//         * 设置开放邀请状态失败。
//         */
//        public void onError();
    }

    /**
     * 接收消息的监听器。
     */
    public static interface OnReceiveMessageListener {
        /**
         * 收到消息的处理。
         *
         * @param message 消息实体。
         */
        public void onReceived(Message message);
    }

    private static MessageContent getMessageContent(String objectName, byte[] content) {
        Constructor<? extends MessageContent> constructor = constructorMap.get(objectName);
        MessageContent result = null;

        if (constructor == null) {
            return new UnknowMessage(content);
        }

        try {
            return constructor.newInstance(content);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 消息基类。
     * <p/>
     * 定义了消息对象和消息数据之间互相转换的方法。
     */
    public static abstract class MessageContent implements Parcelable {

        protected ConversationType conversationType = ConversationType.PRIVATE;//会话类型

        /**
         * 获取目标 Id。
         * <p/>
         * 根据不同的 conversationType，可能是聊天 Id、讨论组 Id、群组 Id 或聊天室 Id。
         */
        protected String targetId = "";

        protected MessageContent() {}

        /**
         *  ===========也就是将服务器传递过来的二进制流生成  本地使用的bitmap=================================================
         * 从消息数据反序列化为本地消息对象，定义新消息对象时必须定义此构造函数。 
         * @param data 消息数据。
         */
        public MessageContent(byte[] data) {}

        /**
         *  ===========也就是将本地的图片转换成二进制流发送给服务器=================================================
         * 将本地消息对象序列化为消息数据。
         * @return 消息数据。
         */
        public abstract byte[] encode();
    }

    /**
     * 文字消息，会存入消息历史记录。
     */
    @MessageTag(value = "RC:TxtMsg", flag = MessageTag.ISCOUNTED | MessageTag.ISPERSISTED)
    public static class TextMessage extends MessageContent {

        private String content;
        /**
         * 将本地消息对象序列化为消息数据。
         * @return 消息数据。
         */
        @Override
        public byte[] encode() {

            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("content", getExpression(getContent()));
            } catch (JSONException e) {
                Log.d("JSONException", e.getMessage());
            }

            try {
                return jsonObj.toString().getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }

        private String getExpression(String content) {

            Pattern pattern = Pattern.compile("\\[/u([0-9A-Fa-f]+)\\]");
            Matcher matcher = pattern.matcher(content);

            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                matcher.appendReplacement(sb, toExpressionChar(matcher.group(1)));
            }

            matcher.appendTail(sb);
            Log.d("getExpression--", sb.toString());

            return sb.toString();
        }

        private String toExpressionChar(String expChar) {
            int inthex = Integer.parseInt(expChar, 16);
            return String.valueOf(Character.toChars(inthex));
        }

        protected TextMessage() {}

        protected TextMessage(byte[] data) {
            String jsonStr = null;
            try {
                jsonStr = new String(data, "UTF-8");
            } catch (UnsupportedEncodingException e1) {

            }

            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                setContent(jsonObj.getString("content"));
            } catch (JSONException e) {
                Log.d("JSONException", e.getMessage());
            }

        }
        /**
         * 构造函数。
         *
         * @param in 初始化传入的 Parcel。
         */
        public TextMessage(Parcel in) {
            content = in.readString();
        }

        /**
         * 设置文字消息的内容。
         *
         * @param content 文字消息的内容。
         */
        public void setContent(String content) {
            this.content = content;
        }

        /**
         * 构造函数。
         *
         * @param content 文字消息的内容。
         */
        public TextMessage(String content) {
            this.setContent(content);
        }

        /**
         * 获取文字消息的内容。
         *
         * @return 文字消息的内容。
         */
        public String getContent() {
            return content;
        }
        
        /**
         * 描述了包含在 Parcelable 对象排列信息中的特殊对象的类型。
         *
         * @return 一个标志位，表明Parcelable对象特殊对象类型集合的排列。
         */
        public int describeContents() {
            return 0;
        }

        /**
         * 将类的数据写入外部提供的 Parcel 中。
         *
         * @param dest  对象被写入的 Parcel。
         * @param flags 对象如何被写入的附加标志，可能是 0 或 PARCELABLE_WRITE_RETURN_VALUE。
         */
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            if (content != null) {
                dest.writeString(content);
            } else {
                dest.writeString("");
            }
        }

        /**
         * 读取接口，目的是要从Parcel中构造一个实现了Parcelable的类的实例处理。
         */
        public static final Creator<TextMessage> CREATOR = new Creator<TextMessage>() {

            @Override
            public TextMessage createFromParcel(Parcel source) {
                return new TextMessage(source);
            }
            @Override
            public TextMessage[] newArray(int size) {
                return new TextMessage[size];
            }
        };
    }
    /**
     * 图片消息，定义了图片缩略图和原图地址，会存入消息历史记录。
     */
    @MessageTag(value = "RC:ImgMsg", flag = MessageTag.ISCOUNTED | MessageTag.ISPERSISTED)
    public static class ImageMessage extends MessageContent {

        private Bitmap thumbnailImage;

        private String imageKey;

        private Uri uri;

        private static final int EXIST_SEPARATOR = 1;

        private static final int NON_SEPARATOR = 0;

        /**
         * 获取图片 Key。
         *
         * @return 图片 Key。
         */
        public String getImageKey() {
            return imageKey;
        }

        protected void setImageKey(String imageKey) {
            this.imageKey = imageKey;
        }

        /**
         * 将本地消息对象序列化为消息数据。
         */
        @Override
        public byte[] encode() {

            String thumbnailStr = BitmapUtil.getBase64FromBitmap(thumbnailImage);

            JSONObject jsonObj = new JSONObject();

            try {
                jsonObj.put("content", thumbnailStr);
                jsonObj.put("imageKey", getImageKey());
            } catch (JSONException e) {
                Log.d("JSONException", e.getMessage());
            }

            return jsonObj.toString().getBytes();
        }

        protected ImageMessage() {

        }

        protected ImageMessage(byte[] data) {

            String jsonStr = new String(data);

            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                String base64Str = jsonObj.getString("content");
                thumbnailImage = BitmapUtil.getBitmapFromBase64(base64Str);
                imageKey = jsonObj.getString("imageKey");
            } catch (JSONException e) {
                Log.d("JSONException", e.getMessage());
            }
        }

        /**
         * 构造函数。
         *
         * @param uri 消息中的图片原图 Uri。
         */
        public ImageMessage(Uri uri) {
            this.setUri(uri);
            try {
                thumbnailImage = BitmapUtil.getResizedBitmap(sContext, uri, 240, 240);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        protected byte[] getImageStream() {
            return FileUtil.getByteFromUri(uri);
        }

        /**
         * 获取缩略图位图。
         *
         * @return 缩略图位图。
         */
        public Bitmap getThumbnail() {
            return thumbnailImage;
        }

        public void setThumbnailNull() {
            this.thumbnailImage = null;
        }

        /**
         * 获取图片的 Uri。
         *
         * @return 图片的 Uri。
         */
        public Uri getUri() {
            return uri;
        }

        /**
         * 设置图片的 Uri。
         *
         * @param uri 图片的 Uri。
         */
        public void setUri(Uri uri) {
            this.uri = uri;
        }

        /**
         * 描述了包含在 Parcelable 对象排列信息中的特殊对象的类型。
         *
         * @return 一个标志位，表明Parcelable对象特殊对象类型集合的排列。
         */
        @Override
        public int describeContents() {
            return 0;
        }

        /**
         * 构造函数。
         *
         * @param in 初始化传入的 Parcel。
         */
        public ImageMessage(Parcel in) {

            int flag = in.readInt();
            if (flag == EXIST_SEPARATOR) {
                thumbnailImage = in.readParcelable(Bitmap.class.getClassLoader());
            } else {
                thumbnailImage = null;
            }

            imageKey = in.readString();

            uri = Uri.parse(in.readString());
        }

        /**
         * 将类的数据写入外部提供的 Parcel 中。
         *
         * @param dest  对象被写入的 Parcel。
         * @param flags 对象如何被写入的附加标志，可能是 0 或 PARCELABLE_WRITE_RETURN_VALUE。
         */
        @Override
        public void writeToParcel(Parcel dest, int flags) {

            if (thumbnailImage == null) {
                dest.writeInt(NON_SEPARATOR);
            } else {
                dest.writeInt(EXIST_SEPARATOR);
                dest.writeParcelable(thumbnailImage, 0);
            }

            if (imageKey != null) {
                dest.writeString(imageKey);
            } else {
                dest.writeString("");
            }

            if (uri == null || uri.getPath() == null) {
                dest.writeString("");
            } else {
                dest.writeString(uri.getPath());
            }
        }

        /**
         * 读取接口，目的是要从Parcel中构造一个实现了Parcelable的类的实例处理。
         */
        public static final Creator<ImageMessage> CREATOR = new Creator<ImageMessage>() {

            @Override
            public ImageMessage createFromParcel(Parcel source) {
                return new ImageMessage(source);
            }

            @Override
            public ImageMessage[] newArray(int size) {
                return new ImageMessage[size];
            }
        };
    }
    //为亚峰 提供数据库存放路径
    private static final String createPath(File file, String appKey) {
        File baseDirectory = new File(file, "RongCloud/" + appKey);
        if (!baseDirectory.exists()) {
            baseDirectory.mkdirs();
        }
        return baseDirectory.getPath();
    }
    /**
     * 初始化 SDK。
     *
     * @param context       应用上下文。
     * @param appKey        从开发者平台(<a href="http://rongcloud.cn"
     *                      target="_blank">rongcloud.cn</a>)申请的应用 AppKey。
     * @param pushIconResId 推送中显示的图标资源。
     */
    public static void init(Context context, String appKey, int pushIconResId) {
        if (context == null || TextUtils.isEmpty(appKey)) { throw new IllegalArgumentException(); }
        //为底层提供版本
        new Version(); sContext = context.getApplicationContext();
        File environmentPath = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            environmentPath = Environment.getExternalStorageDirectory();
            environmentPath = new File(environmentPath, "data/" + context.getPackageName());
        } else {
            environmentPath = context.getFilesDir();
        }
        File baseDirectory = new File(environmentPath, "RongCloud/" + appKey);
        if (!baseDirectory.exists()) {
            baseDirectory.mkdirs();
        }
        RongIMClient.resourcePath = baseDirectory.getPath();
        RongIMClient.appKey = appKey;
        String deviceId = NetworkUtil.getDeviceId(context);
        
        nativeObj = new NativeObject();
        nativeObj.InitClient(RongIMClient.resourcePath, createPath(context.getFilesDir(), appKey), deviceId, RongIMClient.appKey, context.getApplicationInfo().packageName);

        mWakeLock = new RongWakeLock(context);
        nativeObj.SetWakeupQueryListener(new NativeObject.WakeupQueryListener() {
            @Override
            public void QueryWakeup(int type) {
                mWakeLock.acquireWakeLock();
            }

            @Override
            public void ReleaseWakup() {
                mWakeLock.releaseWakeLock();
            }
        });
        try {
            registerMessageType(TextMessage.class);
            registerMessageType(ImageMessage.class);
            registerMessageType(VoiceMessage.class);
            registerMessageType(LocationMessage.class);
        } catch (AnnotationNotFoundException e) {
            return;
        }

        PushUtil.initAppIdActionMapping(context, appKey, pushIconResId);
        PushUtil.startPushSerive(context);
    }
    
    /**
     * 注册消息类型，如果对消息类型进行扩展，可以忽略此方法。
     *
     * @param type 消息类型，必须要继承自 {@link io.rong.imlib.RongIMClient.MessageContent}
     * @throws AnnotationNotFoundException 如果没有找到注解时抛出。
     */
    public static void registerMessageType(Class<? extends MessageContent> type) throws AnnotationNotFoundException {
        if (type == null) {throw new IllegalArgumentException();}

        MessageTag tag = type.getAnnotation(MessageTag.class);
        if (tag != null) {
            String objName = tag.value();
            int flag = tag.flag();
            try {
                Constructor<? extends MessageContent> constructor = type.getDeclaredConstructor(byte[].class);
                constructorMap.put(objName, constructor);
                nativeObj.RegisterMessageType(objName, flag);
            } catch (NoSuchMethodException e) {
                throw new AnnotationNotFoundException();
            }
        } else {
            throw new AnnotationNotFoundException();
        }
    }

    /**
     * 连接服务器。
     *
     * @param token    从服务端获取的用户身份令牌（Token）。
     * @param callback 连接回调。
     * @return RCloudClient IM 客户端核心类的实例。
     * @throws Exception 如果未进行初始化或者初始化失败时抛出。
     */
    public static RongIMClient connect(String token, final ConnectCallback callback) throws Exception {
        if (appKey == null || resourcePath == null) {  throw new NotInitializedException();}
        if (token == null) {  throw new IllegalArgumentException();}
        if (nativeObj == null) { throw new RuntimeException("RongCloud not init");}
        
        if (client == null) {
            client = new RongIMClient();
        }

        // TODO: 获取应用程序签名和服务器比对。开发者平台需要用户提交应用程序签名。具体细节待研究。
        client.token = token; isConnecting = true;
        nativeObj.Connect(token, new ConnectAckCallback() {
            @Override
            public void operationComplete(int status, String userId) {
                if (status == 0) {
                    // Start Heartbeat
                    WakeLockUtils.startNextHeartbeat(sContext);

                    client.currentUserId = userId;
                    client.mUserInfo.setUserId(userId);

//============================从数据库中获取用户信息==================================================================
                    client.getUserInfo(userId, new GetUserInfoCallback() {
                        @Override
                        public void onSuccess(UserInfo user) {
                            client.mUserInfo.setName(user.getName());
                            client.mUserInfo.setPortraitUri(user.getPortraitUri());
                        }
                        @Override
                        public void onError(ErrorCode errorCode) {}
                    });
                    if (callback != null)   callback.onSuccess(userId);
                } else {
                    if (callback != null)   callback.onError(ConnectCallback.ErrorCode.setValue(status));
                }
                isConnecting = false;
            }
        });
        return client;
    }

    /**
     * 获取用户信息。
     * <p/>
     * 如果本地缓存中包含用户信息，则从本地缓存中直接获取，否则将访问融云服务器获取用户登录时注册的信息；<br/>
     * 但如果该用户如果从来没有登录过融云服务器，则返回的用户信息会为空值。
     *
     * @param userId   用户 Id。
     * @param callback 获取用户信息的回调。
     */
    public void getUserInfo(String userId, final GetUserInfoCallback callback) {
        nativeObj.GetUserInfo(userId, new UserInfoOutputListener() {
            @Override
            public void onReceiveUserInfo(String userId, String userName, String userPortrait) {
                UserInfo info = new UserInfo();
                info.setName(userName);
                info.setPortraitUri(userPortrait);
                info.setUserId(userId);
                if (callback != null)  callback.onSuccess(info);
            }

            @Override
            public void OnError(int status) {
                if (callback != null) {
                    callback.onError(GetUserInfoCallback.ErrorCode.setValue(status));
                }
            }
        });
    }
    
    /**
     * 重新连接服务器。
     *
     * @param callback 连接回调。
     */
    public void reconnect(final ConnectCallback callback) {
        if (token != null && !isConnecting) {
            isConnecting = true;
            nativeObj.Connect(token, new ConnectAckCallback() {

                @Override
                public void operationComplete(int status, String userId) {
                    if (status == 0) {
                        client.currentUserId = userId;
                        if (callback != null) {
                            callback.onSuccess(userId);
                        }
                    } else {
                        if (callback != null) {
                            callback.onError(ConnectCallback.ErrorCode.setValue(status));
                        }
                    }
                    isConnecting = false;
                }
            });
        }
    }

    /**
     * 断开连接。
     */
    public void disconnect() {
        nativeObj.Disconnect();
    }

    /**
     * 获取所有未读消息数。
     *
     * @return 未读消息数。
     */
    public int getTotalUnreadCount() {
        return nativeObj.GetTotalUnreadCount();
    }

    /**
     * 获取会话列表。
     * <p/>
     * 会话列表按照时间从前往后排列，如果有置顶会话，则置顶会话在前。
     *
     * @return 会话列表。
     * @see io.rong.imlib.RongIMClient.Conversation
     */
    public List<Conversation> getConversationList() {

        int[] conversationTypes = new int[]{ConversationType.PRIVATE.getValue(), ConversationType.DISCUSSION.getValue(), ConversationType.GROUP.getValue()};

        byte[] bytes = nativeObj.GetConversationList(conversationTypes);
        
        String jsonResult = null;
        try {
            jsonResult = new String(bytes, "UTF-8");
            Log.d("jsonResult", jsonResult);
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        List<Conversation> list = new ArrayList<Conversation>();

        if (!TextUtils.isEmpty(jsonResult)) {
            try {
                JSONObject jo = new JSONObject(jsonResult);
                JSONArray jsonArray = (JSONArray) jo.get("result");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    JSONObject o = (JSONObject) jsonArray.get(i);
                    Conversation msg = new Conversation(o);
                    list.add(msg);
                }
            } catch (JSONException e) {
                Log.d("jsonerror", e.getMessage());
            }
        }

        return list;
    }

    /**
     * 从会话列表中移除某一会话，但是不删除会话内的消息。
     * <p/>
     * 如果此会话中有新的消息，该会话将重新在会话列表中显示，并显示最近的历史消息。
     *
     * @param conversationType 会话类型。
     * @param targetId         目标 Id。根据不同的 conversationType，可能是聊天 Id、讨论组 Id、群组 Id 或聊天室 Id。
     * @return 是否移除成功。
     */
    public boolean removeConversation(ConversationType conversationType, String targetId) {
        if (conversationType == null || TextUtils.isEmpty(targetId) || TextUtils.isEmpty(targetId.trim())) {
            throw new IllegalArgumentException();
        }

        targetId = targetId.trim();
        return nativeObj.RemoveConversation(conversationType.getValue(), targetId);
    }

    /**
     * 设置某一会话为置顶或者取消置顶。
     *
     * @param conversationType 会话类型。
     * @param targetId         目标 Id。根据不同的 conversationType，可能是聊天 Id、讨论组 Id、群组 Id 或聊天室 Id。
     * @param isTop            是否置顶。
     * @return 是否设置成功。
     */
    public boolean setConversationToTop(ConversationType conversationType, String targetId, boolean isTop) {
        if (conversationType == null || StringUtil.isEmpty(targetId)) {
            throw new IllegalArgumentException();
        }
        return nativeObj.SetIsTop(conversationType.getValue(), targetId, isTop);
    }

    /**
     * 获取最新消息记录。
     *
     * @param conversationType 会话类型。
     * @param targetId         目标 Id。
     * @param count            要获取的消息数量。
     * @return 最新消息记录，按照时间顺序从旧到新排列。
     */
    public List<Message> getLatestMessages(ConversationType conversationType, String targetId, int count) {
        if (conversationType == null || StringUtil.isEmpty(targetId)) {
            throw new IllegalArgumentException();
        }
        targetId = targetId.trim();
        return getHistoryMessages(conversationType, targetId, -1, count);
    }

    /**
     * 获取历史消息记录。
     *
     * @param conversationType 会话类型。不支持传入 ConversationType.CHATROOM。
     * @param targetId         目标 Id。根据不同的 conversationType，可能是聊天 Id、讨论组 Id、群组 Id。
     * @param oldestMessageId  最后一条消息的 Id，获取此消息之前的 count 条消息。
     * @param count            要获取的消息数量。
     * @return 历史消息记录，按照时间顺序从旧到新排列。
     */
    public List<Message> getHistoryMessages(ConversationType conversationType, String targetId, long oldestMessageId, int count) {
        if (conversationType == ConversationType.CHATROOM)
            return null;
        if (conversationType == null || StringUtil.isEmpty(targetId)) {
            throw new IllegalArgumentException();
        }
        targetId = targetId.trim();
        byte[] bytes = nativeObj.GetPagedMessage(targetId, oldestMessageId, count, conversationType.getValue());
        List<Message> list = new ArrayList<Message>();

        String jsonResult = null;

        try {
            jsonResult = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        if (!TextUtils.isEmpty(jsonResult)) {
            try {
                JSONObject jo = new JSONObject(jsonResult);
                JSONArray jsonArray = (JSONArray) jo.get("result");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    JSONObject o = (JSONObject) jsonArray.get(i);
                    NativeObject.Message nativeMsg = new NativeObject.Message(o);
                    Message msg = new Message(nativeMsg);
                    list.add(msg);
                }
            } catch (JSONException e) {
                Log.d("jsonerror", e.getMessage());
            }
        }

        return list;
    }

    /**
     * 删除指定的一条或者一组消息。
     *
     * @param messageIds 要删除的消息 Id 数组。
     */
    public void deleteMessages(int[] messageIds) {

        if (messageIds != null && messageIds.length > 0) {
            nativeObj.DeleteMessages(messageIds);
        }
    }

    /**
     * 清空某一会话的所有聊天消息记录。
     *
     * @param conversationType 会话类型。不支持传入 ConversationType.CHATROOM。
     * @param targetId         目标 Id。根据不同的 conversationType，可能是聊天 Id、讨论组 Id、群组 Id。
     * @return 是否清空成功。
     */
    public boolean clearMessages(ConversationType conversationType, String targetId) {
        if (conversationType == ConversationType.CHATROOM)
            return false;

        if (conversationType == null || StringUtil.isEmpty(targetId)) {
            throw new IllegalArgumentException();
        }
        return nativeObj.ClearMessages(conversationType.getValue(), targetId);
    }

    /**
     * 清除消息未读状态。
     *
     * @param conversationType 会话类型。不支持传入 ConversationType.CHATROOM。
     * @param targetId         目标 Id。根据不同的 conversationType，可能是聊天 Id、讨论组 Id、群组 Id。
     * @return 是否清空成功。
     */
    public boolean clearMessagesUnreadStatus(ConversationType conversationType, String targetId) {

        if (conversationType == ConversationType.CHATROOM)
            return false;

        if (conversationType == null || StringUtil.isEmpty(targetId)) {
            throw new IllegalArgumentException();
        }

        return nativeObj.ClearUnread(conversationType.getValue(), targetId);
    }

    /**
     * 设置消息的附加信息，此信息只保存在本地。
     *
     * @param messageId 消息 Id。
     * @param value     消息附加信息，最大 1024 字节。
     * @return 是否设置成功。
     */
    public boolean setMessageExtra(long messageId, String value) {
        return nativeObj.SetMessageExtra(messageId, value);
    }

    /**
     * 保存文字消息草稿。
     *
     * @param conversationType 会话类型。
     * @param targetId         目标 Id。根据不同的 conversationType，可能是聊天 Id、讨论组 Id、群组 Id 或聊天室 Id。
     * @param content          草稿的文字内容。
     * @return 是否保存成功。
     */
    public boolean saveTextMessageDraft(ConversationType conversationType, String targetId, String content) {
        if (conversationType == null || StringUtil.isEmpty(targetId)) {
            throw new IllegalArgumentException();
        }
        return nativeObj.SetTextMessageDraft(conversationType.getValue(), targetId, content);
    }

    /**
     * 获取某一会话的文字消息草稿。
     *
     * @param conversationType 会话类型。
     * @param targetId         目标 Id。根据不同的 conversationType，可能是聊天 Id、讨论组 Id、群组 Id 或聊天室 Id。
     * @return 草稿的文字内容。
     */
    public String getTextMessageDraft(ConversationType conversationType, String targetId) {
        if (conversationType == null || StringUtil.isEmpty(targetId)) {
            throw new IllegalArgumentException();
        }
        return nativeObj.GetTextMessageDraft(conversationType.getValue(), targetId);
    }

    /**
     * 清除某一会话的文字消息草稿。
     *
     * @param conversationType 会话类型。
     * @param targetId         目标 Id。根据不同的 conversationType，可能是聊天 Id、讨论组 Id、群组 Id 或聊天室 Id。
     * @return 是否清除成功。
     */
    public boolean clearTextMessageDraft(ConversationType conversationType, String targetId) {
        if (conversationType == null || StringUtil.isEmpty(targetId)) {
            throw new IllegalArgumentException();
        }

        return saveTextMessageDraft(conversationType, targetId, "");
    }


    public Conversation getConversation(ConversationType conversationType, String targetId) {
        if (conversationType == null || StringUtil.isEmpty(targetId)) {
            throw new IllegalArgumentException();
        }


        byte[] bytes = nativeObj.GetConversation(targetId, conversationType.getValue());
        String jsonResult = null;

        try {
            jsonResult = new String(bytes, "UTF-8");
            Log.d("jsonResult", jsonResult);
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        Conversation conversation = null;

        if (!TextUtils.isEmpty(jsonResult)) {
            try {
                JSONObject jo = new JSONObject(jsonResult);
                JSONArray jsonArray = (JSONArray) jo.get("result");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    JSONObject o = (JSONObject) jsonArray.get(i);
                    conversation = new Conversation(o);
                    break;
                }
            } catch (JSONException e) {
                Log.d("jsonerror", e.getMessage());
            }
        }

        return conversation;
    }

    /**
     * 获取讨论组信息和设置。
     *
     * @param discussionId 讨论组 Id。
     * @param fetchRemote  是否从网络获取
     * @param callback     获取讨论组的回调。
     */
    public void getDiscussion(String discussionId, boolean fetchRemote, final GetDiscussionCallback callback) {

        if (TextUtils.isEmpty(discussionId)) {
            throw new IllegalArgumentException();
        }

        nativeObj.GetDiscussionInfo(discussionId, fetchRemote, new NativeObject.DiscussionInfoListener() {

            @Override
            public void onReceived(NativeObject.DiscussionInfo info) {
                Log.d("MessageLogic", "getDiscussion--GetDiscussionInfo--onReceived:" + info.getDiscussionName());
                callback.onSuccess(new Discussion(info));
            }

            @Override
            public void OnError(int status) {
                callback.onError(GetDiscussionCallback.ErrorCode.setValue(status));
            }
        });
    }

    /**
     * 设置讨论组信息和设置。
     *
     * @param discussionId 讨论组 Id。
     * @param name         讨论组名称
     * @param callback     设置讨论组的回调。
     */
    public long setDiscussionName(String discussionId, String name, final UpdateDiscussionNameCallback callback) {

        if (TextUtils.isEmpty(discussionId) || TextUtils.isEmpty(discussionId.trim()) || TextUtils.isEmpty(name) || TextUtils.isEmpty(name.trim())) {
            throw new IllegalArgumentException();
        }

        nativeObj.RenameDiscussion(discussionId, name, new PublishAckListener() {
            @Override
            public void operationComplete(int code) {
                if (code == 0) {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else {
                    if (callback != null) {
                        callback.onError(UpdateDiscussionNameCallback.ErrorCode.setValue(code));
                    }
                }
            }
        });

        return 0;
    }

    /**
     * 创建讨论组。
     *
     * @param name       讨论组名称，如：当前所有成员的名字的组合。
     * @param userIdList 讨论组成员 Id 列表。
     * @param callback   创建讨论组成功后的回调。
     */
    public void createDiscussion(String name, final List<String> userIdList, final CreateDiscussionCallback callback) {
        if (StringUtil.isEmpty(name) || userIdList == null || userIdList.size() == 0) {
            throw new IllegalArgumentException();
        }

        nativeObj.CreateDiscussion(name, new NativeObject.CreateDiscussionCallback() {

            @Override
            public void OnError(final int errorCode) {
                callback.onError(CreateDiscussionCallback.ErrorCode.setValue(errorCode));
            }

            @Override
            public void OnSuccess(final String discussionId) {

                if (userIdList != null && userIdList.size() > 0) {
                    inviteMemberToDiscussion(discussionId, userIdList, new OperationCallback() {
                        @Override
                        public void onSuccess() {
                            if (callback != null) {
                                callback.onSuccess(discussionId);
                            }
                        }

                        @Override
                        public void onError(ErrorCode errorCode) {
                            if (callback != null) {
                                callback.onError(CreateDiscussionCallback.ErrorCode.setValue(errorCode.getValue()));
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 邀请一名或者一组用户加入讨论组。
     *
     * @param discussionId 讨论组 Id。
     * @param userIdList   邀请的用户 Id 列表。
     * @param callback     执行操作的回调。
     */
    public void inviteMemberToDiscussion(String discussionId, List<String> userIdList, final OperationCallback callback) {
        if (StringUtil.isEmpty(discussionId) || userIdList == null || userIdList.size() == 0) {
            throw new IllegalArgumentException();
        }

        if (userIdList != null && userIdList.size() > 0) {
//            String ids = StringUtil.join(userIdList.iterator(), "\n");
            String[] ids = new String[userIdList.size()];
            userIdList.toArray(ids);

            nativeObj.InviteMemberToDiscussion(discussionId, ids, new PublishAckListener() {
                @Override
                public void operationComplete(int code) {
                    if (callback != null) {
                        if (code == 0) {
                            callback.onSuccess();
                        } else {
                            callback.onError(OperationCallback.ErrorCode.setValue(code));
                        }
                    }
                }
            });
        }
    }

    /**
     * 供创建者将某用户移出讨论组。
     * <p/>
     * 移出自己或者调用者非讨论组创建者将产生
     * {@link io.rong.imlib.RongIMClient.OperationCallback.ErrorCode#UNKNOWN}
     * 错误。
     *
     * @param discussionId 讨论组 Id。
     * @param userId       用户 Id。
     * @param callback     执行操作的回调。
     */
    public void removeMemberFromDiscussion(String discussionId, String userId, final OperationCallback callback) {
        if (StringUtil.isEmpty(discussionId) || StringUtil.isEmpty(userId)) {
            throw new IllegalArgumentException();
        }
        nativeObj.RemoveMemberFromDiscussion(discussionId, userId, new PublishAckListener() {
            @Override
            public void operationComplete(int code) {
                if (code == 0) {
                    callback.onSuccess();
                } else {
                    callback.onError(OperationCallback.ErrorCode.setValue(code));
                }
            }
        });
    }

    /**
     * 退出当前用户所在的某讨论组。
     *
     * @param discussionId 讨论组 Id。
     * @param callback     执行操作的回调。
     */
    public void quitDiscussion(String discussionId, final OperationCallback callback) {
        if (StringUtil.isEmpty(discussionId)) {
            throw new IllegalArgumentException();
        }

        nativeObj.QuitDiscussion(discussionId, new PublishAckListener() {
            @Override
            public void operationComplete(int code) {
                if (callback != null) {
                    if (code == 0) {
                        callback.onSuccess();
                    } else {
                        callback.onError(OperationCallback.ErrorCode.setValue(code));
                    }
                }
            }
        });
    }

    /**
     * 发送消息。
     *
     * @param conversationType 会话类型。
     * @param targetId         目标 Id。根据不同的 conversationType，可能是聊天 Id、讨论组 Id、群组 Id 或聊天室 Id。
     * @param content          消息内容。
     * @param callback         发送消息的回调。
     * @return 发送的消息实体。
     */
    public Message sendMessage(final ConversationType conversationType, final String targetId, MessageContent content, final SendMessageCallback callback) {
        if (conversationType == null || StringUtil.isEmpty(targetId) || content == null) {
            throw new IllegalArgumentException();
        }

        final MessageTag msgTag = content.getClass().getAnnotation(MessageTag.class);

        Message msg = new Message();
        msg.setConversationType(conversationType);
        msg.setTargetId(targetId);
        msg.setMessageDirection(MessageDirection.SEND);
        msg.setSenderUserId(currentUserId);
        msg.setSentStatus(SentStatus.SENDING);
        msg.setSentTime(System.currentTimeMillis());
        msg.setObjectName(msgTag.value());
        msg.setContent(content);

        if (content instanceof ImageMessage) {
            final ImageMessage image = (ImageMessage) content;
            String key = nativeObj.GenerateKey(1);
            Log.d("RongIMCloud", "image key------------>" + key);
            image.setImageKey(key);
            final byte[] msgContent = content.encode();
            final long msgId = saveMessage(conversationType, targetId, content, currentUserId);
            msg.setMessageId(msgId);

            byte[] stream = image.getImageStream();
            nativeObj.SendFile(targetId, conversationType.getValue(), 1, key, stream, stream.length, new SendFileCallback() {
                @Override
                public void OnProgress(int progress) {
                    if (callback != null) {
                        callback.onProgress(progress);
                    }
                }

                @Override
                public void OnError(int errorCode, String url) {
                    if (callback != null) {
                        if (errorCode == 0) {
                            image.setUri(Uri.parse(url));

                            Log.d("RongIMCloud", "image url------------>" + url);

                            switch (conversationType) {
                                case PRIVATE:
                                    nativeObj.SendSingleMessage(3, targetId, msgTag.value(), msgContent, msgContent.length, msgId, new PublishAckListener() {
                                        @Override
                                        public void operationComplete(int code) {
                                            if (code == 0) {
                                                callback.onSuccess();
                                            } else {
                                                callback.onError(SendMessageCallback.ErrorCode.setValue(code));
                                            }
                                        }
                                    });
                                    break;
                                case DISCUSSION:
                                    nativeObj.SendMultiMessage(3, targetId, msgTag.value(), msgContent, msgContent.length, msgId, new PublishAckListener() {

                                        @Override
                                        public void operationComplete(int code) {
                                            if (code == 0) {
                                                callback.onSuccess();
                                            } else {
                                                callback.onError(SendMessageCallback.ErrorCode.setValue(code));
                                            }
                                        }
                                    });
                                    break;
                                case GROUP:
                                    break;
                                case CHATROOM:
                                    break;
                                default:
                                    break;
                            }

                        } else {
                            callback.onError(SendMessageCallback.ErrorCode.setValue(errorCode));
                        }
                    }
                }
            });
            return msg;
        } else {

            byte[] msgContent = content.encode();
            long msgId = saveMessage(conversationType, targetId, content, currentUserId);
            msg.setMessageId(msgId);

            switch (conversationType) {
                case PRIVATE:
                    nativeObj.SendSingleMessage(3, targetId, msgTag.value(), msgContent, msgContent.length, msgId, new PublishAckListener() {

                        @Override
                        public void operationComplete(int code) {
                            if (callback != null) {
                                if (code == 0) {
                                    callback.onSuccess();
                                } else {
                                    callback.onError(SendMessageCallback.ErrorCode.setValue(code));
                                }
                            }
                        }
                    });
                    break;
                case DISCUSSION:
                    nativeObj.SendMultiMessage(3, targetId, msgTag.value(), msgContent, msgContent.length, msgId, new PublishAckListener() {

                        @Override
                        public void operationComplete(int code) {
                            if (callback != null) {
                                if (code == 0) {
                                    callback.onSuccess();
                                } else {
                                    callback.onError(SendMessageCallback.ErrorCode.setValue(code));
                                }
                            }
                        }
                    });
                    break;
                case GROUP:
                    break;
                case CHATROOM:
                    break;
                default:
                    break;
            }
            return msg;
        }
    }

    /**
     * 发送通知消息。
     *
     * @param conversationType 会话类型。
     * @param targetId         目标 Id。根据不同的 conversationType，可能是聊天 Id、讨论组 Id、群组 Id。
     * @param content          通知消息内容。
     * @param callback         发送通知消息的回调。
     * @return 发送的通知消息实体。
     */
    public Message sendNotification(ConversationType conversationType, String targetId, NotificationMessage content, final SendMessageCallback callback) {
        if (conversationType == null || StringUtil.isEmpty(targetId) || content == null) {
            throw new IllegalArgumentException();
        }
        MessageTag msgTag = content.getClass().getAnnotation(MessageTag.class);

        Message msg = new Message();
        msg.setConversationType(conversationType);
        msg.setTargetId(targetId);
        msg.setMessageDirection(MessageDirection.SEND);
        msg.setSenderUserId(currentUserId);
        msg.setSentStatus(SentStatus.SENDING);
        msg.setSentTime(System.currentTimeMillis());
        msg.setObjectName(msgTag.value());
        msg.setContent(content);
        byte[] msgContent = content.encode();
        long msgId = saveMessage(conversationType, targetId, content, currentUserId);

        msg.setMessageId(msgId);
        switch (conversationType) {
            case PRIVATE:
                nativeObj.SendSingleMessage(2, targetId, msgTag.value(), msgContent, msgContent.length, msgId, new PublishAckListener() {

                    @Override
                    public void operationComplete(int code) {
                        if (callback != null) {
                            if (code == 0) {
                                callback.onSuccess();
                            } else {
                                callback.onError(SendMessageCallback.ErrorCode.setValue(code));
                            }
                        }
                    }
                });
                break;
            case DISCUSSION:
                nativeObj.SendMultiMessage(2, targetId, msgTag.value(), msgContent, msgContent.length, msgId, new PublishAckListener() {

                    @Override
                    public void operationComplete(int code) {
                        if (callback != null) {
                            if (code == 0) {
                                callback.onSuccess();
                            } else {
                                callback.onError(SendMessageCallback.ErrorCode.setValue(code));
                            }
                        }
                    }
                });
                break;
            case GROUP:

                break;
            default:
                break;
        }

        return msg;
    }

    /**
     * 发送状态消息。
     * <p/>
     * 此类消息不保证必达，但是速度最快，所以通常用来传递状态信息。如：发送对方正在输入的状态。
     *
     * @param conversationType 会话类型。
     * @param targetId         目标 Id。根据不同的 conversationType，可能是聊天 Id、讨论组 Id、群组 Id。
     * @param content          状态消息的内容。
     * @param callback         发送状态消息的回调。
     * @return 发送的状态消息实体。
     */
    public Message sendStatus(ConversationType conversationType, String targetId, StatusMessage content, final SendMessageCallback callback) {
        if (conversationType == null || StringUtil.isEmpty(targetId) || content == null) {
            throw new IllegalArgumentException();
        }
        MessageTag msgTag = content.getClass().getAnnotation(MessageTag.class);

        Message msg = new Message();
        msg.setConversationType(conversationType);
        msg.setTargetId(targetId);
        msg.setMessageDirection(MessageDirection.SEND);
        msg.setSenderUserId(currentUserId);
        msg.setSentStatus(SentStatus.SENDING);
        msg.setSentTime(System.currentTimeMillis());
        msg.setObjectName(msgTag.value());
        msg.setContent(content);
        byte[] msgContent = content.encode();
        long msgId = saveMessage(conversationType, targetId, content, currentUserId);

        msg.setMessageId(msgId);
        switch (conversationType) {
            case PRIVATE:
                nativeObj.SendSingleMessage(1, targetId, msgTag.value(), msgContent, msgContent.length, msgId, new PublishAckListener() {

                    @Override
                    public void operationComplete(int code) {
                        if (callback != null) {
                            if (code == 0) {
                                callback.onSuccess();
                            } else {
                                callback.onError(SendMessageCallback.ErrorCode.setValue(code));
                            }
                        }
                    }
                });
                break;
            case DISCUSSION:
                nativeObj.SendMultiMessage(1, targetId, msgTag.value(), msgContent, msgContent.length, msgId, new PublishAckListener() {

                    @Override
                    public void operationComplete(int code) {
                        if (callback != null) {
                            if (code == 0) {
                                callback.onSuccess();
                            } else {
                                callback.onError(SendMessageCallback.ErrorCode.setValue(code));
                            }
                        }
                    }
                });
                break;
            case GROUP:

                break;
            default:
                break;
        }

        return msg;
    }

    /**
     * 下载媒体文件。
     * <p/>
     * 用来获取媒体原文件时调用。如果本地缓存中包含此媒体文件，则从本地缓存中直接获取，否则将从服务器端下载。
     *
     * @param conversationType 会话类型。
     * @param targetId         目标 Id。根据不同的 conversationType，可能是聊天 Id、讨论组 Id、群组 Id 或聊天室 Id。
     * @param mediaType        媒体文件类型。
     * @param key              媒体文件 Key。
     * @param callback         下载媒体文件的回调。
     */
    public void downloadMedia(ConversationType conversationType, String targetId, MediaType mediaType, String key, final DownloadMediaCallback callback) {
        if (conversationType == null || StringUtil.isEmpty(targetId) || mediaType == null || StringUtil.isEmpty(key)) {
            throw new IllegalArgumentException();
        }
        nativeObj.DownFile(targetId, conversationType.getValue(), mediaType.getValue(), key, new DownFileCallback() {

            @Override
            public void OnProgress(int progress) {
                if (callback != null) {
                    callback.onProgress(progress);
                }
            }

            @Override
            public void OnError(int errorCode, String description) {
                if (callback != null) {
                    callback.onError(DownloadMediaCallback.ErrorCode.setValue(errorCode));
                }
            }

            @Override
            public void OnData(byte[] bytes) {

            }

            @Override
            public void OnComplete(String url) {
                if (callback != null) {
                    callback.onSuccess(url);
                }
            }

        });

    }

    /**
     * 清除所有缓存的资源。
     * <p/>
     * 清除 IMLib 缓存的各种资源文件，主要是下载的图片、视频等资源。
     *
     * @param callback 清除所有缓存资源的回调。
     */
    private void clearCachedResources(OperationCallback callback) {
        // TODO: 未实现
    }

    // 微信清除缓存的资源时，可以获取列表逐一选择删除，我们未来也可实现，优先级极低。 public List<Media>
    // public void getCachedResources(GetCachedResourcesCallback callback) { }

    // 清除某一缓存的资源，如果文件已经被删除，返回 false 报错？ public bool
    // public boolean clearCachedResource(String mediaKey) { }

  

    /**
     * 缓存用户信息。
     * <p/>
     * 部分 App 的用户信息是每次从远程服务器拉取的，非常不适合 IM 所需的数据访问模型（频繁调用）。<br/>
     * 通过此方法，可以将 App 的用户信息缓存起来，以便可以通过 getUserInfo 方法直接从缓存中获取。<br/>
     * 如果 App 的用户信息发生了更新，请注意在适当的时机再次调用此方法刷新缓存。
     *
     * @param userInfo 用户信息。
     */
    private void addUserInfoToCache(UserInfo userInfo) {
        // TODO: 未实现。
    }

    /**
     * 设置接收消息的监听器。
     * <p/>
     * 所有接收到的消息、通知、状态都经由此处设置的监听器处理。包括私聊消息、讨论组消息、群组消息、聊天室消息以及各种状态。
     *
     * @param listener 接收消息的监听器。
     */
    public void setOnReceiveMessageListener(final OnReceiveMessageListener listener) {
        nativeObj.SetMessageListener(new NativeObject.ReceiveMessageListener() {

            @Override
            public void onReceived(NativeObject.Message nativeMessage) {
                Message message = new Message(nativeMessage);

                Log.d("RongIMClinet", "------setOnReceiveMessageListener--------onReceived-------");

                if (listener != null) {
                    listener.onReceived(message);
                }
            }
        });
    }

    /**
     * 设置连接状态变化的监听器。
     *
     * @param listener 连接状态变化的监听器。
     */
    public static void setConnectionStatusListener(final ConnectionStatusListener listener) {
        mListener = listener;

        nativeObj.SetExceptionListener(new ExceptionListener() {

            @Override
            public void onError(int status, String desc) {
                switch (status) {
                    case 100:// tcp unreachable
                        status = 1;
                        break;
                    case 1001:// 踢人
                        status = 6;
                        break;
                    case 3001:// network invalid
                        status = 1;
                        break;
                    case 2003:// server unreachable
                        status = 8;
                        break;
                    case 3002:// server nav unreachable
                        status = 8;
                        break;
                    case 2001:// schema invalid
                        status = 9;
                        break;
                    case 2002:// user invalid
                        status = 9;
                        break;
                    case 2004:// auth invalid
                        status = 9;
                        break;
                }
                listener.onChanged(ConnectionStatusListener.ConnectionStatus.setValue(status));
            }
        });
    }

    protected static ConnectionStatusListener getConnectionStatusListener() {
        return mListener;
    }

    private long saveMessage(ConversationType conversationType, String targetId, MessageContent content, String senderUserId) {

        if (conversationType == null || StringUtil.isEmpty(targetId) || content == null || TextUtils.isEmpty(senderUserId)) {
            throw new IllegalArgumentException();
        }

        MessageTag msgTag = content.getClass().getAnnotation(MessageTag.class);

        if ((msgTag.flag() & msgTag.ISPERSISTED) == msgTag.ISPERSISTED) {//MessageTag.ISCOUNTED | MessageTag.ISPERSISTED
            byte[] contentByte = content.encode();
            return nativeObj.SaveMessage(contentByte, contentByte.length, targetId, msgTag.value(), senderUserId, conversationType.getValue());
        }

        return 0;
    }

    /**
     * 获取接收新消息状态
     *
     * @param targetId
     * @param fetch
     * @param setBlockStatusCallback
     */
    public void getBlockPushDiscussionStatus(String targetId, boolean fetch, final SetBlockStatusCallback setBlockStatusCallback) {

        nativeObj.GetBlockPushDiscussionStatus(targetId, fetch, new NativeObject.SetBlocktatusListener(){

            @Override
            public void operationComplete(int opStatus, int status) {

                if (setBlockStatusCallback != null) {
                    if (opStatus == 0) {
                        setBlockStatusCallback.onSuccess(status);
                    } else {
                        setBlockStatusCallback.onError();
                    }
                }
            }
        });
    }

    /**
     * 设置是否接收新消息
     *
     * @param targetId
     * @param isBlock
     * @param setBlockStatusCallback
     */
    public void setBlockPushDiscussionStatus(String targetId, boolean isBlock, final SetBlockStatusCallback setBlockStatusCallback) {

        if (isBlock) {

                nativeObj.BlockDiscussionPush(targetId, new NativeObject.SetBlocktatusListener() {

                @Override
                public void operationComplete(int opStatus, int status) {
                    if (opStatus == 0) {
                        if (setBlockStatusCallback != null) {
                            setBlockStatusCallback.onSuccess(status);
                        }
                    } else {
                        if (setBlockStatusCallback != null) {
                            setBlockStatusCallback.onError();
                        }
                    }
                }
            });

        } else {
            nativeObj.UnBlockDiscussionPush(targetId, new NativeObject.SetBlocktatusListener() {

                @Override
                public void operationComplete(int opStatus, int status) {
                    if (opStatus == 0) {
                        if (setBlockStatusCallback != null) {
                            setBlockStatusCallback.onSuccess(status);
                        }
                    } else {
                        if (setBlockStatusCallback != null) {
                            setBlockStatusCallback.onError();
                        }
                    }
                }
            });
        }

    }


    /**
     * 获取接收新消息状态
     *
     * @param targetId
     * @param fetch
     * @param setBlockStatusCallback
     */
    public void getBlockPushUserStatus(String targetId, boolean fetch, final SetBlockStatusCallback setBlockStatusCallback) {

        nativeObj.GetBlockPushUserStatus(targetId, fetch, new NativeObject.SetBlocktatusListener() {

            @Override
            public void operationComplete(int opStatus, int status) {
                if (setBlockStatusCallback != null) {
                    if (opStatus == 0) {
                        setBlockStatusCallback.onSuccess(status);
                    } else {
                        setBlockStatusCallback.onError();
                    }
                }
            }
        });
    }

    /**
     * 设置是否接收新消息
     *
     * @param targetId
     * @param isBlock
     * @param setBlockStatusCallback
     */
    public void setBlockPushUserStatus(String targetId, boolean isBlock, final SetBlockStatusCallback setBlockStatusCallback) {

        if (isBlock) {
            nativeObj.BlockUserPush(targetId, new NativeObject.SetBlocktatusListener() {

                @Override
                public void operationComplete(int opStatus, int status) {
                    if (opStatus == 0) {
                        if (setBlockStatusCallback != null) {
                            setBlockStatusCallback.onSuccess(status);
                        }
                    } else {
                        if (setBlockStatusCallback != null) {
                            setBlockStatusCallback.onError();
                        }
                    }
                }
            });
        } else {
            nativeObj.UnBlockUserPush(targetId, new NativeObject.SetBlocktatusListener() {

                @Override
                public void operationComplete(int opStatus, int status) {
                    if (opStatus == 0) {
                        if (setBlockStatusCallback != null) {
                            setBlockStatusCallback.onSuccess(status);
                        }
                    } else {
                        if (setBlockStatusCallback != null) {
                            setBlockStatusCallback.onError();
                        }
                    }
                }
            });
        }

    }

    /**
     * 设置讨论组开放成员邀请
     *
     * @param targetId
     * @param isOpen                  true:开放  false:不开放  默认开放
     * @param setInviteStatusCallback
     */
    public void setDiscussionInviteStatus(String targetId, boolean isOpen, final SetInviteStatusCallback setInviteStatusCallback) {

        if (StringUtil.isEmpty(targetId)) {
            throw new IllegalArgumentException();
        }

        nativeObj.SetInviteStatus(targetId, isOpen ? 0 : 1, new NativeObject.SetStatusListener() {

            @Override
            public void operationComplete(int opStatus) {

                if (setInviteStatusCallback != null) {
                    setInviteStatusCallback.onComplete(opStatus);
                }

            }
        });
    }


    /**
     * 获取来自某用户的未读消息数。
     *
     * @return 未读消息数。
     */
    public int getUnreadCount(ConversationType conversationType, String targetId) {

        if (StringUtil.isEmpty(targetId) || conversationType == null) {
            throw new IllegalArgumentException();
        }

        return nativeObj.GetUnreadCount(targetId, conversationType.getValue());
    }


    public UserInfo getCurrentUserInfo() {
        return this.mUserInfo;
    }

}