package com.newman.shortcutbar.util;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;

import com.newman.shortcutbar.vo.ShortcutItem;

public class ActivityUtils {
	private static final String TAG = ActivityUtils.class.getSimpleName();

	public static Intent getPackageIntent(Context context, ShortcutItem shortcut) {
		String packageName = shortcut.getPackageName();
		LogUtils.d(TAG, "packageName is ", packageName);
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setPackage(packageName);
		intent.setClassName(packageName, shortcut.getComponentName());
		ResolveInfo info = context.getPackageManager().resolveActivity(intent, 0);
		if (info != null && info.activityInfo != null) {
		} else {
			LogUtils.d(TAG, "Cannot found activity");
			intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setPackage(packageName);
			List<ResolveInfo> infos = context.getPackageManager().queryIntentActivities(intent, 0);
			if (infos != null && !infos.isEmpty()) {
				intent.setClassName(packageName, infos.get(0).activityInfo.name);
//				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			} else {
				intent = null;				
			}
		}
		return intent;
	}
}