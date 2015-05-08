package io.rong.imkit.model;

import io.rong.imkit.common.RCloudConst;
import io.rong.imkit.utils.HTMLDecoder;
import io.rong.imkit.utils.HighLightUtils;
import io.rong.imkit.utils.ParcelUtils;
import io.rong.imkit.utils.Util;
import io.rong.imlib.RongIMClient.Conversation;
import io.rong.imlib.RongIMClient.ConversationType;
import io.rong.imlib.RongIMClient.ImageMessage;
import io.rong.imlib.RongIMClient.SentStatus;
import io.rong.imlib.RongIMClient.TextMessage;
import io.rong.imlib.RongIMClient.VoiceMessage;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableStringBuilder;


public class UIConversation extends io.rong.imlib.RongIMClient.Conversation implements Parcelable, RCloudType {

    private UIUserInfo userInfo;

    private UIDiscussion uiDiscussion;

    private final static int TEXT_MESSAGE = 101;
    private final static int IMAGE_MESSAGE = 102;
    private final static int VOICE_MESSAGE = 103;

    public UIConversation() {    }

    public UIConversation(Conversation conversation) {

        if (conversation != null) {
            setConversationTitle(conversation.getConversationTitle());
            setConversationType(conversation.getConversationType());
            setDraft(conversation.getDraft());
            setLatestMessageId(conversation.getLatestMessageId());
            setLatestMessage(conversation.getLatestMessage());
            setObjectName(conversation.getObjectName());
            // setReadStatus(conversation.getReadStatus());
            setReceivedTime(conversation.getReceivedTime());
            setSenderUserId(conversation.getSenderUserId());
            setSenderUserName(conversation.getSenderUserName());
            setSentStatus(conversation.getSentStatus());
            setSentTime(conversation.getSentTime());
            setTargetId(conversation.getTargetId());
            setTop(conversation.isTop());
            setUnreadMessageCount(conversation.getUnreadMessageCount());
//            setMessageDirection(conversation.getMessageDirection());
        }

    }

    public UIConversation(Parcel in) {

        int flag = in.readInt();

        if (flag == RCloudConst.Parcel.EXIST_SEPARATOR) {
            setConversationType(ConversationType.setValue(in.readInt()));
        } else {
            setConversationType(null);
        }

        setTargetId(ParcelUtils.readStringFromParcel(in));
        setConversationTitle(ParcelUtils.readStringFromParcel(in));
        setUnreadMessageCount(in.readInt());
        setTop(in.readInt() == 1 ? true : false);
        // setReadStatus(in.readInt());

        flag = in.readInt();

        if (flag == RCloudConst.Parcel.EXIST_SEPARATOR) {
            setSentStatus(SentStatus.setValue(in.readInt()));
        } else {
            setSentStatus(null);
        }

        setReceivedTime(in.readLong());
        setSentTime(in.readLong());

        setObjectName(ParcelUtils.readStringFromParcel(in));
        setSenderUserId(ParcelUtils.readStringFromParcel(in));
        setSenderUserName(ParcelUtils.readStringFromParcel(in));
        setLatestMessageId(in.readLong());

        flag = in.readInt();

        if (flag == RCloudConst.Parcel.EXIST_SEPARATOR) {
            int messageType = in.readInt();

            switch (messageType) {
                case TEXT_MESSAGE:
                    setLatestMessage(ParcelUtils.readFromParcel(in, TextMessage.class));
                    break;
                case IMAGE_MESSAGE:
                    setLatestMessage(ParcelUtils.readFromParcel(in, ImageMessage.class));
                    break;
                case VOICE_MESSAGE:
                    setLatestMessage(ParcelUtils.readFromParcel(in, VoiceMessage.class));
                    break;
            }

        } else {
            setLatestMessage(null);
        }


        setDraft(ParcelUtils.readStringFromParcel(in));

//        flag = in.readInt();

//        if (flag == RCloudConst.Parcel.EXIST_SEPARATOR) {
//            setMessageDirection(MessageDirection.setValue(in.readInt()));
//        } else {
//            setMessageDirection(null);
//        }

        setUiDiscussion(ParcelUtils.readFromParcel(in, UIDiscussion.class));

//        setUserInfo(ParcelUtils.readFromParcel(in,UIUserInfo.class));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        if (getConversationType() != null) {
            dest.writeInt(RCloudConst.Parcel.EXIST_SEPARATOR);
            dest.writeInt(getConversationType().getValue());
        } else {
            dest.writeInt(RCloudConst.Parcel.NON_SEPARATOR);
        }

        ParcelUtils.writeStringToParcel(dest, getTargetId());
        ParcelUtils.writeStringToParcel(dest, getConversationTitle());
        dest.writeInt(getUnreadMessageCount());
        dest.writeInt(isTop() ? 1 : 0);
        // dest.writeInt(getReadStatus());

        if (getSentStatus() != null) {
            dest.writeInt(RCloudConst.Parcel.EXIST_SEPARATOR);
            dest.writeInt(getSentStatus().getValue());
        } else {
            dest.writeInt(RCloudConst.Parcel.NON_SEPARATOR);
        }

        dest.writeLong(getReceivedTime());
        dest.writeLong(getSentTime());
        ParcelUtils.writeStringToParcel(dest, getObjectName());
        ParcelUtils.writeStringToParcel(dest, getSenderUserId());
        ParcelUtils.writeStringToParcel(dest, getSenderUserName());

        dest.writeLong(getLatestMessageId());


        if (getLatestMessage() != null) {

            dest.writeInt(RCloudConst.Parcel.EXIST_SEPARATOR);

            if (getLatestMessage() instanceof TextMessage) {
                dest.writeInt(TEXT_MESSAGE);
            } else if (getLatestMessage() instanceof ImageMessage) {
                dest.writeInt(IMAGE_MESSAGE);
            } else if (getLatestMessage() instanceof VoiceMessage) {
                dest.writeInt(VOICE_MESSAGE);
            }

            ParcelUtils.writeToParcel(dest, getLatestMessage());

        } else {
            dest.writeInt(RCloudConst.Parcel.NON_SEPARATOR);
        }


        ParcelUtils.writeStringToParcel(dest, getDraft());

//        if (getMessageDirection() != null) {
//            dest.writeInt(RCloudConst.Parcel.EXIST_SEPARATOR);
//            dest.writeInt(getMessageDirection().getValue());
//        } else {
//            dest.writeInt(RCloudConst.Parcel.NON_SEPARATOR);
//        }

        ParcelUtils.writeToParcel(dest, uiDiscussion);
//        ParcelUtils.writeToParcel(dest, userInfo);

    }

    public static final Creator<UIConversation> CREATOR = new Creator<UIConversation>() {

        @Override
        public UIConversation createFromParcel(Parcel source) {

            return new UIConversation(source);

        }

        @Override
        public UIConversation[] newArray(int size) {

            return new UIConversation[size];

        }

    };

    private SpannableStringBuilder textMessageContent = null;

    public SpannableStringBuilder getTextMessageContent() {

        if (textMessageContent != null) {
            return textMessageContent;
        }

        if (getLatestMessage() instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) getLatestMessage();
            String str = HTMLDecoder.decode(HTMLDecoder.decode(textMessage.getContent()));
            return textMessageContent = Util.highLight(HighLightUtils.loadHighLight(str));

        }

        return new SpannableStringBuilder("");
    }

//=================================ConversationListFragment的getUserInfo方法中设置了用户信息值，主要设置用户名==================================================================
    public UIUserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UIUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public void setTextMessageContent(SpannableStringBuilder textMessageContent) {
        this.textMessageContent = textMessageContent;
    }


    public UIDiscussion getUiDiscussion() {
        return uiDiscussion;
    }

    public void setUiDiscussion(UIDiscussion uiDiscussion) {
        this.uiDiscussion = uiDiscussion;

        if (uiDiscussion != null) {
            this.setConversationTitle(uiDiscussion.getName());
        }
    }
}
