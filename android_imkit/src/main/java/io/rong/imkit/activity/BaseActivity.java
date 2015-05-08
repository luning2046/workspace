package io.rong.imkit.activity;

import io.rong.imkit.RCloudContext;
import io.rong.imkit.common.RCloudConst.SYS;
import io.rong.imkit.model.UIUserInfo;
import io.rong.imkit.utils.ResourceUtils;
import io.rong.imlib.RongIMClient.UserInfo;
import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

/**
 * Created by zhjchen on 14-4-8.
 */
@SuppressWarnings("unchecked")
public abstract class BaseActivity extends Activity {

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);// 使得音量键控制媒体声音
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(setContentViewResId());
		initView();
		initData();
	}

	protected <T extends View> T getViewById(int id) {
		return (T) findViewById(id);
	}

	protected <T extends View> T getViewById(String id) {
		return (T) findViewById(ResourceUtils.getIDResourceId(this, id));
	}

	protected abstract int setContentViewResId();

	protected abstract void initView();

	protected abstract void initData();

	protected UIUserInfo getUserInfo(String userId) {

		if (RCloudContext.getInstance().getGetUserInfoProvider() != null) {
			UserInfo userInfo = RCloudContext.getInstance().getGetUserInfoProvider().getUserInfo(userId);

			if (userInfo != null) {
				return new UIUserInfo(userInfo);
			}
		}
		return null;
	}

}