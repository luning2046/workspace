package io.rong.imkit.veiw;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.widget.EditText;
//import cn.com.fetion.win.models.Friend;
//import cn.com.fetion.win.utils.MentionsSpan;



public class MentionsEditText extends EditText
{

//	private static final String MENTIONS_STYLE = "@%1$s ";
//	private List<MentionsSpan> mentionsSpans = new ArrayList<MentionsSpan>();

	public MentionsEditText(Context context)
	{
		super(context);
	}

	public MentionsEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs, android.R.attr.editTextStyle);
	}

	public MentionsEditText(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}
	
	private Object getNowSpan() {
		int flag = this.getSelectionEnd();
		Object[] spans = this.getEditableText().getSpans(0, flag, Object.class);
		if (spans != null && spans.length > 0) {
			Object span = spans[spans.length - 1];
			if (getEditableText().getSpanEnd(span) >= flag) {
				return span;
			}
		}
		return null;
	}

//
//	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
//        return new MentionsInputConnection(super.onCreateInputConnection(outAttrs), false);
//    }
//
//    public class MentionsInputConnection extends InputConnectionWrapper
//	{
//
//		public MentionsInputConnection(InputConnection target, boolean mutable)
//		{
//			super(target, mutable);
//		}
//
//		public boolean commitText(CharSequence text, int newCursorPosition)
//		{
//			Object mentionsSpan = getNowSpan();
//			if (mentionsSpan != null && mentionsSpan instanceof MentionsSpan)
//			{
//				MentionsEditText.this.getEditableText().removeSpan(mentionsSpan);
//				mentionsSpans.remove(mentionsSpan);
//				return true;
//			}
//			else
//			{
//				return super.commitText(text, newCursorPosition);
//			}
//		}
//	}

	public String getRealString()
	{
		String text = null;
		
//		if (mentionsSpans.size() == 0) {
			text = getEditableText().toString();
			text = text.replaceAll("\n", "");
			return text;
//		}
//        //下面暂时没有到
//		StringBuilder stringBuilder = new StringBuilder();
//
//		Editable editable = getEditableText();
//		MentionsSpan[] spans = editable.getSpans(0, editable.length(), MentionsSpan.class);
//
//		int index = 0;
//
//		Map<Integer, MentionsSpan> spanMap = new HashMap<Integer, MentionsSpan>(spans.length);
//
//		for (MentionsSpan mentionsSpan : spans)
//		{
//			spanMap.put(editable.getSpanStart(mentionsSpan), mentionsSpan);
//		}
//
//		Object[] keys = spanMap.keySet().toArray();
//
//		Arrays.sort(keys);
		
//		for (Object object : keys)
//		{
//			int start = (Integer) object;
//			stringBuilder.append(editable.subSequence(index, start));
//
//			stringBuilder.append("@" + spanMap.get(start).getFriend().getUserIdInt());
//			index = editable.getSpanEnd(spanMap.get(start));
//		}
//
//		stringBuilder.append(editable.subSequence(index, editable.length()));
//		text = stringBuilder.toString();
//		text = text.replaceAll("\n", "");
//		return text;
	}


	
//	public MentionsText getMentionsText(){
//		MentionsText mt = new MentionsText();
//
//		ArrayList<MentionsSpan> list = new ArrayList<MentionsSpan>();
//		list.addAll(mentionsSpans);
//		mt.setMentionsSpans(list);
//		mt.setText(getEditableText());
//
//		return mt;
//	}
//
//	public void setMentionsText(MentionsText mentionsText){
//		mentionsSpans.addAll(mentionsText.getMentionsSpans());
//		setText(mentionsText.getText());
//	}

	public void clearStr()
	{
//		mentionsSpans.clear();
		this.setText(null);
	}



	public int insertText(String text) {
//		Object nowSpan = getNowSpan();
//		if (nowSpan != null && nowSpan instanceof MentionsSpan) {
//			this.setSelection(this.getEditableText().getSpanEnd(nowSpan));
//		}
		int index = getSelectionEnd();
		if (index < 0 || index >= getEditableText().length()) {
			this.append(text);
		} else {
			getEditableText().insert(index, text);
		}
		return index;
	}
	
	public void insertImage(String text, Drawable drawable) {
		int start = insertText(text);
		if (drawable != null) {
			getEditableText().setSpan(new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM), start, start + text.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}
	
	public void deleteChar() {
		if (getEditableText().length() > 0) {
			Object nowSpan = getNowSpan();
			if (nowSpan != null) {
//				if (nowSpan instanceof MentionsSpan) {
//					getEditableText().removeSpan(nowSpan);
//					mentionsSpans.remove(nowSpan);
//				} else
                if (nowSpan instanceof ImageSpan) {
					int start = getEditableText().getSpanStart(nowSpan);
					int end = getEditableText().getSpanEnd(nowSpan);
					getEditableText().removeSpan(nowSpan);
					getEditableText().delete(start, end);
				} else {
					getEditableText().delete(getSelectionEnd()-1, getSelectionEnd());
				}
			} else {
				getEditableText().delete(getSelectionEnd()-1, getSelectionEnd());
			}
		}
	}

	public class MentionsText {
		
		private CharSequence text;
//		private List<MentionsSpan> mentionsSpans;
		
		public CharSequence getText() {
			return text;
		}
		public void setText(CharSequence text) {
			this.text = text;
		}
//		public List<MentionsSpan> getMentionsSpans() {
//			return mentionsSpans;
//		}
//		public void setMentionsSpans(List<MentionsSpan> mentionsSpans) {
//			this.mentionsSpans = mentionsSpans;
//		}
		public int length(){
			if (text != null) {
				return text.length();
			}
			return 0;
		}
	}
}
