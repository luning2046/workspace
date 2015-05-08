package io.rong.imkit.veiw;

import io.rong.imkit.utils.ResourceUtils;
import io.rong.imkit.veiw.EmotionAdapter.OnEmotionItemClickListener;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

public class EmotionView extends FrameLayout implements OnEmotionItemClickListener {

	private ViewPager mPager;
	private EmotionAdapter mAdapter;
	private OnEmotionItemClickListener mListener;
	private Context mContext;
	private CirclePageIndicator mIndicator;

	public EmotionView(Context context) {
		super(context);
		mContext = context;
		initView();
		initData();
	}

	public EmotionView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
		initData();
	}

	private void initView() {
		View view = LayoutInflater.from(mContext).inflate(ResourceUtils.getLayoutResourceId(getContext(), "rc_view_emotion"), this);
		mPager = (ViewPager) view.findViewById(ResourceUtils.getIDResourceId(getContext(), "viewpager"));
		mIndicator = (CirclePageIndicator) view.findViewById(ResourceUtils.getIDResourceId(getContext(), "radio_group"));

	}

	private void initData() {
		mAdapter = new EmotionAdapter(mContext, mPager);
		mAdapter.setOnEmotionItemClickListener(this);

		mPager.setAdapter(mAdapter);
		mPager.setCurrentItem(0);
		mIndicator.setViewPager(mPager);
		mIndicator.setFillColor(0xffffffff);
		mIndicator.setPageColor(0xff848484);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	public void onEmotionClick(Drawable drawable, String code, int flag) {
		if (mListener != null) {
			mListener.onEmotionClick(drawable, code, flag);
		}
	}

	public void setOnEmotionItemClickListener(OnEmotionItemClickListener listener) {
		mListener = listener;
	}

}
