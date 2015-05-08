package io.rong.imkit.utils;

import java.lang.Character.UnicodeBlock;

import android.text.TextUtils;

public class EmojiUtils {

	/**
	 * 
	 * @param source
	 * @return
	 */
	public static boolean isContainEmoji(String source) {

		if (TextUtils.isEmpty(source)) {
			return false;
		}

		int len = source.length();

		for (int i = 0; i < len; i++) {
			char emojiChar = source.charAt(i);

			if (isEmoji(emojiChar)) {
				return true;
			}
		}

		return false;

	}

	/**
	 * 
	 * @param emojiChar
	 * @return
	 */
	private static final boolean isEmoji(char emojiChar) {
		return (emojiChar == 0x0) || (emojiChar == 0x9) || (emojiChar == 0xA) || (emojiChar == 0xD) || ((emojiChar >= 0x20) && (emojiChar <= 0xD7FF))
				|| ((emojiChar >= 0xE000) && (emojiChar <= 0xFFFD)) || ((emojiChar >= 0x10000) && (emojiChar <= 0x10FFFF));
	}

	/**
	 * 
	 * @param source
	 * @return
	 */
	public static String filterEmoji(String source) {

		if (!isContainEmoji(source)) {
			return source;
		}

		StringBuffer sb = null;
		int len = source.length();

		for (int i = 0; i < len; i++) {
			char emojiChar = source.charAt(i);

			if (isEmoji(emojiChar)) {

				if (sb == null) {
					sb = new StringBuffer();
				}

				sb.append(emojiChar);

			}
		}

		if (sb == null) {
			return source;
		} else {
			return sb.toString();
		}

	}

	public static String utf8ToUnicode(String source) {
		int len = source.length();

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < len; i++) {
			char unicodeChar = source.charAt(i);

			UnicodeBlock ub = UnicodeBlock.of(unicodeChar);

			if (ub == UnicodeBlock.BASIC_LATIN) {
				sb.append(source);
			} else if (ub == UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {

				int j = (int) unicodeChar - 65248;
				sb.append((char) j);

			} else {
				int s = (int) unicodeChar;

				if (s < 0)
					s = s + 2 ^ 32;
				String hexS = Integer.toHexString(s);
				String unicode = "\\u" + hexS.toUpperCase();
				sb.append(unicode);
			}
		}
		return sb.toString();
	}

}
