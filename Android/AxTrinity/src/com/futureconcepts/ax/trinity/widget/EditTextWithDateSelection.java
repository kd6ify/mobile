package com.futureconcepts.ax.trinity.widget;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;

public class EditTextWithDateSelection extends EditText implements OnTouchListener{
	 
    //The image we are going to use for the Clear button
    private Drawable imgSelectDate = getResources().getDrawable(android.R.drawable.ic_menu_today);
    private boolean setDate = false;
    public EditTextWithDateSelection(Context context) {
        super(context);
        init();
    }
 
    public EditTextWithDateSelection(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
 
    public EditTextWithDateSelection(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
     
    void init() {         
        // Set bounds of the Clear button so it will look ok
       imgSelectDate.setBounds(0, 0,imgSelectDate.getIntrinsicWidth() , imgSelectDate.getIntrinsicHeight());
       this.setCompoundDrawables(imgSelectDate,this.getCompoundDrawables()[1], this.getCompoundDrawables()[2], this.getCompoundDrawables()[3]);
       this.setOnTouchListener(this); 
    }
     
//    //intercept Typeface change and set it with our custom font
//    public void setTypeface(Typeface tf, int style) {
//        if (style == Typeface.BOLD) {
//            super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/Vegur-B 0.602.otf"));
//        } else {
//            super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/Vegur-R 0.602.otf"));
//        }
//    }   
    
    private void displayDateDialog()
    {
    	final Calendar c = Calendar.getInstance();
        int year  = c.get(Calendar.YEAR);
         int month = c.get(Calendar.MONTH);
         int day   = c.get(Calendar.DAY_OF_MONTH);         
         DatePickerDialog dialog = new DatePickerDialog(getContext(), pickerListener, 
         		year, month,day);
         dialog.setCancelable(false);
         dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
         dialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "Select Date", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				setDate = true;
			}
		});         
         
         dialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				// TODO Auto-generated method stub
 				setDate = false;
 			}
 		});  
         
         dialog.show();
         		
    }
    
	private DatePickerDialog.OnDateSetListener pickerListener = new DatePickerDialog.OnDateSetListener() {
 
        // when dialog box is closed, below method will be called.
        @Override
        public void onDateSet(DatePicker view, int selectedYear,
                int selectedMonth, int selectedDay) {
			if (setDate) {
				int year = selectedYear;
				int month = selectedMonth;
				int day = selectedDay;
				Calendar c = Calendar.getInstance();
				int hour = c.get(Calendar.HOUR_OF_DAY);
				int min = c.get(Calendar.MINUTE);
				String dateTime =(checkDigit(month + 1))+"/"+day+"/"+year+" "+hour+":"+checkDigit(min);
				EditTextWithDateSelection et = EditTextWithDateSelection.this;
				et.setText(dateTime);
			}
           }
        };

        public String checkDigit(int number)
        {
            return number<=9?"0"+number:String.valueOf(number);
        }

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if (getCompoundDrawables()[0] != null) {
			if(imgSelectDate.getBounds().contains((int)event.getX(), (int)event.getY()))
			{
				if (event.getAction() == MotionEvent.ACTION_UP) {
					//setText("");
					EditTextWithDateSelection.this.displayDateDialog();      
				}
				return true;
			}
		}
		return super.onTouchEvent(event);
	}
}

