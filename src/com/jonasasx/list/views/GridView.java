package com.jonasasx.list.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class GridView extends android.widget.GridView implements HeaderFooterAbsListView {

	public GridView(Context context) {
		super(context);
	}

	public GridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void addHeaderView(View v, Object data, boolean isSelectable) {
	}

	@Override
	public void addHeaderView(View v) {
	}

	@Override
	public int getHeaderViewsCount() {
		return 0;
	}

	@Override
	public boolean removeHeaderView(View v) {
		return false;
	}

	@Override
	public void addFooterView(View v, Object data, boolean isSelectable) {
	}

	@Override
	public void addFooterView(View v) {
	}

	@Override
	public int getFooterViewsCount() {
		return 0;
	}

	@Override
	public boolean removeFooterView(View v) {
		return false;
	}

	@Override
	public void setHeaderDividersEnabled(boolean headerDividersEnabled) {
	}

	@Override
	public boolean areHeaderDividersEnabled() {
		return false;
	}

	@Override
	public void setFooterDividersEnabled(boolean footerDividersEnabled) {
	}

	@Override
	public boolean areFooterDividersEnabled() {
		return false;
	}

	@Override
	public void setSelectionFromTop(int position, int y) {
	}

}
