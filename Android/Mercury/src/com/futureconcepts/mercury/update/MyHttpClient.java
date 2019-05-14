package com.futureconcepts.mercury.update;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import android.content.Context;

import com.futureconcepts.mercury.Config;

public class MyHttpClient extends DefaultHttpClient
{
	private Config _config;
	private HttpContext _httpContext;
	
	public MyHttpClient(Context context)
	{
		super();
		_config = Config.getInstance(context);
		_httpContext = new MyHttpContext();
		addRequestInterceptor(new PreemptiveAuth(), 0);
		String deviceId = _config.getDeviceId();
		String password = null;
		try {
			password = _config.getPassword();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, "tracker.antaresx.net"), new UsernamePasswordCredentials(deviceId, password));
	}
	
	public HttpResponse doExecute(HttpUriRequest request) throws ClientProtocolException, IOException
	{
		return super.execute(request, _httpContext);
	}
	
	private final class PreemptiveAuth implements HttpRequestInterceptor
	{
		public void process(HttpRequest request, HttpContext context) throws HttpException, IOException
		{
			AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
	        
	        // If no auth scheme avaialble yet, try to initialize it preemptively
	        if (authState.getAuthScheme() == null)
	        {
	            AuthScheme authScheme = (AuthScheme) context.getAttribute("preemptive-auth");
	            CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(ClientContext.CREDS_PROVIDER);
	            HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
	            if (authScheme != null)
	            {
	                Credentials creds = credsProvider.getCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()));
	                if (creds == null)
	                {
	                    throw new HttpException("No credentials for preemptive authentication");
	                }
	                authState.setAuthScheme(authScheme);
	                authState.setCredentials(creds);
	            }
	        }
		}
	}
	private final class MyHttpContext extends BasicHttpContext
	{
		public MyHttpContext()
		{
			setAttribute("preemptive-auth", new BasicScheme());
		}
	}
}
