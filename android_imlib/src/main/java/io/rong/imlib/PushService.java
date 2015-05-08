package io.rong.imlib;

import io.rong.imlib.PushClient.ClientListener;
import io.rong.imlib.PushClient.ConnectCallback;
import io.rong.imlib.PushProtocalStack.ConnAckMessage;
import io.rong.imlib.PushProtocalStack.PublishMessage;
import io.rong.imlib.RongIMClient.ConversationType;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;


public final class PushService extends Service {

    private PushClient client;

    /**
     * 根据appId生成一个对应的切固定的notifation的id
     */
    private Map<String, Integer> appIdNotificationIdMapping;

    private HashMap<String, ArrayList<PushContact>> conversation;

    public static final String NOTIFICATION_SENDED_BROADCAST_ACTION = "io.rong.imlib.PushService.action.notificationsended";
    private NotificationSendedBroadCastReciver nsb;

    private WifiStatusReceiver mWifiStatusReceiver;
    private NetWorkStatusReceiver mNetWorkStatusReceiver;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("service create", "============onCreate===========");
        init();
//        startTask();
    }    

    private void init() {
        appIdNotificationIdMapping = new HashMap<String, Integer>();
        conversation = new HashMap<String, ArrayList<PushContact>>();

        nsb = new NotificationSendedBroadCastReciver();
        IntentFilter intentFilter = new IntentFilter(NOTIFICATION_SENDED_BROADCAST_ACTION);
        this.registerReceiver(nsb, intentFilter);
        
		mWifiStatusReceiver = new WifiStatusReceiver();
		registerReceiver(mWifiStatusReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
		
		mNetWorkStatusReceiver = new NetWorkStatusReceiver();
	    registerReceiver(mNetWorkStatusReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));  
    }


    private void startTask() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                Map<String, String> ipAndPort = PushUtil.parseServerIdAndPort(PushUtil.httpPostConnect(PushUtil.getDeviceId(PushService.this)));
                if (ipAndPort.get("ip") == null || ipAndPort.get("port") == null) return;
//				Log.d("service create","=============PushService=======thread================" + ipAndPort.get("ip"));
                // 只要第一个参数 mac地址
                client = new PushClient(
                        PushUtil.getDeviceId(PushService.this),
                        "1", "",
                        new MyClientListener());
                try {
                    client.connect(ipAndPort.get("ip"), Integer.parseInt(ipAndPort.get("port")), new MyConnectCallback());

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        }).start();
    }

    private class MyClientListener implements ClientListener {

        /**
         * {appId:"e0x9wycfx7flq",fromUserId:"38",fromUserName:"常延顺",fromUserPo:"",
         * title:"",content:"",channelType:1,channelId:"",channelName:""}
         * 
         * {objectName:"RC:VoipCallMsg",
			appId:"e0x9wycfx7flq",fromUserId:"38",fromUserName:"常延顺",
			fromUserPo:"http://www.gravatar.com/avatar/96d8ae5c29b4866389ccd6969cde588d?s=82",
			title:"",content:"null",channelType:"0",channelId:"",channelName:""}
			
			{objectName:"RC:TxtMsg",
			appId:"e0x9wycfx7flq",fromUserId:"38",fromUserName:"常延顺",
			fromUserPo:"http://www.gravatar.com/avatar/96d8ae5c29b4866389ccd6969cde588d?s=82",
			title:"",content:"常延顺:vvvv",channelType:"0",channelId:"",channelName:""}
         */
        @Override
        public void messageArrived(PublishMessage msg) {
            Log.i("service create", "=============messageArrived====================" + msg.getDataAsString());
            Bundle bundle = PushUtil.parseJson(msg.getDataAsString());

				
			if("RC:VoipCallMsg".equals(bundle.getString("objectName"))){
				startVoip(bundle);
			}else if("RC:VoipFinishMsg".equals(bundle.getString("objectName")) || "RC:VoipAcceptMsg".equals(bundle.getString("objectName"))){
				return;
			}else{
				createConversation(bundle);
				showNotification(bundle.getString("appId"), bundle.getString("channelType"), bundle.getString("title"));
			}
        }
    }
    
    private void startVoip(Bundle bundle){
        String token = this.getSharedPreferences("rc_token", Context.MODE_PRIVATE).getString("token_value", "");
		
		Intent intent = new Intent();   
		intent.setAction("io.rong.voipkit.calledSideActivity.action");
		intent.putExtra("appId",bundle.getString("appId"));
		intent.putExtra("token",token);
		intent.putExtra("fromUserPhoteUri",bundle.getString("fromUserPo"));
		intent.putExtra("fromUserName",bundle.getString("fromUserName"));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startActivity(intent);
	}

    private class MyConnectCallback implements ConnectCallback {
        @Override
        public void connected(ConnAckMessage msg) throws IOException {
            Log.i("service create", "=============MyConnectCallback=======================" + msg.getStatus());
        }
    }

    /**
     * 封装消息
     *
     * @param bundle
     */
    private void createConversation(Bundle bundle) {
        if (conversation.get(bundle.getString("appId")) == null) {
            List<PushMessage> mess = new ArrayList<PushMessage>();
            PushMessage mes = new PushMessage(bundle.getString("title"), bundle.getString("content")
                    , bundle.getString("channelType"), bundle.getString("channelId"), bundle.getString("channelName"));
            mess.add(mes);

            ArrayList<PushContact> cs = new ArrayList<PushContact>();
            PushContact contact = new PushContact(bundle.getString("fromUserId"), bundle.getString("fromUserName"));
            contact.setMessages(mess);
            cs.add(contact);
            conversation.put(bundle.getString("appId"), cs);
        } else {
            List<PushContact> cs = conversation.get(bundle.get("appId"));
            for (PushContact c : cs) {
                if (c.getId().equals(bundle.getString("fromUserId"))) {
                    PushMessage mes = new PushMessage(bundle.getString("title"), bundle.getString("content")
                            , bundle.getString("channelType"), bundle.getString("channelId"), bundle.getString("channelName"));
                    c.getMessages().add(mes);
                    return;
                }
            }

            List<PushMessage> mess = new ArrayList<PushMessage>();
            PushMessage mes = new PushMessage(bundle.getString("title"), bundle.getString("content")
                    , bundle.getString("channelType"), bundle.getString("channelId"), bundle.getString("channelName"));
            mess.add(mes);

            PushContact contact = new PushContact(bundle.getString("fromUserId"), bundle.getString("fromUserName"));
            contact.setMessages(mess);
            cs.add(contact);
        }
    }


    @SuppressWarnings({"deprecation"})
    private void showNotification(String appid, String channelType, String conversationTitle) {

        createAppidNotificationIdMapping(appid);

        NotificationManager nm = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        Notification no = new Notification(/*this.getApplicationInfo().icon*/getApplicationIcon(PushUtil.getPackageNameByAppid(appid)), "您有了一条新消息", System.currentTimeMillis());

        no.flags = Notification.FLAG_AUTO_CANCEL;

        String title = "", content = "";
        List<PushContact> cs = conversation.get(appid);

        Uri uri = null;
        if (cs.size() == 1) {
            if (cs.get(0).getMessages().size() == 1) {
                title = cs.get(0).getName() + " 发来一条消息";
                content = cs.get(0).getMessages().get(0).getContent();
            } else {
                title = cs.get(0).getName();
                content = "发来了" + cs.get(0).getMessages().size() + "条消息";
            }

            String conversationtype = ConversationType.setValue(Integer.parseInt(channelType)).toString().toLowerCase();
            uri = Uri.parse("rong://" + PushUtil.getPackageNameByAppid(appid)).buildUpon().appendPath("conversation")
                    .appendPath(conversationtype).appendQueryParameter("targetId", cs.get(0).getId()).build();
        } else {
            title = getApplicationName(PushUtil.getPackageNameByAppid(appid));
            int count = 0;
            for (PushContact c : cs) {
                count += c.getMessages().size();
            }
            content = cs.size() + "个联系人发来" + count + "条消息";
//rong://io.rong.imkit.demo/conversationList
            uri = Uri.parse("rong://" + PushUtil.getPackageNameByAppid(appid)).buildUpon().appendPath("conversationlist").build();
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(uri);
        Log.i("service onStartCommand", "==========uri=========" + uri.toString());
//		intent.addCategory("com.xiazdong.category");

        // getIntent().getParcelableArrayListExtra("yes")//取得传递的Arraylsit集合
//		intent.putParcelableArrayListExtra("yes", conversation.get(appid));
//		intent.setData(Uri.parse("custom://"+System.currentTimeMillis()));//样就实现了Intent的区别化，以后每次传入的Intent都会具备不同的Extra
        intent.putExtra("appId", appid);
//		intent.putExtra("channelId",conversation.get(appid).get(0).getMessages().get(0).getChannelId());
//		intent.putExtra("channelName",conversation.get(appid).get(0).getMessages().get(0).getChannelName());
//		intent.putExtra("appId",appid);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        no.setLatestEventInfo(this, title, content, contentIntent);
        no.icon = getApplicationIcon(PushUtil.getPackageNameByAppid(appid));//this.getApplicationInfo().icon;
        no.defaults = Notification.DEFAULT_SOUND;
        nm.notify(appIdNotificationIdMapping.get(appid), no);
    }

    /**
     * @param appId
     */
    private void createAppidNotificationIdMapping(String appId) {
        if (appIdNotificationIdMapping.get(appId) == null) {
            appIdNotificationIdMapping.put(appId, appIdNotificationIdMapping.size());
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//		Log.i("service onStartCommand","=========serviced===onStartCommand===========");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("service onDestroy", "========serviced=======onDestroy===========");
		client.disconnectByNormal();
        unregisterReceiver(nsb);
        unregisterReceiver(mWifiStatusReceiver);
        unregisterReceiver(mNetWorkStatusReceiver);
    }

    /**
     * 通知消息已经发送到指定的Activity后，在该activity中发出的
     * 一个广播，并被该接收器接收。
     * 发送广播
     * Intent intent = new Intent();
     * intent.setAction(NOTIFICATION_SENDED_BROADCAST_ACTION);
     * sendBroadcast(intent);//发送广播
     */
    public class NotificationSendedBroadCastReciver extends BroadcastReceiver {
        public void onReceive(Context contex, Intent intent) {
            Log.i("aaa", "===============接收到一个NOTIFICATION_SENDED_BROADCAST_ACTION的广播====" + intent.getStringExtra("appId"));
            String appId = intent.getStringExtra("appId");
            conversation.remove(appId);
        }
    }

    /**
     * 根据应用包名获取应用名称
     *
     * @param packageName
     * @return
     */
    public String getApplicationName(String packageName) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        String applicationName =
                (String) packageManager.getApplicationLabel(applicationInfo);
        return applicationName;
    }

    /**
     * 根据应用包名获取应用icon
     *
     * @param packageName
     * @return
     */
    public int getApplicationIcon(String packageName) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        return applicationInfo.icon;
    }
    
    
    public class NetWorkStatusReceiver extends BroadcastReceiver {
        @Override  
        public void onReceive(Context context, Intent intent){
            State wifiState = null;  
            State mobileState = null;  
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
            wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();  
            mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();  
            if (wifiState != null && mobileState != null  
                    && State.CONNECTED != wifiState  
                    && State.CONNECTED == mobileState) { 
            	Log.i("aff","===============手机网络连接成功====3g==========");
            	startTask();
                // 手机网络连接成功  
            } else if (wifiState != null && mobileState != null  
                    && State.CONNECTED != wifiState  
                    && State.CONNECTED != mobileState) {  
            	Log.i("aff","===============手机没有任何的网络  ==============");
            	endTask();
                // 手机没有任何的网络  
            } /*else if (wifiState != null && State.CONNECTED == wifiState) {  
                // 无线网络连接成功  
            	Log.e("aff","=============== 无线网络连接成功=====wifi=========");
            	startTask();
            }  */
        }
    }
    
    private void endTask(){
    	 Log.i("service onDestroy", "=======push=serviced=======endTask===========");
    	 if(client!=null){
	    	try {
	            client.disconnect();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
    	 }
    }
    
    public class WifiStatusReceiver extends BroadcastReceiver {
  		@Override
  		public void onReceive(Context c, Intent intent) {
  			NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
  			State netstatus = info.getState();//获得网络状态
  			if (netstatus == NetworkInfo.State.CONNECTING){
  				Log.i("aff","==================正在加入。。。===========");
  			}else if (netstatus == NetworkInfo.State.CONNECTED) {//连接上指定Ap时
  				Log.i("aff","================wifi==加入成功。。。===========");
  				startTask();
  			}
  		}
  	}

}
