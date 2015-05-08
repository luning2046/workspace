package io.rong.imkit.activity;

import io.rong.imkit.RCloudContext;
import io.rong.imkit.logic.MessageLogic;
import io.rong.imkit.service.RCloudService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public abstract class APIBaseActivity extends BaseActivity {

    private BroadcastReceiver mBroadcastReceiver;
    private IntentFilter mIntentFilter;
    private Map<String, List<ActionCallback>> mActionCallbackMap;

    private boolean isRegister = false;

    private ArrayList<String> customActions = new ArrayList<String>();
    private ArrayList<String> bundleActions = new ArrayList<String>();
    private int playCount = 0;

    private String mCurrentConversationTargetId;

    public final static String ACTION_BUNDLE_IO_RONG_IMKIT_CONVERSATION_LIST = "action_bundle_io_rong_imkit_conversation_list";
    public final static String ACTION_BUNDLE_IO_RONG_IMKIT_CONVERSATION = "action_bundle_io_rong_imkit_conversation";
    public final static String ACTION_BUNDLE_IO_RONG_IMKIT_CONVERSATION_SETTING = "action_bundle_io_rong_imkit_conversation_setting";
    public final static String ACTION_BUNDLE_IO_RONG_IMKIT_FRIEND_SELECT = "action_bundle_io_rong_imkit_friend_select";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mIntentFilter = new IntentFilter();
        mBroadcastReceiver = new BaseReceiver();
        mActionCallbackMap = new HashMap<String, List<ActionCallback>>();

        registerActions(customActions);
        registerBunlderActions(bundleActions);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        playCount = 0;
        super.onResume();
    }

    public void sendAction(Intent intent, ActionCallback actionCallback) {

        String action = intent.getAction();

        if (bundleActions.contains(action)) {
            recevicePageIntnet(intent);
            return;
        }

        if (!mIntentFilter.hasAction(action)) {
            mIntentFilter.addAction(action);
            this.registerReceiver(mBroadcastReceiver, mIntentFilter);
            isRegister = true;
        }

        if (!mActionCallbackMap.containsKey(action)) {
            mActionCallbackMap.put(action, Collections.synchronizedList(new ArrayList<ActionCallback>()));
        }

        mActionCallbackMap.get(action).add(actionCallback);
        intent.putExtra(RCloudService.EXTRA_CATION_HASHCODE, actionCallback.hashCode());
        Log.d("APIBaseActivity--sendAction-action.hashCode", "action.hashCode:" + action.hashCode() + "|" + action);

        RCloudContext.getInstance().sendAction(intent);
    }

    private class BaseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (bundleActions.contains(action)) {
                recevicePageIntnet(intent);
                return;
            }

            if (customActions.contains(action)) {

                Log.d("customActions", "==customActions:" + action);
                String ss = "mCurrentConversationTargetId:" + mCurrentConversationTargetId;
                Log.d("mCurrentConversationTargetId", ss);

                if (MessageLogic.ACTION_P2P_MESSAGE_RECEVICE.equals(action)) {


                    if (!TextUtils.isEmpty(mCurrentConversationTargetId)) {
                        String messageTargetId = intent.getStringExtra(MessageLogic.TARGET_ID);

                        if (!mCurrentConversationTargetId.equals(messageTargetId)) {
                            newMessageReminder();
                        }
                    } else {
                        Log.d("mCurrentConversationTargetId", "-------------newMessageReminder-------111-------->");
                        newMessageReminder();
                    }
                }

                receviceData(intent);

                if (RCloudContext.CLIENT_CONNECTED_TO_SDK.equals(action)) {
                    Toast.makeText(APIBaseActivity.this, "connected", Toast.LENGTH_LONG).show();
                } else if (RCloudContext.CLIENT_DISCONNECT_TO_SDK.equals(action)) {
                    Toast.makeText(APIBaseActivity.this, "disconnect", Toast.LENGTH_LONG).show();
                }

                return;
            }

            if (mActionCallbackMap != null && mActionCallbackMap.containsKey(action)) {

                int hashCode = intent.getIntExtra(RCloudService.EXTRA_CATION_HASHCODE, -1);
                intent.removeExtra(RCloudService.EXTRA_CATION_HASHCODE);
                List<ActionCallback> list = mActionCallbackMap.get(action);
                Log.d("action.hashCode---1", "action.hashCode:" + action.hashCode() + "|" + action);

                if (list != null) {
                    int listIndex = -1;

                    for (int i = 0; i < list.size(); i++) {
                        Log.d("list-hashCode", "list.get(i).hashCode:" + list.get(i).hashCode() + "|" + list.get(i));

                        if (hashCode == list.get(i).hashCode()) {

                            listIndex = i;
                            break;
                        }
                    }

                    if (listIndex >= 0) {

                        boolean isComplete = intent.getBooleanExtra(MessageLogic.INTENT_IS_COMPLETE, false);

                        if (isComplete) {
                            list.remove(listIndex).callback(intent);
                            Log.d("APIBaseActivity", "remove callback action:" + action);
                        } else {
                            list.get(listIndex).callback(intent);
                            Log.d("APIBaseActivity", "GET callback action:" + action);
                        }

                    } else {
                        Log.d("APIBaseActivity---", "actionCall is null" + action);
                    }

                    if (list.isEmpty()) {
                        mActionCallbackMap.remove(action);
                    }
                }
            }

        }
    }

    private void newMessageReminder() {
        playCount++;

        Log.d("mCurrentConversationTargetId", "playCount1111:" + playCount);

        if (playCount > 1)
            return;

        Log.d("mCurrentConversationTargetId", "-------------newMessageReminder----------2222----->");

        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        switch (audioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                playCount = 0;
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(200);
                playCount = 0;
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                playNewMessageSound();
                break;
        }

        int current = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        Log.d("mCurrentConversationTargetId", "-------------newMessageReminder----------Vibrator----->" + current);

    }

    private void playNewMessageSound() {

        try {
            MediaPlayer mMediaPlayer = new MediaPlayer();
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);

            mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });

            mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {

                    Log.d("mCurrentConversationTargetId", "playCount222:" + playCount);
                    playCount = 0;

                    mp.reset();
                    mp.release();
                    mp = null;
                }
            });

            mMediaPlayer.setDataSource(this, alert);
            mMediaPlayer.prepare();


        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public interface ActionCallback {
        public void callback(Intent intent);
    }

    public void receviceData(Intent intent) {
        Log.d("BaseActivity---|||--receviceData", intent.getAction());
    }

    @Override
    protected void onDestroy() {

        if (isRegister) {
            this.unregisterReceiver(mBroadcastReceiver);
        }

        mCurrentConversationTargetId = null;

        super.onDestroy();
    }

    public void registerActions(List<String> actions) {

        if (actions == null || actions.isEmpty()) {
            return;
        }

        for (String action : actions) {

            if (!mIntentFilter.hasAction(action)) {
                mIntentFilter.addAction(action);
                this.registerReceiver(mBroadcastReceiver, mIntentFilter);
                isRegister = true;
            }
        }

    }

    public void registerBunlderActions(List<String> actions) {

        if (actions == null || actions.isEmpty()) {
            return;
        }

        for (String action : actions) {

            if (!mIntentFilter.hasAction(action)) {
                mIntentFilter.addAction(action);
                this.registerReceiver(mBroadcastReceiver, mIntentFilter);
                isRegister = true;
            }
        }
    }

    public void recevicePageIntnet(Intent intent) {

    }


    public void setCurrentCoversationTargetId(String mCurrentTargetId) {
        this.mCurrentConversationTargetId = mCurrentTargetId;
    }

}
