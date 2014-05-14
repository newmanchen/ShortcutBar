package com.newman.shortcutbar.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.newman.shortcutbar.service.PackageChangeService;
import com.newman.shortcutbar.util.LogUtils;

public class PackageChangeReceiver extends BroadcastReceiver {

	private static final String TAG = PackageChangeReceiver.class.getSimpleName();
	@Override
	public void onReceive(Context context, Intent intent) {
		LogUtils.d(TAG, "onReceive() :: intent.getAction() :: " , intent.getAction());
		Intent outIntent = new Intent(PackageChangeService.ACTION_START_PACKAGE_CHANGE_SERVICE);
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
			String pkgName = intent.getDataString().replace("package:", "");
			LogUtils.d(TAG, "package_name:" + intent.getDataString());
			outIntent.putExtra(PackageChangeService.EXTRA_ACTION_DISPATCH, action);
			outIntent.putExtra(PackageChangeService.EXTRA_PACKAGE_NAME, pkgName);
		}
		context.startService(outIntent);
	}
}