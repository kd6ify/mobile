package com.futureconcepts.jupiter;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ToggleButton;

public class TrackRecorderToggleActivity extends Activity
{
	@Override
    public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
    	setContentView(R.layout.recorder_toggle);
    	ToggleButton toggleButton = (ToggleButton)findViewById(R.id.toggle_button);
    	toggleButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v)
            {
				toggleClick(v);
            }
    	});
    	Button cancelButton = (Button)findViewById(R.id.cancel_button);
    	cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v)
            {
				finish();
            }
    	});
    	if (TrackRecorderService.getInstance() != null)
    	{
    		toggleButton.setChecked(true);
    	}
    	else
    	{
    		toggleButton.setChecked(false);
    	}
	}

	private void toggleClick(View v)
    {
		ToggleButton toggleButton = (ToggleButton)v;
		if (toggleButton.isChecked())
		{
			TrackRecorderService.startIfNeccessary(this, null);
		}
		else
		{
			TrackRecorderService.stopIfNeccessary(this);
		}
		finish();
    }
}
