package com.jonasasx.list;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class WorkFragment extends AbsListFragment implements Callback {
	private List<String>	mDataList	= new ArrayList<String>();
	private BaseAdapter		mAdapter	= new BaseAdapter() {
											@Override
											public int getCount() {
												return mDataList.size();
											}

											@Override
											public String getItem(int position) {
												return mDataList.get(position);
											}

											@Override
											public long getItemId(int position) {
												return 0;
											}

											@Override
											public View getView(int position, View convertView, ViewGroup parent) {
												ViewHolder holder;
												if (convertView == null) {
													holder = new ViewHolder();
													convertView = new FrameLayout(getActivity());
													AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT);
													convertView.setLayoutParams(params);
													holder.text = new TextView(getActivity());
													holder.text.setPadding(12, 32, 12, 32);
													convertView.setBackgroundResource(R.drawable.list_item_bg);
													((FrameLayout) convertView).addView(holder.text);
													convertView.setTag(holder);
												} else {
													holder = (ViewHolder) convertView.getTag();
												}
												holder.position = position;
												holder.text.setText(getItem(position));
												return convertView;
											}

											class ViewHolder {
												int			position	= -1;
												TextView	text;
											}
										};
	protected int			n			= 0;

	public WorkFragment() {
		setFromBottom(false);
		setListMode(MODE_LIST_VIEW);
		setActionModeCallback(this);
	}

	@Override
	protected BaseAdapter getAdapter() {
		return mAdapter;
	}

	@Override
	protected void onLoadingStarted() {
		getListView().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!WorkFragment.this.isResumed())
					return;
				finishLoading(new Exception());
			}
		}, 2000);
	}

	@Override
	public void onClear() {
		mDataList.clear();
		n = 0;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return true;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
	}

}
