package io.rong.imkit.adapter;

import io.rong.imkit.RCloudContext;
import io.rong.imkit.common.MessageContext;
import io.rong.imkit.model.RCloudType;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.model.UIUserInfo;
import io.rong.imkit.utils.ResourceUtils;
import io.rong.imlib.RongIMClient.Message;
import io.rong.imlib.RongIMClient.UserInfo;

import java.util.List;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;

public abstract class BaseViewProvider implements IViewProvider {

	protected MessageContext mContext;
	protected View mConvertView;
    protected OnGetDataListener mOnGetDataListener;

	public BaseViewProvider() {

	}

	public BaseViewProvider(MessageContext context) {
		mContext = context;
	}

	@Override
	public View getItemView(View convertView, LayoutInflater inflater, RCloudType data, int position, List datas) {

		ViewHolder holder;

		if (convertView == null) {
			convertView = inflater.inflate(setItemLayoutRes(), null);
			mConvertView = convertView;
			holder = new ViewHolder();
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		return getItemView(convertView, holder, data, position, datas);

	}

	@Override
	public View getConvertView() {

		if (mConvertView == null) {
			mConvertView = LayoutInflater.from(mContext).inflate(setItemLayoutRes(), null);
		}

		return mConvertView;
	}

	protected class ViewHolder {

		private SparseArray<View> views = new SparseArray<View>();

		@SuppressWarnings("unchecked")
		public <E extends View> E obtainView(View convertView, int resId) {

			View v = views.get(resId);

			if (null == v) {
				v = convertView.findViewById(resId);
				views.put(resId, v);
			}

			return (E) v;
		}

		@SuppressWarnings("unchecked")
		public <E extends View> E obtainView(View convertView, String resIdstr) {

			int resId = ResourceUtils.getIDResourceId(mContext, resIdstr);

			View v = views.get(resId);

			if (null == v) {
				v = convertView.findViewById(resId);
				views.put(resId, v);
			}

			return (E) v;
		}
	}

	protected abstract int setItemLayoutRes();

	protected abstract View getItemView(View convertView, ViewHolder holder, RCloudType data, int position, List datas);

	public interface OnMessageItemClickListener {

		public void onMessageClick(UIMessage message, View view);

		public void onMessageLongClick(UIMessage message);

		public void onMessageDoubleClick(UIMessage message);

		public void onResendMessage(UIMessage message, int position);

	}

	public OnMessageItemClickListener mOnMessageItemClickListener;

	public void setOnMessageItemClickListener(OnMessageItemClickListener listener) {
		mOnMessageItemClickListener = listener;
	}

//	protected UIUserInfo getUserInfo(String userId) {
//
//		if (RCloudContext.getInstance().getGetUserInfoProvider() != null) {
//			UserInfo userInfo = RCloudContext.getInstance().getGetUserInfoProvider().getUserInfo(userId);
//
//			if (userInfo != null) {
//				return new UIUserInfo(userInfo);
//			}
//		}
//		return null;
//	}

	protected UserInfo onPortaitClick(String userId) {

		UserInfo user = new UserInfo();
        user.setUserId(userId);

		if (RCloudContext.getInstance().getConversationLaunchedListener() != null) {
			RCloudContext.getInstance().getConversationLaunchedListener().onClickUserPortrait(user);
		}

		return user;

	}

	protected void onMessageClick(Message message) {

		if (RCloudContext.getInstance().getConversationLaunchedListener() != null) {
			RCloudContext.getInstance().getConversationLaunchedListener().onClickMessage(message);
		}

	}


    public void setOnGetDataListener(OnGetDataListener mOnGetDataListener) {
        this.mOnGetDataListener = mOnGetDataListener;
    }


    public interface OnGetDataListener {
        public void getDiscussionInfo(int position, String discusstionId);
        public void getUserInfo(int position, String userId,long messageId);
    }

}
