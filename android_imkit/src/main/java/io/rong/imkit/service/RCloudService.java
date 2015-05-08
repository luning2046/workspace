package io.rong.imkit.service;

import io.rong.imkit.RCloudContext;
import io.rong.imkit.logic.ActionListener;
import io.rong.imkit.logic.LogicManager;
import io.rong.imkit.logic.MessageLogic;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.model.UIUserInfo;
import io.rong.imkit.utils.MultiValueMap;
import io.rong.imkit.utils.PriorityThreadFactory;
import io.rong.imkit.utils.ResourceUtils;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ImageMessage;
import io.rong.imlib.RongIMClient.TextMessage;
import io.rong.imlib.RongIMClient.UserInfo;
import io.rong.imlib.RongIMClient.VoiceMessage;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by zhjchen on 14-3-20.
 */
public class RCloudService extends Service {

    private static final String TAG = RCloudService.class.getSimpleName();
    public static final String EXTRA_CATION_HASHCODE = "cn.com.feinno.fcloud.service.FCloudService.EXTRA_ACTION_HASHCODE";

    private MultiValueMap<String, ActionListener> mResolver;
    private Handler mHandler;
    private Looper mLooper;

    private static final String NOTICATION_CONVERSATION_URI = "rong://%1$s/conversation/%2$s?targetid=%3$s";
    private static final String NOTICATION_CONVERSTATION_LSIT_URI = "rong://%1$s/conversationList";


    @Override
    public void onCreate() {
        super.onCreate();

        Log.e("FcloudService---init---", "========================fcloudservice---init-onCreate--===============");

        mResolver = new MultiValueMap<String, ActionListener>(8);
        LogicManager.getInstance().init(this);

        HandlerThread handlerThread = new HandlerThread(TAG, PriorityThreadFactory.THREAD_PRORITY_DEFAULT_LESS);
        handlerThread.start();
        mLooper = handlerThread.getLooper();

        mHandler = new Handler(mLooper) { // handler 用HandlerThread
            // handler就不会再run在主线程了

            @Override
            public void handleMessage(Message msg) {
                Log.d("FCloudService---handleMessage", "------------>");

                Intent intent = (Intent) msg.obj;
                onHandleAction(intent);
            }
        };
    }
    
    public void registerAction(ActionListener actionListener, List<String> actions) {
        if (actionListener != null && actions != null) {
            for (String action : actions) {
                if (action != null) {
                    mResolver.put(action, actionListener);
                }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler.obtainMessage(0, intent).sendToTarget();
        Log.e("FcloudService---init---", "========================fcloudservice---onStartCommand--===============");
        return super.onStartCommand(intent, flags, startId);
    }

  
    private final void onHandleAction(Intent intent) {

        if (intent != null) {
            intent.setComponent(null);
            String action = intent.getAction();

            if (TextUtils.isEmpty(action)) {
                return;
            }

            Log.d("FCloudService----onHandleAction----", action + "|hashCode:" + intent.getIntExtra(RCloudService.EXTRA_CATION_HASHCODE, -1));

            synchronized (mResolver) {

                if (!TextUtils.isEmpty(action) && mResolver.containsKey(action)) {
                    List<ActionListener> actionListeners = mResolver.get(action);

                    if (actionListeners != null) {
                        for (int i = 0; i < actionListeners.size(); i++) {
                            actionListeners.get(i).onHandleAction(intent);
                        }
                    }
                }
            }
        }
    }

    public void newMessageNotifycation(final UIMessage message) {

        new Thread() {

            @Override
            public void run() {

                boolean isOnBackgroud = isAppOnBackground();

                Log.d(TAG, "isOnBackgroud----:" + isOnBackgroud);

                if (isOnBackgroud && message != null) {
                    warpMessage(message);
                }
            }

        }.start();

    }

    Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            UIMessage message = null;

            if (msg.obj != null) {
                message = (UIMessage) msg.obj;
            }

            switch (msg.what) {

                case HANDLE_GET_USER_INFO_SUCCESS:
                    showNotification(message);
                    break;
                case HANDLE_GET_USER_INFO_FAILURE:
                    showNotification(message);
                    break;
                case HANDLE_GET_DISCUSSION_INFO_SUCCESS:
                    showNotification(message);
                    break;
                case HANDLE_GET_DISCUSSION_INFO_FAILURE:
                    showNotification(message);
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    private static final int HANDLE_GET_USER_INFO_SUCCESS = 2014082908;
    private static final int HANDLE_GET_USER_INFO_FAILURE = 2014082909;
    private static final int HANDLE_GET_DISCUSSION_INFO_SUCCESS = 2014082910;
    private static final int HANDLE_GET_DISCUSSION_INFO_FAILURE = 2014082911;

    private void warpMessage(final UIMessage message) {


        if (RongIMClient.ConversationType.PRIVATE == message.getConversationType()) {

            getUserInfo(message.getSenderUserId(), new MessageLogic.GetUserInfoCallback() {

                @Override
                public void onSuccess(UserInfo user) {
                    message.setUserInfo(new UIUserInfo(user));
                    handler.obtainMessage(HANDLE_GET_USER_INFO_SUCCESS, message).sendToTarget();
                }

                @Override
                public void onError() {
                    handler.obtainMessage(HANDLE_GET_USER_INFO_FAILURE, message).sendToTarget();
                }
            });

        } else if (RongIMClient.ConversationType.DISCUSSION == message.getConversationType()) {

            RCloudContext.getInstance().getRongIMClient().getDiscussion(message.getTargetId(), false, new RongIMClient.GetDiscussionCallback() {

                @Override
                public void onSuccess(RongIMClient.Discussion discussion) {
                    UIUserInfo uiUserInfo = new UIUserInfo();
                    uiUserInfo.setName(discussion.getName());
                    message.setUserInfo(uiUserInfo);

                    handler.obtainMessage(HANDLE_GET_DISCUSSION_INFO_SUCCESS, message).sendToTarget();
                }

                @Override
                public void onError(ErrorCode errorCode) {
                    handler.obtainMessage(HANDLE_GET_DISCUSSION_INFO_FAILURE, message).sendToTarget();
                }

            });
        }


    }

    public boolean isAppOnBackground() {

        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        String appPackageName = getPackageName();
        List<RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
        String topAppPackageName = runningTaskInfos.get(0).topActivity.getPackageName();
        Log.d(TAG, "topAppPackageName----:" + topAppPackageName);

        if (appPackageName.equals(topAppPackageName))
            return false;
        else
            return true;

    }

    //    private int newMessageCount = 0;
//    private ArrayList<String> userIdList = new ArrayList<String>();
    private String appName = null;

    @SuppressWarnings("deprecation")
    public void showNotification(UIMessage message) {

        int newMessageCount = RCloudContext.getInstance().getNotificationNewMessageCount();
        newMessageCount++;

        String targetId = message.getTargetId();

        ArrayList<String> userIdList = RCloudContext.getInstance().getNotificationUserIdList();

        if (!userIdList.contains(targetId)) {
            userIdList.add(targetId);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);

        Notification notification = new Notification(this.getApplicationInfo().icon, ResourceUtils.getStringResource(this, "notification_new_message"),
                System.currentTimeMillis());

        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

//        notification.defaults |= Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        notification.ledARGB = 0xff00ff00;
        notification.ledOnMS = 300;
        notification.ledOffMS = 1000;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        notification.sound = alert;
//        notification.sound = Uri.withAppendedPath(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, "6");


        String contentText = "";
        String contentTitle = "";
        String name = "";

//        if (message.getConversationType() == RongIMClient.ConversationType.PRIVATE) {
            if (message.getUserInfo() != null) {
                name = message.getUserInfo().getName();
            }
//        } else if (message.getConversationType() == RongIMClient.ConversationType.DISCUSSION) {

//        }


        if (TextUtils.isEmpty(name)) {
            name = message.getTargetId();
        }

        if (newMessageCount > 1) {
            if (userIdList.size() == 1) {
                contentTitle = name;
                String format = ResourceUtils.getStringResource(this, "notification_new_message_one_p");
                contentText = String.format(format, newMessageCount);
                // contentText.append("发来了").append(newMessageCount).append("条消息");
            } else {

                if (appName == null) {
                    appName = getAppName();
                }

                contentTitle = appName;
                String format = ResourceUtils.getStringResource(this, "notification_new_message_mang_p");
                contentText = String.format(format, userIdList.size(), newMessageCount);
                // contentText.append(userIdList.size()).append("个联系人发来").append(newMessageCount).append("条消息");
            }
        } else {
            String format = ResourceUtils.getStringResource(this, "notification_new_message_one_p_one");
            contentTitle = String.format(format, name);
            // contentTitle.append(name).append(" 发来一条消息");

            if (message.getContent() instanceof ImageMessage) {
                contentText = message.getUIImageContent(this);
            } else if (message.getContent() instanceof VoiceMessage) {
                contentText = message.getUIVoiceContent(this);
            } else if (message.getContent() instanceof TextMessage) {
                contentText = message.getTextMessageContentStr();
            }

        }
        //"//rong://%1s$/conversation/%2s%?targetid=%3s$";
        //"//rong://%1s$/conversationList";
        int peopleCount = userIdList.size();
        Uri uri = null;

        if (peopleCount == 1) {
            uri = Uri.parse("rong://" + this.getApplicationInfo().packageName).buildUpon().appendPath("conversation")
                    .appendPath(message.getConversationType().getName().toLowerCase())
                    .appendQueryParameter("targetId", message.getTargetId()).build();
        } else if (peopleCount > 1) {
            uri = Uri.parse("rong://" + this.getApplicationInfo().packageName).buildUpon().appendPath("conversationlist").build();
        }


        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//        intent.putExtra(RCloudConst.EXTRA.NOTICATION_DATA_FLAG, RCloudConst.EXTRA.NOTICATION_DATA_FLAG);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Log.d("RCloudService", "-----------showNotification----------" + contentText);
        notification.setLatestEventInfo(this, contentTitle.toString(), contentText, contentIntent);
        notificationManager.notify(0, notification);

        RCloudContext.getInstance().setNotificationNewMessageCount(newMessageCount);

    }

    private String getAppName() {

        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;

        try {
            packageManager = getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }

        return (String) packageManager.getApplicationLabel(applicationInfo);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "--------onDestroy--------");
        LogicManager.getInstance().onDestory();
        mLooper.quit();
        mResolver = null;
        super.onDestroy();
    }

    protected void getUserInfo(String userId, final MessageLogic.GetUserInfoCallback getUserInfoCallback) {

        if (RCloudContext.getInstance().getGetUserInfoProvider() != null) {

            RongIMClient.UserInfo userInfo = RCloudContext.getInstance().getGetUserInfoProvider().getUserInfo(userId);

            if (userInfo != null) {
                if (getUserInfoCallback != null) {
                    UIUserInfo uiUserInfo = new UIUserInfo(userInfo);
                    getUserInfoCallback.onSuccess(uiUserInfo);
                }

            } else {

                RCloudContext.getInstance().getRongIMClient().getUserInfo(userId, new RongIMClient.GetUserInfoCallback() {

                    @Override
                    public void onSuccess(RongIMClient.UserInfo user) {
                        UIUserInfo uiUserInfo = new UIUserInfo(user);
                        getUserInfoCallback.onSuccess(uiUserInfo);
                    }

                    @Override
                    public void onError(ErrorCode errorCode) {
                        getUserInfoCallback.onError();
                    }
                });
            }

        } else {

            RCloudContext.getInstance().getRongIMClient().getUserInfo(userId, new RongIMClient.GetUserInfoCallback() {

                @Override
                public void onSuccess(RongIMClient.UserInfo user) {
                    UIUserInfo uiUserInfo = new UIUserInfo(user);
                    getUserInfoCallback.onSuccess(uiUserInfo);
                }

                @Override
                public void onError(ErrorCode errorCode) {
                    getUserInfoCallback.onError();
                }
            });
        }


    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
