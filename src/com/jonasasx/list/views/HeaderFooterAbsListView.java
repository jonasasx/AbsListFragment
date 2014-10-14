package com.jonasasx.list.views;

import android.util.SparseBooleanArray;
import android.view.View;

public interface HeaderFooterAbsListView {
	public void addHeaderView(View v, Object data, boolean isSelectable);

	public void addHeaderView(View v);

	public int getHeaderViewsCount();

	public boolean removeHeaderView(View v);

	public void addFooterView(View v, Object data, boolean isSelectable);

	public void addFooterView(View v);

	public int getFooterViewsCount();

	public boolean removeFooterView(View v);

	public void setHeaderDividersEnabled(boolean headerDividersEnabled);

	public boolean areHeaderDividersEnabled();

	public void setFooterDividersEnabled(boolean footerDividersEnabled);

	public boolean areFooterDividersEnabled();

	public void setSelectionFromTop(int position, int y);

	public void setChoiceMode(int choiceMode);

	public void setItemChecked(int position, boolean value);

	public SparseBooleanArray getCheckedItemPositions();

	public int getChoiceMode();

	public void clearChoices();
}
