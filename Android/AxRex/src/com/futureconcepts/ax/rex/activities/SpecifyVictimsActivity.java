package com.futureconcepts.ax.rex.activities;

import com.futureconcepts.ax.model.data.IncidentRequest;
import com.futureconcepts.ax.rex.R;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;

public class SpecifyVictimsActivity extends Activity
{
	private Uri _data;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.specify_victims_activity);
        _data = getIntent().getData();
    }
    
    public void onVictimsClick(View view)
    {
    	update(true);
    	next();
    }
    
    public void onNoVictimsClick(View view)
    {
    	update(false);
    	next();
    }

    private void update(boolean hasVictims)
    {
    	try
    	{
	    	ContentValues values = new ContentValues();
	    	values.put(IncidentRequest.HAS_VICTIMS, hasVictims ? 1 : 0);
	    	getContentResolver().update(_data, values, null, null);
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    }

    private void next()
    {
    	Intent intent = new Intent(this, SelectMediaActivity.class);
    	intent.setData(_data);
    	startActivity(intent);
    	finish();
    }
    
    public void onDoneClick(View view)
    {
    	Intent intent = new Intent(this, SubmitRequestActivity.class);
    	intent.setData(_data);
    	startActivity(intent);
    }
}
