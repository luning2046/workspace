package io.rong.imkit.veiw;

import io.rong.imkit.utils.ResourceUtils;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;

/**
 * Created by zhjchen on 14-4-17.
 */
public class SearchHeaderView extends BaseFrameLayout {

	private EditText mEditView;

	public SearchHeaderView(Context context) {
		super(context);
		initView();
	}

	public SearchHeaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public SearchHeaderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	private void initView() {
		LayoutInflater.from(getContext()).inflate(ResourceUtils.getLayoutResourceId(getContext(), "rc_view_headerview_search"), this);
		mEditView = getView(this, ResourceUtils.getIDResourceId(getContext(), "edit"));
	}

	public EditText getEditText() {
		return mEditView;
	}
}
