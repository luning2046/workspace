package io.rong.imkit.veiw;

import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.AbsListView;
import android.widget.SectionIndexer;

public abstract class PinnedHeaderAdapter<T> extends CompositeAdapter<T> {

	/**
	 * Pinned header state: don't show the header.
	 */
	public static final int PINNED_HEADER_GONE = 0;

	/**
	 * Pinned header state: show the header at the top of the list.
	 */
	public static final int PINNED_HEADER_VISIBLE = 1;

	/**
	 * Pinned header state: show the header. If the header extends beyond the
	 * bottom of the first shown element, push it up and clip.
	 */
	public static final int PINNED_HEADER_PUSHED_UP = 2;

	private SectionIndexer mIndexer;

	public PinnedHeaderAdapter(Context context) {
		super(context);

	}

	public PinnedHeaderAdapter(Context context, Collection<List<T>> collection) {
		super(context);
		changeIndexer(collection);
	}

	public void updateCollection(Collection<List<T>> collection) {
		clearPartitions();
		changeIndexer(collection);
	}

	private void changeIndexer(Collection<List<T>> collection) {
		int size = collection.size();
		for (List<T> data : collection) {
			addPartition(new Partition<T>(false, true, data), size);
		}
	}

	protected abstract SectionIndexer updateIndexer(Partition<T>[] collection);

	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (view instanceof PinnedHeaderListView) {
			((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
		}
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	public int getPinnedHeaderState(int position) {

		if (mIndexer == null) {
			return PINNED_HEADER_GONE;
		}

		if (position < 0) {
			return PINNED_HEADER_GONE;
		}

		// The header should get pushed up if the top item shown
		// is the last item in a section for a particular letter.
		int section = mIndexer.getSectionForPosition(position);

		int nextSectionPosition = mIndexer.getPositionForSection(++section);
		if (nextSectionPosition != -1 && position == nextSectionPosition - 1) {
			return PINNED_HEADER_PUSHED_UP;
		}

		return PINNED_HEADER_VISIBLE;
	}

	public int getSectionForPosition(int position) {
		return mIndexer.getSectionForPosition(position);
	}

	public int getPositionForSection(int section) {
		return mIndexer.getPositionForSection(section);
	}

	@Override
	public void notifyDataSetChanged() {
		if (getPartitions() != null){
			mIndexer = updateIndexer(getPartitions());
		}
		super.notifyDataSetChanged();
	}

	public SectionIndexer getSectionIndexer() {
		return mIndexer;
	}

	@Override
	public void notifyDataSetInvalidated() {
		if (getPartitions() != null) {
			mIndexer = updateIndexer(getPartitions());
		}
		super.notifyDataSetInvalidated();
	}

	public abstract void configurePinnedHeader(View header, int position,
			int alpha);

}
