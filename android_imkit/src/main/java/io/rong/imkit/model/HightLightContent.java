package io.rong.imkit.model;

import io.rong.imkit.common.RCloudConst;
import io.rong.imkit.utils.ParcelUtils;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;



public class HightLightContent implements Parcelable
{
	/**
	 * 纯文字
	 */
	public final static byte NONE_FLAG = 0;
	/**
	 * 纯表情
	 */
	public final static byte EMOTION_FLAG = 1;
	/**
	 * 其他样式（连接、@好友等）
	 */
	public final static byte OTHER_FLAG = 2;
	/**
	 * 混合样式  表情和文字都存在
	 */
	public final static byte MIX_FLAG = 3;
	
	private String content;
	public List<HighLightFlag> flags;

	private HighLightFlag hlfFlag;
	
	private byte typeFlag;

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public List<HighLightFlag> getFlags()
	{
		return flags;
	}

	public void setFlags(List<HighLightFlag> flags)
	{
		this.flags = flags;
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	public byte getTypeFlag() {
		return typeFlag;
	}

	public void setTypeFlag(byte typeFlag) {
		switch(typeFlag) {
		case NONE_FLAG:
			this.typeFlag = NONE_FLAG;
			break;
		case EMOTION_FLAG:
			if (MIX_FLAG == this.typeFlag || OTHER_FLAG == this.typeFlag) {
				this.typeFlag = MIX_FLAG;
			} else {
				this.typeFlag = EMOTION_FLAG;
			}
			break;
		case OTHER_FLAG:
			if (MIX_FLAG == this.typeFlag || EMOTION_FLAG == this.typeFlag) {
				this.typeFlag = MIX_FLAG;
			} else {
				this.typeFlag = OTHER_FLAG;
			}
			break;
		case MIX_FLAG:
			this.typeFlag = MIX_FLAG;
			break;
		default:
			this.typeFlag = MIX_FLAG;
			break;
		}
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		ParcelUtils.writeStringToParcel(dest, content);
		dest.writeByte(typeFlag);

		if (this.flags != null)
		{
			dest.writeInt(this.flags.size());
			for (HighLightFlag flag : this.flags)
			{
				dest.writeParcelable(flag, flags);
			}
		}
		else
		{
			dest.writeInt(RCloudConst.Parcel.NON_SEPARATOR);
		}
	}

	public HightLightContent(Parcel in)
	{
		content = ParcelUtils.readStringFromParcel(in);
		typeFlag = in.readByte();
		
		int flag = in.readInt();
		this.flags = new ArrayList<HighLightFlag>();
		if (flag != RCloudConst.Parcel.NON_SEPARATOR)
		{
			for (int i = 0; i < flag; i++)
			{
				HighLightFlag highLightFlag = in.readParcelable(HighLightFlag.class.getClassLoader());
				this.flags.add(highLightFlag);
			}
		}
	}

	public HightLightContent()
	{
	}

	public static final Parcelable.Creator<HightLightContent> CREATOR = new Parcelable.Creator<HightLightContent>()
	{
		public HightLightContent createFromParcel(Parcel in)
		{
			return new HightLightContent(in);
		}

		@Override
		public HightLightContent[] newArray(int size)
		{
			return new HightLightContent[size];
		}
	};

	public HighLightFlag getHlfFlag()
	{
		return hlfFlag;
	}

	public void setHlfFlag(HighLightFlag hlfFlag)
	{
		this.hlfFlag = hlfFlag;
	}

}
