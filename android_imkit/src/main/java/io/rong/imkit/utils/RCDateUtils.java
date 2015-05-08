package io.rong.imkit.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.util.Log;

public class RCDateUtils {

	private static final int OTHER = 2014;
	private static final int TODAY = 6;
	private static final int YESTERDAY = 15;

	public static int judgeDate(Date date) {
		// 今天
		Calendar calendarToday = Calendar.getInstance();
		calendarToday.set(Calendar.HOUR, 0);
		calendarToday.set(Calendar.MINUTE, 0);
		calendarToday.set(Calendar.SECOND, 0);
		calendarToday.set(Calendar.MILLISECOND, 0);
		// 昨天
		Calendar calendarYesterday = Calendar.getInstance();
		calendarYesterday.add(Calendar.DATE, -1);
		calendarYesterday.set(Calendar.HOUR, 0);
		calendarYesterday.set(Calendar.MINUTE, 0);
		calendarYesterday.set(Calendar.SECOND, 0);
		calendarYesterday.set(Calendar.MILLISECOND, 0);

		// 目标日期
		Calendar calendarTarget = Calendar.getInstance();
		calendarTarget.setTime(date);

		if (calendarTarget.before(calendarYesterday)) {// 是否在calendarT之前
			return OTHER;
		} else if (calendarTarget.before(calendarToday)) {
			return YESTERDAY;
		} else {
			return TODAY;
		}

	}

	public static String getConvastionListFromatDate(Date date) {

		if (date == null)
			return "";

		String fromatedDate = null;

		int type = judgeDate(date);

		switch (type) {
		case TODAY:
			fromatedDate = fromatDate(date, "HH:mm");
			break;

		case YESTERDAY:
			fromatedDate = String.format("昨天 %1$s", fromatDate(date, "HH:mm"));
			break;

		case OTHER:
			fromatedDate = fromatDate(date, "yyyy-MM-dd");
			break;

		default:
			break;
		}

		return fromatedDate;

	}

	public static String getConvastionFromatDate(Date date) {

		if (date == null)
			return "";

		String fromatedDate = null;

		int type = judgeDate(date);

		switch (type) {
		case TODAY:
			fromatedDate = fromatDate(date, "HH:mm");
			break;

		case YESTERDAY:
			fromatedDate = String.format("昨天 %1$s", fromatDate(date, "HH:mm"));
			break;

		case OTHER:
			fromatedDate = fromatDate(date, "yyyy-MM-dd HH:mm");
			break;

		default:
			break;
		}

		return fromatedDate;

	}

	public static boolean isShowChatTime(long currentTime, long preTime) {

		int typeCurrent = judgeDate(new Date(currentTime));
		int typePre = judgeDate(new Date(preTime));

		Log.d("RCDateUtils", "currentTime:" + currentTime);
		Log.d("RCDateUtils", "preTime:" + preTime);

		Log.d("RCDateUtils", "typeCurrent----:" + typeCurrent + "||||typePre:" + typePre);
		Log.d("RCDateUtils", "currentTime-preTime=" + (currentTime - preTime));

		if (typeCurrent == typePre) {

			if ((currentTime - preTime) > 60 * 1000) {
				return true;
			} else {
				return false;
			}

		} else {
			return true;
		}

		// return typeCurrent == typePre ? (((currentTime - preTime) > 60 *
		// 1000) ? true : false) : true;

	}

	public static String fromatDate(Date date, String fromat) {
		SimpleDateFormat sdf = new SimpleDateFormat(fromat);
		return sdf.format(date);
	}

}
