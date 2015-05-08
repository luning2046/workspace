package io.rong.imkit.veiw;

import io.rong.imkit.utils.ResourceUtils;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by zhjchen on 14-4-4.
 */
@SuppressWarnings("unchecked")
public class BaseFrameLayout extends FrameLayout {
	protected Context mContext;

	public BaseFrameLayout(Context context) {
		super(context);
		mContext = context;
	}

	public BaseFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BaseFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	
	protected <T extends View> T getView(View view, String resId) {
		return (T) view.findViewById(ResourceUtils.getIDResourceId(getContext(), resId));
	}

	protected <T extends View> T getView(View view, int resId) {
		return (T) view.findViewById(resId);
	}

}
