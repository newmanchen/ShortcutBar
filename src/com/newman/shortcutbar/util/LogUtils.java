package com.newman.shortcutbar.util;

import android.util.Log;

public class LogUtils {
	
	public static void critical(String tag, String msg) {
		LogUtils.v(Constants.APPLICATION_NAME, getBracketTag(tag), msg);
	}

	public static void critical(String tag, String msg, Throwable tr) {
		try {
			LogUtils.v(Constants.APPLICATION_NAME, getBracketTag(tag), msg, tr);
		} catch (Exception e) {
			LogUtils.v(Constants.APPLICATION_NAME, getBracketTag(tag), msg);
		}
	}

	public static void v(String tag, Object ...msgs) {
		if(msgs.length>0) {
			StringBuilder sb = new StringBuilder();
			sb.append(getBracketTag(tag));
			for(Object msg : msgs) {
				sb.append(msg);
			}
			Log.v(Constants.APPLICATION_NAME, sb.toString());
		}
	}

	public static void d(String tag, Object ...msgs) {
		if(msgs.length>0) {
			StringBuilder sb = new StringBuilder();
			sb.append(getBracketTag(tag));
			for(Object msg : msgs) {
				sb.append(msg);
			}
			Log.d(Constants.APPLICATION_NAME, sb.toString());
		}
	}

	public static void i(String tag, Object ...msgs) {
		if(msgs.length>0) {
			StringBuilder sb = new StringBuilder();
			sb.append(getBracketTag(tag));
			for(Object msg : msgs) {
				sb.append(msg);
			}
			Log.i(Constants.APPLICATION_NAME, sb.toString());
		}
	}

	public static void w(String tag, Object ...msgs) {
		if(msgs.length>0) {
			StringBuilder sb = new StringBuilder();
			sb.append(getBracketTag(tag));
			for(Object msg : msgs) {
				sb.append(msg);
			}
			Log.w(Constants.APPLICATION_NAME, sb.toString());
		}
	}

	public static void w(String tag, String msg, Throwable tr) {
		Log.w(Constants.APPLICATION_NAME, getBracketTag(tag) + msg, tr);
	}

	public static void e(String tag, String message) {
		Log.e(Constants.APPLICATION_NAME, getBracketTag(tag) + message);
	}

	public static void e(String tag, String message, Exception e) {
		Log.e(Constants.APPLICATION_NAME, message, e);
	}

	public static void e(String tag, String prefix, String message, Exception e) {
		Log.e(Constants.APPLICATION_NAME, getBracketTag(tag) + prefix);
		Log.e(Constants.APPLICATION_NAME, message, e);
	}

	private static String getBracketTag(String tag) {
		return "<" + tag + "> ";
	}
}