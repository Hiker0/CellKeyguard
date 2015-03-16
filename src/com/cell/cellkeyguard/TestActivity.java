package com.cell.cellkeyguard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

public class TestActivity extends Activity {
	final static String TAG =  "CellKeyguard/TestActivity";
	final static String KEYGUARD_SERVICE_NAME = "com.cell.cellkeyguard.KeyguardService";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.d(TAG,"oncreate");
		
//		getWindow().setType(
//				WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		
		Intent intent = new Intent();
		intent.setClassName(this, KEYGUARD_SERVICE_NAME);
		this.startService(intent);
		finish();
//		setContentView(R.layout.main);

	}

	public void onAttachedToWindow() {  
//	     this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);     
//	     super.onAttachedToWindow();    
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)

	{

		// TODO Auto-generated method stub

		// 按下键盘上返回按钮

		if (keyCode == KeyEvent.KEYCODE_HOME || 
				keyCode == KeyEvent.KEYCODE_BACK)
		{
			System.exit(0);

			return true;
		}else{
			return super.onKeyDown(keyCode, event);
		}

	}


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

}
