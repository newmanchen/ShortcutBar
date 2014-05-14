package com.newman.shortcutbar.util;

import android.os.Handler;

public class MessageUtils {
	public static void sendMessage(Handler handler, int what) {
		if (handler != null) {
			handler.sendEmptyMessage(what);
		}
	}
}
