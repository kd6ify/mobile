package com.futureconcepts.jupiter;

import com.futureconcepts.jupiter.data.Folder;

import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class EditTripActivity extends Activity
{
	private static final String TAG = "EditTripActivity";	
	
	private Uri _uri;
	
	private Config _config;
	
	private Folder _trip;
		
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_trip);
        _config = Config.getInstance(this);
        _uri = getIntent().getData();
        _trip = new Folder(managedQuery(_uri, null, null, null, null));
        _trip.moveToFirst();
		setTitle("Edit Trip: " + _trip.getName());
		if (_trip.getName() != null)
		{
			setTextView(R.id.name, _trip.getName());
		}
		else
		{
			setTextView(R.id.name, _trip.get_ID());
		}
		if (_trip.getDescription() != null)
		{
			setTextView(R.id.description, _trip.getDescription());
		}
		findViewById(R.id.select).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				_config.setTripId(_trip.getId());
				_config.setTripUri(_uri.toString());
				finish();
			}
		});
		findViewById(R.id.save).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				updateTrip();
				finish();
			}
		});
		findViewById(R.id.delete).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				deleteTrip();
				finish();
			}
		});
    }

    private void setTextView(int resId, String value)
	{
		TextView view = (TextView)findViewById(resId);
		view.setText(value);
	}
    
    private String getTextFromView(int resId)
    {
    	TextView view = (TextView)findViewById(resId);
    	return view.getText().toString();
    }
    
    private void updateTrip()
    {
    	try
    	{
			ContentValues values = new ContentValues();
			values.put(Folder.NAME, getTextFromView(R.id.name));
			values.put(Folder.DESCRIPTION, getTextFromView(R.id.description));
			values.put(Folder.LAST_MODIFIED_TIME, System.currentTimeMillis());
			getContentResolver().update(_uri, values, null, null);
			Log.d(TAG, "updated: " + _uri.toString());
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    }

    private void deleteTrip()
    {
    	try
    	{
			if (_uri != null)
			{
				getContentResolver().delete(_uri, null, null);
				Log.d(TAG, "deleted: " + _uri.toString());
			}
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    }
}
