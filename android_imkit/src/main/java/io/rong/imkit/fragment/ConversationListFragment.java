package io.rong.imkit.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.RCloudContext;
import io.rong.imkit.RongActivity;
import io.rong.imkit.RongIM;
import io.rong.imkit.adapter.ConversationListAdapter;
import io.rong.imkit.common.RCloudConst;
import io.rong.imkit.data.DBHelper;
import io.rong.imkit.logic.MessageLogic;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.model.UIDiscussion;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.model.UIUserInfo;
import io.rong.imkit.utils.ResourceUtils;
import io.rong.imkit.utils.RongToast;
import io.rong.imkit.veiw.LoadingDialog;
import io.rong.imkit.veiw.SelectDialog;
import io.rong.imkit.veiw.SelectDialog.OnDialogItemViewListener;
import io.rong.imlib.PushService;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ConnectionStatusListener.ConnectionStatus;
import io.rong.imlib.RongIMClient.ConversationType;

public class ConversationListFragment extends ActionBaseFrament implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, OnScrollListener, ConversationListAdapter.OnGetDataListener {

    public static final String TAG = "ConversationListFragment";

    public final static int REQUEST_CODE_FRIEND_LIST = 2100;
    public static final String MESSAGE_OBJ = "message_obj";
    public static final int CONVERSATION_ITEM_REQUEST_CODE = 1;
    public static final String INTENT_CONNECT_NET_STATE = "INTENT_CONNECT_NET_STATE";

    private static final int HANDLE_NOTIFY_ADAPTER = 1100;
    private static final int HANDLE_NOTIFY_LOAD_DATA = 1101;
    private static final int GET_DISCUSSION_INFO = 1102;
    private static final int START_CONVERSATION = 1103;
    private static final int HANDLE_ONCLICE_ITEM = 1104;
    private static final int START_CONVERSATION_CREATE_SUCCESS = 1105;
    private static final int HANDLE_SETTING_FUNCTION_SEND_BROADCAST = 1106;
    public static final String INTENT_PRIVATE_SELECT_PEOPLE = "intent_private_select_people";

    private ListView listView;
    private ConversationListAdapter mConversationListAdapter;

    private TextView mEmptyView;

    private String mCurrentTagartId;
    private LoadingDialog mDialog;
    private TextView mConnectStateTextView;//显示网络状态的view

    private int mConnectNetStatue = -1;

    @Override
    public void onResume() {
        RCloudContext.getInstance().setNotificationNewMessageCount(0);
        RCloudContext.getInstance().clearNotificationUserIdList();
        setCurrentCoversationTargetId(null);

        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(ResourceUtils.getLayoutResourceId(getActivity(), "rc_fragment_conversation_list"), null);
        listView = getViewById(view, android.R.id.list);
        mConversationListAdapter = new ConversationListAdapter(getActivity());
        listView.setAdapter(mConversationListAdapter);  listView.setOnItemClickListener(this); listView.setOnItemLongClickListener(this);
        mEmptyView = getViewById(view, android.R.id.empty);
        listView.setEmptyView(mEmptyView);
        mConnectStateTextView = getViewById(view, android.R.id.text1);
        mConversationListAdapter.setOnGetDataListener(this);

        if (getActionBar() == null)   return view;
        //设置titlebar的标题
        getActionBar().getTitleTextView().setText(ResourceUtils.getStringResourceId(getActivity(), "conversation_list_action_bar_title"));
        //设置titleBar的右侧 “添加好友”按钮
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        ImageView peopleView = new ImageView(getActivity());
        peopleView.setLayoutParams(layoutParams);
        peopleView.setImageDrawable(ResourceUtils.getDrawableById(getActivity(), "rc_add_people"));
//==========================================打开“选择好友”页=============================================================================================
        peopleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RongActivity.class);
                intent.putExtra(RCloudConst.EXTRA.CONTENT, FriendMultiChoiceFragment.class.getName());
                startActivity(intent);//, REQUEST_CODE_FRIEND_LIST);
            }
        });
        getActionBar().addView(peopleView);

        getActionBar().setOnBackClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        listView.setOnScrollListener(this);
        mConversationListAdapter.setListView(listView);

        if (RCloudContext.getInstance().getConversationListLaunchedListener() != null) {
            RCloudContext.getInstance().getConversationListLaunchedListener().onCreated();
        }
//====================向数据库取消息列表=======================================================================
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                resetData();
            }
        });

        pushNotification();

        super.onViewCreated(view, savedInstanceState);
    }

    private void pushNotification() {

        if (getActivity().getIntent().getExtras() != null && getActivity().getIntent().getExtras().get("appId") != null) {
            Log.d("intent", "============pushNotification==========coversationList=========");
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
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        UIConversation conversation = mConversationListAdapter.getItem(position);
        mCurrentTagartId = conversation.getTargetId();
        setCurrentCoversationTargetId(mCurrentTagartId);//控制不播放声音


        Uri uri = Uri.parse("rong://" + getActivity().getApplicationInfo().packageName).buildUpon()
                .appendPath("conversation").appendPath(conversation.getConversationType().getName().toLowerCase())
                .appendQueryParameter("targetId", conversation.getTargetId()).appendQueryParameter("title", "").build();

        getActivity().startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }
//=============================================在FriendMultiChoiceFragment中单击“确定”按钮，最终调到这里执行=========================================================================================
    @Override
    public void recevicePageIntnet(Intent intent) {

        String action = intent.getAction();
        Log.d(TAG, "recevicePageIntnet:" + action);

        if (ACTION_BUNDLE_IO_RONG_IMKIT_FRIEND_SELECT.equals(action)) {
            String bunldAction = intent.getStringExtra(FriendMultiChoiceFragment.BUNDLE_ACTION_CONVERSATION_ADD_OR_CREATE);
            if (!TextUtils.isEmpty(bunldAction)) {
                return;
            }
            getHandler().obtainMessage(START_CONVERSATION, intent).sendToTarget();
        } else if (ACTION_BUNDLE_IO_RONG_IMKIT_CONVERSATION.equals(action)) {
            getHandler().obtainMessage(HANDLE_ONCLICE_ITEM, intent).sendToTarget();
            mCurrentTagartId = null;
        } else if (ACTION_BUNDLE_IO_RONG_IMKIT_CONVERSATION_SETTING.equals(action)) {//1、置顶聊天 2、更新讨论组名称 3、二人会话变多人
            getHandler().obtainMessage(HANDLE_SETTING_FUNCTION_SEND_BROADCAST, intent).sendToTarget();
        }
    }
    
    private final void removeConversation(String conversationId) {
        int count = mConversationListAdapter.getCount();

        for (int i = 0; i < count; i++) {
            UIConversation conversation = mConversationListAdapter.getItem(i);

            if (conversation != null && conversation.getTargetId().equals(conversationId)) {
                mConversationListAdapter.remove(i);
                mConversationListAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

//====================获取列表数据==========================================================================
    private final void resetData() {

        mConversationListAdapter.removeAll();

        List<UIConversation> list = DBHelper.getInstance().getConversationList();

        if (list != null) {
            mConversationListAdapter.addData(list);
            mConversationListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void receviceData(Intent intent) {

        String action = intent.getAction();

        Log.d("ConversationListActivity", "-------recrviceData-------");

        if (MessageLogic.ACTION_P2P_MESSAGE_RECEVICE.equals(action) || MessageLogic.ACTION_DISCUSSION_MESSAGE_RECEVICE.equals(action)) {

            UIMessage message = intent.getParcelableExtra(UIMessage.MESSAGE_OBJ);

            String targetId = message.getTargetId();
            if (mCurrentTagartId != null && mCurrentTagartId.equals(targetId)) {
                return;
            }

            hasNewMessage(message, true);

        } else if (MessageLogic.ACTION_RECEVICE_NETWORD_CONNECT_STATE.equals(action)) {

            int connectStateValue = intent.getIntExtra(MessageLogic.INTENT_NETWORK_CONNECT_STATE, -200);
            mConnectNetStatue = connectStateValue;

            Log.d(TAG, "ACTION_RECEVICE_NETWORD_CONNECT_STATE---:" + action + "|connectStateValue:" + connectStateValue);

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
    }

   

    @Override
    public void registerActions(List<String> actions) {

        actions.add(MessageLogic.ACTION_P2P_MESSAGE_RECEVICE);
        actions.add(MessageLogic.ACTION_GROUP_MESSAGE_RECEVICE);
        actions.add(MessageLogic.ACTION_DISCUSSION_MESSAGE_RECEVICE);
        actions.add(RCloudContext.CLIENT_CONNECTED_TO_SDK);
        actions.add(RCloudContext.CLIENT_DISCONNECT_TO_SDK);
        actions.add(MessageLogic.ACTION_RECEVICE_NETWORD_CONNECT_STATE);

        super.registerActions(actions);
    }

    @Override
    public void registerBunlderActions(List<String> actions) {

        actions.add(ACTION_BUNDLE_IO_RONG_IMKIT_CONVERSATION);
        actions.add(ACTION_BUNDLE_IO_RONG_IMKIT_CONVERSATION_SETTING);
        actions.add(ACTION_BUNDLE_IO_RONG_IMKIT_FRIEND_SELECT);

        super.registerBunlderActions(actions);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "----------onDestroy-----------");

        if (RCloudContext.getInstance().getConversationListLaunchedListener() != null) {
            RCloudContext.getInstance().getConversationListLaunchedListener().onDestroyed();
        }

        super.onDestroy();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int positionArg, long id) {

        final SelectDialog mSelectDialog = new SelectDialog(getActivity());
        final UIConversation conversation = mConversationListAdapter.getItem(positionArg);

        if (conversation != null) {
            mSelectDialog.setTitle(conversation.getConversationTitle());
        }

        if (!conversation.isTop()) {
            mSelectDialog.setFristLineContent("dialog_converastion_istop");
        } else {
            mSelectDialog.setFristLineContent("dialog_converastion_istop_cancel");
        }

        mSelectDialog.setSecondLineContent("dialog_converastion_remove");

        mSelectDialog.setOnDialogItemViewListener(new OnDialogItemViewListener() {

            @Override
            public void OnDialogItemViewClick(View view, int position) {
                Log.d("onItemLongClick---", "position:   " + "" + position);

                if (position == 0) {
                    DBHelper.getInstance().setTop(conversation.getConversationType(), conversation.getTargetId(), !conversation.isTop());

                    if (!conversation.isTop()) {

                        if (positionArg != 0 && mConversationListAdapter.getCount() > 1) {

                            conversation.setTop(true);
                            mConversationListAdapter.remove(positionArg);
                            mConversationListAdapter.addItem(0, conversation);
                            mConversationListAdapter.notifyDataSetChanged();
                        } else {
                            conversation.setTop(true);
                            mConversationListAdapter.notifyDataSetChanged();
                        }
                    } else {
                        resetData();// 取消置顶
                    }


                } else if (position == 1) {
                    String targetId = null;

                    if (conversation != null) {
                        targetId = conversation.getTargetId();

                        if (!TextUtils.isEmpty(targetId)) {
                            DBHelper.getInstance().removeConversation(conversation.getConversationType(), targetId);
                            mConversationListAdapter.remove(positionArg);
                            mConversationListAdapter.notifyDataSetChanged();
                        }
                    }
                }

                mSelectDialog.dismiss();

            }
        });

        mSelectDialog.show();

        return true;
    }

    @Override
    protected void rongHandleMessage(Message msg) {

        Log.d("ConversationListFragment", "msg.what == GET_DISCUSSION_INFO---------》");

        if (msg.what == HANDLE_NOTIFY_ADAPTER) {
            mConversationListAdapter.notifyDataSetChanged();
        } else if (msg.what == HANDLE_NOTIFY_LOAD_DATA) {

            resetData();

            if (mDialog != null) {
                mDialog.dismiss();
            }
        } else if (msg.what == GET_DISCUSSION_INFO) {
            Log.d("ConversationListFragment", "msg.what == GET_DISCUSSION_INFO");

            int position = msg.arg1;
            UIDiscussion uiDiscussion = (UIDiscussion) msg.obj;

            UIConversation uiconversation = getUIConveration(uiDiscussion.getId());// mConversationListAdapter.getItem(position);
            uiconversation.setUiDiscussion(uiDiscussion);
            uiconversation.setConversationTitle(uiDiscussion.getName());

            mConversationListAdapter.notifyDataSetChanged(position);
        } else if (msg.what == START_CONVERSATION) {
            Intent data = (Intent) msg.obj;

            List<RongIMClient.UserInfo> userInfos = data.getParcelableArrayListExtra(RCloudConst.EXTRA.USERS);
            int conversationTypeValue = data.getIntExtra(INTENT_PRIVATE_SELECT_PEOPLE, 0);

            ConversationType conversationType = null;
            if (conversationTypeValue > 0) {
                conversationType = ConversationType.setValue(conversationTypeValue);
            }

            if (userInfos.size() > 1 || (conversationType != null && conversationType == ConversationType.DISCUSSION)) {

                final ArrayList<String> userIds = new ArrayList<String>(userInfos.size());
                ArrayList<String> userNames = new ArrayList<String>(10);

                for (RongIMClient.UserInfo info : userInfos) {
                    userIds.add(info.getUserId());

                    if (userNames.size() >= 10)
                        continue;

                    userNames.add(info.getName());
                }

                RongIMClient.UserInfo userInfo = RCloudContext.getInstance().getRongIMClient().getCurrentUserInfo();

                if (userInfo != null && !TextUtils.isEmpty(userInfo.getName())) {
                    userNames.add(userInfo.getName());
                    Log.d(TAG, "-------------->" + userInfo.getName());
                } else {
                    RongIMClient.UserInfo userInfoArg = RCloudContext.getInstance().getRongIMClient().getCurrentUserInfo();

                    if (!TextUtils.isEmpty(userInfoArg.getName())) {
                        userNames.add(userInfoArg.getName());
                    }
                    Log.d(TAG, "--------------userInfo.getName() is null");
                }

                Intent intent = new Intent(MessageLogic.ACTION_DISCUSSION_CREATE);
                intent.putStringArrayListExtra(MessageLogic.DISCUSSION_MEMBER_ID, userIds);
                intent.putExtra(MessageLogic.DISCUSSION_NAME, TextUtils.join(",", userNames));


                mDialog = new LoadingDialog(this.getActivity());
                mDialog.setText(ResourceUtils.getStringResource(getActivity(), "discussion_create_loading_title"));
                mDialog.show();

                sendAction(intent, new ActionCallback() {

                    @Override
                    public void callback(Intent intentArg) {

                        mDialog.dismiss();
                        boolean isSuccess = intentArg.getBooleanExtra(MessageLogic.INTENT_API_OPERATION_STATUS, false);

                        Log.d(TAG, "sendAction-----callback-----isSuccess＞＞＞>" + isSuccess);

                        if (isSuccess) {
                            String targetId = intentArg.getStringExtra(MessageLogic.DISCUSSION_ID);
                            String discusstionName = intentArg.getStringExtra(MessageLogic.DISCUSSION_NAME);
                            mCurrentTagartId = targetId;
                            setCurrentCoversationTargetId(targetId);

                            UIConversation conversation = new UIConversation();
                            conversation.setTargetId(targetId);
                            conversation.setConversationType(ConversationType.DISCUSSION);
                            conversation.setConversationTitle(discusstionName);

                            newMessageSetTop(conversation);

                            getHandler().obtainMessage(START_CONVERSATION_CREATE_SUCCESS, conversation).sendToTarget();

                        } else {
                            RongToast.makeText(getActivity(), ResourceUtils.getStringResource(getActivity(), "discussion_create_failure")).show();
                        }
                    }

                });

            } else if (userInfos.size() == 1) {
                UIConversation conversation = new UIConversation();
                conversation.setConversationTitle(userInfos.get(0).getName());
                conversation.setTargetId(userInfos.get(0).getUserId());
                conversation.setConversationType(ConversationType.PRIVATE);
                startConversation(conversation);
                mCurrentTagartId = userInfos.get(0).getUserId();
                setCurrentCoversationTargetId(mCurrentTagartId);
            }
        } else if (msg.what == HANDLE_ONCLICE_ITEM) {

            Intent intent = (Intent) msg.obj;

            UIConversation conversation = intent.getParcelableExtra(RCloudConst.EXTRA.CONVERSATION);
            UIConversation conversationTemp = DBHelper.getInstance().getConversation(conversation.getConversationType(), conversation.getTargetId());

            if (conversationTemp == null || TextUtils.isEmpty(conversationTemp.getTargetId())) {//二人会话取数据库时，数据库没数据也返回了josn串
                return;
            }

            UIMessage uiMessage = new UIMessage();
            uiMessage.setContent(conversationTemp.getLatestMessage());
            uiMessage.setTargetId(conversationTemp.getTargetId());
            uiMessage.setReceivedTime(conversationTemp.getReceivedTime());
            uiMessage.setSentTime(conversationTemp.getSentTime());
            uiMessage.setConversationType(conversationTemp.getConversationType());
            uiMessage.setDraft(conversationTemp.getDraft());
            uiMessage.setSenderUserId(conversationTemp.getSenderUserId());


            hasNewMessage(uiMessage, false);

        } else if (msg.what == START_CONVERSATION_CREATE_SUCCESS) {
            UIConversation conversation = (UIConversation) msg.obj;
            startConversation(conversation);
        } else if (msg.what == HANDLE_SETTING_FUNCTION_SEND_BROADCAST) {
            Intent intent = (Intent) msg.obj;

            UIConversation conversation = intent.getParcelableExtra(RCloudConst.EXTRA.CONVERSATION);
            boolean isQuitDiscussion = intent.getBooleanExtra(ConversationSettingFragment.INTENT_QUIT_DISCUSSION_CLOSE_PAGE, false);
            int isSetTopConversation = intent.getIntExtra(ConversationSettingFragment.INTENT_SET_TOP_CONVERSATION_SUCCESS, -1);
            String discussionName = intent.getStringExtra(ConversationSettingFragment.INTENT_UPDATE_NAME_DISCUSSION);
            String createDiscussion = intent.getStringExtra(ConversationSettingFragment.INTENT_CREATE_DISCUSSION_SUCCESS);
            boolean isClearMessages = intent.getBooleanExtra(ConversationSettingFragment.INTENT_CLEAR_MESSSAGE_SUCCESS, false);

            if (!TextUtils.isEmpty(createDiscussion)) {
                newMessageSetTop(conversation);
                return;
            }

            UIConversation conversationItem = getUIConveration(conversation.getTargetId());
            if (conversationItem != null) {
                conversation = conversationItem;
            }

            if (isQuitDiscussion) {//退出讨论组
                mConversationListAdapter.remove(conversation);
                mConversationListAdapter.notifyDataSetChanged();
                return;
            }

            //退出就数据库获取不到这条数据了
            UIConversation conversationTemp = DBHelper.getInstance().getConversation(conversation.getConversationType(), conversation.getTargetId());

            if (conversationTemp != null) {
                conversation.setLatestMessage(conversationTemp.getLatestMessage());
                conversation.setReceivedTime(conversationTemp.getReceivedTime());
                conversation.setSentTime(conversationTemp.getSentTime());
            }


            if (!TextUtils.isEmpty(discussionName)) {//修改讨论组名称

                if (conversation != null && conversation.getUiDiscussion() != null) {
                    UIDiscussion uiDiscussion = conversation.getUiDiscussion();
                    uiDiscussion.setName(discussionName);
                }

                conversation.setConversationTitle(discussionName);
                mConversationListAdapter.notifyDataSetChanged();
            }

            if (isClearMessages) {//清除聊天记录（可用）
                String targetId = conversation.getTargetId();
                UIConversation uiconversation = getUIConveration(targetId);

                if (uiconversation != null) {
                    uiconversation.setLatestMessage(null);
                    mConversationListAdapter.notifyDataSetChanged();
                }
            }

            if (isSetTopConversation != -1) {//置顶

                if (isSetTopConversation == 1) {
                    UIConversation uiConversation = getUIConveration(conversation.getTargetId());
                    uiConversation.setTop(true);
                    int count = mConversationListAdapter.getCount();

                    if (count > 1) {
                        removeConversation(conversation.getTargetId());
                        mConversationListAdapter.addItem(0, uiConversation);
                    }
                    mConversationListAdapter.notifyDataSetChanged();
                } else {
                    resetData();
                }
            }

        } else if (msg.what == HANDLE_GET_USER_INFO_WHAT) {
            UIConversation uiConversation = (UIConversation) msg.obj;
            UIConversation conversation = getUIConveration(uiConversation.getTargetId());

            if(uiConversation.getUserInfo()!=null) {
                conversation.setUserInfo(uiConversation.getUserInfo());
                conversation.setConversationTitle(uiConversation.getUserInfo().getName());
            }
            mConversationListAdapter.notifyDataSetChanged();
        }

    }

    private void startConversation(UIConversation conversation) {
        Intent intent = new Intent(getActivity(), RongActivity.class);
        intent.putExtra(RCloudConst.EXTRA.CONTENT, ConversationFragment.class.getCanonicalName());
        intent.putExtra(RCloudConst.EXTRA.CONVERSATION, conversation);
        intent.putExtra(ConversationFragment.CONVERSATION_TYPE, conversation.getConversationType().getValue());
        intent.putExtra(INTENT_CONNECT_NET_STATE, mConnectNetStatue);
        startActivity(intent);//, CONVERSATION_ITEM_REQUEST_CODE);
    }

    /**
     * @param message
     * @param isNewMessage true 新消息，false 来自会话列表返回
     */
    private final void hasNewMessage(UIMessage message, boolean isNewMessage) {

        String targetId = message.getTargetId();

        int count = mConversationListAdapter.getCount();
        boolean isExit = false;

        for (int i = 0; i < count; i++) {
            UIConversation conversation = mConversationListAdapter.getItem(i);

            if (conversation.getTargetId().equals(targetId)) {// 新消息的会话在列表中存在

                conversation.setDraft(message.getDraft());

                if (conversation.getReceivedTime() > message.getReceivedTime() || conversation.getSentTime() > message.getSentTime()) {
                    resetData();
                    return;
                }

                if ((conversation.getReceivedTime() == message.getReceivedTime() || conversation.getSentTime() == message.getSentTime()) && !isNewMessage) {
                    conversation.setUnreadMessageCount(0);
                    mConversationListAdapter.notifyDataSetChanged(i);
                    return;
                }

                conversation.setTextMessageContent(null);


                conversation.setLatestMessage(message.getContent());
                conversation.setReceivedTime(message.getReceivedTime());
                conversation.setSentStatus(message.getSentStatus());

                if (isNewMessage) {
                    conversation.setUnreadMessageCount(conversation.getUnreadMessageCount() + 1);
                } else {
                    Log.d("isNewMessage", "isNewMessage--------false");
                    conversation.setUnreadMessageCount(0);
                }

                if (conversation.isTop()) {
                    if (i != 0) {
                        mConversationListAdapter.remove(i);
                        mConversationListAdapter.addItem(0, conversation);
                    }
                } else {
                    for (int j = 0; j < count; j++) {
                        UIConversation uiConversation = mConversationListAdapter.getItem(j);

                        if (!uiConversation.isTop()) {
                            mConversationListAdapter.remove(i);
                            mConversationListAdapter.addItem(j, conversation);

                            break;
                        }
                    }
                }

                getHandler().obtainMessage(HANDLE_NOTIFY_ADAPTER).sendToTarget();

                isExit = true;

                break;

            }
        }

        if (!isExit) {

            int isNotExitTopPosition = -1;

            for (int j = 0; j < count; j++) {
                UIConversation uiConversation = mConversationListAdapter.getItem(j);

                if (!uiConversation.isTop()) {
                    isNotExitTopPosition = j;
                    break;
                }
            }

            UIConversation conversation = message.toConversation();

            if (!isNewMessage) {
                conversation.setSentStatus(message.getSentStatus());
                conversation.setUnreadMessageCount(0);
            } else {
                conversation.setUnreadMessageCount(1);
            }

            if (isNotExitTopPosition >= 0) {
                mConversationListAdapter.addItem(isNotExitTopPosition, conversation);
            } else {
                isNotExitTopPosition = count;
                mConversationListAdapter.addItem(isNotExitTopPosition, conversation);
            }
            mConversationListAdapter.notifyDataSetChanged();
        }
    }


    /**
     * 新消息动态添加 把这条消息放在置顶消息下面 放到其他非置顶消息上面
     *
     * @param conversation
     */
    private final void newMessageSetTop(UIConversation conversation) {

        if (mConversationListAdapter != null && conversation != null) {

            int count = mConversationListAdapter.getCount();

            if (count <= 0) {
                mConversationListAdapter.addData(conversation);
            } else {

                for (int i = 0; i < count; i++) {
                    UIConversation uiConversation = mConversationListAdapter.getItem(i);

                    if (!uiConversation.isTop()) {
                        mConversationListAdapter.addItem(i, conversation);
                        break;
                    }
                }
            }
            mConversationListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
//=================================根据discusstionId获得讨论组相关信息信息====是ConversationListAdapter中的内部类OnGetDataListener的回调方法============================================
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
                    getHandler().obtainMessage(GET_DISCUSSION_INFO, position, 0, discussion).sendToTarget();
                }
            }
        });
    }
//=================================根据targetId获得用户信息==========是ConversationListAdapter中的内部类OnGetDataListener的回调方法====作用为ConversationListAdapter提供“发送者用户名”==================================
    @Override
    public void getUserInfo(int position, final String targetId) {

        getHandler().post(new Runnable() {

            @Override
            public void run() {

                getUserInfo(targetId, new GetUserInfoCallback() {

                    @Override
                    public void onSuccess(UIUserInfo user) {

                        UIConversation uiConversation = new UIConversation();
                        uiConversation.setTargetId(targetId);
                        uiConversation.setUserInfo(user);

                        getHandler().obtainMessage(HANDLE_GET_USER_INFO_WHAT, uiConversation).sendToTarget();
                    }
                    @Override
                    public void onError() {
                    }
                });
            }
        });
    }


    private final UIConversation getUIConveration(String targetId) {

        int count = mConversationListAdapter.getCount();

        for (int i = 0; i < count; i++) {
            UIConversation conversation = mConversationListAdapter.getItem(i);

            if (conversation != null && conversation.getTargetId().equals(targetId)) {
                return mConversationListAdapter.getItem(i);
            }
        }

        return null;
    }
}
