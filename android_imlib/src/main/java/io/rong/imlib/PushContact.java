package io.rong.imlib;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

class PushContact implements Parcelable{

	private String id;
	private String name;
	private List<PushMessage> messages = new ArrayList<PushMessage>();
	
	private int mSize;
	
	public PushContact(String id,String name){
		this.id = id;
		this.name = name;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeString(id);
		dest.writeString(name);
		
		mSize = messages.size();
		dest.writeInt(mSize);
		
//		Log.d("aaa","============messages.size==============="+messages.size());
		for(int i=0;i<mSize;i++){
			PushMessage message = messages.get(i);
//			Log.d("aaa","============writeToParcel==============="+message.getContent());
			dest.writeParcelable(message, flag);
		}
	}

	public static final Parcelable.Creator<PushContact> CREATOR = new Parcelable.Creator<PushContact>() {
		// 重写Creator
		@SuppressWarnings("unchecked")
		@Override//实现从source中创建出类的实例
		public PushContact createFromParcel(Parcel source) {
			PushContact c = new PushContact(source.readString(),source.readString());
			
			c.mSize = source.readInt();
			c.readFromParcel(source);
			
			return c;
		}
		@Override//创建一个类型为T，长度为size的数组。
		public PushContact[] newArray(int size) {
			return new PushContact[size];
		}
	};
	
	public void readFromParcel(Parcel source){
		Log.d("Person","============createFromParcel==============="+messages.size());
		for(int i=0;i<mSize;i++){
			PushMessage m = source.readParcelable(PushMessage.class.getClassLoader());
			this.messages.add(m);
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<PushMessage> getMessages() {
		return messages;
	}

	public void setMessages(List<PushMessage> messages) {
		this.messages = messages;
	}
	
}
