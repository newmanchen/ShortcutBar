package com.newman.shortcutbar.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.newman.shortcutbar.R;
import com.newman.shortcutbar.util.ImageUtils;

public class Guide extends Activity {
	private static final int GUIDE_COUNT = 7;
	private TextView mTextViewGuide;
	private ViewSwitcher mViewSwitch;
	private Button mButtonStartUsing;
	private ImageView mImageView1;
	private ImageView mImageView2;
	private int mCurrentIndex;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.guide);
		
		mCurrentIndex = 0;
		findViews();
	}
	
	public void findViews() {
		final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		
		mTextViewGuide = (TextView) findViewById(R.id.guide);
		mTextViewGuide.setText("1 / " + GUIDE_COUNT);
		
		mImageView1 = (ImageView) findViewById(R.id.imageView1);
		mImageView2 = (ImageView) findViewById(R.id.imageView2);
		mViewSwitch = (ViewSwitcher) findViewById(R.id.viewSwitcher);
		mViewSwitch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCurrentIndex++;
				mCurrentIndex = mCurrentIndex % GUIDE_COUNT;
				int display = mCurrentIndex+1;
				String displayString = String.valueOf(display) + " / " + GUIDE_COUNT;
				mTextViewGuide.setText(displayString);
				mViewSwitch.showNext();
				if (mViewSwitch.getCurrentView() != mImageView1) {
					mImageView2.setImageDrawable(ImageUtils.getCachedImage(getResources(), getGuideResourceId()));
				} else {
					mImageView1.setImageDrawable(ImageUtils.getCachedImage(getResources(), getGuideResourceId()));
				}
			}
		});
		
		mButtonStartUsing = (Button) findViewById(R.id.start_to_use);
		if (sp.getBoolean(Setting.PREF_FIRST_ACTIVATE, true)) {
			mButtonStartUsing.setVisibility(View.VISIBLE);
			mButtonStartUsing.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					sp.edit().putBoolean(Setting.PREF_FIRST_ACTIVATE, false).commit();
						Intent intent = new Intent(Guide.this, Setting.class); 
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						Guide.this.startActivity(intent);
						Guide.this.finish();
				}
			});
		} else {
			mButtonStartUsing.setVisibility(View.GONE);
		}
	}
	
	private int getGuideResourceId() {
		int result = 0;
		switch (mCurrentIndex) {
		case 0:
			result = R.drawable.guide1;
			break;
		case 1:
			result = R.drawable.guide2;
			break;
		case 2:
			result = R.drawable.guide3;
			break;
		case 3:
			result = R.drawable.guide4;
			break;
		case 4:
			result = R.drawable.guide5;
			break;
		case 5:
			result = R.drawable.guide6;
			break;
		case 6:
			result = R.drawable.guide7;
			break;
		default:
			result = R.drawable.guide1;
			break;
		}
		return result;
	}
}
