package com.cell.cellkeyguard;

import com.android.internal.policy.IKeyguardExitCallback;
import com.android.internal.policy.IKeyguardService;
import com.android.internal.policy.IKeyguardShowCallback;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.MotionEvent;

public class KeyguardService extends Service {
	final static String TAG =  "CellKeyguard/KeyguardService";
	
	KeyguardViewManager mKeyguardViewManager = null;
	KeyguardUpdateMonitor updateMonitor = null;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d(TAG,"oncreate");
		
		updateMonitor = KeyguardUpdateMonitor.getInstance(this);
		mKeyguardViewManager = new KeyguardViewManager(this);
		
	}



	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.d(TAG,"onStartCommand");
		
		if(!mKeyguardViewManager.isShowing()){
			mKeyguardViewManager.attachKeyguardView();
		}
		
		return super.onStartCommand(intent, flags, startId);

	}



	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	private final IKeyguardService.Stub mBinder = new IKeyguardService.Stub() {

		@Override
		public boolean isShowing() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isSecure() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isShowingAndNotHidden() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isInputRestricted() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isDismissable() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void verifyUnlock(IKeyguardExitCallback callback)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyguardDone(boolean authenticated, boolean wakeup)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setHidden(boolean isHidden) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dismiss() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onDreamingStarted() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onDreamingStopped() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onScreenTurnedOff(int reason) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onScreenTurnedOn(IKeyguardShowCallback callback)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setKeyguardEnabled(boolean enabled) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSystemReady() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void doKeyguardTimeout(Bundle options) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setCurrentUser(int userId) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void showAssistant() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dispatch(MotionEvent event) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void launchCamera() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onBootCompleted() throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
	};
	
}

