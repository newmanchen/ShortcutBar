package com.newman.shortcutbar.util;

import java.io.ByteArrayOutputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

public class ImageUtils {
	private static final String TAG = ImageUtils.class.getSimpleName();
	private static Map<String, SoftReference<Drawable>> sCachedDrawables = Collections
			.synchronizedMap(new HashMap<String, SoftReference<Drawable>>());

	private static List<String> sCachedDrawablesKey = Collections
			.synchronizedList(new ArrayList<String>());
	
	public synchronized static void clearAllCache() {
		sCachedDrawables.clear();
		sCachedDrawablesKey.clear();
		LogUtils.d(TAG, "all cache are cleared");
	}
	
	public static void addCachedDrawables(String key, Drawable drawable) {
		sCachedDrawables.put(key, new SoftReference<Drawable>(drawable));
		sCachedDrawablesKey.add(key);
		LogUtils.d(TAG, "add cache ", key);
	}
	
	public synchronized static Drawable getCachedImage(Resources res, int resId) {
		Drawable result;
		if (resId == 0) {
			return null;
		} else {
			String key = String.valueOf(resId);
			SoftReference<Drawable> tempReference = sCachedDrawables.get(key);
			result = (null != tempReference) ? tempReference.get() : null;
			
			if (tempReference != null && result == null) {
				sCachedDrawables.remove(key);
				sCachedDrawablesKey.remove(key);
			}
			
			if (result == null) {
				if (res != null) {
					result = res.getDrawable(resId);
					sCachedDrawables.put(key, new SoftReference<Drawable>(result));
					sCachedDrawablesKey.add(key);
				} else {
					LogUtils.w(TAG, "res is null ?!?!?!?");
				}
			}
		}
		return result;
	}
	
	public synchronized static Drawable getCachedImage(String key) {
		Drawable result;
		if (TextUtils.isEmpty(key)) {
			return null;
		} else {
			SoftReference<Drawable> tempReference = sCachedDrawables.get(key);
			result = (null != tempReference) ? tempReference.get() : null;
			
			if (tempReference != null && result == null) {
				sCachedDrawables.remove(key);
				sCachedDrawablesKey.remove(key);
			}
			// No need to add, but need user to add manually
		}
		return result;
	}
	
	public static Bitmap drawable2bitmap(Drawable d, int w, int h) {
		Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);     
		Canvas canvas = new Canvas(bitmap);     
		d.setBounds(0, 0, w, h);     
		d.draw(canvas);
		return bitmap;
	}

	public static Bitmap drawable2bitmap(Drawable d) {
		Bitmap bp = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bp);
		d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
		d.draw(canvas);
		return bp;
	}

	public static byte[] bitmap2byteArray(Bitmap b) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		b.compress(Bitmap.CompressFormat.PNG, 100, out);
		return out.toByteArray();
	}
}
