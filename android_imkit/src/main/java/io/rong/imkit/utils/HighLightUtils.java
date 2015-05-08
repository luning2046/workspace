package io.rong.imkit.utils;

import io.rong.imkit.RCloudContext;
import io.rong.imkit.model.HighLightFlag;
import io.rong.imkit.model.HightLightContent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;

public class HighLightUtils {
	static HashMap<Integer, String> codeHashMap = null;

	public static HightLightContent loadHighLight(String str) {
		if (!TextUtils.isEmpty(str)) {
			str = replaceEmoji(str);
		}

		HightLightContent content = new HightLightContent();

		Pattern pattern = Pattern.compile("(<a.*?href=['|\"]([^'\"$'\"]*)['|\"].*?>(.*?)</a>)|(\\[/[^\\[\\]]+?\\])");
		Matcher matcher = pattern.matcher(str);

		if (matcher.find()) {
			StringBuilder stringBuilder = new StringBuilder();
			List<HighLightFlag> flags = new ArrayList<HighLightFlag>();
			int startFlag = 0;
			int endFlag = 0;
			int matcherStart = 0;
			int matcherEnd = 0;
			stringBuilder.append(str.substring(0, matcher.start(0)));

			boolean isMatcher = false;
			if (matcher.start(0) != 0) {
				content.setTypeFlag(HightLightContent.OTHER_FLAG);
			}

			do {
				String group = matcher.group(0);
				matcherEnd = matcher.end(0);
				matcherStart = matcher.start(0);

				startFlag = stringBuilder.length();
				if (group.startsWith("[")) {
					stringBuilder.append(matcher.group(4));
					endFlag = startFlag + matcher.end(4) - matcher.start(4);
					flags.add(new HighLightFlag(startFlag, endFlag, "", matcher.group(4)));
					content.setTypeFlag(HightLightContent.EMOTION_FLAG);
				} else if (group.startsWith("<")) {
					stringBuilder.append(matcher.group(3));
					endFlag = startFlag + matcher.end(3) - matcher.start(3);

					flags.add(new HighLightFlag(startFlag, endFlag, matcher.group(2), matcher.group(3)));
					content.setTypeFlag(HightLightContent.OTHER_FLAG);
				}

				isMatcher = matcher.find();

				if (isMatcher) {
					matcherStart = matcher.start(0);
					stringBuilder.append(str.substring(matcherEnd, matcherStart));
					if (matcherEnd != matcherStart) {
						content.setTypeFlag(HightLightContent.OTHER_FLAG);
					}
				} else {
					stringBuilder.append(str.substring(matcherEnd));
					if (matcherEnd != str.length()) {
						content.setTypeFlag(HightLightContent.OTHER_FLAG);
					}
				}
			} while (isMatcher);

			content.setContent(stringBuilder.toString());
			content.setFlags(flags);
		} else {
			content.setContent(str);
			content.setTypeFlag(HightLightContent.NONE_FLAG);
		}
		return content;
	}

	public static SpannableStringBuilder spannableSBInsert(SpannableStringBuilder ssb, int begin, String str) {
		if (str == null)
			return ssb;
		ssb.insert(begin, str);
		ssb.setSpan(new ForegroundColorSpan(0xFFee3c3e), begin, str.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		return ssb;
	}

	public static SpannableStringBuilder spannableSBInsert(SpannableStringBuilder ssb, int begin, String str, String uri) {
		if (str == null)
			return ssb;
		ssb.insert(begin, str);
		ssb.setSpan(new URLSpan(uri), begin, str.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		ssb.setSpan(new ForegroundColorSpan(0xFFee3c3e), begin, str.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		return ssb;
	}

	public static SpannableStringBuilder loadHighLight(String str, int begin, int end) {
		SpannableStringBuilder ssb = new SpannableStringBuilder(str);
		ssb.setSpan(new ForegroundColorSpan(0xFFee3c3e), begin, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		return ssb;
	}

	public static int[] toCodePointArray(String str) {

		char[] ach = str.toCharArray();
		int len = ach.length;
		int[] acp = new int[Character.codePointCount(ach, 0, len)];
		int j = 0;

		for (int i = 0, cp; i < len; i += Character.charCount(cp)) {
			cp = Character.codePointAt(ach, i);
			acp[j++] = cp;
		}

		return acp;
	}

	public static String replaceEmoji(String input) {

		initCodeHashMap();

		StringBuilder result = new StringBuilder();
		int[] codePoints = toCodePointArray(input);

		for (int i = 0; i < codePoints.length; i++) {

			if (codeHashMap.containsKey(codePoints[i])) {

				String value = codeHashMap.get(codePoints[i]);

				if (value != null) {
					result.append(value);
				}

				continue;
			}

			result.append(Character.toChars(codePoints[i]));

		}

		return result.toString();

	}

	static final void initCodeHashMap() {

		if (codeHashMap == null) {

			String intStringArray[] = RCloudContext.getInstance().getContext().getResources()
					.getStringArray(ResourceUtils.getArrayResourceId(RCloudContext.getInstance().getContext(), "rc_emoji_int"));

			String codeStringArray[] = RCloudContext.getInstance().getContext().getResources()
					.getStringArray(ResourceUtils.getArrayResourceId(RCloudContext.getInstance().getContext(), "rc_emoji_code"));

			int length = intStringArray.length;

			codeHashMap = new HashMap<Integer, String>(length + 6);

			for (int i = 0; i < length; i++) {
				codeHashMap.put(Integer.parseInt(intStringArray[i]), codeStringArray[i]);

			}
		}
	}
}
