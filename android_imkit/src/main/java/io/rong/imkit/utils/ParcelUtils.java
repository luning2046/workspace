package io.rong.imkit.utils;

import io.rong.imkit.common.RCloudConst;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelUtils {

	public static void writeStringToParcel(Parcel out, String str) {
		if (str != null) {
			out.writeInt(RCloudConst.Parcel.EXIST_SEPARATOR);
			out.writeString(str);
		} else {
			out.writeInt(RCloudConst.Parcel.NON_SEPARATOR);
		}
	}

	public static String readStringFromParcel(Parcel in) {
		int flag = in.readInt();
		if (flag == RCloudConst.Parcel.EXIST_SEPARATOR) {
			return in.readString();
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Parcelable> T readFromParcel(Parcel in, Class<T> cls) {
		int flag = in.readInt();
		if (flag == RCloudConst.Parcel.EXIST_SEPARATOR) {
			return (T)in.readParcelable(cls.getClassLoader());
		} else {
			return null;
		}
	}

	public static <T extends Parcelable> void writeToParcel(Parcel out, T model) {
		if (model != null) {
			out.writeInt(RCloudConst.Parcel.EXIST_SEPARATOR);
			out.writeParcelable(model, 0);
		} else {
			out.writeInt(RCloudConst.Parcel.NON_SEPARATOR);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Parcelable> List<T> readListFromParcel(Parcel in, Class<T> cls) {
		int flag = in.readInt();
		if (flag == RCloudConst.Parcel.EXIST_SEPARATOR) {
			return (List<T>)in.readArrayList(cls.getClassLoader());
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Parcelable> void writeListToParcel(Parcel out, List<T> collection) {
		if (collection != null) {
			out.writeInt(RCloudConst.Parcel.EXIST_SEPARATOR);
			out.writeParcelableArray((T[]) collection.toArray(), 0);
		} else {
			out.writeInt(RCloudConst.Parcel.NON_SEPARATOR);
		}
	}

	public static void writeListStingToParcel(Parcel out, List<String> collection) {

		if (collection != null) {
			out.writeInt(RCloudConst.Parcel.EXIST_SEPARATOR);
			out.writeStringList(collection);
		} else {
			out.writeInt(RCloudConst.Parcel.NON_SEPARATOR);
		}

	}

	public static ArrayList<String> readListStingToParcel(Parcel in) {

		ArrayList<String> list = new ArrayList<String>();

		int flag = in.readInt();

		if (flag == RCloudConst.Parcel.EXIST_SEPARATOR) {
			in.readStringList(list);
		}

		return list;
	}

}
