package io.rong.imlib;

import io.rong.imlib.RongIMClient.ConnectionStatusListener.ConnectionStatus;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.os.Process;
import android.util.Log;

/**
 * Created by DragonJ on 14-6-21.
 */
final public class RongReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		Log.d(this.getClass().getCanonicalName(), intent.toString());		
		if (intent.getAction().equals(RongService.ACTION_HEARTBEAT)) {
			Intent heartbeatIntent = new Intent(context, RongService.class);
			heartbeatIntent.setAction(RongService.ACTION_HEARTBEAT);
			context.startService(heartbeatIntent);
		} else if (intent.getAction().equals(RongService.ACTION_HANDLER_REMOTE)) {
			Intent handlerRemote = new Intent(context, RongService.class);
			handlerRemote.setAction(RongService.ACTION_HANDLER_REMOTE);
			context.startService(handlerRemote);
		} else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

			if (null != parcelableExtra) {
				NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
				NetworkInfo.State state = networkInfo.getState();

				if (state == NetworkInfo.State.CONNECTED) {
					int type = 0;
					if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
						type = 0;
					} else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
						type = 1;
					}

					Intent connectionIntent = new Intent(context, RongService.class);
					connectionIntent.setAction(RongService.ACTION_CONNECTION);
					connectionIntent.putExtra("conn_type", type);
					context.startService(connectionIntent);

                    PushUtil.startPushSerive(context);

				} else if (state == NetworkInfo.State.DISCONNECTED) {
					
					if (RongIMClient.getConnectionStatusListener() != null)
						RongIMClient.getConnectionStatusListener().onChanged(ConnectionStatus.NETWORK_UNAVAILABLE);
                    Intent connectionIntent = new Intent(context, RongService.class);
                    connectionIntent.setAction(RongService.ACTION_DISCONNECTION);
                    context.startService(connectionIntent);
					WakeLockUtils.cancelHeartbeat(context);
				}
			}
		}else if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            PushUtil.startPushSerive(context);
        }else if(intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)||intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)){
            PushUtil.startPushSerive(context);
        }


	}
}
