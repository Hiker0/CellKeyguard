package com.cell.cellkeyguard;

import com.cell.cellkeyguard.KeyguardUpdateMonitor.BatteryStatus;
import com.cell.cellkeyguard.slider.BatteryView;
import com.cell.cellkeyguard.slider.CellClockView;
import com.cell.cellkeyguard.slider.DateView;
import com.cell.cellkeyguard.slider.SecondView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

public class KeyguardViewManager {
	final static String TAG =  "CellKeyguard/KeyguardViewManager";
	final static int MAX_SLID_DISTANCE=400;
	
	private Context mContext = null;
	private KeyguardViewBase mGuardView = null;
	private KeyguardUpdateMonitor mUpdateMonitor = null;
	private WindowManager mWindowManager;
	
	private BatteryView mBatteryView=null;
	private CellClockView mClockView = null;
	private DateView mDateView = null;
	private SecondView mSecondView= null;
	boolean showing = false;
	private ViewManagerHost mKeyguardHost;
	
	private WindowManager.LayoutParams wlp;
	private LayoutParams params;
	private HostViewListener mOnTouchListener;
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
			mSecondView.updateTime();
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
		
		maybeCreateKeyguardHost();

		
	}
	
	private void maybeCreateKeyguardHost(){
		if(mKeyguardHost == null){
			 mKeyguardHost = new ViewManagerHost(mContext);
			
			 
			 int flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
	                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
	                | WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
			         | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED ;
			 final int stretch = ViewGroup.LayoutParams.MATCH_PARENT;
	        final int type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
	        wlp = new WindowManager.LayoutParams(
	                stretch, stretch, type, flags, PixelFormat.TRANSLUCENT);
	        
	        wlp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
	        wlp.windowAnimations = android.R.style.Animation_Toast;
	        wlp.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	        mWindowManager.addView(mKeyguardHost, wlp);
	        
	        mOnTouchListener = new HostViewListener();
	        mKeyguardHost.setOnTouchListener(mOnTouchListener);
		}
	}
	
	public boolean isShowing(){
		return showing;
	}
	
	public void showView(){
		if(!isShowing()){
			inflateKeyguardView();
			mKeyguardHost.setVisibility(View.VISIBLE);
		}
	}
	
	private void inflateKeyguardView(){
		
		Log.d(TAG,"initKeyguardView");
		
		final LayoutInflater inflater = LayoutInflater.from(mContext);
		mGuardView = (KeyguardViewBase) inflater.inflate(getLayoutRes(), null);
		
		mBatteryView= (BatteryView) mGuardView.findViewById(R.id.battery_view);
		mClockView = (CellClockView) mGuardView.findViewById(R.id.clock_view);
		mDateView = (DateView) mGuardView.findViewById(R.id.date_view);
		mSecondView = (SecondView) mGuardView.findViewById(R.id.second_view);
		
		params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mKeyguardHost.addView(mGuardView, params);
		mUpdateMonitor.registerCallback(mCallback);
		
		
		showing = true;
	}

	
	
	public void hideKeyguardView(){
		Log.d(TAG,"hideKeyguardView");
		
		mUpdateMonitor.removeCallback(mCallback);
		
		mKeyguardHost.removeView(mGuardView);
		showing = false;
	}
	
	private void  doUnlock(){
		mKeyguardHost.setVisibility(View.GONE);
		hideKeyguardView();
		mOnTouchListener.mMoveState=mOnTouchListener.MOVE_STATE_NONE;
	}
	private int getLayoutRes(){
		
		return R.layout.keyguard_slider_layout;
	}
	
	private void animateUnlock(){
		doUnlock();
	}
	
	private void updateWindow(int dx, int dy){
		Log.d(TAG,"updateWindow dx="+dx+" dy="+dy);
		params.rightMargin = -dx;
		params.leftMargin = dx;
		params.topMargin = dy;
		params.bottomMargin = -dy;
		
		mKeyguardHost.updateViewLayout(mGuardView, params);
	}
	
    class ViewManagerHost extends FrameLayout {
    	
		public ViewManagerHost(Context context) {
			this(context, null);
			// TODO Auto-generated constructor stub
		}
		public ViewManagerHost(Context context, AttributeSet attrs) {
			super(context, attrs);
			// TODO Auto-generated constructor stub
		}
    	
    }
    private void setNavigationBarEnabled(boolean enabled) {

        mKeyguardHost.setSystemUiVisibility(enabled ?
               mKeyguardHost.getSystemUiVisibility() &~View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    &~View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN:
                    mKeyguardHost.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN 
                        |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    )
               ;
    }
	
	class HostViewListener implements View.OnTouchListener{


		public final int MOVE_STATE_NONE = -1;
		public final int MOVE_STATE_DOWN = 0;
		public final int MOVE_STATE_LEFT = 1;
		public final int MOVE_STATE_RIGHT = 2;
		public final int MOVE_STATE_TOP = 3;
		public final int MOVE_STATE_BOTTOM = 4;
		public final int MOVE_STATE_UNLOCK = 5;
		
		private Point mDownPoint = new Point(); 
		private int mMoveState = MOVE_STATE_NONE ;
		
		private void resetPoint(int x, int y){
			mDownPoint.x = x;
			mDownPoint.y = y;
		}
		private void handleMoveEvent(int dx, int dy){
			switch(mMoveState){
			case MOVE_STATE_DOWN:
				if(Math.abs(dx)>Math.abs(dy) ){
					if(dx > MotionEvent.AXIS_TOUCH_MINOR){
						mMoveState=MOVE_STATE_RIGHT;
					}else if(dx < -MotionEvent.AXIS_TOUCH_MINOR){
						mMoveState=MOVE_STATE_LEFT;
					}
				}else {
					if(dy > MotionEvent.AXIS_TOUCH_MINOR){
						mMoveState=MOVE_STATE_BOTTOM;
					}else if(dy < -MotionEvent.AXIS_TOUCH_MINOR){
						mMoveState=MOVE_STATE_TOP;
					}
				}
				break;
				
			case MOVE_STATE_LEFT:
				if(dx < 0){
					updateWindow(dx,0);
					if(Math.abs(dx)>MAX_SLID_DISTANCE){
						mMoveState=MOVE_STATE_UNLOCK;
						animateUnlock();
					}
				}else{
					updateWindow(0,0);
					resetPoint(mDownPoint.x+dx,mDownPoint.y);
				}
				break;
			case MOVE_STATE_RIGHT:
				if(dx > 0){
					updateWindow(dx,0);
					if(Math.abs(dx)>MAX_SLID_DISTANCE){
						mMoveState=MOVE_STATE_UNLOCK;
						animateUnlock();
					}
				}else{
					updateWindow(0,0);
					resetPoint(mDownPoint.x+dx,mDownPoint.y);
				}
				break;
			case MOVE_STATE_TOP:
				if(dy < 0){
					updateWindow(0,dy);
					if(Math.abs(dy)>MAX_SLID_DISTANCE){
						mMoveState=MOVE_STATE_UNLOCK;
						animateUnlock();
					}
				}else{
					updateWindow(0,0);
					resetPoint(mDownPoint.x,mDownPoint.y+dy);
				}
				break;
			case MOVE_STATE_BOTTOM:
				if(dy > 0){
					updateWindow(0,dy);
					if(Math.abs(dy)>MAX_SLID_DISTANCE){
						mMoveState=MOVE_STATE_UNLOCK;
						animateUnlock();
					}
				}else{
					updateWindow(0,0);
					resetPoint(mDownPoint.x,mDownPoint.y+dy);
				}
				break;
			default: break;
			}
		}
		
		private void handleUpEvent(){
			
		}
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if(!isShowing()){
				return false;
			}
			float x = event.getX();
			float y = event.getY();
			switch(event.getAction()){
			
			case MotionEvent.ACTION_DOWN:
				
				mDownPoint.x = (int) x;
				mDownPoint.y = (int) y;
				if(mMoveState == MOVE_STATE_NONE){
					mMoveState = MOVE_STATE_DOWN;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				int dx = (int) x - mDownPoint.x;
				int dy = (int) y - mDownPoint.y;
				
				handleMoveEvent(dx,dy);
				
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				if(mMoveState == MOVE_STATE_DOWN){
					mMoveState = MOVE_STATE_NONE;
				}
				if(mMoveState>MOVE_STATE_DOWN && mMoveState < MOVE_STATE_UNLOCK){
					updateWindow(0,0);
				}
			}
			
			return false;
		}
		
	
		
	}
    
    
}
