package com.cell.cellkeyguard.slider;

import java.util.Calendar;

import com.cell.cellkeyguard.R;
import com.cell.cellkeyguard.R.color;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;


public class BatteryView extends ImageView {
	final static String TAG =  "CellKeyguard/BatteryView";
	
	final static int BATTERY_MARGIN = 9;
	Context mContext;
	Calendar mCalendar;
	int srcWidth = 0,srcHeight=0;
	int value = 0;
	Paint rectPaint = null;
	Rect rect = null;
	int powerColor = Color.WHITE;
	int lowPowerColor = Color.RED;
	

	public BatteryView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}
	public BatteryView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}
	public BatteryView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		
		mContext = context;
		mCalendar = Calendar.getInstance();
		
		Drawable drawable = this.getDrawable();
		srcWidth = drawable.getIntrinsicWidth();
		srcHeight = drawable.getIntrinsicHeight();
		
		rect = new Rect();
		rect.top=0;
		rect.bottom=srcHeight;
		rect.left=srcWidth;
		rect.right=srcWidth;
		
		rectPaint = new Paint();
		rectPaint.setAntiAlias(false);
		rectPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		
		powerColor = this.getResources().getColor(R.color.PowerColor);
			
		lowPowerColor= this.getResources().getColor(R.color.LowPowerColor);

	}
	@Override
	protected void onAttachedToWindow() {
		// TODO Auto-generated method stub
		super.onAttachedToWindow();
	}
	@Override
	protected void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
	}
	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		int valueWidth = value*(srcWidth-BATTERY_MARGIN)/100;

		rect.left=srcWidth-valueWidth;
		
		if(value>15){
			rectPaint.setColor(powerColor);
		}else{
			rectPaint.setColor(lowPowerColor);
		}

		canvas.drawRect(rect, rectPaint);
		
		this.getDrawable().draw(canvas);

		canvas.save();
	}
	
	
	public void updateBattery(int level) {
		Log.d(TAG,"updateBattery level="+level); 
		value = level;
        this.invalidate();
	}
	
}
