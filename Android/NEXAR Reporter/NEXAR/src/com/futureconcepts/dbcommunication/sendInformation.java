package com.futureconcepts.dbcommunication;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import android.database.SQLException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import com.futureconcepts.customclass.Client;
import com.futureconcepts.customclass.SchedulerNotifications;
import com.futureconcepts.customclass.SingletonInformation;
import com.futureconcepts.database.AnonymousInfoMediaTable;
import com.futureconcepts.database.AnonymousTable;
import com.futureconcepts.database.DatabaseHelper;
import com.futureconcepts.database.MediaTable;
import com.futureconcepts.database.TableStatus;

public class sendInformation extends Activity{
	public SQLiteDatabase newDB;
	private JSONObject JSAnonymous,JSMedia,JSAIM;
	private JSONArray jArrayA,jArrayM,jArraySAIM;
	private LinkedList<HttpPost> requestsToServer= new LinkedList<HttpPost>();
	private ArrayList<JSONObject> tempObjects = new ArrayList<JSONObject>();
	String IdCurrentImage="";
	String TableName="";
	Boolean StateCount=false;
	Boolean Ready=false;
	Boolean ReadyOff=false;	
	Boolean pending=false;
	String saveData="";
	String saveMedia="";
	public Context context;
	public ProgressDialog progressDialog;
	public Activity activityClass;
    public String IDreport;
	static SendInfo async;
    static int attemps=0;//We will try to send the same report 3 times in case soethign fails.
	/*getValues initialize the reports to be sent.
	 @params: activity is true if the sendinformation class was called from pending reports,
	 * and false if was called from AnonymousActivity.
	 * */
	
	public  void getValues(Context con, boolean activity, String IdReport)
	{ Log.d("test 1","test1");
		SingletonInformation.getInstance().reports.add(IdReport);
		ServerInformation serverInfo=new ServerInformation();
		saveData = serverInfo.SaveData; 
		saveMedia= serverInfo.SaveMedia;
		//Everything is going on background
		Log.d("test 2","test2");
		if(async!=null){
			Log.d("test 3","test3");
			if(async.getStatus() == AsyncTask.Status.RUNNING){
			   //Do nothing because there is another report in process
			}else{
				async=new SendInfo();
				async.execute();				
			}
		}else{
			activityClass= (Activity) con;
			context=con;
			pending=activity;			
			Log.d("test 4","test4");
			async=new SendInfo();
			async.execute();
		
	}
}
	
	
	
	public class SendInfo extends AsyncTask<Object, Object, Object>{	 

		@Override
	    protected void onPreExecute() {
			Log.d("test 5","test5");
			IDreport=SingletonInformation.getInstance().reports.getFirst(); //Id of the current report
			  Log.d("test 1","test 1 last");
				progressDialog=new ProgressDialog(context);
			if(pending==true){
	        progressDialog.setMessage("Sending information please wait.");
	        progressDialog.setCancelable(false);
	        progressDialog.show();
			}
			Log.d("test 6","test6");
	    }
	    
		
		@Override
        protected Object doInBackground(Object... arg0) {
			Log.d("test 2","test 2 last");
        	valuesToSend();
            return null;
        }

        @Override
        protected void onPostExecute (Object result){
        	Cursor Csus = newDB.rawQuery("Select * from AnonymousInfo WHERE ID=?", new String[]{IDreport});
        	Cursor CUS = newDB.rawQuery("Select Distinct * from Media US Inner Join AnonymousInfoMedia sam on US.ID=sam.Media AND sam.NEXARInfo=?",new String[]{IDreport});
   	         	
        	if(Csus.getCount()==0 && CUS.getCount()==0){
				 Log.d("test 3.1","test 3.1 last");
		    deleteTableStatus();
		    SingletonInformation.getInstance().reports.removeFirst();
		    checkView();
		    alertDialog("Report Sent","The report was sent successfully");
			Toast.makeText(context,"The report was sent successfully.",Toast.LENGTH_LONG).show();
			//alertDialog("","The report was sent successfully.");	 
			Log.d("test 3.2","test 3.2 last");
			}else{
				if(attemps<=2){
					attemps+=1;
					if (progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
				}else{
					attemps=0;
					SingletonInformation.getInstance().reports.removeFirst();
					checkView();
					alertDialog("Report not sent.","There was an error while sending the report, make sure you have internet access and try again from pending reports section.");
					Toast.makeText(context,"Report not sent.",Toast.LENGTH_LONG).show();
					//alertDialog("Error","The report was not sent. Please go to settings-->Pending Reports and try to send the report again.");
					SchedulerNotifications temp= new SchedulerNotifications(); 
					//temp.createScheduledNotification(context,10);//10 minutes	
					temp.createScheduledNotification(context,480);//8 hours
				}
			}
				Log.d("test 3.3","test 3.3 last");
				Csus.close();
				CUS.close();
				newDB.close();
				
				if(SingletonInformation.getInstance().reports.size()>0){
					Log.d("el que sigue","siguiente");
					async=new SendInfo();
					async.execute();
				}else{
					Log.d("el que sigue","ya no hay");
		        	async=null;
					SingletonInformation.getInstance().reports.clear();
				}
				
	   }
	}//end asyncTask

	public void valuesToSend() {
		Log.d("test 4","test 4 last");
		try
		{
			DatabaseHelper dbHelper = new DatabaseHelper(context);
			newDB = dbHelper.getWritableDatabase();

		}catch(SQLException e)
        {
            Log.d("Error","Error while Opening Database");
            e.printStackTrace();
        }

		try
		{
			Cursor Csus = newDB.rawQuery("Select * from AnonymousInfo WHERE ID=?", new String[]{IDreport});
			Cursor Cmed = newDB.rawQuery("Select Distinct * from Media M Inner Join AnonymousInfoMedia sam on M.ID=sam.Media AND sam.NEXARInfo=?",new String[]{IDreport});
			Cursor CsusA = newDB.rawQuery("Select * from AnonymousInfoMedia WHERE NEXARInfo=?", new String[]{IDreport});
         
		if(!getStatus("NexarInfoTable")){
			 if(Csus.getCount()!=0){
			    Csus.moveToFirst();			
					Log.d("anonymousInfo","HAVE something");
					 JSAnonymous = new JSONObject();
					 jArrayA = new JSONArray();
					 JSAnonymous.put("ID", Csus.getString(Csus.getColumnIndex("ID")));
					 JSAnonymous.put("Details", Csus.getString(Csus.getColumnIndex("Details")));
					 JSAnonymous.put("Date", Csus.getString(Csus.getColumnIndex("Date")));
					 JSAnonymous.put("Type", Csus.getString(Csus.getColumnIndex("Type")));
					 JSAnonymous.put("School", Csus.getString(Csus.getColumnIndex("School")));
					 JSAnonymous.put("StudentID", Csus.getString(Csus.getColumnIndex("StudentID")));
					 JSAnonymous.put("DeviceID", Csus.getString(Csus.getColumnIndex("DeviceID")));
				     jArrayA.put( JSAnonymous);
				     sendValues(jArrayA,"NEXARInfo");
				     CleanDatabase("AnonymousInfo",jArrayA,Ready);
					 ReadyOff=false;
			 }
		}
			 Log.d("testing upload","testing1");
			//Will send all the data only if the report informatin is on the server
			//This will prevent having images without a report reference.
			 if(getStatus("NexarInfoTable")){
				 if(Cmed.getCount()!=0){
					 Log.d("sending media","sending media");
					 jArrayM = new JSONArray();
						Cmed.moveToFirst(); 
						tempObjects.clear();
						do{
								JSMedia = new JSONObject();
								JSMedia.put("ID", Cmed.getString(Cmed.getColumnIndex("ID")));
								JSMedia.put("Name", Cmed.getString(Cmed.getColumnIndex("Name")));
								JSMedia.put("File", Cmed.getString(Cmed.getColumnIndex("File")));
								JSMedia.put("Type", Cmed.getString(Cmed.getColumnIndex("Type")));
								JSMedia.put("Size", Cmed.getString(Cmed.getColumnIndex("Size")));
								JSMedia.put("Status", Cmed.getString(Cmed.getColumnIndex("Status")));
								JSMedia.put("PartsSent", Cmed.getString(Cmed.getColumnIndex("PartsSent")));
								jArrayM.put(JSMedia);
								tempObjects.add(JSMedia);
				 			}while(Cmed.moveToNext());							
						
						  if(!getStatus("MediaTable")){
						        sendValues(jArrayM,"Media");
						  }
						     
						    ReadyOff=false;  
							jArraySAIM = new JSONArray();     
							CsusA.moveToFirst();
							do{
								JSAIM = new JSONObject();
								JSAIM.put("NEXARInfo", CsusA.getString(CsusA.getColumnIndex("NEXARInfo")));
								JSAIM.put("Media", CsusA.getString(CsusA.getColumnIndex("Media")));
								jArraySAIM.put(JSAIM);
				            }while(CsusA.moveToNext());
							 if(!getStatus("NexarInfoMediaTable")){ 
								  sendValues(jArraySAIM,"NEXARInfoMedia");
							 }
							 getObject(jArrayM);
			
					}//END Cmed	
			 }
				Csus.close();
				Cmed.close();
				CsusA.close();
		 }catch (Throwable t) {
			// checkView();
			 }
		Log.d("test 5","test 5 last");

	}

	
	public void sendValues(JSONArray values, String temp) throws Exception{

		Log.d("test 6","test 6 last");
	try{
      
		int length=values.length();
		HttpClient client = new Client(context);  
	
        String url = saveData+"?Length="+length+"&Table="+temp;        
        HttpPost request = new HttpPost(url);
        request.setEntity(new ByteArrayEntity(values.toString().getBytes("UTF8")));        
        request.setHeader("json", values.toString());
        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();
        // If the response does not enclose an entity, there is no need
        Log.d("test 7777","test 7777 last");
        if (entity != null) {
        	   Log.d("test 7333","test 7333 last");
            InputStream instream = entity.getContent();
            String result =  RestClient.convertStreamToString(instream);
            Log.d("test 74444","test 7444 last");
            Log.d("here is the response",""+result);
            if(result.equals("success")&& ReadyOff==false){
            	Ready=true;	
            	updateTableStatus(temp);
            }else{
            	Ready=false;
            	ReadyOff=true;
            }
        	Log.d("sent","valor de ready"+result);
        }

	}catch (Throwable t) {
	 Log.d("","request fail server"+t.toString());
	 
	 //checkView();
	 		
	}
  }//End Send Values

	public void updateTableStatus(String tableName){
		String ID=SingletonInformation.getInstance().reports.getFirst();
		
		ContentValues values = new ContentValues();
		
		if(tableName.equals("NEXARInfo")){
		values.put(TableStatus.COLUMN_NEXARINFO_TABLE, 1);
		}else if(tableName.equals("NEXARInfoMedia")){
			values.put(TableStatus.COLUMN_NEXARINFOMEDIA_TABLE, 1);
		}
		else if(tableName.equals("Media")){
			values.put(TableStatus.COLUMN_MEDIA_TABLE, 1);
		}
		//Uri uri = MediaTable.CONTENT_URI;
		context.getContentResolver().update(TableStatus.CONTENT_URI, values, "ID=?",new String[]{ID});
		
	}
	
	public void deleteTableStatus(){
		try{
			String ID=SingletonInformation.getInstance().reports.getFirst();
			context.getContentResolver().delete(TableStatus.CONTENT_URI,"ID=?", new String [] {ID});
			}catch (Exception e)
		 {
			 e.printStackTrace();
		 }
	}
	
	public boolean getStatus(String tableName){
		
		String ID=SingletonInformation.getInstance().reports.getFirst();
		Cursor Csus = newDB.rawQuery("Select * from TableStatus WHERE ID=?", new String[]{ID});
		 if(Csus.getCount()!=0){
			 Csus.moveToFirst();			
			 int temp=Csus.getInt(Csus.getColumnIndex(tableName));
			 if(temp==1){
				 return true;
			 }		 
		 }
		return false;
	}
	
	public void getObject(JSONArray objects) throws Exception{
		
		for(int a=0; a<objects.length(); a++){
			Log.d("values in jsarray","val"+objects.get(a));
			
		}
		
		Log.d("test  1.1","test  1.1" );
		JSONObject rec =objects.getJSONObject(0);
		 IdCurrentImage=rec.getString("ID");
         Log.d("id image","id image"+IdCurrentImage);
		getChuncks(rec.getString("File"),rec.getInt("PartsSent"));
		
	}
	
	public void getChuncks( String Path, int jump )  throws Exception{
		Log.d("test  1.2","test  last1.2"+"path "+Path+"parts sent "+jump );
			File file=new File(Path);
			InputStream is = new FileInputStream(file);
			int length = (int)file.length();
		    //int take=524288;
		   // int take=262144;
            if(jump!=0){
            	requestsToServer.clear();
            }
			int take=131072;
			byte[] bytes;// = new byte[take];
		    int a=0;
		    int offset=jump;
		    try{
		       is.skip(jump);
		    do{
		    	Log.d("test  1.22","test  last1.22" );
		    	if((offset+take)<=length){
		    		bytes = new byte[take];
		    	}else{
		    		bytes = new byte[(length-offset)];
		    	}
		    	a=is.read(bytes,0,bytes.length);
		    	offset+=a;
				Log.d("test  1.3","test last1.3" );
		    	String str = Base64.encodeToString(bytes, Base64.DEFAULT);				
		    	Log.d("test  1.3000","test  last1.3000" );
		    	//Log.d("data encoded",str);
		    	  Log.d("test no entro 1.31","test no entro last1.31" );
		    		HttpPost post = new HttpPost(saveMedia);

		    		MultipartEntity mpEntity = new MultipartEntity();        
		    		//Add the data to the multipart entity
		    		mpEntity.addPart("data", new StringBody(str, Charset.forName("UTF-8")));
		    		mpEntity.addPart("ID", new StringBody(IdCurrentImage, Charset.forName("UTF-8")));
		    		mpEntity.addPart("PartsSent", new StringBody (""+a, Charset.forName("UTF-8")));
		    		post.setEntity(mpEntity);
		    		requestsToServer.add(post);		    		
		    		Log.d("test no entro 1.4","test no entro last1.4" );
		    	}while(offset<length);

		    is.close();
		    uploadImage();
		    }catch (Exception e)
			 {   
		    	Log.d("test no entro 1.error","test no entro last1.error" );
		    	e.printStackTrace();    
		        Log.d("test no entro 1.error","test no entro last1.error" );
			 }
		    Log.d("test no entro 1.5","test no entro last1.5" );

		    
		    
	}
	
	public void uploadImage() throws Exception{
		Log.d("test 0","test 0 last");
		if(requestsToServer.size()!=0){
			String status="";
			String totalSaved="";
			String res="";
			
			HttpClient client = new Client(context); 
			HttpPost post=requestsToServer.getFirst();
			//Execute the post request
			HttpResponse response = client.execute(post);
			//Get the response from the server
			HttpEntity resEntity = response.getEntity();
			String Response=EntityUtils.toString(resEntity);
			Log.d("Response:", Response);
			//Generate the array from the response
			JSONArray jsonarray = new JSONArray("["+Response+"]");
			JSONObject jsonobject = jsonarray.getJSONObject(0);
			//Get the result variables from response 
			status = (jsonobject.getString("status"));
			totalSaved = (jsonobject.getString("totalSaved"));
			res = (jsonobject.getString("res"));
			Log.d("test 0.1","test 0.1 last");
			//Close the connection
			client.getConnectionManager().shutdown();
			//After the request verifies info.
		    int TotalKB=0;

		    if(totalSaved!=null){
		    	TotalKB=Integer.parseInt(totalSaved);
		    }else{
		    	TotalKB=0;		    	
		    }
		    
		    if(status.equals("1")){
		    	updateStatus("1",TotalKB);
		    }else{
		    	updateStatus("0",TotalKB);
		    }
		    Log.d("test 0.2","test 0.2 last");
	    	if(res.equals("success")){
			    requestsToServer.removeFirst();
	    	}else{
	    		requestsToServer.clear();
	    	}
	    	uploadImage();
		}
		else
		{
			Log.d("test no entro","test no entro last");
			if(tempObjects.size()!=0){
            	 tempObjects.remove(0);
            	 if(tempObjects.size()!=0){
            		 JSONArray nextImage=new JSONArray(tempObjects);
                	 getObject(nextImage);  
    			} 
			}
			Log.d("test no entro 1","test no entro last1" );
		}
	}
	
	
	
	public  void updateStatus(String status,int TotalKB) throws Exception {
		try{
			//if status==0 mean the image is not on the server
		if(status.equals("0")){
			Log.d("es 0","es 0");
			String where="ID=?";
			ContentValues values2 = new ContentValues();
			values2.put(MediaTable.COLUMN_Status, status);
			values2.put(MediaTable.COLUMN_PartsSent, TotalKB);
			//Uri uri = MediaTable.CONTENT_URI;
			context.getContentResolver().update(MediaTable.CONTENT_URI, values2, where,new String[]{IdCurrentImage});
		}else{
			Log.d("es 1","es 1");
			context.getContentResolver().delete(MediaTable.CONTENT_URI,"ID=?", new String [] {IdCurrentImage});
			context.getContentResolver().delete(AnonymousInfoMediaTable.CONTENT_URI,"Media=?", new String [] {IdCurrentImage});
		}
			
		 }catch (Exception e)
		 {
			 e.printStackTrace();
		 }
	}
   	
		
	public  void CleanDatabase(String Table,JSONArray values, boolean ready ) throws Exception {
		Log.d("clean","cleandatabase"+Ready);
		try{
			if(ready)
			{
			String ID="";
				if(Table.equals("AnonymousInfo")){
					for(int n = 0; n < values.length(); n++)
					{
						JSONObject rec =values.getJSONObject(n);
						ID=rec.getString("ID");	
						context.getContentResolver().delete(AnonymousTable.CONTENT_URI,"ID=?", new String [] {ID});
					}
				}
			}//End if
			}catch (Exception e)
		 {
			 e.printStackTrace();
		 }
	}

	public void checkView(){
		if (progressDialog.isShowing()) {
			progressDialog.dismiss();
			
			Intent returnIntent = new Intent();
			returnIntent.putExtra("action","submit");
			activityClass.setResult(-1,returnIntent);
			activityClass.finish();
	 }
	}
	
	//Displays a AlertDialog with the title and message specified.
	  public void alertDialog(String title,String message){
		 try
		 {
		  AlertDialog.Builder builder = new AlertDialog.Builder(SingletonInformation.getInstance().currentCon);
		  builder.setTitle(title)
      		.setMessage(message)
      		.setCancelable(false)
      		.setNegativeButton("OK",new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {

                   }
               });
               AlertDialog alert = builder.create();
               alert.show();
		 }catch(Exception e){
			Log.d("error","cant display alerts");
		 }
		}
	  
	

}
