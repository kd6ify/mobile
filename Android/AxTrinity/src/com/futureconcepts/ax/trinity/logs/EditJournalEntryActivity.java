package com.futureconcepts.ax.trinity.logs;

import java.io.File;

import com.futureconcepts.ax.model.data.JournalEntry;
import com.futureconcepts.ax.model.data.JournalEntryPriorityBinding;
import com.futureconcepts.ax.model.data.JournalStatus;
import com.futureconcepts.ax.trinity.DefaultOnItemSelectedListener;
import com.futureconcepts.ax.trinity.DefaultTextWatcher;
import com.futureconcepts.ax.trinity.EditModelActivity;
import com.futureconcepts.ax.trinity.GPS;
import com.futureconcepts.ax.trinity.GPS.GpsOnLocationChangeNotifier;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.logs.images.CustomAlertDialog;
import com.futureconcepts.ax.trinity.logs.images.EntryImageObject;
import com.futureconcepts.ax.trinity.logs.images.EntryImageObjectManager;
import com.futureconcepts.ax.trinity.logs.images.GetImageBitmap;
import com.futureconcepts.ax.trinity.logs.images.ImageAdapter;
import com.futureconcepts.cameraintent.TakePhoto;
import com.futureconcepts.cameraintent.TakePhotoIntentGlobals;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class EditJournalEntryActivity extends EditModelActivity implements GpsOnLocationChangeNotifier
{
	private JournalEntry _journalEntry;
	private JournalStatus _journalStatus;
	private ImageAdapter imageAdapter;
	private  GridView imageContainer;
	private View _btnDone;
	private GPS gps;
	private AlertDialog gpsAlert = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setEditContentView(R.layout.edit_log_entry);
    	startManagingModel(_journalEntry = JournalEntry.query(this, getIntent().getData()));
    	if (beginEditReady())
    	{
            setTextView(R.id.text1, _journalEntry.getText());
//        	setupIndexedTypeSpinner(R.id.log_type, _journalEntry.getType());
    		startManagingCursor(_journalStatus = JournalStatus.query(this));
    		_journalStatus.moveToPosition(_journalEntry.getStatusID());    		
        	setupIndexedTypeSpinner(R.id.log_status, _journalStatus);
//        	setSpinnerListener(R.id.log_type, new DefaultOnItemSelectedListener() {
//    			@Override
//    			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
//    				JournalType type = _journalEntry.getType();
//    				if (type != null)
//    				{
//    					type.moveToPosition(position);
//   					_journalEntry.setTypeID(type.getID());
//    				}
//    			}
//           });
        	setSpinnerListener(R.id.log_status, new DefaultOnItemSelectedListener() {
    			@Override
    			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
    			{
					_journalStatus.moveToPosition(position);
					_journalEntry.setStatusID(_journalStatus.getID());
					Spinner spinner = (Spinner)arg0;
					spinner.setSelection(position);
    			}
            });
        	setupPrioritySpinner();
        	setSpinnerListener(R.id.log_priority, new DefaultOnItemSelectedListener() {
        		@Override
        		public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
        		{
        			Spinner spinner = (Spinner)arg0;
        			String value = (String)spinner.getAdapter().getItem(position);
    				_journalEntry.setPriority(JournalEntryPriorityBinding.stringToInt(value));
        		}
        	});
            setTextListener(R.id.text1, new DefaultTextWatcher() {
				@Override
				public void afterTextChanged(Editable s)
				{
					_journalEntry.setText(s.toString());
				}
    		});
            if (_action != null)
            {
            	if (_action.equals(Intent.ACTION_INSERT))
            	{
            		setTitle("Add Entry");
            		EntryImageObjectManager.images.clear();
            		gps = new GPS(getApplicationContext(),this);//start GPS TRAKING
            		addLocation = true;
            		lastLocation();
            		setOnClickListener(R.id.btn_done, new OnClickListener() {
            			public void onClick(View v)
                        {
            				handleCurrentLocation();
                        }
                    });
            	}
            	else if (_action.equals(Intent.ACTION_EDIT))
            	{
            		setTitle("Edit Entry");
            		((LinearLayout)findViewById(R.id.locationContainer)).setVisibility(View.GONE);
            		//((CheckBox)findViewById(R.id.checkBoxGPS)).setText("Is not allowed to edit the location");
            		//((CheckBox)findViewById(R.id.checkBoxGPS)).setClickable(false);
            	}
            }
            listenForChanges();
    	}
    	setGridView();
    }
        
    private void setupPrioritySpinner()
    {
    	Spinner spinner = (Spinner)findViewById(R.id.log_priority);
    	ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.log_priority, R.layout.simple_list_item_3);
    	adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_3);
    	spinner.setAdapter(adapter);
    	spinner.setSelection(_journalEntry.getPriority());
    }
    
    public void onPause()
    {
    	super.onPause();
    	if(gps!=null){
    		//location = gps.getCurrentLocation();
    		gps.removeLocationUpdates();
        }
    }

    public void onResume()
    {
    	if(EntryImageObjectManager.images!=null)
    	{
    		//EditText e =(EditText) findViewById(R.id.text1);
    		if(EntryImageObjectManager.images.size()>EntryImageObjectManager.imagesOnDatabase)
    		{
    			  _btnDone = findViewById(R.id.btn_done);
    			  _btnDone.setEnabled(true);
    		}
    	}
    	if(gps!=null){
    		checkGPS();
    	}
    	super.onResume();
    }
    public void onDestroy()
    {
    	super.onDestroy();
    	EntryImageObjectManager.images.clear();
    	if(gps!=null){
    		gps.removeLocationUpdates();
    		gps.removeListener(this);
    	}
    }
	public void setGridView() {
		EntryImageObjectManager.imagesOnDatabase(EntryImageObjectManager.images.size());
		imageContainer = (GridView) findViewById(R.id.grid_view);
		imageAdapter = new ImageAdapter(this,EntryImageObjectManager.images);
		imageContainer.setAdapter(imageAdapter);
		imageContainer.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,int position, long id) {
				imageActions(v, position);
			}
		});
	}    
    
    public void imageActions(final View v, final int position)
	  {		  
    	 String[] buttons = {"View Image","Cancel"};
    	 if((position+1)>EntryImageObjectManager.imagesOnDatabase)
    	 {
    		 buttons = new String[] {"View Image","Remove Image","Cancel"};
    	 }
		 // Create an instance of the dialog fragment and show it
		  CustomAlertDialog customDialog = new CustomAlertDialog(this, buttons,null,"Actions for this Image",android.R.drawable.ic_menu_info_details,
				 new CustomAlertDialog.DialogButtonClickListener() {
					@Override
					public void onDialogButtonClick(View v) {
						// TODO Auto-generated method stub
						if("View Image".equals(v.getTag()))
						{
							seeImage(v, position);								
						}else if("Remove Image".equals(v.getTag()))
						{
							deleteImage(v, position);
						}						
					}
				});
		 customDialog.show();
	 }
	  /**
	   * Displays the selected image
	   * */
	  public void seeImage(final View v, final int position)
	  {
		    Intent intent = new Intent();  
		    intent.setAction(android.content.Intent.ACTION_VIEW);  
		    File file = new File(EntryImageObjectManager.images.get(position).getImagePath());		  
		    intent.setDataAndType(Uri.fromFile(file), "image/*");  
		    startActivity(intent);	
	  }
	  
	  /**
	   * Deletes the path and bitmap of the selected image 
	   * */
	  public void deleteImage(final View v, final int position)
	  {		
			 //Buttons are organized left to right.
			 String[] buttons = {"OK","Cancel"};
			 // Create an instance of the dialog fragment and show it
		    CustomAlertDialog customDialog = new CustomAlertDialog(this, buttons,"Information","Are you sure you want to remove this image?",android.R.drawable.ic_dialog_alert,
					 new CustomAlertDialog.DialogButtonClickListener() {
						@Override
						public void onDialogButtonClick(View v) {
							// TODO Auto-generated method stub
							if("OK".equals(v.getTag()))
							{
								EntryImageObjectManager.images.remove(position);					
						    	  imageAdapter.notifyDataSetChanged();	
							}						
						}
					});
			 customDialog.show();
	  }
    
    public void selectImage(View view)
    {
    	if(EntryImageObjectManager.images.size()<5){
    		Intent i = new Intent(this, TakePhoto.class);
    		i.putExtra("action",TakePhotoIntentGlobals.SELECT_PICTURE );
    		startActivityForResult(i,TakePhotoIntentGlobals.SELECT_PICTURE);
    	}else
    	{
    		alertDialog("Information","Cannot add more than 5 images.");	 
    	}
    }

	public void takeImage(View view) {
		if (EntryImageObjectManager.images.size() < 5) {
			Intent i = new Intent(this, TakePhoto.class);
			i.putExtra("action", TakePhotoIntentGlobals.TAKE_PICTURE);
			i.putExtra("folderName", "Trinity");
			startActivityForResult(i, TakePhotoIntentGlobals.TAKE_PICTURE);
		} else {
			alertDialog("Information", "Cannot add more than 5 images.");
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			if (resultCode == RESULT_OK) {
				String filePath = data.getStringExtra("filePath");
				Bitmap imageBitmap = null;
				try {
					imageBitmap = GetImageBitmap.lessResolution(filePath,90,90);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (imageBitmap != null) {
					sameImage(filePath);
				} else {
					alertDialog("Warning","This file is corrupted. Please select a different image.");
				}
			} else if (resultCode == RESULT_CANCELED) {
					Toast.makeText(getApplicationContext(), "Action Canceled", Toast.LENGTH_SHORT).show();
					//alertDialog("", "The image can't be attached.");
			} else {
				alertDialog("", "The image can't be attached.");
			}
	}
    
	 /**
	 * Displays a AlertDialog with the title and message specified.
	 * @param title
	 * @param message
	 */
	public void alertDialog(String title,String message){
		
		 String[] buttons = {"OK"};
		 // Create an instance of the dialog fragment and show it
		 final CustomAlertDialog customDialog = new CustomAlertDialog(this, buttons,title,message,android.R.drawable.ic_menu_info_details,
				 new CustomAlertDialog.DialogButtonClickListener() {
					@Override
					public void onDialogButtonClick(View v) {									
					}
				});
		 customDialog.show();
	  }
    
    /**
     * verifies if the media was already attached on this journalEntry
     * @param filePath
     * @param imageBitmap
     */
    public void sameImage(String filePath){
		  boolean exists=false;
		  for(int a=0; a<EntryImageObjectManager.images.size(); a++){
			  if(filePath.matches(EntryImageObjectManager.images.get(a).getImagePath()))
	 			 {
	 				 exists=true;
	 				 break;
	 			 }
		  	}
		  if(exists==false){
			  //Stores the Bitmap of the image.
			  EntryImageObjectManager.images.add(new EntryImageObject(null,filePath,EntryImageObjectManager.NO_NEED_DOWNLOAD,_journalEntry.getID()));	 
			  //Adds the image to the Gridview
			  imageAdapter.notifyDataSetChanged();
		 }
		  else{
	  		Toast.makeText(getBaseContext(), "This image has already been attached.", Toast.LENGTH_SHORT).show();
	  		}	  
	  }
    
    public void addLocationToLog(View view)
    {
    	CheckBox d = (CheckBox) view;			
		if (!GPS.isGPSEnabled(getApplicationContext())) {
			d.setChecked(false);
			buildAlertMessageNoGps();
			//d.setText("No");
			addLocation = false;
		} else {
			if (d.isChecked()) {
				d.setText("Yes");
				//_journalEntry.setAddressID(value)
				addLocation = true;
			} else {
				//d.setText("No");
				addLocation = false;
			}
		}
    }
    
    private void handleCurrentLocation() {
    	if(GPS.lastKnownLocation==null && addLocation){
		 String[] buttons = {"Wait","Continue"};
		 // Create an instance of the dialog fragment and show it
		 final CustomAlertDialog customDialog = new CustomAlertDialog(this, buttons,
				 "Information","Trinity is unable to determine the location of your device. Would you like to wait until it finds your location?",
				 android.R.drawable.ic_dialog_alert,
				 new CustomAlertDialog.DialogButtonClickListener() {
					@Override
					public void onDialogButtonClick(View v) {	
						
						if("Continue".equals(v.getTag()))
						{
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
    
    private void checkGPS()
    { 	
		CheckBox d = (CheckBox) findViewById(R.id.checkBoxGPS);		
		if (!GPS.isGPSEnabled(getApplicationContext()) && d.isChecked() && !isFinishing()) {
			d.setChecked(false);
			addLocation = false;
			buildAlertMessageNoGps();
			//d.setText("No");
		}
		gps.requestLocationUpdates();
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

    private void lastLocation()
	{
		if(GPS.lastKnownLocation!=null){
			updateLocationValues(""+GPS.lastKnownLocation.getLatitude(),""+GPS.lastKnownLocation.getLongitude(),
				"Address (Last Location Remembered)");
		}
	}

    private void updateLocationValues(String lat, String longitude, String text)
	{
		((TextView)findViewById(R.id.entry_lat)).setText("Latitude: "+longitude);
		((TextView)findViewById(R.id.entry_long)).setText("Longitude: "+lat);
		((TextView)findViewById(R.id.entry_address_text)).setText(text);		
	}
    
	@Override
	public void gpsLocationChange(Location point) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Location Adquired", Toast.LENGTH_SHORT).show();
		updateLocationValues(""+point.getLatitude(),""+point.getLongitude(),
				"Address (Current Location Acquired)");
		((TextView)findViewById(R.id.entry_address_text)).setTextColor(Color.GREEN);
	}
}
