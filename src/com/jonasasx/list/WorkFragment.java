package com.jonasasx.list;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

public class WorkFragment extends AbsListFragment {
	private List<String>	mDataList	= new ArrayList<String>();
	private BaseAdapter		mAdapter	= new BaseAdapter() {
											@Override
											public int getCount() {
												return mDataList.size();
											}

											@Override
											public String getItem(int position) {
												return mDataList.get(mDataList.size() - position - 1);
											}

											@Override
											public long getItemId(int position) {
												return 0;
											}

											@Override
											public View getView(int position, View convertView, ViewGroup parent) {
												// if (convertView !=
												// null)
												// return convertView;
												FrameLayout view = new FrameLayout(getActivity());
												TextView text = new TextView(getActivity());
												text.setText(getItem(position));
												text.setPadding(12, 32, 12, 32);
												view.setBackgroundResource(R.drawable.list_item_bg);
												view.addView(text);
												return view;
											}
										};
	protected int			n			= 0;

	public WorkFragment() {
		setFromBottom(true);
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
				synchronized (mDataList) {
					if (n > 30) {
						setNoMoreData();
						finishLoading(null);
						return;
					}
					for (int i = 0; i < 10; i++)
						mDataList.add("Line ___" + Integer.toString(n++) + "___");

					finishLoading(null);
				}
			}
		}, 2000);
	}

	@Override
	public void onClear() {
		mDataList.clear();
		n = 0;
	}

	

}
