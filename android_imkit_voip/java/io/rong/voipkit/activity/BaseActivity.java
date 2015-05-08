package io.rong.voipkit.activity;

import io.rong.imlib.RongIMClient.MessageContent;
import io.rong.voipkit.message.VoipAcceptMessage;
import io.rong.voipkit.message.VoipFinishMessage;
import io.rong.voiplib.RongIMVoip;
import io.rong.voiplib.utils.VoipUtil;

import com.ultrapower.mcs.engine.ITransportListener;
import com.ultrapower.mcs.engine.TransportType;
import com.ultrapower.mcs.engine.UmcsConfig;

import java.io.IOException;




import java.io.InputStream;
import java.net.URL;

import com.ultrapower.mcs.engine.UMCS;
import com.ultrapower.mcs.engine.internal.UMCSInternal;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class BaseActivity extends Activity{
	
	public static final String RECIVE_MSG_BROADCAST_ACTION = "com.ccrc.avtest.action.reciveMsg";
	protected ReciveMsgBroadCastReciver rmb;
	
	protected UMCS session = null;
	protected UMCSInternal uUMCSInternal;
	protected AudioManager mAudioManager = null;
	private WakeLock wakeLock = null;
	
	protected String  remoteIp = "";
	protected int localPort, remotePort;
	
	protected String peerid;
	protected String appId,mySelfId,sessionId,peerUserPhoteUri;
	
	
	protected boolean isVoipSuccess = false;//startVoip或acceptVoip是否成功
	
	protected TextView calling_state;
	protected ImageView user_photo;
	protected MediaPlayer mMediaPlayer;

	protected int initAudioMode;//进入本应用时，系统初始的audiaMdoe
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		
		rmb = new ReciveMsgBroadCastReciver();
		IntentFilter intentFilter = new IntentFilter(RECIVE_MSG_BROADCAST_ACTION);
		this.registerReceiver(rmb, intentFilter);
		
		initAudioManager();
//		
		initAudioMode = mAudioManager.getMode();
	}
	
	
	private void initAudioManager(){
		PowerManager pm = (PowerManager) this
				.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "avtest");
		
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		wakeLock.acquire(); // screen stay on during the call
		if (null == session) {
			// session = new AVSession(this);
			session = new UMCS(this);
		}
		if (null == uUMCSInternal){
			uUMCSInternal = new UMCSInternal();
		}
	}
	
	protected void initCall() {
		UmcsConfig uConfig = new UmcsConfig();
		uConfig.setMultiMode(false);
		uConfig.setTraceFilter(2);
		uConfig.setTransportType(TransportType.kUdpGernal);
		int ret = session.Init(uConfig);
		// init
		if (-1 == ret) {
			return;
		}
	}
	
	protected void createVoip(){
		setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
		initCall();
		createLocaleAudio();// create audio
		createRemoteAudio();
		StartChat();
	}
	
//	private void initSpeaker() {
//		setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
//	}

	protected void createLocaleAudio() {
//		initSpeaker();
		int ret;
		ret = uUMCSInternal.SetECEnable(1, 4);// ((ecEnable == true) ? 1 : 0, 3); // 3
										// speakerphone, 4 loud
										// speakerphone
		if (-1 == ret) {
			return;
		}
		ret = uUMCSInternal.SetNSEnable(1, 6);// ((nsEnable == true) ? 1 : 0, 6); // 4
										// moderate, 5 high, 6 very high
		if (-1 == ret) {
			return;
		}
		//ret = session.SetMicVolumeAutoGain(1, 4);// ((gainEnable == true) ? 1 :
													// 0, 4); // 4 FixedDigital
		if (-1 == ret) {
			return;
		}
		ret = uUMCSInternal.SetHighPassFilterEnable(1);// ((hfEnable == true) ? 1 :
													// 0);
		if (-1 == ret) {
			return;
		}
		
		ret = session.CreateLocalAudio(1);
		
		if (-1 == ret) {
			return;
		}
	}
	
	protected void createRemoteAudio() {
		// create remote audio
		int ret = session.CreateRemoteAudio(1);
		if (-1 == ret) {
			return;
		}
	}

	protected void StartChat() {
		//createRemote();
		int ret = 0;// 3, session, 1,1)
		String localIp =  VoipUtil.getLocalIpAddress();
		ret = session.StartTransport(localIp, localPort, remoteIp, remotePort, null, new ITransportListener(){
			@Override
			public void OnTransportFailed() {
				
			}
		});
		ret = session.StartSendLocalAudio(1);
		if (-1 == ret) {
			return;
		}
		
		ret = session.StartRecvRemoteAudio(1);
		if (-1 == ret) {
			return;
		}
	}
	
	
	
	
	protected void sendMsg(MessageContent mc){
		Intent intent = new Intent();
		intent.setAction("io.rong.imkit.broadcast.SENDMESSAGE");//向 imkit中的io.rong.imkit.broadcast。SendMessageBroadcast这个广播类发消息
		intent.putExtra("messageContent", mc);
		sendBroadcast(intent);// 发送广播
	}
	
	/**
	 * 通知消息已经发送到指定的Activity后，在该activity中发出的 一个广播，并被该接收器接收。 发送广播 Intent intent =
	 * new Intent(); intent.setAction(NOTIFICATION_SENDED_BROADCAST_ACTION);
	 * sendBroadcast(intent);//发送广播
	 */
	public class ReciveMsgBroadCastReciver extends BroadcastReceiver {

		public void onReceive(Context contex, Intent intent) {
			MessageContent mc = (MessageContent) intent
					.getParcelableExtra("messageContent");
			Log.d("voip","==========voip=====接收到一个com.ccrc.avtest.action.reciveMsg的广播===="+ mc);
			if (mc instanceof VoipAcceptMessage) {
				
				((CallSideActivity) BaseActivity.this).CalledAcceptCall();// 通知主叫方，被叫方接受了呼叫请求
				
			} else if (mc instanceof VoipFinishMessage) {
				
				VoipFinishMessage vfm = (VoipFinishMessage) mc;
				if (vfm.getFinish_state() == VoipFinishMessage.FINISH_NORMAL) {
					Toast.makeText(BaseActivity.this, "对方已经挂机！！！",Toast.LENGTH_LONG).show();
					
					stopCountTime();
					
//					sendNormalMsg("通话时长"+calling_state.getText().toString());
					
				} else if (vfm.getFinish_state() == VoipFinishMessage.FINISH_REFUSE) {
					Toast.makeText(BaseActivity.this, "对方拒绝了你的请求！！！",
							Toast.LENGTH_LONG).show();
				}
				BaseActivity.this.finish();
			}
		}
	}
	
//	private void sendNormalMsg(String info){
//		TextMessage textMessage = new TextMessage(info);
//		
//		Intent intent = new Intent();
//		intent.setAction("io.rong.imkit.broadcast.SENDMESSAGE");// 向
//																// imkit中的io.rong.imkit.broadcast。SendMessageBroadcast这个广播类发消息
//		intent.putExtra("messageContent", textMessage);
//		intent.putExtra("targetid", peerid);
//		
//		sendBroadcast(intent);// 发送广播
//	}
	
	private String showTimeCount(long time) {
		if (time >= 360000000) {
			return "00:00:00";
		}
		String timeCount = "";
		long hourc = time / 3600000;
		String hour = "0" + hourc;
		hour = hour.substring(hour.length() - 2, hour.length());

		long minuec = (time - hourc * 3600000) / (60000);
		String minue = "0" + minuec;
		minue = minue.substring(minue.length() - 2, minue.length());

		long secc = (time - hourc * 3600000 - minuec * 60000) / 1000;
		String sec = "0" + secc;
		sec = sec.substring(sec.length() - 2, sec.length());
		timeCount = hour + ":" + minue + ":" + sec;
		return timeCount;
	}

	
	private Handler stepTimeHandler;
	private Runnable mTicker;
	long startTime = 0;

	public void startCountTime() {

		// 清零 开始计时
		calling_state.setText("00:00:00");
		stepTimeHandler = new Handler();
		startTime = System.currentTimeMillis();
		mTicker = new Runnable() {
			public void run() {
				String content = showTimeCount(System.currentTimeMillis()
						- startTime);
				calling_state.setText(content);

				long now = SystemClock.uptimeMillis();
				long next = now + (1000 - now % 1000);
				stepTimeHandler.postAtTime(mTicker, next);
			}
		};
		// 启动计时线程，定时更新
		mTicker.run();
	}

	public void stopCountTime() {
		if(stepTimeHandler!=null)
			stepTimeHandler.removeCallbacks(mTicker);
	}
	
	/**
	 * 播放 听筒音/铃声
	 */
	protected void playerRingtone(int audiaMode) {
		Log.d("playerNotificationSound","==================playerNotificationSound============");
		setVolumeControlStream(AudioManager.STREAM_ALARM);
		mAudioManager.setMode(audiaMode);
		try {
			AssetFileDescriptor fileDescriptor = this.getAssets().openFd("voip_ring.mp3");			
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
                    fileDescriptor.getStartOffset(),
                    fileDescriptor.getLength());
			final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				mMediaPlayer.setLooping(true);
				mMediaPlayer.prepare();
				mMediaPlayer.start();
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void releaseRingtong(){
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		recoverAudiaMode();
	}
	
	private void recoverAudiaMode(){
		mAudioManager.setMode(initAudioMode);
	}
	
	
	/**
	 * 使静音和免提按钮生效
	 */
	protected void enableHandOffAndSoundOff(final ImageView sound_off,final ImageView hands_off){
		
		sound_off.getDrawable().setLevel(2);
		
		sound_off.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("aff","=================sound_off_id================");
				if(sound_off.getDrawable().getLevel()==2){//静音
					session.StopSendLocalAudio(1);
					sound_off.getDrawable().setLevel(3);
				}else if(sound_off.getDrawable().getLevel()==3){//恢复
					session.StartSendLocalAudio(1);
					sound_off.getDrawable().setLevel(2);
				}
			}
		});
		
		hands_off.getDrawable().setLevel(2);
		
		hands_off.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("aff","=================hands_off================");
				if(hands_off.getDrawable().getLevel()==2){//免提
					session.SetLoudSpeakerEnable(true);
					hands_off.getDrawable().setLevel(3);
				}else if(hands_off.getDrawable().getLevel()==3){//恢复
					session.SetLoudSpeakerEnable(false);
					hands_off.getDrawable().setLevel(2);
				}
			}
		});
	}
	
	
	private Handler setPhotoHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bitmap bitmap = (Bitmap)msg.obj;
			if(bitmap!=null)
				user_photo.setImageBitmap(bitmap);
		}
	};
	
	protected void setPhoto(final String uri){
		new Thread(new Runnable() {
			@Override
			public void run() {
	Log.e("aff","======================setPhoto=================="+uri);
				BitmapFactory.Options sDefaultOptions = new BitmapFactory.Options();
				sDefaultOptions.inDither = true;
		    	sDefaultOptions.inScaled = true;
		    	sDefaultOptions.inDensity = DisplayMetrics.DENSITY_MEDIUM;
		    	sDefaultOptions.inTargetDensity = getResources().getDisplayMetrics().densityDpi;
				  
				InputStream inputStream;
				Bitmap bitmap = null;
				try {
					inputStream = new URL(uri).openStream();
					bitmap = BitmapFactory.decodeStream(inputStream, null,sDefaultOptions);
				} catch (IOException e) {
					e.printStackTrace();
				}
				Message msg = setPhotoHandler.obtainMessage();
				msg.obj = bitmap;
				setPhotoHandler.sendMessage(msg);
			}
		}).start();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		releaseRingtong();
		session.StartSendLocalAudio(1);
		session.SetLoudSpeakerEnable(false);
		session.Terminate();
		
		Log.d("fff","==============onDestroy====================="+isVoipSuccess);
		if(isVoipSuccess)
			RongIMVoip.endVoip(appId, sessionId, mySelfId);
		
		this.unregisterReceiver(rmb);
		
		wakeLock.release();
	}
}
