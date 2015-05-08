package io.rong.imlib;

import io.rong.imlib.RongIMClient.ConnectionStatusListener.ConnectionStatus;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by DragonJ on 14-6-21.
 */
final public class RongService extends WakeLockService {

	static final String ACTION_HEARTBEAT = "io.rong.imlib.HEARTBEAT";
	static final String ACTION_HANDLER_REMOTE = "io.rong.imlib.HANDLER_REMOTE";
	static final String ACTION_CONNECTION = "io.rong.imlib.CONNECTION";
    static final String ACTION_DISCONNECTION = "io.rong.imlib.DISCONNECTION";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (RongIMClient.getLastNativeInstance() == null)
			return super.onStartCommand(intent, flags, startId);

		if (intent.getAction().equals(ACTION_HEARTBEAT)) {
			acquireWakeLock();
			RongIMClient.getLastNativeInstance().EnvironmentChangeNotify(105, null, 0, new NativeObject.EnvironmentChangeNotifyListener() {
				@Override
				public void Complete(int type, String desc) {
					releaseWakeLock();
				}
			});
			// Next Heartbeat
			WakeLockUtils.startNextHeartbeat(this);
		} else if (intent.getAction().equals(ACTION_HANDLER_REMOTE)) {
			RongIMClient.getLastNativeInstance().SetWakeupQueryListener(new NativeObject.WakeupQueryListener() {
				@Override
				public void QueryWakeup(int type) {
					acquireWakeLock();
				}

				@Override
				public void ReleaseWakup() {
					releaseWakeLock();
				}
			});
		} else if (intent.getAction().equals(ACTION_CONNECTION)) {
			final int type = intent.getIntExtra("conn_type", 0);
			acquireWakeLock();

			RongIMClient.getLastClientInstance().reconnect(new RongIMClient.ConnectCallback() {
				@Override
				public void onSuccess(String userId) {
					releaseWakeLock();
					WakeLockUtils.startNextHeartbeat(RongService.this);
					if (RongIMClient.getConnectionStatusListener() != null){
						switch (type) {
							case 0:
								RongIMClient.getConnectionStatusListener().onChanged(ConnectionStatus.Cellular_2G);
								break;
							case 1:
								RongIMClient.getConnectionStatusListener().onChanged(ConnectionStatus.WIFI);
								break;
							default:
								break;
						}
					}
						
				}

				@Override
				public void onError(ErrorCode errorCode) {
					releaseWakeLock();
				}
			});
		}else if (intent.getAction().equals(ACTION_DISCONNECTION)){
            acquireWakeLock();

            byte[] data = new byte[]{0};
            RongIMClient.getLastNativeInstance().EnvironmentChangeNotify(101, data, data.length, new NativeObject.EnvironmentChangeNotifyListener() {
                @Override
                public void Complete(int type, String desc) {
                    releaseWakeLock();
                }
            });
        }
		return super.onStartCommand(intent, flags, startId);
	}
}
