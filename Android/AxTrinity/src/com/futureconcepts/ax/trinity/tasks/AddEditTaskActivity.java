package com.futureconcepts.ax.trinity.tasks;


import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.futureconcepts.ax.model.data.OperationalPeriod;
import com.futureconcepts.ax.model.data.Tactic;
import com.futureconcepts.ax.model.data.TacticType;
import com.futureconcepts.ax.trinity.DefaultOnItemSelectedListener;
import com.futureconcepts.ax.trinity.DefaultTextWatcher;
import com.futureconcepts.ax.trinity.EditModelActivity;
import com.futureconcepts.ax.trinity.GPS;
import com.futureconcepts.ax.trinity.GPS.GpsOnLocationChangeNotifier;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.widget.EditTextWithClearButton;
import com.futureconcepts.ax.trinity.widget.EditTextWithDateSelection;
import com.futureconcepts.gqueue.MercurySettings;

public class AddEditTaskActivity extends AddEditTaskBaseActivity implements GpsOnLocationChangeNotifier {

	private CheckBox checkBoxVehicle;
	private CheckBox checkBoxOther;
	private Spinner spinnerTaskType;
	private Spinner spinnerOperationalPeriod;
	private OperationalPeriod _period;
	private TacticType _tacticType;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setEditContentView(R.layout.add_edit_task);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		DateTime.setContext(this);
		DateTimeZone.setProvider(null);	
		simpleFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		taskName = (EditTextWithClearButton)findViewById(R.id.taskName);
		startTimeField = (EditTextWithDateSelection)findViewById(R.id.task_start_time);
		endTimeField = (EditTextWithDateSelection)findViewById(R.id.task_end_time);
		startManagingModel(_tactic = Tactic.query(this, getIntent().getData()));
		Log.e("","inside task id: "+_tactic.getID());
		if (beginEditReady())
	    {		
			if (_action != null)
	         {
				String incidentId = MercurySettings.getCurrentIncidentId(this);
    			startManagingCursor(_period = OperationalPeriod.queryIncident(this, incidentId));
    			startManagingCursor(_tacticType = TacticType.query(this, TacticType.CONTENT_URI));
	         	if (_action.equals(Intent.ACTION_INSERT))
	         	{
	         		setTitle("Add New Task");
	         		gps = new GPS(this,this);
	         		addLocation = true;
	         		lastLocation();	         		
	    			taskName.setError(taskNameError);	    		
	         	}
	         	else if (_action.equals(Intent.ACTION_EDIT))
	         	{
	         		setTitle("Editing Task: "+_tactic.getName());
	         		displayTacticValues();
	         	}
	         }
			setOnClickListener(R.id.btn_done, new OnClickListener() {
    			public void onClick(View v)
                {
    				handleRequiredFields();
                }
            });
			setUpSpinners();
			setUpTextFields();			
			setUpCheckBoxs();
			listenForChanges();
	    }
		 
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if(gps!=null)
			gps.removeListener(this);
		super.onDestroy();
	}
	
	private void displayTacticValues()
	{
		String[] latLong = getGeoPointFromWKT( _tactic.getAddress(this));
		updateLocationValues(latLong[0],latLong[1],"Address (Location cannot be updated from Mobile devices.)");
		taskName.setText(_tactic.getName());
		startTimeField.setText(getFormattedLocalTime(_tactic.getStart(),""));
		endTimeField.setText(getFormattedLocalTime(_tactic.getEnd(),""));
		((TextView)findViewById(R.id.task_notes)).setText(_tactic.getNotes());
		
	}
	
	private void setUpSpinners()
	{
		spinnerTaskType = (Spinner) findViewById(R.id.spinnerTaskType);
		spinnerTaskType.setAdapter(createApdater(_tacticType, new String[]{TacticType.NAME}));
		_tacticType.moveToPosition(_tactic.getTypeID());
		spinnerTaskType.setSelection(_tacticType.getPosition(), true);
		setSpinnerListener(R.id.spinnerTaskType, new DefaultOnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
			{
				_tacticType.moveToPosition(position);
				_tactic.setTypeID(_tacticType.getID());
				spinnerTaskType.setSelection(_tacticType.getPosition());
			}
        });
		spinnerOperationalPeriod = (Spinner) findViewById(R.id.spinnerOperationalPeriod);		
		spinnerOperationalPeriod.setAdapter(createApdater(_period, new String[]{OperationalPeriod.NAME}));
		movePeriodToPosition();
		spinnerOperationalPeriod.setSelection(_period.getPosition(), true);
		setSpinnerListener(spinnerOperationalPeriod.getId(), new DefaultOnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
			{
				_period.moveToPosition(position);
				Log.e("", "period selected : "+_period.getName());
				_tactic.setOperationalPeriod(_period.getID());
				spinnerOperationalPeriod.setSelection(position);
			}
        });
	}
	
	private SimpleCursorAdapter createApdater(Cursor cursor, String[] field)
	{
		 SimpleCursorAdapter adapter =
		 new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor, field,  new int[]{android.R.id.text1} );
		 adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
		 return adapter;
	}
	
	private void movePeriodToPosition()
	{
		_period.moveToFirst();
		int position =_period.getPosition();
		if(_period.getCount()>1)
		{
			boolean next = true;
			do
			{
				if(_period.getID().equals(_tactic.getOperationalPeriodId()))
				{
					position =_period.getPosition();
					 Log.e("asd", "PeriodPosition Found "+position);
					 next=false;
				}				
			}while(_period.moveToNext() && next);			
		}
		_period.moveToPosition(position);
		Log.e("asd", "PeriodPosition: "+_period.getPosition());
	}
	
	private void setUpTextFields()
	{
		setTextListener(R.id.taskName, new DefaultTextWatcher() {
			@Override
			public void afterTextChanged(Editable s)
			{
				if(hasText(taskName)){
					_tactic.setName(s.toString());
					taskName.setError(null);
					taskName.setCompoundDrawables(null, null,taskName.imgCloseButton,null);
				}else{
					taskName.setError(taskNameError);
				}
			}
		});
		setTextListener(R.id.task_start_time, new DefaultTextWatcher() {
			@Override
			public void afterTextChanged(Editable s)
			{
				try {					
					if(hasText(startTimeField) && isValidDateTimeString(s.toString()))
					{
						DateTime dtime = new DateTime(simpleFormat.parse(s.toString()));
						_tactic.setStart(dtime);
						Log.e("asd",dtime.toString());
						startTimeField.setError(null);
					}else if(!hasText(startTimeField)){
						startTimeField.setError(null);
					}else{
						startTimeField.setError(startTimeError);
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		});
		setTextListener(R.id.task_end_time, new DefaultTextWatcher() {
			@Override
			public void afterTextChanged(Editable s)
			{
				try {
					if(hasText(endTimeField) && isValidDateTimeString(s.toString()))
					{
						DateTime dtime = new DateTime(simpleFormat.parse(s.toString()));
						_tactic.setEnd(dtime);
						Log.e("asd",dtime.toString());
						endTimeField.setError(null);
					}else if(!hasText(startTimeField)){
						endTimeField.setError(null);
					}else{
						endTimeField.setError(endTimeError);
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		});
		
		setTextListener(R.id.task_notes, new DefaultTextWatcher() {
			@Override
			public void afterTextChanged(Editable s)
			{
				_tactic.setNotes(s.toString());
			}
		});		
		setTextListener(R.id.editTextOther, new DefaultTextWatcher() {
			@Override
			public void afterTextChanged(Editable s)
			{
				_tactic.setRequirementsDescription(s.toString());
			}
		});
		setTextListener(R.id.editTextVehicle, new DefaultTextWatcher() {
			@Override
			public void afterTextChanged(Editable s)
			{
				_tactic.setSpecialEquipment(s.toString());
			}
		});
	}
	
	private void setUpCheckBoxs()
	{
		CheckBox checkBoxRadio= (CheckBox)findViewById(R.id.checkBoxRadio);
		checkBoxRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		       @Override
		       public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
		    	   if(isChecked){
		    	   	   _tactic.setRequiresRadio(1);
		    	   }else
		    		   _tactic.setRequiresRadio(0);
		       }
		   }
		); 
		
		checkBoxVehicle = (CheckBox)findViewById(R.id.checkBoxVehicle);
		 checkBoxVehicle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		       @Override
		       public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
		    	   if(isChecked){
		    		   findViewById(R.id.editTextVehicle).setVisibility(View.VISIBLE);
		    	   	   _tactic.setRequiresVehicle(1);
		    	   }else{
		    		   findViewById(R.id.editTextVehicle).setVisibility(View.GONE);
		    		   _tactic.setRequiresRadio(0);
		    	   }
		       }
		   }
		); 
		 
		 checkBoxOther = (CheckBox)findViewById(R.id.checkBoxOther);
		 checkBoxOther.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		       @Override
		       public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
		    	   if(isChecked){
		    		   findViewById(R.id.editTextOther).setVisibility(View.VISIBLE);
		    		   _tactic.setRequiresOther(1);
		    	   }else{
		    		   findViewById(R.id.editTextOther).setVisibility(View.GONE);
		    		   _tactic.setRequiresRadio(0);
		    	   }
		       }
		   }
		); 
	}
	
	private void handleRequiredFields()
	{
		if(hasText(taskName) && startTimeField.getError()== null && endTimeField.getError()== null){			
			if(_action==Intent.ACTION_INSERT)
				handleCurrentLocation();
			else
				finish();
		}else if(startTimeField.getError()!= null){
			Toast.makeText(this, startTimeError, Toast.LENGTH_SHORT).show();
		}else if(endTimeField.getError()!= null){
			Toast.makeText(this, endTimeError, Toast.LENGTH_SHORT).show();
		}else
			Toast.makeText(this, taskNameError, Toast.LENGTH_SHORT).show();			
	}
	
    
    private void lastLocation()
	{
		if(GPS.lastKnownLocation!=null){
			updateLocationValues(""+GPS.lastKnownLocation.getLatitude(),""+GPS.lastKnownLocation.getLongitude(),
				"Address (Last Location Remembered)");
		}
	}

    private void updateLocationValues(String lat, String longitude, String text)
	{
		((TextView)findViewById(R.id.task_lat)).setText("Latitude: "+longitude);
		((TextView)findViewById(R.id.task_long)).setText("Longitude: "+lat);
		((TextView)findViewById(R.id.task_address_text)).setText(text);
	}
    
	@Override
	public void gpsLocationChange(Location point) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Location Adquired", Toast.LENGTH_SHORT).show();
		updateLocationValues(""+point.getLatitude(),""+point.getLongitude(),
				"Address (Current Location Acquired)");
	}

}
