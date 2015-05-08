package io.rong.imkit.utils;

import io.rong.imkit.RCloudContext;
import io.rong.imkit.common.RCloudConst;
import io.rong.imkit.model.HighLightFlag;
import io.rong.imkit.model.HightLightContent;
import io.rong.imkit.veiw.MentionsIntentSpan;
import io.rong.imkit.veiw.gif.EmotionParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Vibrator;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.Base64;
import android.util.Log;

@SuppressLint("SimpleDateFormat")
public class Util {

	private static final String TAG = "Util";
	private static final char SPLIT = ',';

	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public static Location getLat(Context context, LocationListener listener) {
		LocationManager locMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		Location location = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (location == null) {
			location = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		return location;
	}

	public static SpannableStringBuilder highLight(HightLightContent content) {
		if (content == null || content.getContent() == null || content.getContent().length() == 0)
			return new SpannableStringBuilder();

		SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(content.getContent());
		if (content.getFlags() != null) {
			for (HighLightFlag item : content.getFlags()) {
				if (item.getTitle().startsWith("[")) {
					Drawable drawable = EmotionParser.getInstance(RCloudContext.getInstance().getContext()).getSmileDrawable(item.getTitle());
					if (drawable != null) {
						if (HightLightContent.EMOTION_FLAG == content.getTypeFlag()) {
							spannableStringBuilder.setSpan(new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM), item.getStart(), item.getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						} else {
							spannableStringBuilder.setSpan(new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE), item.getStart(), item.getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						}
					}
				} else {
					spannableStringBuilder.setSpan(new ForegroundColorSpan(0xFFee3c3e), item.getStart(), item.getEnd(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				}
			}
		}

		return spannableStringBuilder;
	}

	public static SpannableStringBuilder highLightLink(HightLightContent content) {
		if (content == null || content.getContent() == null || content.getContent().length() == 0)
			return new SpannableStringBuilder();

		SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(content.getContent());

		if (content.getFlags() != null) {
			for (HighLightFlag item : content.getFlags()) {
				if (item.getTitle().startsWith("@")) {
					spannableStringBuilder.setSpan(new MentionsIntentSpan(item.getUrl().replace(RCloudConst.API.HOST, "").trim()), item.getStart(), item.getEnd(),
							Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				} else if (item.getTitle().startsWith("#")) {

					String url = getUrlWithAuth(item.getUrl(), RCloudContext.getInstance().getContext());

					if (TextUtils.isEmpty(url))
						spannableStringBuilder.setSpan(new ForegroundColorSpan(0xFFee3c3e), item.getStart(), item.getEnd(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
					else
						spannableStringBuilder.setSpan(new URLSpan(url), item.getStart(), item.getEnd(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

				} else if (item.getUrl().startsWith("http://")) {

					String url = getUrlWithAuth(item.getUrl(), RCloudContext.getInstance().getContext());

					if (TextUtils.isEmpty(url))
						spannableStringBuilder.setSpan(new ForegroundColorSpan(0xFFee3c3e), item.getStart(), item.getEnd(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
					else
						spannableStringBuilder.setSpan(new URLSpan(url), item.getStart(), item.getEnd(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

				}
				if (item.getTitle().startsWith("[")) {
					Drawable drawable = EmotionParser.getInstance(RCloudContext.getInstance().getContext()).getSmileDrawable(item.getTitle());
					if (drawable != null) {
						if (HightLightContent.EMOTION_FLAG == content.getTypeFlag()) {
							spannableStringBuilder.setSpan(new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE), item.getStart(), item.getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						} else {
							spannableStringBuilder.setSpan(new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM), item.getStart(), item.getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						}
					}
				} else {
					spannableStringBuilder.setSpan(new ForegroundColorSpan(0xFFee3c3e), item.getStart(), item.getEnd(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				}

			}
		}
		return spannableStringBuilder;

	}

	public static String getUrlWithAuth(String url, Context context) {

		if (url == null || url.length() == 0) {
			return null;
		}

		URI uri = URI.create(url);

		if (uri != null && uri.getHost() != null) {

			// for (String item : Const.API.EXTENSIONS) {
			// if (item.equalsIgnoreCase(uri.getHost())) {
			// if (uri.getQuery() == null) {
			// url = String.format("%1$s?c=%2$s", url,
			// context.getLogicManager().getCkeyLogic().getM161C());
			// break;
			// } else if (uri.getQuery().length() == 0) {
			// url = String.format("%1$sc=%2$s", url,
			// context.getLogicManager().getCkeyLogic().getM161C());
			// break;
			// } else {
			// url = String.format("%1$s&c=%2$s", url,
			// context.getLogicManager().getCkeyLogic().getM161C());
			// break;
			// }
			// }
			// }
		} else {
			return null;
		}

		return url;
	}

	public static boolean isGIFImage(String filePath) {

		byte[] imageHearByte = new byte[28];

		InputStream inputStream = null;

		try {
			inputStream = new FileInputStream(filePath);
			inputStream.read(imageHearByte, 0, 28);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		String picType = bytesToHexString(imageHearByte);

		if (picType.startsWith("47494638"))
			return true;

		return false;
	}


	public static boolean isGIFImage(InputStream inputStream) {
		if (inputStream == null)
			return false;

		byte[] imageHearByte = new byte[28];
		try {
			inputStream.read(imageHearByte, 0, 28);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		String picType = bytesToHexString(imageHearByte);

		if (picType.startsWith("47494638"))
			return true;

		return false;
	}

	public static boolean isGIFImage(byte[] imgData) {

		if (imgData == null || imgData.length == 0) {
			return false;
		}

		byte[] imageHearByte = new byte[28];

		System.arraycopy(imgData, 0, imageHearByte, 0, 28);

		String picType = bytesToHexString(imageHearByte);

		if (picType.startsWith("47494638"))
			return true;

		return false;
	}

	public static String bytesToHexString(byte[] bytes) {

		if (bytes == null || bytes.length <= 0) {
			return null;
		}

		StringBuilder stringBuilder = new StringBuilder();

		for (int i = 0; i < bytes.length; i++) {
			int intValue = bytes[i] & 0xFF;
			String hexValue = Integer.toHexString(intValue);

			if (hexValue.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hexValue);
		}

		return stringBuilder.toString().toUpperCase();
	}

	public static void shake(long milliseconds, Context context) {
		Vibrator vVi = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
		vVi.vibrate(milliseconds);
		vVi.vibrate(new long[] { 200, 200, 1000 }, -1);
	}

	public static int calculateLength(String arg) {

		if (arg == null || "".equals(arg))
			return 0;

		int count = 0;

		for (int i = 0; i < arg.length(); i++) {
			if (isChinese(arg.charAt(i))) {
				count = count + 2;
			} else {
				count++;
			}
		}

		return count;
	}

	/**
	 * 当名字超过20个字符时，只取20个字符
	 * 
	 * @param arg
	 * @return
	 */
	public static String getTwentyCharNickName(String arg) {
		if (!TextUtils.isEmpty(arg) && calculateLength(arg) > 20) {
			int length = 0;
			for (int i = 0; i < arg.length(); i++) {
				if (isChinese(arg.charAt(i))) {
					length = length + 2;
				} else {
					length++;
				}
				if (length == 20) {
					return arg.substring(0, i + 1);
				} else if (length >= 21) {
					return arg.substring(0, i);
				}
			}
		}
		return arg;
	}

	/**
	 * 根据Unicode编码完美的判断中文汉字和符号
	 * 
	 * @param c
	 * @return
	 */
	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
			return true;
		}
		return false;
	}

	public static boolean isChinese(String arg) {

		Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5]");
		Matcher matcher = pattern.matcher(arg);

		return matcher.find();
	}

	/**
	 * 获取网络连接类型. 返回-1代表无网络连接
	 */
	public static int getNetWorkType(Context context) {
		try {
			ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connManager != null) {
				NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
				if (networkInfo != null)
					return networkInfo.getType();
			}
		} catch (Exception e) {
			// if (DEBUG)
			// Log.d(TAG, "NetWorkType" + e.toString());
		}

		return -1;
	}

	/**
	 * 保存下拉刷新更新时间
	 * 
	 * @param context
	 * @param spName
	 */
	// public static String updateTimeLable(Context context, String spName) {
	// if (context == null)
	// return "";
	//
	// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	// String curTime = context.getString(R.string.pull_to_refresh_lasttime,
	// sdf.format(new Date()));
	// context.getSharedPreferences(spName, 0).edit().putString(spName,
	// curTime).commit();
	// return curTime;
	// }

	public static void exitApp() {
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	/**
	 * DES 加密
	 * 
	 * @param datasource
	 *            加密数据
	 * @param key
	 *            key串
	 * @return
	 */
	public static byte[] desEncrypt(byte[] datasource, String key) {
		try {
			SecureRandom random = new SecureRandom();
			DESKeySpec desKey = new DESKeySpec(key.getBytes());
			// 创建一个密匙工厂，然后用它把DESKeySpec转换成SecretKey
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey securekey = keyFactory.generateSecret(desKey);
			// Cipher对象实际完成加密操作
			Cipher cipher = Cipher.getInstance("DES");
			// 用密匙初始化Cipher对象
			cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
			// 获取数据并加密
			return Base64.encode(cipher.doFinal(datasource), 0);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * DES 解密
	 * 
	 * @param
	 * @param key
	 *            key串
	 * @return
	 * @throws Exception
	 */
	public static byte[] desDecrypt(byte[] datasource, String key) {
		try {
			byte[] base64Data = Base64.decode(datasource, 0);
			// DES算法要求有一个可信任的随机数源
			SecureRandom random = new SecureRandom();
			// 创建一个DESKeySpec对象
			DESKeySpec desKey = new DESKeySpec(key.getBytes());
			// 创建一个密匙工厂
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			// 将DESKeySpec对象转换成SecretKey对象
			SecretKey securekey = keyFactory.generateSecret(desKey);
			// Cipher对象实际完成解密操作
			Cipher cipher = Cipher.getInstance("DES");
			// 用密匙初始化Cipher对象解密模式
			cipher.init(Cipher.DECRYPT_MODE, securekey, random);
			// 真正开始解密操作
			return cipher.doFinal(base64Data);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 集合转字符串 *
	 */
	public static <T> String listToString(List<T> list, char split) {
		StringBuilder buffer = new StringBuilder();
		int size = list.size();
		for (int i = 0; i < size; i++) {
			buffer.append(list.get(i));
			buffer.append(split);
		}
		buffer.deleteCharAt(buffer.length() - 1);
		return buffer.toString();
	}

	public static <T> String listToString(List<T> list) {
		return listToString(list, SPLIT);
	}

	/**
	 * 获取字符串的长度，如果有中文，则每个中文字符计为2位
	 * 
	 * @param value
	 *            指定的字符串
	 * @return 字符串的长度
	 */
	public static int strLength(String value) {
		if (TextUtils.isEmpty(value)) {
			return 0;
		}
		int valueLength = 0;
		String chinese = "[\u0391-\uFFE5]";
		/* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */
		for (int i = 0; i < value.length(); i++) {
			/* 获取一个字符 */
			String temp = value.substring(i, i + 1);
			/* 判断是否为中文字符 */
			if (temp.matches(chinese)) {
				/* 中文字符长度为2 */
				valueLength += 2;
			} else {
				/* 其他字符长度为1 */
				valueLength += 1;
			}
		}
		return valueLength;
	}

	// public static Photo createPhoto(ArrayList<Photo> photos) {
	//
	// if (photos == null || photos.size() == 0)
	// return null;
	//
	// Photo photo = null;
	// Photo tempPhoto = null;
	//
	// for (Photo photoArg : photos) {
	//
	// if (photo == null) {
	// photo = photoArg;
	// tempPhoto = photo;
	// } else {
	// tempPhoto.setNextModel(photoArg);
	// }
	// tempPhoto = photoArg;
	// }
	//
	// return photo;
	// }

	public static String getCrrentDateTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(new Date());
	}

	public static String getImageKeyFileName(String imageKey) {
		String fileName = null;

		if (!TextUtils.isEmpty(imageKey) && imageKey.lastIndexOf("/") != -1) {
			fileName = imageKey.substring(imageKey.lastIndexOf("/") + 1);
		}
		return fileName;
	}

	public static void saveDataToDir(File file, byte[] data) throws IOException {

		FileOutputStream stream = new FileOutputStream(file);

		stream.write(data);
		stream.flush();
		stream.close();

	}

	public static byte[] getByteFromUri(Context context, Uri uri) {
		InputStream input = null;

		try {
			input = context.getContentResolver().openInputStream(uri);

			int count = 0;
			while (count == 0) {
				count = input.available();
			}

			byte[] bytes = new byte[count];
			input.read(bytes);

			return bytes;
		} catch (Exception e) {
			// Log.d(DevConfig.MESSAGING, e.getMessage(), e);
			return null;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					// Log.d(DevConfig.MESSAGING, e.getMessage(), e);
				}
			}
		}
	}

	public static Bitmap getResizedBitmap(Context context, Uri uri, int quality, int widthLimit, int heightLimit) {

		Options opt = decodeBitmapOptionsInfo(context, uri);

		int outSize = opt.outWidth > opt.outHeight ? opt.outWidth : opt.outHeight;

		int s = 1;
		while ((outSize / s > widthLimit)) {
			s += 1;
		}

		Log.d(TAG, String.format("OUTPUT: x:%1$s  y:%2$s", String.valueOf(opt.outWidth / s), String.valueOf(opt.outHeight / s)));

		Options options = new BitmapFactory.Options();
		options.inSampleSize = s;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		options.inJustDecodeBounds = false;

		InputStream input = null;
		try {
			input = context.getContentResolver().openInputStream(uri);
			Bitmap b = BitmapFactory.decodeStream(input, null, options);
			String path = uri.getPath();
			if (!TextUtils.isEmpty(path)) {
				b = rotateBitMap(path, b);
			}
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			b.compress(CompressFormat.PNG, quality, os);
			return b;
		} catch (Exception e) {

			if (input == null) {
				input = FileUtil.getFileInputStream(uri.getPath());
			}

			Bitmap b = BitmapFactory.decodeStream(input, null, options);
			String path = uri.getPath();
			if (!TextUtils.isEmpty(path)) {
				b = rotateBitMap(path, b);
			}
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			b.compress(CompressFormat.PNG, quality, os);

			return b;

		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					// Log.d(DevConfig.MESSAGING, e.getMessage(), e);
				}
			}
		}
	}

	/*
	 * 压缩 图片
	 */
	public static byte[] getResizedImageData(Context context, Uri uri, int quality, int widthLimit, int heightLimit) {
		Options opt = decodeBitmapOptionsInfo(context, uri);
		int outSize = opt.outWidth > opt.outHeight ? opt.outWidth : opt.outHeight;

		int s = 1;
		while ((outSize / s > widthLimit)) {
			s += 1;
		}

		Log.d(TAG, String.format("OUTPUT: x:%1$s  y:%2$s", String.valueOf(opt.outWidth / s), String.valueOf(opt.outHeight / s)));

		Options options = new BitmapFactory.Options();
		options.inSampleSize = s;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		options.inJustDecodeBounds = false;

		InputStream input = null;
		try {
			input = context.getContentResolver().openInputStream(uri);
			Bitmap b = BitmapFactory.decodeStream(input, null, options);
			String path = uri.getPath();
			if (!TextUtils.isEmpty(path)) {
				b = rotateBitMap(path, b);
			}
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			b.compress(CompressFormat.PNG, 100, os);
			b.recycle();
			return os.toByteArray();
		} catch (FileNotFoundException e) {

			if (input == null) {
				input = FileUtil.getFileInputStream(uri.getPath());
			}

			Bitmap b = BitmapFactory.decodeStream(input, null, options);
			String path = uri.getPath();
			if (!TextUtils.isEmpty(path)) {
				b = rotateBitMap(path, b);
			}
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			b.compress(CompressFormat.PNG, 100, os);
			b.recycle();

			return os.toByteArray();

		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					// Log.d(DevConfig.MESSAGING, e.getMessage(), e);
				}
			}
		}
	}

	private static Options decodeBitmapOptionsInfo(Context context, Uri uri) {
		InputStream input = null;
		Options opt = new Options();
		try {
			input = context.getContentResolver().openInputStream(uri);
			opt.inJustDecodeBounds = true;
			opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
			BitmapFactory.decodeStream(input, null, opt);
			return opt;
		} catch (FileNotFoundException e) {

			if (input == null) {
				input = FileUtil.getFileInputStream(uri.getPath());
			}
			opt.inJustDecodeBounds = true;
			opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
			BitmapFactory.decodeStream(input, null, opt);
			return opt;
			// Log.d(DevConfig.MESSAGING,IOException caught while opening
			// stream",
			// e);
		} finally {
			if (null != input) {
				try {
					input.close();
				} catch (IOException e) {
					// Ignore
					// Log.d(DevConfig.MESSAGING,
					// "IOException caught while closing stream", e);
				}
			}
		}
	}

	public static Bitmap getRotateBitmap(float degrees, Bitmap bm) {
		int bmpW = bm.getWidth();
		int bmpH = bm.getHeight();

		Matrix mt = new Matrix();
		// 设置旋转角度
		// 如果是设置为0则表示不旋转
		// 设置的数是负数则向左转
		// 设置的数是正数则向右转
		mt.setRotate(degrees);
		return Bitmap.createBitmap(bm, 0, 0, bmpW, bmpH, mt, true);
	}

	/**
	 * 用于压缩时旋转图片
	 * 
	 * @param srcFilePath
	 * @param bitmap
	 * @return
	 * @throws IOException
	 * @throws OutOfMemoryError
	 */
	private static Bitmap rotateBitMap(String srcFilePath, Bitmap bitmap) {
		ExifInterface exif = null;

		try {
			exif = new ExifInterface(srcFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		float degree = 0F;

		if (exif != null) {
			switch (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90F;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180F;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270F;
				break;
			default:
				break;
			}
		}

		if (degree != 0F) {
			Matrix matrix = new Matrix();
			matrix.setRotate(degree, bitmap.getWidth(), bitmap.getHeight());
			Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			if (b2 != null && bitmap != b2) {
				bitmap.recycle();
				bitmap = b2;
			}
		}

		return bitmap;
	}

	public static Date strTODate(String dateStr) {
		if (dateStr == null || dateStr.length() == 0)
			return null;
		Date date = null;
		try {
			date = timeFormat.parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}

	public static String dateToStr(Date date) {
		if (date == null) {
			return null;
		}
		// SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return timeFormat.format(date);
	}

	public static int dipTopx(Context context, float dipValue) {

		final float scale = context.getResources().getDisplayMetrics().density;

		return (int) (dipValue * scale + 0.5f);
	}

	public static int pxTodip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

}
