package com.jonasasx.list;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class MainActivity extends SherlockFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {

			Fragment fragment = new WorkFragment();
			getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment, "FRAGMENT").commit();
		}
	}

}
