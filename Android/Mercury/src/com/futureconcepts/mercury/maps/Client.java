package com.futureconcepts.mercury.maps;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;

import com.futureconcepts.mercury.Config;
import com.futureconcepts.mercury.main.PreemptiveAuth;

public class Client extends DefaultHttpClient {
	//we will remove this and set MercurySettings.getMediaImagesServerAddress(context)
	//--============For test server--===============
//	public static String serverAddress;// = "http://205.129.7.52/MobileMedia/";
//	private static String username;// = "test1";
//	private static String password;// = "B83rhe2x";
//--============For live server--===============
//	public static String serverAddress = "https://media.antaresx.net/MobileMedia/";
//	private static String username = "axmedia";
//	private static String password = "ch4m+$hUt3uS";
	
	public static DefaultHttpClient madeClient(Context context)
	{
		Config _config = Config.getInstance(context);
		String username = _config.getMediaImagesServerUser();
		String password = _config.getMediaImagesServerPassword();
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 15000);//15 seg.
		HttpConnectionParams.setSoTimeout(httpParameters, 15000);//15 seg.
		DefaultHttpClient _client = new DefaultHttpClient();	
		_client.addRequestInterceptor(new PreemptiveAuth(), 0);
		_client.setParams(httpParameters);
		_client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), new UsernamePasswordCredentials(username,password));
		return _client;
	}
}
