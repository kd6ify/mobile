package com.futureconcepts.dbcommunication;


import java.io.InputStream;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.database.SQLException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;
import com.futureconcepts.database.AnonymousTable;

public class sendInformation{
	public SQLiteDatabase newDB;
	JSONObject JSAnonymous;
	JSONArray jArrayA = new JSONArray();
	Boolean StateCount=false;
	Boolean Ready=false;
	Boolean ReadyOff=false;	
	
	String saveData="";
	String saveMedia="";
	Context context;
	
	public  void getValues(String ip, String port, Context con)
	{		
		context=con;
		//saveData = "http://"+ip+":"+port+"/TheObserver/saveData.php";
		//saveMedia = "http://"+ip+":"+port+"/TheObserver/saveMedia.php";
		saveData = "http://"+ip+"/Anonymous/saveData.php";
		
		Log.d("Send information class","Esto recibi en get values como ip y port "+ip+port);
		Log.d("esto TIENE SaveData","SAVEDATA "+saveData);
		try
		{
			newDB = SQLiteDatabase.openDatabase("/data/data/com.futureconcepts.anonymous/databases/anonymous.db",null,SQLiteDatabase.CONFLICT_NONE);
			
		}catch(SQLException e)
        {
            Log.d("Error","Error while Opening Database");
            e.printStackTrace();
        }
		
		try
		{
			Cursor Csus = newDB.rawQuery("Select * from AnonymousInfo",null);
				Csus.moveToFirst();			
				do{ 
					Log.d("anonymousInfo","HAVE something");
					 JSAnonymous = new JSONObject();
					 JSAnonymous.put("ID", Csus.getString(Csus.getColumnIndex("ID")));
					 JSAnonymous.put("Name", Csus.getString(Csus.getColumnIndex("Name")));
					 JSAnonymous.put("Details", Csus.getString(Csus.getColumnIndex("Details")));
					 JSAnonymous.put("Date", Csus.getString(Csus.getColumnIndex("Date")));
					 JSAnonymous.put("Type", Csus.getString(Csus.getColumnIndex("Type")));
					 JSAnonymous.put("School", Csus.getString(Csus.getColumnIndex("School")));
				   jArrayA.put( JSAnonymous);
				   
				}while(Csus.moveToNext());
				//sendValues(jArrayA,"AnonymousInfo");
				 new SendInfo ().execute();
				
				ReadyOff=false;
	
				Csus.close();
				newDB.close();
				
				Log.d("","ALL THE DATA"+jArrayA.toString());
		 }catch (Throwable t) {
	    	 Log.d("","request fail gettin data"+t.toString());
	    }
		
	}
	public void sendValues(JSONArray values, String temp){
	try{
		int length=values.length();
		HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams,9000);
        HttpConnectionParams.setSoTimeout(httpParams, 9000);
        
        HttpClient client = new DefaultHttpClient(httpParams);
        
        String url = saveData+"?Length="+length+"&Table="+temp;

        HttpPost request = new HttpPost(url);
        request.setEntity(new ByteArrayEntity(values.toString().getBytes("UTF8")));
        
        request.setHeader("json", values.toString());
        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();
        // If the response does not enclose an entity, there is no need

        if (entity != null) {
            InputStream instream = entity.getContent();
            String result =  RestClient.convertStreamToString(instream);
            Log.d("here",""+result);
            if(result.equals("success")&& ReadyOff==false){
            	Ready=true;
            }else{
            	Ready=false;
            	ReadyOff=true;
            }
        	Log.d("sent","valor de ready"+Ready);
        }
		
	}catch (Throwable t) {
	 Log.d("","request fail server"+t.toString());
	}
  }//End Send Values
	
   	
		
	public static void CleanDatabase(String Table,JSONArray values,Boolean Ready,Context context) throws Exception {
		Log.d("clean","cleandatabase"+Ready);
		
		try{
			String ID="";
			if(Ready==true && Table.equals("AnonymousInfo")){
				for(int n = 0; n < values.length(); n++)
				{
				    JSONObject rec =values.getJSONObject(n);
				    ID=rec.getString("ID");	
				    context.getContentResolver().delete(AnonymousTable.CONTENT_URI,"ID=?", new String [] {ID});
				}
			}
			}catch (Exception e)
		 {
			 e.printStackTrace();
		 }
	}
	
	

class SendInfo extends AsyncTask {	 
	        @Override
	        protected Object doInBackground(Object... arg0) {
	        	sendValues(jArrayA,"AnonymousInfo");
	        	try {
					CleanDatabase("AnonymousInfo",jArrayA,Ready,context);
				} catch (Exception e) {
					e.printStackTrace();
				}
	            return null;
	        }
   }
}
