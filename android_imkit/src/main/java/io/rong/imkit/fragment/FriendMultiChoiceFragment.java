package io.rong.imkit.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.RCloudContext;
import io.rong.imkit.adapter.FriendMultiChoiceAdapter;
import io.rong.imkit.adapter.FriendMultiChoiceAdapter.MutilChoiceCallback;
import io.rong.imkit.common.RCloudConst;
import io.rong.imkit.model.Friend;
import io.rong.imkit.model.UIUserInfo;
import io.rong.imkit.utils.ResourceUtils;
import io.rong.imkit.utils.RongToast;
import io.rong.imlib.RongIMClient;

public class FriendMultiChoiceFragment extends FriendListFragment implements Handler.Callback {

    private static final String TAG = FriendMultiChoiceFragment.class.getSimpleName();

    private static final int HANDLE_UPDATE_CONFIRM_BUTTON = 10001;

    private MutilChoiceCallback mCallback;
//=============================页面中“确定(0)”按钮=================================================================
    private TextView mConfirmTextView;
    private String mConfirmFromatString;
    private boolean isDiscussionAddFriend = false;
    private ArrayList<String> memberIds;
    private RongIMClient.ConversationType mConversationType;
    private Handler mHandle;

    public static final String BUNDLE_ACTION_CONVERSATION_ADD_OR_CREATE="bundle_action_conversation_add_or_create";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        isDiscussionAddFriend = getActivity().getIntent().getBooleanExtra(ConversationSettingFragment.INTENT_SELECT_FRIEND_FOR_ADD, true);
        memberIds = getActivity().getIntent().getStringArrayListExtra(ConversationSettingFragment.INTENT_ADDED_MEMBER_IDS_FOR_DISCUSSION);

        if (memberIds == null)
            memberIds = new ArrayList<String>();

        int conversationTypeValue = getActivity().getIntent().getIntExtra(ConversationListFragment.INTENT_PRIVATE_SELECT_PEOPLE, -1);

        if (conversationTypeValue != -1) {
            mConversationType = RongIMClient.ConversationType.setValue(conversationTypeValue);
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        setMultiChoice(true, new ArrayList<String>(memberIds));
        super.onViewCreated(view, savedInstanceState);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 0, 10, 0);
        mConfirmTextView = new TextView(getActivity());
        mConfirmTextView.setLayoutParams(layoutParams);
        mConfirmTextView.setGravity(Gravity.CENTER);
        mConfirmTextView.setTextAppearance(getActivity(), android.R.style.TextAppearance_Small_Inverse);

        mConfirmTextView.setPadding(10, 0, 10, 0);
//==============================================单击“确定”按钮的响应事件===================================================================================================
        mConfirmTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectPeopleComplete();
            }
        });

        selectButtonShowStyle(0);

        getActionBar().addView(mConfirmTextView);

        getActionBar().getTitleTextView().setText(ResourceUtils.getStringResourceId(getActivity(), "friend_list_multi_choice_title"));

        mHandle = new Handler(this);

    }

    private void selectButtonShowStyle(int selectedCount) {

        if (selectedCount > 0) {
            mConfirmTextView.setEnabled(true);
            mConfirmTextView.setBackgroundResource(ResourceUtils.getDrawableResourceId(getActivity(), "rc_select_friends_selector"));
            mConfirmFromatString = getResources().getString(ResourceUtils.getStringResourceId(this.getActivity(), "friend_list_multi_choice_comfirt_btn"));
            mConfirmTextView.setTextColor(ResourceUtils.getColorByResId(getActivity(), "rc_text_color_secondary_inverse"));
            mConfirmTextView.setText(String.format(mConfirmFromatString, selectedCount));
        } else {
            mConfirmTextView.setEnabled(false);
            mConfirmTextView.setBackgroundResource(ResourceUtils.getDrawableResourceId(getActivity(), "rc_select_friend_disable"));
            mConfirmFromatString = getResources().getString(ResourceUtils.getStringResourceId(this.getActivity(), "friend_list_multi_choice_comfirt_btn"));
            mConfirmTextView.setText(String.format(mConfirmFromatString, 0));
            mConfirmTextView.setTextColor(ResourceUtils.getColorByResId(getActivity(), "rc_text_color_select_friend_btn_disable"));
        }
    }

    private final void selectPeopleComplete() {

        if (mAdapter == null)
            return;

        ArrayList<RongIMClient.UserInfo> userInfos = ((FriendMultiChoiceAdapter) mAdapter).getChoiceUserInfos();


        int selected = 0;
        if (memberIds != null) {
            selected = memberIds.size();
        }

        if (userInfos.size() + selected > RCloudConst.SYS.DISCUSSION_PEOPLE_MAX_COUNT - 1) {
            RongToast.makeText(this.getActivity(), ResourceUtils.getStringResource(this.getActivity(), "friend_multi_choice_people_max_prompt")).show();
            return;
        }
//===========================这个广播被ActionBaseFrament中的BaseReceiver广播接收===处理这个广播在ConverationListFragment中recevicePageIntnet方法============================================================
        Intent intent = new Intent(ACTION_BUNDLE_IO_RONG_IMKIT_FRIEND_SELECT);
        intent.putParcelableArrayListExtra(RCloudConst.EXTRA.USERS, userInfos);

        if (memberIds.size() > 0 && mConversationType != null) {
            if(mConversationType== RongIMClient.ConversationType.PRIVATE){
                String targetId=memberIds.get(0);
                RongIMClient.UserInfo userInfo=new RongIMClient.UserInfo();
                userInfo.setUserId(targetId);

                if(mFriendsList!=null&& !TextUtils.isEmpty(targetId)){
                    for(Friend friend:mFriendsList){
                        if(targetId.equals(friend.getUserId())){
                            userInfo.setName(friend.getNickname());
                            userInfo.setUserId(friend.getUserId());
                            userInfo.setPortraitUri(friend.getPortrait());
                            break;
                        }
                    }
                }

                userInfos.add(userInfo);
            }
            intent.putExtra(ConversationListFragment.INTENT_PRIVATE_SELECT_PEOPLE, mConversationType.getValue());
            intent.putExtra(BUNDLE_ACTION_CONVERSATION_ADD_OR_CREATE, BUNDLE_ACTION_CONVERSATION_ADD_OR_CREATE);
        }

        intent.putParcelableArrayListExtra(RCloudConst.EXTRA.USERS, userInfos);

//        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().sendBroadcast(intent);
        getActivity().finish();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (mCallback == null) {
            mCallback = new MutilChoiceCallback() {
                @Override
                public void callback(int count) {

                    if (count >= RCloudConst.SYS.DISCUSSION_PEOPLE_MAX_COUNT) {
                        RongToast.makeText(FriendMultiChoiceFragment.this.getActivity(), ResourceUtils.getStringResource(FriendMultiChoiceFragment.this.getActivity(), "friend_multi_choice_people_max_prompt")).show();
                        return;
                    } else {
                        mHandle.obtainMessage(HANDLE_UPDATE_CONFIRM_BUTTON, count - memberIds.size()).sendToTarget();
                    }
                }
            };
        }

        FriendMultiChoiceAdapter adapter = (FriendMultiChoiceAdapter) mAdapter;
        adapter.setCallback(mCallback);

        super.onItemClick(parent, view, position, id);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void registerActions(List<String> actions) {
        actions.add(RCloudContext.CLIENT_CONNECTED_TO_SDK);
        actions.add(RCloudContext.CLIENT_DISCONNECT_TO_SDK);

        super.registerActions(actions);
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == HANDLE_UPDATE_CONFIRM_BUTTON) {
            selectButtonShowStyle((Integer) msg.obj);
        }
        return false;
    }
}
