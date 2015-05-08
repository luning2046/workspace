package io.rong.imlib;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * 应用卸载广播
 * @author Administrator
 */
public final class InstallAndUninstallListener extends BroadcastReceiver {    
    @Override  
    public void onReceive(Context context, Intent intent) {   
    	
    	Log.d("afff","=================InstallAndUninstallListener====================");
    	
        PackageManager manager = context.getPackageManager();   
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {       
        	String packageName = intent.getData().getSchemeSpecificPart(); 
        }   
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {  
        	String packageName = intent.getData().getSchemeSpecificPart(); 
        	PushUtil.removeMappingByPacageName(packageName);
//        	context.startService(new Intent(context,PushService.class));
        	Log.d("afff","=================Uninstall===================="+packageName);
        	PushUtil.startPushSerive(context);
        }   
    }
};
