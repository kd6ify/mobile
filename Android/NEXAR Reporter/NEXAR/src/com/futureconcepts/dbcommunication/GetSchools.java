package com.futureconcepts.dbcommunication;

import java.nio.charset.Charset;
import java.util.ArrayList;
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
import com.futureconcepts.anonymous.DataList;
import com.futureconcepts.customclass.Client;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

/**
 * Purpose: Get schools stored on server.
 **/
public class GetSchools{
	 public ProgressDialog progressDialog;	
	 public Context context;
	 /**
	  * @param c: Activity context.
	  */
	 public GetSchools (Context c)
	 {
		 context = c;
	 }
	
	 /**
	  * @return ArrayList of schools.
	  */
	public ArrayList<DataList>  doInBackground() {
		// TODO Auto-generated method stub
		try{

			HttpClient client = new Client(context);
			ServerInformation serverInfo=new ServerInformation();
	        String url = serverInfo.GetData;
	        HttpPost request = new HttpPost(url);
	       
	        MultipartEntity mpEntity = new MultipartEntity();        
			mpEntity.addPart("Table", new StringBody("School", Charset.forName("UTF-8")));
			request.setEntity(mpEntity);	        
	        
	        HttpResponse response = client.execute(request);
	        HttpEntity entity = response.getEntity();
	        // If the response does not enclose an entity, there is no need
	        if (entity != null) {
	              String Response=EntityUtils.toString(entity);		      
		          //Generate the array from the response
		          JSONArray jsonarray = new JSONArray(""+Response+"");
		        return updateSchool(jsonarray);
	         }        
		}catch (Throwable t) {
		 Log.d("","request fail server"+t.toString());
		 //return null;    
		}
		return null;
		    
	}

	  /**
	   * 
	   * @param jsonarray : Array returned from server.
	   * @return  ArrayList of schools.
	   * @throws JSONException
	   */
	   public ArrayList<DataList> updateSchool(JSONArray jsonarray) throws JSONException{
	       ArrayList<DataList>  result = new ArrayList<DataList>();
		   for(int n = 0; n < jsonarray.length(); n++)
	  		{
	  		    JSONObject rec =jsonarray.getJSONObject(n);
	  		    String ID=rec.getString("ID");
	  		    String Name=rec.getString("Name");
	  		    result.add( new DataList(ID,Name));
	  		  Log.d("","adding new itme");
	  		}
		  
		  return result;
	    }
   

}//end asyncTask
