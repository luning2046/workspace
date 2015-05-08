package io.rong.imkit.veiw;

import io.rong.imkit.common.IVoiceHandler;
import io.rong.imkit.utils.ResourceUtils;

import java.io.File;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 *  按住声音控件时，在屏幕中间显示“提示框” 
 */
public class VoiceCoverView extends FrameLayout implements Handler.Callback, CoverFrameLayout.CoverHandler {

    public static final int STATUS_REC = 1 + STATUS_OPEN;
    public static final int STATUS_CANCEL = 2 + STATUS_OPEN;


    private final int MSG_SEC = 1;
    private final int MSG_CANCEL = 2;
    private final int MSG_SAMPLING = 3;
    private final int MSG_READY = 4;


    private ImageView mIcon;
    private TextView mText, mMessage;
    private Handler mHandler;
    private int mStatus;
    private boolean mTrigger = false;
    private IVoiceHandler mVoiceHandler;

    public VoiceCoverView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, ResourceUtils.getLayoutResourceId(context,
                "rc_voice_cover"), this);

        mIcon = (ImageView) findViewById(android.R.id.icon);
        mText = (TextView) findViewById(android.R.id.text1);
        mMessage = (TextView) findViewById(android.R.id.message);

        mHandler = new Handler(this);
    }

    public void setVoiceHandler(IVoiceHandler voiceHandler) {
        mVoiceHandler = voiceHandler;
    }

    public void setStatus(int status) {

        if (mStatus == status)
            return;

        switch (status) {
            case STATUS_NORMAL:
                mText.setVisibility(View.GONE);
                setVisibility(View.GONE);
                break;
            case STATUS_OPEN:
                mHandler.sendEmptyMessageDelayed(MSG_READY, 300);
                break;
            case STATUS_CLOSE:
                mHandler.removeMessages(MSG_READY);
                mHandler.removeMessages(MSG_SEC);
                mHandler.removeMessages(MSG_CANCEL);
                mVoiceLength = SystemClock.elapsedRealtime() - mVoiceLength;
                if (mStatus == STATUS_REC)
                    mLastVoiceUri = mVoiceHandler.stopRec(true);
                else
                    mLastVoiceUri = mVoiceHandler.stopRec(false);

                mHandler.removeMessages(MSG_SAMPLING);
                setVisibility(View.GONE);
                break;
            case STATUS_REC:
                mMessage.setText(ResourceUtils.getStringResourceId(getContext(),
                        "voice_dialog_collect"));
                mIcon.setImageResource(ResourceUtils.getDrawableResourceId(
                        getContext(), "rc_volume_zero"));
                mMessage.setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));
                break;
            case STATUS_CANCEL:
                mMessage.setText(ResourceUtils.getStringResourceId(getContext(),
                        "voice_dialog_cancel_send"));
                mIcon.setImageResource(ResourceUtils.getDrawableResourceId(
                        getContext(), "rc_cancel_send_voice"));
                mMessage.setBackgroundColor(ResourceUtils.getColorByResId(getContext(),"rc_text_color_warning"));
                break;
        }
        mStatus = status;
    }

    public int getStatus() {
        return mStatus;
    }

    long mVoiceLength;
    Uri mLastVoiceUri;


    public Uri getLastVoiceUri() {
        return mLastVoiceUri;
    }

    public long getLastVoiceLength() {
        return mVoiceLength;
    }

    public void removeLastVoiceUri() {
        if (mLastVoiceUri != null) {
            File voiceFile = new File(mLastVoiceUri.getPath());
            if (voiceFile.exists())
                voiceFile.delete();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SEC:
                mText.setVisibility(View.VISIBLE);
                mText.setText(msg.arg1 + "s");
                if (msg.arg1 > 0)
                    mHandler.sendMessageDelayed(
                            mHandler.obtainMessage(MSG_SEC, --msg.arg1, 0), 1000);
                else {
                    mHandler.sendEmptyMessage(MSG_CANCEL);
                }
                break;
            case MSG_CANCEL:
                setStatus(STATUS_CLOSE);
                break;
            case MSG_SAMPLING:

                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SAMPLING),
                        150);
                if (mStatus == STATUS_CANCEL)
                    break;

                int db = mVoiceHandler.getCurrentDb();
                switch (db / 5) {
                    case 0:
                        mIcon.setImageResource(ResourceUtils.getDrawableResourceId(
                                getContext(), "rc_volume_zero"));
                        break;
                    case 1:
                        mIcon.setImageResource(ResourceUtils.getDrawableResourceId(
                                getContext(), "rc_volume_one"));
                        break;
                    case 2:
                        mIcon.setImageResource(ResourceUtils.getDrawableResourceId(
                                getContext(), "rc_volume_two"));
                        break;
                    case 3:
                        mIcon.setImageResource(ResourceUtils.getDrawableResourceId(
                                getContext(), "rc_volume_three"));
                        break;
                    case 4:
                        mIcon.setImageResource(ResourceUtils.getDrawableResourceId(
                                getContext(), "rc_volume_four"));
                    default:
                        mIcon.setImageResource(ResourceUtils.getDrawableResourceId(
                                getContext(), "rc_volume_four"));
                        break;
                }

                break;

            case MSG_READY:
                setVisibility(View.VISIBLE);
                mStatus = 0;
                mVoiceLength = SystemClock.elapsedRealtime();
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SEC, 10, 0),
                        50 * 1000);
                mLastVoiceUri = null;
                mVoiceHandler.startRec();

                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SAMPLING),
                        150);
                mIcon.setImageResource(ResourceUtils.getDrawableResourceId(
                        getContext(), "volume_zero"));
                mMessage.setText(ResourceUtils.getStringResourceId(getContext(),
                        "voice_dialog_collect"));
                mText.setVisibility(View.GONE);
                break;

            default:
                break;
        }
        return true;
    }

}
