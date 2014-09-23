package com.jonasasx.list;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jonasasx.list.delegates.AbsListViewDelegate;
import com.jonasasx.list.views.GridView;
import com.jonasasx.list.views.HeaderFooterAbsListView;
import com.jonasasx.list.views.ListLineView;
import com.jonasasx.list.views.ListView;

abstract public class AbsListFragment extends SherlockFragment implements OnRefreshListener, OnScrollListener, OnItemLongClickListener, OnItemClickListener {
	public final static byte			MODE_LIST_VIEW	= 0;
	public final static byte			MODE_GRID_VIEW	= 1;
	private HeaderFooterAbsListView		mCastedList;
	private boolean						mFromBottom		= false;
	private View						mFullEmptyContainer;
	private TextView					mFullEmptyView;
	private View						mFullErrorButton;
	private View						mFullErrorContainer;
	private TextView					mFullErrorView;
	private View						mFullProgress;
	private boolean						mIsLoading		= false;
	private int							mLastRowsCount;
	private ListLineView				mLineProgress;
	@SuppressWarnings("unused")
	private View						mListContainer;
	private AbsListView					mListView;
	private ListLineView				mLoadMoreContainer;
	private TextView					mLoadMoreView;
	private boolean						mNoMoreData		= false;
	private PullToRefreshLayout			mPullToRefreshLayout;
	private View						mView;
	private ActionMode					mActionMode;
	private ActionMode.Callback			mActionModeCallback;
	private byte						mListMode		= MODE_LIST_VIEW;
	private OnListItemClickListener		mOnListItemClickListener;
	private OnListItemLongClickListener	mOnListItemLongClickListener;
	private boolean						mResumed		= false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.abs_list_content, container, false);
		mPullToRefreshLayout = (PullToRefreshLayout) mView.findViewById(R.id.absPtrLayout);
		PullToRefreshLayout.LayoutParams lp = new PullToRefreshLayout.LayoutParams(PullToRefreshLayout.LayoutParams.MATCH_PARENT, PullToRefreshLayout.LayoutParams.MATCH_PARENT);
		switch (mListMode) {
			case MODE_LIST_VIEW:
				mListView = new ListView(getActivity());
				((ListView) mListView).setDrawSelectorOnTop(false);
				((ListView) mListView).setDividerHeight(0);
				break;
			case MODE_GRID_VIEW:
				mListView = new GridView(getActivity());
				((GridView) mListView).setGravity(Gravity.CENTER);
				break;
		}
		mListView.setId(android.R.id.list);
		mListView.setLayoutParams(lp);
		lp.setViewDelegateClassName(AbsListViewDelegate.class.getName());
		mPullToRefreshLayout.addView(mListView, 0, lp);
		mCastedList = (HeaderFooterAbsListView) mListView;
		mFullProgress = mView.findViewById(R.id.absProgressContainer);
		mListContainer = mView.findViewById(R.id.absListContainer);
		mFullErrorContainer = mView.findViewById(R.id.absInternalError);
		mFullErrorView = (TextView) mView.findViewById(R.id.absInternalErrorText);
		mFullErrorButton = mView.findViewById(R.id.absInternalErrorButton);
		mFullEmptyContainer = mView.findViewById(R.id.absInternalEmpty);
		mFullEmptyView = (TextView) mView.findViewById(R.id.absInternalEmptyText);
		((TextView) mView.findViewById(R.id.absLoadingText)).setText(getLoadingText());
		((Button) mFullErrorButton).setText(getTryAgainText());
		((ImageView) mView.findViewById(R.id.absErrorIcon)).setImageDrawable(getErrorDrawable());

		mFullEmptyView.setText(getEmptyText());
		setEmptyView(mFullEmptyView);
		mListView.setStackFromBottom(mFromBottom);
		if (mFromBottom)
			mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mLineProgress = new ListLineView(getActivity());
		ProgressBar progressView = new ProgressBar(getActivity());
		progressView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
		mLineProgress.addView(progressView);
		mLineProgress.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
		if (mFromBottom)
			mCastedList.addHeaderView(mLineProgress);
		else
			mCastedList.addFooterView(mLineProgress);

		mLoadMoreContainer = new ListLineView(getActivity());
		mLoadMoreView = new Button(getActivity());
		mLoadMoreView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
		mLoadMoreView.setText(getLoadMoreText());
		mLoadMoreView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadData();
			}
		});
		mLoadMoreContainer.addView(mLoadMoreView);
		mLoadMoreContainer.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
		if (mFromBottom)
			mCastedList.addHeaderView(mLoadMoreContainer);
		else
			mCastedList.addFooterView(mLoadMoreContainer);

		if (mFromBottom)
			mCastedList.setHeaderDividersEnabled(false);
		else
			mCastedList.setFooterDividersEnabled(false);
		mListView.setEmptyView(mFullEmptyContainer);
		((AdapterView<ListAdapter>) mListView).setAdapter(getAdapter());
		int[] viewIds = new int[] { android.R.id.list, R.id.absInternalEmpty, R.id.absInternalError };
		ActionBarPullToRefresh.from(getActivity()).theseChildrenArePullable(viewIds).options(new Options.Builder().fromTop(!mFromBottom).build()).listener(this).setup(mPullToRefreshLayout);
		mListView.setOnScrollListener(this);
		mFullErrorButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clear();
			}
		});

		mListView.setOnItemLongClickListener(this);
		mListView.setOnItemClickListener(this);
		if (!mNoMoreData)
			loadData();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mCastedList = null;
		mFullEmptyContainer = null;
		mFullEmptyView = null;
		mFullErrorButton = null;
		mFullErrorContainer = null;
		mFullErrorView = null;
		mFullProgress = null;
		mLineProgress = null;
		mListContainer = null;
		mListView = null;
		mLoadMoreContainer = null;
		mLoadMoreView = null;
		mPullToRefreshLayout = null;
		mView = null;
		mActionMode = null;
		mActionModeCallback = null;
	}

	@Override
	public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mCastedList.getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE) {
			if (mActionMode != null)
				if (getCheckedItemCount() == 0) {
					mActionMode.finish();
				} else {
					mActionMode.invalidate();
				}
			return;
		}
		if (mOnListItemClickListener != null)
			mOnListItemClickListener.onListItemClick(parent, view, position - mCastedList.getHeaderViewsCount(), id);
	}

	@Override
	public final boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (mOnListItemLongClickListener != null && mOnListItemLongClickListener.onListItemLongClick(parent, view, position - mCastedList.getHeaderViewsCount(), id))
			return true;
		if (mActionMode != null || mActionModeCallback == null)
			return false;
		mCastedList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
		ActionMode.Callback callback = new ActionMode.Callback() {

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				return mActionModeCallback.onCreateActionMode(mode, menu);
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return mActionModeCallback.onPrepareActionMode(mode, menu);
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				return mActionModeCallback.onActionItemClicked(mode, item);
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				clearChoices();
				mActionMode = null;
				mActionModeCallback.onDestroyActionMode(mode);
			}
		};
		mCastedList.setItemChecked(position, true);
		mActionMode = ((SherlockFragmentActivity) getActivity()).startActionMode(callback);
		return true;
	}

	protected void clearChoices() {
		SparseBooleanArray positions = mCastedList.getCheckedItemPositions();
		if (positions == null)
			return;
		for (int i = 0; i < positions.size(); i++) {
			if (positions.valueAt(i))
				mCastedList.setItemChecked(positions.keyAt(i), false);
		}
		mListView.clearChoices();
		mListView.requestLayout();
		mListView.post(new Runnable() {
			@Override
			public void run() {
				mListView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);

			}
		});
	}

	@Override
	public void onRefreshStarted(View paramView) {
		clear();
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (!mResumed || mIsLoading || mNoMoreData)
			return;
		if ((mFromBottom && firstVisibleItem <= getPreloadCount()) || (!mFromBottom && (firstVisibleItem + visibleItemCount) >= getAdapter().getCount() - getPreloadCount()) || getPreloadCount() == 0)
			loadData();
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	public void clear() {
		clearChoices();
		if (mActionMode != null) {
			mActionMode.finish();
			mActionMode = null;
		}
		mListView.post(new Runnable() {
			@Override
			public void run() {
				onClear();
				mNoMoreData = false;
				notifyDataSetChanged();
			}
		});
	}

	protected void finishLoading(Exception e) {
		mIsLoading = false;
		if (e != null) {
			mNoMoreData = true;
			notifyDataSetChanged();
			showError(e.getMessage());
		} else if (getAdapter().getCount() == 0) {
			showItems();
		} else if (mLastRowsCount == getAdapter().getCount()) {
			mNoMoreData = true;
			showItems();
		} else {
			notifyDataSetChanged();
			showItems();
		}
		if (mPullToRefreshLayout.isRefreshing())
			mPullToRefreshLayout.setRefreshComplete();
		onLoadingFinished();
		if (mLastRowsCount == 0 && mFromBottom)
			mListView.post(new Runnable() {
				@Override
				public void run() {
					mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
				}
			});
	}

	protected abstract BaseAdapter getAdapter();

	public AbsListView getListView() {
		return mListView;
	}

	public HeaderFooterAbsListView getCastedListView() {
		return mCastedList;
	}

	private int getPreloadCount() {
		return getListView().getChildCount();
	}

	protected boolean isEmpty() {
		return getAdapter().getCount() == 0;
	}

	public boolean isFromBottom() {
		return mFromBottom;
	}

	private void loadData() {
		mNoMoreData = false;
		showProgress();
		mIsLoading = true;
		mLastRowsCount = getAdapter().getCount();
		if (mLastRowsCount == 0 && mFromBottom)
			mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		onLoadingStarted();
	}

	private void notifyDataSetChanged() {
		if (mFromBottom && !isEmpty() && mLastRowsCount > 0) {
			final int firstVisPos = mListView.getLastVisiblePosition();
			View firstVisView = mListView.getChildAt(mListView.getChildCount() - 1);
			final int top = ((firstVisView == null || firstVisView instanceof ListLineView) ? 0 : firstVisView.getTop()) - mListView.getPaddingTop();
			final int newRowsCount = getAdapter().getCount() - mLastRowsCount;
			final KeepCheckedHelper keeper = new KeepCheckedHelper(mListView);
			getAdapter().notifyDataSetChanged();
			mCastedList.setSelectionFromTop(firstVisPos + newRowsCount, top);
			mListView.post(new Runnable() {
				@Override
				public void run() {
					keeper.restore(newRowsCount);
				}
			});

		} else
			getAdapter().notifyDataSetChanged();
	}

	abstract public void onClear();

	protected void onLoadingFinished() {

	}

	abstract protected void onLoadingStarted();

	public void setFromBottom(boolean fromBottom) {
		mFromBottom = fromBottom;
	}

	protected void setNoMoreData() {
		mNoMoreData = true;
	}

	private void showItems() {
		mLineProgress.hide();
		mLoadMoreContainer.hide();
		setEmptyView(mFullEmptyContainer);
	}

	private void showError(CharSequence text) {
		showItems();
		if (isEmpty()) {
			mFullErrorView.setText(text);
			setEmptyView(mFullErrorContainer);
		} else {
			mLoadMoreContainer.show();
		}
	}

	private void showProgress() {
		showItems();
		if (isEmpty()) {
			setEmptyView(mFullProgress);
		} else {
			mLineProgress.show();
		}
	}

	private void setEmptyView(View newView) {
		if (newView == null)
			return;
		View view = mListView.getEmptyView();
		if (view == newView)
			return;
		if (view != null && view.getVisibility() == View.VISIBLE)
			view.setVisibility(View.GONE);
		if (newView.getVisibility() != View.VISIBLE)
			newView.setVisibility(View.VISIBLE);
		mListView.setEmptyView(newView);

	}

	private static class KeepCheckedHelper {

		SparseBooleanArray		mCheckedList;
		AbsListView				mList;
		HeaderFooterAbsListView	mCasted;

		public KeepCheckedHelper(AbsListView list) {
			mList = list;
			mCasted = (HeaderFooterAbsListView) list;
			mCheckedList = mCasted.getCheckedItemPositions() == null ? null : mList.getCheckedItemPositions().clone();
		}

		public void restore(final int offset) {
			if (mCheckedList != null && mList.getChildCount() > 0 && mCasted.getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE) {
				for (int i = 0; i < mCheckedList.size(); i++) {
					int position = mCheckedList.keyAt(i);
					if (mCheckedList.get(position)) {
						mCasted.setItemChecked(position, false);
					}
				}

				for (int i = 0; i < mCheckedList.size(); i++) {
					int position = mCheckedList.keyAt(i);
					if (mCheckedList.get(position)) {
						mCasted.setItemChecked(position + offset, true);
					}
				}
			}
		}
	}

	private int getCheckedItemCount() {
		int count = 0;
		SparseBooleanArray positions = mCastedList.getCheckedItemPositions();
		if (positions != null) {
			for (int i = 0; i < positions.size(); i++) {
				if (positions.get(positions.keyAt(i), false))
					count++;
			}
		}
		return count;
	}

	public void setActionModeCallback(ActionMode.Callback actionModeCallback) {
		if (actionModeCallback != null)
			mActionModeCallback = actionModeCallback;
	}

	public byte getListMode() {
		return mListMode;
	}

	public void setListMode(byte listMode) {
		mListMode = listMode;
	}

	protected CharSequence getEmptyText() {
		return "Empty";
	}

	protected CharSequence getLoadMoreText() {
		return "Load More";
	}

	protected CharSequence getLoadingText() {
		return "Loading";
	}

	protected CharSequence getTryAgainText() {
		return "Try again";
	}

	protected Drawable getErrorDrawable() {
		return null;
	}

	public void setOnListItemClickListener(OnListItemClickListener onListItemClickListener) {
		mOnListItemClickListener = onListItemClickListener;
	}

	public void setOnListItemLongClickListener(OnListItemLongClickListener onListItemLongClickListener) {
		mOnListItemLongClickListener = onListItemLongClickListener;
	}

	public static interface OnListItemClickListener {
		public void onListItemClick(AdapterView<?> parent, View view, int position, long id);
	}

	public static interface OnListItemLongClickListener {
		public boolean onListItemLongClick(AdapterView<?> parent, View view, int position, long id);
	}

	@Override
	public void onPause() {
		super.onPause();
		mResumed = false;
	}

	@Override
	public void onResume() {
		super.onResume();
		mResumed = true;
	}
}
