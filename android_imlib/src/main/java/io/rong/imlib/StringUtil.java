package io.rong.imlib;

import java.util.Iterator;

class StringUtil {
	public static String join(Iterator<?> iterator, String separator) {
		// handle null, zero and one elements before building a buffer
		if (iterator == null) {
			return null;
		}

		if (!iterator.hasNext()) {
			return "";
		}

		String first = String.valueOf(iterator.next());

		if (!iterator.hasNext()) {
			return first;
		}
		// two or more elements
		StringBuilder buf = new StringBuilder();
		if (first != null) {
			buf.append(first);
		}

		while (iterator.hasNext()) {
			if (separator != null) {
				buf.append(separator);
			}

			String obj = String.valueOf(iterator.next());
			if (obj != null) {
				buf.append(obj);
			}
		}

		return buf.toString();
	}

	public static boolean isEmpty(String str) {
		if (str == null || str.length() == 0 || str.trim().length() == 0) {
			return true;
		} else {
			return false;
		}
	}

	public static String ClearSymbols(String str) {
		return "";
	}
}
