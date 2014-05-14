package com.newman.shortcutbar.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.text.TextUtils;

import com.newman.shortcutbar.provider.table.ShortcutTable;
import com.newman.shortcutbar.util.CloseUtils;
import com.newman.shortcutbar.util.LogUtils;
import com.newman.shortcutbar.vo.ShortcutItem;

public class DatabaseHelper {
	private static final String TAG = DatabaseHelper.class.getSimpleName();

	private ContentResolver mResolver = null;
	private Resources mResources = null;
	
	private static final String SORT_BY_ORDER = ShortcutTable.ColumnNames.LIST_ORDER + " ASC";

	public DatabaseHelper(Context context) {
		mResolver = context.getContentResolver();
		mResources = context.getResources();
	}
	
	public long addShortcutItem(ArrayList<ShortcutItem> scs) {
		int count = 0;
		try {
			ArrayList<ContentValues> values = new ArrayList<ContentValues>();
			for (ShortcutItem sc : scs) {
				values.add(sc.toContentValues());
			}
			count = mResolver.bulkInsert(ShortcutTable.CONTENT_URI, values.toArray(new ContentValues[values.size()]));
		} catch (Exception e) {
			LogUtils.e(TAG, e.getMessage(), e);
		}
		return count;
	}
	
	public long addShortcutItem(HashMap<String, ShortcutItem> scs) {
		int count = 0;
		try {
			ArrayList<ContentValues> values = new ArrayList<ContentValues>();
			Set<String> keys = scs.keySet();
			for (String key : keys) {
				ShortcutItem sc = scs.get(key);
				values.add(sc.toContentValues());
			}
			count = mResolver.bulkInsert(ShortcutTable.CONTENT_URI, values.toArray(new ContentValues[values.size()]));
		} catch (Exception e) {
			LogUtils.e(TAG, e.getMessage(), e);
		}
		return count;
	}
	
	public ShortcutItem getShortcutItem(long id) {
		ShortcutItem si = new ShortcutItem();
		Cursor c = findShortcutItem(id);
		if (c != null && c.moveToFirst()) {
			si.populate(c, mResources);
			CloseUtils.closeQuietly(c);
		}
		return si;
	}
	
	public ArrayList<ShortcutItem> getShortcutItem(ArrayList<Long> ids) {
		ArrayList<ShortcutItem> sis = new ArrayList<ShortcutItem>();
		Cursor c = findShortcutItem(ids);
		if (c != null) {
			while (c.moveToNext()) {
				ShortcutItem a = new ShortcutItem();
				a.populate(c, mResources);
				sis.add(a);
			}
			CloseUtils.closeQuietly(c);
		}
		return sis;
	}
	
	public ArrayList<ShortcutItem> getAllShortcutItem() {
		ArrayList<ShortcutItem> list = new ArrayList<ShortcutItem>();
		Cursor c = findAllShortcutItem();
		if (c != null) {
			while (c.moveToNext()) {
				ShortcutItem si = new ShortcutItem();
				si.populate(c, mResources);
				list.add(si);
			}
			CloseUtils.closeQuietly(c);
		}
		return list;
	}
	
	public ArrayList<String> getAllShortcutItemPackageName() {
		ArrayList<String> list = new ArrayList<String>();
		Cursor c = findAllShortcutItem();
		if (c != null) {
			while (c.moveToNext()) {
				list.add(c.getString(ShortcutTable.ColumnIndexes.PACKAGE_NAME));
			}
			CloseUtils.closeQuietly(c);
		}
		return list;
	}
	
	public ArrayList<String> getAllShortcutItemComponentName() {
		ArrayList<String> list = new ArrayList<String>();
		Cursor c = findAllShortcutItem();
		if (c != null) {
			while (c.moveToNext()) {
				list.add(c.getString(ShortcutTable.ColumnIndexes.COMPONENT_NAME));
			}
			CloseUtils.closeQuietly(c);
		}
		return list;
	}
	
	private Cursor findShortcutItem(long id) {
		Cursor cursor = null;
		try {
			String where = ShortcutTable.ColumnNames.ID + " = '" + id + "'";
			cursor = mResolver.query(ShortcutTable.CONTENT_URI, ShortcutTable.PROJECTION, where, null, SORT_BY_ORDER);
		} catch (Exception e) {
			LogUtils.e(TAG, e.getMessage(), e);
		}
		return cursor;
	}
	
	private Cursor findShortcutItem(ArrayList<Long> ids) {
		Cursor cursor = null;
		try {
			String where = ShortcutTable.ColumnNames.ID + " IN ('" + TextUtils.join("', '", ids) + "')";
			cursor = mResolver.query(ShortcutTable.CONTENT_URI, ShortcutTable.PROJECTION, where, null, SORT_BY_ORDER);
		} catch (Exception e) {
			LogUtils.e(TAG, e.getMessage(), e);
		}
		return cursor;
	}
	
	public Cursor findAllShortcutItem() {
		Cursor cursor = null;
		try {
			cursor = mResolver.query(ShortcutTable.CONTENT_URI, ShortcutTable.PROJECTION, null, null, SORT_BY_ORDER);
		} catch (Exception e) {
			LogUtils.e(TAG, e.getMessage(), e);
		}
		return cursor;
	}
	
	public int deleteShortcutItem() {
		return mResolver.delete(ShortcutTable.CONTENT_URI, null, null);
	}
	
	public int deleteShortcutItem(ArrayList<ShortcutItem> items) {
		if (items != null && !items.isEmpty()) {
			ArrayList<Integer> deleteIds = new ArrayList<Integer>();
			for (ShortcutItem ii : items) {
				deleteIds.add(ii.getId());
			}
			String where = ShortcutTable.ColumnNames.ID + " IN ('" + TextUtils.join("', '", deleteIds) + "')";
			return mResolver.delete(ShortcutTable.CONTENT_URI, where, null);
		} else {
			LogUtils.w(TAG, "deleteShortcutItem()::items is empty or null");
			return -1;
		}
	}
}