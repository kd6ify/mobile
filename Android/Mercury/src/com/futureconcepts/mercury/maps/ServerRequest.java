package com.futureconcepts.mercury.maps;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;

import com.futureconcepts.mercury.Config;

import android.content.Context;
import android.util.Log;

public class ServerRequest {
	
	private static final String TAG= ServerRequest.class.getSimpleName();
	private static String _configurationServiceUrl;	
	public static String ACTION_DOWNLOAD_FILE = "downloadMapFile";
	public static String ACTION_GET_MAP_FILES = "getFiles";
	private ServerRequestAddParameters listener;
	
	public static interface ServerRequestAddParameters
	{
		 void addParameters(HttpPost post);
	}
	
	ServerRequest(String action,ServerRequestAddParameters listener)
	{
		if(listener!=null){
			this.listener = listener;
		}else
		{
			throw new RuntimeException("You must provide a Listener");
		}
	}	

	public JsonParser madeRequestToServer(Context context)
	{
		JsonFactory jsonFactory = new JsonFactory();
		Config _config = Config.getInstance(context);
		DefaultHttpClient _client = Client.madeClient(context);
		_configurationServiceUrl = _config.getMediaImagesServerAddress() + "maps.php";
		HttpPost post = new HttpPost(_configurationServiceUrl);
		listener.addParameters(post);
		Log.d(TAG, post.getRequestLine().toString());		
		HttpResponse response = null;
		try {
			response = _client.execute(post);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode != 200) {
				throw new Exception(post.toString() + " failed");
			}			
			return jsonFactory.createJsonParser(response.getEntity()
					.getContent());
		} catch (final Exception e) {
			e.printStackTrace();
		}finally
		{
			response  = null;
		}
		return null;
	}
	
//	private void addParameters(HttpPost post)
//	{ 
//		 List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
//		if(ACTION_GET_MAP_FILES.equals(currentAction)){			
//			nameValuePairs.add(new BasicNameValuePair("action", "getFiles"));
//		}else if(ACTION_DOWNLOAD_FILE.equals(currentAction))
//		{
//			 nameValuePairs.add(new BasicNameValuePair("action", "downloadMapFile"));
//			 nameValuePairs.add(new BasicNameValuePair("fileName", "fileName"));
//			 nameValuePairs.add(new BasicNameValuePair("fileSize", "bytesStored"));
//			 //nameValuePairs.add(new BasicNameValuePair("version", "ReplaceWithFileSize"));
//		}
//		try {
//			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

}
