package com.futureconcepts.anonymous;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.futureconcepts.customclass.NetworkHandler;
import com.futureconcepts.customclass.SchedulerNotifications;
import com.futureconcepts.customclass.SingletonInformation;
import com.futureconcepts.database.AnonymousInfoMediaTable;
import com.futureconcepts.database.AnonymousTable;
import com.futureconcepts.database.DatabaseHelper;
import com.futureconcepts.database.MediaTable;
import com.futureconcepts.database.TableStatus;
import com.futureconcepts.dbcommunication.VerifyKey;
import com.futureconcepts.dbcommunication.sendInformation;
import com.futureconcepts.anonymous.ImageActivity;
import com.google.ads.AdRequest;
import com.google.ads.AdView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

public class AnonymousActivity extends Activity {
	private SQLiteDatabase newDB;
	private String CATEGORY_TYPE;
	private String idCategory = "";
	private String idAnonymous;
	View activityRootView;
	private EditText details;
	public Toast mToastText;
	public String test = "";
	private String detailsText;
	private String studentID;
	private String IDSchoolName = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.anonymous_activity);
		final TextView textViewToChange = (TextView) findViewById(R.id.Category);
		mToastText = Toast.makeText(AnonymousActivity.this, "",Toast.LENGTH_SHORT);
		details = (EditText) findViewById(R.id.detailsText);
		details.setSingleLine(false);
		Intent intent = getIntent();
		CATEGORY_TYPE = intent.getStringExtra("Category");
		textViewToChange.setText("Category - " + CATEGORY_TYPE);
		DatabaseHelper dbHelper = new DatabaseHelper(this.getApplicationContext());
		newDB = dbHelper.getWritableDatabase();
		Cursor c = newDB.rawQuery("Select ID from AnonymousCategory where Type=?",
				new String[] { CATEGORY_TYPE });
		Log.d("SELECTED", "CATEGORY:" + CATEGORY_TYPE);
		c.moveToFirst();
		idCategory = c.getString(c.getColumnIndex("ID"));
		idAnonymous = intent.getStringExtra("IDAnonymous");// ID for report
		c.close();
		newDB.close();
		activityRootView = findViewById(R.id.submit);
		details.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (event != null
						&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					Log.d("hello", "hello");
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(details.getWindowToken(), 0);

					return true;
				}

				Log.d("hello", "hello2");
				return false;
			}
		});
		//Get the current context to display the AlertDialog for not send it reports.
		SingletonInformation.getInstance().currentCon=this;
	}

	public void imageActions(View view) {
		Intent image = new Intent(this, ImageActivity.class);
		image.putExtra("IDAnonymousInfo", idAnonymous);
		startActivity(image);
	}

	/*
	 * Delete all the data created for the current report.
	 * */
	public void cleanDatabase() {
		Log.d("Clean", "here in the method");
		DatabaseHelper dbHelper = new DatabaseHelper(
				this.getApplicationContext());
		newDB = dbHelper.getWritableDatabase();

		try {
			// Clean AnonyousInfoTable
			getContentResolver().delete(AnonymousTable.CONTENT_URI, "ID=?",
					new String[] { idAnonymous });
			Cursor deleteMedia = newDB
					.rawQuery(
							"Select Distinct M.ID from Media M Inner Join AnonymousInfoMedia sam on M.ID=sam.Media AND sam.NEXARInfo=?",
							new String[] { idAnonymous });
			                 
			// Clean MediaTable
			if (deleteMedia.getCount() == 0) {
				deleteMedia.close();
				Log.d("testing", "Nothing in database media");
			} else {

				deleteMedia.moveToFirst();
				do {
					String idMedia = deleteMedia.getString(deleteMedia
							.getColumnIndex("ID"));
					String where = "id=?";
					getContentResolver().delete(MediaTable.CONTENT_URI, where,
							new String[] { idMedia });

				} while (deleteMedia.moveToNext());
				deleteMedia.close();
			}
			// Clean AnonyousInfoMediaTable
			getContentResolver().delete(AnonymousInfoMediaTable.CONTENT_URI,
					"NEXARInfo=?", new String[] { idAnonymous });

			Log.d("Clean", "Clean success");

			deleteMedia.close();
		} catch (Exception e) {
			Log.d("Clean", "error while cleaning");
		}

		newDB.close();
	}
	/*
	 * Validates all the infroamtion and instanciates the SendInformation object.
	 * */
	@SuppressLint("SimpleDateFormat")
	public void sendReport(View view) {
		Log.d("in", "in sendReport");		
		SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
		IDSchoolName = settings.getString("IDSchool", "");
		String android_id = Secure.getString(getBaseContext().getContentResolver(),Secure.ANDROID_ID);
		boolean isKeyValid= settings.getBoolean("isKeyValid", false);
		studentID = settings.getString("StudentID", "Anonymous");
		String key = settings.getString("key", "");
		if (IDSchoolName.length() != 0 && isKeyValid ) {
			// get Description text			
				detailsText = details.getText().toString();
				int a = detailsText.length();
				boolean isWhitespace = detailsText.matches("^\\s*$");
				if (a != 0 && isWhitespace == false ){
					try {
						
					} catch (Exception e) {

					} finally {
					
						//Verify internet conection
						int networkResult = NetworkHandler.sendInformationToServer(getApplicationContext());
						SchedulerNotifications temp= new SchedulerNotifications();
						switch (networkResult)
						{
							case 0:
								saveReport(studentID, detailsText, IDSchoolName);
								alertDialog("Information","No Internet access: Please go to settings-->Pending Reports and send the information when you get internet access.",1); 
								//temp.createScheduledNotification(this,10);//10 minutes	
								temp.createScheduledNotification(this,480);//8 hours
								break;
							case 1:
								//Verify if the key still active on the database
								// send key to server.
								VerifyKey vk = new VerifyKey(IDSchoolName,key,android_id,"VerifyKey",this);
								vk.validateKey();
								
								break;
							case 2:
								saveReport(studentID, detailsText, IDSchoolName);
								alertDialog("Information","You need WIFI Access to send this amount of data: Please go to settings-->Pending Reports and send the information when you get WIFI access.",1);
								//temp.createScheduledNotification(this,10);//10 minutes	
								temp.createScheduledNotification(this,480);//8 hours
								break;	
						}
					}// End finally
				} else if (a == 0 || isWhitespace == true) {
					mToastText.setText("Please include the details of the report.");
					mToastText.show();
				}
			
		}else {
			alertDialog("Settings","Please specify all configuration options.",2);
		}	

	}
	
	public void handleVerifyKeyResult(String result)
	{
		if(result.equals("valid")){
			saveReport(studentID, detailsText, IDSchoolName);
			
			//Sending information to server
			sendInformation send = new sendInformation();
			send.getValues(AnonymousActivity.this,false, idAnonymous);
			//return to home screen
			goHome();
		}else if(result.equals("connection fail"))
		{
			alertDialog("Information","Connection to server failed.",0);
			
		}else
		{
			//VerifyKey.alert(this, isKeyStillValid);
			removeUserSettings();
			alertDialog("Information","Your key is not valid, you cannot send the report. Please specify all configuration options.",2);
		}  
	}
	
	/**
	 * 
	 * @param studentID:  key registered
	 * @param detailsText: text to save
	 * @param IDSchoolName: ID of the school registered
	 */
	@SuppressLint("SimpleDateFormat")
	private void saveReport(String studentID, String detailsText,String IDSchoolName)
	{
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat(
			"yyyy-MM-dd");
		String formattedDate = df.format(calendar.getTime());
		saveAnonymousInfo(studentID, detailsText,formattedDate, IDSchoolName);
	}
	
	/**
	 * Reset all the settings of the user
	 **/
	private void removeUserSettings()
	{
		SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("key","" );
		editor.putBoolean("isKeyValid",false);
		editor.putString("IDState","");
		editor.putString("StateName","");
		editor.putString("IDSchoolType","");
		editor.putString("NameSchoolType","");
		editor.putString("IDSchool","");
		editor.putString("NameSchool","");
		//editor.putBoolean("firstLaunch",true);	
		editor.commit();
	}

	public void setOptions(View view) {
		Intent intent = new Intent(this, ChooseSchool.class);
		startActivity(intent);

	}

	@Override
	public void onBackPressed() {
		backToMain();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.anonymous_home, menu);
		getMenuInflater().inflate(R.menu.anonymous_homesad, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent d = new Intent(this, ChooseSchool.class);
			startActivity(d);
			break;
		}
		return false;

	}

	 //Displays a AlertDialog with the title and message specified.
	  public void alertDialog(String title,String message, final int task){
		  AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
        	.setMessage(message)
            .setCancelable(false)
            .setNegativeButton("OK",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                   	if(task==1){
                   		//return to home screen
                   		
                   		goHome();
                   	   }else if(task==2){
                   		Intent home = new Intent(
								AnonymousActivity.this,
								ChooseSchool.class);
						startActivity(home);
                   		   
                   	   }
                     }
                 });
                 AlertDialog alert = builder.create();
                 alert.show();
	  }
	  
	  
	  /*
	   * Saves the report data in AnonymousTable.
	   * */
	  public void saveAnonymousInfo(String studentID, String detailsText,String today, String IDSchoolName){
		  
		   String android_id = Secure.getString(getBaseContext().getContentResolver(),
                  Secure.ANDROID_ID); 
		  
		  ContentValues anonymousValues = new ContentValues();
			anonymousValues.put(AnonymousTable.COLUMN_ID, idAnonymous);
			anonymousValues.put(AnonymousTable.COLUMN_DETAILS,detailsText);
			anonymousValues.put(AnonymousTable.COLUMN_DATE,today);
			anonymousValues.put(AnonymousTable.COLUMN_TYPE,idCategory.toString());
			anonymousValues.put(AnonymousTable.COLUMN_SCHOOL,IDSchoolName);
			anonymousValues.put(AnonymousTable.COLUMN_STUDENTID,studentID);
			anonymousValues.put(AnonymousTable.COLUMN_DEVICEID,android_id);	
			getContentResolver().insert(AnonymousTable.CONTENT_URI,anonymousValues);
			
			//Store Media and AnonymousInfoMedia data.
			SingletonInformation.getInstance().saveData(this,idAnonymous,today,idCategory.toString());
			
			//Store TableStatus 0 mean is not in the server.
			 ContentValues tableStatus = new ContentValues();
			 tableStatus.put(TableStatus.COLUMN_ID, idAnonymous);
			 tableStatus.put(TableStatus.COLUMN_NEXARINFO_TABLE,0);
			 tableStatus.put(TableStatus.COLUMN_NEXARINFOMEDIA_TABLE,0);
			 tableStatus.put(TableStatus.COLUMN_MEDIA_TABLE,0);
			  getContentResolver().insert(TableStatus.CONTENT_URI,tableStatus);
			//Clear SingletonInformatino.
			  for(Bitmap t: SingletonInformation.getInstance().bitmapImages ){
				  t.recycle();
			  }
			SingletonInformation.getInstance().bitmapImages.clear();
			SingletonInformation.getInstance().pathImages.clear();
			
			Log.d("result", "ID" + idAnonymous + "Details"
					+ detailsText + "Date" + today + "type"
					+ idCategory);		  
	  }
	  
	  /**
	   * Return to nexar home screen.
	   * */
	  private void goHome()
	  {
		  Intent intent = new Intent(getApplicationContext(), AnonymousHome.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
	  }
	  
	  /**
		 * ask the user if  he want to exit the active report and lose all filled data
		 **/
	  public void backToMain()
		{
			// 1. Instantiate an AlertDialog.Builder with its constructor
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			// 2. Chain together various setter methods to set the dialog characteristics
			builder.setMessage("Are you sure you want to cancel report? All information will be discarded.")
			       .setTitle("Information")
			       .setCancelable(false);
			       // .setIcon(R.drawable.info);
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   clean();
		        	  
		           }
		       });
			builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               dialog.dismiss();
		               dialog=null;
		           }
		       });

			// 3. Get the AlertDialog from create()
			AlertDialog dialog = builder.create();
			dialog.show();
			//return dialog;
		}
		
	 /**
	  *Reset all saved data and cancel all toast of this activity. 
	  **/
		public  void clean()
		{
			SingletonInformation.getInstance().bitmapImages.clear();
			SingletonInformation.getInstance().pathImages.clear();
			mToastText.cancel();
			finish();
		}
	  
}
