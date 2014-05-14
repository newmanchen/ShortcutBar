package com.newman.shortcutbar.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class Entry extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		boolean isFirstActivate = sp.getBoolean(Setting.PREF_FIRST_ACTIVATE, true);
		Intent intent = new Intent();
		if (isFirstActivate) {
			intent.setClass(this, Guide.class);
		} else {
			intent.setClass(this, Setting.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		startActivity(intent);
		finish();
	}
}