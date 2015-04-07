package com.cell.cellkeyguard;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.cell.cellkeyguard.KeyguardUpdateMonitor.BatteryStatus;
import com.cell.cellkeyguard.slider.BatteryView;
import com.cell.cellkeyguard.slider.CellClockView;
import com.cell.cellkeyguard.slider.DateView;
import com.cell.cellkeyguard.slider.SecondView;

public class KeyguardViewManager {
	final static String TAG = "CellKeyguard/KeyguardViewManager";
	final static int VER_UNLOCK_DISTANCE = 300;
	final static int HOR_UNLOCK_DISTANCE = 500;

	private Context mContext = null;
	private KeyguardViewBase mGuardView = null;
	private KeyguardUpdateMonitor mUpdateMonitor = null;
	private WindowManager mWindowManager;

	private BatteryView mBatteryView = null;
	private CellClockView mClockView = null;
	private DateView mDateView = null;
	private SecondView mSecondView = null;
	boolean showing = false;
	private ViewManagerHost mKeyguardHost;

	private WindowManager.LayoutParams wlp;
	private LayoutParams params;
	private HostViewListener mOnTouchListener = new HostViewListener();;
	private ValueAnimator mUnlocAnimation;
	private UnlockAnimationListener mUnlockAnimationListener;
	private final int WIDTH, HEIGHT;
	private Handler mHandler = new Handler();
	
	KeyguardUpdateMonitorCallback mCallback = new KeyguardUpdateMonitorCallback() {

		@Override
		void onRefreshBatteryInfo(BatteryStatus status) {
			// TODO Auto-generated method stub
			if (isShowing() && status != null) {
				mBatteryView.updateBattery(status.level);
			}
		}

		@Override
		void onTimeChanged() {
			if(isShowing()){
				// TODO Auto-generated method stub
				mClockView.updateTime();
				mDateView.updateTime();
				mSecondView.updateTime();
			}
		}

		@Override
		public void onScreenTurnedOn() {
			// TODO Auto-generated method stub
			showView();
		}

		@Override
		public void onScreenTurnedOff(int why) {
			// TODO Auto-generated method stub
			maybeCreateKeyguardHost();
		}

	};

	KeyguardViewManager(Context context) {
		Log.d(TAG, "oncreate");

		mContext = context;
		mUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
		mWindowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);

		WindowManager wm = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
		Point outSize = new Point();
		wm.getDefaultDisplay().getSize(outSize);
		WIDTH = Math.max(outSize.x ,outSize.y);
		HEIGHT = Math.min(outSize.x ,outSize.y);

		maybeCreateKeyguardHost();

	}

	private void resetState() {
		mOnTouchListener.mMoveState = HostViewListener.MOVE_STATE_NONE;
	}

	private void maybeCreateKeyguardHost() {
		Log.d(TAG, "maybeCreateKeyguardHost");
		if (mKeyguardHost == null) {
			mKeyguardHost = new ViewManagerHost(mContext);

			int flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
					| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
					| WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
					| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
			
			final int stretch = ViewGroup.LayoutParams.MATCH_PARENT;
			final int type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
			wlp = new WindowManager.LayoutParams(stretch, stretch, type, flags,
					PixelFormat.TRANSLUCENT);

			wlp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
			wlp.windowAnimations = android.R.style.Animation_Toast;
			wlp.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
			mWindowManager.addView(mKeyguardHost, wlp);

			resetState();
			mKeyguardHost.setOnTouchListener(mOnTouchListener);
			mUpdateMonitor.registerCallback(mCallback);
		}
	}

	public boolean isShowing() {
		return showing;
	}

	public void showView() {
		Log.d(TAG, "showView");
		if (!isShowing()) {
			inflateKeyguardView();
			mKeyguardHost.setVisibility(View.VISIBLE);
		}
	}

	private void inflateKeyguardView() {

		Log.d(TAG, "initKeyguardView");

		final LayoutInflater inflater = LayoutInflater.from(mContext);
		mGuardView = (KeyguardViewBase) inflater.inflate(getLayoutRes(), null);

		mBatteryView = (BatteryView) mGuardView.findViewById(R.id.battery_view);
		mClockView = (CellClockView) mGuardView.findViewById(R.id.clock_view);
		mDateView = (DateView) mGuardView.findViewById(R.id.date_view);
		mSecondView = (SecondView) mGuardView.findViewById(R.id.second_view);

		params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		mKeyguardHost.addView(mGuardView, params);

		showing = true;
	}

	public void hideKeyguardView() {
		Log.d(TAG, "hideKeyguardView");


		mKeyguardHost.removeView(mGuardView);
		showing = false;
	}

	private void doUnlock() {
		mHandler.post(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mKeyguardHost.setVisibility(View.GONE);
				hideKeyguardView();
				mOnTouchListener.mMoveState = HostViewListener.MOVE_STATE_NONE;
			}
			
		});

	}

	private int getLayoutRes() {

		return R.layout.keyguard_slider_layout;
	}

	private void updateWindow(int dx, int dy) {
		Log.d(TAG, "updateWindow dx=" + dx + " dy=" + dy);
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

	private void animateUnlock(int state) {

		if (mUnlocAnimation != null) {
			mUnlocAnimation.cancel();
		} else {
			mUnlocAnimation = ValueAnimator.ofFloat(0f, 1f);
			mUnlocAnimation.setDuration(300);
			mUnlocAnimation.setInterpolator(new LinearInterpolator());
		}

		switch (state) {
		case HostViewListener.MOVE_STATE_TOP:

			mUnlockAnimationListener = new UnlockAnimationListener(
					-VER_UNLOCK_DISTANCE, -HEIGHT,
					UnlockAnimationListener.UNLOCK_TYPE_TOP);
			break;
		case HostViewListener.MOVE_STATE_BOTTOM:
			mUnlockAnimationListener = new UnlockAnimationListener(
					VER_UNLOCK_DISTANCE, HEIGHT,
					UnlockAnimationListener.UNLOCK_TYPE_BOTTOM);
			break;
		case HostViewListener.MOVE_STATE_LEFT:
			mUnlockAnimationListener = new UnlockAnimationListener(
					-HOR_UNLOCK_DISTANCE, -WIDTH,
					UnlockAnimationListener.UNLOCK_TYPE_LEFT);
			break;
		case HostViewListener.MOVE_STATE_RIGHT:
			mUnlockAnimationListener = new UnlockAnimationListener(
					HOR_UNLOCK_DISTANCE, WIDTH,
					UnlockAnimationListener.UNLOCK_TYPE_RIGHT);
			break;
		default:
		}

		mUnlocAnimation.addUpdateListener(mUnlockAnimationListener);
		mUnlocAnimation.start();
	}

	class HostViewListener implements View.OnTouchListener {

		public final static int MOVE_STATE_NONE = -1;
		public final static int MOVE_STATE_DOWN = 0;
		public final static int MOVE_STATE_LEFT = 1;
		public final static int MOVE_STATE_RIGHT = 2;
		public final static int MOVE_STATE_TOP = 3;
		public final static int MOVE_STATE_BOTTOM = 4;
		public final static int MOVE_STATE_UNLOCK = 5;

		private Point mDownPoint = new Point();
		private int mMoveState = MOVE_STATE_NONE;

		private void resetPoint(int x, int y) {
			mDownPoint.x = x;
			mDownPoint.y = y;
		}

		private void handleMoveEvent(int dx, int dy) {
			switch (mMoveState) {
			case MOVE_STATE_DOWN:
				if (Math.abs(dx) > Math.abs(dy)) {
					if (dx > MotionEvent.AXIS_TOUCH_MINOR) {
						mMoveState = MOVE_STATE_RIGHT;
					} else if (dx < -MotionEvent.AXIS_TOUCH_MINOR) {
						mMoveState = MOVE_STATE_LEFT;
					}
				} else {
					if (dy > MotionEvent.AXIS_TOUCH_MINOR) {
						mMoveState = MOVE_STATE_BOTTOM;
					} else if (dy < -MotionEvent.AXIS_TOUCH_MINOR) {
						mMoveState = MOVE_STATE_TOP;
					}
				}
				break;

			case MOVE_STATE_LEFT:
				if (dx < 0) {
					updateWindow(dx, 0);
					if (Math.abs(dx) > HOR_UNLOCK_DISTANCE) {
						mMoveState = MOVE_STATE_UNLOCK;
						animateUnlock(MOVE_STATE_LEFT);
					}
				} else {
					updateWindow(0, 0);
					resetPoint(mDownPoint.x + dx, mDownPoint.y);
				}
				break;
			case MOVE_STATE_RIGHT:
				if (dx > 0) {
					updateWindow(dx, 0);
					if (Math.abs(dx) > HOR_UNLOCK_DISTANCE) {
						mMoveState = MOVE_STATE_UNLOCK;
						animateUnlock(MOVE_STATE_RIGHT);
					}
				} else {
					updateWindow(0, 0);
					resetPoint(mDownPoint.x + dx, mDownPoint.y);
				}
				break;
			case MOVE_STATE_TOP:
				if (dy < 0) {
					updateWindow(0, dy);
					if (Math.abs(dy) > VER_UNLOCK_DISTANCE) {
						mMoveState = MOVE_STATE_UNLOCK;
						animateUnlock(MOVE_STATE_TOP);
					}
				} else {
					updateWindow(0, 0);
					resetPoint(mDownPoint.x, mDownPoint.y + dy);
				}
				break;
			case MOVE_STATE_BOTTOM:
				if (dy > 0) {
					updateWindow(0, dy);
					if (Math.abs(dy) > VER_UNLOCK_DISTANCE) {
						mMoveState = MOVE_STATE_UNLOCK;
						animateUnlock(MOVE_STATE_BOTTOM);
					}
				} else {
					updateWindow(0, 0);
					resetPoint(mDownPoint.x, mDownPoint.y + dy);
				}
				break;
			default:
				break;
			}
		}

		private void handleUpEvent() {

		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if (!isShowing()) {
				return false;
			}
			float x = event.getX();
			float y = event.getY();
			switch (event.getAction()) {

			case MotionEvent.ACTION_DOWN:

				mDownPoint.x = (int) x;
				mDownPoint.y = (int) y;
				if (mMoveState == MOVE_STATE_NONE) {
					mMoveState = MOVE_STATE_DOWN;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				int dx = (int) x - mDownPoint.x;
				int dy = (int) y - mDownPoint.y;

				handleMoveEvent(dx, dy);

				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				if (mMoveState == MOVE_STATE_DOWN) {
					mMoveState = MOVE_STATE_NONE;
				}
				if (mMoveState > MOVE_STATE_DOWN
						&& mMoveState < MOVE_STATE_UNLOCK) {
					updateWindow(0, 0);
					mMoveState = MOVE_STATE_NONE;
				}
			}

			return false;
		}

	}

	class UnlockAnimation extends Animation {

		int mFromXDelta, mToXDelta, mFromYDelta, mToYDelta;

		public UnlockAnimation(int fromx, int tox, int fromy, int toy) {
			mFromXDelta = fromx;
			mToXDelta = tox;
			mFromYDelta = fromy;
			mToYDelta = toy;
		}

		@Override
		public void initialize(int width, int height, int parentWidth,

		int parentHeight)

		{

			super.initialize(width, height, parentWidth, parentHeight);

			setDuration(300);

			setFillAfter(true);

			setInterpolator(new LinearInterpolator());

		}

		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			float dx = mFromXDelta;
			float dy = mFromYDelta;
			if (mFromXDelta != mToXDelta) {
				dx = mFromXDelta
						+ ((mToXDelta - mFromXDelta) * interpolatedTime);
			}
			if (mFromYDelta != mToYDelta) {
				dy = mFromYDelta
						+ ((mToYDelta - mFromYDelta) * interpolatedTime);
			}

			updateWindow((int) dx, (int) dy);

		}

	}

	class UnlockAnimationListener implements AnimatorUpdateListener {
		final static int UNLOCK_TYPE_NONE = 0;
		final static int UNLOCK_TYPE_LEFT = 1;
		final static int UNLOCK_TYPE_RIGHT = 2;
		final static int UNLOCK_TYPE_TOP = 3;
		final static int UNLOCK_TYPE_BOTTOM = 4;
		int unlockType = UNLOCK_TYPE_NONE;
		int mFrom, mTo;

		UnlockAnimationListener(int from, int to, int type) {
			mFrom = from;
			mTo = to;
			unlockType = type;
		}

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			// TODO Auto-generated method stub
			if (mFrom == mTo) {
				return;
			}
			float distance = mFrom;
			float value = (Float) animation.getAnimatedValue();
			distance = mFrom + ((mTo - mFrom) * value);

			switch (unlockType) {
			case UNLOCK_TYPE_LEFT:
				updateWindow((int) distance, 0);
				break;
			case UNLOCK_TYPE_RIGHT:
				updateWindow((int) distance, 0);
				break;
			case UNLOCK_TYPE_TOP:
				updateWindow(0, (int) distance);
				break;
			case UNLOCK_TYPE_BOTTOM:
				updateWindow(0, (int) distance);
				break;
			}// end switch
			
			if (Math.abs(distance) >= Math.abs(mTo)) {
				switch (unlockType) {
				case UNLOCK_TYPE_LEFT:
					doUnlock();
					break;
				case UNLOCK_TYPE_RIGHT:
					doUnlock();
					break;
					
				case UNLOCK_TYPE_TOP:
					doUnlock();
					break;
				case UNLOCK_TYPE_BOTTOM:
					doUnlock();
					break;
				}
			}


		}// end onAnimationUpdate
	}// end UnlockAnimationListener

}// end class
