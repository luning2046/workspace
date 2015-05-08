package io.rong.imkit.veiw.gif;

import io.rong.imkit.utils.ResourceUtils;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

public class EmotionParser {

	/** png表情资源路径 */
	private final int[] defaultFrameResIds;
	/** 表情转义符 */
	private final String[] defaultCodeArray;

	private Context mContext;
	private static EmotionParser mInstance;
	private int size;

	private EmotionParser(Context context) {
		this.mContext = context;
//		TypedArray resEmotionIds = mContext.getResources().obtainTypedArray(ResourceUtils.getArrayResourceId(context, "expression_frame_array"));// PC动态表情关键帧真列表
//		defaultCodeArray = mContext.getResources().getStringArray(ResourceUtils.getArrayResourceId(context, "expression_code_array"));
		TypedArray resEmotionIds = mContext.getResources().obtainTypedArray(ResourceUtils.getArrayResourceId(context, "rc_emoji_array"));// PC动态表情关键帧真列表
		defaultCodeArray = mContext.getResources().getStringArray(ResourceUtils.getArrayResourceId(context, "rc_emoji_code"));
		float d = mContext.getResources().getDisplayMetrics().density;
		size = (int) (d * 20);
		int length = defaultCodeArray.length;
		defaultFrameResIds = new int[length];

		for (int i = 0; i < length; i++) {
			defaultFrameResIds[i] = resEmotionIds.getResourceId(i, 0);
		}
	}

	public static EmotionParser getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new EmotionParser(context);
		}
		return mInstance;
	}

	public Drawable getSmileDrawable(String smileCode) {
		Drawable drawable = null;
		int resId = getSmileResId(smileCode);
		if (resId != 0) {
			drawable = mContext.getResources().getDrawable(resId);
			// 图片大小要根据分辨率设置
			drawable.setBounds(0, 0, size, size);
		}
		return drawable;
	}

//	public Drawable getSmileEmojiDrawable(String smileCode) {
//		Drawable drawable = null;
//
//		if (!TextUtils.isEmpty(smileCode) && smileCode.indexOf("+") != -1) {
//			String[] smileCodes = smileCode.split("+");
//			
//
//			int resId = getSmileResId(smileCodes[0].toLowerCase() + smileCodes[1].toLowerCase());
//			if (resId != 0) {
//				drawable = mContext.getResources().getDrawable(resId);
//				// 图片大小要根据分辨率设置
//				drawable.setBounds(0, 0, size, size);
//			}
//		}
//
//		return drawable;
//	}

	private int getSmileResId(String smileCode) {
		int length = defaultCodeArray.length;
		for (int i = 0; i < length; i++) {
			if (smileCode.equals(defaultCodeArray[i])) {
				return defaultFrameResIds[i];
			}
		}
		return 0;
	}

	public String getSmileCode(int resId) {
		for (int i = 0; i < defaultFrameResIds.length; i++) {
			if (resId == defaultFrameResIds[i]) {
				return defaultCodeArray[i];
			}
		}
		return "";
	}

	public int[] getSmileResIds() {
		int[] res = new int[defaultFrameResIds.length];
		System.arraycopy(defaultFrameResIds, 0, res, 0, defaultFrameResIds.length);
		return res;
	}

}
