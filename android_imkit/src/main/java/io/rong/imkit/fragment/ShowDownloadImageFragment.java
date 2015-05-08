package io.rong.imkit.fragment;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;

import io.rong.imkit.logic.MessageLogic;
import io.rong.imkit.utils.ResourceUtils;
import io.rong.imkit.utils.RongToast;
import io.rong.imkit.veiw.LoadingDialog;
import io.rong.imlib.RongIMClient.ConversationType;

public class ShowDownloadImageFragment extends ActionBaseFrament implements Callback, View.OnClickListener {

    public static final String MESSAGE_IMAGE_KEY = "message_image_key";
    private ImageView mImageView;
    private String mImageKey;
    private Handler mHandler;
    private LoadingDialog mDialog;
    private TextView mSaveTextView;
    private File mFile;

    private String mTargetId;

    private final int HANDLE_DOWNLOAD_SUCCESS = 10009;
    private final int HANDLE_DOWNLOAD_FAILURE = 10010;
    private final String IMAGE_DESCRIPTION = "RongCloud_Download";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mHandler = new Handler(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(ResourceUtils.getLayoutResourceId(getActivity(), "rc_fragment_show_dowload_image"), null);
        mImageView = getViewById(view, android.R.id.icon);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        if (getActionBar() != null) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            mSaveTextView = new TextView(getActivity());
            mSaveTextView.setLayoutParams(layoutParams);
            mSaveTextView.setBackgroundResource(ResourceUtils.getDrawableResourceId(getActivity(), "rc_send_selector"));
            mSaveTextView.setText(getResources().getString(ResourceUtils.getStringResourceId(getActivity(), "show_image_save")));
            mSaveTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            mSaveTextView.setOnClickListener(this);
            mSaveTextView.setTextColor(Color.WHITE);
            getActionBar().addView(mSaveTextView);
            getActionBar().setOnBackClick(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
            getActionBar().getTitleTextView().setText(ResourceUtils.getStringResourceId(getActivity(), "show_download_image_title"));
        }

        Intent intentArg = getActivity().getIntent();

        mImageKey = intentArg.getStringExtra(MESSAGE_IMAGE_KEY);
        mTargetId = intentArg.getStringExtra(MessageLogic.TARGET_ID);

        Intent intent = new Intent(MessageLogic.ACTION_MESSAGE_IMAGE_DOWNLOAD);
        intent.putExtra(MessageLogic.INTENT_MESSAGE_FILE_DOWN_RES_KEY, mImageKey);
        intent.putExtra(MessageLogic.INTENT_MESSAGE_FILE_DOWN_TYPE, ConversationType.PRIVATE.getValue());
        intent.putExtra(MessageLogic.TARGET_ID, mTargetId);

        mDialog = new LoadingDialog(getActivity());
        mDialog.setText(ResourceUtils.getStringResource(getActivity(), "show_download_image_loading"));
        mDialog.show();

        if (!TextUtils.isEmpty(mImageKey)) {

            sendAction(intent, new ActionCallback() {

                @Override
                public void callback(Intent intent) {
                    String path = intent.getStringExtra(MessageLogic.INTENT_MESSAGE_FILE_DOWN_RES_PATH);
                    boolean isOperation = intent.getBooleanExtra(MessageLogic.INTENT_API_OPERATION_STATUS, false);
                    boolean isComplete = intent.getBooleanExtra(MessageLogic.INTENT_IS_COMPLETE, false);
                    if (isComplete) {
                        if (isOperation) {
                            if (!TextUtils.isEmpty(path)) {
                                mHandler.obtainMessage(HANDLE_DOWNLOAD_SUCCESS, path).sendToTarget();
                            }
                        } else {
                            mHandler.obtainMessage(HANDLE_DOWNLOAD_FAILURE).sendToTarget();
                        }
                    }
                }
            });

        }

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public boolean handleMessage(Message msg) {

        if (msg.what == HANDLE_DOWNLOAD_SUCCESS) {
            String path = (String) msg.obj;
            setImage(path);
            if (mDialog != null) {
                mDialog.dismiss();
            }
        } else if (msg.what == HANDLE_DOWNLOAD_FAILURE) {
            if (mDialog != null) {
                mDialog.dismiss();
            }

            mImageView.setImageDrawable(ResourceUtils.getDrawableById(getActivity(), "rc_image_download_failure"));
            mImageView.setScaleType(ImageView.ScaleType.CENTER);

            RongToast.toast(getActivity(), ResourceUtils.getStringResource(getActivity(), "show_image_download_failure"));
        }

        return false;
    }

    private final void setImage(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        mFile = new File(path);
        mImageView.setImageBitmap(BitmapFactory.decodeFile(path, options));
        mDialog.dismiss();

    }

    @Override
    public void onClick(View v) {
        if (v == mSaveTextView) {
            new AsyncTask<Void, Void, Boolean>() {

                @Override
                protected void onPreExecute() {
                    // mHandler.obtainMessage(SHOW_DIALOG).sendToTarget();
                    super.onPreExecute();
                }

                @Override
                protected Boolean doInBackground(Void... params) {
                    ContentResolver resolver = getActivity().getContentResolver();
                    try {
                        MediaStore.Images.Media.insertImage(resolver, mFile.getPath(), mFile.getName(), IMAGE_DESCRIPTION);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                    // 保存成功发送广播，刷新系统相册
                    getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
                    return true;
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    // mHandler.obtainMessage(HIDE_DIALOG).sendToTarget();
                    if (result) {
                        mSaveTextView.setEnabled(false);
                        mSaveTextView.setTextColor(Color.GRAY);
                        RongToast.toast(getActivity(), ResourceUtils.getStringResource(getActivity(), "show_image_save_success"));
                    } else {
                        RongToast.toast(getActivity(), ResourceUtils.getStringResource(getActivity(), "show_image_save_failure"));
                    }
                    super.onPostExecute(result);
                }

            }.execute();
        }

    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
