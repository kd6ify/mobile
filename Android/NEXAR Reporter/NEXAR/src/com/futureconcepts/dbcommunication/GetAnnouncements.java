package com.futureconcepts.dbcommunication;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.futureconcepts.anonymous.AnonymousHome;
import com.futureconcepts.anonymous.ArrayAdapterAnnouncements;
import com.futureconcepts.anonymous.DataList;
import com.futureconcepts.anonymous.R;
import com.futureconcepts.customclass.Client;
import com.futureconcepts.database.DatabaseHelper;
import com.futureconcepts.database.SchoolFeedTable;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;

/*
 Is caled from AnonymousHome only when is monday or when is the first lunch.
 Is called from NetworkReceiver the next time the internet acces is enabled.
 */
public class GetAnnouncements extends AsyncTask<Void, Object, Object>{
	public Context context;
	public 	ListView listView;
	ArrayList<DataList> list;
	String[] values;
	private SQLiteDatabase newDB;
	boolean Home=false;
	boolean RequestFaild=false;
    public ProgressDialog progressDialog;
	
    public GetAnnouncements(AnonymousHome anonymousHome, ListView listView2) {
          context=anonymousHome;
          listView=listView2;
          progressDialog=new ProgressDialog(context);

	}

	@Override
    protected void onPreExecute() {
        this.progressDialog.setMessage("Loading Announcements");
       // this.progressDialog.setCancelable(false);
        this.progressDialog.show();
    }
    
	@Override
    protected Object doInBackground(Void... arg0) {
		//get the data to show

    	Log.d("background","in");
    	try{

	        HttpClient client = new Client(context);
	        Log.d("background","3");
	        ServerInformation serverInfo=new ServerInformation();
	        String url =serverInfo.GetData; 

	        HttpPost request = new HttpPost(url);
	        
	        MultipartEntity mpEntity = new MultipartEntity();        
			mpEntity.addPart("Table", new StringBody("SchoolFeed", Charset.forName("UTF-8")));
			request.setEntity(mpEntity);
			
	        HttpResponse response = client.execute(request);
	        HttpEntity entity = response.getEntity();
	        // If the response does not enclose an entity, there is no need
	        if (entity != null) {
	              String Response=EntityUtils.toString(entity);
		         // Log.d("Response:", Response);
		          //Generate the array from the response
		          JSONArray jsonarray = new JSONArray(""+Response+"");
		          updateSchoolFeed(jsonarray);
	         }
	        
		}catch (Throwable t) {
		 Log.d("","request fail server"+t.toString());
		 RequestFaild=true;
		}
        return null;
    }

    @SuppressLint("SimpleDateFormat")
	@Override
    protected void onPostExecute (Object result){
        list=new ArrayList<DataList>();
    	list.clear();
     if(RequestFaild==false){
    	 Log.d("request","faild still in");
		 Calendar calendar = Calendar.getInstance();
		 Calendar calendarEnd = Calendar.getInstance();
		 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		 int day = calendar.get(Calendar.DAY_OF_WEEK);

        if(day==7 || day==1){//saturday and sunday
   			calendarEnd.add(Calendar.DAY_OF_MONTH, (0));
   		}else{
   			calendarEnd.add(Calendar.DAY_OF_MONTH, (6-day));//Friday.
   			calendar.add(Calendar.DAY_OF_MONTH, (2-day));//Monday.
   		}
   		String endUpdate = df.format(calendarEnd.getTime());
		String formattedDate = df.format(calendar.getTime());

    	DatabaseHelper dbHelper = new DatabaseHelper(context);
    	newDB = dbHelper.getWritableDatabase(); 
        Cursor CSF = newDB.rawQuery("Select * from SchoolFeed where Date BETWEEN"+"'"+formattedDate+"'"+"AND"+"'"+endUpdate+"'"+"ORDER BY Date ASC",null);
    		
    	if(CSF.getCount()!=0){
    		CSF.moveToFirst();			
    			do{ 
    				Log.d("SchoolFeed","HAVE something");
    				list.add(new DataList(CSF.getString(CSF.getColumnIndex("Details")),"Posted: "+CSF.getString(CSF.getColumnIndex("Date"))));
    				Log.d("add","to list");
    			}while(CSF.moveToNext());	 
    		}else{
    			list.add(new DataList("No Announcements for this week",""));  
    		 }
    		Log.d("here","in the update22");

   	     	//ListView listView2 = listView.findViewById(R.id.schoolFeed); 
    		
            ArrayAdapterAnnouncements adapter= new ArrayAdapterAnnouncements(context,R.layout.list_layout,list);
    	 	listView.setAdapter(adapter);
    	
   	     	CSF.close();
   	     	newDB.close();
    	}else{
    		list.add(new DataList("The Ads can't be downloaded, we will try to update the next time you open the app.",""));
    		 ArrayAdapterAnnouncements adapter= new ArrayAdapterAnnouncements(context,R.layout.list_layout,list);
     	 	 listView.setAdapter(adapter);
    	}
     
     if (progressDialog.isShowing()) {
			progressDialog.dismiss();
     }
     
    }
    
   public void updateSchoolFeed(JSONArray jsonarray) throws JSONException{
         
	   DatabaseHelper dbHelper = new DatabaseHelper(context);
  		newDB = dbHelper.getWritableDatabase();
  		newDB.delete("SchoolFeed",null,null);
  		ContentValues schoolFeedValues = new ContentValues();

	   
	   for(int n = 0; n < jsonarray.length(); n++)
  		{
  		    JSONObject rec =jsonarray.getJSONObject(n);
  		    String ID=rec.getString("ID");
  		    String Details=rec.getString("Details");
  		    String Date=rec.getString("Date");
  		    String Time=rec.getString("Time");
  		    
  		    schoolFeedValues.put(SchoolFeedTable.COLUMN_ID,ID);
  		    schoolFeedValues.put(SchoolFeedTable.COLUMN_DETAILS,Details);
  			schoolFeedValues.put(SchoolFeedTable.COLUMN_DATE,Date);
  			schoolFeedValues.put(SchoolFeedTable.COLUMN_TIME,Time);
  			context.getContentResolver().insert(SchoolFeedTable.CONTENT_URI,schoolFeedValues);
  			
  		    //Log.d("INSERT","here"+"ID"+ID+"Details"+Details+"  Date"+Date+"  Time"+Time+" length"+jsonarray.length());
  		}	   

	   newDB.close();
    }

   
}//end asyncTask