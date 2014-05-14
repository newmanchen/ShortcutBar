package com.newman.shortcutbar.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.newman.shortcutbar.R;
import com.newman.shortcutbar.service.TouchEventMonitorService;
import com.newman.shortcutbar.util.Constants;
import com.newman.shortcutbar.util.LogUtils;

public class Setting extends PreferenceActivity {
	private static final String TAG = Setting.class.getSimpleName();
	
	public static final String PREF_ENABLE_SERVICE = "enable_service_checkbox_preference";
	public static final String PREF_EDIT_SHORTCUT_ORDER = "edit_shortcut_order";
	public static final String PREF_EDIT_ENABLE_DISABLE = "edit_enable_disable";
	public static final String PREF_HIDE_SHORTCUT_TEXT = "hide_shortcut_text_checkbox_preference";
	public static final String PREF_BACKGROUND_OPACITY = "background_opacity";
	public static final String PREF_BACKGROUND_LOCATION = "background_location";
	public static final String PREF_SHORTCUT_ICON_SIZE = "shorctut_icon_size";
	public static final String PREF_BACKGROUND_COLOR = "shorctut_background_color";
	public static final String PREF_DONATE_ME = "donate_me";
	
	public static final String PREF_FIRST_INDEX = "first_index";
	public static final String PREF_LISTVIEW_TOP = "listview_top";
	public static final String PREF_FIRST_ACTIVATE = "first_activate";
	
	private SharedPreferences mSharedPreferences;
	private CheckBoxPreference mPreferenceEnableService;
	private Preference mPreferenceEditShortcutBar;
	private Preference mPreferenceEditEnableDisable;
	private CheckBoxPreference mPreferenceHideShortcutText;
	private Preference mPreferenceBackgroundOpacity;
	private Preference mPreferenceBackgroundLocation;
	private Preference mPreferenceShortcutIconSize;
	private Preference mPreferenceBackgroundColor;
	private Preference mPreferenceDonateMe;
	
	private int mOpacity;
//	private int mBackgroundColor;
	private int mShortcutIconSize;
	private int mShortcutDefaultIconSzie;
	
	private TextView mTextViewAboutVersion;
	private PackageInfo mPackageInfo;
	private AdView mAdView;
	@Override
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.general_config);
		addPreferencesFromResource(R.xml.general_config);

		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mOpacity = Constants.DEFAULT_OPACITY;  //default
//		mBackgroundColor = Constants.DEFAULT_BACKGROUND_COLOR;
		mShortcutIconSize = mShortcutDefaultIconSzie = getResources().getDimensionPixelSize(Constants.DEFAULT_SHORTCUT_ICON_SIZE_ID);
		try {
			mPackageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			LogUtils.w(TAG, e.getMessage(), e);
		}
		
		findPreference();
		
		mAdView = (AdView) findViewById(R.id.adView);
		AdRequest request = new AdRequest();
//		request.addTestDevice("56D63A77B274B556D00C3D6C4191560C");
//		request.addTestDevice("19C036F0231C3870BF96D723B76E7215");
		mAdView.loadAd(request);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting, menu);
        return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_guide:
			Intent intent = new Intent(Setting.this, Guide.class);
			startActivity(intent);
			break;
		case R.id.menu_changelog:
			Intent i = new Intent(Setting.this, Changelog.class);
			startActivity(i);
			break;
		case R.id.menu_about:
			showDialog(DIALOG_WHAT_VALUE_ABOUT, null);
			break;
		}
		return true;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(Setting.this);
		mOpacity = sp.getInt(PREF_BACKGROUND_OPACITY, Constants.DEFAULT_OPACITY); // default
		mPreferenceBackgroundOpacity.setSummary(mOpacity+"%");
		
		mShortcutIconSize = sp.getInt(PREF_SHORTCUT_ICON_SIZE, mShortcutDefaultIconSzie);
		mPreferenceShortcutIconSize.setSummary(mShortcutIconSize+"px");
		
//		mBackgroundColor = sp.getInt(PREF_BACKGROUND_COLOR, Constants.DEFAULT_BACKGROUND_COLOR);
//		mPreferenceBackgroundColor.setSummary(Integer.toHexString(mBackgroundColor));
		
		String location = getLocationString(sp.getInt(PREF_BACKGROUND_LOCATION, -1));
		if (!TextUtils.isEmpty(location)) {
			mPreferenceBackgroundLocation.setSummary(location);
		}
	}
	
	@Override
	protected void onDestroy() {
		if (mAdView != null) {
			mAdView.destroy();
		}
		super.onDestroy();
	}
	
	private void findPreference() {
		mPreferenceEnableService = (CheckBoxPreference) findPreference(PREF_ENABLE_SERVICE);
		mPreferenceEnableService.setOnPreferenceChangeListener(mOnPreferenceChangeListenerEnableService);
		mPreferenceEditShortcutBar = (Preference) findPreference(PREF_EDIT_SHORTCUT_ORDER);
		mPreferenceEditShortcutBar.setOnPreferenceClickListener(mOnPreferenceClickListenerEditShortcutBar);
		mPreferenceEditEnableDisable = (Preference) findPreference(PREF_EDIT_ENABLE_DISABLE);
		mPreferenceEditEnableDisable.setOnPreferenceClickListener(mOnPreferenceClickListenerEditEnableDisable);
		mPreferenceHideShortcutText = (CheckBoxPreference) findPreference(PREF_HIDE_SHORTCUT_TEXT);
		mPreferenceHideShortcutText.setOnPreferenceChangeListener(mOnPreferenceChangeListenerHideShortcutText);
		mPreferenceBackgroundOpacity = (Preference) findPreference(PREF_BACKGROUND_OPACITY);
		mPreferenceBackgroundOpacity.setOnPreferenceClickListener(mOnPreferenceChangeListenerBackgroundOpacity);
		mPreferenceBackgroundLocation = (Preference) findPreference(PREF_BACKGROUND_LOCATION);
		mPreferenceBackgroundLocation.setOnPreferenceClickListener(mOnPreferenceChangeListenerBackgroundLocation);
		mPreferenceShortcutIconSize = (Preference) findPreference(PREF_SHORTCUT_ICON_SIZE);
		mPreferenceShortcutIconSize.setOnPreferenceClickListener(mOnPreferenceChangeListenerShortcutIconSize);
		mPreferenceBackgroundColor = (Preference) findPreference(PREF_BACKGROUND_COLOR);
//		mPreferenceBackgroundColor.setOnPreferenceClickListener(mOnPreferenceClickListenerBackgroundColor);
		mPreferenceDonateMe = (Preference) findPreference(PREF_DONATE_ME);
		mPreferenceDonateMe.setOnPreferenceClickListener(mOnPreferenceClickListenerDonateMe);

		if (mSharedPreferences.getBoolean(PREF_ENABLE_SERVICE, false)) {
			LogUtils.d(TAG, "enable service : ", true);
			startService();
		} else {
			LogUtils.d(TAG, "enable service : ", false);
		}
	}
	
	OnPreferenceChangeListener mOnPreferenceChangeListenerEnableService = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			boolean enable = (Boolean) newValue;
			LogUtils.d(TAG, "Enable service :: ", enable);
			if (enable) {
				startService();
			} else {
				stopService();
			}
			return true; // true for updating view
		}
	};
	
	OnPreferenceClickListener mOnPreferenceClickListenerEditShortcutBar = new OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			startEditShortcutBarPage();
			return true;
		}
	};
	
	OnPreferenceClickListener mOnPreferenceClickListenerEditEnableDisable = new OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			startEditEnableDisablePage();
			return true;
		}
	};
	
	OnPreferenceClickListener mOnPreferenceChangeListenerBackgroundOpacity = new OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			showDialog(DIALOG_WHAT_VALUE_OPACITY, null);
			return false;
		}
	};
	
	OnPreferenceClickListener mOnPreferenceChangeListenerBackgroundLocation = new OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			showDialog(DIALOG_WHAT_VALUE_LOCATION, null);
			return false;
		}
	};
	
	OnPreferenceClickListener mOnPreferenceChangeListenerShortcutIconSize = new OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			showDialog(DIALOG_WHAT_VALUE_SHORTCUT_ICON_SIZE, null);
			return false;
		}
	};
	
	OnPreferenceChangeListener mOnPreferenceChangeListenerHideShortcutText = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			boolean hide = (Boolean) newValue;
			LogUtils.d(TAG, "Hide shortcut text :: ", hide);
			return true;
		}
	};
	
	OnPreferenceClickListener mOnPreferenceClickListenerDonateMe = new OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.DONATE_URL));
			startActivity(browserIntent);
			return true;
		}
	};
	
//	OnPreferenceClickListener mOnPreferenceClickListenerBackgroundColor = new OnPreferenceClickListener() {
//		@Override
//		public boolean onPreferenceClick(Preference preference) {
//			AmbilWarnaDialog dialog = new AmbilWarnaDialog(Setting.this, mBackgroundColor, new OnAmbilWarnaListener() {
//				@Override
//				public void onOk(AmbilWarnaDialog arg0, int selectedColor) {
//					mBackgroundColor = selectedColor;
//					String hex = Integer.toHexString(mBackgroundColor);
//					LogUtils.d(TAG, "background color ", mBackgroundColor, " in HEX :: ", hex);
//					mPreferenceBackgroundColor.setSummary(hex);
//					
//					SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(Setting.this);
//					sp.edit().putInt(PREF_BACKGROUND_COLOR, mBackgroundColor).commit();
//				}
//
//				@Override
//				public void onCancel(AmbilWarnaDialog arg0) {
//				}
//			});
//			dialog.show();
//			return false;
//		}
//	};
	
	private void startService() {
		if (Setting.this != null) {
			Intent i = new Intent(TouchEventMonitorService.ACTION_SERVICE);
			Setting.this.startService(i);
		}
	}
	
	private void stopService() {
		if (Setting.this != null) {
			Intent i = new Intent(TouchEventMonitorService.ACTION_SERVICE);
			Setting.this.stopService(i);
		}
	}
	
	private void startEditShortcutBarPage() {
		Context context = Setting.this;
		if (context != null) {
			Intent intent = new Intent(context, EditShortcutBar.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
	}
	
	private void startEditEnableDisablePage() {
		Context context = Setting.this;
		if (context != null) {
			Intent intent = new Intent(context, EnableDisableSetting.class);
			context.startActivity(intent);
		}
	}
	
	private String getLocationString(int location) {
		LogUtils.d(TAG, "getLocationString() :: ", location);
		String result = null;
		switch(location) {
		case Constants.LOCATION_LEFT_TOP:
			result = getString(R.string.text_left_top);
			break;
		case Constants.LOCATION_LEFT_CENTER:
			result = getString(R.string.text_left_center);
			break;
		case Constants.LOCATION_LEFT_BOTTOM:
			result = getString(R.string.text_left_bottom);
			break;
		case Constants.LOCATION_RIGHT_TOP:
			result = getString(R.string.text_right_top);
			break;
		case Constants.LOCATION_RIGHT_CENTER:
			result = getString(R.string.text_right_center);
			break;
		case Constants.LOCATION_RIGHT_BOTTOM:
			result = getString(R.string.text_right_bottom);
			break;
		}
		return result;
	}
	
	
	private static final int DIALOG_WHAT_VALUE_ABOUT = 501;
	private static final int DIALOG_WHAT_VALUE_OPACITY = 511;
	private static final int DIALOG_WHAT_VALUE_LOCATION = 521;
	private static final int DIALOG_WHAT_VALUE_SHORTCUT_ICON_SIZE = 531;
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		Dialog result = null;
		switch (id) {
		case DIALOG_WHAT_VALUE_ABOUT:
			View myView = View.inflate(Setting.this, R.layout.about, null);
			mTextViewAboutVersion = (TextView) myView.findViewById(R.id.about_version);
			StringBuilder sb1 = new StringBuilder();
			if (mPackageInfo != null) {
				sb1.append(Setting.this.getString(R.string.about_version)).append(" ").append(mPackageInfo.versionName);
			}
			mTextViewAboutVersion.setText(sb1.toString());
			
			result = new AlertDialog.Builder(Setting.this)
			.setTitle(R.string.menu_about)
			.setView(myView)
			.setCancelable(false)
			.setOnKeyListener(new DialogInterface.OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						dialog.dismiss();
					}
					return false;
				}
			})
			.create();
			break;
		case DIALOG_WHAT_VALUE_OPACITY:
			View myOpacityView = View.inflate(Setting.this, R.layout.background_opacity_setting, null); 
			final SeekBar seekBarBackgroundOpacity = (SeekBar) myOpacityView.findViewById(R.id.background_opacity_seekbar);
			final TextView textViewBackgroundOpacity = (TextView) myOpacityView.findViewById(R.id.progress_text);
			textViewBackgroundOpacity.setText(String.valueOf(mOpacity));
			LogUtils.d(TAG, "background opacity :: ", mOpacity);
			seekBarBackgroundOpacity.setProgress(mOpacity);
			seekBarBackgroundOpacity.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					textViewBackgroundOpacity.setText(String.valueOf(progress));
				}
			});

			result = new AlertDialog.Builder(Setting.this)
			.setTitle(R.string.title_background_opacity)
			.setView(myOpacityView)
			.setCancelable(false)
			.setOnKeyListener(new DialogInterface.OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						dialog.dismiss();
					}
					return false;
				}
			})
			.setPositiveButton(android.R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (seekBarBackgroundOpacity != null) {
						mOpacity = seekBarBackgroundOpacity.getProgress();
						LogUtils.d(TAG, "background opacity :: ", mOpacity);
						mPreferenceBackgroundOpacity.setSummary(mOpacity + "%");
						SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(Setting.this);
						sp.edit().putInt(PREF_BACKGROUND_OPACITY, mOpacity).commit();
					}
				}
			})
			.create();
			break;
			
		case DIALOG_WHAT_VALUE_LOCATION:
			final String[] items = new String[] 
					{getString(R.string.text_left_top), getString(R.string.text_left_center), getString(R.string.text_left_bottom)
					, getString(R.string.text_right_top), getString(R.string.text_right_center), getString(R.string.text_right_bottom)}; 
			result = new AlertDialog.Builder(Setting.this)
			.setTitle(R.string.title_indicator_location)
			.setItems(items, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(Setting.this);
					mPreferenceBackgroundLocation.setSummary(items[which]);
					if (sp.getBoolean(PREF_ENABLE_SERVICE, false)) {
						stopService();
						startService();
					}
					
					int location = Constants.LOCATION_LEFT_CENTER;
					switch(which) {
					case 0:
						location = Constants.LOCATION_LEFT_TOP;
						break;
					case 1:
						location = Constants.LOCATION_LEFT_CENTER;
						break;
					case 2:
						location = Constants.LOCATION_LEFT_BOTTOM;
						break;
					case 3:
						location = Constants.LOCATION_RIGHT_TOP;
						break;
					case 4:
						location = Constants.LOCATION_RIGHT_CENTER;
						break;
					case 5:
						location = Constants.LOCATION_RIGHT_BOTTOM;
						break;
					}
					
					sp.edit().putInt(PREF_BACKGROUND_LOCATION, location).commit();
				}
			})
			.create();
			break;
			
		case DIALOG_WHAT_VALUE_SHORTCUT_ICON_SIZE:
			View myShortcutIconView = View.inflate(Setting.this, R.layout.shortcut_icon_size_setting, null); 
			final SeekBar seekBarShortcutIconSzie = (SeekBar) myShortcutIconView.findViewById(R.id.shortcut_icon_size_seekbar);
			final TextView textViewShortcutIconSzie = (TextView) myShortcutIconView.findViewById(R.id.progress_text);
			textViewShortcutIconSzie.setText(String.valueOf(mShortcutIconSize));
			LogUtils.d(TAG, "shortcut icon size :: ", mShortcutIconSize);
			seekBarShortcutIconSzie.setMax(mShortcutDefaultIconSzie);
			seekBarShortcutIconSzie.setProgress(mShortcutIconSize);
			seekBarShortcutIconSzie.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					textViewShortcutIconSzie.setText(String.valueOf(progress));
				}
			});

			result = new AlertDialog.Builder(Setting.this)
			.setTitle(R.string.title_shortcut_icon_size)
			.setView(myShortcutIconView)
			.setCancelable(false)
			.setOnKeyListener(new DialogInterface.OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						dialog.dismiss();
					}
					return false;
				}
			})
			.setPositiveButton(android.R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (seekBarShortcutIconSzie != null) {
						mShortcutIconSize = seekBarShortcutIconSzie.getProgress();
						mPreferenceShortcutIconSize.setSummary(mShortcutIconSize + "px");
						SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(Setting.this);
						sp.edit().putInt(PREF_SHORTCUT_ICON_SIZE, mShortcutIconSize).commit();
					}
				}
			})
			.create();
			break;
		}
		if (result != null) {
			result.setCanceledOnTouchOutside(true);
		}
		return result;
	}
}