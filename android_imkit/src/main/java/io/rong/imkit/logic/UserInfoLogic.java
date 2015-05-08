package io.rong.imkit.logic;

import io.rong.imkit.service.RCloudService;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;


public class UserInfoLogic extends BaseLogic {

	public static final String ACTION_GET_USER_INFO = "cn.rongcloud.imkit.user.logic.userinfo";
	public static final String ACTION_GET_USER_PORTRAIT = "cn.rongcloud.imkit.user.logic.portrait";

	RCloudService mFCloudService;

	public UserInfoLogic(RCloudService fCloudService) {
		super(fCloudService);

		mFCloudService = fCloudService;

		List<String> actions = new ArrayList<String>();
		actions.add(ACTION_GET_USER_INFO);
		actions.add(ACTION_GET_USER_PORTRAIT);

		fCloudService.registerAction(this, actions);
	}

	@Override
	public void onHandleAction(Intent intent) {

		if (intent == null)
			return;

		final String action = intent.getAction();

		if (ACTION_GET_USER_INFO.equals(action)) {

		} else if (ACTION_GET_USER_PORTRAIT.equals(action)) {

		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
	}

}
