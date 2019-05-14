package com.futureconcepts.jupiter.compass;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.futureconcepts.awt.geom.Point2D;
import com.futureconcepts.jupiter.Config;
import com.futureconcepts.jupiter.PrefsActivity;
import com.futureconcepts.jupiter.R;
import com.futureconcepts.jupiter.data.Placemark;
import com.futureconcepts.jupiter.util.FormatterFactory;
import com.futureconcepts.jupiter.util.FormatterFactory.LocationTokenFormatter;
import com.futureconcepts.jupiter.util.FormatterFactory.ScalarFormatter;

public class MainCompassActivity extends Activity implements ICurrentLocationView {
	  
	private CompassEngine _engine;
	private ICompassView rose;
	private IWaypointView waypoint;
	private IGpsSatelliteView gpssats;
	
	private Config _config;
	private OnSharedPreferenceChangeListener _sharedPreferenceChangeListener;
	
	private FormatterFactory _formatterFactory;
	private ScalarFormatter formatSpeed;
	private ScalarFormatter formatDistance;
	private LocationTokenFormatter formatLocation;
	
	private Location _lastLocation;
	
	private TextView loc_first_label;
	private TextView loc_second_label;
	private TextView loc_third_label;
	private TextView loc_first_value;
	private TextView loc_second_value;
	private TextView loc_third_value;
	
	private LinearLayout loc_third_layout;
	
	private TextView loc_accuracy;
	private TextView loc_speed;
	private TextView loc_altitude;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
  
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);  
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  
        
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        _engine = new CompassEngine(this);
        
        setContentView(R.layout.compass_activity);
        fetchControls();
        
        _engine.addView(rose);
        _engine.addView(gpssats);
        _engine.addView(this);

        _formatterFactory = new FormatterFactory(this);
        formatLocation = _formatterFactory.getLocationTokenFormatter();
        formatDistance = _formatterFactory.getDistanceFormatter();
        formatSpeed = _formatterFactory.getVelocityFormatter();
        
        _config = Config.getInstance(this);
        _sharedPreferenceChangeListener = new OnSharedPreferenceChangeListener()
        {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
			{
				if (key.equals(Config.FORMAT_LOCATION))
				{
					formatLocation = _formatterFactory.getLocationTokenFormatter();
					updateLocation();
				}
				else if(key.equals(Config.FORMAT_DISTANCE))
				{
					formatDistance = _formatterFactory.getDistanceFormatter();
					updateDistance();
				}
				else if(key.equals(Config.FORMAT_VELOCITY))
				{
					formatSpeed = _formatterFactory.getVelocityFormatter();
					updateSpeed();
				}
				
			}
        };
        _config.getSharedPreferences().registerOnSharedPreferenceChangeListener(_sharedPreferenceChangeListener);
        
        updateLocation();
        updateDistance();
        updateSpeed();
        
        if(getIntent() != null)
        {
        	onNewIntent(getIntent());
        }
        else if(waypoint != null)
        {
        	waypoint.setShowWaypoint(false);
        }   
        /*
        Location test = new Location("test");
        test.setLatitude(34.100528);
        test.setLongitude(-117.818077);
        _engine.setWaypoint(test);*/
    }
   
    /**
     * gets the instances of our controls from the inflated XML
     */
    private void fetchControls()
    {
        rose = (ICompassView) findViewById(R.id.compass_activity_rose);
        waypoint = (IWaypointView)rose;
        gpssats = (IGpsSatelliteView) findViewById(R.id.compass_activity_gpssats);
        
        loc_first_label = (TextView)findViewById(R.id.compass_location_leftvalue_name);
    	loc_second_label = (TextView)findViewById(R.id.compass_location_rightvalue_name);
    	loc_third_label = (TextView)findViewById(R.id.compass_location_extra_name);
    	loc_first_value = (TextView)findViewById(R.id.compass_location_leftvalue);
    	loc_second_value = (TextView)findViewById(R.id.compass_location_rightvalue);
    	loc_third_value = (TextView)findViewById(R.id.compass_location_extra);
    	
    	loc_third_layout = (LinearLayout)findViewById(R.id.compass_location_extra_layout);
    	
    	loc_accuracy = (TextView)findViewById(R.id.compass_location_error);
    	loc_speed = (TextView)findViewById(R.id.compass_speed);
    	loc_altitude =(TextView)findViewById(R.id.compass_location_altitude);
    }
   
    @Override
    protected void onNewIntent(Intent intent)
    {
    	if(waypoint == null)
    	{
    		return;
    	}
    	
    	if(intent != null)
    	{
	        String action = intent.getAction();
	        Uri data = intent.getData();
	        if((action == null) || (data == null))
	        {
	        	return;
	        }
	        String contentType = getContentResolver().getType(data);
	        if ((action.equals(Intent.ACTION_VIEW)) &&
	        	(contentType.equals(Placemark.CONTENT_ITEM_TYPE)))
	        {
	        	 Placemark place = Placemark.getPlacemarkByUri(this, intent.getData());
	        	 _engine.setWaypoint(place.toLocation());
	        	 place.close();
	        	 return;
	        }
    	}
    }
    
    @Override
    public void onPause()
    {
    	_engine.pause();
    	
    	super.onPause();
    }
    
    @Override
    public void onResume()
    {
    	_engine.resume();
    	
    	super.onResume();
    }
    
    @Override
    public void onDestroy()
    {
    	_engine.close();
    	
    	super.onDestroy();
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.nav_computer_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_preferences:
			startActivity(new Intent(this, PrefsActivity.class));
			break;
		}
		return false;
	}
    
    
	@Override
	public Location getCurrentLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCurrentLocation(Location current)
	{
		_lastLocation = current;
		updateLocation();
	}
    
	/**
	 * Runs the last known location thru the formatter and drops the results into the textviews
	 */
	private void updateLocation()
	{
		if (_lastLocation == null)
		{
			loc_first_value.setText("--");
			loc_second_value.setText("--");
			loc_third_value.setText("--");
			loc_altitude.setText("--");
			loc_accuracy.setText("--");
			loc_speed.setText("--");
		}
		else
		{
			formatLocation.format(new Point2D.Double(_lastLocation.getLongitude(), _lastLocation.getLatitude()));
			boolean showZone = formatLocation.hasToken(FormatterFactory.LOCATION_TOKEN_EXTRA);
			loc_third_layout.setVisibility(showZone ? View.VISIBLE : View.GONE);
			if(showZone)
			{
				loc_third_label.setText(formatLocation.getLabel(FormatterFactory.LOCATION_TOKEN_EXTRA));
				loc_third_value.setText(formatLocation.getValue(FormatterFactory.LOCATION_TOKEN_EXTRA));
			}
			
			loc_first_label.setText(formatLocation.getLabel(FormatterFactory.LOCATION_TOKEN_FIRST));
			loc_first_value.setText(formatLocation.getValue(FormatterFactory.LOCATION_TOKEN_FIRST));
			
			loc_second_label.setText(formatLocation.getLabel(FormatterFactory.LOCATION_TOKEN_SECOND));
			loc_second_value.setText(formatLocation.getValue(FormatterFactory.LOCATION_TOKEN_SECOND));
			
			updateDistance();
			
			updateSpeed();
		}
	}
	
	private void updateDistance()
	{
		if(_lastLocation == null)
		{
			loc_altitude.setText("--");
			loc_accuracy.setText("--");
		}
		else
		{
			if(_lastLocation.hasAltitude())
			{
				loc_altitude.setText(formatDistance.format(_lastLocation.getAltitude()));
			}
			else
			{
				loc_altitude.setText("--");
			}
			
			if(_lastLocation.hasAccuracy())
			{
				loc_accuracy.setText(formatDistance.format(_lastLocation.getAccuracy()));
			}
			else
			{
				loc_accuracy.setText("--");
			}
		}
	}
	
	private void updateSpeed()
	{
		if(_lastLocation == null)
		{
			loc_speed.setText("--");
		}
		else
		{
			if(_lastLocation.hasSpeed())
			{
				loc_speed.setText(formatSpeed.format(_lastLocation.getSpeed()));
			}
			else
			{
				loc_speed.setText("--");
			}
		}
	}
}
