package com.futureconcepts.gqueue;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.futureconcepts.gqueue.GQueue;
import com.futureconcepts.gqueue.QueueListenerService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public abstract class HttpQueueService extends QueueListenerService
{
	private static final String TAG = HttpQueueService.class.getSimpleName();

	private ConnectivityManager _connectivityManager;
	private DefaultHttpClient _client;
	private HttpContext _httpContext;
	private String _authToken;
	private BroadcastReceiver _loginCompletedReceiver;

	@Override
	public void onCreate()
	{
		super.onCreate();
		Date now = new Date(System.currentTimeMillis());
		Log.d(TAG, "onCreate at " + now.toString());
    	_loginCompletedReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent)
			{
				setCredentials();
			}
    	};
    	registerReceiver(_loginCompletedReceiver, new IntentFilter("com.futureconcepts.action.LOGIN_COMPLETED"));
	}
		
	@Override
	public void onDestroy()
	{
		Date now = new Date(System.currentTimeMillis());
		Log.d(TAG, "onDestroy at " + now.toString());
		if (_loginCompletedReceiver != null)
		{
			unregisterReceiver(_loginCompletedReceiver);
			_loginCompletedReceiver = null;
		}
		super.onDestroy();
	}

	@Override
	protected boolean onReceive(GQueue queue) throws OnReceiveFatalException, OnRetryException
	{
		String serverUrlUnfixed = queue.getServerUrl();
		byte[] content = queue.getContent();
		String contentType = queue.getContentMimeType();
		String action = queue.getAction();
		return postTransaction(serverUrlUnfixed, content, contentType, action);
	}

	protected boolean postTransaction(String serverUrlUnfixed, byte[] content, String contentType, String action ) throws OnReceiveFatalException, OnRetryException
	{
		try
		{
			if (_client == null)
			{
				BasicScheme basicAuth = new BasicScheme();
				_client = new DefaultHttpClient();
				_httpContext = new BasicHttpContext();
				_httpContext.setAttribute("preemptive-auth", basicAuth);
				_client.addRequestInterceptor(new PreemptiveAuth(), 0);
				setCredentials();
			}
			HttpRequestBase request = null;
			HttpEntityEnclosingRequestBase enclosingRequest = null;			
			String serverUrl = serverUrlUnfixed.replace("server:/", MercurySettings.getWebServiceAddress(this));
			if (action == null)
			{
				action = Intent.ACTION_VIEW;
			}
			if (action.equals(Intent.ACTION_VIEW))
			{
				HttpGet get = new HttpGet(serverUrl);
				request = get;
			}
			else if (action.equals(Intent.ACTION_INSERT))
			{
				HttpPut put = new HttpPut(serverUrl);
				enclosingRequest = put;
				request = put;
			}
			else if (action.equals(Intent.ACTION_EDIT))
			{
				HttpPost post = new HttpPost(serverUrl);
				enclosingRequest = post;
				request = post;
			}
			else if (action.equals(Intent.ACTION_DELETE))
			{
				HttpDelete delete = new HttpDelete(serverUrl);
				request = delete;
			}
			if (enclosingRequest != null)
			{
				if (content != null)
				{
					enclosingRequest.setEntity(new StringEntity(new String(content)));
					if (contentType == null)
					{
						request.addHeader("Content-Type", "application/json; charset=utf-8");
					}
					else
					{
						request.addHeader("Content-Type", contentType);
					}
				}
			}
			request.getParams().setIntParameter("http.socket.timeout", 60 * 1000); // 1 minute
			request.addHeader("DeviceId", MercurySettings.getDeviceId(this));
			Log.i(TAG, request.getRequestLine().toString());
			HttpResponse response = _client.execute(request, _httpContext);
			HttpEntity ent = response.getEntity();
			StatusLine statusLine = response.getStatusLine();
			Log.i(TAG, statusLine.toString());
			int statusCode = statusLine.getStatusCode();
			if (statusCode != 200)
			{
				 // handle gateway timeout (504) and temp unavailable (503) like a socket exception
				// socket exceptions are auto retried
				if (statusCode == 504 || statusCode == 503 || statusCode == 502)
				{
					throw new SocketException();
				}
				else if (statusCode == 403)
				{
					if (_authToken != null)
					{
						_authToken = null;
						setCredentials();
						throw new AuthTokenInvalidException();
					}
					else
					{
						throw new HttpException(statusLine.toString());
					}
				}
				else
				{
					throw new HttpException(statusLine.toString());
				}
			}
			if (ent != null)
			{
				onReceiveResponse(ent);
				ent.consumeContent();
			}
			Header authTokenHeader = response.getFirstHeader("AuthToken");
			if (authTokenHeader != null)
			{
				_authToken = new String(authTokenHeader.getValue()); 
				_client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, "tracker.antaresx.net"), new UsernamePasswordCredentials(MercurySettings.getDeviceId(this), _authToken));
			}
		}
		catch (AuthTokenInvalidException e)
		{
			throw new OnRetryException(e, 1000);
		}
		catch (OnRetryException e)
		{
			throw e;
		}
		catch (SocketException e)
		{
			throw new OnRetryException(e, 60 * 1000);
		}
		catch (SocketTimeoutException e)
		{
			throw new OnRetryException(e, 60 * 1000);
		}
		catch (ConnectTimeoutException e)
		{
			throw new OnRetryException(e, 60 * 1000);
		}
		catch (UnknownHostException e)
		{
			throw new OnRetryException(e, 60 * 1000 * 5); // a DNS error - how?  Retry in 5 minutes
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new OnReceiveFatalException(e);
		}
		return true;
	}
	
	private void setCredentials()
	{
		if (_client != null)
		{
			String deviceId = MercurySettings.getDeviceId(this);
			String password = MercurySettings.getPassword(this);
			_client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, "tracker.antaresx.net"), new UsernamePasswordCredentials(deviceId, password));
			Log.d(TAG, "password set to " + password);
		}
	}

	private ConnectivityManager getConnectivityManager()
	{
		if (_connectivityManager == null)
		{
			_connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
		}
		return _connectivityManager;
	}

	private boolean isConnectedAndAuthenticated()
	{
		boolean result = false;
		NetworkInfo networkInfo = getConnectivityManager().getActiveNetworkInfo();
		if ((networkInfo != null) && (networkInfo.getState() == NetworkInfo.State.CONNECTED) && (MercurySettings.getPassword(this) != null))
		{
			result = true;
		}
		return result;
	}
	
	protected abstract void onReceiveResponse(HttpEntity ent) throws OnReceiveFatalException, OnRetryException;
}
