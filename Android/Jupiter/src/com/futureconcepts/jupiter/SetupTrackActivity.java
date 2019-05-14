package com.futureconcepts.jupiter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import com.futureconcepts.jupiter.R;
import com.futureconcepts.jupiter.data.Folder;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class SetupTrackActivity extends Activity
{
//	private static final String TAG = "SetupTrackActivity";
	
	public static final String EXTRA_RECORD_METHOD = "RecordMethod";
	public static final String EXTRA_INTERVAL = "Interval";
	
	private ArrayAdapter<CharSequence> _recordMethodAdapter;
	private ArrayAdapter<CharSequence> _intervalAdapter;
	
	private Spinner _recordMethodSpinner;
	private Spinner _intervalSpinner;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_track);
        setupRecordMethodSpinner();
        setupIntervalSpinner();
        Button button = (Button)findViewById(R.id.ok);
        button.setOnClickListener(new OnClickListener() {
			public void onClick(View v)
            {
				Intent intent = getIntent();
				intent.putExtra(EXTRA_RECORD_METHOD, (String)_recordMethodSpinner.getSelectedItem());
				intent.putExtra(EXTRA_INTERVAL, (String)_intervalSpinner.getSelectedItem());
				intent.setData(createRoute());
				setResult(RESULT_OK, intent);
				finish();
            }
        });
        button = (Button)findViewById(R.id.cancel);
        button.setOnClickListener(new OnClickListener() {
			public void onClick(View v)
            {
				setResult(RESULT_CANCELED);
				finish();
            }
        });
    }

    private void setupRecordMethodSpinner()
    {
	   	_recordMethodAdapter = ArrayAdapter.createFromResource(this, R.array.record_method, android.R.layout.simple_spinner_item);
	    _recordMethodAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_1);
        _recordMethodSpinner = (Spinner)findViewById(R.id.record_method);
        _recordMethodSpinner.setAdapter(_recordMethodAdapter);
    }
    
    private void setupIntervalSpinner()
    {
    	_intervalAdapter = ArrayAdapter.createFromResource(this, R.array.interval, android.R.layout.simple_spinner_item);
    	_intervalAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_1);
        _intervalSpinner = (Spinner)findViewById(R.id.interval);
        _intervalSpinner.setAdapter(_intervalAdapter);
    }
    
	private Uri createRoute()
	{
		Uri result = null;
		Folder tracksFolder = Folder.getTripFolder(this, Folder.NAME_TRACKS);
		if (tracksFolder != null)
		{
			SimpleDateFormat format = new SimpleDateFormat();
			String routeId = UUID.randomUUID().toString().toLowerCase();
			ContentValues values = new ContentValues();
			values.put(Folder.ID, routeId);
			values.put(Folder.PARENT_ID, tracksFolder.getId());
			values.put(Folder.NAME, "Route " + format.format(new Date(System.currentTimeMillis())));
			result = getContentResolver().insert(Folder.CONTENT_URI, values);
			tracksFolder.close();
		}
		return result;
	}
}
