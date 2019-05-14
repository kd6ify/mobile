package com.futureconcepts.ax.video.viewer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class AuthenticateStreamActivity extends Activity
{
	public static final String TAG = AuthenticateStreamActivity.class.getSimpleName();
	
	private String _streamID;
	private String _streamName;
	private Uri _uri;
	
    public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		
		//Create a VideoView widget in the layout file
		//use setContentView method to set content of the activity to the layout file which contains videoView
		setContentView(R.layout.authenticate_stream);

		_streamID = getIntent().getStringExtra("ID");
		_streamName = getIntent().getStringExtra("Name");

		new GetUrlAsyncTask().execute();
    }

    @Override
    public void onDestroy()
    {
    	Log.d(TAG, "onDestroy");
    	super.onDestroy();
    }
	
	private String getWebServiceUrl()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String serviceAddress = prefs.getString(Settings.KEY_WEB_SERVICE_ADDRESS, "localhost");
		return String.format("http://%s:8080/one_time_url?ID=%s", serviceAddress, _streamID);
	}
	
	private Context getContext()
	{
		return this;
	}
	
	private void startViewer()
	{
		Intent intent = new Intent(this, ViewVideoActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.putExtra("ID", _streamID);
		intent.putExtra("Name", _streamName);
		intent.setData(_uri);
		startActivity(intent);
		finish();
	}
	
	private final class GetUrlAsyncTask extends AsyncTask<Void, Integer, Exception>
	{
		private ProgressDialog _progressDialog;
		
		@Override
		protected void onPreExecute()
		{
			Log.d(TAG, "GetUrlAsyncTask..onPreExecute");
			_progressDialog = new ProgressDialog(getContext());
			_progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			_progressDialog.setIndeterminate(true);
			_progressDialog.setTitle("Authentication");
			_progressDialog.setMessage(String.format("Authenticating %s...", _streamName));
			_progressDialog.show();
		}
		
		@Override
	    protected Exception doInBackground(Void... params)
	    {
	    	Exception result = null;
	        try
	        {
	        	String webServiceUrlString = getWebServiceUrl();
	        	URL url = new URL(webServiceUrlString);
	        	HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
	        	InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
	        	InputStreamReader inputStreamReader = new InputStreamReader(stream);
	        	BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	        	String path = bufferedReader.readLine();
	        	String serverAddress = Settings.getWebServiceAddress(getContext());
	        	_uri = Uri.parse(String.format("rtsp://%s:1935/%s", serverAddress, path));
				urlConnection.disconnect();
				Log.d(TAG, "doInBackground after disconnect");
			}
	        catch (IOException e)
	        {
				e.printStackTrace();
				result = e;
			}
	    	return result;
	     }
	    
//	     protected void onProgressUpdate(Integer... progress)
//	     {
//	         setProgressPercent(progress[0]);
//	     }

	     protected void onPostExecute(Exception result)
	     {
	    	 Log.d(TAG, "onPostExecute");
	    	 if (_progressDialog != null && _progressDialog.isShowing())
	    	 {
				_progressDialog.dismiss();
				_progressDialog = null;
	    	 }
	    	 if (result == null) // no error
	    	 {
	    		 startViewer();
	    	 }
	     }
	}
}
