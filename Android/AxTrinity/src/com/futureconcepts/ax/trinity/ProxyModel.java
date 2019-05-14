package com.futureconcepts.ax.trinity;

import java.io.IOException;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import com.futureconcepts.gqueue.MercurySettings;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public abstract class ProxyModel extends Thread implements Runnable
{
	private static final String TAG = "ProxyModel";
	
	protected DefaultHttpClient mClient;
	        
    private IProxyModelObserver mObserver;
    
    private Handler mHandler = new Handler();
    
    private Context mContext;
    
    private String mUrl;
    
    private boolean mIsDataReady = false;
    
	public ProxyModel(Context context, String url, IProxyModelObserver observer)
	{
		mContext = context;
		mUrl = url;
		mObserver = observer;
	}
	
	@Override
	public void destroy()
	{
		mHandler.post(new Runnable() {
			public void run() {
				mObserver.onDataReady();
			}
		});
	}
	
	public void setUrl(String value)
	{
		mUrl = value;
	}
	
	public HttpGet getHttpGet()
	{
		HttpGet get = new HttpGet(mUrl);
		get.getParams().setIntParameter("http.socket.timeout", 10000); // has no effect
		return get;
	}
	
	public String getResponseString() throws ClientProtocolException, IOException
	{
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		return mClient.execute(getHttpGet(), responseHandler);
	}
	
	public void run()
	{
		try
		{
			mClient = new DefaultHttpClient();
			String deviceId = MercurySettings.getDeviceId(mContext);
			String password = MercurySettings.getPassword(mContext);
			mClient.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, "tracker.antaresx.net"), new UsernamePasswordCredentials(deviceId, password));
			queryServer();
			mIsDataReady = true;
			mHandler.post(new Runnable() {
				public void run() {
					Log.i(TAG, "fire onDataReady");
					mObserver.onDataReady();
				}
			});
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			mHandler.post(new Runnable() {
				public void run() {
					String message = e.getMessage();
					String cause = "cause unknown";
					if (e.getCause() != null)
					{
						cause = e.getCause().getMessage();
					}
					mObserver.onError(message + ": " + cause);
				}
			});
		}
	}

	public abstract void queryServer() throws JSONException, ClientProtocolException, IOException;
	
	public void setDataReady()
	{
		mIsDataReady = true;
		if (mObserver != null)
		{
			mObserver.onDataReady();
		}
	}
	
	public boolean isDataReady()
	{
		return mIsDataReady;
	}
}
