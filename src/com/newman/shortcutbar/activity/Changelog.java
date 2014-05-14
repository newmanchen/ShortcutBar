package com.newman.shortcutbar.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.newman.shortcutbar.R;

public class Changelog extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changelog);
		
		findViews();
	}
	
	private void findViews() {
		generateIntroduction();

		generateVOneDotTwoDotOne();
		generateVOneDotTwoDotZero();
		generateVOneDotOneDotOne();
		generateVOneDotOneDotZero();
		generateVOneDotZeroDotFour();
		generateVOneDotZeroDotThree();
		generateVOneDotZeroDotTwo();
		generateVOneDotDotZeroDotOne();
		generateVOneDotZero();
	}
	
	private void generateIntroduction() {
		// introduction
		View v10 = findViewById(R.id.introduction);
		TextView version = (TextView)v10.findViewById(R.id.version);
		TextView changelog = (TextView) v10.findViewById(R.id.change_log_content);
		version.setText(R.string.text_introdution);
		changelog.setText(getString(R.string.app_introdution));
	}
	
	private void generateVOneDotTwoDotOne() {
		// v1.2.1
		View v121 = findViewById(R.id.v_one_dot_two_dot_one);
		TextView version = (TextView) v121.findViewById(R.id.version);
		TextView changelog = (TextView) v121.findViewById(R.id.change_log_content);
		version.setText(" V 1.2.1 ");
		changelog.setText(getString(R.string.change_log_v_1_dot_2_dot_1));
	}
	
	private void generateVOneDotTwoDotZero() {
		// v1.2.0
		View v120 = findViewById(R.id.v_one_dot_two_dot_zero);
		TextView version = (TextView) v120.findViewById(R.id.version);
		TextView changelog = (TextView) v120.findViewById(R.id.change_log_content);
		version.setText(" V 1.2.0 ");
		changelog.setText(getString(R.string.change_log_v_1_dot_2_dot_0));
	}
	
	private void generateVOneDotOneDotOne() {
		// v1.1.1
		View v111 = findViewById(R.id.v_one_dot_one_dot_one);
		TextView version = (TextView) v111.findViewById(R.id.version);
		TextView changelog = (TextView) v111.findViewById(R.id.change_log_content);
		version.setText(" V 1.1.1 ");
		changelog.setText(getString(R.string.change_log_v_1_dot_1_dot_1));
	}
	
	private void generateVOneDotOneDotZero() {
		// v1.1.0
		View v110 = findViewById(R.id.v_one_dot_one_dot_zero);
		TextView version = (TextView) v110.findViewById(R.id.version);
		TextView changelog = (TextView) v110.findViewById(R.id.change_log_content);
		version.setText(" V 1.1.0 ");
		changelog.setText(getString(R.string.change_log_v_1_dot_1_dot_0));
	}
	
	private void generateVOneDotZeroDotFour() {
		// v1.0.4
		View v104 = findViewById(R.id.v_one_dot_zero_dot_four);
		TextView version = (TextView) v104.findViewById(R.id.version);
		TextView changelog = (TextView) v104.findViewById(R.id.change_log_content);
		version.setText(" V 1.0.4 ");
		changelog.setText(getString(R.string.change_log_v_1_dot_0_dot_4));
	}

	private void generateVOneDotZeroDotThree() {
		// v1.0.3
		View v103 = findViewById(R.id.v_one_dot_zero_dot_three);
		TextView version = (TextView) v103.findViewById(R.id.version);
		TextView changelog = (TextView) v103.findViewById(R.id.change_log_content);
		version.setText(" V 1.0.3 ");
		changelog.setText(getString(R.string.change_log_v_1_dot_0_dot_3));
	}
	
	private void generateVOneDotZeroDotTwo() {
		// v1.0.2
		View v102 = findViewById(R.id.v_one_dot_zero_dot_two);
		TextView version = (TextView) v102.findViewById(R.id.version);
		TextView changelog = (TextView) v102.findViewById(R.id.change_log_content);
		version.setText(" V 1.0.2 ");
		changelog.setText(getString(R.string.change_log_v_1_dot_0_dot_2));
	}
	
	private void generateVOneDotDotZeroDotOne() {
		// v1.0.1
		View v101 = findViewById(R.id.v_one_dot_zero_dot_one);
		TextView version = (TextView) v101.findViewById(R.id.version);
		TextView changelog = (TextView) v101.findViewById(R.id.change_log_content);
		version.setText(" V 1.0.1 ");
		changelog.setText(getString(R.string.change_log_v_1_dot_0_dot_1));
	}
	
	private void generateVOneDotZero() {
		// v1.0
		View v10 = findViewById(R.id.v_one_dot_zero);
		TextView version = (TextView) v10.findViewById(R.id.version);
		TextView changelog = (TextView) v10.findViewById(R.id.change_log_content);
		version.setText(" V 1.0 ");
		changelog.setText(getString(R.string.change_log_v_1_dot_0));
	}
}
