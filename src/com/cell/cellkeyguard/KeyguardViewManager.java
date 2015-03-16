package com.cell.cellkeyguard;

import com.cell.cellkeyguard.KeyguardUpdateMonitor.BatteryStatus;
import com.cell.cellkeyguard.slider.BatteryView;
import com.cell.cellkeyguard.slider.CellClockView;
import com.cell.cellkeyguard.slider.DateView;
import com.cell.cellkeyguard.slider.SecondView;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;

public class KeyguardViewManager {
	final static String TAG =  "CellKeyguard/KeyguardViewManager";
	
	private Context mContext = null;
	private KeyguardViewBase mGuardView = null;
	private KeyguardUpdateMonitor mUpdateMonitor = null;
	private WindowManager mWindowManager;
	
	private BatteryView mBatteryView=null;
	private CellClockView mClockView = null;
	private DateView mDateView = null;
	private SecondView mSecondView= null;
	boolean showing = false;
	

	KeyguardUpdateMonitorCallback mCallback = new KeyguardUpdateMonitorCallback(){

		@Override
		void onRefreshBatteryInfo(BatteryStatus status) {
			// TODO Auto-generated method stub
			if(status != null){
				mBatteryView.updateBattery(status.level);
			}
		}

		@Override
		void onTimeChanged() {
			// TODO Auto-generated method stub
			mClockView.updateTime();
			mDateView.updateTime();
		}

		@Override
		public void onScreenTurnedOn() {
			// TODO Auto-generated method stub
			super.onScreenTurnedOn();
		}

		@Override
		public void onScreenTurnedOff(int why) {
			// TODO Auto-generated method stub
			super.onScreenTurnedOff(why);
		}
		
	};
	
	
	
	KeyguardViewManager(Context context){
		Log.d(TAG,"oncreate");
		
		mContext = context;
		mUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
		mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		initKeyguardView();
		
	}
	
	public boolean isShowing(){
		return showing;
	}
	private void initKeyguardView(){
		Log.d(TAG,"initKeyguardView");
		
		final LayoutInflater inflater = LayoutInflater.from(mContext);
		mGuardView = (KeyguardViewBase) inflater.inflate(getLayoutRes(), null);
		
		mBatteryView= (BatteryView) mGuardView.findViewById(R.id.battery_view);
		mClockView = (CellClockView) mGuardView.findViewById(R.id.clock_view);
		mDateView = (DateView) mGuardView.findViewById(R.id.date_view);
		mSecondView = (SecondView) mGuardView.findViewById(R.id.second_view);
	}
	public void attachKeyguardView(){
		 Log.d(TAG,"attachKeyguardView");
		
		 int flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                 | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                 | WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
		         | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED 
                 ;
		 final int stretch = ViewGroup.LayoutParams.MATCH_PARENT;
         final int type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
         WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                 stretch, stretch, type, flags, PixelFormat.TRANSLUCENT);
         lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
         lp.windowAnimations = android.R.style.Animation_Toast;
         lp.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
         mWindowManager.addView(mGuardView, lp);
         
		mUpdateMonitor.registerCallback(mCallback);
		showing = true;
		
	}
	public void detachKeyguardView(){
		Log.d(TAG,"detachKeyguardView");
		
		mUpdateMonitor.removeCallback(mCallback);
		
		mWindowManager.removeView(mGuardView);
		showing = false;
	}
	
	private int getLayoutRes(){
		
		return R.layout.keyguard_slider_layout;
	}
}
