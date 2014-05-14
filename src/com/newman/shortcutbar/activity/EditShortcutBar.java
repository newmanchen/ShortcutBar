package com.newman.shortcutbar.activity;

import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;
import com.newman.shortcutbar.R;
import com.newman.shortcutbar.provider.DatabaseHelper;
import com.newman.shortcutbar.provider.table.ShortcutTable;
import com.newman.shortcutbar.util.ImageUtils;
import com.newman.shortcutbar.util.LogUtils;
import com.newman.shortcutbar.util.ShortcutItemCompare;
import com.newman.shortcutbar.vo.ShortcutItem;

public class EditShortcutBar extends FragmentActivity {
	private static final String TAG = EditShortcutBar.class.getSimpleName();

	// View
	private DragSortListView mListView;
	private Button mButtonOk;
	private Button mButtonCancel;
	private MyDialog mMyDialog;
	// Object
	private DragSortController mDragSortController;
	private ShortcutAdapter mShortcutAdapter;
	private DatabaseHelper mDatabaseHelper;
	private ContentObserver mContentObserverShortcutTable;
	private NonUiHandler mNonUiHandler;
	private boolean mOnPause;
	private boolean mDirtyFlag;
	private boolean mOrderHasChanged;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_shortcut_bar);

		mOnPause = false;
		mOrderHasChanged = false;
		mDatabaseHelper = new DatabaseHelper(EditShortcutBar.this);

		HandlerThread thread = new HandlerThread(TAG);
		thread.setPriority(Thread.NORM_PRIORITY);
		thread.start();
		mNonUiHandler = new NonUiHandler(thread.getLooper());
		mContentObserverShortcutTable = new ContentObserver(mNonUiHandler) {
			public void onChange(boolean selfChange) {
				LogUtils.d(TAG, "onChange: ", selfChange, " , mOnPause: ", mOnPause);
				if (mOnPause) {
					mDirtyFlag = true;
				}
			}
		};
		getContentResolver().registerContentObserver(ShortcutTable.CONTENT_URI, true, mContentObserverShortcutTable);
		findViews();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mOnPause = false;
		if (mDirtyFlag) {
			mNonUiHandler.sendEmptyMessage(MESSAGE_REFETCH_SHORTCUT_LIST);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mOnPause = true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		getContentResolver().unregisterContentObserver(mContentObserverShortcutTable);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.edit_shortcut_bar, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_add:
			Intent intent = new Intent(EditShortcutBar.this, ApplicationSelector.class);
			EditShortcutBar.this.startActivity(intent);
			break;
		}
		return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean propagate = false;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			startConfirmLeavingDialog();
			propagate = true;
		}
		return propagate;
	}

	private void findViews() {
		mButtonOk = (Button) findViewById(R.id.footer_btn_ok);
		mButtonOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOrderHasChanged) {
					int deleteCount = mDatabaseHelper.deleteShortcutItem();
					LogUtils.d(TAG, "onClickListenerOk :: delete ", deleteCount, "items in database");  
					mDatabaseHelper.addShortcutItem(mShortcutAdapter.getList());
				}
				finish();
			}
		});
		mButtonCancel = (Button) findViewById(R.id.footer_btn_cancel);
		mButtonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startConfirmLeavingDialog();
			}
		});

		ArrayList<ShortcutItem> list = mDatabaseHelper.getAllShortcutItem();
		mShortcutAdapter = new ShortcutAdapter(EditShortcutBar.this, list);
		mListView = (DragSortListView) findViewById(R.id.listview);
		mListView.setAdapter(mShortcutAdapter);
		mDragSortController = buildController(mListView);
		mListView.setFloatViewManager(mDragSortController);
		mListView.setOnTouchListener(mDragSortController);
		mListView.setDropListener(mDropListener);
	}
	
	public DragSortController buildController(DragSortListView dslv) {
        DragSortController controller = new DragSortController(dslv);
        controller.setDragHandleId(R.id.handle_icon);
        controller.setDragInitMode(DragSortController.ON_DOWN);
        controller.setBackgroundColor(0x55000000);
        return controller;
    }
	
	private DropListener mDropListener = new DropListener() {
		@Override
		public void drop(int from, int to) {
			mShortcutAdapter.onDrop(from, to);
		}
	};
	
	private void startConfirmLeavingDialog() {
		if (mMyDialog == null) {
			mMyDialog = new MyDialog();
		}
		Bundle b = new Bundle();
		b.putInt(DIALOG_EXTRA_WHAT, DIALOG_WHAT_VALUE_CONFIRM);
		mMyDialog.setArguments(b);
		mMyDialog.show(getSupportFragmentManager(), TAG_CONFIRM_DIALOG);
	}

	private static final String TAG_CONFIRM_DIALOG = "TAG_CONFIRM_DIALOG";
	private static final String DIALOG_EXTRA_WHAT = "dialog_extra_what";

	private static final int DIALOG_WHAT_VALUE_CONFIRM = 501;
	private class MyDialog extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Dialog result = null;
			int id = getArguments().getInt(DIALOG_EXTRA_WHAT);
			switch (id) {
			case DIALOG_WHAT_VALUE_CONFIRM:
				TextView textView = new TextView(EditShortcutBar.this);
				int padding = getResources().getDimensionPixelSize(R.dimen.dialog_confirm_text_padding);
				textView.setPadding(padding, padding, padding, padding);
				textView.setTextColor(EditShortcutBar.this.getResources().getColor(R.color.DialogTextColor));
				textView.setText(R.string.text_confirm_leaving);
				result = new AlertDialog.Builder(EditShortcutBar.this)
				.setTitle(R.string.text_title_confirm_leaving)
				.setView(textView)
				.setCancelable(false)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mMyDialog.dismiss();
						EditShortcutBar.this.finish();
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mMyDialog.dismiss();
					}
				})
				.create();
				break;
			}
			return result;
		}
	}

	private static final int MESSAGE_REFETCH_SHORTCUT_LIST = 500;
	private class NonUiHandler extends Handler {
		public NonUiHandler(Looper looper) {
			super(looper);
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_REFETCH_SHORTCUT_LIST:
				LogUtils.d(TAG, "Receive MESSAGE_REFETCH_SHORTCUT_LIST");
				if (!mOnPause) {
					final ArrayList<ShortcutItem> list = mDatabaseHelper.getAllShortcutItem();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mShortcutAdapter.clearShortcutList();
							mShortcutAdapter.addShortcutList(list);
							mShortcutAdapter.notifyDataSetChanged();
						}
					});
					mDirtyFlag = false;
				}
				break;
			}
		}
	}

	private class ShortcutAdapter extends ArrayAdapter<ShortcutItem> {

		private ArrayList<ShortcutItem> mShortcutList;
		private LayoutInflater mInflater;
		public ShortcutAdapter(Context context, ArrayList<ShortcutItem> shortcutList) {
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
				}
			} else {
				LogUtils.w(TAG, "getView() wrong position");
			}
			return convertView;
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
				convertView = mInflater.inflate(R.layout.edit_shortcut_list_item, null);
			}

			TextView textView = (TextView) convertView.findViewById(R.id.appLabel);
			ImageView imageView = (ImageView) convertView.findViewById(R.id.appImage);

			textView.setText(shortcut.getAppName());
			Drawable d = ImageUtils.getCachedImage(shortcut.getPackageName());
			if (d == null) {
				d = shortcut.getAppIcon();
				ImageUtils.addCachedDrawables(shortcut.getPackageName(), d);
			}
			imageView.setImageDrawable(d);

//			ImageButton up = (ImageButton) convertView.findViewById(R.id.up_button);
//			ImageButton down = (ImageButton) convertView.findViewById(R.id.down_button);
			ImageButton remove = (ImageButton) convertView.findViewById(R.id.remove_button);
			final int pos = getPosition(shortcut);
//			if (pos == 0) {
//				up.setVisibility(View.INVISIBLE);
//			} else {
//				up.setVisibility(View.VISIBLE);
//				up.setOnClickListener(new OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						mOrderHasChanged = true;
//						synchronized(mShortcutList) {
//							ShortcutItem curItem = mShortcutList.get(pos);
//							ShortcutItem lastItem = mShortcutList.get(pos-1);
//							int curO = curItem.getOrder();
//							int lastO = lastItem.getOrder();
//							curItem.setOrder(lastO);
//							lastItem.setOrder(curO);
//							Collections.sort(mShortcutList, new ShortcutItemCompare());
//						}
//						notifyDataSetChanged();
//					}
//				});
//			}
//			if (pos == getCount()-1) {
//				down.setVisibility(View.INVISIBLE);
//			} else {
//				down.setVisibility(View.VISIBLE);
//				down.setOnClickListener(new OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						mOrderHasChanged = true;
//						synchronized(mShortcutList) {
//							ShortcutItem curItem = mShortcutList.get(pos);
//							ShortcutItem nextItem = mShortcutList.get(pos+1);
//							int curO = curItem.getOrder();
//							int nextO = nextItem.getOrder();
//							curItem.setOrder(nextO);
//							nextItem.setOrder(curO);
//							Collections.sort(mShortcutList, new ShortcutItemCompare());
//						}
//						notifyDataSetChanged();
//					}
//				});
//			}
			remove.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mOrderHasChanged = true;
					synchronized(mShortcutList) {
						mShortcutList.remove(pos);
						int size = mShortcutList.size(); 
						for (int i = pos; i < size ; i++) {
							mShortcutList.get(i).setOrder(i);
						}
					}
					notifyDataSetChanged();
				}
			});

			convertView.setTag(shortcut);
			return convertView;
		}

		private void clearShortcutList() {
			mShortcutList.clear();
		}

		private void addShortcutList(ArrayList<ShortcutItem> list) {
			mShortcutList.addAll(list);
		}

		private ArrayList<ShortcutItem> getList() {
			return mShortcutList;
		}
		
		public void onDrop(int from, int to) {
			LogUtils.d(TAG, "drag from ", from , " to ", to);
			if (from == to) return;
			
			mOrderHasChanged = true;
			synchronized(mShortcutList) {
				ShortcutItem oriItem = mShortcutList.get(from);
				oriItem.setOrder(to);
				
				int a = to - from;
				if (a > 0) {
					for (int i = from+1 ; i <= to ; i++) {
						mShortcutList.get(i).setOrder(i-1);
					}
				} else {
					for (int i = to ; i < from ; i++) {
						mShortcutList.get(i).setOrder(i+1);
					}
				}
				Collections.sort(mShortcutList, new ShortcutItemCompare());
			}
			notifyDataSetChanged();
		}
	}
}