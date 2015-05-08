package io.rong.imkit.adapter;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sea_monster.core.resource.model.Resource;

import java.util.Date;
import java.util.List;

import io.rong.imkit.RCloudContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.common.MessageContext;
import io.rong.imkit.model.RCloudType;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.model.UIUserInfo;
import io.rong.imkit.utils.RCDateUtils;
import io.rong.imkit.utils.ResourceUtils;
import io.rong.imkit.veiw.AsyncImageView;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ImageMessage;
import io.rong.imlib.RongIMClient.MessageDirection;
import io.rong.imlib.RongIMClient.SentStatus;
import io.rong.imlib.RongIMClient.UserInfo;

public class ImageItemViewProvider extends BaseViewProvider {

    private UIUserInfo mUserInfo = null;

    public ImageItemViewProvider() {
    }

    public ImageItemViewProvider(MessageContext context) {
        super(context);
    }

    @Override
    public int setItemLayoutRes() {
        return ResourceUtils.getLayoutResourceId(mContext, "rc_item_image_conversation");
    }

    @Override
    public View getItemView(View convertView, ViewHolder holder, RCloudType data, final int position, List datas) {

        TextView messageTimeTV = holder.obtainView(convertView, "conversation_message_time_tv");
        AsyncImageView otherPortrait = holder.obtainView(convertView, "conversation_message_other_portrait");
        TextView userNameRightTextView = holder.obtainView(convertView, "conversation_message_username_right_tv");
        ImageView sendFailedImageView = holder.obtainView(convertView, "conversation_message_send_failed");
        ImageView messageRightContent = holder.obtainView(convertView, "conversation_message_right_tv");
        // ProgressBar progressBar = holder.obtainView(convertView,
        // "conversation_message_image_progressbar");
        AsyncImageView selfPortrait = holder.obtainView(convertView, "conversation_message_self_portrait");
        LinearLayout textRightLayout = holder.obtainView(convertView, "text_right_layout_layout");
        LinearLayout textLeftLayout = holder.obtainView(convertView, "text_left_layout_layout");
        TextView progressTextview = holder.obtainView(convertView, "progress_textview");

        TextView userNameLeftTextView = holder.obtainView(convertView, "conversation_message_username_left_tv");
        ImageView messageLeftContent = holder.obtainView(convertView, "conversation_message_left_tv");

        final UIMessage message = (UIMessage) data;
        message.setPositionInList(position);
        // messageTimeTV.setText(Util.dateToStr(new
        // Date(message.getReceivedTime())));
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
//                RCloudContext.getInstance().setUserInfo(user);
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

        // 设置头像 begin

        if (message.getMessageDirection() == MessageDirection.SEND) {

            Resource resource = null;

            UIUserInfo userInfoSend = message.getUserInfo();

            if (userInfoSend == null || TextUtils.isEmpty(userInfoSend.getName())) {

                if (mUserInfo != null) {
                    resource = mUserInfo.getPortraitResource();
                }else{

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
                Log.d("TextItemViewProvider", "11111:" + userInfoTarget.getName());
            } else {

                if (mOnGetDataListener != null) {
                    mOnGetDataListener.getUserInfo(position, message.getSenderUserId(), message.getMessageId());
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

                if (mOnMessageItemClickListener != null) {
                    mOnMessageItemClickListener.onMessageClick(message, null);
                }
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

                if (mOnMessageItemClickListener != null) {
                    mOnMessageItemClickListener.onMessageClick(message, null);
                }
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
        messageRightContent.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnMessageItemClickListener != null) {
                    mOnMessageItemClickListener.onMessageLongClick(message);
                }
                return true;
            }
        });

        // 对方消息布局
        if (message.getMessageDirection() == MessageDirection.RECEIVE) {

            textRightLayout.setVisibility(View.GONE);
            textLeftLayout.setVisibility(View.VISIBLE);
//==============================================在列表中设置图片的显示（压缩后的附件）=========================================================================
            if (message.getContent() != null) {
                ImageMessage imageMessage = (ImageMessage) message.getContent();
                messageLeftContent.setImageBitmap(imageMessage.getThumbnail());
            }

            sendFailedImageView.setVisibility(View.GONE);
            userNameRightTextView.setVisibility(View.GONE);
            otherPortrait.setVisibility(View.VISIBLE);
            selfPortrait.setVisibility(View.INVISIBLE);
            selfPortrait.setVisibility(View.INVISIBLE);
            progressTextview.setVisibility(View.GONE);

        } else if (message.getMessageDirection() == MessageDirection.SEND) {// 自己的消息布局

            if (SentStatus.SENT == message.getSentStatus()) {
                sendFailedImageView.setVisibility(View.GONE);
                progressTextview.setVisibility(View.INVISIBLE);
            } else if (SentStatus.FAILED == message.getSentStatus()) {
                sendFailedImageView.setVisibility(View.VISIBLE);
                progressTextview.setVisibility(View.INVISIBLE);
            } else if (SentStatus.SENDING == message.getSentStatus()) {
                progressTextview.setVisibility(View.VISIBLE);
                sendFailedImageView.setVisibility(View.GONE);
            }

            textRightLayout.setVisibility(View.VISIBLE);
            textLeftLayout.setVisibility(View.GONE);

            if (message.getProgressText() == 100 || message.getProgressText() == -1) {
                progressTextview.setVisibility(View.GONE);
            } else {
                Log.d("ImageItemViewProvider", "set progress----->");
                progressTextview.setVisibility(View.VISIBLE);
                progressTextview.setText(message.getProgressText() + "%");
            }

            if (message.getContent() != null) {
                ImageMessage imageMessage = (ImageMessage) message.getContent();
                messageRightContent.setImageBitmap(imageMessage.getThumbnail());
            }

            userNameLeftTextView.setVisibility(View.GONE);
            selfPortrait.setVisibility(View.VISIBLE);
            otherPortrait.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }
}
