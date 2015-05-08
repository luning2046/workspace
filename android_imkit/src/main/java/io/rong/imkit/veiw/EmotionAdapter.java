package io.rong.imkit.veiw;

import io.rong.imkit.utils.ResourceUtils;
import io.rong.imkit.veiw.gif.EmotionParser;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class EmotionAdapter extends PagerAdapter implements OnClickListener {

	Drawable[] mDrawableArray;
	int[] mDrawableResId;
	int[][] mDrawablePageId;
	Context mContext;
	private static int ROW_COUNT = 3;
	private static int COLUMN_COUNT = 7;
	private static int P_COUNT = ROW_COUNT * COLUMN_COUNT - 1;

	public static final int FLAG_DELETE = -1;
	public static final int FLAG_PNG = 0;

	private int rate = 0;
	private int item_length = 0;
	private int grid_height = 0;

	int mCount;
	ViewHolder mViewHolder;
	OnEmotionItemClickListener mListener;
	ViewPager mViewPager;

	public EmotionAdapter(Context context, ViewPager viewPager) {
		mViewPager = viewPager;
		mContext = context;
		initData();
	}

	private void initData() {
		mDrawableResId = EmotionParser.getInstance(mContext).getSmileResIds();
		if (mDrawableResId.length % P_COUNT > 0) {
			mCount = mDrawableResId.length / P_COUNT + 1;
		} else {
			mCount = mDrawableResId.length / P_COUNT;
		}
		mDrawablePageId = new int[mCount][ROW_COUNT * COLUMN_COUNT];
		for (int i = 0; i < mCount; i++) {
			for (int j = 0; j < P_COUNT; j++) {
				if ((i * P_COUNT + j) < mDrawableResId.length) {
					mDrawablePageId[i][j] = mDrawableResId[i * P_COUNT + j];
				} else {
					break;
				}
			}
		}

		DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
		// 计算item大小和间距 (图片长度/图片间距 = 7/3 ；图片长度/屏幕边距 = 7/2 )
		rate = dm.widthPixels / (COLUMN_COUNT * 10 + 3);
		item_length = rate * 9;
		grid_height = item_length * ROW_COUNT + (rate * 6);

		Log.d("rate", "--------------------" + rate);

		// if (mViewPager != null && mViewPager.getLayoutParams() != null) {
		// mViewPager.getLayoutParams().height =406;// grid_height;
		// }

	}

	public int getMaxHeight() {
		return grid_height;
	}

	@Override
	public int getCount() {
		return mCount;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		ViewHolder holder = (ViewHolder) arg1;
		return arg0 == holder.gridView;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		GridView gridView = null;
		EmotionListAdapter adapter = null;
		ViewHolder holder = null;

		if (mViewHolder != null) {
			gridView = mViewHolder.gridView;
			adapter = mViewHolder.adapter;
			holder = mViewHolder;
			mViewHolder = null;
		} else {
			gridView = (GridView) LayoutInflater.from(mContext).inflate(ResourceUtils.getLayoutResourceId(mContext, "rc_emotion"), null);
			gridView.setScrollContainer(false);
			gridView.setPadding(rate * 2, rate * 2, rate * 2, rate * 2);
			gridView.setHorizontalSpacing(rate);
			final float scale = mContext.getResources().getDisplayMetrics().density;
			
			if (scale <= 1.5) {
				Log.d("scale---", "-----<=1.5---scale:" + scale);
				gridView.setVerticalSpacing(38);
				gridView.setMinimumHeight(272);
			} else {
				Log.d("scale---", "--->1.5-----scale:" + scale);
				gridView.setVerticalSpacing(58);
				gridView.setMinimumHeight(406);
			}

			adapter = new EmotionListAdapter();
			holder = new ViewHolder();
			holder.gridView = gridView;
			holder.adapter = adapter;
			holder.gridView.setNumColumns(COLUMN_COUNT);
		}
		adapter.setData(mDrawablePageId[position]);
		gridView.setAdapter(adapter);
		container.addView(gridView);
		return holder;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		mViewHolder = (ViewHolder) object;
		container.removeView(mViewHolder.gridView);
	}

	class ViewHolder {
		GridView gridView;
		EmotionListAdapter adapter;
	}

	public void setOnEmotionItemClickListener(OnEmotionItemClickListener listener) {
		this.mListener = listener;
	}

	class EmotionListAdapter extends BaseAdapter {

		int[] resIds;

		public EmotionListAdapter() {
		}

		public void setData(int[] resIds) {
			this.resIds = resIds;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			if (resIds != null) {
				return resIds.length;
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return resIds[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView img = (ImageView) LayoutInflater.from(mContext).inflate(ResourceUtils.getLayoutResourceId(mContext, "rc_item_emotion"), null);
			// img.getLayoutParams().width = item_length;
			// img.getLayoutParams().height = item_length;

			img.setLayoutParams(new GridView.LayoutParams(item_length, item_length));

			img.setPadding(rate, rate, rate, rate);
			img.setTag(resIds[position]);
			img.setVisibility(View.INVISIBLE);
			img.setOnClickListener(EmotionAdapter.this);
			if (resIds[position] != 0) {
				img.setImageResource(resIds[position]);
				img.setVisibility(View.VISIBLE);
			} else if (position == (resIds.length - 1)) {
				img.setTag(-1);
				img.setImageResource(ResourceUtils.getDrawableResourceId(mContext, "rc_ic_delete"));
				img.setVisibility(View.VISIBLE);
			}
			return img;
		}
	}

	// private void checkMaxHeight(int height) {
	// Log.d("","height="+height);
	// if (mHeight < height) {
	// mHeight = height;
	// }
	// }

	public interface OnEmotionItemClickListener {
		public void onEmotionClick(Drawable drawable, String code, int flag);
	}

	@Override
	public void onClick(View v) {
		if (mListener != null) {
			int resId = (Integer) v.getTag();
			if (resId == -1) {
				mListener.onEmotionClick(null, "", FLAG_DELETE);
			} else if (resId != 0) {
				mListener.onEmotionClick(null, EmotionParser.getInstance(mContext).getSmileCode(resId), FLAG_PNG);
			}
		}
	}

	private int pxTodip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		Log.d("pxTodip", "-------scale-----" + scale + ":--" + (int) (pxValue / scale + 0.5f));
		return (int) (pxValue / scale + 0.5f);
	}
}
