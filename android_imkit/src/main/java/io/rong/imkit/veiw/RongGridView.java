package io.rong.imkit.veiw;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class RongGridView extends GridView {

	public RongGridView(Context context) {
		super(context);
	}

	public RongGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RongGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

//	@Override
//	public boolean dispatchTouchEvent(MotionEvent ev) {
//		if (ev.getAction() == MotionEvent.ACTION_MOVE) {
//			return true;
//		}
//		return super.dispatchTouchEvent(ev);
//	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);

	}
}
