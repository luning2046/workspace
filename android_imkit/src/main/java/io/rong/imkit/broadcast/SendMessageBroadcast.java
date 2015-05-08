package io.rong.imkit.broadcast;

import io.rong.imkit.RCloudContext;
import io.rong.imlib.RongIMClient.ConversationType;
import io.rong.imlib.RongIMClient.Message;
import io.rong.imlib.RongIMClient.MessageContent;
import io.rong.imlib.RongIMClient.SendMessageCallback;
import io.rong.imlib.RongIMClient.TextMessage;
import io.rong.voipkit.message.VoipCallMessage;
import io.rong.voipkit.message.VoipAcceptMessage;
import io.rong.voipkit.message.VoipFinishMessage;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

public class SendMessageBroadcast  extends BroadcastReceiver {
	
	public void onReceive(Context contex, Intent intent) {
		
		Log.d("aaa","===============接收到一个SendMessageBroadcast的广播===="+intent.getParcelableExtra("messageContent"));
		MessageContent mc = intent.getParcelableExtra("messageContent");
		if(mc instanceof VoipCallMessage){
			VoipCallMessage vcm = (VoipCallMessage)mc;
			Message message = RCloudContext.getInstance().getRongIMClient().sendMessage(ConversationType.PRIVATE, vcm.getToId(), vcm, new MySendMessageCallback());
		}else if(mc instanceof VoipAcceptMessage){
			VoipAcceptMessage vam = (VoipAcceptMessage)mc;
			Message message = RCloudContext.getInstance().getRongIMClient().sendMessage(ConversationType.PRIVATE, vam.getToId(), vam, new MySendMessageCallback());
		}else if(mc instanceof VoipFinishMessage){
			VoipFinishMessage vfm = (VoipFinishMessage)mc;
            if(!TextUtils.isEmpty(vfm.getToId())) {
                Message message = RCloudContext.getInstance().getRongIMClient().sendMessage(ConversationType.PRIVATE, vfm.getToId(), vfm, new MySendMessageCallback());
            }
		}else if(mc instanceof TextMessage){
			TextMessage tm = (TextMessage)mc;
			String targetid = intent.getCharSequenceExtra("targetid").toString();
			Message message = RCloudContext.getInstance().getRongIMClient().sendMessage(ConversationType.PRIVATE, targetid, tm, new MySendMessageCallback());
		}
	}
	
	class MySendMessageCallback implements SendMessageCallback {

		@Override
		public void onSuccess() {
			Log.d("SendMessageBroadcast", "=================onSucces====================");

		}

		@Override
		public void onProgress(int progress) {
			Log.d("SendMessageBroadcast", "===============onProgress==================");
		}

		@Override
		public void onError(ErrorCode errorCode) {
			Log.d("SendMessageBroadcast", "================onError=============="+errorCode);
		}

	}
	
}
