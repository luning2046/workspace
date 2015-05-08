package io.rong.imkit.data;

import io.rong.imkit.RCloudContext;
import io.rong.imkit.model.HandshakeMessage;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.model.UIMessage;
import io.rong.imlib.RongIMClient.Conversation;
import io.rong.imlib.RongIMClient.ConversationType;
import io.rong.imlib.RongIMClient.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhjchen on 14-4-16.
 */
public class DBHelper {

    public static final String TAG = "DBHelper";

    public static DBHelper dbHelper = null;

    public static DBHelper getInstance() {
        if (dbHelper == null) {
            dbHelper = new DBHelper();
        }

        return dbHelper;
    }

    /**
     * 获取会话列表
     *
     * @return
     */
    public ArrayList<UIConversation> getConversationList() {

        List<Conversation> conversations = null;
        if (RCloudContext.getInstance() != null && RCloudContext.getInstance().getRongIMClient() != null) {
            conversations = RCloudContext.getInstance().getRongIMClient().getConversationList();
        }

        ArrayList<UIConversation> uiConversations = new ArrayList<UIConversation>();

        if (conversations != null) {
            for (Conversation conversation : conversations) {
                uiConversations.add((new UIConversation(conversation)));
            }
        }
        return uiConversations;
    }

    /**
     * @param conversationType
     * @param conversationId
     * @param beginId
     * @param count
     * @return
     */
    public ArrayList<UIMessage> getMessageList(ConversationType conversationType, String conversationId, long beginId, int count) {

        ArrayList<UIMessage> uiMessages = new ArrayList<UIMessage>();

        List<Message> messages = RCloudContext.getInstance().getRongIMClient().getHistoryMessages(conversationType, conversationId, beginId, count);

        if (messages != null) {
            for (Message message : messages) {
                if (!(message.getContent() instanceof HandshakeMessage)) {
                    uiMessages.add(new UIMessage(message));
                }
            }
        }

        return uiMessages;

    }

    /**
     * @param conversationType
     * @param conversationId
     * @param count
     * @return
     */
    public ArrayList<UIMessage> getLasetMessageList(ConversationType conversationType, String targetId, int count) {

        ArrayList<UIMessage> uiMessages = new ArrayList<UIMessage>();

        List<Message> messages = RCloudContext.getInstance().getRongIMClient().getLatestMessages(conversationType, targetId, count);

        if (messages != null) {
            for (Message message : messages) {
                if (!(message.getContent() instanceof HandshakeMessage)) {
                    uiMessages.add((new UIMessage(message)));
                }
            }
        }

        return uiMessages;

    }

    /**
     * @param ids
     */
    public void deleteMessage(ArrayList<Long> ids) {
        int[] messageIds = null;
        int i = 0;

        if (ids != null && ids.size() > 0) {
            messageIds = new int[ids.size()];

            for (Long id : ids) {
                messageIds[i] = id.intValue();
                i++;
            }
        }

        if (messageIds != null) {
            RCloudContext.getInstance().getRongIMClient().deleteMessages(messageIds);
        }

    }

    public void clearMessages(ConversationType conversationType, String targetId) {
        RCloudContext.getInstance().getRongIMClient().clearMessages(conversationType, targetId);
    }

    public void setTop(ConversationType conversationType, String targetId, boolean isTop) {
        RCloudContext.getInstance().getRongIMClient().setConversationToTop(conversationType, targetId, isTop);
    }

    /**
     * @param conversationType
     * @param targetId
     */
    public void clearUnReadMessage(ConversationType conversationType, String targetId) {
        RCloudContext.getInstance().getRongIMClient().clearMessagesUnreadStatus(conversationType, targetId);
    }

    public int getTotalUnreadCount() {
        return RCloudContext.getInstance().getRongIMClient().getTotalUnreadCount();
    }

    public boolean setMessageExtra(long messageId, String value) {
        return RCloudContext.getInstance().getRongIMClient().setMessageExtra(messageId, value);
    }

    public boolean removeConversation(ConversationType conversationType, String targetId) {
        return RCloudContext.getInstance().getRongIMClient().removeConversation(conversationType, targetId);
    }


    public UIConversation getConversation(ConversationType conversationType, String targetId) {
        Conversation conversation = RCloudContext.getInstance().getRongIMClient().getConversation(conversationType, targetId);

        if (conversation != null) {
            return new UIConversation(conversation);
        }

        return null;
    }
}
