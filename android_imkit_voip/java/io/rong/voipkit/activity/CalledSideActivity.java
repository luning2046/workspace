package io.rong.voipkit.activity;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ConnectCallback;
import io.rong.imlib.RongIMClient.ConversationType;
import io.rong.imlib.RongIMClient.MessageContent;
import io.rong.imlib.RongIMClient.OnReceiveMessageListener;
import io.rong.imlib.RongIMClient.SendMessageCallback;
import io.rong.imlib.RongIMClient.Message;
import io.rong.voipkit.message.VoipAcceptMessage;
import io.rong.voipkit.message.VoipCallMessage;
import io.rong.voipkit.message.VoipFinishMessage;
import io.rong.voiplib.NativeObject.AcceptVoipCallback;
import io.rong.voiplib.RongIMVoip;
import io.rong.voiplib.utils.ResourceUtils;
import io.rong.voiplib.utils.VoipUtil;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CalledSideActivity extends BaseActivity {
	
	private Button call_finish;
	private LinearLayout vioce_control_container;
	private TextView user_name;
	
	private ImageView sound_off,hands_off;
	
	private VoipCallMessage voipCallMessage;
	
	private RongIMClient mRongIMClient = null;
	
	private boolean isPushJumpHere = false;
	private String fromUserNameByPush = "";
	
	Handler acceptVoipHandler = new Handler(){
    	@Override
    	public void handleMessage(android.os.Message msg) {
    		if(!isPushJumpHere)
    			sendMsg(new VoipAcceptMessage(peerid));
    		else
    			sendMessageByPushJump(new VoipAcceptMessage(peerid));
    		switchView();
    		startCountTime();
    		createVoip();
    	}
	};
	
	
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(ResourceUtils.getLayoutResourceId(this,"rc_calledside_layout"));

		call_finish = (Button)findViewById(ResourceUtils.getIDResourceId(this,"call_finish"));
		vioce_control_container = (LinearLayout)findViewById(ResourceUtils.getIDResourceId(this,"vioce_control_container"));
		user_name = (TextView)findViewById(ResourceUtils.getIDResourceId(this,"user_name"));
		user_photo = (ImageView)findViewById(ResourceUtils.getIDResourceId(this, "user_photo"));
		calling_state = (TextView)findViewById(ResourceUtils.getIDResourceId(this,"calling_state"));
		
		sound_off = (ImageView)findViewById(ResourceUtils.getIDResourceId(this,"sound_off_id"));
		hands_off = (ImageView)findViewById(ResourceUtils.getIDResourceId(this,"hands_off_id"));
		
		
		if(getIntent().getStringExtra("appId")!=null && getIntent().getStringExtra("token")!=null){//push 过来
			appId = getIntent().getStringExtra("appId");
			String token = getIntent().getStringExtra("token");
			peerUserPhoteUri = getIntent().getStringExtra("fromUserPhoteUri");
			fromUserNameByPush = getIntent().getStringExtra("fromUserName");
			Log.e("fff","===================doPush=====================ak="+appId+"=token="+token+"==fromUserName="+fromUserNameByPush);
			isPushJumpHere = true;
			doPush(appId,token);
		}else{
			isPushJumpHere = false;
			appId = getIntent().getStringExtra("appId");
			voipCallMessage = (VoipCallMessage)getIntent().getParcelableExtra("VoipCallMessage");
			peerUserPhoteUri = getIntent().getStringExtra("peerUserPhoteUri");
			initParam();
		}
		
		playerRingtone(AudioManager.MODE_NORMAL);
	}

	private void initParam(){
		Log.e("aff","=================initParam==============up==="+voipCallMessage.getToId());
		mySelfId = voipCallMessage.getToId();
		peerid = voipCallMessage.getFromId();
		user_name.setText(voipCallMessage.getFromUserName());
		setPhoto(peerUserPhoteUri);
		
		remoteIp = voipCallMessage.getIp();
		remotePort = voipCallMessage.getRemoteTransferPort();
		sessionId = voipCallMessage.getSessionId();

		Log.e("fff","============================mySelfId="+mySelfId+"==peerid="+peerid+"==un="
					+user_name+"===remoteIp="+remoteIp+"==remotePort="+remotePort+"==sid="+sessionId+"=--="+voipCallMessage.getFromUserName());
	}
	
	Handler connectedHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			
				String info = "网络连接错误，请稍后在试！！！";
				 new AlertDialog.Builder(CalledSideActivity.this).setTitle(info)
                 .setMessage("是否退出通话界面？").setPositiveButton("是", new OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                    	 CalledSideActivity.this.finish();
                     }
                 })
                 .show();
		}
	};
	
	private void connectVoipServer_accept(){
    	new Thread(new Runnable() {
			@Override
			public void run() {
				localPort = VoipUtil.getPort();
		    	RongIMVoip.acceptVoip(appId, sessionId,mySelfId,remoteIp, 
		    			remotePort, localPort, 
		    			voipCallMessage.getRemoteControlPort(),new AcceptVoipCallback() {
							public void OnSuccess(){
								Log.e("voip", "==============acceptVoip=======java enter OnSuccess===================");
								isVoipSuccess = true;
								android.os.Message msg = acceptVoipHandler.obtainMessage();
								acceptVoipHandler.sendMessage(msg);
							}
							public void OnError(int errorcode, String description){
								isVoipSuccess = false;
								Log.e("voip", "===========acceptVoip=====================java enter OnError=====");
								connectedHandler.sendEmptyMessage(0);
							}
				});
			}
		}).start();
    }
	
	/**
	 * 应答
	 */
	public void doYes(View view){
		
		if(voipCallMessage == null){
			Toast.makeText(CalledSideActivity.this,"网络异常请稍后再试！",Toast.LENGTH_LONG).show();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			finish();
			return;
		}
		Log.e("fff","====================doYes=======================");
		
		releaseRingtong();
		
		connectVoipServer_accept();
	}
	
	/**
	 * 拒绝
	 */
	public void doRefuse(View view){
		
		if(voipCallMessage == null){
			Toast.makeText(CalledSideActivity.this,"网络异常请稍后再试！",Toast.LENGTH_LONG).show();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			finish();
			return;
		}
		VoipFinishMessage vfm = new VoipFinishMessage(peerid);
		vfm.setFinish_state(VoipFinishMessage.FINISH_REFUSE);
		if(!isPushJumpHere)
			sendMsg(vfm);
		else
			sendMessageByPushJump(vfm);
		
		this.finish();
	}
	
	@Override
	public void onBackPressed() {
		
		if(voipCallMessage == null){
			Toast.makeText(CalledSideActivity.this,"网络异常请稍后再试！",Toast.LENGTH_LONG).show();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			finish();
			return;
		}
		VoipFinishMessage vfm = new VoipFinishMessage(peerid);
		vfm.setFinish_state(VoipFinishMessage.FINISH_NORMAL);
		if(!isPushJumpHere)
			sendMsg(vfm);
		else
			sendMessageByPushJump(vfm);
		super.onBackPressed();
	}
	
	/**
	 * 挂机
	 */
	public void doFinishChat(View view){
		VoipFinishMessage vfm = new VoipFinishMessage(peerid);
		vfm.setFinish_state(VoipFinishMessage.FINISH_NORMAL);
		if(!isPushJumpHere)
			sendMsg(vfm);
		else
			sendMessageByPushJump(vfm);
		this.finish();
	}
	
	private void switchView(){
		call_finish.setVisibility(View.VISIBLE);
		vioce_control_container.setVisibility(View.VISIBLE);
		
		enableHandOffAndSoundOff(sound_off,hands_off);
	}
	
	
	public void doPush(String appKey,String token){
//		if(!PushUtil.processesIsAlive(this,PushUtil.getAppName(this))){
			RongIMClient.init(this, appKey, 0);
//			Log.e("aff","=========RongIMClient===init==================");
//		}
	    try {
	    	mRongIMClient = RongIMClient.connect(token, new ConnectCallback() {
				
				@Override
				public void onSuccess(String userId) {
					Log.e("fff","=============voip.connect======onSuccess======================");
					registerReceiveMessage();
				}
				
				@Override
				public void onError(ErrorCode errorCode) {
					Log.e("fff","=============voip.connect======onError======================");
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static final int MESSAGE_VOIPCALL = 1020;
	private static final int MESSAGE_VOIPFINISH = 1021;
	
	Handler doPushRecevieHandler = new Handler(){
    	@Override
    	public void handleMessage(android.os.Message msg) {
    		switch (msg.what) {
			case MESSAGE_VOIPCALL:
				if(voipCallMessage == null){
					Toast.makeText(CalledSideActivity.this,"网络异常请稍后再试！",Toast.LENGTH_LONG).show();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					finish();
					return;
				}
				initParam();
				break;
			case MESSAGE_VOIPFINISH:
				Toast.makeText(CalledSideActivity.this, "对方已经挂机！！！",Toast.LENGTH_LONG).show();
				stopCountTime();
				finish();
				break;
			default:
				break;
			}
    		
    	}
	}; 
	public void registerReceiveMessage(){
		final long startTime = System.currentTimeMillis();
		mRongIMClient.setOnReceiveMessageListener(new OnReceiveMessageListener() {
			@Override
			public void onReceived(Message message) {
				if (message.getConversationType() == ConversationType.PRIVATE) {
                    Log.e("afff","==============================push=====message====type======="+message.getContent());
					if(message.getContent() instanceof VoipCallMessage) {
						voipCallMessage = (VoipCallMessage)message.getContent();
						Log.i("fff","=========================push=================voipcall==============="+voipCallMessage.getFromId());
						if(voipCallMessage!=null)
							voipCallMessage.setFromUserName(fromUserNameByPush);
						doPushRecevieHandler.sendEmptyMessage(MESSAGE_VOIPCALL);
					}else if(message.getContent() instanceof VoipFinishMessage) {
						Log.i("fff","=========================push==================voipfinish==============");
						long endTime = System.currentTimeMillis();
						if((endTime-startTime)>1000){
							doPushRecevieHandler.sendEmptyMessage(MESSAGE_VOIPFINISH);	
						}
					}
                }
			}
		});
	}
	
	
	public void sendMessageByPushJump(MessageContent mc) {
		Log.d("aaa","============voip===sendmessage==========");
		if(mc instanceof VoipCallMessage){
			VoipCallMessage vcm = (VoipCallMessage)mc;
			mRongIMClient.sendMessage(ConversationType.PRIVATE, vcm.getToId(), vcm, new MySendMessageCallback());
//			mRongIMClient.sendNotification(ConversationType.PRIVATE, vcm.getToId(), content, callback)
		}else if(mc instanceof VoipAcceptMessage){
			VoipAcceptMessage vam = (VoipAcceptMessage)mc;
			mRongIMClient.sendMessage(ConversationType.PRIVATE, vam.getToId(), vam, new MySendMessageCallback());
		}else if(mc instanceof VoipFinishMessage){
			VoipFinishMessage vfm = (VoipFinishMessage)mc;
			mRongIMClient.sendMessage(ConversationType.PRIVATE, vfm.getToId(), vfm, new MySendMessageCallback());
		}
	}
	class MySendMessageCallback implements SendMessageCallback {
		@Override
		public void onSuccess() {
			Log.i("SendMessageBroadcast", "=================onSucces====================");
		}
		@Override
		public void onProgress(int progress) {
			Log.i("SendMessageBroadcast", "===============onProgress==================");
		}
		@Override
		public void onError(ErrorCode errorCode) {
			Log.i("SendMessageBroadcast", "================onError=============="+errorCode);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(isPushJumpHere)
			android.os.Process.killProcess(android.os.Process.myPid());
	}
}