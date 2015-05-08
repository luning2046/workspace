package io.rong.voiplib.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

@SuppressWarnings("rawtypes")
public class ResourceUtils {

	// private static final String DEFAULT_PACKAGE = "cn.rongcloud.imkit";
	private static final String DEFAULT_TYPE_ID = "id";
	private static final String DEFAULT_TYPE_LAYOUT = "layout";

	/**
	 * ID
	 * 
	 * @param context
	 * @param resName
	 * @return
	 */
	public static int getIDResourceId(Context context, String resName) {

		return getResourceId(context, resName, DEFAULT_TYPE_ID, context.getPackageName());
	}

	/**
	 * layout
	 * 
	 * @param context
	 * @param resName
	 * @return
	 */
	public static int getLayoutResourceId(Context context, String resName) {

		return getResourceId(context, resName, DEFAULT_TYPE_LAYOUT, context.getPackageName());
	}

	private static int getResourceId(Context context, String resName, String defType, String defPackage) {

		int resId = 0;

		String packageName = context.getPackageName();
		Class r = null;
		try {
			r = Class.forName(packageName + ".R");
			// Log.d("class", r.getName());

			Class[] classes = r.getDeclaredClasses();// .getClasses();
			Class desireClass = null;

			for (int i = 0; i < classes.length; i++) {
				// Log.d("desireClass", classes[i].getName());
				if (classes[i].getName().split("\\$")[1].equals(defType)) {
					desireClass = classes[i];
					// Log.d("desireClass -----", desireClass.getName());
					break;
				}
			}

			// Field[] fields = desireClass.getDeclaredFields();

			// for (int i = 0; i < fields.length; i++) {
			// Log.d("desireClass--fields", fields[i].getName());
			// }

			if (desireClass != null)
				resId = desireClass.getDeclaredField(resName).getInt(null);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}

		// Log.d("getLayoutResourceId", "R." + defType + "." + resName +
		// " value:" + resId + " defPackage:" + defPackage);

		return resId;
	}

	private static int[] getResourceIds(Context context, String resName, String defType, String defPackage) {

		int[] resId = null;

		String packageName = context.getPackageName();
		Class<?> r = null;
		try {
			r = Class.forName(packageName + ".R");
			// Log.d("class", r.getName());

			Class[] classes = r.getDeclaredClasses();// .getClasses();
			Class desireClass = null;

			for (int i = 0; i < classes.length; i++) {
				// Log.d("desireClass", classes[i].getName());
				if (classes[i].getName().split("\\$")[1].equals(defType)) {
					desireClass = classes[i];
					// Log.d("desireClass -----", desireClass.getName());
					break;
				}
			}

			// Field[] fields = desireClass.getDeclaredFields();

			// for (int i = 0; i < fields.length; i++) {
			// Log.d("desireClass--fields", fields[i].getName());
			// }

			// if (desireClass != null)
			// resId = desireClass.getDeclaredField(resName).getInt(null);

			if ((desireClass != null) && (desireClass.getDeclaredField(resName).get(desireClass) != null)
					&& (desireClass.getDeclaredField(resName).get(desireClass).getClass().isArray()))

				resId = (int[]) desireClass.getDeclaredField(resName).get(desireClass);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}

		// if (resId != null) {
		// for (int i = 0; i < resId.length; i++) {
		// Log.d("getLayoutResourceId", "R." + defType + "." + resName +
		// " values:" + resId[i] + " defPackage:" + defPackage);
		// }
		// }

		return resId;
	}

	/**
	 * string
	 * 
	 * @param context
	 * @param resName
	 * @return
	 */
	public static int getStringResourceId(Context context, String resName) {

		return getResourceId(context, resName, "string", context.getPackageName());
	}

	public static String getStringResource(Context context, String resName) {
		int resId = getResourceId(context, resName, "string", context.getPackageName());
		return context.getResources().getString(resId);
	}

	/**
	 * color
	 * 
	 * @param context
	 * @param resName
	 * @return
	 */
	public static int getColorResourceId(Context context, String resName) {

		return getResourceId(context, resName, "color", context.getPackageName());
	}

	public static int getColorByResId(Context context, String resName) {
		return context.getResources().getColor(getColorResourceId(context, resName));
	}

	/**
	 * drawable
	 * 
	 * @param context
	 * @param resName
	 * @return
	 */
	public static int getDrawableResourceId(Context context, String resName) {

		return getResourceId(context, resName, "drawable", context.getPackageName());
	}

	public static Drawable getDrawableById(Context context, String resName) {
		return context.getResources().getDrawable(getDrawableResourceId(context, resName));
	}

	/**
	 * array
	 * 
	 * @param context
	 * @param resName
	 * @return
	 */
	public static int getArrayResourceId(Context context, String resName) {

		return getResourceId(context, resName, "array", context.getPackageName());
	}

	public static String[] getArrayById(Context context, String resName) {
		int resId = getArrayResourceId(context, resName);
		return context.getResources().getStringArray(resId);
	}

	/**
	 * attr
	 * 
	 * @param context
	 * @param resName
	 * @return
	 */
	public static int getAttrResourceId(Context context, String resName) {

		return getResourceId(context, resName, "attr", context.getPackageName());
	}

	/**
	 * styleables
	 * 
	 * @param context
	 * @param resName
	 * @return
	 */
	public static int[] getStyleablesResourceId(Context context, String resName) {

		return getResourceIds(context, resName, "styleable", context.getPackageName());
	}

	/**
	 * styleable
	 * 
	 * @param context
	 * @param resName
	 * @return
	 */
	public static int getStyleableResourceId(Context context, String resName) {

		return getResourceId(context, resName, "styleable", context.getPackageName());
	}

	/**
	 * 
	 * @param context
	 * @param resName
	 * @return
	 */
	public static int getStyleResourceId(Context context, String resName) {

		return getResourceId(context, resName, "style", context.getPackageName());
	}

	/**
	 * Integer
	 * 
	 * @param context
	 * @param resName
	 * @return
	 */
	public static int getIntegerResourceId(Context context, String resName) {

		return getResourceId(context, resName, "integer", context.getPackageName());
	}

	/**
	 * Dimen
	 * 
	 * @param context
	 * @param resName
	 * @return
	 */
	public static int getDimenResourceId(Context context, String resName) {

		return getResourceId(context, resName, "dimen", context.getPackageName());
	}

	/**
	 * Bool
	 * 
	 * @param context
	 * @param resName
	 * @return
	 */
	public static int getBoolResourceId(Context context, String resName) {

		return getResourceId(context, resName, "bool", context.getPackageName());
	}

	/**
	 * 
	 * @param context
	 * @param resName
	 * @return
	 */
	public static int getAnimResourceId(Context context, String resName) {

		return getResourceId(context, resName, "anim", context.getPackageName());
	}

}
