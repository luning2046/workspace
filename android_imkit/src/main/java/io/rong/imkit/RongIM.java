package io.rong.imkit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import io.rong.imkit.common.RCloudConst;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imkit.model.HandshakeMessage;
import io.rong.imlib.AnnotationNotFoundException;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ConnectCallback;
import io.rong.imlib.RongIMClient.ConversationType;
import io.rong.imlib.RongIMClient.Message;
import io.rong.imlib.RongIMClient.MessageContent;
import io.rong.imlib.RongIMClient.SendMessageCallback;
import io.rong.imlib.RongIMClient.UserInfo;

/**
 * IM 界面组件核心类。
 * <p/>
 * 所有 IM 相关界面、功能都由此调用和设置。
 */
public class RongIM {

    private static final String TAG = RongIM.class.getSimpleName();

    private static RongIM rongIM;
    private static Context mContext;
    private static RongIMClient mRrongIMClient;
    // private OnActivityStartedListener mListlistener;
    private OnConversationStartedListener mConversationLaunchedListener;
    private Class<? extends Activity> mClass;

private RongIM() {}

    /**
     * 初始化 SDK。
     *
     * @param context       应用上下文。
     * @param appKey        从开发者平台(<a href="http://rongcloud.cn"
     *                      target="_blank">rongcloud.cn</a>)申请的应用 AppKey
     * @param pushIconResId 推送中显示的图标资源。
     */
    public static void init(Context context, String appKey, int pushIconResId) {

        if (TextUtils.isEmpty(appKey)) {   throw new NullPointerException("App key is null."); }
        if (context == null || TextUtils.isEmpty(appKey)) { throw new IllegalArgumentException();}
        mContext = context.getApplicationContext();

        RongIMClient.init(context, appKey, pushIconResId);

        RCloudContext.getInstance().init(mContext, appKey);

        try {
            RongIMClient.registerMessageType(HandshakeMessage.class);
            RongIMClient.registerMessageType(io.rong.voipkit.message.VoipCallMessage.class);
            RongIMClient.registerMessageType(io.rong.voipkit.message.VoipAcceptMessage.class);
            RongIMClient.registerMessageType(io.rong.voipkit.message.VoipFinishMessage.class);
        } catch (AnnotationNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * IM 界面组件登录。
     *
     * @param token    从服务端获取的<a
     *                 href="http://docs.rongcloud.cn/android#token">用户身份令牌（
     *                 Token）</a>。
     * @param callback 登录回调。
     * @return IM 界面组件。
     */
    public static RongIM connect(String token, final ConnectCallback callback) {
        if (TextUtils.isEmpty(token)) { throw new IllegalArgumentException();}
        saveToken(token);
        rongIM = new RongIM();
        Log.d("RCloudContext－－－n", "－－－－login---begin------");
        
        try {
            rongIM.mRrongIMClient = RongIMClient.connect(token, new ConnectCallback() {

                @Override
                public void onSuccess(String userId) {
           	
                    Log.d("RCloudContext.getInstance().initService()---begin", "begin");
//==================启动RCloudService这个serive=========================================================================   
RCloudContext.getInstance().initService();
                    Log.d("RCloudContext.getInstance().initService()---end", "end");
                    callback.onSuccess(userId);
                }

                @Override
                public void onError(ErrorCode errorCode) {
                    callback.onError(errorCode);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        RCloudContext.getInstance().setRongIMClient(rongIM.mRrongIMClient);

        return rongIM;
    }

    private static void saveToken(String token) {
        SharedPreferences preferences = mContext.getSharedPreferences("rc_token", Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString("token_value", token);
        editor.commit();// 提交数据到背后的xml文件中
    }

    /**
     * 注销当前登录。
     */
    public void disconnect() {
        RCloudContext.getInstance().logout();
        mRrongIMClient.disconnect();
        Log.d("logout---", "self disconnect---");
    }

    /**
     * 获取界面组件的核心类单例。
     *
     * @return 界面组件的核心类单例。
     */
    public static RongIM getInstance() {
        return rongIM;
    }

    /**
     * 启动会话列表界面。
     *
     * @param context  应用上下文。
     * @param listener 会话列表 Activity 启动的监听器。
     */
    public void startConversationList(Context context, OnConversationListStartedListener listener) {
        if (context == null) {  throw new IllegalArgumentException(); }
        RCloudContext.getInstance().setConversationListLaunchedListener(listener);

        Uri uri = Uri.parse("rong://" + context.getApplicationInfo().packageName).buildUpon()
                .appendPath("conversationlist").build();

        context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    /**
     * 启动单聊界面。
     *
     * @param context      应用上下文。
     * @param targetUserId 要与之聊天的用户 Id。
     * @param title        聊天的标题，如果传入空值，则默认显示与之聊天的用户名称。
     * @param listener     单聊会话 Activity 启动的监听器。
     */
    public void startPrivateChat(Context context, String targetUserId, String title, OnConversationStartedListener listener) {

        if (context == null || TextUtils.isEmpty(targetUserId)) {
            throw new IllegalArgumentException();
        }

        RCloudContext.getInstance().setConversationLaunchedListener(listener);

        Uri uri = Uri.parse("rong://" + context.getApplicationInfo().packageName).buildUpon()
                .appendPath("conversation").appendPath(ConversationType.PRIVATE.getName().toLowerCase())
                .appendQueryParameter("targetId", targetUserId).appendQueryParameter("title", title).build();

        context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    public void startConversationSetting(Context context, ConversationType conversationType, String targetId){
        if (context == null || TextUtils.isEmpty(targetId)) {
            throw new IllegalArgumentException();
        }

        Uri uri = Uri.parse("rong://" + context.getApplicationInfo().packageName).buildUpon()
                .appendPath("conversationsetting").appendPath(conversationType.getName().toLowerCase())
                .appendQueryParameter("targetId", targetId).build();

        context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    /**
     * 启动客户服聊天界面。
     *
     * @param context               应用上下文。
     * @param customerServiceUserId 要与之聊天的客服 Id。
     * @param title                 聊天的标题，如果传入空值，则默认显示与之聊天的客服名称。
     * @param listener              单聊会话 Activity 启动的监听器。
     */
    public void startCustomerServiceChat(Context context, String customerServiceUserId, String title, OnConversationStartedListener listener) {
        if (context == null || TextUtils.isEmpty(customerServiceUserId)) {
            throw new IllegalArgumentException();
        }
        RCloudContext.getInstance().setConversationLaunchedListener(listener);

        Uri uri = Uri.parse("rong://" + context.getApplicationInfo().packageName).buildUpon()
                .appendPath("conversation").appendPath(ConversationType.CUSTOMERSERVICE.getName().toLowerCase())
                .appendQueryParameter("targetId", customerServiceUserId).appendQueryParameter("title", title).build();

        context.startActivity(new Intent(Intent.ACTION_VIEW, uri));

        sendMessage(ConversationType.PRIVATE, customerServiceUserId, new HandshakeMessage(), null);
    }


    /**
     * 以当前用户的身份发送一条消息。<br/>
     *
     * @param conversationType 会话类型。
     * @param targetId         目标 Id。根据不同的 conversationType，可能是聊天 Id、讨论组 Id、群组 Id 或聊天室 Id。
     * @param content          消息内容。
     * @param callback         发送消息的回调。
     * @return 发送的消息实体。
     */
    public Message sendMessage(final ConversationType conversationType, final String targetId, MessageContent content, final SendMessageCallback callback) {
        if (conversationType == null || TextUtils.isEmpty(targetId) || content == null) {
            throw new IllegalArgumentException();
        }
        return mRrongIMClient.sendMessage(conversationType, targetId, content, callback);
    }

    /**
     * 获取所有未读消息数。
     *
     * @return 未读消息数。
     */
    public int getTotalUnreadCount() {
        return mRrongIMClient.getTotalUnreadCount();
    }

    /**
     * 刷新用户信息
     *
     * @param userInfo
     */
    private void refreshUserInfo(UserInfo userInfo) {
        if (userInfo == null) {
            throw new IllegalArgumentException();
        }
        // TODO: 未实现。
    }

    /**
     * 设置接收消息的监听器。
     * <p/>
     * 所有接收到的消息、通知、状态都经由此处设置的监听器处理。包括单聊消息、讨论组消息、群组消息、聊天室消息以及各种状态。<br/>
     * 此处仅为扩展功能提供，默认可以不做实现。
     *
     * @param listener 接收消息的监听器。
     */
    public void setReceiveMessageListener(final OnReceiveMessageListener listener) {
        RCloudContext.getInstance().setReceiveMessageListener(listener);
    }

    /**
     * 设置获取用户信息的提供者，供 RongIM 调用获取用户名称和头像信息。
     *
     * @param provider      获取用户信息提供者。
     * @param cacheUserInfo 设置是否由 IMKit 来缓存用户信息。<br/>
     *                      如果 App 提供的 GetUserInfoProvider
     *                      每次都需要通过网络请求用户数据，而不是将用户数据缓存到本地，会影响用户信息的加载速度；<br/>
     *                      此时最好将本参数设置为 true，由 IMKit 来缓存用户信息。
     * @see io.rong.imkit.RongIM.GetUserInfoProvider
     */
    public static void setGetUserInfoProvider(GetUserInfoProvider provider, boolean cacheUserInfo) {
        RCloudContext.getInstance().setGetUserInfoProvider(provider);
    }

    /**
     * 设置获取好友列表的提供者，供 RongIM 调用获取好友列表以及好友的名称和头像信息。
     *
     * @param provider 获取好友列表的提供者。
     * @see io.rong.imkit.RongIM.GetFriendsProvider
     */
    public static void setGetFriendsProvider(GetFriendsProvider provider) {
        RCloudContext.getInstance().setGetFriendsProvider(provider);
    }

    /**
     * 启动界面后调用的监听器。
     */
    public static interface OnConversationListStartedListener {
        /**
         * 当 Activity 创建后执行。
         */
        public void onCreated();

        /**
         * 当 Activity 销毁后执行。
         */
        public void onDestroyed();
    }

    /**
     * 启动会话列表界面后调用的监听器。
     */
    public static interface OnConversationStartedListener {

        /**
         * 当 Activity 创建后执行。
         *
         * @param conversationType 会话类型。
         * @param targetId         目标 Id。根据不同的 conversationType，可能是聊天 Id、讨论组 Id、群组 Id 或聊天室
         *                         Id。
         */
        public void onCreated(ConversationType conversationType, String targetId);

        /**
         * 当 Activity 销毁后执行。
         */
        public void onDestroyed();

        /**
         * 当点击用户头像后执行。
         *
         * @param user 被点击的用户的信息。
         */
        public void onClickUserPortrait(UserInfo user);

        /**
         * 当点击消息时执行。
         *
         * @param message 被点击的消息的实体信息。
         */
        public void onClickMessage(Message message);
    }

    /**
     * 接收消息的监听器。
     */
    public static interface OnReceiveMessageListener {
        /**
         * 接收到消息后执行。
         *
         * @param message 接收到的消息的实体信息。
         */
        public void onReceived(Message message);
    }

    /**
     * 用户信息的提供者。
     * <p/>
     * 如果在聊天中遇到的聊天对象是没有登录过的用户（即没有通过融云服务器鉴权过的），RongIM 是不知道用户信息的，RongIM 将调用此
     * Provider 获取用户信息。
     */
    public static interface GetUserInfoProvider {
        /**
         * 获取用户信息。
         *
         * @param userId 用户 Id。
         * @return 用户信息。
         */
        public UserInfo getUserInfo(String userId);
    }

    /**
     * 好友列表的提供者。
     * <p/>
     * RongIM 本身不保存 App 的好友关系，如果在聊天中需要使用好友关系时（如：需要选择好友加入群聊），RongIM 将调用此 Provider
     * 获取好友列表信息。
     */
    public static interface GetFriendsProvider {
        /**
         * 获取好友信息列表。
         *
         * @return 好友信息列表。
         */
        public List<UserInfo> getFriends();
    }

    /**
     * 获取来自某用户的未读消息数。
     *
     * @return 未读消息数。
     */
    public int getUnreadCount(ConversationType conversationType,String targetId) {

        if (TextUtils.isEmpty(targetId) || TextUtils.isEmpty(targetId.trim()) || conversationType == null) {
            throw new IllegalArgumentException();

        }

        return mRrongIMClient.getUnreadCount(conversationType,targetId);
    }

}
