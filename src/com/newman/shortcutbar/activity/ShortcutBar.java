package com.newman.shortcutbar.activity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.app.Activity;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.newman.shortcutbar.R;
import com.newman.shortcutbar.provider.DatabaseHelper;
import com.newman.shortcutbar.receiver.StartTouchEventMonitorService;
import com.newman.shortcutbar.util.ActivityUtils;
import com.newman.shortcutbar.util.Constants;
import com.newman.shortcutbar.util.LogUtils;
import com.newman.shortcutbar.util.MessageUtils;
import com.newman.shortcutbar.vo.ShortcutItem;

public class ShortcutBar extends Activity {

	private static final String TAG = ShortcutBar.class.getSimpleName();
	
	public static final String EXTRA_SKIP_THIS_PAGE_AND_GO_TO_DEVICE_ADMIN = "extra_skip_this_page_and_go_to_device_admin";
	
	private RelativeLayout mNoneShortcutBarRegion;
	private ListView mListView;
	private ShortcutItemAdapter mShortcutItemAdapter;
	private PackageManager mPackageManager;
	private GetAllShortcutItemTask mGetAllShortcutItemTask;
	private DatabaseHelper mDatabaseHelper;
	private boolean mHideShortcutText;
	private SharedPreferences mSharedPreference;
	private ComponentName mDeviceAdminComponent;
	private HandlerThread mThread;
	private UiHandler mUiHandler;
	private NonUiHandler mNonUiHandler;
	private boolean mWifiEnable;
	private boolean mAutoRotateEnable;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	LogUtils.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shortcut_bar);
        
        mDeviceAdminComponent = new ComponentName(this, MyDeviceAdminReceiver.class);
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(ShortcutBar.this);
        startDirectlyToDeviceAdmin(getIntent());
        
        mPackageManager = getPackageManager();
        mDatabaseHelper = new DatabaseHelper(this);
        mHideShortcutText = false;
        
        mThread = new HandlerThread("ShortcutBarService");
		mThread.setDaemon(true);
		mThread.start();
		mNonUiHandler = new NonUiHandler(mThread.getLooper());
		mUiHandler = new UiHandler();
        
        findViews();
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
    	LogUtils.d(TAG, "onNewIntent()");
    	super.onNewIntent(intent);
    	
    	startDirectlyToDeviceAdmin(intent);
    }
    
    @Override
    protected void onResume() {
    	LogUtils.d(TAG, "onResume()");
    	super.onResume();
    	
    	MessageUtils.sendMessage(mNonUiHandler, MSG_NON_UI_CHECK_WIFI_STATE);
    	MessageUtils.sendMessage(mNonUiHandler, MSG_NON_UI_CHECK_AUTO_ROTATE_STATE);
    	doGetAllShortcutItemTask();
    	updateUI();
    }
    
    @Override
    protected void onPause() {
    	LogUtils.d(TAG, "onPause()");
    	super.onPause();
    	sendBroadcast(new Intent(StartTouchEventMonitorService.ACTION_RESTART));
    	
    	// to save the index and top for retaining the list index
    	int firstIndex = mListView.getFirstVisiblePosition();
    	View v = mListView.getChildAt(0);
    	int top = (v == null) ? 0 : v.getTop();
    	LogUtils.d(TAG, "listview first index :: ", firstIndex, " top :: ", top);
    	mSharedPreference.edit().putInt(Setting.PREF_FIRST_INDEX, firstIndex).commit();
    	mSharedPreference.edit().putInt(Setting.PREF_LISTVIEW_TOP, top).commit();
    }
    
    private void startDirectlyToDeviceAdmin(Intent intent) {
    	if (getIntent().getBooleanExtra(EXTRA_SKIP_THIS_PAGE_AND_GO_TO_DEVICE_ADMIN, false)) {
        	startDeviceAdmininstrators(this, mDeviceAdminComponent);
        	this.overridePendingTransition(0, 0);        	
        } else {
        	switch(mSharedPreference.getInt(Setting.PREF_BACKGROUND_LOCATION, Constants.LOCATION_LEFT_CENTER)) {
        	case Constants.LOCATION_LEFT_TOP:
        	case Constants.LOCATION_RIGHT_TOP:
        		this.overridePendingTransition(R.anim.top2bottom, 0);
        		break;
        	case Constants.LOCATION_LEFT_CENTER:
        		this.overridePendingTransition(R.anim.left2right, 0);
        		break;
        	case Constants.LOCATION_RIGHT_CENTER:
        		this.overridePendingTransition(R.anim.right2left, 0);
        		break;
        	case Constants.LOCATION_LEFT_BOTTOM:
        	case Constants.LOCATION_RIGHT_BOTTOM:
        		this.overridePendingTransition(R.anim.bottom2top, 0);
        		break;
        	}
        }
    }
    
    private void doGetAllShortcutItemTask() {
    	if (mGetAllShortcutItemTask == null || mGetAllShortcutItemTask.getStatus() == AsyncTask.Status.FINISHED) {
    		mGetAllShortcutItemTask = new GetAllShortcutItemTask();
		}
    	LogUtils.d(TAG, "task status : ", mGetAllShortcutItemTask.getStatus());
    	if (mGetAllShortcutItemTask.getStatus() != AsyncTask.Status.RUNNING) {
    		mGetAllShortcutItemTask.execute();
		}
    }
    
    private void updateUI() {
    	if (mListView != null) {
    		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ShortcutBar.this);
    		
    		// Opacity and color
    		int percent = sp.getInt(Setting.PREF_BACKGROUND_OPACITY, Constants.DEFAULT_OPACITY); // default
    		LogUtils.d(TAG, "opacity percent is ", percent, " opacity is ", 255*percent/100);
    		int bkgcolor = sp.getInt(Setting.PREF_BACKGROUND_COLOR, Constants.DEFAULT_BACKGROUND_COLOR); // default
    		LogUtils.d(TAG, "background color  is ", bkgcolor);
    		mListView.setBackgroundColor(((255*percent/100) << 24) | (bkgcolor & 0x00ffffff));
    		
    		// Retain the position last user drag to
        	int firstIndex = sp.getInt(Setting.PREF_FIRST_INDEX, 0);
        	int top = sp.getInt(Setting.PREF_LISTVIEW_TOP, 0);
        	LogUtils.d(TAG, "listview first index :: ", firstIndex, " top :: ", top);
    		mListView.setSelectionFromTop(firstIndex, top);
    		
    		// Hide/show shortcut text
    		mHideShortcutText = sp.getBoolean(Setting.PREF_HIDE_SHORTCUT_TEXT, false);
    		
    		// Icon Size
    		LayoutParams layoutPara = mListView.getLayoutParams();
    		int width = mSharedPreference.getInt(Setting.PREF_SHORTCUT_ICON_SIZE, -1);
    		if (width != -1) {
    			layoutPara.width = width + 2*getResources().getDimensionPixelSize(R.dimen.shortcut_item_padding);
    			mListView.setLayoutParams(layoutPara);
    		}
    		
    		// Indicator Location
    		switch (sp.getInt(Setting.PREF_BACKGROUND_LOCATION, Constants.LOCATION_LEFT_CENTER)) {
    		case Constants.LOCATION_LEFT_TOP:
    			mNoneShortcutBarRegion.setGravity(Gravity.TOP | Gravity.LEFT);
    			break;
    		case Constants.LOCATION_LEFT_CENTER:
    			mNoneShortcutBarRegion.setGravity(Gravity.CENTER | Gravity.LEFT);
    			break;
    		case Constants.LOCATION_LEFT_BOTTOM:
    			mNoneShortcutBarRegion.setGravity(Gravity.BOTTOM | Gravity.LEFT);
    			break;
    		case Constants.LOCATION_RIGHT_TOP:
    			mNoneShortcutBarRegion.setGravity(Gravity.TOP | Gravity.RIGHT);
    			break;
    		case Constants.LOCATION_RIGHT_CENTER:
    			mNoneShortcutBarRegion.setGravity(Gravity.CENTER | Gravity.RIGHT);
    			break;
    		case Constants.LOCATION_RIGHT_BOTTOM:
    			mNoneShortcutBarRegion.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
    			break;
    		}
    		
    		if (mShortcutItemAdapter != null) {
    			mShortcutItemAdapter.notifyDataSetChanged();
    		}
    	}
    }
    
    private void findViews() {
    	mNoneShortcutBarRegion = (RelativeLayout) findViewById(R.id.shortcutBar);
    	mNoneShortcutBarRegion.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				LogUtils.d(TAG, "touch on none list region- then close the bar");
				finish();
				return false;
			}
		});
    	mListView = (ListView) findViewById(R.id.listview);
    	mShortcutItemAdapter = new ShortcutItemAdapter(ShortcutBar.this, generatorShortcuts());
    	mListView.setAdapter(mShortcutItemAdapter);
    	mListView.setOnItemClickListener(mShortcutItemAdapter);
    	mListView.setOnItemLongClickListener(mShortcutItemAdapter);
    	mListView.setDividerHeight(0);
    	mListView.setBackgroundColor(0x55FFFFFF);
    }
    
    private class GetAllShortcutItemTask extends AsyncTask<Void, Void, Void> {
    	private ArrayList<ShortcutItem> mItemList;
    	
    	public GetAllShortcutItemTask() {
    		mItemList = new ArrayList<ShortcutItem>();
		}
    	
		@Override
		protected Void doInBackground(Void... params) {
			mItemList = mDatabaseHelper.getAllShortcutItem();
			return null;
		}
		
    	@Override
    	protected void onPostExecute(Void result) {
    		mShortcutItemAdapter.clearItems();
    		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ShortcutBar.this);
    		if (!sp.getBoolean(EnableDisableSetting.PREF_HIDE_SETTING_SHORTCUT, false)) {
    			mShortcutItemAdapter.add(generateSelfShortcut());
    		}
    		if (!sp.getBoolean(EnableDisableSetting.PREF_HIDE_HOME_SHORTCUT, false)) {
    			mShortcutItemAdapter.add(generateHomeShortcut());
    		}
    		for (ShortcutItem si : mItemList) {
    			mShortcutItemAdapter.add(si); 
    		}
    		if (!sp.getBoolean(EnableDisableSetting.PREF_HIDE_ADD_MORE, false)) {
    			mShortcutItemAdapter.add(generateAddMoreShortcut());
    		}
    		if (!sp.getBoolean(EnableDisableSetting.PREF_HIDE_LOCKSCREEN, false)) {
    			mShortcutItemAdapter.add(generateLockScreenShortcut());
    		}
    		if (!sp.getBoolean(EnableDisableSetting.PREF_HIDE_WIFI, false)) {
    			mShortcutItemAdapter.add(generateWifiEnableShortcut());
    		}
    		if (!sp.getBoolean(EnableDisableSetting.PREF_HIDE_AUTO_ROTATE, false)) {
    			mShortcutItemAdapter.add(generateAutoRotateEnableShortcut());
    		}
    		mShortcutItemAdapter.notifyDataSetChanged();
    	}
    }

    private ArrayList<ShortcutItem> generatorShortcuts() {
    	ArrayList<ShortcutItem> array = new ArrayList<ShortcutItem>();
    	// default shortcut
    	// selfApp may be confing page
    	array.add(generateSelfShortcut());
    	// home
    	array.add(generateHomeShortcut());
    	// add more shortcut
    	array.add(generateAddMoreShortcut());
    	// add lockscreen
    	array.add(generateLockScreenShortcut());
    	// quick setting wifi
    	array.add(generateWifiEnableShortcut());
    	// quick setting auto rotate
    	array.add(generateAutoRotateEnableShortcut());
    	return array;
    }
    
    private ShortcutItem generateSelfShortcut() {
    	return new ShortcutItem(this.getPackageName()
    			, this.getApplicationInfo().loadLabel(mPackageManager).toString()
    			, Entry.class.getName()
    			, this.getApplicationInfo().loadIcon(mPackageManager)
    			, ShortcutItem.ITEM_TYPE_APP);
    }
    
    private ShortcutItem generateHomeShortcut() {
    	return new ShortcutItem(this.getPackageName()
    			, getString(R.string.text_home)
    			, getString(R.string.text_home)
    			, getResources().getDrawable(R.drawable.home)
    			, ShortcutItem.ITEM_TYPE_HOME);
    }
    
    private ShortcutItem generateAddMoreShortcut() {
    	return new ShortcutItem(this.getPackageName()
    			, getString(R.string.text_add_more)
    			, getString(R.string.text_add_more)
    			, getResources().getDrawable(R.drawable.add)
    			, ShortcutItem.ITEM_TYPE_ADD);
    }
    
    private ShortcutItem generateLockScreenShortcut() {
    	return new ShortcutItem(this.getPackageName()
    			, getString(R.string.text_lockscreen)
    			, getString(R.string.text_lockscreen)
    			, getResources().getDrawable(R.drawable.lock)
    			, ShortcutItem.ITEM_TYPE_LOCK);
    }
    
    private ShortcutItem generateWifiEnableShortcut() {
    	return new ShortcutItem(this.getPackageName()
    			, getString(R.string.text_wifi)
    			, getString(R.string.text_wifi)
    			, getResources().getDrawable(R.drawable.wifi)
    			, ShortcutItem.ITEM_TYPE_WIFI);
    }
    
    private ShortcutItem generateAutoRotateEnableShortcut() {
    	return new ShortcutItem(this.getPackageName()
    			, getString(R.string.text_auto_rotate)
    			, getString(R.string.text_auto_rotate)
    			, getResources().getDrawable(R.drawable.auto_rotate)
    			, ShortcutItem.ITEM_TYPE_AUTO_ROATE);
    }
    
    private class ShortcutItemAdapter extends ArrayAdapter<ShortcutItem> implements 
    	OnItemClickListener, OnItemLongClickListener {

    	private ArrayList<ShortcutItem> mShortcutList;
    	private LayoutInflater mInflater;
		public ShortcutItemAdapter(Context context, ArrayList<ShortcutItem> shortcutList) {
			super(context, 0, shortcutList);
			this.mShortcutList = shortcutList;
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (null != mShortcutList && position < mShortcutList.size()) {
				ShortcutItem shortcut = mShortcutList.get(position);
				switch(shortcut.getItemType()) {
				case ShortcutItem.ITEM_TYPE_APP:
					convertView = appView(convertView, parent, shortcut);
					break;

				case ShortcutItem.ITEM_TYPE_ADD:
				case ShortcutItem.ITEM_TYPE_LOCK:
				case ShortcutItem.ITEM_TYPE_HOME:
					convertView = otherView(convertView, parent, shortcut);
					break;
				case ShortcutItem.ITEM_TYPE_WIFI:
					convertView = wifiView(convertView, parent, shortcut);
					break;
				case ShortcutItem.ITEM_TYPE_AUTO_ROATE:
					convertView = autoRotateView(convertView, parent, shortcut);
					break;
				}
			} else {
				LogUtils.w(TAG, "getView() wrong position");
			}
			return convertView;
		}
		
		@Override
		public int getViewTypeCount() {
			return ShortcutItem.ITEM_TYPE_SIZE;
		}
		
		@Override
		public int getItemViewType(int position) {
			return mShortcutList.get(position).getItemType();
		}
		
		@Override
		public ShortcutItem getItem(int position) {
			return mShortcutList.get(position);
		}
		
		@Override
		public int getCount() {
			return mShortcutList == null ? 0 : mShortcutList.size();
		}
		
		private View appView(View convertView, ViewGroup parent, ShortcutItem shortcut) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.shortcut_list_item, null);
			}
			
			ImageView imageView = (ImageView) convertView.findViewById(R.id.appIcon);
			imageView.setImageDrawable(shortcut.getAppIcon());
			LayoutParams layoutPara = imageView.getLayoutParams();
			int width_height = mSharedPreference.getInt(Setting.PREF_SHORTCUT_ICON_SIZE, -1);
			if (width_height != -1) {
				layoutPara.width = width_height;
				layoutPara.height = width_height;
				imageView.setLayoutParams(layoutPara);
			}
			
			TextView textView = (TextView) convertView.findViewById(R.id.appName);
			if (mHideShortcutText) {
				textView.setVisibility(View.GONE);
			} else {
				textView.setVisibility(View.VISIBLE);
				textView.setText(shortcut.getAppName());
			}
			
			convertView.setTag(shortcut);
			return convertView;
		}
		
		private View otherView(View convertView, ViewGroup parent, ShortcutItem shortcut) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.shortcut_list_item, null);
			}
			
			ImageView imageView = (ImageView) convertView.findViewById(R.id.appIcon);
			imageView.setImageDrawable(shortcut.getAppIcon());
			LayoutParams layoutPara = imageView.getLayoutParams();
			int width_height = mSharedPreference.getInt(Setting.PREF_SHORTCUT_ICON_SIZE, -1);
			if (width_height != -1) {
				layoutPara.width = width_height;
				layoutPara.height = width_height;
				imageView.setLayoutParams(layoutPara);
			}

			TextView textView = (TextView) convertView.findViewById(R.id.appName);
			if (mHideShortcutText) {
				textView.setVisibility(View.GONE);
			} else {
				textView.setVisibility(View.VISIBLE);
				textView.setText(shortcut.getAppName());	
			}
			convertView.setTag(shortcut);
			return convertView;
		}
		
		private View wifiView(View convertView, ViewGroup parent, ShortcutItem shortcut) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.shortcut_list_item, null);
			}
			
			ImageView imageView = (ImageView) convertView.findViewById(R.id.appIcon);
			LogUtils.d(TAG, "getView()::mWifiEnable::", mWifiEnable);
			if (mWifiEnable) {
				imageView.setImageDrawable(shortcut.getAppIcon());;
			} else {
				imageView.setImageResource(R.drawable.wifi_off);
			}
			LayoutParams layoutPara = imageView.getLayoutParams();
			int width_height = mSharedPreference.getInt(Setting.PREF_SHORTCUT_ICON_SIZE, -1);
			if (width_height != -1) {
				layoutPara.width = width_height;
				layoutPara.height = width_height;
				imageView.setLayoutParams(layoutPara);
			}

			TextView textView = (TextView) convertView.findViewById(R.id.appName);
			if (mHideShortcutText) {
				textView.setVisibility(View.GONE);
			} else {
				textView.setVisibility(View.VISIBLE);
				textView.setText(shortcut.getAppName());	
			}
			convertView.setTag(shortcut);
			return convertView;
		}
		
		private View autoRotateView(View convertView, ViewGroup parent, ShortcutItem shortcut) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.shortcut_list_item, null);
			}
			
			ImageView imageView = (ImageView) convertView.findViewById(R.id.appIcon);
			LogUtils.d(TAG, "getView()::mAutoRotateEnable::", mAutoRotateEnable);
			if (mAutoRotateEnable) {
				imageView.setImageDrawable(shortcut.getAppIcon());
			} else {
				imageView.setImageResource(R.drawable.auto_rotate_disable);
			}
			LayoutParams layoutPara = imageView.getLayoutParams();
			int width_height = mSharedPreference.getInt(Setting.PREF_SHORTCUT_ICON_SIZE, -1);
			if (width_height != -1) {
				layoutPara.width = width_height;
				layoutPara.height = width_height;
				imageView.setLayoutParams(layoutPara);
			}

			TextView textView = (TextView) convertView.findViewById(R.id.appName);
			if (mHideShortcutText) {
				textView.setVisibility(View.GONE);
			} else {
				textView.setVisibility(View.VISIBLE);
				textView.setText(shortcut.getAppName());	
			}
			convertView.setTag(shortcut);
			return convertView;
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
			return false;
		}

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
			ShortcutItem shortcut = getItem(position);
			if (shortcut.getItemType() == ShortcutItem.ITEM_TYPE_APP) {
				Intent intent = ActivityUtils.getPackageIntent(ShortcutBar.this, shortcut);
				ShortcutBar.this.startActivity(intent);
				finish();
			} else if (shortcut.getItemType() == ShortcutItem.ITEM_TYPE_ADD) {
				Intent intent = new Intent(ShortcutBar.this, ApplicationSelector.class);
				ShortcutBar.this.startActivity(intent);
				finish();
			} else if (shortcut.getItemType() == ShortcutItem.ITEM_TYPE_LOCK) {
				DevicePolicyManager dPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
				if (dPM.isAdminActive(mDeviceAdminComponent)) {
					dPM.lockNow();
					finish();
				} else {
					LogUtils.d(TAG, "No Device Admin's permission");
					startDeviceAdmininstrators(ShortcutBar.this, mDeviceAdminComponent);
				}
			} else if (shortcut.getItemType() == ShortcutItem.ITEM_TYPE_WIFI) {
				MessageUtils.sendMessage(mNonUiHandler, MSG_NON_UI_ENABLE_WIFI);
			} else if (shortcut.getItemType() == ShortcutItem.ITEM_TYPE_HOME) {
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			} else if (shortcut.getItemType() == ShortcutItem.ITEM_TYPE_AUTO_ROATE) {
				MessageUtils.sendMessage(mNonUiHandler, MSG_NON_UI_ENABLE_AUTO_ROTATE);
			}
		}
		
		public void clearItems() {
			mShortcutList.clear();
		}
    }
    
    public static class MyDeviceAdminReceiver extends DeviceAdminReceiver {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		LogUtils.d(TAG, "onReceive ?? hihi");
    	}
    	
    	@Override
    	public void onEnabled(Context context, Intent intent) {
    		LogUtils.d(TAG, "enable the device admin");
    	}
    	
    	@Override
    	public void onDisabled(Context context, Intent intent) {
    		LogUtils.d(TAG, "disable the device admin");
    	}
    }
    
    private void startDeviceAdmininstrators(Activity activity, ComponentName deviceAdminComponent) {
		try {
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminComponent);
			activity.startActivityForResult(intent, REQUEST_CODE_LOCKSCREEN);
		} catch (ActivityNotFoundException e) {
			LogUtils.w(TAG, e.getMessage(), e);
		}
	}
    
    private static final int REQUEST_CODE_LOCKSCREEN = 150;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode) {
    	case REQUEST_CODE_LOCKSCREEN:
    		finish();
    		break;
    	}
    }
    
    private static final int MSG_NON_UI_ENABLE_WIFI = 2500;
    private static final int MSG_NON_UI_CHECK_WIFI_STATE = 2600;
    private static final int MSG_NON_UI_ENABLE_AUTO_ROTATE = 2700;
    private static final int MSG_NON_UI_CHECK_AUTO_ROTATE_STATE = 2800;
    private class NonUiHandler extends Handler {
    	public NonUiHandler(Looper looper) {
			super(looper);
		}
    	
    	@Override
    	public void handleMessage(Message msg) {
    		switch (msg.what) {
    		case MSG_NON_UI_ENABLE_WIFI:
    			WifiManager wm = (WifiManager) ShortcutBar.this.getSystemService(WIFI_SERVICE);
				if (wm.isWifiEnabled()) {
					if (wm.setWifiEnabled(false)) {
						mWifiEnable = false;
					} else {
						mWifiEnable = true;
					}
				} else {
					boolean success = wm.setWifiEnabled(true);
					if (success) {
						mWifiEnable = true;
					} else {
						mWifiEnable = false;
					}
				}
				MessageUtils.sendMessage(mUiHandler, MSG_UI_UPDATE_ADAPTER);
    			break;
    		case MSG_NON_UI_CHECK_WIFI_STATE:
    			WifiManager wm1 = (WifiManager) ShortcutBar.this.getSystemService(WIFI_SERVICE);
    			mWifiEnable = wm1.isWifiEnabled();
    			LogUtils.d(TAG, "NonUiHandler::mWifiEnable::", mWifiEnable);
    			MessageUtils.sendMessage(mUiHandler, MSG_UI_UPDATE_ADAPTER);
    			break;
    		case MSG_NON_UI_ENABLE_AUTO_ROTATE:
    			if (setAutoOrientationEnabled(ShortcutBar.this.getContentResolver(), !mAutoRotateEnable)) {
    				mAutoRotateEnable = !mAutoRotateEnable;
    			}
    			MessageUtils.sendMessage(mUiHandler, MSG_UI_UPDATE_ADAPTER);
    			break;
    		case MSG_NON_UI_CHECK_AUTO_ROTATE_STATE:
    			try {
    				mAutoRotateEnable = getAutoOrientationState();
    				LogUtils.d(TAG, "NonUiHandler::mAutoRotateEnable::", mAutoRotateEnable);
    				MessageUtils.sendMessage(mUiHandler, MSG_UI_UPDATE_ADAPTER);
    			} catch (SettingNotFoundException e) {
    				LogUtils.w(TAG, e.getMessage(), e);
    			}
    			break;
    		}
    	}
    }
    
    private static final int MSG_UI_UPDATE_ADAPTER = 5500;
    private class UiHandler extends Handler {
    	@Override
    	public void handleMessage(Message msg) {
    		switch(msg.what) {
    		case MSG_UI_UPDATE_ADAPTER:
    			mShortcutItemAdapter.notifyDataSetChanged();
    			break;
    		}
    	}
    }
    
    private boolean setAutoOrientationEnabled(ContentResolver resolver, boolean enabled) {
		return Settings.System.putInt(resolver, Settings.System.ACCELEROMETER_ROTATION, enabled ? 1 : 0);
	}
    
    private boolean getAutoOrientationState() throws SettingNotFoundException {
    	return (Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION) == 1) ? true : false;
    }
    
    private void setDataConnectionEnable(Context context, boolean enable) {
    	try {
    		final ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			final Class<?> connClass = Class.forName(conn.getClass().getName());
			final Field iConnectivityManagerField = connClass.getDeclaredField("mService");
			iConnectivityManagerField.setAccessible(true);
			final Object iConnectivityManager = iConnectivityManagerField.get(conn);
			final Class<?> iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
		    final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
		    setMobileDataEnabledMethod.setAccessible(true);
		    setMobileDataEnabledMethod.invoke(iConnectivityManager, enable);
		} catch (Exception e) {
			LogUtils.w(TAG, e.getMessage(), e);
		}
    }
}