package com.newman.shortcutbar.service;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;

import com.newman.shortcutbar.provider.DatabaseHelper;
import com.newman.shortcutbar.util.LogUtils;
import com.newman.shortcutbar.vo.ShortcutItem;

public class PackageChangeService extends IntentService {

	private static final String TAG = PackageChangeService.class.getSimpleName();
	private static final String SERVICE_NAME = "com.newman.shortcutbar.package.change.service";
	public static final String ACTION_START_PACKAGE_CHANGE_SERVICE = "com.newman.shortcutbar.action.START_PACKAGE_CHANGE_SERVICE";
	
	public static final String EXTRA_ACTION_DISPATCH = "extra_action_dispatch";
	public static final String EXTRA_PACKAGE_NAME = "extra_package_name";

	private DatabaseHelper mDatabaseHelper;
	public PackageChangeService() {
		super(SERVICE_NAME);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		LogUtils.d(TAG, "onHandleIntent() :: start service :: action :: ", intent.getAction());
		String action = intent.getAction();
		if (ACTION_START_PACKAGE_CHANGE_SERVICE.equals(action)) {
			mDatabaseHelper = new DatabaseHelper(this);
			
			startSync(intent);
		} else {
			LogUtils.d(TAG, "action name is not match");
		}
	}
	
	private void startSync(Intent intent) {
		String action = intent.getStringExtra(EXTRA_ACTION_DISPATCH);
		if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
			String pkgName = intent.getStringExtra(EXTRA_PACKAGE_NAME);
			deletePackage(pkgName);
		}
	}
	
	private void deletePackage(String pkgName) {
		if (!TextUtils.isEmpty(pkgName)) {
			ArrayList<ShortcutItem> list = mDatabaseHelper.getAllShortcutItem();
			ArrayList<ShortcutItem> finalList = new ArrayList<ShortcutItem>();
			LogUtils.d(TAG, "ori list :: ", list);
			int size = list.size();
			boolean find = false;
			for (int i = 0 ; i < size ; i++) {
				ShortcutItem item = list.get(i);
				if (pkgName.equals(item.getPackageName())) {
					find = true;
					continue;
				} else {
					if (find) {
						int temp = item.getOrder();
						item.setOrder(temp-1);
					}
					finalList.add(item);
				}
			}
			if (find) {
				mDatabaseHelper.deleteShortcutItem();
				LogUtils.d(TAG, "final list :: ", finalList);
				mDatabaseHelper.addShortcutItem(finalList);
			} else {
				LogUtils.d(TAG, "not find same package");
			}
		} else {
			LogUtils.d(TAG, "pkgName is empty or null");
		}
	}
}