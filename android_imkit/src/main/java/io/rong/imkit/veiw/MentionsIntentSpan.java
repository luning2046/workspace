package io.rong.imkit.veiw;

import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.style.ClickableSpan;
import android.view.View;

public class MentionsIntentSpan extends ClickableSpan implements ParcelableSpan {

	private final String userId;

	private final static int typeId = 9097463;

	private static final String USER_FEEDS_URL = "fetion://cn.com.fetion.win/userfeed";
	private static final String PERSONAL_FEEDS_URL = "fetion://cn.com.fetion.win/personalfeed";

	public MentionsIntentSpan(String userId) {
		this.userId = userId;
	}

	public MentionsIntentSpan(Parcel src) {
		this.userId = src.readString();
	}

	public int getSpanTypeId() {
		return typeId;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(userId);
	}

	public String getUserId() {
		return userId;
	}

	private final boolean isEnter(String tag) {

//		if (PersonalFeedsFragment.TAG.equals(tag)) {
//			return true;
//		} else if (UserFeedsFragment.TAG.equals(tag)) {
//			return true;
//		}

		return false;

	}

	public void onClick(View widget) {

//		if (widget.getContext() instanceof Activity) {
//
//			if (!userId.equals(WinContext.getInstance().getLogicManager().getLoginLogic().getLoginId())) {
//
//				Uri uri = Uri.withAppendedPath(Uri.parse(USER_FEEDS_URL), userId);
//				Intent intent = new Intent("android.intent.action.VIEW", uri);
//				intent.setData(uri);
//
//				Activity activity = (Activity) widget.getContext();
//				activity.startActivity(intent);
//				activity.overridePendingTransition(R.anim.fragment_slide_bottom_enter, 0);
//
//			} else {
//
//				Uri uri = Uri.withAppendedPath(Uri.parse(PERSONAL_FEEDS_URL), userId);
//				Intent intent = new Intent("android.intent.action.VIEW", uri);
//				intent.setData(uri);
//
//				Activity activity = (Activity) widget.getContext();
//				activity.startActivity(intent);
//				activity.overridePendingTransition(R.anim.fragment_slide_bottom_enter, 0);
//
//			}
//		}

	}
}
