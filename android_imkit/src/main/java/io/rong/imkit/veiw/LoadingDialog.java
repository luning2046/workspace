package io.rong.imkit.veiw;

import io.rong.imkit.utils.ResourceUtils;
import android.app.Dialog;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class LoadingDialog extends Dialog {

	private TextView mTextView;

	public LoadingDialog(Context context) {
		super(context, ResourceUtils.getStyleResourceId(context, "RcDialog"));

		setContentView(ResourceUtils.getLayoutResourceId(context, "rc_dialog_loading"));
		mTextView = (TextView) findViewById(android.R.id.message);

	}

	@Override
	public void show() {
		super.show();

	}

	@Override
	public void dismiss() {
		super.dismiss();
	}

	public void setText(String s) {
		if (mTextView != null) {
			mTextView.setText(s);
			mTextView.setVisibility(View.VISIBLE);
		}
	}

	public void setText(int res) {
		if (mTextView != null) {
			mTextView.setText(res);
			mTextView.setVisibility(View.VISIBLE);
		}
	}

	public void setTextColor(int color) {
		mTextView.setTextColor(color);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			return false;
		}
		return super.onTouchEvent(event);
	}

}
