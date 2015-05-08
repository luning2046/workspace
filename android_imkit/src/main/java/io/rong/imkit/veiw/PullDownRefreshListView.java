package io.rong.imkit.veiw;

/**
 * Created by zhjchen on 14-4-14.
 */

import io.rong.imkit.utils.ResourceUtils;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.ListView;

public class PullDownRefreshListView extends ListView implements OnScrollListener {

	private static final String TAG = "PullDownRefreshListView";

    // 刷新完成标志
    private final static int DONE = 0;
	// 松开刷新标志
	private final static int RELEASE_TO_REFRESH = 1;
	// 下拉刷新标志
	private final static int PULL_TO_REFRESH = 2;
	// 正在刷新标志
	private final static int REFRESHING = 3;

	private LayoutInflater inflater;
	private LinearLayout headView;
	// 用来设置箭头图标动画效果
	private RotateAnimation animation;
	private RotateAnimation reverseAnimation;
	// 用于保证startY的值在一个完整的touch事件中只被记录一次
	private boolean isRecored;
	private boolean isBack;
	private int headContentHeight;
	private int startY;
	private int mFirstVisiableItem;
	private int mVisableCount;
	private int state;
	public OnRefreshListener refreshListener;

	public PullDownRefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		inflater = LayoutInflater.from(context);
		headView = (LinearLayout) inflater.inflate(ResourceUtils.getLayoutResourceId(context, "rc_pull_head"), null);
		measureView(headView);
		headContentHeight = headView.getMeasuredHeight();
		headView.getMeasuredWidth();
		headView.setPadding(0, -1 * headContentHeight, 0, 0);
		headView.invalidate();
		headView.setVisibility(View.GONE);
		addHeaderView(headView);
		setOnScrollListener(this);
		animation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(500);
		animation.setFillAfter(true);
		reverseAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(500);
		reverseAnimation.setFillAfter(true);
	}

	@Override
	public void onScroll(AbsListView arg0, int firstVisiableItem, int arg2, int arg3) {
		mFirstVisiableItem = firstVisiableItem;
		mVisableCount = arg2;
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {

		if (OnScrollListener.SCROLL_STATE_IDLE == arg1) {

			try {
				for (int i = 0; i <= mVisableCount - 1; i++) {

					if (null != arg0.getChildAt(i)) {
						View convertView = arg0.getChildAt(i);
						View textView = convertView.findViewById(ResourceUtils.getIDResourceId(getContext(), "conversation_message_content_tv"));

						if (textView instanceof AnimationTextView) {
							AnimationTextView msgText = (AnimationTextView) textView;

							if (null != msgText && msgText.getGifDrawable() != null) {
								if (!msgText.getGifDrawable().isOnStarted()) {
									msgText.getGifDrawable().setOnStarted(true);
								}
								msgText.startGifAnimation();
							}
						}
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				// if (LogF.DEBUG) {
				Log.d(TAG, " class PullDownRefreshListView method onScrollStateChanged has exception:" + e.getMessage());
				// }
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (mFirstVisiableItem == 0 && !isRecored) {
				startY = (int) event.getY();
				isRecored = true;
			}
			break;
		case MotionEvent.ACTION_UP:
			if (state != REFRESHING) {
				if (state == DONE) {
				}
				if (state == PULL_TO_REFRESH) {
					state = DONE;
					changeHeaderViewByState();
				}
				if (state == RELEASE_TO_REFRESH) {
					state = REFRESHING;
					changeHeaderViewByState();
					onRefresh();
				}
			}
			isRecored = false;
			isBack = false;
			break;
		case MotionEvent.ACTION_MOVE:
			int tempY = (int) event.getY();
			if (!isRecored && mFirstVisiableItem == 0) {
				isRecored = true;
				startY = tempY;
			}
			if (state != REFRESHING && isRecored) {
				int diffY = (tempY - startY) * 2 / 3;// 此处是计算差值高度，用于listview下拉超过最顶端时返回的距离
				// 可以松开刷新了
				if (state == RELEASE_TO_REFRESH) {
					// 往上推，推到屏幕足够掩盖head的程度，但还没有全部掩盖
					if ((diffY < headContentHeight) && diffY > 0) {
						state = PULL_TO_REFRESH;
						changeHeaderViewByState();
					}
					// 一下子推到顶
					else if (diffY <= 0) {
						state = DONE;
						changeHeaderViewByState();
					}
					// 往下拉，或者还没有上推到屏幕顶部掩盖head
					else {
						// 不用进行特别的操作，只用更新paddingTop的值就行了
					}
				}
				// 还没有到达显示松开刷新的时候,DONE或者是PULL_To_REFRESH状态
				if (state == PULL_TO_REFRESH) {
					// 下拉到可以进入RELEASE_TO_REFRESH的状态
					if (diffY >= headContentHeight) {
						state = RELEASE_TO_REFRESH;
						isBack = true;
						changeHeaderViewByState();
					}
					// 上推到顶了
					else if (diffY <= 0) {
						state = DONE;
						changeHeaderViewByState();
					}
				}
				// done状态下
				if (state == DONE) {
					if (diffY > 0) {
						state = PULL_TO_REFRESH;
						changeHeaderViewByState();
					}
				}
				// 更新headView的size
				if (state == PULL_TO_REFRESH) {
					headView.setPadding(0, -1 * headContentHeight + diffY, 0, 0);
					headView.invalidate();
				}
				// 更新headView的paddingTop
				if (state == RELEASE_TO_REFRESH) {
					headView.setPadding(0, diffY - headContentHeight, 0, 0);
					headView.invalidate();
				}
			}
			break;
		}
		return super.onTouchEvent(event);
	}

	// 当状态改变时候，调用该方法，以更新界面
	private void changeHeaderViewByState() {
		switch (state) {
		case RELEASE_TO_REFRESH:
		case PULL_TO_REFRESH:
			headView.setVisibility(View.GONE);
			break;
		case REFRESHING:
			headView.setPadding(0, 20, 0, 0);
			headView.invalidate();
			headView.setVisibility(View.VISIBLE);
			break;
		case DONE:
			headView.setPadding(0, -1 * headContentHeight, 0, 0);
			headView.invalidate();
			headView.setVisibility(View.GONE);
			break;
		}
	}

	public void setonRefreshListener(OnRefreshListener refreshListener) {
		this.refreshListener = refreshListener;
	}

	public interface OnRefreshListener {
		public void onRefresh();
	}

	public void onRefreshComplete() {
		state = DONE;
		changeHeaderViewByState();
	}

	private void onRefresh() {
		if (refreshListener != null) {
			refreshListener.onRefresh();
		}
	}

	// 此处是“估计”headView的width以及height
	@SuppressWarnings("deprecation")
	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	@Override
	public void setSelection(int position) {
		// smoothScrollBy(0, 0);//停止滚动
		super.setSelection(position);
	}

}