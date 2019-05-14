package com.futureconcepts.jupiter;

import com.futureconcepts.jupiter.data.Folder;

import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class EditFolderActivity extends Activity
{
	private static final String TAG = "EditFolderActivity";	
	private Uri _uri;
	private Folder _folder;
	private boolean _revertChanges = false;
		
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_frame);
        getLayoutInflater().inflate(R.layout.edit_folder, (ViewGroup)findViewById(R.id.editors));
        _uri = getIntent().getData();
        _folder = new Folder(managedQuery(_uri, null, null, null, null));
        _folder.moveToFirst();
		setTitle("Edit " + _folder.getName());
		if (_folder.getName() != null)
		{
			setTextView(R.id.name, _folder.getName());
		}
		else
		{
			setTextView(R.id.name, _folder.getId());
		}
		if (_folder.getDescription() != null)
		{
			setTextView(R.id.description, _folder.getDescription());
		}
		findViewById(R.id.btn_done).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
		findViewById(R.id.btn_discard).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				_revertChanges = true;
				finish();
			}
		});
    }

    @Override
    public void onPause()
    {
    	super.onPause();
    	if (isFinishing())
    	{
    		if (_revertChanges)
    		{
    			Toast.makeText(this, "All changes discarded", Toast.LENGTH_LONG).show();
    		}
    		else
    		{
	        	try
	        	{
	    			ContentValues values = new ContentValues();
	    			String name = getTextFromView(R.id.name);
	    			values.put(Folder.NAME, name);
	    			values.put(Folder.DESCRIPTION, getTextFromView(R.id.description));
	    			values.put(Folder.LAST_MODIFIED_TIME, System.currentTimeMillis());
	    			getContentResolver().update(_uri, values, null, null);
	    			Log.d(TAG, "updated: " + _uri.toString());
	    			Toast.makeText(this, "Committed changes to " + name, Toast.LENGTH_LONG).show();
	        	}
	        	catch (Exception e)
	        	{
	        		e.printStackTrace();
	        	}
    		}
    	}
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
}
