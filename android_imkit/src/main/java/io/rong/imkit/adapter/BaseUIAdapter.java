package io.rong.imkit.adapter;

import io.rong.imkit.utils.ResourceUtils;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseUIAdapter<T> extends BaseAdapter<T> {

	public BaseUIAdapter(Context context) {
		super(context);
	}

	public BaseUIAdapter(Context context, List<T> data) {
		super(context, data);
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;

		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(setItemLayoutRes(), null);
			holder = new ViewHolder();
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		return getView(position, convertView, parent, holder);
	}

	protected class ViewHolder {

		private SparseArray<View> views = new SparseArray<View>();

		@SuppressWarnings("unchecked")
		public <E extends View> E obtainView(View convertView, int resId) {

			View v = views.get(resId);

			if (null == v) {
				v = convertView.findViewById(resId);
				views.put(resId, v);
			}

			return (E) v;
		}

		@SuppressWarnings("unchecked")
		public <E extends View> E obtainView(View convertView, String resIdstr) {

			int resId = ResourceUtils.getIDResourceId(mContext, resIdstr);
			Log.d("BaseUIAdapter--obtainView", "resIdstr:" + resIdstr + "   resId:" + resId);

			View v = views.get(resId);

			if (null == v) {
				v = convertView.findViewById(resId);
				views.put(resId, v);
			}

			return (E) v;
		}
	}

	public abstract View getView(int position, View convertView, ViewGroup parent, ViewHolder holder);

	public abstract int setItemLayoutRes();

}
