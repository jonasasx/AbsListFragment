package com.jonasasx.list.delegates;

import uk.co.senab.actionbarpulltorefresh.library.viewdelegates.ViewDelegate;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.widget.AbsListView;

public class AbsListViewDelegate implements ViewDelegate {

	@SuppressWarnings("rawtypes")
	public static final Class[]	SUPPORTED_VIEW_CLASSES	= { AbsListView.class };

	@Override
	public boolean isReadyForPull(View view, final float x, final float y) {
		boolean ready = false;

		// First we check whether we're scrolled to the top or the bottom
		AbsListView absListView = (AbsListView) view;
		if (absListView.getCount() == 0) {
			ready = true;
		} else if ((!absListView.isStackFromBottom() && absListView.getFirstVisiblePosition() == 0)
				|| (absListView.isStackFromBottom() && absListView.getLastVisiblePosition() == (absListView.getAdapter().getCount() - 1))) {
			if (absListView.isStackFromBottom()) {
				final View lastVisibleChild = absListView.getChildAt(absListView.getChildCount() - 1);
				ready = lastVisibleChild != null && lastVisibleChild.getBottom() == (absListView.getHeight() - absListView.getPaddingBottom());
			} else {
				final View firstVisibleChild = absListView.getChildAt(0);
				ready = firstVisibleChild != null && firstVisibleChild.getTop() >= absListView.getPaddingTop();
			}
		}

		// Then we have to check whether the fas scroller is enabled, and check we're not starting
		// the gesture from the scroller
		if (ready && absListView.isFastScrollEnabled() && isFastScrollAlwaysVisible(absListView)) {
			switch (getVerticalScrollbarPosition(absListView)) {
				case View.SCROLLBAR_POSITION_RIGHT:
					ready = x < absListView.getRight() - absListView.getVerticalScrollbarWidth();
					break;
				case View.SCROLLBAR_POSITION_LEFT:
					ready = x > absListView.getVerticalScrollbarWidth();
					break;
			}
		}

		return ready;
	}

	private int getVerticalScrollbarPosition(AbsListView absListView) {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? CompatV11.getVerticalScrollbarPosition(absListView) : Compat.getVerticalScrollbarPosition(absListView);
	}

	private boolean isFastScrollAlwaysVisible(AbsListView absListView) {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? CompatV11.isFastScrollAlwaysVisible(absListView) : Compat.isFastScrollAlwaysVisible(absListView);
	}

	private static class Compat {
		static int getVerticalScrollbarPosition(AbsListView absListView) {
			return View.SCROLLBAR_POSITION_RIGHT;
		}

		static boolean isFastScrollAlwaysVisible(AbsListView absListView) {
			return false;
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	static class CompatV11 {
		static int getVerticalScrollbarPosition(AbsListView absListView) {
			return absListView.getVerticalScrollbarPosition();
		}

		static boolean isFastScrollAlwaysVisible(AbsListView absListView) {
			return absListView.isFastScrollAlwaysVisible();
		}
	}
}
