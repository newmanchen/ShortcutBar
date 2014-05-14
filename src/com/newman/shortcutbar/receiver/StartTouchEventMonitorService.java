package com.newman.shortcutbar.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.newman.shortcutbar.activity.Setting;
import com.newman.shortcutbar.service.TouchEventMonitorService;
import com.newman.shortcutbar.util.LogUtils;

public class StartTouchEventMonitorService extends BroadcastReceiver {
	public static final String TAG = StartTouchEventMonitorService.class.getSimpleName();
	public static final String ACTION_RESTART = "com.newman.shortcutbar.action.RESTRAT";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		LogUtils.d(TAG, "StartTouchEventMonitorService onReceive()");
		String action = intent.getAction();
		LogUtils.d(TAG, "action :: ", action);
		
		if (!TextUtils.isEmpty(action)) {
			if (Intent.ACTION_BOOT_COMPLETED.equals(action) || ACTION_RESTART.equals(action)) {
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
				if (sp.getBoolean(Setting.PREF_ENABLE_SERVICE, false)) {
					Intent i = new Intent(TouchEventMonitorService.ACTION_SERVICE);
					context.startService(i);
				}
			}
		}
	}
}