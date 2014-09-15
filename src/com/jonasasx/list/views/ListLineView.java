package com.jonasasx.list.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class ListLineView extends FrameLayout {

	private boolean	mVisible	= true;

	public ListLineView(Context context) {
		super(context);
	}

	public ListLineView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ListLineView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void show() {
		if (mVisible)
			return;
		mVisible = true;
		requestLayout();
	}

	public void hide() {
		if (!mVisible)
			return;
		mVisible = false;
		requestLayout();
	}

	public void onMeasure(int width, int height) {
		if (mVisible) {
			super.onMeasure(width, height);
			return;
		}
		setMeasuredDimension(1, 1);
	}
}
