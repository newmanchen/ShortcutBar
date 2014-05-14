package com.newman.shortcutbar.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

import com.newman.shortcutbar.R;
import com.newman.shortcutbar.activity.Setting;
import com.newman.shortcutbar.activity.ShortcutBar;
import com.newman.shortcutbar.util.Constants;
import com.newman.shortcutbar.util.LogUtils;

public class TouchEventMonitorService extends Service {
	private static final String TAG = TouchEventMonitorService.class.getSimpleName();
	public static final String ACTION_SERVICE = "com.newman.shortcutbar.action.START_TOUCH_EVENT_MONITOR_SERVICE";
	
	private ImageView mTouchRegion;
	private GestureDetector mGestureDetector;
	private WindowManager mWindowManager;
	private LayoutParams mParams;
	private boolean mViewAdded = false;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		mGestureDetector = new GestureDetector(this, mOnGestureListener);
		
		mTouchRegion = new ImageView(this);
//		mTouchRegion.setImageResource(android.R.drawable.btn_star);
		mTouchRegion.setImageResource(R.drawable.left_side_bar);
		mTouchRegion.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return mGestureDetector.onTouchEvent(event);
			}
		});
		
		int width = mTouchRegion.getDrawable().getIntrinsicWidth();
		int height = mTouchRegion.getDrawable().getIntrinsicHeight();
		mParams = new WindowManager.LayoutParams(
				width,
				height,
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);

		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		int px = mWindowManager.getDefaultDisplay().getWidth();
		int py = mWindowManager.getDefaultDisplay().getHeight();
		switch (sp.getInt(Setting.PREF_BACKGROUND_LOCATION, Constants.LOCATION_LEFT_CENTER)) {
			case Constants.LOCATION_LEFT_BOTTOM:
				mParams.y = (int) (py * 0.08);
				mParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
				break;
			case Constants.LOCATION_LEFT_CENTER:
				mParams.gravity = Gravity.CENTER | Gravity.LEFT;
				break;
			case Constants.LOCATION_LEFT_TOP:
				mParams.y = (int) (py * 0.08);
				mParams.gravity = Gravity.TOP | Gravity.LEFT;
				break;
			case Constants.LOCATION_RIGHT_BOTTOM:
				mParams.y = (int) (py * 0.08);
				mParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
				mTouchRegion.setImageResource(R.drawable.right_side_bar);
				break;
			case Constants.LOCATION_RIGHT_CENTER:
				mParams.gravity = Gravity.CENTER | Gravity.RIGHT;
				mTouchRegion.setImageResource(R.drawable.right_side_bar);
				break;
			case Constants.LOCATION_RIGHT_TOP:
				mParams.y = (int) (py * 0.08);
				mParams.gravity = Gravity.TOP | Gravity.RIGHT;
				mTouchRegion.setImageResource(R.drawable.right_side_bar);
				break;
		}
		
        mWindowManager.addView(mTouchRegion, mParams);
        mViewAdded = true;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (!mViewAdded) {
			mWindowManager.addView(mTouchRegion, mParams);
			mViewAdded = true;
		}
		return START_STICKY;
	};
	
	OnGestureListener mOnGestureListener = new OnGestureListener() {
		
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			LogUtils.d(TAG, "OnGestureListener :: onSingleTapUp :: ", e.getAction());
			return false;
		}
		
		@Override
		public void onShowPress(MotionEvent e) {
			LogUtils.d(TAG, "OnGestureListener :: onShowPress :: ", e.getAction());
		}
		
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
				float distanceY) {
			LogUtils.d(TAG, "OnGestureListener :: onScroll");
			return false;
		}
		
		@Override
		public void onLongPress(MotionEvent e) {
			LogUtils.d(TAG, "OnGestureListener :: onLongPress :: ", e.getAction());
		}
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			LogUtils.d(TAG, "OnGestureListener :: onFling");
			
			if (Math.abs(e2.getX() - e1.getX()) > 0f) { // from left to right
				LogUtils.d(TAG, "OnGestureListener from ", e1.getX(), " to ", e2.getX());
				Intent i = new Intent(TouchEventMonitorService.this, ShortcutBar.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				TouchEventMonitorService.this.startActivity(i);
				
				mWindowManager.removeViewImmediate(mTouchRegion);
				mViewAdded = false;
			}
			return false;
		}
		
		@Override
		public boolean onDown(MotionEvent e) {
			LogUtils.d(TAG, "OnGestureListener :: onDown :: ", e.getAction());
			return false;
		}
	};
	
	public void onDestroy() {
		if(mWindowManager != null) {
			if(mTouchRegion != null) mWindowManager.removeView(mTouchRegion);
		}
	};
}
