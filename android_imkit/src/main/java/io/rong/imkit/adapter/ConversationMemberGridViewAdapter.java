package io.rong.imkit.adapter;

import io.rong.imkit.RCloudContext;
import io.rong.imkit.model.Friend;
import io.rong.imkit.utils.ResourceUtils;
import io.rong.imkit.veiw.AsyncImageView;
import io.rong.imlib.RongIMClient.UserInfo;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.sea_monster.core.resource.model.Resource;

@SuppressWarnings("unused")
public class ConversationMemberGridViewAdapter extends BaseAdapter<Friend> {

    private boolean isAdd = false;
    private boolean isSub = false;
    private boolean isDeleteState = false;

    private OnGridViewImageLisenter mGridViewImageLisenter;
    private String mUserId = RCloudContext.getInstance().getRongIMClient().getCurrentUserInfo().getUserId();

    private ConversationListAdapter.OnGetDataListener mOnGetDataListener;

    public void setOnGetDataListener(ConversationListAdapter.OnGetDataListener mOnGetDataListener) {
        this.mOnGetDataListener = mOnGetDataListener;
    }

    public ConversationMemberGridViewAdapter(Context context, List<Friend> data) {
        super(context, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(setItemLayoutRes(), null);
            holder = new ViewHolder();
            holder.deleteImageView = (ImageView) convertView.findViewById(android.R.id.icon1);
            holder.nameTextView = (TextView) convertView.findViewById(android.R.id.text1);
            holder.portaintImageView = (AsyncImageView) convertView.findViewById(android.R.id.icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            holder.nameTextView.setText("");
        }

        Friend friend = dataSet.get(position);
//        Log.d("ConversationSettingFragment", "getView--position:" + position);
//        Log.d("ConversationSettingFragment", "getView--friend-Name:" + friend.getNickname());
//        Log.d("ConversationSettingFragment", "getView--friend-userId:" + friend.getUserId());


        if (friend.isSub()) {
            holder.deleteImageView.setVisibility(View.GONE);
            holder.nameTextView.setVisibility(View.INVISIBLE);
            holder.portaintImageView.setImageDrawable(ResourceUtils.getDrawableById(mContext, "rc_ic_setting_friends_delete"));
        } else if (friend.isAdd()) {
            holder.nameTextView.setVisibility(View.INVISIBLE);
            holder.deleteImageView.setVisibility(View.GONE);
            holder.portaintImageView.setImageDrawable(ResourceUtils.getDrawableById(mContext, "rc_ic_setting_friends_add"));
        } else{
            holder.nameTextView.setVisibility(View.VISIBLE);

            if (friend.isDel()) {
                holder.deleteImageView.setVisibility(View.VISIBLE);
            }else {
                holder.deleteImageView.setVisibility(View.GONE);
            }

            if(TextUtils.isEmpty(friend.getNickname())){
                if(mOnGetDataListener!=null){
                    mOnGetDataListener.getUserInfo(position,friend.getUserId());
                }
                holder.portaintImageView.setImageDrawable(ResourceUtils.getDrawableById(mContext, "rc_default_portrait"));
            }else{
                holder.portaintImageView.setResource(friend.getPortraitResource());
                holder.nameTextView.setText(friend.getNickname());
            }

        }


        final String userId = friend.getUserId();
        final int aPosition = position;

        if (holder.deleteImageView.getVisibility() == View.VISIBLE) {

            holder.deleteImageView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (mGridViewImageLisenter != null) {
                        Log.d("ConversationSettingFragment", "ConversationMemberGridViewAdapter_userId:" + userId);
                        v.setTag(userId);
                        mGridViewImageLisenter.onGridViewImageClick(v, aPosition);
                    }

                }
            });
        }

        convertView.setTag(ResourceUtils.getDrawableResourceId(mContext, "rc_default_portrait"), friend);

        return convertView;

    }

    public int setItemLayoutRes() {
        return ResourceUtils.getLayoutResourceId(mContext, "rc_item_conversation_member");
    }

    class ViewHolder {
        AsyncImageView portaintImageView;
        ImageView deleteImageView;
        TextView nameTextView;
    }

    public void addAddButtonItem() {
        Friend friend = new Friend();
        friend.setAdd(true);
        friend.setCall(true);
        dataSet.add(friend);
        isAdd = true;
        notifyDataSetChanged();
    }

    public void addSubButtonItem() {
        Friend friend = new Friend();
        friend.setSub(true);
        friend.setCall(true);
        dataSet.add(friend);
        isSub = true;
        notifyDataSetChanged();
    }

    public void deleteToggle(boolean isDelete) {

        if (dataSet != null) {

            for (Friend friend : dataSet) {

                if (isDelete) {
                    if (!TextUtils.isEmpty(friend.getUserId()) && !friend.getUserId().equals(mUserId)) {
                        friend.setDel(true);
                        isDeleteState = true;
                    }
                } else {
                    friend.setDel(false);
                    isDeleteState = false;
                }
            }
        }
    }

    public interface OnGridViewImageLisenter {
        public void onGridViewImageClick(View view, int position);
    }

    public void setGridViewImageLisenter(OnGridViewImageLisenter mGridViewImageLisenter) {
        this.mGridViewImageLisenter = mGridViewImageLisenter;
    }

    public boolean isDeleteState() {
        return isDeleteState;
    }


    public boolean isAdd() {
        return isAdd;
    }

    public void setAdd(boolean isAdd) {
        this.isAdd = isAdd;
    }

    public boolean isSub() {
        return isSub;
    }

    public void setSub(boolean isSub) {
        this.isSub = isSub;
    }

}
