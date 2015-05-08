package io.rong.imkit.utils;


import android.os.Parcel;
import android.text.style.ForegroundColorSpan;

public class MentionsSpan extends ForegroundColorSpan {
    public MentionsSpan(int color) {
        super(color);
    }

    public MentionsSpan(Parcel src) {
        super(src);
    }

//	private Friend friend;
//
//
//	public MentionsSpan(int color,Friend friend) {
//		super(color);
//	this.friend = friend;
//	}
//
//	public Friend getFriend() {
//		return friend;
//	}
}