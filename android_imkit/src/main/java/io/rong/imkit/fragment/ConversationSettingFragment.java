package io.rong.imkit.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.RCloudContext;
import io.rong.imkit.RongActivity;
import io.rong.imkit.adapter.ConversationListAdapter;
import io.rong.imkit.adapter.ConversationMemberGridViewAdapter;
import io.rong.imkit.adapter.ConversationMemberGridViewAdapter.OnGridViewImageLisenter;
import io.rong.imkit.common.RCloudConst;
import io.rong.imkit.data.DBHelper;
import io.rong.imkit.logic.MessageLogic;
import io.rong.imkit.model.Friend;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.model.UIDiscussion;
import io.rong.imkit.model.UIUserInfo;
import io.rong.imkit.utils.ResourceUtils;
import io.rong.imkit.utils.RongToast;
import io.rong.imkit.veiw.AlterDialog;
import io.rong.imkit.veiw.EditTextRongDialog;
import io.rong.imkit.veiw.LoadingDialog;
import io.rong.imkit.veiw.RongGridView;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ConversationType;
import io.rong.imlib.RongIMClient.UserInfo;

/**
 * Created by zhjchen on 14-4-17.
 */
public class ConversationSettingFragment extends ActionBaseFrament implements OnClickListener, OnItemClickListener, OnTouchListener, ConversationListAdapter.OnGetDataListener
        , OnGridViewImageLisenter {

    private static final String TAG = ConversationSettingFragment.class.getSimpleName();

    private static final int HANDLE_GET_DISCUSSION_INFO_SUCCESS = 10001;
    private static final int HANDLE_GET_DISCUSSION_INFO_FAILURE = 10002;
    private static final int HANDLE_NOTIFYDATASETCHANGED = 100030;
    private static final int HANDLE_REMOVE_MEMBER_SUCCESS = 10003;
    private static final int HANDLE_REMOVE_MEMBER_FAILURE = 10004;
    private static final int HANDLE_QUIT_DISCUSSION_FAILURE = 10005;
    private static final int HANDLE_QUIT_DISCUSSION_SUCCESS = 10006;
    private static final int HANDLE_ADD_MEMBER_FOR_DISCUSSION = 10007;
    private static final int HANDLE_ADD_MEMBER_FOR_DISCUSSION_SUCCESS = 10008;
    private static final int HANDLE_ADD_MEMBER_FOR_DISCUSSION_FAILURE = 10009;
    private static final int HANDLE_UPDATE_DISCUSSION_NAME_SUCCESS = 10010;
    private static final int HANDLE_UPDATE_DISCUSSION_NAME_FAILURE = 10011;
    private static final int HANDLE_CREATE_DISCUSSION_SUCCESS = 10012;
    private static final int HANDLE_CREATE_DISCUSSION_FAILURE = 10013;

    private static final int HANDLE_GET_BLOCK_PUSH_STATUS_SUCCESS = 10014;
    private static final int HANDLE_GET_BLOCK_PUSH_STATUS_FAILURE = 10015;

    private static final int HANDLE_SET_DISCUSSION_INVITE_STATUS_SUCCESS = 10016;
    private static final int HANDLE_SET_DISCUSSION_INVITE_STATUS_FAILURE = 10017;

    private static final int HANDLE_SET_BLOCK_PUSH_STATUS_SUCCESS = 10018;
    private static final int HANDLE_SET_BLOCK_PUSH_STATUS_FAILURE = 10019;

    private static final int HANDLE_COLSE_DIALOG = 10020;
    public static final String INTENT_SELECT_FRIEND_FOR_ADD = "intent_select_friend_for_add";
    public static final String INTENT_QUIT_DISCUSSION_CLOSE_PAGE = "intent_quit_discussion_close_page";
    public static final String INTENT_CLEAR_MESSSAGE_SUCCESS = "intent_clear_message_success";
    public static final String INTENT_SET_TOP_CONVERSATION_SUCCESS = "intent_set_top_conversation_success";
    public static final String INTENT_ADDED_MEMBER_IDS_FOR_DISCUSSION = "intent_added_member_ids_for_discussion";
    public static final String INTENT_UPDATE_NAME_DISCUSSION = "intent_update_name_discussion";
    public static final String INTENT_CREATE_DISCUSSION_SUCCESS = "intent_create_discussion_success";


    private RongGridView mGridView;
    private CheckBox mSetTopConversationCheckBox;
    private CheckBox mNewMessageNotifySetCheckBox;
    private Button mExitButton;
    private RelativeLayout mClearMessagesLayout;
    private RelativeLayout mDiscussionNameLayout;
    private RelativeLayout mOpenMemberInviteLayout;
    private TextView mDiscussionNameTextView;
    private CheckBox mOpenMemberInviteCheckBox;

    private ConversationMemberGridViewAdapter mMemberGridViewAdapter;
    private ConversationType mConversationType;
    private String mTargetId;

    private LoadingDialog mLoadingDialog = null;
    private AlterDialog mAlterDialog;

    private UIConversation mConversation;

    private String mTempDiscussionName = null;

    private int mDelPositio = -1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity().getIntent() != null) {

            Uri uri = getActivity().getIntent().getData();
            mTargetId = uri.getQueryParameter("targetId");

            mConversationType = ConversationType
                    .valueOf(uri.getLastPathSegment().toUpperCase());

            mConversation = new UIConversation();
            mConversation.setTargetId(mTargetId);
            mConversation.setConversationType(mConversationType);

        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(ResourceUtils.getLayoutResourceId(getActivity(), "rc_fragment_conversation_setting"), null);

        mLoadingDialog = new LoadingDialog(this.getActivity());
        mGridView = getViewById(view, android.R.id.list);

        mSetTopConversationCheckBox = getViewById(view, android.R.id.button1);
        mNewMessageNotifySetCheckBox = getViewById(view, android.R.id.button2);
        mSetTopConversationCheckBox.setOnClickListener(this);
        mNewMessageNotifySetCheckBox.setOnClickListener(this);
        mNewMessageNotifySetCheckBox.setChecked(true);

        mExitButton = getViewById(view, android.R.id.button3);
        mExitButton.setOnClickListener(this);


        mClearMessagesLayout = getViewById(view, "rc_clear_messages");
        mClearMessagesLayout.setTag(false);
        mClearMessagesLayout.setOnClickListener(this);

        mDiscussionNameLayout = getViewById(view, "rc_discussion_name_layout");
        mDiscussionNameLayout.setOnClickListener(this);

        mDiscussionNameTextView = getViewById(view, android.R.id.text1);
        mOpenMemberInviteLayout = getViewById(view, "rc_open_member_invite_layout");

        mOpenMemberInviteCheckBox = getViewById(view, "rc_open_member_invite");
        mOpenMemberInviteCheckBox.setOnClickListener(this);

        UIConversation conversation = DBHelper.getInstance().getConversation(mConversationType, mTargetId);


        if(conversation!=null){
            mConversation=conversation;
        }

        if (mConversation.isTop()) {
            mSetTopConversationCheckBox.setChecked(true);
        } else {
            mSetTopConversationCheckBox.setChecked(false);
        }


        if (mConversation.getConversationTitle() != null) {
            mDiscussionNameTextView.setText(mConversation.getConversationTitle());
        }

        if (mConversationType != null) {

            if (ConversationType.DISCUSSION == mConversationType) {
                mExitButton.setVisibility(View.VISIBLE);
                mDiscussionNameLayout.setVisibility(View.VISIBLE);
            } else if (ConversationType.PRIVATE == mConversationType) {
                mExitButton.setVisibility(View.GONE);
                mDiscussionNameLayout.setVisibility(View.GONE);
            }
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActionBar() != null) {

            getActionBar().getTitleTextView().setText(ResourceUtils.getStringResource(this.getActivity(), "conversation_setting_title"));

            getActionBar().setOnBackClick(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });

        }


        ArrayList<Friend> friends = new ArrayList<Friend>();

        if (mConversationType == ConversationType.PRIVATE) {

            Friend friend = new Friend();
            friend.setUserId(mTargetId);
            friends.add(friend);


        } else if (mConversationType == ConversationType.DISCUSSION) {

            Intent intent = new Intent(MessageLogic.ACTION_DISCUSSION_INFO_GET);
            intent.putExtra(MessageLogic.DISCUSSION_ID, mTargetId);

            sendAction(intent, new ActionCallback() {

                @Override
                public void callback(Intent intent) {

                    boolean isSuccess = intent.getBooleanExtra(MessageLogic.INTENT_API_OPERATION_STATUS, false);
                    Log.d(TAG, "sendAction----callback---isSuccess-" + isSuccess);

                    if (isSuccess) {
                        UIDiscussion uiDiscussion = intent.getParcelableExtra(MessageLogic.DISCUSSION_OBJECT);
                        getHandler().obtainMessage(HANDLE_GET_DISCUSSION_INFO_SUCCESS, uiDiscussion).sendToTarget();
                    } else {
                        getHandler().obtainMessage(HANDLE_GET_DISCUSSION_INFO_FAILURE).sendToTarget();
                    }

                }
            });

        }

        mMemberGridViewAdapter = new ConversationMemberGridViewAdapter(this.getActivity(), friends);
        mMemberGridViewAdapter.setGridViewImageLisenter(this);
        mMemberGridViewAdapter.setOnGetDataListener(this);

        mGridView.setAdapter(mMemberGridViewAdapter);

        if (mConversationType == ConversationType.PRIVATE) {
            mMemberGridViewAdapter.addAddButtonItem();
        }

        mMemberGridViewAdapter.notifyDataSetChanged();
        mGridView.setOnItemClickListener(this);


        getBlockPushStatus(mConversationType, mTargetId);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Friend friend = (Friend) view.getTag(ResourceUtils.getDrawableResourceId(ConversationSettingFragment.this.getActivity(), "rc_default_portrait"));

        if (friend.isAdd()) {

            Intent intent = new Intent(ConversationSettingFragment.this.getActivity(), RongActivity.class);
            intent.putExtra(INTENT_SELECT_FRIEND_FOR_ADD, true);
            intent.putExtra(RCloudConst.EXTRA.CONTENT, FriendMultiChoiceFragment.class.getName());

            if (mConversation.getConversationType() == ConversationType.PRIVATE) {

                ArrayList<String> userIds = new ArrayList<String>();
                userIds.add(mConversation.getTargetId());

                intent.putStringArrayListExtra(INTENT_ADDED_MEMBER_IDS_FOR_DISCUSSION, userIds);
                intent.putExtra(ConversationListFragment.INTENT_PRIVATE_SELECT_PEOPLE, ConversationType.PRIVATE.getValue());
                startActivity(intent);

            } else if (mConversation.getConversationType() == ConversationType.DISCUSSION) {

                if (mConversation.getUiDiscussion() != null) {
                    ArrayList<String> userIds = new ArrayList<String>();

                    int count = mMemberGridViewAdapter.getCount();
                    String userId = RCloudContext.getInstance().getRongIMClient().getCurrentUserInfo().getUserId();
                    for (int i = 0; i < count; i++) {
                        Friend friendArg = mMemberGridViewAdapter.getItem(i);
                        if (!friendArg.isAdd() && !friendArg.isSub() && !friendArg.getUserId().equals(userId)) {
                            userIds.add(friendArg.getUserId());
                        }
                    }

                    intent.putExtra(ConversationListFragment.INTENT_PRIVATE_SELECT_PEOPLE, ConversationType.DISCUSSION.getValue());
                    intent.putStringArrayListExtra(INTENT_ADDED_MEMBER_IDS_FOR_DISCUSSION, userIds);
                    startActivity(intent);
                }
            }


        } else if (friend.isSub()) {
            mMemberGridViewAdapter.deleteToggle(true);
            mMemberGridViewAdapter.notifyDataSetChanged();
        }

    }


    @Override
    public void onGridViewImageClick(View view, int position) {


        String userId = (String) view.getTag();
        mDelPositio = position;

        Log.d(TAG, "mDelPositio_onGridViewImageClick_mDelPositio:" + mDelPositio);
        Log.d(TAG, "---onGridViewImageClick--------userId:" + userId);

        removeMemberForDiscussion(mConversation.getTargetId(), userId);
    }

    @Override
    protected void rongHandleMessage(Message msg) {
        super.rongHandleMessage(msg);

        if (msg.what == HANDLE_GET_DISCUSSION_INFO_SUCCESS) {
            Log.d(TAG, "handleMessage------HANDLE_GET_DISCISTION_INFO_SUCCESS");

            UIDiscussion discussion = (UIDiscussion) msg.obj;
            if (mConversation != null) {
                mConversation.setUiDiscussion(discussion);
            }

            List<String> userIds = discussion.getUserIds();

            ArrayList<Friend> friends = new ArrayList<Friend>();

            if (userIds != null) {

                for (String userId : userIds) {
                    Log.d(TAG, userId);
                    Friend friend = new Friend();
                    friend.setNickname("");
                    friend.setUserId(userId);
                    friends.add(friend);
                }

            }

            mMemberGridViewAdapter.addData(friends);

            String userId = RCloudContext.getInstance().getRongIMClient().getCurrentUserInfo().getUserId();

            if (discussion != null) {


                if (!TextUtils.isEmpty(discussion.getAdminId()) && userId.equals(discussion.getAdminId())) {

                    mMemberGridViewAdapter.addAddButtonItem();

                    if (discussion.isOpen()) {
                        mOpenMemberInviteCheckBox.setChecked(true);
                    } else {
                        mOpenMemberInviteCheckBox.setChecked(false);
                    }

                    mOpenMemberInviteLayout.setVisibility(View.VISIBLE);
                    mMemberGridViewAdapter.addSubButtonItem();
                } else {
                    if (discussion.isOpen()) {
                        mMemberGridViewAdapter.addAddButtonItem();
                        mOpenMemberInviteCheckBox.setChecked(true);
                    } else {
                        mOpenMemberInviteLayout.setVisibility(View.GONE);
                    }
                }


                mDiscussionNameTextView.setText(discussion.getName());
            }

            mMemberGridViewAdapter.notifyDataSetChanged();

        } else if (msg.what == HANDLE_GET_DISCUSSION_INFO_FAILURE) {
            RongToast.makeText(ConversationSettingFragment.this.getActivity(), ResourceUtils.getStringResource(ConversationSettingFragment.this.getActivity(), "conversation_setting_get_info_failure")).show();
        } else if (msg.what == HANDLE_NOTIFYDATASETCHANGED) {

            mMemberGridViewAdapter.notifyDataSetChanged();

        } else if (msg.what == HANDLE_REMOVE_MEMBER_FAILURE) {

            RongToast.makeText(ConversationSettingFragment.this.getActivity(), ResourceUtils.getStringResource(ConversationSettingFragment.this.getActivity(), "discussion_remove_member_failure")).show();

        } else if (msg.what == HANDLE_QUIT_DISCUSSION_FAILURE) {

            RongToast.makeText(ConversationSettingFragment.this.getActivity(), ResourceUtils.getStringResource(ConversationSettingFragment.this.getActivity(), "conversation_setting_discussion_exit_failure"))
                    .show();

            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }

            RongToast.makeText(this.getActivity(), ResourceUtils.getStringResource(this.getActivity(), "rc_conversation_setting_exit_discussion_failure")).show();

        } else if (msg.what == HANDLE_QUIT_DISCUSSION_SUCCESS) {

            DBHelper.getInstance().removeConversation(mConversation.getConversationType(), mConversation.getTargetId());
            DBHelper.getInstance().clearMessages(mConversation.getConversationType(), mConversation.getTargetId());

            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }

            Intent intent = new Intent(ACTION_BUNDLE_IO_RONG_IMKIT_CONVERSATION_SETTING);
            intent.putExtra(INTENT_QUIT_DISCUSSION_CLOSE_PAGE, true);
            intent.putExtra(RCloudConst.EXTRA.CONVERSATION, mConversation);
            this.getActivity().sendBroadcast(intent);
            getActivity().finish();


        } else if (msg.what == HANDLE_COLSE_DIALOG) {

            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }

        } else if (msg.what == HANDLE_ADD_MEMBER_FOR_DISCUSSION) {

            mLoadingDialog = new LoadingDialog(this.getActivity());
            mLoadingDialog.setText(ResourceUtils.getStringResource(this.getActivity(), "rc_public_data_process"));

            if (!mLoadingDialog.isShowing()) {
                mLoadingDialog.show();
            }

            ArrayList<UserInfo> userInfos = (ArrayList<UserInfo>) msg.obj;

            if (ConversationType.PRIVATE == mConversation.getConversationType()) {
                createDiscussion(userInfos);
            } else {
                addMemberForDiscussion(mTargetId, userInfos);
            }


        } else if (msg.what == HANDLE_ADD_MEMBER_FOR_DISCUSSION_SUCCESS) {

            ArrayList<UserInfo> userInfos = (ArrayList<UserInfo>) msg.obj;

            ArrayList<Friend> friends = new ArrayList<Friend>();

            for (UserInfo userInfo : userInfos) {
                if (userInfo != null) {
                    Friend friend = new Friend();
                    friend.setNickname(userInfo.getName());
                    friend.setPortrait(userInfo.getPortraitUri());
                    friend.setUserId(userInfo.getUserId());
                    friends.add(friend);
                }
            }

            int count = mMemberGridViewAdapter.getCount();

            int position = -1;

            if (mMemberGridViewAdapter.isAdd() && mMemberGridViewAdapter.isSub()) {
                position = count - 2;
            } else if (mMemberGridViewAdapter.isAdd() || mMemberGridViewAdapter.isSub()) {
                position = count - 1;
            }

            if (position > 0) {
                mMemberGridViewAdapter.addItems(position, friends);
            }

            mMemberGridViewAdapter.deleteToggle(false);

            mMemberGridViewAdapter.notifyDataSetChanged();

            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }

        } else if (msg.what == HANDLE_ADD_MEMBER_FOR_DISCUSSION_FAILURE) {

            RongToast.makeText(this.getActivity(), ResourceUtils.getStringResource(this.getActivity(), "rc_conversation_setting_add_discussion_member_failure")).show();

            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }

        } else if (msg.what == HANDLE_REMOVE_MEMBER_FAILURE) {

            RongToast.makeText(this.getActivity(), ResourceUtils.getStringResource(this.getActivity(), "rc_conversation_setting_remove_discussion_member_failure")).show();
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
            mDelPositio = -1;

        } else if (msg.what == HANDLE_REMOVE_MEMBER_SUCCESS) {

            Log.d(TAG, "mDelPositio_msg.what:" + mDelPositio);

            if (mDelPositio != -1) {
                mMemberGridViewAdapter.remove(mDelPositio);
                mMemberGridViewAdapter.notifyDataSetChanged();
                mDelPositio = -1;
            }

        } else if (msg.what == HANDLE_UPDATE_DISCUSSION_NAME_SUCCESS) {

            mDiscussionNameTextView.setText(mTempDiscussionName);

            if (mConversation != null) {
                mConversation.setConversationTitle(mTempDiscussionName);

                if (mConversation.getUiDiscussion() != null) {
                    mConversation.getUiDiscussion().setName(mTempDiscussionName);
                }
            }

            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }

        } else if (msg.what == HANDLE_UPDATE_DISCUSSION_NAME_FAILURE) {

            RongToast.makeText(this.getActivity(), ResourceUtils.getStringResource(this.getActivity(), "rc_conversation_setting_update_discussion_name_failure")).show();

            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }

        } else if (msg.what == HANDLE_CREATE_DISCUSSION_SUCCESS) {

            UIConversation conversation = (UIConversation) msg.obj;

            Intent intent1 = new Intent(ACTION_BUNDLE_IO_RONG_IMKIT_CONVERSATION_SETTING);
            intent1.putExtra(INTENT_CREATE_DISCUSSION_SUCCESS, INTENT_CREATE_DISCUSSION_SUCCESS);
            intent1.putExtra(RCloudConst.EXTRA.CONVERSATION, mConversation);
            this.getActivity().sendBroadcast(intent1);

            Intent intent = new Intent(this.getActivity(), RongActivity.class);
            intent.putExtra(RCloudConst.EXTRA.CONTENT, ConversationFragment.class.getCanonicalName());
            intent.putExtra(RCloudConst.EXTRA.CONVERSATION, conversation);
            startActivity(intent);
            getActivity().finish();

        } else if (msg.what == HANDLE_CREATE_DISCUSSION_FAILURE) {

            RongToast.makeText(this.getActivity(), ResourceUtils.getStringResource(this.getActivity(), "discussion_create_failure")).show();

        } else if (msg.what == HANDLE_SET_DISCUSSION_INVITE_STATUS_SUCCESS) {

        } else if (msg.what == HANDLE_SET_DISCUSSION_INVITE_STATUS_FAILURE) {

            mOpenMemberInviteCheckBox.setChecked(!mOpenMemberInviteCheckBox.isChecked());

        } else if (msg.what == HANDLE_SET_BLOCK_PUSH_STATUS_SUCCESS) {

            int status = (Integer) msg.obj;
            mNewMessageNotifySetCheckBox.setChecked(status == 100 ? false : true);

        } else if (msg.what == HANDLE_SET_BLOCK_PUSH_STATUS_FAILURE) {

            mNewMessageNotifySetCheckBox.setChecked(!mNewMessageNotifySetCheckBox.isChecked());

        } else if (msg.what == HANDLE_GET_BLOCK_PUSH_STATUS_SUCCESS) {

            int status = (Integer) msg.obj;
            mNewMessageNotifySetCheckBox.setChecked(status == 100 ? false : true);

        } else if (msg.what == HANDLE_GET_BLOCK_PUSH_STATUS_FAILURE) {

            mNewMessageNotifySetCheckBox.setChecked(!mNewMessageNotifySetCheckBox.isChecked());

        } else if (msg.what == HANDLE_GET_USER_INFO_WHAT) {

            int position = (Integer) msg.arg1;
            UIUserInfo uiUserInfo = (UIUserInfo) msg.obj;
            Friend friend = mMemberGridViewAdapter.getItem(position);
            friend.setNickname(uiUserInfo.getName());
            friend.setPortrait(uiUserInfo.getPortraitUri());
            mMemberGridViewAdapter.notifyDataSetChanged();

        }

    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "-------onTouch--------");

        if (!mMemberGridViewAdapter.isDeleteState()) {
            mMemberGridViewAdapter.deleteToggle(false);
            mMemberGridViewAdapter.notifyDataSetChanged();
        }
        return false;
    }

    @Override
    public void onClick(View v) {

        if (v == mExitButton) {

            final Intent intent = new Intent(MessageLogic.ACTION_DISCUSSION_QUIT);
            intent.putExtra(MessageLogic.DISCUSSION_ID, mTargetId);

            mLoadingDialog = new LoadingDialog(this.getActivity());
            mLoadingDialog.setText(ResourceUtils.getStringResource(this.getActivity(), "rc_public_data_process"));

            mAlterDialog = new AlterDialog(this.getActivity());
            mAlterDialog.setTitle(ResourceUtils.getStringResourceId(this.getActivity(), "conversation_setting_exit_prompt"));

            mAlterDialog.setButton1(ResourceUtils.getStringResource(this.getActivity(), "alter_dialog_confirm"), new OnClickListener() {


                @Override
                public void onClick(View v) {
                    mAlterDialog.dismiss();

                    sendAction(intent, new ActionCallback() {

                        @Override
                        public void callback(Intent intent) {
                            boolean isSuccess = intent.getBooleanExtra(MessageLogic.INTENT_API_OPERATION_STATUS, false);

                            if (isSuccess) {
                                getHandler().obtainMessage(HANDLE_QUIT_DISCUSSION_SUCCESS).sendToTarget();
                            } else {
                                getHandler().obtainMessage(HANDLE_QUIT_DISCUSSION_FAILURE).sendToTarget();
                            }
                        }
                    });
                }
            });

            mAlterDialog.setButton2(ResourceUtils.getStringResource(this.getActivity(), "alter_dialog_cancel"), new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mAlterDialog.dismiss();
                }
            });

            mAlterDialog.show();


        } else if (v == mClearMessagesLayout) {

            mLoadingDialog = new LoadingDialog(this.getActivity());
            mLoadingDialog.setText(ResourceUtils.getStringResource(this.getActivity(), "rc_public_data_process"));

            mAlterDialog = new AlterDialog(this.getActivity());
            mAlterDialog.setTitle(ResourceUtils.getStringResourceId(this.getActivity(), "rc_conversation_setting_clear_messages_prompt"));

            mAlterDialog.setButton1(ResourceUtils.getStringResource(this.getActivity(), "alter_dialog_confirm"), new OnClickListener() {


                @Override
                public void onClick(View v) {

                    mAlterDialog.dismiss();
                    mLoadingDialog.show();

                    DBHelper.getInstance().clearMessages(mConversation.getConversationType(), mConversation.getTargetId());
                    mClearMessagesLayout.setTag(true);
                    getHandler().obtainMessage(HANDLE_COLSE_DIALOG).sendToTarget();

                }
            });

            mAlterDialog.setButton2(ResourceUtils.getStringResource(this.getActivity(), "alter_dialog_cancel"), new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mAlterDialog.dismiss();
                }
            });

            mAlterDialog.show();

        } else if (v == mSetTopConversationCheckBox) {

            DBHelper.getInstance().setTop(mConversation.getConversationType(), mConversation.getTargetId(), mSetTopConversationCheckBox.isChecked());

        } else if (v == mNewMessageNotifySetCheckBox) {

            setBlockPushStatus(mConversationType, mTargetId, !mNewMessageNotifySetCheckBox.isChecked());

        } else if (v == mDiscussionNameLayout) {

            mLoadingDialog = new LoadingDialog(this.getActivity());
            mLoadingDialog.setText(ResourceUtils.getStringResource(this.getActivity(), "rc_public_data_process"));

            final EditTextRongDialog editTextRongDialog = new EditTextRongDialog(this.getActivity());
            editTextRongDialog.setTitle(ResourceUtils.getStringResourceId(this.getActivity(), "rc_conversation_setting_update_discussion_name"));

            if (mConversation != null) {

                if (!TextUtils.isEmpty(mConversation.getConversationTitle())) {
                    editTextRongDialog.setEditText(mConversation.getConversationTitle());
                } else if (mConversation.getUiDiscussion() != null && !TextUtils.isEmpty(mConversation.getUiDiscussion().getName())) {
                    editTextRongDialog.setEditText(mConversation.getUiDiscussion().getName());
                }

            }

            editTextRongDialog.setButton1(ResourceUtils.getStringResource(this.getActivity(), "alter_dialog_confirm"), new OnClickListener() {


                @Override
                public void onClick(View v) {
                    mTempDiscussionName = editTextRongDialog.getText();

                    if (!TextUtils.isEmpty(mTempDiscussionName)) {
                        updateDiscussionName(mConversation.getTargetId(), mTempDiscussionName);

                        mLoadingDialog.show();
                    }
                    editTextRongDialog.dismiss();
                }
            });

            editTextRongDialog.setButton2(ResourceUtils.getStringResource(this.getActivity(), "alter_dialog_cancel"), new OnClickListener() {

                @Override
                public void onClick(View v) {
                    editTextRongDialog.dismiss();
                }
            });

            editTextRongDialog.show();

        } else if (v == mOpenMemberInviteCheckBox) {

            setDiscussionInviteStatus(mTargetId, mOpenMemberInviteCheckBox.isChecked());

        }

    }


    @Override
    public void onPause() {
        sendBroadcast();
        super.onPause();
    }


    private final void sendBroadcast() {

        Intent intent = new Intent(ACTION_BUNDLE_IO_RONG_IMKIT_CONVERSATION_SETTING);
        intent.putExtra(INTENT_CLEAR_MESSSAGE_SUCCESS, ((Boolean) mClearMessagesLayout.getTag()));

        if (mConversation != null && !TextUtils.isEmpty(mTempDiscussionName)) {
            if (mTempDiscussionName.equals(mConversation.getConversationTitle())) {
                intent.putExtra(INTENT_UPDATE_NAME_DISCUSSION, mTempDiscussionName);
            }
        }

        intent.putExtra(RCloudConst.EXTRA.CONVERSATION, mConversation);
        boolean isSetTop = mSetTopConversationCheckBox.isChecked();

        if (isSetTop != mConversation.isTop()) {
            intent.putExtra(INTENT_SET_TOP_CONVERSATION_SUCCESS, mSetTopConversationCheckBox.isChecked() ? 1 : 0);
        }

        this.getActivity().sendBroadcast(intent);
    }

    private void addMemberForDiscussion(String discussionId, final ArrayList<UserInfo> userInfos) {

        ArrayList<String> userIds = new ArrayList<String>(userInfos.size());

        for (UserInfo userInfo : userInfos) {
            userIds.add(userInfo.getUserId());
        }

        Intent intent = new Intent(MessageLogic.ACTION_DISCUSSION_INVITE_MEMBER);
        intent.putExtra(MessageLogic.DISCUSSION_ID, discussionId);
        intent.putStringArrayListExtra(MessageLogic.DISCUSSION_MEMBER_ID, userIds);

        sendAction(intent, new ActionCallback() {

            @Override
            public void callback(Intent intent) {
                boolean isSuccess = intent.getBooleanExtra(MessageLogic.INTENT_API_OPERATION_STATUS, false);

                if (isSuccess) {
                    getHandler().obtainMessage(HANDLE_ADD_MEMBER_FOR_DISCUSSION_SUCCESS, userInfos).sendToTarget();
                } else {
                    getHandler().obtainMessage(HANDLE_ADD_MEMBER_FOR_DISCUSSION_FAILURE).sendToTarget();
                }
            }
        });
    }

    private void updateDiscussionName(String discussionId, String newDiscussionName) {


        Intent intent = new Intent(MessageLogic.ACTION_DISCUSSION_UPDATE_NAME);
        intent.putExtra(MessageLogic.DISCUSSION_ID, discussionId);
        intent.putExtra(MessageLogic.DISCUSSION_NAME, newDiscussionName);

        sendAction(intent, new ActionCallback() {

            @Override
            public void callback(Intent intent) {
                boolean isSuccess = intent.getBooleanExtra(MessageLogic.INTENT_API_OPERATION_STATUS, false);

                if (isSuccess) {
                    getHandler().obtainMessage(HANDLE_UPDATE_DISCUSSION_NAME_SUCCESS).sendToTarget();
                } else {
                    getHandler().obtainMessage(HANDLE_UPDATE_DISCUSSION_NAME_FAILURE).sendToTarget();
                }
            }
        });

    }


    private void removeMemberForDiscussion(String discussionId, String memberId) {

        Intent intent = new Intent(MessageLogic.ACTION_DISCUSSION_REMOVE_MEMBER);
        intent.putExtra(MessageLogic.DISCUSSION_ID, discussionId);
        intent.putExtra(MessageLogic.INTENT_USER_ID, memberId);

        sendAction(intent, new ActionCallback() {

            @Override
            public void callback(Intent intent) {
                boolean isSuccess = intent.getBooleanExtra(MessageLogic.INTENT_API_OPERATION_STATUS, false);

                if (isSuccess) {
                    getHandler().obtainMessage(HANDLE_REMOVE_MEMBER_SUCCESS).sendToTarget();
                } else {
                    getHandler().obtainMessage(HANDLE_REMOVE_MEMBER_FAILURE).sendToTarget();
                }
            }
        });
    }


    public void createDiscussion(ArrayList<UserInfo> userInfos) {

        mLoadingDialog = new LoadingDialog(this.getActivity());
        mLoadingDialog.setText(ResourceUtils.getStringResource(this.getActivity(), "discussion_create_loading_title"));
        mLoadingDialog.show();


        ArrayList<String> userIds = new ArrayList<String>(userInfos.size());
        ArrayList<String> userNames = new ArrayList<String>(10);

        UserInfo userInfoArg = RCloudContext.getInstance().getRongIMClient().getCurrentUserInfo();
        userInfos.add(userInfoArg);

        for (UserInfo userInfo : userInfos) {
            userIds.add(userInfo.getUserId());

            if (userNames.size() >= 10)
                continue;

            if (!TextUtils.isEmpty(userInfo.getName())) {
                userNames.add(userInfo.getName());
            } else {
                userNames.add(userInfo.getUserId());
            }
        }

        Intent intent = new Intent(MessageLogic.ACTION_DISCUSSION_CREATE);
        intent.putStringArrayListExtra(MessageLogic.DISCUSSION_MEMBER_ID, userIds);
        intent.putExtra(MessageLogic.DISCUSSION_NAME, TextUtils.join(",", userNames));


        sendAction(intent, new ActionCallback() {

            @Override
            public void callback(Intent intentArg) {

                mLoadingDialog.dismiss();
                boolean isSuccess = intentArg.getBooleanExtra(MessageLogic.INTENT_API_OPERATION_STATUS, false);

                Log.d(TAG, "sendAction-----callback-----isSuccess＞＞＞>" + isSuccess);

                if (isSuccess) {

                    String targetId = intentArg.getStringExtra(MessageLogic.DISCUSSION_ID);
                    String discusstionName = intentArg.getStringExtra(MessageLogic.DISCUSSION_NAME);

                    mConversation.setTargetId(targetId);
                    mConversation.setConversationType(ConversationType.DISCUSSION);
                    mConversation.setConversationTitle(discusstionName);

                    getHandler().obtainMessage(HANDLE_CREATE_DISCUSSION_SUCCESS, mConversation).sendToTarget();

                } else {
                    getHandler().obtainMessage(HANDLE_CREATE_DISCUSSION_FAILURE).sendToTarget();
                }
            }

        });
    }


    private final void setDiscussionInviteStatus(String targetId, boolean isOpen) {

        if (TextUtils.isEmpty(targetId)) {
            return;
        }

        Intent intent = new Intent(MessageLogic.ACTION_SET_DISCUSSION_INVITE_STATUS);
        intent.putExtra(MessageLogic.DISCUSSION_ID, targetId);
        intent.putExtra(MessageLogic.DISCUSSION_IS_OPEN_INVITE_STATUS, isOpen);

        sendAction(intent, new ActionCallback() {

            @Override
            public void callback(Intent intentArg) {

                int opStatus = intentArg.getIntExtra(MessageLogic.INTENT_API_OPERATION_STATUS, 1);
                Log.d(TAG, "setDiscussionInviteStatus--opStatus:" + opStatus);

                if (opStatus == 0) {
                    getHandler().obtainMessage(HANDLE_SET_DISCUSSION_INVITE_STATUS_SUCCESS).sendToTarget();
                } else {
                    getHandler().obtainMessage(HANDLE_SET_DISCUSSION_INVITE_STATUS_FAILURE).sendToTarget();
                }
            }
        });

    }

    private final void setBlockPushStatus(ConversationType conversationType, String targetId, boolean isBlock) {

        if (conversationType == null || TextUtils.isEmpty(targetId)) {
            return;
        }

        Intent intent = new Intent(MessageLogic.ACTION_SET_BLOCK_PUSH_STATUS);
        intent.putExtra(MessageLogic.INTENT_CONVERSATION_TYPE, conversationType.getValue());
        intent.putExtra(MessageLogic.TARGET_ID, targetId);
        intent.putExtra(MessageLogic.INTENT_NEW_MESSAGE_BLOCK, isBlock);


        sendAction(intent, new ActionCallback() {

            @Override
            public void callback(Intent intentArg) {
                boolean isSuccess = intentArg.getBooleanExtra(MessageLogic.INTENT_API_OPERATION_STATUS, false);
                int status = intentArg.getIntExtra(MessageLogic.INTENT_STATUE_VALUE, 0);

                Log.d(TAG, "setBlockPushStatus--status:" + status);

                if (isSuccess) {
                    getHandler().obtainMessage(HANDLE_SET_BLOCK_PUSH_STATUS_SUCCESS, status).sendToTarget();
                } else {
                    getHandler().obtainMessage(HANDLE_SET_BLOCK_PUSH_STATUS_FAILURE).sendToTarget();
                }
            }
        });
    }


    private final void getBlockPushStatus(ConversationType conversationType, String targetId) {

        if (conversationType == null || TextUtils.isEmpty(targetId)) {
            return;
        }

        Intent intent = new Intent(MessageLogic.ACTION_GET_BLOCK_PUSH_STATUS);
        intent.putExtra(MessageLogic.TARGET_ID, targetId);
        intent.putExtra(MessageLogic.INTENT_CONVERSATION_TYPE, conversationType.getValue());

        sendAction(intent, new ActionCallback() {

            @Override
            public void callback(Intent intentArg) {

                boolean isSuccess = intentArg.getBooleanExtra(MessageLogic.INTENT_API_OPERATION_STATUS, false);
                int status = intentArg.getIntExtra(MessageLogic.INTENT_STATUE_VALUE, 0);
                Log.d(TAG, "getBlockPushStatus--status:" + status);

                if (isSuccess) {
                    getHandler().obtainMessage(HANDLE_GET_BLOCK_PUSH_STATUS_SUCCESS, status).sendToTarget();
                } else {
                    getHandler().obtainMessage(HANDLE_GET_BLOCK_PUSH_STATUS_FAILURE).sendToTarget();
                }
            }
        });

    }


    @Override
    public void registerBunlderActions(List<String> actions) {

        actions.add(ACTION_BUNDLE_IO_RONG_IMKIT_FRIEND_SELECT);
        super.registerBunlderActions(actions);
    }


    @Override
    public void recevicePageIntnet(Intent intent) {

        List<RongIMClient.UserInfo> userInfos = intent.getParcelableArrayListExtra(RCloudConst.EXTRA.USERS);
        getHandler().obtainMessage(HANDLE_ADD_MEMBER_FOR_DISCUSSION, userInfos).sendToTarget();
        super.recevicePageIntnet(intent);
    }

    @Override
    public boolean onBackPressed() {

        sendBroadcast();
        getActivity().finish();

        return false;
    }

    @Override
    public void getDiscussionInfo(int position, String discusstionId) {

    }


    @Override
    public void getUserInfo(final int position, final String targetId) {

        getHandler().post(new Runnable() {

            @Override
            public void run() {

                getUserInfo(targetId, new GetUserInfoCallback() {

                    @Override
                    public void onSuccess(UIUserInfo user) {
                        getHandler().obtainMessage(HANDLE_GET_USER_INFO_WHAT, position, 0, user).sendToTarget();
                    }

                    @Override
                    public void onError() {

                    }
                });
            }
        });

    }
}