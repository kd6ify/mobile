package com.futureconcepts.jupiter;

import com.futureconcepts.awt.geom.Point2D;
import com.futureconcepts.jupiter.data.Placemark;
import com.futureconcepts.jupiter.util.FormatterFactory;
import com.futureconcepts.jupiter.util.LocationValidatorFactory;
import com.futureconcepts.jupiter.util.FormatterFactory.LocationFormatter;
import com.futureconcepts.jupiter.util.LocationValidatorFactory.LocationValidator;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class EditPlacemarkActivity extends Activity
{
	private static final String TAG = "EditPlacemarkActivity";
	private Config _config;
	private Uri _uri;
	private Placemark _placemark;
	private boolean _revertChanges = false;
	private FormatterFactory _formatterFactory;
	private LocationFormatter _locationFormatter;
	private LocationValidator _locationValidator;
	private Spinner _locationFormatSpinner;
	private TextView _locationView;
	private Point2D.Double _validatedLocation;
		
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        _config = Config.getInstance(this);
        setContentView(R.layout.edit_frame);
        getLayoutInflater().inflate(R.layout.edit_placemark, (ViewGroup)findViewById(R.id.editors));
        _locationValidator = LocationValidatorFactory.getLocationValidator(_config.getLocationFormat());
        _locationView = (TextView)findViewById(R.id.location);
        _locationView.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (hasFocus == false)
				{
					_validatedLocation = _locationValidator.validate(_locationView.getText().toString());
					_locationView.setText(_locationFormatter.format(_validatedLocation));
				}
			}
        });
        _formatterFactory = new FormatterFactory(this);
        _locationFormatter = _formatterFactory.getLocationFormatter();
        _uri = getIntent().getData();
        _placemark = new Placemark(managedQuery(_uri, null, null, null, null));
        _placemark.moveToFirst();
		setTitle("Edit " + _placemark.getName());
		if (_placemark.getName() != null)
		{
			setTextView(R.id.name, _placemark.getName());
		}
		else
		{
			setTextView(R.id.name, _placemark.getId());
		}
		if (_placemark.getDescription() != null)
		{
			setTextView(R.id.description, _placemark.getDescription());
		}
		_validatedLocation = new Point2D.Double(_placemark.getLongitude(), _placemark.getLatitude());
		_locationFormatSpinner = (Spinner)findViewById(R.id.location_format);
		setLocationFormatSpinner(_config.getLocationFormat());
		_locationFormatSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				String format = (String)_locationFormatSpinner.getSelectedItem();
				_locationFormatter = _formatterFactory.getLocationFormatter(format);
				if (_validatedLocation != null)
				{
					setTextView(R.id.location, _locationFormatter.format(_validatedLocation));
				}
				Log.d(TAG, "onItemSelected");
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
				Log.d(TAG, "onNothingSelected");
			}
		});
		findViewById(R.id.btn_done).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (_uri != null)
				{
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.addCategory(JIntent.CATEGORY_MAP);
					intent.setData(_uri);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
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
    				values.put(Placemark.NAME, name);
    				values.put(Placemark.DESCRIPTION, getTextFromView(R.id.description));
    				values.put(Placemark.LATITUDE, _validatedLocation.y);
    				values.put(Placemark.LONGITUDE, _validatedLocation.x);
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

    @Override
    public void onResume()
    {
    	super.onResume();
    	if (_placemark != null)
    	{
    		_placemark.moveToFirst();
    	}
    }

    private void setLocationFormatSpinner(String value)
    {
    	String[] entries = getResources().getStringArray(R.array.location_format_values);
    	for (int i = 0; i < entries.length; i++)
    	{
    		if (entries[i].equals(value))
    		{
    			_locationFormatSpinner.setSelection(i);
    			return;
    		}
    	}
    }
    
    private void setTextView(int resId, String value)
	{
		TextView view = (TextView)findViewById(resId);
		view.setText(value);
	}
    
    private float getFloatFromView(int resId)
    {
    	TextView view = (TextView)findViewById(resId);
    	return Float.valueOf(view.getText().toString());
    }
    
    private String getTextFromView(int resId)
    {
    	TextView view = (TextView)findViewById(resId);
    	return view.getText().toString();
    }
    
    private String getStringFromSpinner(int resId)
    {
    	Spinner spinner = (Spinner)findViewById(resId);
    	String selectedValue = (String)spinner.getSelectedItem();
    	return selectedValue;
    }
}
