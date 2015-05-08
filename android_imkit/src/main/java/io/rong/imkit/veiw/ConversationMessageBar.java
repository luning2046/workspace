package io.rong.imkit.veiw;

import io.rong.imkit.utils.ResourceUtils;
import io.rong.imkit.veiw.gif.EmotionParser;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 *  完整的消息输入框控件，包含“单击 加号 按钮”弹出的 完整界面 
 *  				“弹出的笑脸列表页”
 *  				“弹出的启动voip等按钮”
 */
public class ConversationMessageBar extends BaseFrameLayout implements MessageBar.MessageBarListener, EmotionAdapter.OnEmotionItemClickListener, View.OnClickListener {

    public ConversationMessageBar(Context context) {
        super(context);
        initView();
    }

    public ConversationMessageBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ConversationMessageBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();

    }

    private LinearLayout mExpressionLayout;//弹出的笑脸列表页  容器
    private RelativeLayout mRichTextLayout;//弹出的启动voip等按钮   容器
    private MessageBar messageBar;//默认状态的   消息输入框
    private EmotionView mEmotionView;//弹出的笑脸列表页
    private RichIconTextView mImageRichIconTextView;
    private RichIconTextView mCameraRichIconTextView;
    private RichIconTextView mVOIPRichIconTextView;

    private int mCurrentState = MessageBar.MESSAGE_BAR_STATE_TEXT;

    private ConversationMessageBarLinstener conversationMessageBarLinstener;

    private void initView() {

        LayoutInflater.from(getContext()).inflate(ResourceUtils.getLayoutResourceId(getContext(), "rc_conversation_message_bar"), this);

        messageBar = getView(this, "conversation_message_bar_message_bar");
        mExpressionLayout = getView(this, "conversation_message_bar_expression_layout");
        mRichTextLayout = getView(this, "conversation_message_bar_rich_text_layout");
        mEmotionView = getView(this, "conversation_message_bar_emotion_view");
mEmotionView.setOnEmotionItemClickListener(this);
        messageBar.setMessageBarListener(this);

        mImageRichIconTextView = getView(this, "imagetRichIcon");
        mCameraRichIconTextView = getView(this, "camertRichIcon");
        mVOIPRichIconTextView = getView(this, "voipRichIcon");

        ImageView imageView = mImageRichIconTextView.getIconView();
        imageView.setBackgroundResource(ResourceUtils.getDrawableResourceId(getContext(), "rc_ic_pic"));
        mImageRichIconTextView.getNameView().setText(getResourceStringID("rich_icon_image_txt"));

        ImageView cameraView = mCameraRichIconTextView.getIconView();
        cameraView.setBackgroundResource(ResourceUtils.getDrawableResourceId(getContext(), "rc_ic_camera"));
        mCameraRichIconTextView.getNameView().setText(getResourceStringID("rich_icon_take_photo_txt"));

        ImageView voipView = mVOIPRichIconTextView.getIconView();
        voipView.setBackgroundResource(ResourceUtils.getDrawableResourceId(getContext(), "rc_ic_phone"));
        mVOIPRichIconTextView.getNameView().setText(getResourceStringID("rich_icon_phone_txt"));

        mImageRichIconTextView.setOnClickListener(this);
        mCameraRichIconTextView.setOnClickListener(this);
        mVOIPRichIconTextView.setOnClickListener(this);

    }

    // 点击“录音/键盘切换”按钮触发的事件         ====================                           state 是切换过来的状态
    @Override
    public void onChangeIVClick(int state) {

        if (conversationMessageBarLinstener == null) {
            return;
        }

        mCurrentState = state;

        Log.d("conversationMessageBar", "onChangeIVclick:" + state);

        if (state == MessageBar.MESSAGE_BAR_STATE_TEXT) {//当前”键盘“状态

            if (mExpressionLayout.getVisibility() == View.VISIBLE || mRichTextLayout.getVisibility() == View.VISIBLE) {
                toggleInputMethod(false, messageBar.getMessageEditText());
            } else {
                mExpressionLayout.setVisibility(View.GONE);
                mRichTextLayout.setVisibility(View.GONE);
                toggleInputMethod(true, messageBar.getMessageEditText());
            }

        } else if (state == MessageBar.MESSAGE_BAR_STATE_VOICE) {

            toggleInputMethod(false, messageBar.getMessageEditText());
            mExpressionLayout.setVisibility(View.GONE);
            mRichTextLayout.setVisibility(View.GONE);
        }

        messageBar.getExpressionImageView().setBackgroundDrawable(ResourceUtils.getDrawableById(getContext(),"rc_smiley_selector"));

    }

    //开关软键盘的回调
    private final void toggleInputMethod(boolean isOpen, EditText editText) {

        if (isOpen) {
            conversationMessageBarLinstener.toggleInputMethod(isOpen, editText);
            editText.requestFocus();
        } else {
            conversationMessageBarLinstener.toggleInputMethod(isOpen, editText);
        }
    }
    //====================单击“加号  按钮”触发的事件=================================
    @Override
    public void onAddIVClick() {

        mExpressionLayout.setVisibility(View.GONE);

        if (mRichTextLayout.getVisibility() == View.VISIBLE) {
            toggleInputMethod(true, messageBar.getMessageEditText());
            mRichTextLayout.setVisibility(View.GONE);
        } else {
            toggleInputMethod(false, messageBar.getMessageEditText());
            getHandler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    mRichTextLayout.setVisibility(View.VISIBLE);
                    mOnRichIconTextViewClickLinstener.isVisibility();
                }
            }, 500);
        }

    }
    //===============单击“发送”按钮的触发事件================================================
    @Override
    public void onSendBtnClick() {
        String message = getMessageEditString();
        conversationMessageBarLinstener.sendMessage(message);
        messageBar.getMessageEditText().clearStr();
    }
    //================单击“笑脸”按钮的触发事件=====================================
    @Override
    public void onExpressionClick() {

        mRichTextLayout.setVisibility(View.GONE);

        if (mExpressionLayout.getVisibility() == View.GONE) {
            toggleInputMethod(false, messageBar.getMessageEditText());
            getHandler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    mExpressionLayout.setVisibility(View.VISIBLE);
                    mOnRichIconTextViewClickLinstener.isVisibility();
                }
            }, 500);

            messageBar.getExpressionImageView().setBackgroundDrawable(ResourceUtils.getDrawableById(getContext(), "rc_smiley_hover"));

        } else {
            mExpressionLayout.setVisibility(View.GONE);
            toggleInputMethod(true, messageBar.getMessageEditText());
            messageBar.getExpressionImageView().setBackgroundDrawable(ResourceUtils.getDrawableById(getContext(),"rc_smiley_selector"));
        }
    }

    public String getMessageEditString() {
        return messageBar.getMessageEditText().getRealString();
    }
//=====================单击“中间的 EditText 输入框“触发事件====================================== 
    @Override
    public void onEditTextClick() {
        Log.d("onEditTextClick", "onEditTextClick-------");
        mExpressionLayout.setVisibility(View.GONE);
        mRichTextLayout.setVisibility(View.GONE);
        messageBar.getExpressionImageView().setBackgroundDrawable(ResourceUtils.getDrawableById(getContext(), "rc_smiley_selector"));
        mOnRichIconTextViewClickLinstener.isVisibility();
    }

    public void setHiddenRichAndExpressionView() {
        mExpressionLayout.setVisibility(View.GONE);
        mRichTextLayout.setVisibility(View.GONE);
    }

    public boolean isShowRichOrExpressionView() {

        if (mExpressionLayout.getVisibility() == View.VISIBLE || mRichTextLayout.getVisibility() == View.VISIBLE) {
            return true;
        }

        return false;

    }
//========================EmotionAdapter.OnEmotionItemClickListener的回调方法=========================================================
    @Override
    public void onEmotionClick(Drawable drawable, String code, int flag) {

        MentionsEditText mentionsEditText = messageBar.getMessageEditText();

        if (flag == EmotionAdapter.FLAG_DELETE) {
            mentionsEditText.deleteChar();
        } else if (flag == EmotionAdapter.FLAG_PNG) {
            Drawable d = EmotionParser.getInstance(getContext()).getSmileDrawable(code);
            mentionsEditText.insertImage(code, d);
        }
    }

    public interface ConversationMessageBarLinstener {
        public void toggleInputMethod(boolean isOpen, View view);

        public void sendMessage(String msg);

    }

    public void setConversationMessageBarLinstener(ConversationMessageBarLinstener conversationMessageBarLinstener) {
        this.conversationMessageBarLinstener = conversationMessageBarLinstener;
    }

    private final int getResourceColorID(String resId) {
        return getContext().getResources().getColor(ResourceUtils.getColorResourceId(getContext(), resId));
    }

    private final int getResourceStringID(String resId) {
        return ResourceUtils.getStringResourceId(getContext(), resId);
    }

    @Override
    public void onClick(View v) {

        if (v != null && mOnRichIconTextViewClickLinstener != null) {

            if (v == mImageRichIconTextView) {
                mOnRichIconTextViewClickLinstener.onImageRichTextClick();
            } else if (v == mCameraRichIconTextView) {
                mOnRichIconTextViewClickLinstener.onCameraRichTextClick();
            } else if (v == mVOIPRichIconTextView) {
                mOnRichIconTextViewClickLinstener.onVoipRichTextClick();
            }

        }

    }

    private OnRichIconTextViewClickLinstener mOnRichIconTextViewClickLinstener;

    public void setOnRichIconTextViewClickLinstener(OnRichIconTextViewClickLinstener onRichIconTextViewClickLinstener) {
        mOnRichIconTextViewClickLinstener = onRichIconTextViewClickLinstener;
    }

    public interface OnRichIconTextViewClickLinstener {

        public void onImageRichTextClick();

        public void onCameraRichTextClick();

        public void onLocatonRichTextClick();

        public void onVoipRichTextClick();

        public void onAddRichTextClick();

        public void isVisibility();

    }


    public void setRichOrExpressionViewVisibility() {
        mRichTextLayout.setVisibility(View.GONE);
        mExpressionLayout.setVisibility(View.GONE);
    }


    public void setMessageEditText(CharSequence msg){
        messageBar.getMessageEditText().setText(msg);
    }


    public RichIconTextView getVOIPRichIconTextView() {
        return mVOIPRichIconTextView;
    }

    public void setVOIPRichIconTextView(RichIconTextView mVOIPRichIconTextView) {
        this.mVOIPRichIconTextView = mVOIPRichIconTextView;
    }

}
