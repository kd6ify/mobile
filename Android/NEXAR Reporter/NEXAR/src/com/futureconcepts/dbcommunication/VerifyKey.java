package com.futureconcepts.dbcommunication;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.futureconcepts.anonymous.AnonymousActivity;
import com.futureconcepts.anonymous.ChooseSchool;
import com.futureconcepts.anonymous.ViewPendingData;
import com.futureconcepts.customclass.Client;


/**
 *  Purpose: send data to server.
 */
public class VerifyKey {
	String school;
	String studentKey;
	String deviceID;
	String action;
	Context con;
	String result;
	ChooseSchool chooseSchool;
	
	public VerifyKey(String school, String studentKey, String deviceID,String action, Context con) {
		super();
		this.school = school;
		this.studentKey = studentKey;
		this.deviceID = deviceID;
		this.action = action;
		this.con = con;
	}
	
	public void validateKey()
	{
		new sendRequest().execute();
		
	}
	
	/**
	 * @param context: context of the activity.
	 * @param Response: response of the server.
	 */
	public static void alert( final Context context, String Response)
	{
		final AlertDialog.Builder alert = new AlertDialog.Builder(context);
		alert.setTitle("Validation Key");
		alert.setCancelable(false);
		if(Response.equals("saved"))
		{
			alert.setMessage("Key was successfully validated. Welcome to Nexar.");
			        
		}else if (Response.equals("fail"))
		{
			alert.setMessage("Key not Available please try again");
		}else if(Response.equals("invalid"))
		{
			alert.setMessage("Your key is not valid, you cannot send the report.");
		}
		else if(Response.equals("connection fail"))
		{
			alert.setMessage("Connection to server failed.");
		}
	      alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	        	  dialog.dismiss();
	        }
	    });
	      alert.show();		
	}
	
	/**
	 * @param school: id of the school registered.
	 * @param studentKey: key registered.
	 * @param DeviceID: unique device ID.
	 * @param action: action to perform on server.
	 * @return response of server.
	 */	
	public class sendRequest extends AsyncTask<Object, Object, String>{
		
		@Override
		protected String doInBackground(Object... params) {
			// TODO Auto-generated method stub
			try {
				result = sendKeyToServer(school,studentKey,deviceID,action,con);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return result;
		}
		
		protected void onPostExecute(String result) {
			if("ChooseSchool".contains(con.getClass().getSimpleName()))
			{
				ChooseSchool cs = (ChooseSchool) con;
				cs.handleVerifyKeyResult(result, studentKey);
			}else if("AnonymousActivity".contains(con.getClass().getSimpleName()))
			{
				AnonymousActivity cs = (AnonymousActivity) con;
				cs.handleVerifyKeyResult(result);
			}else if("ViewPendingData".contains(con.getClass().getSimpleName()))
			{
				ViewPendingData cs = (ViewPendingData) con;
				cs.handleVerifyKeyResult(result);				
			}
			
	     }

		
	public String sendKeyToServer(String school, String studentKey, String DeviceID, String action, Context con) throws Exception{
     		
		try 
		{
			ServerInformation serverInfo=new ServerInformation();
	        HttpClient client = new Client(con);
			//String saveKey = "https://172.16.21.187/NEXAR/saveKeys.php";
			String saveKey = serverInfo.SaveKeys;
	        HttpPost postKey = new HttpPost(saveKey);
			MultipartEntity mpEntity = new MultipartEntity();        
			//Add the data to the multipart entity
			mpEntity.addPart("SchoolID", new StringBody(school, Charset.forName("UTF-8")));
			//mpEntity.addPart("Table", new StringBody("SchoolKeys", Charset.forName("UTF-8")));
			mpEntity.addPart("Action", new StringBody(action,Charset.forName("UTF-8")));
			mpEntity.addPart("StudentKey", new StringBody(studentKey,Charset.forName("UTF-8")));
			mpEntity.addPart("DeviceID", new StringBody ( DeviceID, Charset.forName("UTF-8")));
			postKey.setEntity(mpEntity);      
 
			HttpPost post=postKey;
			//Execute the post request
			HttpResponse response = client.execute(post);
			//Get the response from the server
			HttpEntity resEntity = response.getEntity();
			if(resEntity != null)
			{
				  InputStream instream = resEntity.getContent();
		          String serverResponse =  RestClient.convertStreamToString(instream);
		          Log.d("SaveKey","This is dthe result of response:  "+ serverResponse);
		          client.getConnectionManager().shutdown();
		   		return serverResponse;
			}else
			{
				return null;
			}
		}catch(Exception e)
		{ e.printStackTrace();
		 
		}
		
		return "connection fail";
		
  }


}
}