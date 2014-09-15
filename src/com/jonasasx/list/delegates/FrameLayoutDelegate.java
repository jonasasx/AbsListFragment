package com.jonasasx.list.delegates;

import android.view.View;
import uk.co.senab.actionbarpulltorefresh.library.viewdelegates.ViewDelegate;

public class FrameLayoutDelegate implements ViewDelegate {

	@Override
	public boolean isReadyForPull(View view, float x, float y) {
		return true;
	}

}
