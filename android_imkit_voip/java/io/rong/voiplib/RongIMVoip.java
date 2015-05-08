package io.rong.voiplib;

import android.util.Log;
import io.rong.voiplib.NativeObject;
import io.rong.voiplib.NativeObject.StartVoipCallback;
import io.rong.voiplib.NativeObject.AcceptVoipCallback;

public final class RongIMVoip {
	
	private final static String TAG = "HYJ";

	private static NativeObject nativeObj;
	
	static {
		nativeObj = new NativeObject();
	}
	
	/**
	 * 启动voip通话，回调函数中返回sessionId、ip、port
	 * @param appId
	 * @param token
	 * @param fromId
	 * @param toId
	 */
	public static void startVoip(String appId, String token, String fromId, String toId, int localPort,StartVoipCallback mStartVoipCallback) {
		nativeObj.StartVoip(appId, token, fromId, toId, localPort, mStartVoipCallback);
	}
	
	/**
	 * 接受voip通话，回调函数中返回成功
	 * @param appId
	 * @param sessionId
	 * @param userId
	 * @param serverIp
	 * @param port
	 */
	public static void acceptVoip(String appId, String sessionId, String userId, String serverIp, int serverTransferPort, int localPort, int serverControlPort, AcceptVoipCallback mAcceptVoipCallback) {
		nativeObj.AcceptVoip(appId, sessionId, serverIp, serverTransferPort, userId, localPort, serverControlPort, mAcceptVoipCallback);
	}
	
	/**
	 * 结束voip通话
	 * @param appId
	 * @param sessionId
	 * @param userId
	 */
	public static void endVoip(String appId, String sessionId, String userId) {
		nativeObj.EndVoip(appId, sessionId, userId, new AcceptVoipCallback() {
			public void OnSuccess()
			{
				Log.w(TAG, "java enter OnSuccess");
			}
			public void OnError(int errorcode, String description)
			{
				Log.w(TAG, "java enter OnError");
			}
		});
	}
}
