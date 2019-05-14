package com.futureconcepts.mercury.main;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.futureconcepts.mercury.Config;
import com.futureconcepts.mercury.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class SetPasswordActivity extends Activity
{
	private static final String TAG = SetPasswordActivity.class.getSimpleName();
	private Config _config;
	private String _deviceId;
	private String _serverUrl;
	private String _password;
	
	@Override
	public void onCreate(Bundle savedState)
	{
		super.onCreate(savedState);
		_config = Config.getInstance(this);
		_deviceId = _config.getDeviceId();
		_serverUrl = _config.getWebServiceAddress() + "/Authenticate/Login";
//		requestWindowFeature(Window.FEATURE_PROGRESS);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.set_password);
		final TextView passwordView = (TextView)findViewById(R.id.password);
		TextView usernameView = (TextView)findViewById(R.id.username);
		usernameView.setText("Password for " + _config.getDeviceId());
		setProgressBarIndeterminate(true);
		((Button)findViewById(R.id.save)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try
				{
					new LoginAsyncTask().execute(String.valueOf(passwordView.getText()));
				}
				catch (Exception e)
				{
					onError(e.getMessage());
				}
			}
		});
		((Button)findViewById(R.id.cancel)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void savePassword() throws Exception
	{
		Config config = Config.getInstance(this);
		config.setPassword(_password);
	}
	
	private void onError(String message)
	{
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("Login failed");
		ab.setMessage(message);
		ab.setNeutralButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
//				finish();
			}
		});
		AlertDialog dialog = ab.create();
		dialog.show();
	}

	private void broadcastLoginCompleted()
	{
		sendBroadcast(new Intent("com.futureconcepts.action.LOGIN_COMPLETED"));
		Log.d(TAG, "sent LOGIN_COMPLETED");
	}
	
	private class LoginAsyncTask extends AsyncTask<String, Void, String>
	{
		private DefaultHttpClient _client;
		private HttpContext _httpContext;
			
	    public LoginAsyncTask()
	    {
 	    	_client = new DefaultHttpClient();
	    	_httpContext = new BasicHttpContext();
			BasicScheme basicAuth = new BasicScheme();
			_httpContext.setAttribute("preemptive-auth", basicAuth);
			_client.addRequestInterceptor(new PreemptiveAuth(), 0);
	    }

		@Override
		protected String doInBackground(String... args)
		{
			String result = null;
			_password = args[0];
			try
			{
				_client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, "tracker.antaresx.net"), new UsernamePasswordCredentials(_deviceId, _password));
				HttpGet get = new HttpGet(_serverUrl);
				get.getParams().setIntParameter("http.socket.timeout", 60 * 1000); // 1 minute
				get.addHeader("DeviceId", _deviceId);
				HttpResponse response = _client.execute(get, _httpContext);
				HttpEntity ent = response.getEntity();
				if (ent != null)
				{
					ent.consumeContent();
				}
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() != 200)
				{
					result = statusLine.getReasonPhrase();
				}
			}
			catch (Exception e)
			{
				result = e.getMessage();
			}
			return result;
		}
		
		@Override
		protected void onPreExecute()
		{
			setProgressBarIndeterminateVisibility(true);
		}
		
		@Override
		protected void onPostExecute(String result)
		{
			setProgressBarIndeterminateVisibility(false);
			if (result != null)
			{
				onError(result);
			}
			else
			{
				try
				{
					savePassword();
					broadcastLoginCompleted();
					finish();
				}
				catch (Exception e)
				{
					onError(e.getMessage());
				}
			}
		}
	}
}
