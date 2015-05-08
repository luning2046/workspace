package io.rong.voipkit.activity;


import io.rong.voiplib.RongIMVoip;
import io.rong.voiplib.NativeObject.StartVoipCallback;
import io.rong.voiplib.utils.ResourceUtils;
import io.rong.voiplib.utils.VoipUtil;
import io.rong.voipkit.message.VoipCallMessage;
import io.rong.voipkit.message.VoipFinishMessage;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CallSideActivity extends BaseActivity{
	
	private String peerUserName;
	private TextView userName;
	private ImageView sound_off,hands_off;
	
	private String myselfName,token;
	 
	public static final int STARTVOIP_SUCCESS = 0;
	public static final int STARTVOIP_ERROR = 1;
	
	Handler connectedHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == STARTVOIP_SUCCESS) {
				VoipCallMessage vcm = (VoipCallMessage) msg.obj;

				// 发送通信消息
				Intent intent = new Intent();
				intent.setAction("io.rong.imkit.broadcast.SENDMESSAGE");// 向
																		// imkit中的io.rong.imkit.broadcast。SendMessageBroadcast这个广播类发消息
				intent.putExtra("messageContent", vcm);
				CallSideActivity.this.sendBroadcast(intent);// 发送广播

				playerRingtone(AudioManager.MODE_IN_CALL);
			} else if (msg.what == STARTVOIP_ERROR) {
				String info = "网络繁忙，请稍后在试！！！";
				if (msg.arg1 == 404) {
					info = "对方正在通话!";
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				new AlertDialog.Builder(CallSideActivity.this).setTitle(info)
                 	.setMessage("是否退出通话界面？").setPositiveButton("是", new OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                    	 CallSideActivity.this.finish();
                     }
                 })
//                 .setNegativeButton("否", null)
                 .show();
			}
		}
	};
	    
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(ResourceUtils.getLayoutResourceId(this, "rc_callside_layout"));

		userName = (TextView)findViewById(ResourceUtils.getIDResourceId(this,"user_name"));
		sound_off = (ImageView)findViewById(ResourceUtils.getIDResourceId(this,"sound_off_id"));
		hands_off = (ImageView)findViewById(ResourceUtils.getIDResourceId(this, "hands_off_id"));
		user_photo = (ImageView)findViewById(ResourceUtils.getIDResourceId(this, "user_photo"));
		
		calling_state = (TextView)findViewById(ResourceUtils.getIDResourceId(this, "calling_state"));
		
		reciveIntentData();
		
		userName.setText(peerUserName);
		setPhoto(peerUserPhoteUri);
		
		delayTimesFinish();
		Log.i("fff","===================called====================mode=="+mAudioManager.getMode()+"==="+mySelfId+"==="+appId+"==="+sessionId);
		
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		connectVoipServer_call();
	}
	
	private void reciveIntentData(){
		appId = getIntent().getStringExtra("appId");
		token = getIntent().getStringExtra("token");
		mySelfId = getIntent().getStringExtra("mySelfId");
		myselfName = getIntent().getStringExtra("myselfName");
		
		peerid = getIntent().getStringExtra("peerUId");
		peerUserName = getIntent().getStringExtra("peerUserName");
		peerUserPhoteUri = getIntent().getStringExtra("peerUserPhoteUri");
	}
	
	private void connectVoipServer_call() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				localPort = VoipUtil.getPort();
				RongIMVoip.startVoip(appId, token, mySelfId, peerid,localPort, new StartVoipCallback() {

							@Override
							public void OnSuccess(String sessionId, String ip,
									String remoteTransferPort,String remoteControlPort) {
								Log.i("voip",
										"===============java enter OnSuccess==============="+ sessionId + "====" + ip+ "===" + remoteTransferPort);
								isVoipSuccess = true;
								CallSideActivity.this.sessionId = sessionId;
								CallSideActivity.this.remoteIp = ip;
								CallSideActivity.this.remotePort = Integer.parseInt(remoteTransferPort); 
								Message msg = connectedHandler.obtainMessage();

								VoipCallMessage vcm = new VoipCallMessage(sessionId, ip, Integer.parseInt(remoteTransferPort),
										Integer.parseInt(remoteControlPort),peerid, peerUserName, mySelfId, myselfName);
								msg.obj = vcm;
								msg.what = STARTVOIP_SUCCESS;
								connectedHandler.sendMessage(msg);
							}

							@Override
							public void OnError(int errorcode,String description) {
								Log.i("void","============java enter OnError=======================" + errorcode + "==="+ description);
								isVoipSuccess = false;
								Message msg = connectedHandler.obtainMessage();
								msg.what = STARTVOIP_ERROR;
								msg.arg1 = errorcode;
								connectedHandler.sendMessage(msg);
							}
						});
			}
		}).start();
	}
	
	/**
	 * 挂机
	 */
	public void doFinishChat(View view){
		Log.i("aff","======================finishChat==================");
		VoipFinishMessage vfm = new VoipFinishMessage(peerid);
		vfm.setFinish_state(VoipFinishMessage.FINISH_NORMAL);
		sendMsg(vfm);
		
		this.finish();
	}
	
	@Override
	public void onBackPressed() {
		VoipFinishMessage vfm = new VoipFinishMessage(peerid);
		vfm.setFinish_state(VoipFinishMessage.FINISH_NORMAL);
		sendMsg(vfm);
		super.onBackPressed();
	}

	Handler delayHandler;
	Runnable finishActivity;
	
	private void delayTimesFinish(){
		delayHandler = new Handler();
		finishActivity = new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(CallSideActivity.this, "对方未接听！！",Toast.LENGTH_LONG).show();
				
				VoipFinishMessage vfm = new VoipFinishMessage(peerid);
				vfm.setFinish_state(VoipFinishMessage.FINISH_NORMAL);
				sendMsg(vfm);
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				CallSideActivity.this.finish();
			}
		};
		delayHandler.postDelayed(finishActivity, 60*1000);
	}
	
	/**
	 * 被叫方接受了呼叫
	 */
	public void CalledAcceptCall(){
		
		Log.i("effaaa","=========================被叫方接受了呼叫请求==============================");
		Toast.makeText(this,"对方接受了通话请求！！！",Toast.LENGTH_LONG).show();
		delayHandler.removeCallbacks(finishActivity);
		startCountTime();
		releaseRingtong();
		
		createVoip();
		
		enableHandOffAndSoundOff(sound_off,hands_off);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		delayHandler.removeCallbacks(finishActivity);
	}
}