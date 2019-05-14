package com.futureconcepts.ax.trinity.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;

public class EditTextWithClearButton extends EditText implements OnTouchListener{
	 
    //The image we are going to use for the Clear button
    public Drawable imgCloseButton = getResources().getDrawable(android.R.drawable.ic_menu_close_clear_cancel);
     
    public EditTextWithClearButton(Context context) {
        super(context);
        init();
    }
 
    public EditTextWithClearButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
 
    public EditTextWithClearButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
     
    void init() {
         
        // Set bounds of the Clear button so it will look ok
        imgCloseButton.setBounds(0, 0, imgCloseButton.getIntrinsicWidth(), imgCloseButton.getIntrinsicHeight());
 
        // There may be initial text in the field, so we may need to display the  button
        handleClearButton();
 
        //if the Close image is displayed and the user remove his finger from the button, clear it. Otherwise do nothing
        this.setOnTouchListener(this);
 
        //if text changes, take care of the button
        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
 
            	EditTextWithClearButton.this.handleClearButton();
            }
 
            @Override
            public void afterTextChanged(Editable arg0) {
            }
 
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
    }
     
    //intercept Typeface change and set it with our custom font
//    public void setTypeface(Typeface tf, int style) {
//        if (style == Typeface.BOLD) {
//            super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/Vegur-B 0.602.otf"));
//        } else {
//            super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/Vegur-R 0.602.otf"));
//        }
//    }
     
    void handleClearButton() {
        if (this.getText().toString().equals(""))
        {
            // remove the clear button
            this.setCompoundDrawables(this.getCompoundDrawables()[0], this.getCompoundDrawables()[1], null, this.getCompoundDrawables()[3]);
        }
        else
        {
            //add clear button
            this.setCompoundDrawables(this.getCompoundDrawables()[0], this.getCompoundDrawables()[1], imgCloseButton, this.getCompoundDrawables()[3]);
        }
    }

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if (getCompoundDrawables()[2] != null) {
			boolean tappedX = event.getX() > (getWidth()
					- getPaddingRight() - imgCloseButton.getIntrinsicWidth());
			if (tappedX) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					setText("");
					EditTextWithClearButton.this.handleClearButton();      
				}
				return true;
			}
		}    	
		return super.onTouchEvent(event);
	}
}
//public class EditTextWithClearButton {
//
//}
