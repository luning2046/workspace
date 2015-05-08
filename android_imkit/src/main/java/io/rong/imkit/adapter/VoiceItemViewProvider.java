package io.rong.imkit.adapter;

import io.rong.imkit.RCloudContext;
import io.rong.imkit.common.MessageContext;
import io.rong.imkit.model.RCloudType;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.model.UIUserInfo;
import io.rong.imkit.utils.RCDateUtils;
import io.rong.imkit.utils.ResourceUtils;
import io.rong.imkit.veiw.AsyncImageView;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.MessageDirection;
import io.rong.imlib.RongIMClient.SentStatus;
import io.rong.imlib.RongIMClient.UserInfo;
import io.rong.imlib.RongIMClient.VoiceMessage;

import java.util.Date;
import java.util.List;

import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sea_monster.core.resource.model.Resource;

public class VoiceItemViewProvider extends BaseViewProvider {

    private UIUserInfo mUserInfo = null;

    public VoiceItemViewProvider() {
    }

    public VoiceItemViewProvider(MessageContext context) {
        super(context);
    }

    @Override
    public int setItemLayoutRes() {
        return ResourceUtils.getLayoutResourceId(mContext, "rc_item_voice_conversation");
    }

    @Override
    public View getItemView(final View convertView, final ViewHolder holder, RCloudType data, final int position, List datas) {

        TextView messageTimeTV = holder.obtainView(convertView, "conversation_message_time_tv");
        AsyncImageView otherPortrait = holder.obtainView(convertView, "conversation_message_other_portrait");
        TextView userNameRightTextView = holder.obtainView(convertView, "conversation_message_username_right_tv");
        ImageView sendFailedImageView = holder.obtainView(convertView, "conversation_message_send_failed");

        ProgressBar progressBar = holder.obtainView(convertView, "conversation_message_progressbar");
        AsyncImageView selfPortrait = holder.obtainView(convertView, "conversation_message_self_portrait");
        LinearLayout textRightLayout = holder.obtainView(convertView, "text_right_layout_layout");
        LinearLayout textLeftLayout = holder.obtainView(convertView, "text_left_layout_layout");
        final ImageView messageLeftContent = holder.obtainView(convertView, "conversation_message_voice_left");
        final ImageView messageRightContent = holder.obtainView(convertView, "conversation_message_voice_right");
        TextView userNameLeftTextView = holder.obtainView(convertView, "conversation_message_username_left_tv");

        TextView messageRightLength = holder.obtainView(convertView, "conversation_message_right_voice_length");
        TextView messageLeftLength = holder.obtainView(convertView, "conversation_message_left_voice_length");
        FrameLayout messageLeftBubble = holder.obtainView(convertView, "left_message_bubble_layout");
        FrameLayout messageRightBubble = holder.obtainView(convertView, "right_message_bubble_layout");

        ImageView voiceStateView = holder.obtainView(convertView, "conversation_message_left_voice_unread");

        final UIMessage message = (UIMessage) data;
        message.setPositionInList(position);

        // 时间处理 begin
        long currentTime = 0;

        final VoiceMessage voiceMessage = (VoiceMessage) message.getContent();

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

//            selfPortrait.setDefaultDrawable(ResourceUtils.getDrawableById(mContext, "rc_default_portrait"));


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

            if (voiceMessage.getUri().equals(mContext.getVoiceHandler().getCurrentPlayUri())) {

                AnimationDrawable drawable = (AnimationDrawable) ResourceUtils.getDrawableById(mContext, "rc_send_voice_anim");
                messageRightContent.setImageDrawable(drawable);

                messageRightContent.post(new Runnable() {
                    @Override
                    public void run() {
                        if (messageRightContent.getDrawable() instanceof AnimationDrawable)
                            ((AnimationDrawable) messageRightContent.getDrawable()).start();
                    }
                });

            } else {
                messageRightContent.setImageDrawable(ResourceUtils.getDrawableById(mContext, "rc_voice_icon_right"));
            }

        } else if (message.getMessageDirection() == MessageDirection.RECEIVE) {

            if (RongIMClient.ConversationType.PRIVATE != message.getConversationType()) {
                userNameLeftTextView.setVisibility(View.VISIBLE);
            }

            UIUserInfo userInfoTarget = message.getUserInfo();
            Resource resource = null;


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

            if (voiceMessage.getUri().equals(mContext.getVoiceHandler().getCurrentPlayUri())) {

                AnimationDrawable drawable = (AnimationDrawable) ResourceUtils.getDrawableById(mContext, "rc_receive_voice_anim");
                messageLeftContent.setImageDrawable(drawable);

                messageLeftBubble.post(new Runnable() {
                    @Override
                    public void run() {
                        if (messageLeftContent.getDrawable() instanceof AnimationDrawable)
                            ((AnimationDrawable) messageLeftContent.getDrawable()).start();
                    }
                });

            } else
                messageLeftContent.setImageDrawable(ResourceUtils.getDrawableById(mContext, "rc_voice_icon_left"));
        }
        // 设置头像 end

        messageRightBubble.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mOnMessageItemClickListener != null) {
                    mOnMessageItemClickListener.onMessageClick(message, messageRightContent);
                }
                onMessageClick(message);
            }
        });

        messageRightBubble.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnMessageItemClickListener != null) {
                    mOnMessageItemClickListener.onMessageLongClick(message);
                }
                return true;
            }
        });

        messageLeftBubble.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mOnMessageItemClickListener != null) {
                    mOnMessageItemClickListener.onMessageClick(message, messageLeftContent);
                }
                onMessageClick(message);
            }

        });
        messageLeftBubble.setOnLongClickListener(new OnLongClickListener() {
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

        if (message.getMessageDirection() == MessageDirection.RECEIVE) {// 对方消息布局
            // if (message.isMessageDirection()) {

            textRightLayout.setVisibility(View.GONE);
            textLeftLayout.setVisibility(View.VISIBLE);

            // messageLeftContent.setImageBitmap(BitmapUtils.getBitmapFromBase64(message.getContent()));

            sendFailedImageView.setVisibility(View.GONE);
            progressBar.setVisibility(View.INVISIBLE);
            userNameRightTextView.setVisibility(View.GONE);
            otherPortrait.setVisibility(View.VISIBLE);
            selfPortrait.setVisibility(View.INVISIBLE);

            String voiceLengthFormat = mContext.getString(ResourceUtils.getStringResourceId(mContext, "voice_length"));

            if (message.getContent() instanceof VoiceMessage) {
                messageLeftLength.setText(String.format(voiceLengthFormat, voiceMessage.getDuration()));

                int width = (int) mContext.getResources().getDimension(ResourceUtils.getDimenResourceId(mContext, getDimenStr(voiceMessage.getDuration())));
                Log.d("voice", "voice----left---width:" + width);
                messageLeftBubble.getLayoutParams().width = width;

                // messageLeftBubble.getLayoutParams().width = (int)
                // mContext.getResources().getDimension(
                // ResourceUtils.getDimenResourceId(mContext,
                // getDimenStr(voiceMessage.getDuration())));
            }

            if (message.getReceivedStatus().isListened()) {
                voiceStateView.setVisibility(View.GONE);
            } else {
                voiceStateView.setVisibility(View.VISIBLE);
            }

            // otherPortrait.setImageResource(FCloudContext.getInstance().getUserPortrait(message.getSenderUserId()));

        } else if (message.getMessageDirection() == MessageDirection.SEND) { // 自己的消息布局

            textRightLayout.setVisibility(View.VISIBLE);
            textLeftLayout.setVisibility(View.GONE);

            // messageRightContent.setImageBitmap(BitmapUtils.getBitmapFromBase64(message.getContent()));

            userNameLeftTextView.setVisibility(View.GONE);
            selfPortrait.setVisibility(View.VISIBLE);
            otherPortrait.setVisibility(View.INVISIBLE);

            if (SentStatus.SENT == message.getSentStatus()) {
                sendFailedImageView.setVisibility(View.GONE);
                progressBar.setVisibility(View.INVISIBLE);
            } else if (SentStatus.FAILED == message.getSentStatus()) {
                sendFailedImageView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            } else if (SentStatus.SENDING == message.getSentStatus()) {
                progressBar.setVisibility(View.VISIBLE);
                sendFailedImageView.setVisibility(View.GONE);
            }

            String voiceLengthFormat = mContext.getString(ResourceUtils.getStringResourceId(mContext, "voice_length"));

            if (message.getContent() instanceof VoiceMessage) {
                messageRightLength.setText(String.format(voiceLengthFormat, voiceMessage.getDuration()));

                int width = (int) mContext.getResources().getDimension(ResourceUtils.getDimenResourceId(mContext, getDimenStr(voiceMessage.getDuration())));
                Log.d("voice", "voice---width:" + width + "|getDuration:" + voiceMessage.getDuration());
                messageRightBubble.getLayoutParams().width = width;

            }
        }

        return convertView;
    }

    private final String getDimenStr(long voiceLenght) {

        int totleLength = 400;
        int baseLength = 120;

        int actulTotaleLength = totleLength - baseLength;
        int lenght = (int) voiceLenght * (actulTotaleLength / 60);
        return String.format("px_to_dip_%1$s", baseLength + lenght);
    }

}
