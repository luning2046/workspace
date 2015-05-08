package io.rong.imkit.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import io.rong.imkit.RCloudContext;
import io.rong.imkit.RongActivity;
import io.rong.imkit.RongIM;
import io.rong.imkit.adapter.BaseViewProvider;
import io.rong.imkit.adapter.BaseViewProvider.OnMessageItemClickListener;
import io.rong.imkit.adapter.MitulItemViewListAdapter;
import io.rong.imkit.common.IVoiceHandler;
import io.rong.imkit.common.MessageContext;
import io.rong.imkit.common.RCloudConst;
import io.rong.imkit.data.DBHelper;
import io.rong.imkit.logic.MessageLogic;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.model.UIDiscussion;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.model.UIUserInfo;
import io.rong.imkit.utils.HighLightUtils;
import io.rong.imkit.utils.ResourceUtils;
import io.rong.imkit.utils.Util;
import io.rong.imkit.veiw.ConversationMessageBar;
import io.rong.imkit.veiw.ConversationMessageBar.OnRichIconTextViewClickLinstener;
import io.rong.imkit.veiw.CoverFrameLayout;
import io.rong.imkit.veiw.LoadingDialog;
import io.rong.imkit.veiw.PullDownRefreshListView;
import io.rong.imkit.veiw.SelectDialog;
import io.rong.imkit.veiw.SelectDialog.OnDialogItemViewListener;
import io.rong.imkit.veiw.VoiceCoverView;
import io.rong.imlib.PushService;
import io.rong.imlib.RongIMClient.ConnectionStatusListener.ConnectionStatus;
import io.rong.imlib.RongIMClient.ConversationType;
import io.rong.imlib.RongIMClient.ImageMessage;
import io.rong.imlib.RongIMClient.MessageDirection;
import io.rong.imlib.RongIMClient.SentStatus;
import io.rong.imlib.RongIMClient.TextMessage;
import io.rong.imlib.RongIMClient.UserInfo;
import io.rong.imlib.RongIMClient.VoiceMessage;
import io.rong.voipkit.activity.CallSideActivity;
import io.rong.voiplib.utils.NetworkUtil;


public class ConversationFragment extends BaseConversationFragment implements PullDownRefreshListView.OnRefreshListener, ConversationMessageBar.ConversationMessageBarLinstener,
        OnTouchListener, OnRichIconTextViewClickLinstener, OnMessageItemClickListener, CoverFrameLayout.OnTriggeredTouchListener, BaseViewProvider.OnGetDataListener {

    public static final String TAG = "ConversationFragment";
    public static final int GET_SQLITE_DATA_DEFALUT_COUNT = 10;
    public static final String CONVERSATION_TYPE = "conversation_type";

    private static final int HANDLE_NOTIFY_LOAD_DATA = 1101;
    private static final int HANDLE_GET_DISCUSSION_INFO = 1103;
    private static final int HANDLE_GET_USERINFO_FOR_TITLE = 1104;

    private LoadingDialog mDialog;
    private PullDownRefreshListView mListView;
    private ConversationMessageBar mConversationMessageBar;

    private TextView mConnectStateTextView;
    private CoverFrameLayout mFrameLayout;
    private VoiceCoverView mCoverView;


    private IVoiceHandler mVoiceHandler;
    private View mHideCover;

    private MitulItemViewListAdapter mConversationAdapter;

    private UIConversation mConversation;

    private ConcurrentHashMap<Long, Integer> mMessageIdMap = new ConcurrentHashMap<Long, Integer>();

    private boolean mIsHaveDBMessage = true;

    private int mUnReadMessageCount = 0;
    private long mLastSelectMsgId;

    private boolean mIsSendingMessage = false;


    private int mConnectNetStatueValue = -1;

    private ConversationType tempConversationType;


    @Override
    public void onResume() {
        RCloudContext.getInstance().setNotificationNewMessageCount(0);
        RCloudContext.getInstance().clearNotificationUserIdList();
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
//=============发送音频相关=======================================================================
        mVoiceHandler = new IVoiceHandler.VoiceHandler(getActivity(), new File(RCloudContext.getInstance().getAppResourceDir(), "voice"));

        Intent intent = getActivity().getIntent();

        if (intent != null) {

            if (intent.getData() != null && intent.getData().getScheme().equals("rong")) {//push或通知过来

                if (intent.getExtras() != null && intent.getExtras().get("appId") != null) {
                    parserIntent(intent);
                    pushNotification();
                } else {// 第三方 只传给我们的ID
                    parserIntent(intent);
                }
            } else {
                // 下面两个参数来ConversationListActivity页面传值
                mConversation = intent.getParcelableExtra(RCloudConst.EXTRA.CONVERSATION);
                mConnectNetStatueValue = intent.getIntExtra(ConversationListFragment.INTENT_CONNECT_NET_STATE, -1);
            }
        }

        setCurrentCoversationTargetId(mConversation.getTargetId());

        super.onCreate(savedInstanceState);

    }

    @Override//================注册广播，MessageLogic中发送广播=========处理这个广播的接收器是BaseReceiver==定义在父类ActionBaseFragment中===============
    public void registerActions(List<String> actions) {

        actions.add(MessageLogic.ACTION_P2P_MESSAGE_RECEVICE);//二人会话接收消息
        actions.add(MessageLogic.ACTION_GROUP_MESSAGE_RECEVICE);
        actions.add(MessageLogic.ACTION_DISCUSSION_MESSAGE_RECEVICE);
        actions.add(RCloudContext.CLIENT_CONNECTED_TO_SDK);
        actions.add(RCloudContext.CLIENT_DISCONNECT_TO_SDK);
        actions.add(MessageLogic.ACTION_RECEVICE_NETWORD_CONNECT_STATE);

        super.registerActions(actions);
    }
    
  //==================================================收到一条新消息的回调===MessageLogic发过来的广播==也就是	mRongIMClient.setOnReceiveMessageListener(new OnReceiveMessageListener() {}执行后的回调================================================================================
    @Override
    public void receviceData(Intent intent) {
        super.receviceData(intent);

        String action = intent.getAction();

        Log.d("ConversationActivity", "-------recrviceData-------");

        if (MessageLogic.ACTION_P2P_MESSAGE_RECEVICE.equals(action) || MessageLogic.ACTION_DISCUSSION_MESSAGE_RECEVICE.equals(action)) {

            UIMessage message = intent.getParcelableExtra(UIMessage.MESSAGE_OBJ);

            if (mConversation.getTargetId().equals(message.getTargetId())) {

                Log.d("ConversationActivity---", "msg--" + message.getContent());
                mConversationAdapter.addData(Arrays.asList(message));
                mConversationAdapter.notifyDataSetChanged();
                mListView.setSelection(mConversationAdapter.getCount() + mListView.getHeaderViewsCount());

            } else {
                mUnReadMessageCount++;
                showNewMessage(mUnReadMessageCount);
            }

        } else if (MessageLogic.ACTION_RECEVICE_NETWORD_CONNECT_STATE.equals(action)) {

            int connectStateValue = intent.getIntExtra(MessageLogic.INTENT_NETWORK_CONNECT_STATE, -200);

            Log.d(TAG, "ACTION_RECEVICE_NETWORD_CONNECT_STATE---:" + action + "|connectStateValue:" + connectStateValue);

            setNetStatus(connectStateValue);
        }
    }

    @Override
    public void registerBunlderActions(List<String> actions) {

        actions.add(ACTION_BUNDLE_IO_RONG_IMKIT_CONVERSATION_SETTING);
        actions.add(ACTION_BUNDLE_IO_RONG_IMKIT_FRIEND_SELECT);

        super.registerBunlderActions(actions);
    }

    private final void parserIntent(Intent intent) {//push 通知共用

        mConversation = new UIConversation();

        String targetId = intent.getData().getQueryParameter("targetId");

        ConversationType conversationType = ConversationType
                .valueOf(intent.getData().getLastPathSegment().toUpperCase());

        if (conversationType != null && conversationType == ConversationType.CUSTOMERSERVICE) {
            tempConversationType = conversationType;
            conversationType = ConversationType.PRIVATE;
        }

        mConversation.setConversationType(conversationType);

        mConversation.setTargetId(targetId);
        mConversation.setConversationTitle(intent.getData().getQueryParameter("title"));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(ResourceUtils.getLayoutResourceId(getActivity(), "rc_fragment_conversation"), null);
        mListView = getViewById(view, android.R.id.list);
//====================消息输入框=================================================================
        mConversationMessageBar = getViewById(view, "conversation_message_bar");
        mConversationMessageBar.setConversationMessageBarLinstener(this);//注册  单击“录音/键盘切换”使软件盘的打开关闭
        
        mConnectStateTextView = getViewById(view, "connect_state");//连接状态view

        mFrameLayout = getViewById(view, "rong_cover_layout");
//=========================按住声音控件时，在屏幕中间显示“提示框”===========================================================
        mCoverView = getViewById(view, "rong_voice_cover");
        mHideCover = getViewById(view, android.R.id.toggle);//覆盖全屏的view  初始gone
       
        if (getActionBar() != null) {

            if (ConversationType.CUSTOMERSERVICE != tempConversationType) {//不是讨论组时，在titleBar的右侧要显示一个icon

                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                ImageView settingView = new ImageView(getActivity());
                settingView.setLayoutParams(layoutParams);
                settingView.setImageDrawable(ResourceUtils.getDrawableById(getActivity(), "rc_bar_more"));

                settingView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RongIM.getInstance().startConversationSetting(ConversationFragment.this.getActivity(), mConversation.getConversationType(), mConversation.getTargetId());
                    }
                });

                getActionBar().addView(settingView);
            }
//单击actionBar的回退 按钮  触发的事件
 getActionBar().setOnBackClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setConversationResult();
                    getActivity().finish();
                }
            });


            if (!TextUtils.isEmpty(mConversation.getConversationTitle())) {
                getActionBar().getTitleTextView().setText(mConversation.getConversationTitle());
            }
        }
        return view;
    }

    private void pushNotification() {

        Log.d("intent", "============pushNotification=========conversation==========");
        mDialog = new LoadingDialog(getActivity());
        mDialog.setText(ResourceUtils.getStringResource(getActivity(), "converastion_list_notify_load_data"));
        mDialog.setTextColor(Color.WHITE);
        mDialog.show();

        SharedPreferences preferences = getActivity().getSharedPreferences("rc_token", Context.MODE_PRIVATE);
        String token = preferences.getString("token_value", "");

        Log.d("intent", "============getIntent===========" + getActivity().getIntent().getExtras().get("channelId") + "====token=" + token);

        RongIM.init(getActivity(), (String) getActivity().getIntent().getExtras().get("appId"), 0);

        try {
            RongIM.connect(token, new io.rong.imlib.RongIMClient.ConnectCallback() {

                @Override
                public void onSuccess(String userId) {
                    Log.d("adf", "==================onSuccess================" + userId);
                    getHandler().obtainMessage(HANDLE_NOTIFY_LOAD_DATA).sendToTarget();
                }

                @Override
                public void onError(ErrorCode errorCode) {
                    Log.d("adf", "===================onError===============" + errorCode);
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent();
        intent.setAction(PushService.NOTIFICATION_SENDED_BROADCAST_ACTION);
        intent.putExtra("appId", (String) getActivity().getIntent().getExtras().get("appId"));
        getActivity().sendBroadcast(intent);// 发送广播
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        if (getActivity().getIntent().getData() != null &&
                getActivity().getIntent().getData().getScheme().equals("rong") &&
                getActivity().getIntent().getExtras() != null &&
                getActivity().getIntent().getExtras().get("appId") != null) {//from push
            return;
        }
        //取得“ 草稿   ”
        String draft = RCloudContext.getInstance().getRongIMClient().getTextMessageDraft(mConversation.getConversationType(), mConversation.getTargetId());

        if (!TextUtils.isEmpty(draft)) {//设置草稿
            mConversationMessageBar.setMessageEditText(Util.highLight(HighLightUtils.loadHighLight(draft)));
        }

        initListView();
    }

    private void initListView() {
        List<UIMessage> list = null;

        mConversationAdapter = new MitulItemViewListAdapter(new MessageContext(getActivity(), mVoiceHandler));
        mListView.setAdapter(mConversationAdapter);
        mListView.setonRefreshListener(this);
        mListView.setOnTouchListener(this);
        mFrameLayout.setOnTriggeredTouchListener(this);
      //================================设置“voip，等按钮的回调”========================================
        mConversationMessageBar.setOnRichIconTextViewClickLinstener(this);
        mConversationAdapter.setOnMessageItemClickListener(this);
        mConversationAdapter.setOnGetDataListener(this);
//======================================初始化时取得最新消息的列表数据======================================================================================================
        if (mConversation != null) {
list = DBHelper.getInstance().getLasetMessageList(mConversation.getConversationType(), mConversation.getTargetId(), GET_SQLITE_DATA_DEFALUT_COUNT);
        }

        if (list == null || list.size() < 10) {
            mIsHaveDBMessage = false;
        }

        if (list != null) {
            mConversationAdapter.addData(reverseList(list));
            mConversationAdapter.notifyDataSetChanged();
        }

//================设置titlebar上的“未读新消息数view”============================================================================
        mUnReadMessageCount = DBHelper.getInstance().getTotalUnreadCount();
 showNewMessage(mUnReadMessageCount);

        if (RCloudContext.getInstance().getConversationLaunchedListener() != null) {
            RCloudContext.getInstance().getConversationLaunchedListener().onCreated(mConversation.getConversationType(), mConversation.getTargetId());
        }

        mListView.setSelection(mConversationAdapter.getCount() + mListView.getHeaderViewsCount());


//=================== 初始化title name=========================================
        if (TextUtils.isEmpty(mConversation.getConversationTitle())) {

            if (mConversation.getConversationType() == ConversationType.PRIVATE) {

                getHandler().post(new Runnable() {

                    @Override
                    public void run() {

                        getUserInfo(mConversation.getTargetId(), new GetUserInfoCallback() {

                            @Override
                            public void onSuccess(UIUserInfo user) {
                                getHandler().obtainMessage(HANDLE_GET_USERINFO_FOR_TITLE, user).sendToTarget();
                            }

                            @Override
                            public void onError() {

                            }

                        });
                    }
                });

            } else if (mConversation.getConversationType() == ConversationType.DISCUSSION) {

                getHandler().post(new Runnable() {

                    @Override
                    public void run() {
                        getDiscussionInfo(0, mConversation.getTargetId());
                    }

                });
            }
        }

//================处理 按住声音控件时，在屏幕中间显示“提示框”的回调函数=============================================================================
        mCoverView.setVoiceHandler(mVoiceHandler);
        mVoiceHandler.setRecListener(new IVoiceHandler.OnRecListener() {
            @Override
            public void onRec() {
                mConversationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCover(boolean limited) {
                if (limited) {
                    if (mHideCover.getVisibility() == View.GONE)
                        mHideCover.setVisibility(View.VISIBLE);
                } else {
                    if (mHideCover.getVisibility() == View.VISIBLE)
                        mHideCover.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCompleted(Uri uri) {
                if (mHideCover.getVisibility() == View.VISIBLE) {
                    mHideCover.setVisibility(View.GONE);
                }
                publishVoice(uri, mCoverView.getLastVoiceLength());
            }
        });
        mVoiceHandler.setPlayListener(new IVoiceHandler.OnPlayListener() {
            @Override
            public void onPlay() {
                mConversationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCover(boolean limited) {
                if (limited) {
                    if (mHideCover.getVisibility() == View.GONE)
                        mHideCover.setVisibility(View.VISIBLE);
                } else {
                    if (mHideCover.getVisibility() == View.VISIBLE)
                        mHideCover.setVisibility(View.GONE);
                }
            }

            @Override
            public void onStop() {
                mConversationAdapter.notifyDataSetChanged();
                if (mHideCover.getVisibility() == View.VISIBLE)
                    mHideCover.setVisibility(View.GONE);
            }
        });

        if (mConnectNetStatueValue != -1) {
            setNetStatus(mConnectNetStatueValue);//=========================界面端实现网络连接状态的view=====
        }

        if (mConversation.getConversationType() == ConversationType.PRIVATE ) {
            mConversationMessageBar.getVOIPRichIconTextView().setVisibility(View.VISIBLE);
        } else {
            mConversationMessageBar.getVOIPRichIconTextView().setVisibility(View.GONE);
        }

        if(tempConversationType!=null && ConversationType.CUSTOMERSERVICE == tempConversationType){
            mConversationMessageBar.getVOIPRichIconTextView().setVisibility(View.GONE);
        }
    }

    @Override// 点击“录音/键盘切换”按钮触发的事件         =============开关软键盘的回调=======
    public void toggleInputMethod(boolean isOpen, View view) {
        Log.d(TAG, "---------toggleInputMethod-----------" + isOpen);

        InputMethodManager inputMethodManager = ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));

        if (isOpen) {
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            isVisibility();
        } else {
            if (inputMethodManager.isActive()) {
                inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    @Override  //===============单击“发送”按钮的触发事件===========笑脸也是字符串形式保存===========发送图片及语音不用使用这个方法触发==========================
    public void sendMessage(String msgContent) {

        UIMessage message = messageWrap();

        TextMessage textMessage = new TextMessage(msgContent);
        message.setContent(textMessage);
        message.setMessageId(System.currentTimeMillis());

        mConversationAdapter.addData(Arrays.asList(message));
        mConversationAdapter.notifyDataSetChanged();
        mListView.setSelection(mConversationAdapter.getCount() + mListView.getHeaderViewsCount());

        Log.d("conversationActivity--sendmessage", "messageId:" + message.getMessageId() + " position:" + (mConversationAdapter.getCount() - 1));

        if (mConversationAdapter.getCount() >= 1) {
            mMessageIdMap.put(message.getMessageId(), mConversationAdapter.getCount() - 1);
        } else {
            mMessageIdMap.put(message.getMessageId(), 0);
        }

        sendMessage(message);

    }

    private final List<UIMessage> reverseList(List<UIMessage> list) {

        List<UIMessage> messags = new ArrayList<UIMessage>();

        for (int i = list.size() - 1; i >= 0; i--) {
            messags.add(list.get(i));
        }

        if (messags != null && messags.size() > 0) {
            mLastSelectMsgId = ((UIMessage) messags.get(0)).getMessageId();
        }

        return messags;
    }

    private final UIMessage messageWrap() {

        UIMessage message = new UIMessage();
        message.setTargetId(mConversation.getTargetId());

        message.setMessageDirection(MessageDirection.SEND);
        message.setSending(true);

        UserInfo uerInfo = RCloudContext.getInstance().getRongIMClient().getCurrentUserInfo();
        if (uerInfo != null) {
            message.setSenderUserId(uerInfo.getUserId());
        }

        message.setSentTime(System.currentTimeMillis());

        message.setSentStatus(SentStatus.SENDING);
        message.setConversationType(mConversation.getConversationType());

        return message;
    }


    //处理图片发送时候的Extra数据
    private void imageMessageSuccessProcess(UIMessage message) {
        String ex = message.getExtra();
        if (!TextUtils.isEmpty(ex)) {

            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            URLEncodedUtils.parse(pairs, new Scanner(ex), HTTP.UTF_8);
            NameValuePair uriPair = null;
            for (NameValuePair pair : pairs) {
                if (pair.getName().equals("image_uri")) {
                    uriPair = pair;
                    break;
                }
            }

            if (uriPair != null) {
                Uri uri = Uri.parse(uriPair.getValue());

                File file = new File(uri.getPath());
                if (file.exists()) {
                    file.delete();
                }
            }


        }

        RCloudContext.getInstance().getRongIMClient().setMessageExtra(message.getMessageId(), null);
    }
//====================发送文件消息的方法========================================================================
    public void sendMessage(UIMessage message) {

        Intent intent = new Intent();
        intent.setAction(MessageLogic.ACTION_P2P_MESSAGE_SEND);
        intent.putExtra(UIMessage.MESSAGE_OBJ, message);

        mIsSendingMessage = true;
//========================发送消息的过程1.启动RCloudService并执行onHandleAction方法，2.MessaageLogic类的实现了ActionListener接口并实现了onHandleAction方法也就是在这个方法中处理发送消息=====================
        sendAction(intent, new ActionCallback() {
            @Override
            public void callback(Intent intent) {//发送成功与否的回调

                if (intent != null) {
                    Log.d("conversationActivity", "send message enter---callback--11111111-");

                    boolean sendState = intent.getBooleanExtra(MessageLogic.SEND_MESSAGE_STATE, false);
                    boolean isComplete = intent.getBooleanExtra(MessageLogic.INTENT_IS_COMPLETE, false);
                    int progress = intent.getIntExtra(MessageLogic.INTENT_MESSAGE_FILE_DOWN_PROGRESS, 100);

                    long messageIdTemp = intent.getLongExtra(MessageLogic.MESSAGE_ID_TEMP_SEND_MESSAGE_RETURN, -1);

                    if (isComplete) {// 消息发送完成

                        UIMessage mUiMessage = mConversationAdapter.getItem(mMessageIdMap.get(messageIdTemp));

                        if (sendState) {// 发送成功
                            mUiMessage.setSending(false);

                            if (mUiMessage.getContent() instanceof ImageMessage) {
                                imageMessageSuccessProcess(mUiMessage);
                            }

                            mUiMessage.setSentStatus(SentStatus.SENT);
                            getHandler().obtainMessage(HANDLE_ADAPTER_NOTIFY).sendToTarget();
                            Log.d("conversationActivity", "send---message---is--33333333333333-success");
                            mIsSendingMessage = false;
                        } else {// 发送失败
                            mUiMessage.setSending(false);
                            mUiMessage.setSentStatus(SentStatus.FAILED);
                            getHandler().obtainMessage(HANDLE_ADAPTER_NOTIFY).sendToTarget();
                            Log.d("conversationActivity", "send---message---is--44444444444--failure");
                            mIsSendingMessage = false;
                        }

                        mMessageIdMap.remove(messageIdTemp);

                    } else {// 消息未发送完成

                        UIMessage message = intent.getParcelableExtra(MessageLogic.MESSAGE_OBJ_SEND_MESSAGE_RETURN);

                        int position = mMessageIdMap.get(messageIdTemp);
                        UIMessage uiMessage = mConversationAdapter.getItem(position);

                        if (progress > 0 && uiMessage.getContent() instanceof ImageMessage) {// 发图进度
                            Log.d(TAG, "sendPicMessage---progress--" + progress);

                            if (message != null) {// 第一次返回
                                Log.d(TAG, "sendAction--callback--image key---->" + ((ImageMessage) uiMessage.getContent()).getImageKey());
                                mConversationAdapter.setItem(position, message);// 替换当前消息
                                message.setProgressText(progress);
                            } else {
                                uiMessage.setProgressText(progress);
                            }

                            getHandler().obtainMessage(HANDLE_ADAPTER_NOTIFY).sendToTarget();
                            return;
                        }

                        if (message != null) {// 第一次返回

                            Log.d("conversationActivity", "send message first return ------->" + messageIdTemp);

                            if (mMessageIdMap.containsKey(messageIdTemp)) {

                                message.setSentStatus(SentStatus.SENDING);
                                message.setSending(true);
                                mConversationAdapter.setItem(position, message);// 替换当前消息
                                getHandler().obtainMessage(HANDLE_ADAPTER_NOTIFY).sendToTarget();

                                Log.d("conversationActivity", "reset message|" + messageIdTemp);

                                return;
                            }
                        }
                    }
                }
            }
        });

    }

    @Override
    public void onRefresh() {

        Log.d(TAG, "--------onRefresh-----setSelection--11111111-");

        if ((mConversationAdapter != null && mConversationAdapter.getCount() < 10) || !mIsHaveDBMessage) {
            mListView.onRefreshComplete();
            return;
        }

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {

            @Override
            public void run() {

                List<UIMessage> list = DBHelper.getInstance().getMessageList(mConversation.getConversationType(), mConversation.getTargetId(), mLastSelectMsgId,
                        GET_SQLITE_DATA_DEFALUT_COUNT);

                int count = list.size();

                if (list == null || list.size() == 0) {
                    mIsHaveDBMessage = false;
                }

                if (list != null) {
                    mConversationAdapter.addData(0, reverseList(list));
                    mConversationAdapter.notifyDataSetChanged();
                }

                mListView.onRefreshComplete();
                Log.d(TAG, "--------onRefresh-----setSelection---");

                if (count > 0) {
                    mListView.setSelection(count);
                }

            }
        }, 2000);
    }


    @Override
    public void recevicePageIntnet(Intent intent) {
        String action = intent.getAction();


        Log.d(TAG, "recevicePageIntnet:" + action);


        if (ACTION_BUNDLE_IO_RONG_IMKIT_CONVERSATION_SETTING.equals(action)) {//1、清除聊天消息 2、退出讨论组

            if (intent != null) {

                boolean isQuitDiscussion = intent.getBooleanExtra(ConversationSettingFragment.INTENT_QUIT_DISCUSSION_CLOSE_PAGE, false);
                boolean isClearMessages = intent.getBooleanExtra(ConversationSettingFragment.INTENT_CLEAR_MESSSAGE_SUCCESS, false);
                String discussionName = intent.getStringExtra(ConversationSettingFragment.INTENT_UPDATE_NAME_DISCUSSION);
                String createDiscussion = intent.getStringExtra(ConversationSettingFragment.INTENT_CREATE_DISCUSSION_SUCCESS);

                if (!TextUtils.isEmpty(createDiscussion)) {
                    getActivity().finish();
                }

                if (!TextUtils.isEmpty(discussionName)) {
                    getActionBar().getTitleTextView().setText(discussionName);
                    mConversation.setConversationTitle(discussionName);
                }

                if (isQuitDiscussion) {
                    getActivity().finish();
                }

                if (isClearMessages) {
                    mConversationAdapter.removeAll();
                    mConversationAdapter.notifyDataSetChanged();
                }
            }
        }
        super.recevicePageIntnet(intent);
    }

//=========================界面端实现网络连接状态的view==============================================================================================
    private final void setNetStatus(int connectStateValue) {

        ConnectionStatus connectionStatus = ConnectionStatus.setValue(connectStateValue);

        if (connectionStatus == ConnectionStatus.Cellular_2G || connectionStatus == ConnectionStatus.Cellular_3G_4G || connectionStatus == ConnectionStatus.WIFI) {
            mConnectStateTextView.setVisibility(View.GONE);
            return;
        }


        if (connectionStatus == ConnectionStatus.AIRPLANE_MODE || connectionStatus == ConnectionStatus.NETWORK_UNAVAILABLE) {
            mConnectStateTextView.setText(ResourceUtils.getStringResource(getActivity(), "conntect_state_prompt_network_unavailable"));
        } else if (connectionStatus == ConnectionStatus.UNKNOWN) {
            mConnectStateTextView.setText(ResourceUtils.getStringResource(getActivity(), "conntect_state_prompt_unknow_error"));
        } else if (connectionStatus == ConnectionStatus.LOGIN_ON_WEB || connectionStatus == ConnectionStatus.KICKED_OFFLINE_BY_OTHER_CLIENT) {
            mConnectStateTextView.setText(ResourceUtils.getStringResource(getActivity(), "conntect_state_prompt_other_device_login"));
        } else if (connectionStatus == ConnectionStatus.SERVER_INVALID) {
            mConnectStateTextView.setText(ResourceUtils.getStringResource(getActivity(), "conntect_state_prompt_service_unavailable"));
        } else if (connectionStatus == ConnectionStatus.VALIDATE_INVALID) {
            mConnectStateTextView.setText(ResourceUtils.getStringResource(getActivity(), "conntect_state_prompt_auth_unavailable"));
        }

        mConnectStateTextView.setVisibility(View.VISIBLE);
    }

//==========================设置新消息数量  view===============================================================================================
    private final void showNewMessage(int messageCount) {

        if (getActionBar() != null && messageCount > 0) {
            if (getActionBar().getNewMessageView() != null) {
                String moreMsgFlag = null;

                if (messageCount > 99) {
                    moreMsgFlag = ResourceUtils.getStringResource(getActivity(), "new_message_more");
                } else {
                    moreMsgFlag = String.valueOf(messageCount);
                }
                getActionBar().getNewMessageView().setText(moreMsgFlag);
                getActionBar().getNewMessageView().setVisibility(View.VISIBLE);
            }
        } else {
            if (getActionBar().getNewMessageView() != null) {
                getActionBar().getNewMessageView().setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (v.getId()) {
            case android.R.id.list:
                toggleInputMethod(false, v);

                if (mConversationMessageBar.isShowRichOrExpressionView()) {
                    mConversationMessageBar.setHiddenRichAndExpressionView();
                }
                break;

            default:
                break;
        }
        return false;
    }

    @Override
    protected void rongHandleMessage(Message msg) {

        if (msg != null) {
            switch (msg.what) {
                case HANDLE_ADAPTER_NOTIFY:
                    mConversationAdapter.notifyDataSetChanged();
                    Log.d("ConversationActivity----handleMessage---adapter.count", "" + mConversationAdapter.getCount());
                    break;
                case HANDLE_NOTIFY_LOAD_DATA:
                    initListView();
                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                    break;
                case HANDLE_GET_USER_INFO_WHAT:

                    int position = msg.arg1;
                    UIMessage message = (UIMessage) msg.obj;
                    UIMessage uiMessage = mConversationAdapter.getItem(position);

                    if (uiMessage.getMessageId() == message.getMessageId()) {
                        uiMessage.setUserInfo(message.getUserInfo());
                        // mConversationAdapter.notifyDataSetChanged(position);
                    } else {
                        //消息被删除 位置变化
                    }
                    mConversationAdapter.notifyDataSetChanged();

                    break;
                case HANDLE_GET_DISCUSSION_INFO:
                    UIDiscussion discussion = (UIDiscussion) msg.obj;

                    if (discussion != null) {
                        mConversation.setConversationTitle(discussion.getName());
                        mConversation.setUiDiscussion(discussion);
                    }

                    if (!TextUtils.isEmpty(mConversation.getConversationTitle())) {
                        getActionBar().getTitleTextView().setText(mConversation.getConversationTitle());
                    }

                    break;
                case HANDLE_GET_USERINFO_FOR_TITLE:
                    UIUserInfo uiUserInfo = (UIUserInfo) msg.obj;

                    if (uiUserInfo != null) {
                        mConversation.setConversationTitle(uiUserInfo.getName());
                        mConversation.setUserInfo(uiUserInfo);
                    }

                    if (!TextUtils.isEmpty(mConversation.getConversationTitle())) {
                        getActionBar().getTitleTextView().setText(mConversation.getConversationTitle());
                    }
                    break;
                default:
                    break;
            }

        }
    }

    @Override
    public void onImageRichTextClick() {
        gotoSysPic();
    }

    @Override
    public void onCameraRichTextClick() {
        gotoSysCamera();
    }

    @Override
    public void onLocatonRichTextClick() {

    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.obj == null) {
                return;
            }
            final UIUserInfo peerUser = (UIUserInfo) msg.obj;
            final String mySelfId = RCloudContext.getInstance().getRongIMClient().getCurrentUserInfo().getUserId();
            final String mySelfName = mConversationAdapter.getUserInfo(mySelfId) == null ? "未知" : mConversationAdapter.getUserInfo(mySelfId).getName();

            Log.i("aff", "===========onVoipRichTextClick==================mySelfId=" + mySelfId + "====peerUId=" + peerUser.getUserId() + "===" + peerUser.getName() + "===" + mySelfName +
                    "===" + peerUser.getPortraitUri());
            if(mySelfId.equals(peerUser.getUserId())){
            	Toast.makeText(getActivity(), "不支持自己给自己语音通话！",Toast.LENGTH_LONG).show();
            	return;
            }
            NetworkUtil.isConnectivityAvailable(getActivity(), new NetworkUtil.DoEventByConnectivityState() {
                @Override
                public void doWarnCall() {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("当前是3G网络，是否继续？")
                            .setMessage("确定吗？")
                            .setPositiveButton("是", new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent videoChatInt = new Intent(getActivity(), CallSideActivity.class);


                                    videoChatInt.putExtra("appId", RCloudContext.getInstance().getAppKey());
                                    videoChatInt.putExtra("token", getActivity().getSharedPreferences("rc_token", Context.MODE_PRIVATE).getString("token_value", ""));
                                    videoChatInt.putExtra("mySelfId", mySelfId);
                                    videoChatInt.putExtra("myselfName", mySelfName);
//                                    videoChatInt.putExtra("peerUId", peerUser.getUserId());
                                    videoChatInt.putExtra("peerUId", mySelfId);
                                    videoChatInt.putExtra("peerUserName", peerUser.getName());
                                    videoChatInt.putExtra("peerUserPhoteUri", peerUser.getPortraitUri());

                                    startActivity(videoChatInt);
                                }
                            })
                            .setNegativeButton("否", null)
                            .show();
                }

                @Override
                public void doCall() {
                    Intent videoChatInt = new Intent(getActivity(), CallSideActivity.class);

                    videoChatInt.putExtra("appId", RCloudContext.getInstance().getAppKey());
                    videoChatInt.putExtra("token", getActivity().getSharedPreferences("rc_token", Context.MODE_PRIVATE).getString("token_value", ""));
                    videoChatInt.putExtra("mySelfId", mySelfId);
                    videoChatInt.putExtra("myselfName", mySelfName);
                    videoChatInt.putExtra("peerUId", peerUser.getUserId());
                    videoChatInt.putExtra("peerUserName", peerUser.getName());
                    videoChatInt.putExtra("peerUserPhoteUri", peerUser.getPortraitUri());

                    startActivity(videoChatInt);
                }
            });
        }
    };

    @Override
    public void onVoipRichTextClick() {

        getUserInfo(mConversation.getTargetId(), new GetUserInfoCallback() {
            @Override
            public void onSuccess(UIUserInfo user) {
                android.os.Message msg = mHandler.obtainMessage();
                msg.obj = user;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onError() {
            }
        });
    }

    @Override
    public void onAddRichTextClick() {

    }

    @Override
    public void isVisibility() {

        if (mListView != null && mConversationAdapter != null) {

            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    mListView.setSelection(mConversationAdapter.getCount() + mListView.getHeaderViewsCount());
                }
            });

            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mListView.setSelection(mConversationAdapter.getCount() + mListView.getHeaderViewsCount());
                }
            }, 500);


        }
    }
//===================================将发送的图片放入列表中=========调用onImageRichTextClick后回调这个方法=========================================================================
    @Override
    public void publishBitmap(Uri uri) {
//file:///data/data/io.rong.imkit.demo/cache/1796190353
        ImageMessage imageMessage = new ImageMessage(uri);
        UIMessage uiMessage = messageWrap();
        uiMessage.setContent(imageMessage);
        uiMessage.setMessageId(System.currentTimeMillis());

        List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
        pairs.add(new BasicNameValuePair("image_uri", uri.toString()));
        
        //image_uri=file%3A%2F%2F%2Fdata%2Fdata%2Fio.rong.imkit.demo%2Fcache%2F-1535573698
        String encoder = URLEncodedUtils.format(pairs, HTTP.UTF_8);
        uiMessage.setExtra(encoder);

        mConversationAdapter.addData(Arrays.asList(uiMessage));
        mConversationAdapter.notifyDataSetChanged();
        mListView.setSelection(mConversationAdapter.getCount() + mListView.getHeaderViewsCount());

        if (mConversationAdapter.getCount() >= 1) {
            mMessageIdMap.put(uiMessage.getMessageId(), mConversationAdapter.getCount() - 1);
        }

        sendMessage(uiMessage);
    }
//========================单击列表中的图片与声音触发的函数==================================================================================
    @Override
    public void onMessageClick(final UIMessage message, final View view) {


        if (message != null) {

            if (message.getContent() instanceof ImageMessage) {
                ImageMessage imageMessage = null;
                String imageKey = null;

                imageMessage = (ImageMessage) message.getContent();

                if (imageMessage != null) {
//                    imageKey = imageMessage.getImageKey();
                    imageKey = imageMessage.getImageKey();
                    Log.d(TAG, "ConversationActionActivity----onMessageClick---imageKey:" + imageKey);

                    if (!TextUtils.isEmpty(imageKey)) {
                        Intent intent = new Intent(getActivity(), RongActivity.class);
                        intent.putExtra(RCloudConst.EXTRA.CONTENT, ShowDownloadImageFragment.class.getCanonicalName());
                        intent.putExtra(MessageLogic.TARGET_ID, mConversation.getTargetId());
                        intent.putExtra(ShowDownloadImageFragment.MESSAGE_IMAGE_KEY, imageKey);
                        startActivity(intent);
                    }
                }

            } else if (message.getContent() instanceof VoiceMessage) {

                if (message.getMessageDirection() == MessageDirection.RECEIVE) {
                    DBHelper.getInstance().setMessageExtra(message.getMessageId(), UIMessage.MESSAGE_VOICE_READED);
                }

                message.setExtra(UIMessage.MESSAGE_VOICE_READED);
                // mConversationAdapter.notifyDataSetChanged();

                VoiceMessage voiceMessage = (VoiceMessage) message.getContent();

                ImageView imageView = null;

                if (view != null && view instanceof ImageView) {
                    imageView = (ImageView) view;
                }

                if (voiceMessage != null) {

//                    final String filePath = voiceMessage.getUri().getPath();

                    if (MessageDirection.SEND == message.getMessageDirection()) {
                        AnimationDrawable drawable = (AnimationDrawable) ResourceUtils.getDrawableById(getActivity(), "rc_send_voice_anim");
                        imageView.setImageDrawable(drawable);
                    } else {
                        AnimationDrawable drawable = (AnimationDrawable) ResourceUtils.getDrawableById(getActivity(), "rc_receive_voice_anim");
                        imageView.setImageDrawable(drawable);
                    }

                    if (message.getReceivedStatus() != null && !message.getReceivedStatus().isListened()) {
                        message.getReceivedStatus().setListened();
                    }

                    final ImageView temImageView = imageView;


                    temImageView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (temImageView.getDrawable() instanceof AnimationDrawable)
                                ((AnimationDrawable) temImageView.getDrawable()).start();
                        }
                    });

                    try {
                        if (voiceMessage.getUri().equals(mVoiceHandler.getCurrentPlayUri()))
                            mVoiceHandler.stop();
                        else
                            mVoiceHandler.play(voiceMessage.getUri());

                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }

                }
            }

        }
    }
//========================长按“文本消息”的触发===弹出“复制/删除”消息=========================================================================
    @Override
    public void onMessageLongClick(UIMessage message) {

        final SelectDialog mSelectDialog = new SelectDialog(getActivity());
        final UIMessage fmessage = message;

        String username = fmessage.getUserInfo().getName();
        if (TextUtils.isEmpty(username)) {
            UserInfo userInfo = mConversationAdapter.getUserInfo(fmessage.getUserInfo().getUserId());
            if (userInfo != null) {
                username = userInfo.getName();
            }
        }

        mSelectDialog.setTitle(username);

        if (fmessage.getContent() instanceof TextMessage) {
            mSelectDialog.setFristLineContent("dialog_converastion_cope_message");
            mSelectDialog.setSecondLineContent("dialog_converastion_delete_message");
        } else {
            mSelectDialog.setDiaogLayoutFirstGone();
            mSelectDialog.setSecondLineContent("dialog_converastion_delete_message");
        }

        mSelectDialog.setOnDialogItemViewListener(new OnDialogItemViewListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void OnDialogItemViewClick(View view, int position) {
                Log.d("onItemLongClick---", "position:   " + "" + position);

                if (position == 0) {
                    @SuppressWarnings("deprecation")
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(fmessage.getTextMessageContent());
                } else if (position == 1) {
                    long id = fmessage.getMessageId();
                    ArrayList<Long> ids = new ArrayList<Long>();
                    ids.add(id);
                    DBHelper.getInstance().deleteMessage(ids);
                    mConversationAdapter.remove(fmessage.getPositionInList());
                    mConversationAdapter.notifyDataSetChanged();
                }

                mSelectDialog.dismiss();

            }
        });

        mSelectDialog.show();
    }

    @Override
    public void onMessageDoubleClick(UIMessage message) {
    }

    public void publishVoice(Uri uri, long voiceLength) {

        if (voiceLength <= 1000) {
            Toast.makeText(getActivity(), ResourceUtils.getStringResourceId(getActivity(), "voice_dialog_time_short"), Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] voiceData = Util.getByteFromUri(getActivity(), uri);

        if (voiceData == null) {
            Log.d("publishVoice--", "voiceData is null null null");
            return;
        }

        UIMessage message = messageWrap();

        VoiceMessage voiceMessage = new VoiceMessage(Uri.parse(uri.getPath()), (int) (voiceLength / 1000));
        message.setContent(voiceMessage);

        message.setMessageId(System.currentTimeMillis());

        sendMessage(message);

        mConversationAdapter.addData(Arrays.asList(message));
        mConversationAdapter.notifyDataSetChanged();
        mListView.setSelection(mConversationAdapter.getCount() + mListView.getHeaderViewsCount());

        if (mConversationAdapter.getCount() >= 1) {
            mMessageIdMap.put(message.getMessageId(), mConversationAdapter.getCount() - 1);
        }

    }

    @Override
    public void onDestroy() {

        if (RCloudContext.getInstance().getConversationLaunchedListener() != null) {
            RCloudContext.getInstance().getConversationLaunchedListener().onDestroyed();
        }

        super.onDestroy();
    }

    @Override
    public void onPause() {
        mVoiceHandler.stopRec(false);
        mVoiceHandler.stop();
        setConversationResult();
        super.onPause();
    }


    @Override
    public void onResendMessage(UIMessage message, int position) {

        if (mIsSendingMessage || mConnectStateTextView.getVisibility() == View.VISIBLE) {
            return;
        }

        if (message != null) {

            ArrayList<Long> messageIds = new ArrayList<Long>();
            messageIds.add(message.getMessageId());
            DBHelper.getInstance().deleteMessage(messageIds);
            mConversationAdapter.remove(position);

            mConversationAdapter.addData(Arrays.asList(message));
            mConversationAdapter.notifyDataSetChanged();
            mListView.setSelection(mConversationAdapter.getCount() + mListView.getHeaderViewsCount());

            if (mConversationAdapter.getCount() >= 1) {
                mMessageIdMap.put(message.getMessageId(), mConversationAdapter.getCount() - 1);
            } else {
                mMessageIdMap.put(message.getMessageId(), 0);
            }

            sendMessage(message);
        }

    }

    @Override
    public void OnTriggeredTouchEvent(MotionEvent event, CoverFrameLayout coverFrameLayout) {

        View temp1 = mConversationMessageBar;
        int offsetBarY = 0;

        while (temp1.getParent() != mFrameLayout) {
            if (temp1.getParent() instanceof View) {
                temp1 = (View) temp1.getParent();
            } else {
                break;
            }


            offsetBarY += temp1.getTop();
        }

        View temp2 = mCoverView;
        int offsetCoverY = 0;

        while (temp2.getParent() != mFrameLayout) {
            if (temp2.getParent() instanceof View) {
                temp2 = (View) temp2.getParent();
            } else {
                break;
            }
            offsetCoverY += temp2.getTop();
        }

        int offset = Math.round(event.getY()) - (mConversationMessageBar.getTop() + offsetBarY);

        int length = (mCoverView.getBottom() + offsetCoverY) - (mConversationMessageBar.getTop() + offsetBarY);

        if (offset > length / 2) {
            mCoverView.setStatus(VoiceCoverView.STATUS_REC);
        } else {
            mCoverView.setStatus(VoiceCoverView.STATUS_CANCEL);
        }

    }

    private final void setConversationResult() {

        Intent intent = new Intent(ACTION_BUNDLE_IO_RONG_IMKIT_CONVERSATION);

        DBHelper.getInstance().clearUnReadMessage(mConversation.getConversationType(), mConversation.getTargetId());

        intent.putExtra(RCloudConst.EXTRA.CONVERSATION, mConversation);
        String draft = mConversationMessageBar.getMessageEditString();

        if (draft != null) {
            RCloudContext.getInstance().getRongIMClient().saveTextMessageDraft(mConversation.getConversationType(), mConversation.getTargetId(), draft.trim());
        }

        getActivity().sendBroadcast(intent);

    }


    @Override
    public boolean onBackPressed() {

        if (mConversationMessageBar.isShowRichOrExpressionView()) {
            mConversationMessageBar.setRichOrExpressionViewVisibility();
            return true;
        }
        setConversationResult();
        getActivity().finish();
        return false;
    }


    @Override
    public void getDiscussionInfo(final int position, String discusstionId) {

        Intent intent = new Intent(MessageLogic.ACTION_DISCUSSION_INFO_GET);
        intent.putExtra(MessageLogic.DISCUSSION_ID, discusstionId);

        sendAction(intent, new ActionCallback() {
            @Override
            public void callback(Intent intent) {


                boolean isSuccess = intent.getBooleanExtra(MessageLogic.INTENT_API_OPERATION_STATUS, false);

                Log.d("getDiscussionInfo", "isSuccess:" + isSuccess);

                if (isSuccess) {
                    UIDiscussion discussion = intent.getParcelableExtra(MessageLogic.DISCUSSION_OBJECT);
                    Log.d("getDiscussionInfo", "discussion:" + discussion.getName());
                    getHandler().obtainMessage(HANDLE_GET_DISCUSSION_INFO, discussion).sendToTarget();
                }

            }
        });
    }

    @Override
    public void getUserInfo(final int position, String userId, final long messageId) {

        getUserInfo(userId, new GetUserInfoCallback() {
            @Override
            public void onSuccess(UIUserInfo user) {
                UIMessage uiMessage = new UIMessage();
                uiMessage.setUserInfo(user);
                uiMessage.setMessageId(messageId);

                getHandler().obtainMessage(HANDLE_GET_USER_INFO_WHAT, position, 0, uiMessage).sendToTarget();
            }
            @Override
            public void onError() {
            }
        });
    }
}