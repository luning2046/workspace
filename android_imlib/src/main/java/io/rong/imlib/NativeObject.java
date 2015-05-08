package io.rong.imlib;

import org.json.JSONObject;

class NativeObject {

    static {
        System.loadLibrary("RongIMLib");
    }

    NativeObject() {
        setJNIEnv(this);
    }

    /**
     *
     *
     */
    public static interface ConnectAckCallback {
        void operationComplete(int status, String userId);
    }

    public static interface SendFileCallback {
        void OnProgress(int progress);

        void OnError(int errorCode, String description);
    }

    public static interface PublishAckListener {
        void operationComplete(int code);
    }

    public static interface CreateDiscussionCallback {
        public void OnSuccess(String discussionId);

        public void OnError(int errorCode);
    }

    public static interface DownFileCallback extends SendFileCallback {
        void OnData(byte[] bytes);

        void OnComplete(String url);
    }

    public static interface UserInfoOutputListener {
        void onReceiveUserInfo(String userId, String userName, String userPortrait);

        void OnError(int status);
    }

    public static interface WakeupQueryListener {
        void QueryWakeup(int type);

        void ReleaseWakup();
    }

    public static interface EnvironmentChangeNotifyListener {
        void Complete(int type, String desc);
    }

    public static abstract class ReceiveMessageListener {
        public abstract void onReceived(Message message);

        protected Message getNewMessage() {
            return new Message();
        }
    }

    public static interface DiscussionInfoListener {
        void onReceived(DiscussionInfo info);

        void OnError(int status);
    }

    public static interface ExceptionListener {
        void onError(int status, String desc);
    }


    public static interface SetStatusListener {
        void operationComplete(int status);
    }

    public static interface SetBlocktatusListener {
         void operationComplete(int opStatus, int status);
    }

    public static class Message {
        private int conversationType;
        private String targetId;
        private long messageId;
        private boolean messageDirection;
        private String senderUserId;
        private int readStatus;
        private int sentStatus;
        private long receivedTime;
        private long sentTime;
        private String objectName;
        private byte[] content;
        private String extra;

        public Message(JSONObject jsonObj) {
            conversationType = jsonObj.optInt("conversation_category");
            targetId = jsonObj.optString("target_id");
            messageId = jsonObj.optLong("id");
            messageDirection = jsonObj.optBoolean("message_direction");
            senderUserId = jsonObj.optString("sender_user_id");
            readStatus = jsonObj.optInt("read_status");
            sentStatus = jsonObj.optInt("send_status");
            receivedTime = jsonObj.optLong("receive_time");
            sentTime = jsonObj.optLong("send_time");
            objectName = jsonObj.optString("object_name");
            content = jsonObj.optString("content").getBytes();
            extra = jsonObj.optString("extra");
        }

        public Message() {
        }

        public int getConversationType() {
            return conversationType;
        }

        public void setConversationType(int conversationType) {
            this.conversationType = conversationType;
        }

        public String getTargetId() {
            return targetId;
        }

        public void setTargetId(String targetId) {
            this.targetId = targetId;
        }

        public long getMessageId() {
            return messageId;
        }

        public void setMessageId(long messageId) {
            this.messageId = messageId;
        }

        public boolean getMessageDirection() {
            return messageDirection;
        }

        public void setMessageDirection(boolean messageDirection) {
            this.messageDirection = messageDirection;
        }

        public int getReadStatus() {
            return readStatus;
        }

        public void setReadStatus(int readStatus) {
            this.readStatus = readStatus;
        }

        public int getSentStatus() {
            return sentStatus;
        }

        public void setSentStatus(int sentStatus) {
            this.sentStatus = sentStatus;
        }

        public long getReceivedTime() {
            return receivedTime;
        }

        public void setReceivedTime(long receivedTime) {
            this.receivedTime = receivedTime;
        }

        public long getSentTime() {
            return sentTime;
        }

        public void setSentTime(long sentTime) {
            this.sentTime = sentTime;
        }

        public String getObjectName() {
            return objectName;
        }

        public void setObjectName(String objectName) {
            this.objectName = objectName;
        }

        public byte[] getContent() {
            return content;
        }

        public void setContent(byte[] content) {
            this.content = content;
        }

        public String getExtra() {
            return extra;
        }

        public void setExtra(String extra) {
            this.extra = extra;
        }

        public String getSenderUserId() {
            return senderUserId;
        }

        public void setSenderUserId(String senderUserId) {
            this.senderUserId = senderUserId;
        }
    }

    public static class DiscussionInfo {
        private String discussionId;
        private String discussionName;
        private String adminId;
        private String userIds;
        private int inviteStatus;//0:开放 1:非开放

        public String getDiscussionId() {
            return discussionId;
        }

        public void setDiscussionId(String discussionId) {
            this.discussionId = discussionId;
        }

        public String getDiscussionName() {
            return discussionName;
        }

        public void setDiscussionName(String discussionName) {
            this.discussionName = discussionName;
        }

        public String getAdminId() {
            return adminId;
        }

        public void setAdminId(String adminId) {
            this.adminId = adminId;
        }

        public String getUserIds() {
            return userIds;
        }

        public void setUserIds(String userIds) {
            this.userIds = userIds;
        }

        public int getInviteStatus() {
            return inviteStatus;
        }

        public void setInviteStatus(int inviteStatus) {
            this.inviteStatus = inviteStatus;
        }

    }

    /**
     * @param nativeObj
     */
    protected native void setJNIEnv(NativeObject nativeObj);

    /**
     * 初始化AppId
     *
     * @param appid
     * @return
     */
    protected native long InitClient(String localPath, String dbPath, String deviceId, String appid, String packageName);

    /**
     * 注册对象
     *
     * @param objName      对象名称
     * @param isPersistent 是否持久化数据
     */
    protected native void RegisterMessageType(String objName, int flag);

    /**
     * 连接服务器
     *
     * @param token
     * @param callback
     */
    protected native void Connect(String token, ConnectAckCallback callback);

    /**
     * 断开服务器连接
     */
    protected native void Disconnect();

    /**
     * 下载媒体文件。
     *
     * @param messageId 消息 Id
     * @param callback  下载媒体文件的回调
     */
    protected native void DownFile(String targetId, int conversationType, int type, String key, DownFileCallback callback);

    protected native String GenerateKey(int mimeType);

    protected native void SendFile(String targetId, int category, int type, String imageKey, byte[] data, long len, SendFileCallback callback);

    /**
     * 获取历史消息记录。
     *
     * @param conversationType 会话类型
     * @param targetId         会话 Id
     * @param oldestMessageId  最后一条消息的 Id，获取此消息之前的 count 条消息
     * @param count            要获取的消息数量
     * @return 历史消息记录，按照时间顺序从旧到新排列
     */
    protected native byte[] GetPagedMessage(String targetId, long beginId, int count, int category);

    /**
     * 删除指定的消息。
     *
     * @param idList 要删除的消息 Id 列表
     */
    protected native void DeleteMessages(int[] idList);

    /**
     * 清空某一会话的所有聊天消息记录。
     *
     * @param conversationType 会话类型
     * @param targetId         会话 Id
     */
    protected native boolean ClearMessages(int conversationType, String targetId);

    /**
     * TODO 需攀哥来增加备注
     *
     * @param conversationType 会话类型
     * @param targetId         会话 Id
     * @return
     */
    protected native boolean ClearUnread(int conversationType, String targetId);

    /**
     * 设置消息的附加信息，此信息只保存在本地，如：设置语音消息的已读未读状态。
     *
     * @param messageId 消息 Id
     * @param value     消息附加信息，最大 1024 字节
     */
    protected native boolean SetMessageExtra(long messageId, String value);

    /**
     * 从会话列表中移除某一会话，但是不删除会话内的消息，如果此会话中有新的消息，该会话将重新在会话列表中显示，并显示最近的历史消息。
     *
     * @param conversationType 会话类型
     * @param targetId         会话 Id
     */
    protected native boolean RemoveConversation(int conversationType, String targetId);

    /**
     * 保存文字消息的草稿。
     *
     * @param conversationType 会话类型
     * @param targetId         会话 Id
     * @param content          草稿的文字内容
     */
    protected native boolean SetTextMessageDraft(int conversationType, String targetId, String content);

    protected native boolean SetMessageContent(long messageId, byte[] content);

    /**
     * 获取文字消息的草稿。
     *
     * @param conversationType 会话类型
     * @param targetId         会话 Id
     * @return 草稿的文字内容
     */
    protected native String GetTextMessageDraft(int conversationType, String targetId);

    /**
     * 获取会话列表
     *
     * @return 会话列表 JSON
     */
    protected native byte[] GetRecentConversation();

    /**
     * 设置某一会话为置顶或者取消置顶。
     *
     * @param conversationType 会话类型
     * @param targetId         会话 Id
     * @param isTop            是否置顶
     */
    protected native boolean SetIsTop(int conversationType, String targetId, boolean isTop);

    /**
     * 获取所有未读消息数。
     *
     * @return 未读消息数
     */
    protected native int GetTotalUnreadCount();

    /**
     * 创建讨论组
     *
     * @param name     讨论组名称。如当前所有成员的名字的组合
     * @param callback 创建讨论组成功后的回调
     */
    protected native void CreateDiscussion(String name, CreateDiscussionCallback callback);

    /**
     * 邀请用户加入讨论组。
     *
     * @param discussionId 讨论组 Id
     * @param userIdList   邀请的用户 Id 列表
     * @param callback     操作回调
     */
    protected native void InviteMemberToDiscussion(String discussionId, String[] userIds, PublishAckListener callback);

    /**
     * 供管理员将某用户移出讨论组，移出自己或者非讨论组创建者执行移出操作将产生错误。
     *
     * @param discussionId 讨论组 Id
     * @param userId       用户 Id
     * @param callback     操作回调
     */
    protected native void RemoveMemberFromDiscussion(String discussionId, String userId, PublishAckListener callback);

    /**
     * 退出自己所在的某讨论组。
     *
     * @param discussionId 讨论组 Id
     * @param callback     操作回调
     */
    protected native void QuitDiscussion(String discussionId, PublishAckListener callback);

    protected native long SaveMessage(byte[] content, int len, String targetId, String objectName, String senderUserId, int category);

    /**
     * 发送二人会话消息
     *
     * @param userId
     * @param message
     * @param callback
     * @return
     */
    protected native void SendSingleMessage(int transferType, String userId, String objectName, byte[] message, int len, long messageId, PublishAckListener callback);

    /**
     * 发送客服会话消息
     *
     * @param userId
     * @param message
     * @param callback
     * @return
     */
    protected native void SendReceptionMessage(int transferType, String userId, String objectName, byte[] message, int len, long messageId, PublishAckListener callback);

    
    /**
     * 发送讨论组消息
     *
     * @param discussionId
     * @param message
     * @param callback
     * @return
     */
    protected native void SendMultiMessage(int transferType, String userId, String objectName, byte[] message, int len, long messageId, PublishAckListener callback);
    /**
     * 发送群组消息
     *
     * @param discussionId
     * @param message
     * @param callback
     * @return
     */
    protected native void SendGroupMessage(int transferType, String userId, String objectName, byte[] message, int len, long messageId, PublishAckListener callback);

    protected native void GetUserInfo(String userId, UserInfoOutputListener callback);

    protected native void SetMessageListener(ReceiveMessageListener listener);

    protected native boolean SetReadStatus(long messageId, int status);

    protected native void SetWakeupQueryListener(WakeupQueryListener listener);

    protected native void EnvironmentChangeNotify(int type, byte[] data, int dataSize, EnvironmentChangeNotifyListener callback);

    protected native void GetDiscussionInfo(String discussionId, boolean fetchRemote, DiscussionInfoListener callback);

    protected native void SetExceptionListener(ExceptionListener listener);

    protected native void RenameDiscussion(String targetId, String discussionName, PublishAckListener publishAckListener);

    protected native byte[] GetConversation(String targetId, int conversationType);


    protected native void BlockUserPush(String targetId, SetBlocktatusListener callback);

    protected native void UnBlockUserPush(String targetId, SetBlocktatusListener callback);

    protected native void GetBlockPushUserStatus(String targetId, boolean fetchRemote, SetBlocktatusListener callback);

    protected native void BlockDiscussionPush(String targetId, SetBlocktatusListener callback);

    protected native void UnBlockDiscussionPush(String targetId, SetBlocktatusListener callback);

    protected native void GetBlockPushDiscussionStatus(String targetId, boolean fetchRemote, SetBlocktatusListener callback);

    protected native void SetInviteStatus(String targetId, int status, SetStatusListener callback);

    protected native  int GetUnreadCount(String targetId,int conversationType);

    protected native byte[] GetConversationList(int[] conversationTypes);
    
    /**
     * 同步群组信息。
     *
     * @param groupIds 		自己所加入群组 Id 列表 
     * @param groupNames   	自己所加入群组 Name 列表
     * @param callback     	操作回调
     */
    protected native void SyncGroups(String[] groupIds,String[] groupNames, PublishAckListener callback);
    
    /**
     * 加入群组。
     *
     * @param groupIds 		加入群组 Id  
     * @param groupNames   	加入群组 Name
     * @param callback     	操作回调
     */
    protected native void JoinGroup(String groupId,String groupName, PublishAckListener callback);
    
    /**
     * 退出群组。
     *
     * @param groupIds 		退出群组 Id  
     * @param groupNames   	退出群组 Name
     * @param callback     	操作回调
     */
    protected native void QuitGroup(String groupId,String groupName, PublishAckListener callback);
    
}
