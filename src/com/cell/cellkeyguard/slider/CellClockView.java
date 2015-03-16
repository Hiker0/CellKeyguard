package com.cell.cellkeyguard.slider;

import java.util.Calendar;
import java.util.Date;

import com.cell.cellkeyguard.R;
import com.cell.cellkeyguard.R.drawable;
import com.cell.cellkeyguard.R.id;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class CellClockView extends LinearLayout {
	Context mContext;
	Calendar mCalendar;
	ImageView[] hourViews;
	ImageView[] minuViews;
	int[] hours = new int[]{
			R.drawable.h0, R.drawable.h1,
			R.drawable.h2, R.drawable.h3,
			R.drawable.h4, R.drawable.h5,
			R.drawable.h6, R.drawable.h7,
			R.drawable.h8, R.drawable.h9,
		};
	int[] minus = new int[]{
			R.drawable.m0, R.drawable.m1,
			R.drawable.m2, R.drawable.m3,
			R.drawable.m4, R.drawable.m5,
			R.drawable.m6, R.drawable.m7,
			R.drawable.m8, R.drawable.m9,
	};
	
	
	public CellClockView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}
	public CellClockView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}
	public CellClockView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		
		mContext = context;
		mCalendar = Calendar.getInstance();
		hourViews = new ImageView[2];
		minuViews = new ImageView[2];
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		hourViews[0]=(ImageView) findViewById(R.id.hour_ge);
		hourViews[1]=(ImageView) findViewById(R.id.hour_shi);
		
		minuViews[0]=(ImageView) findViewById(R.id.minu_ge);
		minuViews[1]=(ImageView) findViewById(R.id.minu_shi);		
		
		updateTime();
	}

    public void updateTime() {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        
        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int minu = mCalendar.get(Calendar.MINUTE);
        
        hourViews[0].setImageResource(hours[hour%10]);
        hourViews[1].setImageResource(hours[hour/10]);
        
        minuViews[0].setImageResource(minus[minu%10]);
        minuViews[1].setImageResource(minus[minu/10]);
    }	
	
}
