package com.futureconcepts.ax.trinity.triage;

import com.futureconcepts.ax.trinity.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;

public class EnterTrackingIdActivity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.triage_enter_tracking_id);
        setResult(Activity.RESULT_CANCELED);
    }
    
    public void onEnterClicked(View view)
    {
    	EditText editText = (EditText)findViewById(R.id.tracking_id);
    	Editable text = editText.getText();
    	String value = text.toString();
    	if (value == null || value.length() == 0)
    	{
    		editText.setText("Unknown");
    		value = "Unknown";
    	}
    	Intent resultIntent = new Intent();
    	resultIntent.putExtra("tracking_id", value);
    	setResult(Activity.RESULT_OK, resultIntent);
    	finish();
    }
    
    public void goBack(View view)
	{
		finish();
	}
}
