package io.rong.voipkit.message;

import org.json.JSONException;
import org.json.JSONObject;

import io.rong.imlib.MessageTag;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

//@MessageTag(value = "RC:VoipCallMsg", flag = MessageTag.ISCOUNTED
//		| MessageTag.ISPERSISTED)
@MessageTag(value = "RC:VoipCallMsg", flag = 0)
public class VoipCallMessage extends io.rong.imlib.RongIMClient.MessageContent{
	
	private String sessionId;
	private String ip;
	private int remoteTransferPort;
	private int remoteControlPort;
	private String toId;
	private String toUserName;
	private String fromId;
	private String fromUserName;

	
	@Override
	public byte[] encode() {
		JSONObject jsonObj = new JSONObject();

		try {
			jsonObj.put("sessionId", sessionId);
			jsonObj.put("ip", ip);
			jsonObj.put("remoteTransferPort", remoteTransferPort);
			jsonObj.put("remoteControlPort", remoteControlPort);
			jsonObj.put("toId", toId);
			jsonObj.put("toUserName", toUserName);
			jsonObj.put("fromId", fromId);
			jsonObj.put("fromUserName", fromUserName);
		} catch (JSONException e) {
			Log.d("JSONException", e.getMessage());
		}

		return jsonObj.toString().getBytes();
	}

	public VoipCallMessage(byte[] data) {
		String jsonStr = new String(data);

		try {
			JSONObject jsonObj = new JSONObject(jsonStr);
			setSessionId(jsonObj.getString("sessionId"));
			setIp(jsonObj.getString("ip"));
			setRemoteTransferPort(jsonObj.getInt("remoteTransferPort"));
			setRemoteControlPort(jsonObj.getInt("remoteControlPort"));
			
			setToId(jsonObj.getString("toId"));
			setToUserName(jsonObj.getString("toUserName"));
			setFromId(jsonObj.getString("fromId"));
			setFromUserName(jsonObj.getString("fromUserName"));

		} catch (JSONException e) {
			Log.d("JSONException", e.getMessage());
		}
	}
	
	public VoipCallMessage(){
	}
	
	public VoipCallMessage(String sessionId, String ip, int remoteTransferPort,int remoteControlPort,String toId,
			String toUserName, String fromId, String fromUserName) {
		super();
		this.sessionId = sessionId;
		this.ip = ip;
		this.remoteTransferPort = remoteTransferPort;
		this.remoteControlPort = remoteControlPort;
		this.toId = toId;
		this.toUserName = toUserName;
		this.fromId = fromId;
		this.fromUserName = fromUserName;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	
	public int getRemoteTransferPort() {
		return remoteTransferPort;
	}

	public void setRemoteTransferPort(int remoteTransferPort) {
		this.remoteTransferPort = remoteTransferPort;
	}

	public int getRemoteControlPort() {
		return remoteControlPort;
	}

	public void setRemoteControlPort(int remoteControlPort) {
		this.remoteControlPort = remoteControlPort;
	}

	public String getToId() {
		return toId;
	}

	public void setToId(String toId) {
		this.toId = toId;
	}

	public String getToUserName() {
		return toUserName;
	}

	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}

	public String getFromId() {
		return fromId;
	}

	public void setFromId(String fromId) {
		this.fromId = fromId;
	}

	public String getFromUserName() {
		return fromUserName;
	}

	public void setFromUserName(String fromUserName) {
		this.fromUserName = fromUserName;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeString(sessionId);
		dest.writeString(ip);
		dest.writeInt(remoteTransferPort);
		dest.writeInt(remoteControlPort);
		dest.writeString(toId);
		dest.writeString(toUserName);
		dest.writeString(fromId);
		dest.writeString(fromUserName);
	}

	public static final Parcelable.Creator<VoipCallMessage> CREATOR = new Parcelable.Creator<VoipCallMessage>() {
		@SuppressWarnings("unchecked")
		@Override
		public VoipCallMessage createFromParcel(Parcel source) {
			VoipCallMessage c = new VoipCallMessage(source.readString(),source.readString(),source.readInt(),source.readInt(),source.readString(),source.readString(),source.readString(),source.readString());
			return c;
		}
		@Override//创建一个类型为T，长度为size的数组。
		public VoipCallMessage[] newArray(int size) {
			return new VoipCallMessage[size];
		}
	};
}