package io.rong.imkit.model;

import io.rong.imkit.utils.ParcelUtils;
import io.rong.imlib.RongIMClient.UserInfo;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.sea_monster.core.resource.model.Resource;

public class UIUserInfo extends UserInfo implements Parcelable {

	private Resource portraitResource;

	public UIUserInfo() {

	}

	public UIUserInfo(Parcel in) {
		super(in);

		portraitResource = ParcelUtils.readFromParcel(in, Resource.class);
	}

	public UIUserInfo(UserInfo userInfo) {

		setName(userInfo.getName());
		setPortraitUri(userInfo.getPortraitUri());
		setUserId(userInfo.getUserId());

		if (!TextUtils.isEmpty(getPortraitUri())) {
			portraitResource = new Resource(getPortraitUri());
		}

	}

	public Resource getPortraitResource() {

		if (portraitResource == null) {
			if (!TextUtils.isEmpty(getPortraitUri())) {
				portraitResource = new Resource(getPortraitUri());
			}
		}

		return portraitResource;
	}

	public void setPortraitResource(Resource portraitResource) {
		this.portraitResource = portraitResource;
	}

	@Override
	public int describeContents() {
		return super.describeContents();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);

		ParcelUtils.writeToParcel(dest, portraitResource);
	}

}
