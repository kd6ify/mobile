package com.futureconcepts.anonymous;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.futureconcepts.customclass.NetworkHandler;
import com.futureconcepts.customclass.SingletonInformation;
import com.futureconcepts.dbcommunication.VerifyKey;



public class ChooseSchool extends Activity   {
	public static final String PREFS_NAME = "MyPrefsFile";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_choose_school);		 
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    checkPreferences(); 
		 SingletonInformation.getInstance().activitySettingsVisible=true;
	     final boolean isFirstLaunch = settings.getBoolean("firstLaunch",true);
	     Log.d("created?","lets see"+isFirstLaunch);
	   //Get the current context to display the AlertDialog for not send it reports.
		SingletonInformation.getInstance().currentCon=this;
	     
    	}
	@Override
	public boolean  onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_choose_school, menu);
		return true;
	}
	
  	
	public void currentList(View view)
	{ 	//Verifies if the user has internet connection.
		int networkResult = NetworkHandler.sendInformationToServer(getApplicationContext());
		switch (networkResult)
		{
			case 0:
				  AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
	                alertDialogBuilder.setTitle("Information");
	                alertDialogBuilder
	                        .setMessage("To perform this action you need internet access.")
	                        .setCancelable(false)
	                        .setNegativeButton("OK",
	                                new DialogInterface.OnClickListener() {
	                                    public void onClick(DialogInterface dialog,
	                                            int id) {
	                                        dialog.cancel();
	                                    }
	                                });
	                AlertDialog alertDialog = alertDialogBuilder.create();
	                alertDialog.show();
				break;
			case 1:
				//call activity CurrentList
				Intent currentlist = new Intent(this,CurrentList.class);
				currentlist.putExtra("listName", "School");
				startActivityForResult(currentlist,3);				
				break;			
		}
	}	
	
	public void contactInfo(View v) {
		Intent contactInformation = new Intent(this,ContactInfoActivity.class);
		startActivity(contactInformation);
	}
	
	public void studentID(final View v)
	{
		String Button;
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
	    final EditText input = new EditText(this);
	
	    input.setImeOptions(EditorInfo.IME_ACTION_DONE);
	    input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
	    if (v.getId() == R.id.contactInfoButton)
	    {
	        input.setFilters( new InputFilter[] {new InputFilter.LengthFilter(10)});
	    	alert.setTitle("Enter your student ID");
	    	alert.setMessage("Maximun 10 characters.");
	   
	    	
	    	Button = "Save";
	    }else
	    {
	        input.setFilters( new InputFilter[] {new InputFilter.LengthFilter(20)});
	    	alert.setTitle("Enter Key");
	    	Button = "Validate";
	    }
	    
	    alert.setView(input);
	    alert.setPositiveButton(Button, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	        	 String value = input.getText().toString().trim();
	        	 if (v.getId() == R.id.contactInfoButton)
	     	    {
	        		 saveStudentID(value);	
	     	    }else
	     	    {
	     	    	validateKey(value);
	     	    	
	     	    }   	    
	        	            
	        }
	    });

	    alert.setNegativeButton("Cancel",
	            new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                    dialog.cancel();
	                }
	            });
	    AlertDialog popup = alert.create();
	  popup.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	  popup.show();	
	    
	
	}
	
	
	/**
	 * 
	 * @param key: user key to validate on server.
	 */
	private void validateKey( String key) 
	{
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);		
		String IDSchoolName = settings.getString("IDSchool","");		
		String android_id = Secure.getString(getBaseContext().getContentResolver(),Secure.ANDROID_ID); 
		String result;
		// Verifies if the user has Internet connection.
		int networkResult = NetworkHandler.sendInformationToServer(getApplicationContext());
		switch (networkResult)
		{
			case 0:
				AlertDialog.Builder builder = new AlertDialog.Builder(ChooseSchool.this);
	            builder.setTitle("Information")
	            .setMessage("You need internet to perform this action.")
	            .setCancelable(false)
	            .setNegativeButton("Close",new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {}
	            });
	            AlertDialog alert = builder.create();
	            alert.show();
				break;
			case 1:
				try {
					// send key to server.
					VerifyKey vk = new VerifyKey(IDSchoolName,key,android_id,"SaveKey",this);
					vk.validateKey();
				} catch (Exception e) {
					e.printStackTrace();
					result=null;
				}				
				break;		
		}
		
	}	
	
	public void handleVerifyKeyResult(String result,String key)
	{
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		if(result.equals("saved")){		
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("key",key );
			editor.putBoolean("isKeyValid",true);
			editor.commit();
			Button b = (Button)findViewById(R.id.user_key);
			b.setText("NEXAR is Activated");
			b.setEnabled(false);
			Button ChangeSchool = (Button) findViewById(R.id.schoolNameButton);
			ChangeSchool.setEnabled(false);
		}		
		VerifyKey.alert(this,result);
	}
	
	/**
	 * 
	 * @param id: the id to save.
	 */
	private void saveStudentID(String id)
	{	
		if(id.equals(""))
		{
			id="Anonymous";
		}
			
         SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);	
         SharedPreferences.Editor editor = settings.edit();
         editor.putString("StudentID", id);
         editor.commit();
         Toast.makeText(getApplicationContext(), id+"  Saved",
                 Toast.LENGTH_SHORT).show();
         Button student = (Button) findViewById(R.id.contactInfoButton);
         student.setText("Student ID: "+id);
		
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		SharedPreferences shared = getSharedPreferences("MyPrefsFile",0);
		
		if(resultCode == RESULT_OK){ 
			if(requestCode == 3){
			    String ID = shared.getString("IDSchool","");
				String Name = shared.getString("NameSchool","");
				Button ChangeStateName = (Button) findViewById(R.id.schoolNameButton);
				ChangeStateName.setText(Name);
				Log.d("ID","idstate"+ID+".."+Name);
				Button key = (Button) findViewById(R.id.user_key);
				key.setEnabled(true);
			}
		}//end result ok
	}//onactivityResult

	
	public void menuButton(View view) {
		openOptionsMenu();

	}
	
	public void checkPreferences() {
		
		String IDSchoolName ="";
		//String StudentID = "";
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);		
		Boolean isKeyValid = settings.getBoolean("isKeyValid", false);
		
		IDSchoolName = settings.getString("IDSchool","");
		String StudentID = settings.getString("StudentID", "No ID");		
		Button student = (Button) findViewById(R.id.contactInfoButton);
        student.setText("Student ID: "+StudentID);		
		if( IDSchoolName.length()==0){
			
		}else{
				String NameSchool = settings.getString("NameSchool","");
				Button ChangeSchool = (Button) findViewById(R.id.schoolNameButton);
				ChangeSchool.setText(NameSchool);
				ChangeSchool.setEnabled(true);
				if(isKeyValid)
				{
					Log.d("0000000","Key was valid");
					Button b = (Button)findViewById(R.id.user_key);
					b.setText("NEXAR is Activated");
					b.setClickable(false);
					ChangeSchool.setEnabled(false);
				}else
				{
					Log.d("0000000","Key was not valid");
					Button b = (Button)findViewById(R.id.user_key);
				//	b.setText("You have a valid key");
					b.setEnabled(true);
				}		
				
			}
	}//End checkPreferences method
	
public void saveSelection(View v) {
	String IDSchoolName ="";
	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	
	IDSchoolName = settings.getString("IDSchool","");
	boolean key = settings.getBoolean("isKeyValid",false);
	if(IDSchoolName.length()!=0 && key){
		finish();
	}else{
		Toast.makeText(ChooseSchool.this,"PLEASE SET ALL REQUIRED OPTIONS.", Toast.LENGTH_SHORT).show();
	}
		
}	
	
	@Override
	public void onBackPressed() {
	//String IDState ="";
		//String IDSchoolType = "";
		String IDSchoolName ="";
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		
		IDSchoolName = settings.getString("IDSchool","");
		//sboolean isFirstLouch = settings.getBoolean("firstLaunch",true);
		boolean key = settings.getBoolean("isKeyValid",false);
		if(IDSchoolName.length()!=0 && key){
			finish();
		}else{
		
        	AlertDialog.Builder builder = new AlertDialog.Builder(ChooseSchool.this);
            builder.setTitle("Settings")
            .setMessage("Settings You must set REQUIRED options to send a report")
            .setCancelable(false)
            .setNegativeButton("Close",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                	//try activityname.finish instead of this
                	Intent intent = new Intent(Intent.ACTION_MAIN);
                	intent.addCategory(Intent.CATEGORY_HOME);
                	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                	startActivity(intent);
                	//finish();
                	//android.os.Process.killProcess(android.os.Process.myPid());
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
	  
	}
	
	

}
