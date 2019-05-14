package com.futureconcepts.anonymous;




import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import com.futureconcepts.database.AnonymousTable;
import com.futureconcepts.database.DatabaseHelper;
import com.futureconcepts.dbcommunication.sendInformation;
import com.futureconcepts.anonymous.MainActivity;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

public class AnonymousActivity extends Activity {
	private SQLiteDatabase newDB;
	private String CATEGORY_TYPE;
	private String idCategory="";
	private UUID idAnonymous;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.anonymous_activity);
		
		final TextView textViewToChange = (TextView) findViewById(R.id.Category);
		
		Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/kr1.ttf");
        textViewToChange.setTypeface(tf);
        TextView tv1 = (TextView) findViewById(R.id.details);
        tv1.setTypeface(tf);
        		
		
		Intent intent = getIntent();
		CATEGORY_TYPE = intent.getStringExtra("Category");
		textViewToChange.setText("Category - "+ CATEGORY_TYPE);
		
		 DatabaseHelper dbHelper = new DatabaseHelper(this.getApplicationContext());
		 newDB = dbHelper.getWritableDatabase();
		 
		 Cursor c = newDB.rawQuery("Select ID from AnonymousCategory where Type=?", new String[] {CATEGORY_TYPE});
		 Log.d("SELECTED","CATEGORY:"+ CATEGORY_TYPE);
		 c.moveToFirst();
		 idCategory=c.getString(c.getColumnIndex("ID"));
		 idAnonymous=UUID.randomUUID();
		 c.close();
		 newDB.close();
		
		
	}

	public void goBack(View view)
	  {	 
		 Log.d("destroy"," clean database called");
		  finish();
	  }
	
	public void cleanDatabase()
	{
		DatabaseHelper dbHelper = new DatabaseHelper(this.getApplicationContext());
		newDB = dbHelper.getWritableDatabase();
		
		try
		{
		    getContentResolver().delete(AnonymousTable.CONTENT_URI, "ID=?", new String [] {idAnonymous.toString()});
			Log.d("Clean","Clean success");
				
			newDB.close();
		}catch (Exception e)
		{
			Log.d("Clean","error while cleaning");
		}	
	}
	
	
	public void sendReport(View view){
	 String IDState ="";
	 String IDSchoolType = "";
	 String IDSchoolName ="";
	 String studentText="";
	 SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
	 IDState = settings.getString("IDState","");
	 IDSchoolType = settings.getString("IDSchoolType","");
	 IDSchoolName = settings.getString("IDSchool","");
     if(IDState.length()!=0&&IDSchoolType.length()!=0&&IDSchoolName.length()!=0){		
		//get Description text
		 EditText details =  (EditText) findViewById(R.id.details);		
		 String detailsText=details.getText().toString();
		//get name	
		 EditText studentName =  (EditText) findViewById(R.id.studentName);		
		  studentText=studentName.getText().toString();
		  if(studentText.length()==0){
			  studentText="Anonymous";
		  }

		 int a=detailsText.length();
		 Log.d("result","length"+a);
		 
		if(a!=0){ 
		try{ 
		 Calendar calendar = Calendar.getInstance();	

		 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		 String formattedDate = df.format(calendar.getTime());
		 
		 ContentValues anonymousValues= new ContentValues();
		 anonymousValues.put(AnonymousTable.COLUMN_ID,idAnonymous.toString());
		 anonymousValues.put(AnonymousTable.COLUMN_NAME,studentText);
		 anonymousValues.put(AnonymousTable.COLUMN_DETAILS,detailsText);
		 anonymousValues.put(AnonymousTable.COLUMN_DATE,formattedDate);
		 anonymousValues.put(AnonymousTable.COLUMN_TYPE, idCategory.toString());
		 anonymousValues.put(AnonymousTable.COLUMN_SCHOOL,IDSchoolName);
		 getContentResolver().insert(AnonymousTable.CONTENT_URI,anonymousValues);
		 
		 Log.d("result","ID"+idAnonymous+"Details"+detailsText+"Date"+formattedDate+"type"+idCategory);
		}catch(Exception e){
			
		}finally{
			     String IP1 ="172.16.21.187";
				 String Port1 ="";		 
				 
				 ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
				 NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

				 if (mWifi.isConnected()) {

					 
					 sendInformation send=new sendInformation();
					 send.getValues(IP1,Port1,AnonymousActivity.this);
					 Intent home = new Intent(AnonymousActivity.this, MainActivity.class);
		       	     home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		       	     startActivity(home);
				 }else{
				        					        	
				        	AlertDialog.Builder builder = new AlertDialog.Builder(AnonymousActivity.this);
				            builder.setTitle("NO WIFI")
				            .setMessage("No WiFi access: the information will be automatically sent when WiFi is enabled.")
				            .setCancelable(false)
				            .setNegativeButton("Close",new DialogInterface.OnClickListener() {
				                public void onClick(DialogInterface dialog, int id) {
				              	 
				                	Intent home = new Intent(AnonymousActivity.this, MainActivity.class);
						       	     home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						       	     startActivity(home);
				                }
				            });
				            AlertDialog alert = builder.create();
				            alert.show();				            
				 }		
			}//End finally
		}else{
			
			 Toast.makeText(AnonymousActivity.this,"Please include details of the report.", Toast.LENGTH_LONG).show();
		}
	 }else{
		 AlertDialog.Builder builder = new AlertDialog.Builder(AnonymousActivity.this);
         builder.setTitle("Settings")
         .setMessage("Please specify all configuration options.")
         .setCancelable(false)
         .setNegativeButton("Close",new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
           	 
             	     Intent home = new Intent(AnonymousActivity.this, ChooseSchool.class);
		       	     startActivity(home);
             }
         });
         AlertDialog alert = builder.create();
         alert.show();	
		 
	 }
	
	}
	public void setOptions(View view) {
		Intent intent = new Intent(this, ChooseSchool.class);
		startActivity(intent);

	}
	
	public void back(View v){
		finish();		
	}
	
}
