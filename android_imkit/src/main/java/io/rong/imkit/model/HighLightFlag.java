package io.rong.imkit.model;

import io.rong.imkit.utils.ParcelUtils;
import android.os.Parcel;
import android.os.Parcelable;


public class HighLightFlag implements Parcelable
{

	/**
	 * @return the title
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	private int start, end;
	private String url;
	private String title;

	public HighLightFlag()
	{
	}

	public int getStart()
	{
		return start;
	}

	public HighLightFlag(int startIndex, int endIndex, String url, String title)
	{
		this.start = startIndex;
		this.end = endIndex;
		this.url = url;
		this.title = title;
	}

	public void setStart(int start)
	{
		this.start = start;
	}

	public int getEnd()
	{
		return end;
	}

	public void setEnd(int end)
	{
		this.end = end;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeInt(start);
		dest.writeInt(end);
		ParcelUtils.writeStringToParcel(dest, url);
		ParcelUtils.writeStringToParcel(dest, title);
	}

	public HighLightFlag(Parcel in)
	{
		start = in.readInt();
		end = in.readInt();
		url = ParcelUtils.readStringFromParcel(in);
		title = ParcelUtils.readStringFromParcel(in);
	}

	public static final Parcelable.Creator<HighLightFlag> CREATOR = new Parcelable.Creator<HighLightFlag>()
	{
		public HighLightFlag createFromParcel(Parcel in)
		{
			return new HighLightFlag(in);
		}

		@Override
		public HighLightFlag[] newArray(int size)
		{
			return new HighLightFlag[size];
		}
	};

}
