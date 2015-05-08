package io.rong.voipkit.message;

import org.json.JSONException;
import org.json.JSONObject;

import io.rong.imlib.MessageTag;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

//@MessageTag(value = "RC:VoipCallMsg", flag = MessageTag.ISCOUNTED
//		| MessageTag.ISPERSISTED)
@MessageTag(value = "RC:VoipAcceptMsg", flag = 0)
public class VoipAcceptMessage extends io.rong.imlib.RongIMClient.MessageContent{
	
	private String toId;

	
	@Override
	public byte[] encode() {
		JSONObject jsonObj = new JSONObject();

		try {
			jsonObj.put("toId", toId);
		} catch (JSONException e) {
			Log.d("JSONException", e.getMessage());
		}

		return jsonObj.toString().getBytes();
	}

	public VoipAcceptMessage(byte[] data) {
		String jsonStr = new String(data);

		try {
			JSONObject jsonObj = new JSONObject(jsonStr);
			setToId(jsonObj.getString("toId"));
		} catch (JSONException e) {
			Log.d("JSONException", e.getMessage());
		}
	}
	
	public VoipAcceptMessage(){
	}
	
	public VoipAcceptMessage(String toId) {
		super();
		this.toId = toId;
	}

	public String getToId() {
		return toId;
	}

	public void setToId(String toId) {
		this.toId = toId;
	}


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeString(toId);
	}

	public static final Parcelable.Creator<VoipAcceptMessage> CREATOR = new Parcelable.Creator<VoipAcceptMessage>() {
		@SuppressWarnings("unchecked")
		@Override
		public VoipAcceptMessage createFromParcel(Parcel source) {
			VoipAcceptMessage c = new VoipAcceptMessage(source.readString());
			return c;
		}
		@Override//创建一个类型为T，长度为size的数组。
		public VoipAcceptMessage[] newArray(int size) {
			return new VoipAcceptMessage[size];
		}
	};
}