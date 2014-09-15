package com.jonasasx.list;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.SparseBooleanArray;
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
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jonasasx.list.views.HeaderFooterAbsListView;
import com.jonasasx.list.views.ListLineView;
import com.jonasasx.list.views.ListView;

abstract public class AbsListFragment extends SherlockFragment implements OnRefreshListener, OnScrollListener, OnItemLongClickListener, OnItemClickListener {
	private HeaderFooterAbsListView	mCastedList;
	private boolean					mFromBottom	= false;
	private View					mFullEmptyContainer;
	private TextView				mFullEmptyView;
	private View					mFullErrorButton;
	private View					mFullErrorContainer;
	private TextView				mFullErrorView;
	private View					mFullProgress;
	private boolean					mIsLoading	= false;
	private int						mLastRowsCount;
	private ListLineView			mLineProgress;
	@SuppressWarnings("unused")
	private View					mListContainer;
	private AbsListView				mListView;
	private ListLineView			mLoadMoreContainer;
	private TextView				mLoadMoreView;
	private boolean					mNoMoreData	= false;
	private PullToRefreshLayout		mPullToRefreshLayout;
	private View					mView;
	private ActionMode				mMode;
	private ActionMode.Callback		mActionModeCallback;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.list_content, container, false);
		mListView = (AbsListView) mView.findViewById(android.R.id.list);
		mCastedList = (HeaderFooterAbsListView) mListView;
		mFullProgress = mView.findViewById(R.id.progressContainer);
		mListContainer = mView.findViewById(R.id.listContainer);
		mFullErrorContainer = mView.findViewById(R.id.internalError);
		mFullErrorView = (TextView) mView.findViewById(R.id.internalErrorText);
		mFullErrorButton = mView.findViewById(R.id.internalErrorButton);
		mFullEmptyContainer = mView.findViewById(R.id.internalEmpty);
		mFullEmptyView = (TextView) mView.findViewById(R.id.internalEmptyText);
		return mView;
	}

	@SuppressLint("NewApi")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mListView.setStackFromBottom(mFromBottom);
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
		mLoadMoreView.setText("Load more");
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
		mPullToRefreshLayout = (PullToRefreshLayout) mView.findViewById(R.id.ptr_layout);
		int[] viewIds = new int[] { android.R.id.list, R.id.internalEmpty, R.id.internalError };
		ActionBarPullToRefresh.from(getActivity()).theseChildrenArePullable(viewIds).options(new Options.Builder().fromTop(!mFromBottom).build()).listener(this).setup(mPullToRefreshLayout);
		mListView.setOnScrollListener(this);
		mFullErrorButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadData();
			}
		});

		mListView.setOnItemLongClickListener(this);
		mListView.setOnItemClickListener(this);
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
		mMode = null;
		mActionModeCallback = null;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mCastedList.getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE) {
			if (mMode != null)
				if (getCheckedItemCount() == 0) {
					mMode.finish();
				} else {
					mMode.invalidate();
				}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (mMode != null || mActionModeCallback == null)
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
				mCastedList.setChoiceMode(ListView.CHOICE_MODE_NONE);
				mMode = null;
				mActionModeCallback.onDestroyActionMode(mode);
			}
		};
		mMode = ((SherlockFragmentActivity) getActivity()).startActionMode(callback);
		mCastedList.setItemChecked(position, true);
		return true;
	}

	protected void clearChoices() {
		SparseBooleanArray positions = mCastedList.getCheckedItemPositions();
		for (int i = 0; i < positions.size(); i++) {
			if (positions.valueAt(i))
				mCastedList.setItemChecked(positions.keyAt(i), false);
		}
	}

	@Override
	public void onRefreshStarted(View paramView) {
		if (mMode != null)
			mMode.finish();
		onClear();
		mNoMoreData = false;
		notifyDataSetChanged();
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (mIsLoading || mNoMoreData)
			return;
		if ((mFromBottom && firstVisibleItem <= getPreloadCount()) || (!mFromBottom && (firstVisibleItem + visibleItemCount) >= getAdapter().getCount() - getPreloadCount()) || getPreloadCount() == 0)
			loadData();
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	protected void finishLoading(Exception e) {
		mIsLoading = false;
		if (e != null) {
			mNoMoreData = true;
			notifyDataSetChanged();
			showError(e.getMessage());
		} else if (getAdapter().getCount() == 0) {
			showEmpty("Empty");
		} else {
			notifyDataSetChanged();
			showItems();
		}
		if (mPullToRefreshLayout.isRefreshing())
			mPullToRefreshLayout.setRefreshComplete();
		onLoadingFinished();
	}

	protected abstract BaseAdapter getAdapter();

	public AbsListView getListView() {
		return mListView;
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
		onLoadingStarted();
	}

	private void notifyDataSetChanged() {
		if (mFromBottom && !isEmpty()) {
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

	private void showEmpty(CharSequence text) {
		showItems();
		if (isEmpty()) {
			mFullEmptyView.setText(text);
			if (mFullEmptyContainer.getVisibility() != View.VISIBLE)
				mFullEmptyContainer.setVisibility(View.VISIBLE);
		}
	}

	private void showError(CharSequence text) {
		showItems();
		if (isEmpty()) {
			mFullErrorView.setText(text);
			if (mFullErrorContainer.getVisibility() != View.VISIBLE)
				mFullErrorContainer.setVisibility(View.VISIBLE);
		} else {
			mLoadMoreContainer.show();
		}
	}

	private void showItems() {
		if (mFullProgress.getVisibility() == View.VISIBLE)
			mFullProgress.setVisibility(View.GONE);
		mLineProgress.hide();
		if (mFullErrorContainer.getVisibility() == View.VISIBLE)
			mFullErrorContainer.setVisibility(View.GONE);
		mLoadMoreContainer.hide();
		if (mFullEmptyContainer.getVisibility() == View.VISIBLE)
			mFullEmptyContainer.setVisibility(View.GONE);
	}

	private void showProgress() {
		showItems();
		if (isEmpty()) {
			if (mFullProgress.getVisibility() != View.VISIBLE)
				mFullProgress.setVisibility(View.VISIBLE);
		} else {
			mLineProgress.show();
		}
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
}
