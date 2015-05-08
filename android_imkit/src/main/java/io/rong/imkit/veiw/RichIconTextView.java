package io.rong.imkit.veiw;

import io.rong.imkit.utils.ResourceUtils;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

public class RichIconTextView extends BaseFrameLayout {

	public RichIconTextView(Context context) {
		super(context);
		initView();
	}

	public RichIconTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public RichIconTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	private ImageView iconView;

	private TextView nameTextView;

	private void initView() {

		LayoutInflater.from(getContext()).inflate(ResourceUtils.getLayoutResourceId(getContext(), "rc_view_rich_text"), this);

        iconView = getView(this, android.R.id.icon);
		nameTextView = getView(this, android.R.id.text2);


	}

	public ImageView getIconView() {
		return iconView;
	}

	public void setIconView(ImageView icon) {
		this.iconView = icon;
	}

	public TextView getNameView() {
		return nameTextView;
	}

	public void setNameView(TextView nameTextView) {
		this.nameTextView = nameTextView;
	}

}
