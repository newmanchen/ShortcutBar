package com.newman.shortcutbar.activity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.newman.shortcutbar.R;
import com.newman.shortcutbar.provider.DatabaseHelper;
import com.newman.shortcutbar.util.ImageUtils;
import com.newman.shortcutbar.util.LogUtils;
import com.newman.shortcutbar.vo.ShortcutItem;

public class ApplicationSelector extends Activity {
	private static final String TAG = ApplicationSelector.class.getSimpleName();
	
	// Views
	private ListView mListView;
	private Button mButtonOk;
	private Button mButtonCancel;
	private EditText mEditTextSearch;
	// Objects
	private LoadApplicationsTask mLoadApplicationsTask;
	private AppSelectorAdapter mAppSelectorAdapter;
	private PackageManager mPackageManager;
	private DatabaseHelper mDatabaseHelper;
	private LinkedHashMap<String, ShortcutItem> mSelectedMap;
	private int mExistShortcutCount;
	private String mSearchText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_selector_list);
		
		mExistShortcutCount = 0;
		mPackageManager = getPackageManager();
		mDatabaseHelper = new DatabaseHelper(this);
		mSelectedMap = new LinkedHashMap<String, ShortcutItem>();
		
		findViews();
	}
	
	private void findViews() {
		// search bar
		mEditTextSearch = (EditText) findViewById(R.id.search_input);
		mEditTextSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				mSearchText = s.toString();
				mAppSelectorAdapter.updateSearchKey(mSearchText);
			}
		});
		// list view
		mListView = (ListView) findViewById(R.id.listview);
		mAppSelectorAdapter = new AppSelectorAdapter(ApplicationSelector.this, new ArrayList<ActivityInfo>());
    	mListView.setAdapter(mAppSelectorAdapter);
    	mListView.setOnItemClickListener(mAppSelectorAdapter);
    	//footer
    	mButtonOk = (Button) findViewById(R.id.footer_btn_ok);
    	mButtonOk.setEnabled(false);
    	mButtonOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//FIXME may cause ANR, progressing dialog to hint?
				Set<String> keys = mSelectedMap.keySet();
				int temp = mExistShortcutCount;
				for (String key : keys) {
					ShortcutItem item = mSelectedMap.get(key);
					item.setOrder(temp);
					temp++;
				}
				mDatabaseHelper.addShortcutItem(mSelectedMap);
				finish();
			}
		});
    	mButtonCancel = (Button) findViewById(R.id.footer_btn_cancel);
    	mButtonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		doLoadApplicationTask();
	}
	
	private void doLoadApplicationTask() {
    	if (mLoadApplicationsTask == null || mLoadApplicationsTask.getStatus() == AsyncTask.Status.FINISHED) {
    		mLoadApplicationsTask = new LoadApplicationsTask();
		}
    	LogUtils.d(TAG, "task status : ", mLoadApplicationsTask.getStatus());
    	if (mLoadApplicationsTask.getStatus() != AsyncTask.Status.RUNNING) {
    		mLoadApplicationsTask.execute();
		}
    }
	
	private class LoadApplicationsTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			ArrayList<String> mExistedShortcutList = mDatabaseHelper.getAllShortcutItemComponentName();
			mExistShortcutCount = mExistedShortcutList.size();
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			List<ResolveInfo> a = mPackageManager.queryIntentActivities(intent, 0);
			LogUtils.d(TAG, "a size is ", a.size());
			List<ActivityInfo> bb = new ArrayList<ActivityInfo>();
			for (ResolveInfo ri : a) {
				if (!mExistedShortcutList.contains(ri.activityInfo.name)) {
					bb.add(ri.activityInfo);
				}
			}
			mAppSelectorAdapter.addActivityInfos(bb);
			mAppSelectorAdapter.addActivityInforsForCached(bb);
//			mAppSelectorAdapter.addActivityInfos(mPackageManager.getInstalledApplications(0));
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			mAppSelectorAdapter.notifyDataSetChanged();
		}
	}
	
	private class AppSelectorAdapter extends ArrayAdapter<ActivityInfo> implements 
		OnItemClickListener {

		private List<ActivityInfo> mInstalledAppList = new ArrayList<ActivityInfo>();
		private List<ActivityInfo> mCachedInstalledAppList = new ArrayList<ActivityInfo>();
		private LayoutInflater mInflater;
		
		public AppSelectorAdapter(Context context, List<ActivityInfo> list) {
			super(context, 0, list);
			this.mCachedInstalledAppList.addAll(list);
			mInstalledAppList = list; 
			mInflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (mInstalledAppList != null && mInstalledAppList.size() > 0) {
				ActivityInfo info = getItem(position);
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.app_selector_list_item, null);
				}

				ImageView imageView = (ImageView) convertView.findViewById(R.id.appImage);
				String pkgName = info.packageName;
				Drawable d = ImageUtils.getCachedImage(pkgName);
				if (d == null) {
					d = info.loadIcon(mPackageManager);
					ImageUtils.addCachedDrawables(pkgName, d);
				}
				imageView.setImageDrawable(d);
				
				TextView textView = (TextView) convertView.findViewById(R.id.appLabel);
				textView.setText(info.loadLabel(mPackageManager));
				CheckBox checkbox = (CheckBox) convertView.findViewById(R.id.check_box);
				checkbox.setChecked(mSelectedMap.containsKey(info.packageName+":"+info.name));
			} else {
				LogUtils.w(TAG, "getView() wrong position");
			}
			return convertView;
		}
		
		@Override
		public ActivityInfo getItem(int position) {
			return mInstalledAppList.get(position);
		}
		
		public ShortcutItem getShortcutItem(int position) {
			ActivityInfo info = getItem(position);
			return new ShortcutItem(info.packageName
					, info.loadLabel(mPackageManager).toString()
					, info.name
					, info.loadIcon(mPackageManager)
					, ShortcutItem.ITEM_TYPE_APP);
		}
		
		public void addActivityInforsForCached(List<ActivityInfo> infos) {
			mCachedInstalledAppList.clear();
			mCachedInstalledAppList.addAll(infos);
			LogUtils.d(TAG, "mCachedInstalledAppList size is ", mCachedInstalledAppList.size());
		}
		
		public void addActivityInfos(List<ActivityInfo> infos) {
			mInstalledAppList.clear();
			mInstalledAppList.addAll(infos);
		}
		
		@SuppressWarnings("unused")
		public void addActivityInfo(ActivityInfo info) {
			mInstalledAppList.add(info);
		}
		
		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
			ShortcutItem item = getShortcutItem(position);
			if (!mSelectedMap.containsKey(item.getKey())) {
				mSelectedMap.put(item.getKey(), item);
			} else {
				mSelectedMap.remove(item.getKey());
			}
			notifyDataSetChanged();
			enableFooter();
		}
		
		private void enableFooter() {
			if (mSelectedMap != null && mSelectedMap.size() > 0) {
				mButtonOk.setEnabled(true);
			} else {
				mButtonOk.setEnabled(false);
			}
		}
		
		public void updateSearchKey(String key) {
			LogUtils.d(TAG, "key is ", key);
			LogUtils.d(TAG, "mCachedInstalledAppList size is ", mCachedInstalledAppList.size());
			if (!TextUtils.isEmpty(key)) {
				String noCaseKey = key.toLowerCase();
				mInstalledAppList.clear();
				ArrayList<ActivityInfo> temp = new ArrayList<ActivityInfo>();
				for (ActivityInfo info : mCachedInstalledAppList) {
					String applabel = info.loadLabel(mPackageManager).toString();
					String noCaseLabel = applabel.toLowerCase();
					if (noCaseLabel.contains(noCaseKey)) {
						LogUtils.d(TAG, "app noCaseLabel ", noCaseLabel, " contains the noCaseKey ", noCaseKey);
						LogUtils.d(TAG, "app label ", applabel, " contains the key ", key);
						temp.add(info);						
					}
				}
				LogUtils.d(TAG, "temp size is ", temp.size());
				mInstalledAppList.addAll(temp);
			} else {
				mInstalledAppList.clear();
				mInstalledAppList.addAll(mCachedInstalledAppList);
			}
			notifyDataSetChanged();
		}
	}
}