package com.cell.cellkeyguard.slider;

import java.util.Calendar;

import com.cell.cellkeyguard.R;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;


public class DateView extends TextView {
	private static final String CELL_CLOCK_FONT_FILE = "fonts/04B_20_.TTF";
	Context mContext;
	Calendar mCalendar;
	String[] months=null;
	String[] weeks=null;
	Typeface tf;
	public DateView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}
	public DateView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}
	public DateView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		
		mContext = context;
		mCalendar = Calendar.getInstance();
		months = this.getResources().getStringArray(R.array.months);
		weeks = this.getResources().getStringArray(R.array.weeks);
		
		tf = Typeface.createFromAsset(getResources().getAssets(), CELL_CLOCK_FONT_FILE);
		this.setTypeface(tf);
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		updateTime();
		
	}

	public void updateTime() {
		mCalendar.setTimeInMillis(System.currentTimeMillis());
        
        int week = mCalendar.get(Calendar.DAY_OF_WEEK);
        int month = mCalendar.get(Calendar.MONTH);
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);
        fommatDates(month,day,week);
        this.invalidate();
	}
	
	private void fommatDates(int month, int day, int week){
		StringBuilder sb = new StringBuilder();
		sb.append(">>");
		sb.append(weeks[week]);
		sb.append("-");
		sb.append(months[month]);
		sb.append(" ");
		sb.append(day);
		
		this.setText(sb.toString());
	}
}
