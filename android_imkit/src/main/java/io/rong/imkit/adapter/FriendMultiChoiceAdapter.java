package io.rong.imkit.adapter;

import io.rong.imkit.common.RCloudConst;
import io.rong.imkit.model.Friend;
import io.rong.imkit.utils.ResourceUtils;
import io.rong.imkit.veiw.AsyncImageView;
import io.rong.imlib.RongIMClient.UserInfo;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.sea_monster.core.resource.model.Resource;

public class FriendMultiChoiceAdapter extends FriendListAdapter {

    private List<String> mChoiceFriendIds;
    private MutilChoiceCallback mCallback;
    private ArrayList<Friend> mFriends;

    public FriendMultiChoiceAdapter(Context context, List<Friend> friends, List<String> mSelectedList) {
        super(context, friends);
        this.mFriends = (ArrayList<Friend>) friends;
        mChoiceFriendIds = mSelectedList;
    }

    @Override
    protected void bindView(View v, int partition, List<Friend> data, int position) {
        super.bindView(v, partition, data, position);

        ViewHolder holder = (ViewHolder) v.getTag();
        TextView name = holder.name;
        AsyncImageView photo = holder.photo;

        Friend friend = data.get(position);
        name.setText(friend.getNickname());

        Resource res = friend.getPortraitResource();

        photo.setDefaultDrawable(ResourceUtils.getDrawableById(mContext, "rc_default_portrait"));
        photo.setResource(res);

        Log.d("UserId", "------userId---------");

        String userId = friend.getUserId();
        holder.userId = userId;

        if (friend.isSelected()) {
            holder.choice.setButtonDrawable(ResourceUtils.getDrawableById(mContext, "rc_multi_choice_disable"));
            holder.choice.setChecked(true);
            holder.choice.setEnabled(false);
        } else {
            holder.choice.setEnabled(true);
            holder.choice.setChecked(mChoiceFriendIds.contains(friend.getUserId()));
            holder.choice.setButtonDrawable(ResourceUtils.getDrawableById(mContext, "rc_checkbox_selector"));
        }

//        holder.choice.setChecked(mChoiceFriendIds.contains(friend.getUserId()));
    }

    @Override
    protected void newSetTag(View view, ViewHolder holder, int position, List<Friend> data) {
        super.newSetTag(view, holder, position, data);

        CheckBox checkBox = (CheckBox) view.findViewById(android.R.id.checkbox);
        checkBox.setVisibility(View.VISIBLE);
        holder.choice = checkBox;
    }

    @Override
    public void onItemClick(String friendId, CheckBox checkBox) {

        if (!checkBox.isEnabled()) {
            return;
        }

        boolean isChoose = checkBox.isChecked();


        if (isChoose) {
            checkBox.setChecked(!isChoose);

            mChoiceFriendIds.remove(friendId);

            if (mCallback != null) {
                mCallback.callback(mChoiceFriendIds.size());
            }
        } else {

            if (mChoiceFriendIds.size() <= RCloudConst.SYS.DISCUSSION_PEOPLE_MAX_COUNT - 2) {

                checkBox.setChecked(!isChoose);

                mChoiceFriendIds.add(friendId);

                if (mCallback != null) {
                    mCallback.callback(mChoiceFriendIds.size());
                }
            } else {
                if (mCallback != null) {
                    mCallback.callback(RCloudConst.SYS.DISCUSSION_PEOPLE_MAX_COUNT);
                }
            }
        }


    }

    public List<String> getChoiceList() {
        return mChoiceFriendIds;
    }

    public ArrayList<UserInfo> getChoiceUserInfos() {
        ArrayList<UserInfo> userInfos = new ArrayList<UserInfo>();

        if (mChoiceFriendIds.size() > 0) {

            for (String userId : mChoiceFriendIds) {

                for (Friend friend : mFriends) {

                    if (userId.equals(friend.getUserId()) && !friend.isSelected()) {
                        UserInfo userInfo = new UserInfo();
                        userInfo.setName(friend.getNickname());
                        userInfo.setPortraitUri(friend.getPortrait());
                        userInfo.setUserId(friend.getUserId());
                        userInfos.add(userInfo);
                    }
                }
            }
        }

        return userInfos;
    }

    public void setCallback(MutilChoiceCallback callback) {
        this.mCallback = callback;
    }

    public interface MutilChoiceCallback {
        public void callback(int count);
    }

}
