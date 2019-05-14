package com.futureconcepts.anonymous;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;


public class ChooseSchool extends Activity   {
	public static final String PREFS_NAME = "MyPrefsFile";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_choose_school);
		
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    checkPreferences(); 
        
	      // boolean silent = settings.getBoolean("policiesAccepted", false);
	     final boolean isFirstLaunch = settings.getBoolean("firstLaunch",true);
	     Log.d("created?","lets see"+isFirstLaunch);
	     
    	}
	@Override
	public boolean  onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_choose_school, menu);
		return true;
	}
	
  	
	public void currentList(View view)
	{    
		   String ListName="";
		   int requestCode=0;
		      switch (view.getId()) {
		        case R.id.stateButton:
		        	 ListName="states";
		        	 requestCode=1;
		                    break;
		        case R.id.schoolTypeButton:
		        	ListName="schoolType";
		        	requestCode=2;
		                    break;
		        case R.id.schoolNameButton:
		        	ListName="schoolName";
		        	requestCode=3;
		        break;
		     }
		   
		    Log.d("test","test"+ListName);
		Intent currentlist = new Intent(this,CurrentList.class);
		currentlist.putExtra("listName", ListName);
		startActivityForResult(currentlist,requestCode);	
	}
	

	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		SharedPreferences shared = getSharedPreferences("MyPrefsFile",0);
		
	if(resultCode == RESULT_OK){ 
		if (requestCode == 1) {
			String ID = shared.getString("IDState","");
			String Name = shared.getString("StateName","");
			Button ChangeStateName = (Button) findViewById(R.id.stateButton);
			Button ChangeState = (Button) findViewById(R.id.schoolTypeButton);
			ChangeState.setEnabled(true);
			ChangeStateName.setText(Name);
			Log.d("ID","idstate"+ID+".."+Name);
			
		  }else if(requestCode == 2){
			    String ID = shared.getString("IDSchoolType","");
				String Name = shared.getString("NameSchoolType","");
				Button ChangeStateName = (Button) findViewById(R.id.schoolTypeButton);
				ChangeStateName.setText(Name);
				Button ChangeState = (Button) findViewById(R.id.schoolNameButton);
				ChangeState.setEnabled(true);
				Log.d("ID","idstate"+ID+".."+Name);
		  }else if(requestCode == 3){
			    String ID = shared.getString("IDSchool","");
				String Name = shared.getString("NameSchool","");
				Button ChangeStateName = (Button) findViewById(R.id.schoolNameButton);
				ChangeStateName.setText(Name);
				Log.d("ID","idstate"+ID+".."+Name);
		   }
	   }//end result ok
	}//onactivityResult

	
	public void menuButton(View view) {
		openOptionsMenu();

	}
	
	public void checkPreferences() {
		String IDState ="";
		String IDSchoolType = "";
		String IDSchoolName ="";
		
		
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		IDState = settings.getString("IDState","");
		IDSchoolType = settings.getString("IDSchoolType","");
		IDSchoolName = settings.getString("IDSchool","");
		
		if(IDState.length()==0){
			Button ChangeState = (Button) findViewById(R.id.schoolTypeButton);
			Button ChangeState2 = (Button) findViewById(R.id.schoolNameButton);
			ChangeState2.setEnabled(false);
			ChangeState.setEnabled(false);
		}else{
			
			String StateName = settings.getString("StateName","");
			Button ChangeStateName = (Button) findViewById(R.id.stateButton);
			ChangeStateName.setText(StateName);
			Button ChangeSchooltype = (Button) findViewById(R.id.schoolTypeButton);
			ChangeSchooltype.setEnabled(true);

			if( IDSchoolType.length()==0){
				Button ChangeState2 = (Button) findViewById(R.id.schoolNameButton);
				ChangeState2.setEnabled(false);
				}else{
					String SchoolTypeName = settings.getString("NameSchoolType","");
					Button ChangeSchooltypeName = (Button) findViewById(R.id.schoolTypeButton);
					ChangeSchooltypeName.setText(SchoolTypeName);
					ChangeSchooltypeName.setEnabled(true);
					Button ChangeSchool2 = (Button) findViewById(R.id.schoolNameButton);
					ChangeSchool2.setEnabled(true);
					
					if( IDSchoolName.length()==0){
						}else{
							String NameSchool = settings.getString("NameSchool","");
							Button ChangeSchool = (Button) findViewById(R.id.schoolNameButton);
							ChangeSchool.setText(NameSchool);
							ChangeSchool.setEnabled(true);
						}
				}
			
			
		}
		

	}//End checkPreferences method
public void saveSelection(View v) {
	String IDState ="";
	String IDSchoolType = "";
	String IDSchoolName ="";
	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	IDState = settings.getString("IDState","");
	IDSchoolType = settings.getString("IDSchoolType","");
	IDSchoolName = settings.getString("IDSchool","");
	if(IDState.length()!=0&&IDSchoolType.length()!=0&&IDSchoolName.length()!=0){
		finish();
	}else{
		Toast.makeText(ChooseSchool.this,"PLEASE SET ALL THE OPTIONS.", Toast.LENGTH_SHORT).show();
	}
		
}	
	
	@Override
	public void onBackPressed() {
		String IDState ="";
		String IDSchoolType = "";
		String IDSchoolName ="";
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		IDState = settings.getString("IDState","");
		IDSchoolType = settings.getString("IDSchoolType","");
		IDSchoolName = settings.getString("IDSchool","");
		if(IDState.length()!=0&&IDSchoolType.length()!=0&&IDSchoolName.length()!=0){
			finish();
		}else{
		
        	AlertDialog.Builder builder = new AlertDialog.Builder(ChooseSchool.this);
            builder.setTitle("Settings")
            .setMessage("You'll have to set these options to send a report")
            .setCancelable(false)
            .setNegativeButton("Close",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
              	 
                	finish();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
	  
	}

}
