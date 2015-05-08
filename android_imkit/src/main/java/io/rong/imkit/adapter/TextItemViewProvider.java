package io.rong.imkit.adapter;

import io.rong.imkit.RCloudContext;
import io.rong.imkit.common.MessageContext;
import io.rong.imkit.model.RCloudType;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.model.UIUserInfo;
import io.rong.imkit.utils.HTMLDecoder;
import io.rong.imkit.utils.HighLightUtils;
import io.rong.imkit.utils.RCDateUtils;
import io.rong.imkit.utils.ResourceUtils;
import io.rong.imkit.utils.Util;
import io.rong.imkit.veiw.AsyncImageView;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.MessageDirection;
import io.rong.imlib.RongIMClient.SentStatus;
import io.rong.imlib.RongIMClient.TextMessage;
import io.rong.imlib.RongIMClient.UserInfo;

import java.util.Date;
import java.util.List;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sea_monster.core.resource.model.Resource;

public class TextItemViewProvider extends BaseViewProvider {

    private UIUserInfo mUserInfo = null;

    public TextItemViewProvider() {
    }

    public TextItemViewProvider(MessageContext context) {
        super(context);
    }

    @Override
    public int setItemLayoutRes() {
        return ResourceUtils.getLayoutResourceId(mContext, "rc_item_text_conversation");
    }

    @Override
    public View getItemView(View convertView, ViewHolder holder, RCloudType data, final int position, List datas) {

        TextView messageTimeTV = holder.obtainView(convertView, "conversation_message_time_tv");
        AsyncImageView otherPortrait = holder.obtainView(convertView, "conversation_message_other_portrait");
        TextView userNameRightTextView = holder.obtainView(convertView, "conversation_message_username_right_tv");
        ImageView sendFailedImageView = holder.obtainView(convertView, "conversation_message_send_failed");
        TextView messageRightContent = holder.obtainView(convertView, "conversation_message_content_right_tv");
        ProgressBar progressBar = holder.obtainView(convertView, "conversation_message_progressbar");
        AsyncImageView selfPortrait = holder.obtainView(convertView, "conversation_message_self_portrait");
        LinearLayout textRightLayout = holder.obtainView(convertView, "text_right_layout_layout");
        LinearLayout textLeftLayout = holder.obtainView(convertView, "text_left_layout_layout");

        TextView userNameLeftTextView = holder.obtainView(convertView, "conversation_message_username_left_tv");
        TextView messageLeftContent = holder.obtainView(convertView, "conversation_message_content_left_tv");

        final UIMessage message = (UIMessage) data;
        message.setPositionInList(position);

        // 时间处理 begin
        long currentTime = 0;

        if (message.getMessageDirection() == MessageDirection.SEND) {
            currentTime = message.getSentTime();
        } else {
            currentTime = message.getReceivedTime();
        }

        if (position > 0) {

            UIMessage messageTemp = (UIMessage) datas.get(position - 1);
            long preTime = 0;

            if (messageTemp.getMessageDirection() == MessageDirection.SEND) {
                preTime = messageTemp.getSentTime();
            } else {
                preTime = messageTemp.getReceivedTime();
            }

            if (RCDateUtils.isShowChatTime(currentTime, preTime)) {
                messageTimeTV.setText(RCDateUtils.getConvastionFromatDate(new Date(currentTime)));
                messageTimeTV.setVisibility(View.VISIBLE);
            } else {
                messageTimeTV.setVisibility(View.GONE);
            }
        } else {
            messageTimeTV.setText(RCDateUtils.getConvastionFromatDate(new Date(currentTime)));
            messageTimeTV.setVisibility(View.VISIBLE);
        }

        // 时间处理 end

        selfPortrait.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                UserInfo user = onPortaitClick(RCloudContext.getInstance().getRongIMClient().getCurrentUserInfo().getUserId());
            }
        });

        otherPortrait.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (RongIMClient.ConversationType.PRIVATE == message.getConversationType()) {
                    onPortaitClick(message.getTargetId());
                } else if (RongIMClient.ConversationType.DISCUSSION == message.getConversationType()) {
                    if (message.getUserInfo() != null) {
                        onPortaitClick(message.getUserInfo().getUserId());
                    }
                }
            }
        });

        Log.d("message.getMessageDirection():", "" + message.getMessageDirection().getValue());

        // 设置头像 begin
        if (message.getMessageDirection() == MessageDirection.SEND) {

            Resource resource = null;
            UIUserInfo userInfoSend = message.getUserInfo();

            if (userInfoSend == null || TextUtils.isEmpty(userInfoSend.getName())) {

                if (mUserInfo != null) {
                    resource = mUserInfo.getPortraitResource();
                } else {

                    if (mOnGetDataListener != null) {
                        mOnGetDataListener.getUserInfo(position, message.getSenderUserId(), message.getMessageId());
                    }
                }

            } else {
                mUserInfo = userInfoSend;

                if (userInfoSend != null && userInfoSend.getPortraitResource() != null) {
                    resource = userInfoSend.getPortraitResource();
                }
            }

            if (resource != null) {
                selfPortrait.setResource(resource);
            }

        } else if (message.getMessageDirection() == MessageDirection.RECEIVE) {

            UIUserInfo userInfoTarget = message.getUserInfo();
            Resource resource = null;

            if (RongIMClient.ConversationType.PRIVATE != message.getConversationType()) {
                userNameLeftTextView.setVisibility(View.VISIBLE);
            }

            if (userInfoTarget != null && !TextUtils.isEmpty(userInfoTarget.getName())) {
                resource = userInfoTarget.getPortraitResource();
                userNameLeftTextView.setText(userInfoTarget.getName());
                Log.d("TextItemViewProvider", "Name:" + userInfoTarget.getName());
                Log.d("TextItemViewProvider", "Portrait:" + userInfoTarget.getPortraitResource().getUri().getPath());
            } else {


                if (mOnGetDataListener != null) {
                    mOnGetDataListener.getUserInfo(position, userInfoTarget.getUserId(), message.getMessageId());
                }

            }

            if (resource != null) {
                otherPortrait.setResource(resource);
            }
        }
        // 设置头像 end

        messageRightContent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onMessageClick(message);
            }
        });

        messageRightContent.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                if (mOnMessageItemClickListener != null) {
                    mOnMessageItemClickListener.onMessageLongClick(message);
                }

                return true;
            }
        });


        messageLeftContent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onMessageClick(message);
            }
        });

        messageLeftContent.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                if (mOnMessageItemClickListener != null) {
                    mOnMessageItemClickListener.onMessageLongClick(message);
                }
                return true;
            }
        });

        sendFailedImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mOnMessageItemClickListener != null) {
                    mOnMessageItemClickListener.onResendMessage(message, position);
                }
            }
        });

        if (message.getMessageDirection() == MessageDirection.RECEIVE) {

            textRightLayout.setVisibility(View.GONE);
            textLeftLayout.setVisibility(View.VISIBLE);

            TextMessage textMessage = null;

            if (message.getContent() instanceof TextMessage) {
                textMessage = (TextMessage) message.getContent();
                String str = HTMLDecoder.decode(HTMLDecoder.decode(textMessage.getContent()));
                messageLeftContent.setText(Util.highLight(HighLightUtils.loadHighLight(str)));
            }

            sendFailedImageView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            userNameRightTextView.setVisibility(View.GONE);
            otherPortrait.setVisibility(View.VISIBLE);
            selfPortrait.setVisibility(View.GONE);


        } else {// 自己的消息布局

            textRightLayout.setVisibility(View.VISIBLE);
            textLeftLayout.setVisibility(View.GONE);

            if (message.getContent() instanceof TextMessage) {
                messageRightContent.setText(message.getTextMessageContent());
            }

            userNameRightTextView.setVisibility(View.GONE);
            selfPortrait.setVisibility(View.VISIBLE);
            otherPortrait.setVisibility(View.INVISIBLE);

            Log.d("TextItemViewProvider", message.getSentStatus() + "||" + message.isSending());
            sendFailedImageView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);

            if (message.isSending()) {
                sendFailedImageView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            } else if (SentStatus.FAILED == message.getSentStatus() || (SentStatus.SENDING == message.getSentStatus() && !message.isSending())) {
                Log.d("TextItemViewProvider", "failure.........");
                sendFailedImageView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            } else if (SentStatus.SENT == message.getSentStatus()) {
                sendFailedImageView.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }


        }

        return convertView;
    }
}
