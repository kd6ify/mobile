package com.futureconcepts.ax.rex.activities;

import com.futureconcepts.ax.model.data.IncidentRequest;
import com.futureconcepts.ax.rex.R;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;

public class InputDescriptionActivity extends Activity
{
	private Uri _data;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_description_activity);
        _data = getIntent().getData();
    }
    
    public void onDoneClick(View view)
    {
    	ContentValues values = new ContentValues();
    	values.put(IncidentRequest.INCIDENT_NAME, getTextViewContent(R.id.incident_name));
    	values.put(IncidentRequest.DESCRIPTION, getTextViewContent(R.id.description));
    	getContentResolver().update(_data, values, null, null);
    	Intent intent = new Intent(this, SubmitRequestActivity.class);
    	intent.setData(_data);
    	startActivity(intent);
    	finish();
    }
    
    private String getTextViewContent(int resid)
    {
    	String result = null;
    	TextView view = (TextView)findViewById(resid);
    	if (view != null)
    	{
    		result = view.getText().toString();
    	}
    	return result;
    }
}
