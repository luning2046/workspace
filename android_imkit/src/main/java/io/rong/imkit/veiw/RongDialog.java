package io.rong.imkit.veiw;

import io.rong.imkit.utils.ResourceUtils;
import io.rong.imkit.utils.Util;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

@SuppressWarnings("unchecked")
public class RongDialog extends Dialog {

	protected Context mContext;

	private TextView mTtileView, mContextView;
	private Button mButton1, mButton2, mButton3;

    private ViewGroup mTopViewGroup, mBottomViewGroup,mContentViewGroup;

	public RongDialog(Context context) {
		super(context, ResourceUtils.getStyleResourceId(context, "RcDialog"));
		mContext = context;

		initView(0);
	}

	public RongDialog(Context context, float width) {
		super(context, ResourceUtils.getStyleResourceId(context, "RcDialog"));
		mContext = context;

		initView(width);
	}

	public void initView(float width) {

		setContentView(ResourceUtils.getLayoutResourceId(mContext, "rc_dialog"));
		View view = findViewById(ResourceUtils.getIDResourceId(mContext, "rc_dialog"));

		if (width != 0) {
			view.getLayoutParams().width = Util.dip2px(mContext, width);
		}

		mTopViewGroup = getViewById("dialog_topbar");
		mBottomViewGroup = getViewById("dialog_bottombar");
        mContentViewGroup = getViewById("dialog_content");

		mTtileView = getViewById("dialog_title");
		mTopViewGroup = getViewById("dialog_content");

	}

	public void setView(View view) {
		ViewGroup viewGroup = getViewById("dialog_content");
		viewGroup.removeAllViews();
		viewGroup.setVisibility(View.VISIBLE);
		viewGroup.addView(view);
	}

	public void setContentText(CharSequence charSequence) {
		mContextView = getViewById("dialog_content_txt");
		mContextView.setVisibility(View.VISIBLE);

		mContextView.setText(charSequence);
	}

	public void setContentText(int res) {
		mContextView = getViewById("dialog_content_txt");
		mContextView.setVisibility(View.VISIBLE);

		mContextView.setText(res);
	}

	public void setTitle(CharSequence charSequence) {
		mTopViewGroup.setVisibility(View.VISIBLE);
		mTtileView.setText(charSequence);

	}

	public void setButton1(int strRes, View.OnClickListener listener) {
		setBottomViewVisibility(true);
		mButton1 = getViewById(android.R.id.button1);
		if (strRes != 0)
			mButton1.setText(strRes);
		mButton1.setVisibility(View.VISIBLE);
		if (listener != null)
			mButton1.setOnClickListener(listener);
	}

	public void setButton2(int strRes, View.OnClickListener listener) {
		setBottomViewVisibility(true);
		mButton2 = getViewById(android.R.id.button2);
		if (strRes != 0)
			mButton2.setText(strRes);
		mButton2.setVisibility(View.VISIBLE);
		if (listener != null)
			mButton2.setOnClickListener(listener);
	}

	public void setButton3(int strRes, View.OnClickListener listener) {
		setBottomViewVisibility(true);
		mButton3 = getViewById(android.R.id.button3);
		if (strRes != 0)
			mButton3.setText(strRes);
		mButton3.setVisibility(View.VISIBLE);
		if (listener != null)
			mButton3.setOnClickListener(listener);
	}

	public void setButton1(String str, View.OnClickListener listener) {
		setBottomViewVisibility(true);
		mButton1 = getViewById(android.R.id.button1);
		if (str != null)
			mButton1.setText(str);
		mButton1.setVisibility(View.VISIBLE);
		if (listener != null)
			mButton1.setOnClickListener(listener);
	}

	public void setButton2(String str, View.OnClickListener listener) {
		setBottomViewVisibility(true);
		mButton2 = getViewById(android.R.id.button2);
		if (str != null)
			mButton2.setText(str);
		mButton2.setVisibility(View.VISIBLE);
		if (listener != null)
			mButton2.setOnClickListener(listener);
	}

	public void setButton3(String str, View.OnClickListener listener) {
		setBottomViewVisibility(true);
		mButton3 = getViewById(android.R.id.button3);
		if (str != null)
			mButton3.setText(str);
		mButton3.setVisibility(View.VISIBLE);
		if (listener != null)
			mButton3.setOnClickListener(listener);
	}

	public void setBottomViewVisibility(boolean visible) {

		if (mBottomViewGroup == null)
			mBottomViewGroup = getViewById("dialog_bottombar");
		mBottomViewGroup.setVisibility((visible ? View.VISIBLE : View.GONE));
	}

	protected <T extends View> T getViewById(String id) {
		return (T) findViewById(ResourceUtils.getIDResourceId(mContext, id));
	}

	protected <T extends View> T getViewById(int id) {
		return (T) findViewById(id);
	}

	protected <T extends View> T getViewById(View view, String id) {
		return (T) view.findViewById(ResourceUtils.getIDResourceId(mContext, id));
	}

	protected <T extends View> T getViewById(View view, int id) {
		return (T) view.findViewById(id);
	}

    protected ViewGroup getTopViewGroup() {
        return mTopViewGroup;
    }

    protected void setTopViewGroup(ViewGroup mTopViewGroup) {
        this.mTopViewGroup = mTopViewGroup;
    }

    protected ViewGroup getBottomViewGroup() {
        return mBottomViewGroup;
    }

    protected void setBottomViewGroup(ViewGroup mBottomViewGroup) {
        this.mBottomViewGroup = mBottomViewGroup;
    }

    protected ViewGroup getContentViewGroup() {
        return mContentViewGroup;
    }

    protected void setContentViewGroup(ViewGroup mContentViewGroup) {
        this.mContentViewGroup = mContentViewGroup;
    }
}
