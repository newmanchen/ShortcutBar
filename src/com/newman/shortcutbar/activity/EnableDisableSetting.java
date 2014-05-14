package com.newman.shortcutbar.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.TextView;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.newman.shortcutbar.R;
import com.newman.shortcutbar.util.LogUtils;

public class EnableDisableSetting extends PreferenceActivity {
	private static final String TAG = EnableDisableSetting.class.getSimpleName();
	
	public static final String PREF_HIDE_SETTING_SHORTCUT = "hide_setting_shortcut_checkbox_preference";
	public static final String PREF_HIDE_HOME_SHORTCUT = "hide_home_checkbox_preference";
	public static final String PREF_HIDE_ADD_MORE = "hide_add_more_checkbox_preference";
	public static final String PREF_HIDE_LOCKSCREEN = "hide_lockscreen_checkbox_preference";
	public static final String PREF_ACTIVATE_DEACTIVATE_LOCKSCREEN_DEVICE_ADMIN = "activate_deactivate_lockscreen_admin_checkbox_preference";
	public static final String PREF_HIDE_WIFI = "hide_wifi_checkbox_preference";
	public static final String PREF_HIDE_AUTO_ROTATE = "hide_auto_rotate_checkbox_preference";
	
	private CheckBoxPreference mPreferenceHideSetting;
	private CheckBoxPreference mPreferenceHideAddMore;
	private CheckBoxPreference mPreferenceHideLockscreen;
	private CheckBoxPreference mPreferenceHideWifi;
	private CheckBoxPreference mPreferenceHideHome;
	private Preference mPreferenceActivateLockscreenDeviceAdmin;
	private CheckBoxPreference mPreferenceHideAutoRotate;

	private ComponentName mDeviceAdminComponent;	
	private AdView mAdView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.enable_disable);
		addPreferencesFromResource(R.xml.enable_disable);
		
		mDeviceAdminComponent = new ComponentName(this, ShortcutBar.MyDeviceAdminReceiver.class);
		
		findPreference();
		
		mAdView = (AdView) findViewById(R.id.adView);
		AdRequest request = new AdRequest();
		mAdView.loadAd(request);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		evaluateActivateLockscreenState();
	}
	
	@Override
	protected void onDestroy() {
		if (mAdView != null) {
			mAdView.destroy();
		}
		super.onDestroy();
	}
	
	private void findPreference() {
		mPreferenceHideSetting = (CheckBoxPreference) findPreference(PREF_HIDE_SETTING_SHORTCUT);
		mPreferenceHideSetting.setOnPreferenceChangeListener(mOnPreferenceChangeListenerHideSeting);
		mPreferenceHideAddMore = (CheckBoxPreference) findPreference(PREF_HIDE_ADD_MORE);
		mPreferenceHideAddMore.setOnPreferenceChangeListener(mOnPreferenceChangeListenerHideAddMore);
		mPreferenceHideLockscreen = (CheckBoxPreference) findPreference(PREF_HIDE_LOCKSCREEN);
		mPreferenceHideLockscreen.setOnPreferenceChangeListener(mOnPreferenceChangeListenerLockscreen);
		mPreferenceActivateLockscreenDeviceAdmin = (Preference) findPreference(PREF_ACTIVATE_DEACTIVATE_LOCKSCREEN_DEVICE_ADMIN);
		mPreferenceActivateLockscreenDeviceAdmin.setOnPreferenceClickListener(mOnPreferenceClickListenerActivateLockscreenDeviceAdmin);
		mPreferenceHideWifi = (CheckBoxPreference) findPreference(PREF_HIDE_WIFI);
		mPreferenceHideWifi.setOnPreferenceChangeListener(mOnPreferenceChangeListenerHideWifi);
		mPreferenceHideHome = (CheckBoxPreference) findPreference(PREF_HIDE_HOME_SHORTCUT);
		mPreferenceHideHome.setOnPreferenceChangeListener(mOnPreferenceChangeListenerHideHome);
		mPreferenceHideAutoRotate = (CheckBoxPreference) findPreference(PREF_HIDE_AUTO_ROTATE);
		mPreferenceHideAutoRotate.setOnPreferenceChangeListener(mOnPreferenceChangeListenerHideAutoRotate);
		
		evaluateActivateLockscreenState();
	}
	
	OnPreferenceChangeListener mOnPreferenceChangeListenerHideSeting = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			boolean hide = (Boolean) newValue;
			LogUtils.d(TAG, "Hide add more :: ", hide);
			return true; // true for updating viewmOnPreferenceChangeListenerActivateLockscreenDeviceAdmin
		}
	};
	
	OnPreferenceChangeListener mOnPreferenceChangeListenerHideAddMore = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			boolean hide = (Boolean) newValue;
			LogUtils.d(TAG, "Hide add more :: ", hide);
			return true; // true for updating view
		}
	};
	
	OnPreferenceChangeListener mOnPreferenceChangeListenerLockscreen = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			boolean hide = (Boolean) newValue;
			LogUtils.d(TAG, "Hide Lockscreen :: ", hide);
			return true;
		}
	};
	
	OnPreferenceClickListener mOnPreferenceClickListenerActivateLockscreenDeviceAdmin = new OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			Context context = EnableDisableSetting.this;
			DevicePolicyManager dPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
			boolean enableDeviceAdmin = dPM.isAdminActive(mDeviceAdminComponent);
			if(!enableDeviceAdmin) {
				Intent intent = new Intent();
				intent.setClass(context, ShortcutBar.class);
				intent.putExtra(ShortcutBar.EXTRA_SKIP_THIS_PAGE_AND_GO_TO_DEVICE_ADMIN, true);
				context.startActivity(intent);
			} else {
				startConfirmDeactivateDialog();
			}
			return false;
		}
	};
	
	OnPreferenceChangeListener mOnPreferenceChangeListenerHideWifi = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			boolean hide = (Boolean) newValue;
			LogUtils.d(TAG, "Hide Wifi :: ", hide);
			return true;
		}
	};
	
	OnPreferenceChangeListener mOnPreferenceChangeListenerHideHome = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			boolean hide = (Boolean) newValue;
			LogUtils.d(TAG, "Hide Home :: ", hide);
			return true;
		}
	};
	
	OnPreferenceChangeListener mOnPreferenceChangeListenerHideAutoRotate = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			boolean hide = (Boolean) newValue;
			LogUtils.d(TAG, "Hide AutoRotate :: ", hide);
			return true;
		}
	};
	
	private void evaluateActivateLockscreenState() {
		Context context = EnableDisableSetting.this;
		DevicePolicyManager dPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		boolean enableDeviceAdmin = dPM.isAdminActive(mDeviceAdminComponent);
		if (enableDeviceAdmin) {
			mPreferenceActivateLockscreenDeviceAdmin.setTitle(R.string.title_deactivate_lockscreen_admin);
			mPreferenceActivateLockscreenDeviceAdmin.setSummary(R.string.summary_deactivate_lockscreen_admin);
		} else {
			mPreferenceActivateLockscreenDeviceAdmin.setTitle(R.string.title_activate_lockscreen_admin);
			mPreferenceActivateLockscreenDeviceAdmin.setSummary(R.string.summary_activate_lockscreen_admin);
		}
	}
	
	private void startConfirmDeactivateDialog() {
		showDialog(DIALOG_WHAT_VALUE_DEACTIVATE, null);
	}
	
	private static final int DIALOG_WHAT_VALUE_DEACTIVATE = 541;
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		Dialog result = null;
		switch(id) {
		case DIALOG_WHAT_VALUE_DEACTIVATE:
			TextView textView = new TextView(EnableDisableSetting.this);
			int padding = getResources().getDimensionPixelSize(R.dimen.dialog_confirm_text_padding);
			textView.setPadding(padding, padding, padding, padding);
			textView.setTextColor(EnableDisableSetting.this.getResources().getColor(R.color.DialogTextColor));
			textView.setText(R.string.text_confirm_deactivate);
			result = new AlertDialog.Builder(EnableDisableSetting.this)
			.setTitle(R.string.text_title_confirm_deactivate)
			.setView(textView)
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					DevicePolicyManager dPM = (DevicePolicyManager) EnableDisableSetting.this.getSystemService(Context.DEVICE_POLICY_SERVICE);
					dPM.removeActiveAdmin(mDeviceAdminComponent);
					mPreferenceActivateLockscreenDeviceAdmin.setTitle(R.string.title_activate_lockscreen_admin);
					mPreferenceActivateLockscreenDeviceAdmin.setSummary(R.string.summary_activate_lockscreen_admin);
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
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