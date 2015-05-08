package io.rong.imlib;

import android.os.Parcel;
import android.os.Parcelable;

class PushMessage implements Parcelable{

	private String title;
	private String content;
	private String channelType;
	private String channelId;
	private String channelName;
	
	public PushMessage(String title,String content,String channelType,String channelId,String channelName){
		this.title = title;
		this.content = content;
		this.channelType = channelType;
		this.channelId = channelId;
		this.channelName = channelName;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flag) {
		parcel.writeString(title);
		parcel.writeString(content);
		parcel.writeString(channelType);
		parcel.writeString(channelId);
		parcel.writeString(channelName);
	}

	public static final Parcelable.Creator<PushMessage> CREATOR = new Parcelable.Creator<PushMessage>() {
		// 重写Creator
		@SuppressWarnings("unchecked")
		@Override//实现从source中创建出类的实例
		public PushMessage createFromParcel(Parcel source) {
			PushMessage c = new PushMessage(source.readString(),source.readString(),source.readString(),source.readString(),source.readString());
			return c;
		}
		@Override//创建一个类型为T，长度为size的数组。
		public PushMessage[] newArray(int size) {
			return new PushMessage[size];
		}
	};

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getChannelType() {
		return channelType;
	}

	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}
	
}
