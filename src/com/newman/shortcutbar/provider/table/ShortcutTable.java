package com.newman.shortcutbar.provider.table;

import android.net.Uri;

import com.newman.shortcutbar.provider.ContentProvider;

public class ShortcutTable {
	public static final String TABLE = "shortcut";
	public static final Uri CONTENT_URI = Uri.parse("content://" + ContentProvider.AUTHORITY + "/" + TABLE);
	
	public static String getCreateTableSql() {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ").append(TABLE).append("(");
		sb.append(ColumnNames.ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
		sb.append(ColumnNames.PACKAGE_NAME).append(" LONG NOT NULL,");
		sb.append(ColumnNames.APPLICATION_NAME).append(" TEXT NOT NULL,");
		sb.append(ColumnNames.COMPONENT_NAME).append(" TEXT NOT NULL,");
		sb.append(ColumnNames.APP_ICON).append(" BLOB NOT NULL,");
		sb.append(ColumnNames.ITEM_TYPE).append(" INTEGER,");
		sb.append(ColumnNames.ADD_TIMESTAMP).append(" LONG DEFAULT 0,");
		sb.append(ColumnNames.LIST_ORDER).append(" INTEGER");
		sb.append(");");
		return sb.toString();
	}
	
	public static final class ColumnNames {
		public static final String ID = "_id";
		public static final String PACKAGE_NAME = "package_name";
		public static final String APPLICATION_NAME = "appliction_name";
		public static final String COMPONENT_NAME = "component_name";
		public static final String APP_ICON = "app_icon";
		public static final String ITEM_TYPE = "item_type";
		public static final String ADD_TIMESTAMP = "add_timestamp";
		public static final String LIST_ORDER = "list_order";
	}

	public static final String[] PROJECTION = {
		ColumnNames.ID,
		ColumnNames.PACKAGE_NAME,
		ColumnNames.APPLICATION_NAME,
		ColumnNames.COMPONENT_NAME,
		ColumnNames.APP_ICON,
		ColumnNames.ITEM_TYPE,
		ColumnNames.ADD_TIMESTAMP,
		ColumnNames.LIST_ORDER
	};

	public static final class ColumnIndexes {
		private static int index = 0;
		public static final int ID = index++;
		public static final int PACKAGE_NAME = index++;
		public static final int APPLICATION_NAME = index++;
		public static final int COMPONENT_NAME = index++;
		public static final int APP_ICON = index++;
		public static final int ITEM_TYPE = index++;
		public static final int ADD_TIMESTAMP = index++;
		public static final int LIST_ORDER = index++;
	}
}