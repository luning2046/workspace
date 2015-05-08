package io.rong.imkit.veiw;

import io.rong.imkit.utils.ResourceUtils;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 *  默认状态的   消息输入框
 */
public class MessageBar extends BaseFrameLayout implements View.OnClickListener {

    public static int MESSAGE_BAR_STATE_TEXT = 1;
    public static int MESSAGE_BAR_STATE_VOICE = 2;

    public MessageBar(Context context) {
        super(context);
        initView();
    }

    public MessageBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MessageBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private ImageView mChangeImageView;//  录音/键盘   切换 按钮
    private MentionsEditText mMessageEditText;//中间的 EditText 输入框 
    private ImageView mAddImageView;//加号  按钮
    private Button mSendButton;//发送消息按钮
    private Button mVoiceButton;//===========================按住 按钮  录音按钮=====================================


    private ImageView mExpressionImageView;//笑脸 按钮

    private LinearLayout mEditLayout;//输入框按钮的容器

    private MessageBarListener mMessageBarListener;

    private int mCurrentState = MESSAGE_BAR_STATE_TEXT;

    public void initView() {
        LayoutInflater.from(getContext()).inflate(ResourceUtils.getLayoutResourceId(getContext(), "rc_view_message_bar"), this);

        mChangeImageView = getView(this, "conversation_message_bar_change_iv");
        mMessageEditText = getView(this, "conversation_message_bar_edit");
        mAddImageView = getView(this, "conversation_message_bar_add_iv");
        mSendButton = getView(this, "conversation_message_bar_send_btn");
        mVoiceButton = getView(this, "conversation_message_bar_voice_btn");
        mExpressionImageView = getView(this, "conversation_message_expression");
        mEditLayout = getView(this, "conversation_message_edit_layout");

        //==================当输入框中，输入内容后，将”发送“按钮显示出来======================
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() > 0) {
                    mSendButton.setVisibility(View.VISIBLE);
                    mAddImageView.setVisibility(View.GONE);
                } else {
                    mSendButton.setVisibility(View.GONE);
                    mAddImageView.setVisibility(View.VISIBLE);
                }
            }
        });

        //==================当输入框获得焦点后，切换界面，弹出”软键盘“===============================
        mMessageEditText.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d("messageBar", "onFocusChange--hasFocus:" + hasFocus);

                if (mMessageBarListener != null) {
                    mMessageBarListener.onEditTextClick();
                }

            }
        });

        mAddImageView.setOnClickListener(this);
        mSendButton.setOnClickListener(this);
        mExpressionImageView.setOnClickListener(this);
        mChangeImageView.setOnClickListener(this);
        mMessageEditText.setOnClickListener(this);

    }

    enum ChangeState {
        TEXT, VOICE
    }

    public interface MessageBarListener {

        public void onChangeIVClick(int state);

        public void onAddIVClick();

        public void onSendBtnClick();

        public void onExpressionClick();

        public void onEditTextClick();
    }

    @Override
    public void onClick(View v) {

        if (mMessageBarListener == null)
            return;

        if (v == mChangeImageView) {

            if (mCurrentState == MESSAGE_BAR_STATE_TEXT) {//根据当前按钮的状态   更新控件的UI     

                mEditLayout.setVisibility(View.GONE);
                mVoiceButton.setVisibility(View.VISIBLE);
                mChangeImageView.setImageDrawable(ResourceUtils.getDrawableById(getContext(), "rc_message_bar_keyboard"));
                mCurrentState = MESSAGE_BAR_STATE_VOICE;
                mMessageBarListener.onChangeIVClick(mCurrentState);

                mAddImageView.setVisibility(View.VISIBLE);
                mSendButton.setVisibility(View.GONE);
            } else if (mCurrentState == MESSAGE_BAR_STATE_VOICE) {
                mEditLayout.setVisibility(View.VISIBLE);
                mVoiceButton.setVisibility(View.GONE);
                mChangeImageView.setImageDrawable(ResourceUtils.getDrawableById(getContext(), "rc_message_bar_vioce_icon"));
                mCurrentState = MESSAGE_BAR_STATE_TEXT;


                if (mMessageEditText.getText().toString().trim().length() > 0) {
                    mAddImageView.setVisibility(View.GONE);
                    mSendButton.setVisibility(View.VISIBLE);
                    mMessageBarListener.onChangeIVClick(MESSAGE_BAR_STATE_VOICE);
                } else {
                    mMessageBarListener.onChangeIVClick(mCurrentState);
                }

            }


        } else if (v == mAddImageView) {
            mMessageBarListener.onAddIVClick();
            mEditLayout.setVisibility(View.VISIBLE);
            mVoiceButton.setVisibility(View.GONE);

            if (mMessageEditText.getText().toString().trim().length() > 0) {
                mAddImageView.setVisibility(View.GONE);
                mSendButton.setVisibility(View.VISIBLE);
            }

        } else if (v == mSendButton) {
            mMessageBarListener.onSendBtnClick();
        } else if (v == mExpressionImageView) {
            mMessageBarListener.onExpressionClick();
        } else if (v == mMessageEditText) {
            mMessageBarListener.onEditTextClick();

        }

    }

    public void setMessageBarListener(MessageBarListener mMessageBarListener) {
        this.mMessageBarListener = mMessageBarListener;
    }

    public ImageView getChangeImageView() {
        return mChangeImageView;
    }

    public MentionsEditText getMessageEditText() {
        return mMessageEditText;
    }

    public ImageView getAddImageView() {
        return mAddImageView;
    }

    public Button getSendButton() {
        return mSendButton;
    }

    public ImageView getExpressionImageView() {
        return mExpressionImageView;
    }

}
