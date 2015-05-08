package io.rong.imlib;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

/**
 * Created by DragonJ on 14-6-23.
 */
class WakeLockUtils {

    private static final int HEARTBEAT_SPAN = 1000*60*3; // 10 seconds

    static void startNextHeartbeat(Context context){
        Intent heartbeatIntent = new Intent(context,RongReceiver.class);
        heartbeatIntent.setAction(RongService.ACTION_HEARTBEAT);
        PendingIntent intent = PendingIntent.getBroadcast(context,0,heartbeatIntent,0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long time = SystemClock.elapsedRealtime()+HEARTBEAT_SPAN;
        //Cancel old
        alarmManager.cancel(intent);
        //Obtain new
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,time,intent);
    }

    static void cancelHeartbeat(Context context){
        Intent heartbeatIntent = new Intent(context,RongReceiver.class);
        heartbeatIntent.setAction(RongService.ACTION_HEARTBEAT);
        PendingIntent intent = PendingIntent.getBroadcast(context,0,heartbeatIntent,0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(intent);
    }
}
