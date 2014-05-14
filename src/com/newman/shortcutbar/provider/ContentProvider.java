package com.newman.shortcutbar.provider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.newman.shortcutbar.provider.table.ShortcutTable;
import com.newman.shortcutbar.util.LogUtils;

public class ContentProvider extends android.content.ContentProvider {
	private static final String TAG = ContentProvider.class.getSimpleName();

	public static final String AUTHORITY = "com.newman.shortcutbar.provider.authority";
	private static final String DATABASE_NAME = "shortcut.db";
	private static final int DATABASE_VERSION = 2;

	private SQLiteDatabase mDatabase;

	private class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			LogUtils.d(TAG, "DatabaseHelper onCreate");
			db.execSQL(ShortcutTable.getCreateTableSql());
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			LogUtils.d(TAG, "DatabaseHelper onUpgrade, from ", oldVersion, " to ", newVersion);
			db.execSQL("DROP TABLE IF EXISTS " + ShortcutTable.TABLE);
			onCreate(db);
		}
	}

	public boolean onCreate() {
		LogUtils.d(TAG, "provider onCreate");
		DatabaseHelper dbHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION);
		try {
			mDatabase = dbHelper.getWritableDatabase();
		} catch (Exception e) {
			LogUtils.e(TAG, "Can not open database.");
		}
		return (mDatabase == null) ? false : true;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		int count = 0;
		switch (sUriMatcher.match(uri)) {

		case CASE_SHORTCUT:
			count = mDatabase.delete(ShortcutTable.TABLE, where, whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] valuesArray) {
		int count = 0;
		switch (sUriMatcher.match(uri)) {

		case CASE_SHORTCUT:
			count = doDefaultBulkInsert(ShortcutTable.TABLE, null, valuesArray);
			break;
		}
		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowId = 0;
		switch (sUriMatcher.match(uri)) {

		case CASE_SHORTCUT:
			rowId = mDatabase.insert(ShortcutTable.TABLE, null, values);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		if (rowId > 0) {
			Uri rowUri = ContentUris.withAppendedId(uri, rowId);
			getContext().getContentResolver().notifyChange(rowUri, null);
			return rowUri;
		} else {
			throw new SQLException("Failed to insert row into " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor cursor = null;
		switch (sUriMatcher.match(uri)) {

		case CASE_SHORTCUT:
			cursor = mDatabase.query(ShortcutTable.TABLE, projection, selection, selectionArgs, null, null, sortOrder);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		if (cursor != null) {
			// Tell the cursor what uri to watch, so it knows when its source data changes
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
		}
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		int count = 0;
		switch (sUriMatcher.match(uri)) {

		case CASE_SHORTCUT:
			count = mDatabase.update(ShortcutTable.TABLE, values, where, whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}

	private int doDefaultBulkInsert(String table, String nullColumnHack, ContentValues[] valuesArray) {
		int count = 0;
		mDatabase.beginTransaction();
		try {
			for (ContentValues values : valuesArray) {
				mDatabase.insert(table, nullColumnHack, values);
				count++;
			}
			mDatabase.setTransactionSuccessful();
		} catch (Exception e) {
			LogUtils.w(TAG, e.getMessage(), e);
			count = 0;
		} finally {
			mDatabase.endTransaction();
		}
		return count;
	}

	private static final int CASE_SHORTCUT = 100;

	private static UriMatcher sUriMatcher;
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		sUriMatcher.addURI(AUTHORITY, ShortcutTable.TABLE, CASE_SHORTCUT);
	}
}
