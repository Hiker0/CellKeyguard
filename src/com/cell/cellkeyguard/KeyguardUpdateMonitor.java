package com.cell.cellkeyguard;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.util.Log;
import static android.os.BatteryManager.BATTERY_STATUS_FULL;
import static android.os.BatteryManager.BATTERY_STATUS_UNKNOWN;
import static android.os.BatteryManager.BATTERY_HEALTH_UNKNOWN;
import static android.os.BatteryManager.EXTRA_STATUS;
import static android.os.BatteryManager.EXTRA_PLUGGED;
import static android.os.BatteryManager.EXTRA_LEVEL;
import static android.os.BatteryManager.EXTRA_HEALTH;

public class KeyguardUpdateMonitor {
	final static String TAG =  "CellKeyguard/SecondView";
	
    private static final int LOW_BATTERY_THRESHOLD = 16;
    
    // Callback messages
    private static final int MSG_TIME_UPDATE = 301;
    private static final int MSG_BATTERY_UPDATE = 302;
    private static final int MSG_CARRIER_INFO_UPDATE = 303;
    private static final int MSG_SIM_STATE_CHANGE = 304;
    private static final int MSG_RINGER_MODE_CHANGED = 305;
    private static final int MSG_PHONE_STATE_CHANGED = 306;
    private static final int MSG_CLOCK_VISIBILITY_CHANGED = 307;
    private static final int MSG_DEVICE_PROVISIONED = 308;
    private static final int MSG_DPM_STATE_CHANGED = 309;
    private static final int MSG_USER_SWITCHING = 310;
    private static final int MSG_USER_REMOVED = 311;
    private static final int MSG_KEYGUARD_VISIBILITY_CHANGED = 312;
    protected static final int MSG_BOOT_COMPLETED = 313;
    private static final int MSG_USER_SWITCH_COMPLETE = 314;
    private static final int MSG_SET_CURRENT_CLIENT_ID = 315;
    protected static final int MSG_SET_PLAYBACK_STATE = 316;
    protected static final int MSG_USER_INFO_CHANGED = 317;
    protected static final int MSG_REPORT_EMERGENCY_CALL_ACTION = 318;
    private static final int MSG_SCREEN_TURNED_ON = 319;
    private static final int MSG_SCREEN_TURNED_OFF = 320;
    
	Context mContext = null;
	static KeyguardUpdateMonitor sInstance = null;
	BatteryStatus mBatteryStatus;
	private boolean mScreenOn;
	  
    private final ArrayList<WeakReference<KeyguardUpdateMonitorCallback>> mCallbacks;

	 private final Handler mHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	                case MSG_TIME_UPDATE:
	                    handleTimeUpdate();
	                    break;
	                case MSG_BATTERY_UPDATE:
	                    handleBatteryUpdate((BatteryStatus) msg.obj);
	                    break;
	                case MSG_BOOT_COMPLETED:
	                    handleBootCompleted();
	                    break;
	                case MSG_SCREEN_TURNED_OFF:
	                    handleScreenTurnedOff(msg.arg1);
	                    break;
	                case MSG_SCREEN_TURNED_ON:
	                    handleScreenTurnedOn();
	                    break;
	            }
	        }
	    };
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
    	
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG,"onReceive  action="+action);
            
            if (Intent.ACTION_TIME_TICK.equals(action)
                    || Intent.ACTION_TIME_CHANGED.equals(action)
                    || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
                mHandler.sendEmptyMessage(MSG_TIME_UPDATE);
            } else if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                int status = intent.getIntExtra(EXTRA_STATUS, BATTERY_STATUS_UNKNOWN);
                int plugged = intent.getIntExtra(EXTRA_PLUGGED, 0);
                int level = intent.getIntExtra(EXTRA_LEVEL, 0);
                int health = intent.getIntExtra(EXTRA_HEALTH, BATTERY_HEALTH_UNKNOWN);
                Message msg = mHandler.obtainMessage(
                        MSG_BATTERY_UPDATE, new BatteryStatus(0, status, level, plugged, health));
                mHandler.sendMessage(msg);

            } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
                dispatchBootCompleted();

            } 
        }
    };
	
static class BatteryStatus {
        public final int index;
        public final int status;
        public final int level;
        public final int plugged;
        public final int health;
        public BatteryStatus(int index, int status, int level, int plugged, int health) {
            this.index = index;
            this.status = status;
            this.level = level;
            this.plugged = plugged;
            this.health = health;
        }


        boolean isPluggedIn() {
            return plugged == BatteryManager.BATTERY_PLUGGED_AC
                    || plugged == BatteryManager.BATTERY_PLUGGED_USB
                    || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
        }

 
        public boolean isCharged() {
            return status == BATTERY_STATUS_FULL || level >= 100;
        }


        public boolean isBatteryLow() {
            return level < LOW_BATTERY_THRESHOLD;
        }

    }
 
	public KeyguardUpdateMonitor(Context context) {
		// TODO Auto-generated constructor stub
		 mContext = context;
		 mCallbacks = new  ArrayList<WeakReference<KeyguardUpdateMonitorCallback>>();
		 
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        

        context.registerReceiver(mBroadcastReceiver, filter);

        final IntentFilter bootCompleteFilter = new IntentFilter();
        bootCompleteFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        bootCompleteFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
        context.registerReceiver(mBroadcastReceiver, bootCompleteFilter);
	}
	
	public static KeyguardUpdateMonitor getInstance(Context context) {
		// TODO Auto-generated constructor stub
        if (sInstance == null) {
            sInstance = new KeyguardUpdateMonitor(context);
        }
        return sInstance;
	}
	
    public void removeCallback(KeyguardUpdateMonitorCallback callback) {
    	
        for (int i = mCallbacks.size() - 1; i >= 0; i--) {
            if (mCallbacks.get(i).get() == callback) {
                mCallbacks.remove(i);
            }
        }
    }
    
    public void registerCallback(KeyguardUpdateMonitorCallback callback) {
        // Prevent adding duplicate callbacks
        for (int i = 0; i < mCallbacks.size(); i++) {
            if (mCallbacks.get(i).get() == callback) {
                return;
            }
        }
        mCallbacks.add(new WeakReference<KeyguardUpdateMonitorCallback>(callback));
        removeCallback(null); 
        sendUpdates(callback);

    }
    
    private void sendUpdates(KeyguardUpdateMonitorCallback callback) {
        // Notify listener of the current state
    	
        callback.onRefreshBatteryInfo(mBatteryStatus);

        callback.onTimeChanged();
    }
    
	private void handleTimeUpdate(){
		 for (int i = 0; i < mCallbacks.size(); i++) {
			 KeyguardUpdateMonitorCallback cb = mCallbacks.get(i).get();
	         if(cb != null){
	        	 cb.onTimeChanged();
	         }
	     }
	};
	
	
	private void handleBootCompleted(){
		 for (int i = 0; i < mCallbacks.size(); i++) {
			 KeyguardUpdateMonitorCallback cb = mCallbacks.get(i).get();
	         if(cb != null){
	        	 
	         }
	     }
	};
	
	private  void handleBatteryUpdate(BatteryStatus status){
		 for (int i = 0; i < mCallbacks.size(); i++) {
			 KeyguardUpdateMonitorCallback cb = mCallbacks.get(i).get();
	         if(cb != null){
	        	 cb.onRefreshBatteryInfo(status);
	         }
	     }
	};
	
    public boolean isScreenOn() {
        return mScreenOn;
    }
    
	private void dispatchBootCompleted(){

	};
	
    public void dispatchScreenTurnedOn() {
        synchronized (this) {
            mScreenOn = true;
        }
        mHandler.sendEmptyMessage(MSG_SCREEN_TURNED_ON);
    }	
    
    public void dispatchScreenTurndOff(int why) {
        synchronized(this) {
            mScreenOn = false;
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SCREEN_TURNED_OFF, why, 0));
    }
    
    protected void handleScreenTurnedOn() {
        final int count = mCallbacks.size();
        for (int i = 0; i < count; i++) {
            KeyguardUpdateMonitorCallback cb = mCallbacks.get(i).get();
            if (cb != null) {
                cb.onScreenTurnedOn();
            }
        }
    }

    protected void handleScreenTurnedOff(int arg1) {
        final int count = mCallbacks.size();
        for (int i = 0; i < count; i++) {
            KeyguardUpdateMonitorCallback cb = mCallbacks.get(i).get();
            if (cb != null) {
                cb.onScreenTurnedOff(arg1);
            }
        }
    }	
	
}
