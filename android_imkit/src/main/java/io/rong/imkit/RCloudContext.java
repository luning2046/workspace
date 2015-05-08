package io.rong.imkit;

import io.rong.imkit.RongIM.GetFriendsProvider;
import io.rong.imkit.RongIM.GetUserInfoProvider;
import io.rong.imkit.RongIM.OnConversationListStartedListener;
import io.rong.imkit.RongIM.OnConversationStartedListener;
import io.rong.imkit.service.RCloudService;
import io.rong.imkit.version.Version;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.UserInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import uk.co.senab.bitmapcache.BitmapLruCache;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sea_monster.core.common.Const;
import com.sea_monster.core.common.DiscardOldestPolicy;
import com.sea_monster.core.network.DefaultHttpHandler;
import com.sea_monster.core.network.HttpHandler;
import com.sea_monster.core.resource.ResourceManager;
import com.sea_monster.core.resource.cache.ResourceCacheWrapper;
import com.sea_monster.core.resource.compress.IResourceCompressHandler;
import com.sea_monster.core.resource.compress.ResourceCompressHandler;
import com.sea_monster.core.resource.io.FileSysHandler;
import com.sea_monster.core.resource.io.ResourceRemoteWrapper;

/**
 * Created by zhjchen on 14-3-21.
 */
public class RCloudContext {

    public static final String CLIENT_CONNECTED_TO_SDK = "client_connected_to_sdk";
    public static final String CLIENT_DISCONNECT_TO_SDK = "client_disconnect_to_sdk";

    public static final String CLICK_REPEAT = "click_repeat";
    public static final Version objVer = new Version();


    private static RCloudContext mFCloudSDK;

    private Context mContext;
    private ComponentName mComponentName;

    private SharedPreferences mPreferences;
    private String mAppResourceDir;

    private RongIMClient mRongIMClient;

//    private UserInfo mUserInfo;

    private FileSysHandler mFileSysHandler;

    private String appKey;

    private int mNotificationNewMessageCount = 0;

    private ArrayList<String> mNotificationUserIdList = new ArrayList<String>();
private GetFriendsProvider mGetFriendsProvider;//===========================登录成功后===== 获取好友列表=========================================

private RCloudContext() {}

    public Context getContext() {
        return mContext;
    }

    public static RCloudContext getInstance() {

        if (mFCloudSDK == null) {
            mFCloudSDK = new RCloudContext();
        }

        return mFCloudSDK;
    }

    /**
     * @param context
     */
    public void init(Context context, String appId) {

new CrashHandler(context).init();//?  待研究

        this.mContext = context;
        mComponentName = new ComponentName(mContext, RCloudService.class);//封装要启动的“组件”，如   service、activity

        initHttpAndResource(context);//封装http请求的包

        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        appKey=appId;
    }

    private ThreadFactory mThreadFactory;
    private static ThreadPoolExecutor sExecutor;

    // private IFileSysHandler mFileSysHandler;
    private static HttpHandler mHttpHandler;
    private IResourceCompressHandler mCompressHandler;
    private BlockingQueue<Runnable> mWorkQueue;

    /**
     * @param context
     * @return
     */
    private final HttpHandler initHttpAndResource(Context context) {

        BlockingQueue<Runnable> mWorkQueue = new PriorityBlockingQueue<Runnable>(Const.SYS.WORK_QUEUE_MAX_COUNT);

        ThreadFactory mThreadFactory = new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            public Thread newThread(Runnable r) {
                return new Thread(r, "ConnectTask #" + mCount.getAndIncrement());
            }
        };

        ThreadPoolExecutor mExecutor = new ThreadPoolExecutor(Const.SYS.DEF_THREAD_WORDER_COUNT, Const.SYS.MAX_THREAD_WORKER_COUNT, Const.SYS.CREATE_THREAD_TIME_SPAN,
                TimeUnit.SECONDS, mWorkQueue, mThreadFactory);

        mExecutor.setRejectedExecutionHandler(new DiscardOldestPolicy());

        mFileSysHandler = new FileSysHandler(mExecutor, getResourceDir(context), "file", "rong");

        HttpHandler httpHandler = new DefaultHttpHandler(context, mExecutor);

        ResourceRemoteWrapper remoteWrapper = new ResourceRemoteWrapper(context, mFileSysHandler, httpHandler);

        BitmapLruCache cache = new BitmapLruCache.Builder(context).setDiskCacheLocation(new File(getResourceDir(context), "cache")).build();

        ResourceCompressHandler compressHandler = new ResourceCompressHandler(context, mFileSysHandler);

        ResourceCacheWrapper cacheWrapper = new ResourceCacheWrapper(context, cache, mFileSysHandler, compressHandler);

        ResourceManager.init(context, remoteWrapper, cacheWrapper);

        return httpHandler;
    }

    public void initService() {

        Log.d("RCloudContext", "-------initService------");

        if (isRCloudServiceRuning("io.rong.imkit.service.RCloudService")) {
            mContext.stopService(new Intent().setComponent(mComponentName));
        }

        mContext.startService(new Intent().setComponent(mComponentName));

    }

    public void logout() {
        Log.d("RCloudContext", "--------logout-----");
        mContext.stopService(new Intent().setComponent(mComponentName));
    }

    /**
     * @param intent
     */
    public void sendAction(Intent intent) {
        Log.d("MessageLogic", "----RCloudContext----sendAction-----");
        if (intent != null) {
            intent.setComponent(mComponentName);
            mContext.startService(intent);
        }
    }


    private boolean isRCloudServiceRuning(String className) {
        Log.d("runningServiceInfo", "1111:" + System.currentTimeMillis());

        ActivityManager activityManager = (ActivityManager) this.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> servicesList = activityManager.getRunningServices(Integer.MAX_VALUE);
        Log.d("runningServiceInfo", "111122:" + System.currentTimeMillis());

        for (ActivityManager.RunningServiceInfo runningServiceInfo : servicesList) {
            if (runningServiceInfo.service.getClassName().equals(className)) {
                return true;
            }
        }
        Log.d("runningServiceInfo", "222:" + System.currentTimeMillis());

        return false;
    }

    public SharedPreferences getSharedPreferences() {
        return mPreferences;
    }

    public void setSharedPreferences(SharedPreferences mSharedPreferences) {
        this.mPreferences = mSharedPreferences;
    }

    public void setViewClickFlag(boolean isRepeat) {

        if (mPreferences != null) {
            Editor editor = mPreferences.edit();

            if (isRepeat) {
                editor.putBoolean(CLICK_REPEAT, true);
            } else {
                editor.putBoolean(CLICK_REPEAT, false);
            }

            editor.commit();
        }
    }

    public String getAppResourceDir() {
        return mAppResourceDir;
    }

    public RongIMClient getRongIMClient() {
        return mRongIMClient;
    }

    private OnConversationStartedListener mConversationLaunchedListener;// 会话页面
    private OnConversationListStartedListener mConversationListLaunchedListener;// 会话列表
 
    private GetUserInfoProvider mGetUserInfoProvider;// 获取用户信息
    private RongIM.OnReceiveMessageListener mOnReceiveMessageListener;// 新消息提示

    public OnConversationListStartedListener getConversationListLaunchedListener() {
        return mConversationListLaunchedListener;
    }

    public void setConversationListLaunchedListener(OnConversationListStartedListener mConversationListLaunchedListener) {
        this.mConversationListLaunchedListener = mConversationListLaunchedListener;
    }

    public OnConversationStartedListener getConversationLaunchedListener() {
        return mConversationLaunchedListener;
    }

    public void setConversationLaunchedListener(OnConversationStartedListener conversationLaunchedListener) {
        this.mConversationLaunchedListener = conversationLaunchedListener;
    }

    public GetFriendsProvider getGetFriendsProvider() {
        return mGetFriendsProvider;
    }
//===============从LoginActivity设置过来的=================================================================
    public void setGetFriendsProvider(GetFriendsProvider mGetFriendsProvider) {
        this.mGetFriendsProvider = mGetFriendsProvider;
    }

    public GetUserInfoProvider getGetUserInfoProvider() {
        return mGetUserInfoProvider;
    }

    public void setGetUserInfoProvider(GetUserInfoProvider mGetUserInfoProvider) {
        this.mGetUserInfoProvider = mGetUserInfoProvider;
    }

    public void setReceiveMessageListener(RongIM.OnReceiveMessageListener listener){
        mOnReceiveMessageListener=listener;
    }


    public RongIM.OnReceiveMessageListener getOnReceiveMessageListener(){
        return mOnReceiveMessageListener;
    }

//    public UserInfo getUserInfo() {
//        return mUserInfo;
//    }
//
//    public void setUserInfo(UserInfo mUserInfo) {
//        this.mUserInfo = mUserInfo;
//    }

    public void setRongIMClient(RongIMClient mRongIMClient) {
        this.mRongIMClient = mRongIMClient;
    }

    /**
     * @param context
     * @return
     */
    private final File getResourceDir(Context context) {

        File environmentPath = null;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // File root = new File(Environment.getExternalStorageDirectory(),
            // "Android/data");
            File root = new File(Environment.getExternalStorageDirectory(), "RongCloud");

            environmentPath = new File(root, getContext().getPackageName());
        } else {
            environmentPath = context.getFilesDir();
        }

        File voicePath = new File(environmentPath, "voice");

        if (!voicePath.isDirectory()) {
            voicePath.mkdir();
        }

        mAppResourceDir = environmentPath.getPath();

        return environmentPath;
    }

    public String getAppKey(){
        return  appKey;
    }


    public int getNotificationNewMessageCount() {
        return mNotificationNewMessageCount;
    }

    public void setNotificationNewMessageCount(int mNotificationNewMessageCount) {
        this.mNotificationNewMessageCount = mNotificationNewMessageCount;
    }

    public void setNotificationNewMessageCountIncrease(){
        this.mNotificationNewMessageCount++;
    }

    public ArrayList<String> getNotificationUserIdList() {
        return mNotificationUserIdList;
    }

    public void clearNotificationUserIdList() {
        this.mNotificationUserIdList.clear();
    }
}
