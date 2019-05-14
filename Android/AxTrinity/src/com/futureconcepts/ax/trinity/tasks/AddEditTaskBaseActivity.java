package com.futureconcepts.ax.trinity.tasks;

import java.text.SimpleDateFormat;

import org.joda.time.DateTime;
import org.mapsforge.core.model.LatLong;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Tactic;
import com.futureconcepts.ax.trinity.EditModelActivity;
import com.futureconcepts.ax.trinity.GPS;
import com.futureconcepts.ax.trinity.logs.images.CustomAlertDialog;
import com.futureconcepts.ax.trinity.widget.EditTextWithClearButton;
import com.futureconcepts.ax.trinity.widget.EditTextWithDateSelection;

public class AddEditTaskBaseActivity extends EditModelActivity {
	protected GPS gps;
	protected AlertDialog gpsAlert = null;
	protected String taskNameError = "Task name is required";
	protected String startTimeError = "Invalid Start Time value";
	protected String endTimeError = "Invalid End Time value";
	protected SimpleDateFormat simpleFormat;
	protected EditTextWithClearButton taskName;
	protected EditTextWithDateSelection startTimeField;
	protected EditTextWithDateSelection endTimeField;
	protected Tactic _tactic;
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(gps!=null){
    		gps.removeLocationUpdates();
        }
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		checkGPS();
	}
	
	private void checkGPS() {
		if (!GPS.isGPSEnabled(getApplicationContext())) {
			buildAlertMessageNoGps();
		}
		if(gps!=null){
			Log.e("","requestClled");
			gps.requestLocationUpdates();
        }
		
	}
	
	public String getFormattedLocalTime(DateTime datetime, String nullText)
    {
    	String result = nullText;
    	if (datetime != null)
    	{
    		result = datetime.toLocalDateTime().toString("MM/dd/yy HH:mm");
    	}
    	return result;
    } 
	
	// check the input field has any text or not
    // return true if it contains text otherwise false
    protected  boolean hasText(TextView editText) {
 
        String text = editText.getText().toString().trim(); 
        // length 0 means there is no text
        if (text.length() == 0) {
         //   editText.setError(taskNameError);
            return false;
        } 
        return true;
    }

	    private void buildAlertMessageNoGps() {
			 if(gpsAlert==null){
			    AlertDialog.Builder builder = new AlertDialog.Builder(this);
			    builder.setMessage("Your GPS is disabled, do you want to enable it?")
			           .setCancelable(false)
			           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			               public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
			            	   gpsAlert=null;
			            	   startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			               }
			           })
			           .setNegativeButton("No", new DialogInterface.OnClickListener() {
			               public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
			                    dialog.cancel();
			                    gpsAlert =null;
			               }
			           });
			     gpsAlert = builder.create();
			     gpsAlert.show();
			}
		}
	
	protected void handleCurrentLocation() {
    	if(GPS.lastKnownLocation==null && addLocation){
		 String[] buttons = {"Wait","Continue"};
		 // Create an instance of the dialog fragment and show it
		 final CustomAlertDialog customDialog = new CustomAlertDialog(this, buttons,
				 "Information","Trinity is unable to determine the location of your phone. Would you like you like to wait until it finds your location?",
				 android.R.drawable.ic_dialog_alert,
				 new CustomAlertDialog.DialogButtonClickListener() {
					@Override
					public void onDialogButtonClick(View v) {	
						
						if("Continue".equals(v.getTag()))
						{
							addLocation = false;
							finish();
						}						
					}
				});
		 customDialog.show();
    	}else
    	{
    		finish();
    	}
	}
	
	protected boolean isValidDateTimeString(String value){
		String regex = "(0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])/((19|20)\\d\\d) ([01]?[0-9]|2[0-3]):[0-5][0-9]";
		if(value.matches(regex)){
			return true;
		}else{
			return false;
		}
	}
	
	protected static String[] getGeoPointFromWKT(Address address)
	{
		String result[] = new String[]{"",""};
		if(address!=null && address.moveToFirst() && address.getWKT()!=null){
			if(address.getWKT().contains("POINT"))
			{
				String a =  address.getWKT().replaceAll("[()]", "");
				String [] g = a.replaceAll("POINT", "").split(" ");
				result = new String[]{""+Double.parseDouble(g[1]),""+Double.parseDouble(g[0])};
			}else if(address.getWKT().contains("POLYGON"))
			{
				String a =  address.getWKT().replaceAll("[()]", "");
				String [] g = a.replaceAll("POLYGON", "").split(",");
				//String [] f = g[0].split(" ");
				result = new String[]{""+Double.parseDouble(g[1]),""+Double.parseDouble(g[0])};
			}
		}
		return result;
	}

}
