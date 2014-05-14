package com.newman.shortcutbar.vo;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.newman.shortcutbar.provider.table.ShortcutTable;
import com.newman.shortcutbar.provider.table.ShortcutTable.ColumnNames;
import com.newman.shortcutbar.util.ImageUtils;

public class ShortcutItem {
	public static final int ITEM_TYPE_APP = 0;
	public static final int ITEM_TYPE_HOME = 1;
	public static final int ITEM_TYPE_ADD = 2;
	public static final int ITEM_TYPE_LOCK = 3;
	public static final int ITEM_TYPE_WIFI = 4;
	public static final int ITEM_TYPE_AUTO_ROATE = 5;
	public static final int ITEM_TYPE_SIZE = 6;
	
	private int Id;
	private String PackageName;
	private String AppName;
	private String ComponentName;
	private Drawable AppIcon;
	private int ItemType;
	private long AddTimestamp;
	private int Order;
	
	public ShortcutItem() {
	}
	
	public ShortcutItem(String pkgName, String appName, String componentName, Drawable appIcon, int itemType) {
		this.PackageName = pkgName;
		this.AppName = appName;
		this.ComponentName = componentName;
		this.AppIcon = appIcon;
		this.ItemType = itemType;
		this.AddTimestamp = System.currentTimeMillis();
		this.Order = -1;
	}
	
	public ShortcutItem(String pkgName, String appName, String componentName, Drawable appIcon, int itemType, long addTimestamp) {
		this.PackageName = pkgName;
		this.AppName = appName;
		this.ComponentName = componentName;
		this.AppIcon = appIcon;
		this.ItemType = itemType;
		this.AddTimestamp = addTimestamp;
		this.Order = -1;
	}
	
	public int getId() {
		return Id;
	}

	@SuppressWarnings("unused")
	private void setId(int id) {
		// cannot be set
		Id = id;
	}
	
	public String getKey() {
		return this.PackageName + ":" + this.ComponentName;
	}
	
	public String getPackageName() {
		return PackageName;
	}
	
	public void setPackageName(String packageName) {
		PackageName = packageName;
	}
	
	public String getAppName() {
		return AppName;
	}
	
	public void setAppName(String appName) {
		AppName = appName;
	}
	
	public String getComponentName() {
		return ComponentName;
	}
	
	public void setComponentName(String component) {
		ComponentName = component;
	}
	
	public Drawable getAppIcon() {
		return AppIcon;
	}
	
	public void setAppIcon(Drawable appIcon) {
		AppIcon = appIcon;
	}
	
	public int getItemType() {
		return ItemType;
	}

	public void setItemType(int itemType) {
		ItemType = itemType;
	}
	
	public long getAddTimestamp() {
		return AddTimestamp;
	}

	public void setAddTimestamp(long addTimestamp) {
		AddTimestamp = addTimestamp;
	}
	
	public int getOrder() {
		return Order;
	}
	
	public void setOrder(int order) {
		Order = order;
	}
	
	public void populate(Cursor c, Resources res) {
		this.Id = c.getInt(ShortcutTable.ColumnIndexes.ID);
		this.PackageName = c.getString(ShortcutTable.ColumnIndexes.PACKAGE_NAME);
		this.AppName = c.getString(ShortcutTable.ColumnIndexes.APPLICATION_NAME);
		this.ComponentName = c.getString(ShortcutTable.ColumnIndexes.COMPONENT_NAME);
		byte[] bytes = c.getBlob(ShortcutTable.ColumnIndexes.APP_ICON);
		this.AppIcon = new BitmapDrawable(res, BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
		this.ItemType = c.getInt(ShortcutTable.ColumnIndexes.ITEM_TYPE);
		this.AddTimestamp = c.getLong(ShortcutTable.ColumnIndexes.ADD_TIMESTAMP);
		this.Order = c.getInt(ShortcutTable.ColumnIndexes.LIST_ORDER);
	}
	
	public ContentValues toContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(ColumnNames.PACKAGE_NAME, this.PackageName);
		cv.put(ColumnNames.APPLICATION_NAME, this.AppName);
		cv.put(ColumnNames.COMPONENT_NAME, this.ComponentName);
		byte[] bytes = ImageUtils.bitmap2byteArray(ImageUtils.drawable2bitmap(this.AppIcon));
		if (bytes != null) {
			cv.put(ColumnNames.APP_ICON, bytes);
		}
		cv.put(ColumnNames.ADD_TIMESTAMP, this.AddTimestamp);
		cv.put(ColumnNames.ITEM_TYPE, this.ItemType);
		cv.put(ColumnNames.LIST_ORDER, this.Order);
		return cv;
	}
}