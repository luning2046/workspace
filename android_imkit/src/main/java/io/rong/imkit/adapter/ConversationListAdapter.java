package io.rong.imkit.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.Date;
import io.rong.imkit.RCloudContext;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.model.UIUserInfo;
import io.rong.imkit.utils.HighLightUtils;
import io.rong.imkit.utils.RCDateUtils;
import io.rong.imkit.utils.ResourceUtils;
import io.rong.imkit.utils.Util;
import io.rong.imkit.veiw.AsyncImageView;
import io.rong.imlib.RongIMClient.ConversationType;
import io.rong.imlib.RongIMClient.ImageMessage;
import io.rong.imlib.RongIMClient.SentStatus;
import io.rong.imlib.RongIMClient.TextMessage;
import io.rong.imlib.RongIMClient.VoiceMessage;


/**
 * Created by zhjchen on 14-4-16.
 */
public class ConversationListAdapter extends BaseAdapter<UIConversation> {

    private OnGetDataListener mOnGetDataListener;

    public ConversationListAdapter(Context context) {
        super(context);
    }


    public int setItemLayoutRes() {
        return ResourceUtils.getLayoutResourceId(mContext, "rc_item_conversationlist");
    }

    class ViewHodler {
        View layout;//整个item的顶层容器
        AsyncImageView icon;//列表头像
        TextView message;//消息数量
        TextView username;//用户名
        TextView time;//最后一条消息  发送时间
        TextView content;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHodler hodler = null;

        if (convertView == null || convertView.getTag() == null) {
            View view = LayoutInflater.from(mContext).inflate(ResourceUtils.getLayoutResourceId(mContext, "rc_item_conversationlist"), null);
            hodler = new ViewHodler();
            hodler.layout = view.findViewById(android.R.id.widget_frame);//整个item的顶层容器
            hodler.icon = (AsyncImageView) view.findViewById(android.R.id.icon);
            hodler.message = (TextView) view.findViewById(ResourceUtils.getIDResourceId(mContext, "rc_new_message"));//消息数量
            hodler.username = (TextView) view.findViewById(android.R.id.text1);//用户名
            hodler.time = (TextView) view.findViewById(android.R.id.text2);//最后一条消息  发送时间
            hodler.content = (TextView) view.findViewById(android.R.id.message);//显示内容
            hodler.icon.setDefaultDrawable(ResourceUtils.getDrawableById(mContext, "rc_default_portrait"));//头像
            convertView = view;
            convertView.setTag(hodler);
        } else {
            hodler = (ViewHodler) convertView.getTag();
            hodler.username.setText("");
            hodler.time.setText("");
            hodler.content.setText("");
            hodler.content.setCompoundDrawables(null, null, null, null);
            hodler.icon.setResource(null);
        }

//=================dataSet数据集=========================================
UIConversation conversation = dataSet.get(position);

        if (conversation != null) {

            
//=====设置未读消息数 begin
            int unReadCont = conversation.getUnreadMessageCount();
            String moreMsgFlag = null;
            if (unReadCont > 0) {
                if (unReadCont > 99) {
                    moreMsgFlag = ResourceUtils.getStringResource(mContext, "new_message_more");
                } else {
                    moreMsgFlag = String.valueOf(unReadCont);
                }
                hodler.message.setVisibility(View.VISIBLE);
                hodler.message.setText(moreMsgFlag);
            } else {
                hodler.message.setVisibility(View.GONE);
            }
//=====设置未读消息数 end
            
//将置顶item的背景为指定的颜色begin
            if (conversation.isTop()) {
                hodler.layout.setBackgroundColor(ResourceUtils.getColorByResId(mContext, "rc_list_item_istop_bg_color"));
            } else {
                hodler.layout.setBackgroundColor(Color.TRANSPARENT);
            }
//将置顶item的背景为指定的颜色end            

//===========================================================二人会话==================设置用户名，头像===============================================================================
            if (conversation.getConversationType() == ConversationType.PRIVATE) {//二人会话

                if (conversation.getUserInfo() == null) {

                    if (mOnGetDataListener != null) {
//=========================调用这个方法后，一条用户信息放入UIConversation中=======忘conversation对象中放入一个User的用户信息========getUserInfo方法在ConversationListFragment重写=====================================
mOnGetDataListener.getUserInfo(position, conversation.getTargetId());//第二个参数targetId就是对方用户的id
                    }
                    hodler.icon.setResource(null);
                } else {

                    UIUserInfo userInfo = conversation.getUserInfo();

                    if (userInfo != null) {
                        hodler.username.setText(userInfo.getName());

                        if (userInfo.getPortraitResource() != null) {
                            hodler.icon.setResource(userInfo.getPortraitResource());
                        } else {
                            hodler.icon.setResource(null);
                        }
                    } else {
                        hodler.icon.setResource(null);
                    }
                }
//===========================================================讨论组================设置用户名，头像=================================================================================
            } else if (conversation.getConversationType() == ConversationType.DISCUSSION) {//讨论组

                hodler.icon.setImageDrawable(ResourceUtils.getDrawableById(mContext, "rc_default_discussion_portrait"));

                if (conversation.getUiDiscussion() == null && mOnGetDataListener != null) {
                    Log.d("ConversationListAdapter", "conversation.getUiDiscussion()==null&&mOnGetDataListener!=null-----");
                    mOnGetDataListener.getDiscussionInfo(position, conversation.getTargetId());
                } else {

                    if (conversation.getUiDiscussion() != null && !TextUtils.isEmpty(conversation.getUiDiscussion().getName())) {
                        conversation.setConversationTitle(conversation.getUiDiscussion().getName());
                    }
                }

                if (!TextUtils.isEmpty(conversation.getConversationTitle())) {
                    hodler.username.setText(conversation.getConversationTitle());
                } else {
                    hodler.username.setText(ResourceUtils.getStringResource(mContext, "default_discussion_name"));
                }

                if (conversation.getLatestMessage() == null && conversation.getReceivedTime() == 0) {//创建讨论组时给个默认时间
                    conversation.setReceivedTime(System.currentTimeMillis());
                }


            } else {
                hodler.username.setText("");
                hodler.icon.setResource(null);
            }
        }

        if (!TextUtils.isEmpty(conversation.getDraft())) {
            SpannableStringBuilder spannable = new SpannableStringBuilder(ResourceUtils.getStringResource(getContext(), "message_type_draft_content"));
            hodler.content.setText(spannable.append(Util.highLight(HighLightUtils.loadHighLight(conversation.getDraft()))));
        } else if (conversation.getLatestMessage() != null) {

            if (conversation.getLatestMessage() instanceof TextMessage) {
                hodler.content.setText(conversation.getTextMessageContent());
            } else if (conversation.getLatestMessage() instanceof ImageMessage) {
                hodler.content.setText(ResourceUtils.getStringResource(mContext, "message_type_image_content"));
            } else if (conversation.getLatestMessage() instanceof VoiceMessage) {
                hodler.content.setText(ResourceUtils.getStringResource(mContext, "message_type_voice_content"));
            }

            if (conversation.getSenderUserId() != null && conversation.getSenderUserId().equals(RCloudContext.getInstance().getRongIMClient().getCurrentUserInfo().getUserId()) && TextUtils.isEmpty(conversation.getDraft())) {

                if (conversation.getSentStatus() == SentStatus.FAILED) {// || conversation.getSentStatus() == SentStatus.SENDING) {

                    if (conversation.getLatestMessage() instanceof TextMessage) {
                        int width = (int) mContext.getResources().getDimension(ResourceUtils.getDimenResourceId(mContext, "px_to_dip_26"));
                        Drawable drawable = ResourceUtils.getDrawableById(mContext, "rc_conversation_list_msg_send_failure");
                        drawable.setBounds(0, 0, width, width);
                        hodler.content.setCompoundDrawables(drawable, null, null, null);
                    }
                }
            }
        } else {
            hodler.content.setCompoundDrawables(null, null, null, null);
            hodler.content.setText("");
        }


        Log.d("ConversationListAdapter", "conversation.getReceivedTime()-----" + conversation.getReceivedTime());

        if (conversation.getReceivedTime() > 0) {
            String time = RCDateUtils.getConvastionListFromatDate(new Date(conversation.getReceivedTime()));
            hodler.time.setText(time);
        } else {
            hodler.time.setText("");
        }


        return convertView;
    }

    public void setOnGetDataListener(OnGetDataListener mOnGetDataListener) {
        this.mOnGetDataListener = mOnGetDataListener;
    }


    public interface OnGetDataListener {
        public void getDiscussionInfo(int position, String discusstionId);

        public void getUserInfo(int position, String targetId);
    }


}
