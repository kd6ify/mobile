package com.futureconcepts.ax.trinity.tasks;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Tactic;
import com.futureconcepts.ax.model.data.TacticPriority;
import com.futureconcepts.ax.model.data.TacticStatus;
import com.futureconcepts.ax.trinity.DefaultOnItemSelectedListener;
import com.futureconcepts.ax.trinity.DefaultTextWatcher;
import com.futureconcepts.ax.trinity.EditModelActivity;
import com.futureconcepts.ax.trinity.GPS;
import com.futureconcepts.ax.trinity.GPS.GpsOnLocationChangeNotifier;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.logs.images.CustomAlertDialog;
import com.futureconcepts.ax.trinity.widget.EditTextWithClearButton;
import com.futureconcepts.ax.trinity.widget.EditTextWithDateSelection;

public class AddEditPriorityTaskActivity extends AddEditTaskBaseActivity implements GpsOnLocationChangeNotifier {
	
	private Spinner spinnerPriority;
	private Spinner spinnerStatus;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setEditContentView(R.layout.add_edit_priority_task);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		taskName = (EditTextWithClearButton)findViewById(R.id.taskName);
		startTimeField = (EditTextWithDateSelection)findViewById(R.id.ptask_start_time);
		endTimeField = (EditTextWithDateSelection)findViewById(R.id.ptask_end_time);
		simpleFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		DateTime.setContext(this);
		DateTimeZone.setProvider(null);
		startManagingModel(_tactic = Tactic.query(this, getIntent().getData()));
		if(beginEditReady()){
		 if (_action != null)
         {
         	if (_action.equals(Intent.ACTION_INSERT))
         	{
         		setTitle("Add New PriorityTask");
         		gps = new GPS(this,this);
         		addLocation = true;
         		taskName.setError(taskNameError);
         		lastLocation();
         	}
         	else if (_action.equals(Intent.ACTION_EDIT))
         	{
         		setTitle("Editing PriorityTask: "+_tactic.getName());
         		displayTacticValues();
         	}
         }
		 setOnClickListener(R.id.btn_done, new OnClickListener() {
 			public void onClick(View v)
             {
 				handleRequiredFields();
             }
         });
		 setUpTextFields();
		 setUpSpinners();
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
	
	private void lastLocation()
	{
		if(GPS.lastKnownLocation!=null){
			updateLocationValues(""+GPS.lastKnownLocation.getLatitude(),""+GPS.lastKnownLocation.getLongitude(),
				"Address (Last Location Remembered)");
		}
	}
	
	private void displayTacticValues()
	{
		String[] latLong = getGeoPointFromWKT( _tactic.getAddress(this));
		updateLocationValues(latLong[0],latLong[1],"Address (Location cannot be updated from Mobile devices.)");
		taskName.setText(_tactic.getName());
		startTimeField.setText(getFormattedLocalTime(_tactic.getStart(),""));
		endTimeField.setText(getFormattedLocalTime(_tactic.getEnd(),""));
		((TextView)findViewById(R.id.pTask_notes)).setText(_tactic.getNotes());
		
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
		setTextListener(R.id.ptask_start_time, new DefaultTextWatcher() {
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
		setTextListener(R.id.ptask_end_time, new DefaultTextWatcher() {
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
					}
					else if(!hasText(endTimeField)){
						endTimeField.setError(null);
					}else{
						endTimeField.setError(endTimeError);
					}
					
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		});
		
		setTextListener(R.id.pTask_notes, new DefaultTextWatcher() {
			@Override
			public void afterTextChanged(Editable s)
			{
				_tactic.setNotes(s.toString());
			}
		});
		
	}
	
	private void setUpSpinners()
	{
		spinnerPriority = (Spinner) findViewById(R.id.spinnerPriority);
		spinnerPriority.setSelection(getPriorityPosition(_tactic.getPriorityID()), true);
		setSpinnerListener(spinnerPriority.getId(), new DefaultOnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
			{
				_tactic.setPriority(getPriorityIdByPosition(position));
				spinnerPriority.setSelection(position);
			}
        });
		spinnerStatus = (Spinner) findViewById(R.id.spinnerStatus);
		spinnerStatus.setSelection(getStatusPosition(_tactic.getStatusID()), true);
		setSpinnerListener(spinnerStatus.getId(), new DefaultOnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
			{
				_tactic.setStatusID(getStatusIdByPosition(position));
				spinnerStatus.setSelection(position);
			}
        });
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
	
	
	private int getPriorityPosition(String priorityID)
	{
		if(TacticPriority.NORMAL.equals(priorityID))
			return 0;
		else
			return 1;
		
	}
	
	private String getPriorityIdByPosition(int position)
	{
		switch(position)
		{
			case 0: return TacticPriority.NORMAL;
			default: return  TacticPriority.IMMEDIATE;
		}		
	}
	
	private String getStatusIdByPosition(int position)
	{
		switch(position)
		{
			case 0: return TacticStatus.ACTIVE;
			case 1: return TacticStatus.COMPLETE;
			default: return TacticStatus.PENDING;
		}
	}
	
	private int getStatusPosition(String statusId)
	{
		if(TacticStatus.ACTIVE.equals(statusId))
			return 0;
		else if(TacticStatus.COMPLETE.equals(statusId))
			return 1;
		else
			return 2;
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
