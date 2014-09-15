package com.jonasasx.list.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class ListView extends android.widget.ListView implements HeaderFooterAbsListView {

	private int	height	= -1;

	public ListView(Context context) {
		super(context);
	}

	public ListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		View v = getChildAt(getChildCount() - 1);
		if (v != null && height > 0 && changed && ((bottom - top) < height)) {
			int b = height - v.getTop();
			final int scrollTo = getLastVisiblePosition();
			super.onLayout(changed, left, top, right, bottom);
			final int offset = (bottom - top) - b;
			post(new Runnable() {
				@Override
				public void run() {
					try {
						setSelectionFromTop(scrollTo, offset - getPaddingTop());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} else {
			try {
				super.onLayout(changed, left, top, right, bottom);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		height = (bottom - top);
	}
}
