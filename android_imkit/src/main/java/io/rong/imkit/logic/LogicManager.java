package io.rong.imkit.logic;

import io.rong.imkit.service.RCloudService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.util.Log;


/**
 * Created by zhjchen on 14-3-20.
 */
public class LogicManager {

	private static LogicManager mlogicManager;
	private Map<Integer, BaseLogic> mLogics;


	private MessageLogic messageLogic;

	public static LogicManager getInstance() {
		if (mlogicManager == null) {
			mlogicManager = new LogicManager();
		}
		return mlogicManager;
	}

	public void init(RCloudService fCloudService) {

		if (fCloudService == null) {
			throw new NullPointerException("FCloudService is null");
		}

		Log.d("LogicManager---init", "LogicManager------>");

		mLogics = new ConcurrentHashMap<Integer, BaseLogic>();

		messageLogic = new MessageLogic(fCloudService);
		// mTestLogic = new TestLogic(fCloudService);

		mLogics.put(messageLogic.hashCode(), messageLogic);
	}

	public void onDestory() {

		if (!mLogics.isEmpty()) {
			for (Map.Entry<Integer, BaseLogic> entry : mLogics.entrySet()) {
				entry.getValue().destroy();
			}
			mLogics.clear();
		}
	}
}