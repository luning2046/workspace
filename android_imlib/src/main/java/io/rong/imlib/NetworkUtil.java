package io.rong.imlib;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

/**
 * 网络相关功能的工具类。
 */
class NetworkUtil {

    /**
     * 获取设备唯一 Id。
     *
     * @param context 应用程序上下文。
     * @return 设备的唯一 Id。
     */
    public static String getDeviceId(Context context) {

        TelephonyManager mTelephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        String mDeviceId = mTelephonyManager.getDeviceId();

        if (TextUtils.isEmpty(mDeviceId)) {
            StringBuffer sb = new StringBuffer();
            sb.append("35").append(Build.BOARD.length() % 10).append(Build.BRAND.length() % 10).append(Build.CPU_ABI.length() % 10).append(Build.DEVICE.length() % 10).append(Build.DISPLAY.length() % 10).append(Build.HOST.length() % 10).append(Build.ID.length() % 10).append(Build.MANUFACTURER.length() % 10).append(Build.MODEL.length() % 10).append(Build.PRODUCT.length() % 10).append(Build.TAGS.length() % 10).append(Build.TYPE.length() % 10).append(Build.USER.length() % 10);
            mDeviceId=sb.toString();
        }

        return mDeviceId;
    }
}
