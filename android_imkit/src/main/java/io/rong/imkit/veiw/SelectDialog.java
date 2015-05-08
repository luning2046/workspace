package io.rong.imkit.veiw;

import io.rong.imkit.utils.ResourceUtils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SelectDialog extends RongDialog implements OnClickListener {

	private TextView textView1, textView2;
	private RelativeLayout mDiaogLayoutOne;
	private RelativeLayout mDiaogLayoutTwo;

	private OnDialogItemViewListener mDialogItemViewListener;

	public SelectDialog(Context context) {
		super(context, 0);
		initView();
	}

	public SelectDialog(Context context, float width) {
		super(context, 180f);
		initView();
	}

	public void initView() {

		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
		View view = layoutInflater.inflate(ResourceUtils.getLayoutResourceId(mContext, "rc_dialog_select_item"), null);

		setView(view);

		mDiaogLayoutOne = getViewById(view, ResourceUtils.getIDResourceId(mContext, "dialog_layout_one"));
		mDiaogLayoutTwo = getViewById(view, ResourceUtils.getIDResourceId(mContext, "dialog_layout_two"));
		mDiaogLayoutOne.setOnClickListener(this);
		mDiaogLayoutTwo.setOnClickListener(this);

		textView1 = getViewById(view, android.R.id.text1);
		textView2 = getViewById(view, android.R.id.text2);

	}

	public void setFristLineContent(String resId) {
		textView1.setText(ResourceUtils.getStringResource(mContext, resId));
	}

	public void setSecondLineContent(String resId) {
		textView2.setText(ResourceUtils.getStringResource(mContext, resId));
	}

	@Override
	public void onClick(View v) {

		if (v == mDiaogLayoutOne) {
			if (mDialogItemViewListener != null) {
				mDialogItemViewListener.OnDialogItemViewClick(mDiaogLayoutOne, 0);
			}
		} else if (v == mDiaogLayoutTwo) {
			if (mDialogItemViewListener != null) {
				mDialogItemViewListener.OnDialogItemViewClick(mDiaogLayoutTwo, 1);
			}
		}
	}

	public interface OnDialogItemViewListener {
		public void OnDialogItemViewClick(View view, int position);

	}

	public void setOnDialogItemViewListener(OnDialogItemViewListener mDialogItemViewListener) {
		this.mDialogItemViewListener = mDialogItemViewListener;
	}
	
	
	public void setDiaogLayoutFirstGone(){
		mDiaogLayoutOne.setVisibility(View.GONE);
	}
}
