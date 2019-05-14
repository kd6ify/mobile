package com.futureconcepts.anonymous;

import com.futureconcepts.contentprovider.anonymous.DeleteInformation;
import com.futureconcepts.customclass.ImageAdapter;
import com.futureconcepts.customclass.NetworkHandler;
import com.futureconcepts.database.DatabaseHelper;
import com.futureconcepts.dbcommunication.VerifyKey;
import com.futureconcepts.dbcommunication.sendInformation;

import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;



public class ViewPendingData extends Activity  {
	String IDReport;
	ImageAdapter imgAdapter;
	String details;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_view_pending_data);
		Intent intent = getIntent();
		IDReport = intent.getStringExtra("IdReport");
		Log.d("id view",""+IDReport);		
		TextView labelPending = (TextView)findViewById(R.id.labelPending);
		labelPending.setText("Report "+(intent.getIntExtra("ReportNumber", 0)+1));
		
		TextView labelCategory= (TextView)findViewById(R.id.labelCategory);
		labelCategory.setText("Category:"+(intent.getStringExtra("Category")));
		setData();

	}
	
	public void pendingReports(View v){
		alertDialog("Details:",details,0);
	}

	public void pendingImages(View view)
	{  
	
	DatabaseHelper dbHelper = new DatabaseHelper(this);
    SQLiteDatabase newDB = dbHelper.getWritableDatabase();
 	Cursor US = newDB.rawQuery("Select Distinct * from Media US Inner Join AnonymousInfoMedia sam on US.ID=sam.Media AND sam.NEXARInfo=?",new String[]{IDReport});
	if (US.getCount() != 0)
		{
		    US.close();
		    newDB.close();
			Intent pendingImages = new Intent(this,ViewPendingImages.class);
			pendingImages.putExtra("IdReport",IDReport);
			startActivity(pendingImages);
		}else{
			US.close();
			alertDialog("","No pending images for this report.",0);
		}
	
	
	}
	public void setData(){
		 details="No pending details";
		DatabaseHelper dbHelper = new DatabaseHelper(this);
		SQLiteDatabase newDB = dbHelper.getWritableDatabase(); 
		Cursor CSF = newDB.rawQuery("Select * from AnonymousInfo WHERE ID=?",new String[]{IDReport});
    		if(CSF.getCount()!=0){
    		CSF.moveToFirst();			
    				details=CSF.getString(CSF.getColumnIndex("Details"));
    		}
    		CSF.close();
    		newDB.close();
	}
	
	/**
	 *Display alerts 
	 * @param title: title to display on the alert
	 * @param message: message to display on the alert
	 * @param deleteReport: action to perform
	 **/
	private void alertDialog(String title, String message,int deleteReport) {
	
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
        .setMessage(message);
        //.setCancelable(false);
        if(deleteReport == 1)
        {
        	builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	  
		        	   deleteRepot();
		           }
		       });
			builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               dialog.dismiss();
		               dialog=null;
		           }
		       });
        	
        }else if(deleteReport==2){
        	builder.setNegativeButton("OK",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    Intent home = new Intent(ViewPendingData.this,ChooseSchool.class);
        			startActivity(home); 
                }
            });
       		      		   
       	}else
        {
        	builder.setNegativeButton("Close",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
        }        
        AlertDialog alert = builder.create();
        alert.show();
    }

	public void sendPendingData(View view)
	{ 
		//Verifies Internet connection.
		int networkResult = NetworkHandler.sendInformationToServer(getApplicationContext());
		switch (networkResult)
			{
				case 0:
					alertDialog("Information","No Internet access.",0);
					break;
				case 1:
						//Get user settings
						SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
						String IDSchoolName = settings.getString("IDSchool", "");
						String android_id = Secure.getString(getBaseContext().getContentResolver(),Secure.ANDROID_ID);
						String key = settings.getString("key", "");
						//Verify if the key still active on the database
						// send key to server.
						VerifyKey vk = new VerifyKey(IDSchoolName,key,android_id,"VerifyKey",this);
						vk.validateKey();							
					break;
				case 2:
					alertDialog("Information","You need WIFI Access to send this amount of data: Please go to settings-->Pending Reports and send the information when you get WIFI access.",0);							
					break;	
			}	 
	}
	
	public void handleVerifyKeyResult(String result)
	{
		if(result.equals("valid")){
			//Sending information to server
			sendInformation send = new sendInformation();
			send.getValues(ViewPendingData.this,true, IDReport);
			
		}else if(result.equals("connection fail"))
		{
			alertDialog("Information","Connection to server failed.",0);
			
		}else
		{
			//VerifyKey.alert(this, isKeyStillValid);
			alertDialog("Information","Your key is not valid, you cannot send the report. Please specify all configuration options.",2);
			removeUserSettings();
		}  		
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
	
	public void deletePendingData(View view)
	{  
		alertDialog("Information","Are you sure you want to delete this report?",1);
	}
	
	/**
	 * Delete the active report
	**/
	private void deleteRepot()
	{
		DeleteInformation clean=new DeleteInformation();
		clean.deleteReport(this,IDReport);		
		Intent returnIntent = new Intent();
		returnIntent.putExtra("action","deletion");
		setResult(RESULT_OK,returnIntent);     
		finish();
	}
	
}
