package com.futureconcepts.mercury.main;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.w3c.util.InvalidDateException;

import com.futureconcepts.mercury.Config;
import com.futureconcepts.mercury.R;
import com.futureconcepts.mercury.sync.TransactionException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DownloadConfigurationActivity extends Activity implements Runnable
{
	private static final String TAG = "DownloadConfiguration";
	
    private Handler _handler = new Handler();
    
    private String _deviceId;
    
    private String _configurationServiceUrl;
    
	private DefaultHttpClient _client;
	private HttpContext _httpContext;

	private Thread _thread;
	
	private Config _config;
	
	@Override
    public void onCreate(Bundle icicle)
	{
		Log.d(TAG, "onCreate");
		super.onCreate(icicle);
		_config = Config.getInstance(this);
    	setContentView(R.layout.download_configuration);
    	((Button)findViewById(R.id.ok)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
    	});
		_thread = new Thread(this);
		_thread.start();
	}
	
	public void run()
	{
		try
		{
			JsonFactory jsonFactory = new JsonFactory();
			_client = new DefaultHttpClient();
	    	_httpContext = new BasicHttpContext();
			_client.getParams().setParameter("http.socket.timeout", Integer.valueOf(10000)); // has no effect
			BasicScheme basicAuth = new BasicScheme();
			_httpContext.setAttribute("preemptive-auth", basicAuth);
			_client.addRequestInterceptor(new PreemptiveAuth(), 0);
			_client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, "tracker.antaresx.net"), new UsernamePasswordCredentials(_config.getDeviceId(), _config.getPassword()));
			_deviceId = _config.getDeviceId();
			showKeyValue("DeviceId", _deviceId);
			_configurationServiceUrl = _config.getWebServiceAddress() + "/Admin/Configuration";
			showKeyValue("Config URL", _configurationServiceUrl);
			HttpGet get = new HttpGet(_configurationServiceUrl);
			get.addHeader("DeviceId", _deviceId);
			Log.d(TAG, get.getRequestLine().toString());
			HttpResponse response = null;
			try
			{
				response = _client.execute(get);
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode != 200)
				{
					throw new Exception(get.toString() + " failed");
				}
				parse(jsonFactory.createJsonParser(response.getEntity().getContent()));
				enableOK();
				showKeyValue("COMPLETE", "SUCCESS");
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				showKeyValue("ERROR", e.getMessage());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void parse(JsonParser p) throws JsonParseException, IOException, InvalidDateException, TransactionException
	{
		p.nextToken();
		while (p.nextToken() != JsonToken.END_OBJECT)
		{
			String name = p.getCurrentName();
			if (name.equals("ItemCount"))
			{
				ignoreValue(p);
			}
			else if (name.equals("Items"))
			{
				parseItems(p);
			}
			else if (name.equals("ErrorMessage"))
			{
				p.nextToken();
				throw new TransactionException(p.getText());
			}
			else
			{
				ignoreValue(p);
			}
		}
	}
	
	public void parseItems(JsonParser p) throws JsonParseException, IOException, InvalidDateException
	{
		if (p.nextToken() == JsonToken.START_ARRAY)
		{
			while (p.nextToken() != JsonToken.END_ARRAY)
			{
				if (p.getCurrentToken() == JsonToken.START_OBJECT)
				{
					while (p.nextToken() != JsonToken.END_OBJECT)
					{
						String name = p.getCurrentName();
						if (name.equals("Action"))
						{
							ignoreValue(p);
						}
						else if (name.equals("Content"))
						{
							parseContent(p);
						}
						else
						{
							ignoreValue(p);
						}
					}
				}
			}
		}
	}
	
	public void parseContent(JsonParser p) throws JsonParseException, IOException, InvalidDateException
	{
		if (p.nextToken() == JsonToken.START_ARRAY)
		{
			while (p.nextToken() != JsonToken.END_ARRAY)
			{
				if (p.getCurrentToken() == JsonToken.START_OBJECT)
				{
					while (p.nextToken() != JsonToken.END_OBJECT)
					{
						if (p.getCurrentToken() == JsonToken.FIELD_NAME)
						{
							String name = p.getCurrentName();
							p.nextToken();
							if (name.equals("WSUSBridgeServiceAddress"))
							{
								_config.setWsusServiceAddress(p.getText());
								showKeyValue(p);
							}
							else if (name.equals("PhoneNumber"))
							{
								_config.setPhoneNumber(p.getText());
								showKeyValue(p);
							}
							else if (name.equals("EquipmentId"))
							{
								_config.setMyEquipmentId(p.getText());
								showKeyValue(p);
							}
							else if (name.equals("DeviceName"))
							{
								_config.setDeviceName(p.getText());
								showKeyValue(p);
							}else if(name.equals("MediaImagesServerAddress"))
							{
								_config.setMediaImagesServerAddressField(p.getText());
								showKeyValue(p);
							}
							else if(name.equals("MediaImagesServerUser"))
							{
								_config.setMediaImagesServerUserField(p.getText());
								showKeyValue(p);
								//showKeyValue(p.getCurrentName(), "Adquired");
							}
							else if(name.equals("MediaImagesServerPassword"))
							{
								_config.setMediaImagesServerPasswordField(p.getText());
								//showKeyValue(p);
								showKeyValue(p.getCurrentName(), "*******");
							}
							else 
							{
								ignoreValue(p);
							}
						}
					}
				}
			}
		}
	}

	private void ignoreValue(JsonParser p) throws JsonParseException, IOException
	{
		p.nextToken();
		if (p.getCurrentToken() == JsonToken.START_OBJECT)
		{
			while (p.nextToken() != JsonToken.END_OBJECT)
			{
			}
		}
	}
	
	private void showKeyValue(JsonParser p) throws JsonParseException, IOException
	{
		showKeyValue(p.getCurrentName(), p.getText());
	}

	private void showKeyValue(final String key, final String value) throws JsonParseException, IOException
	{
		_handler.post(new Runnable() {
			public void run()
			{
				TextView view = (TextView)findViewById(R.id.text1);
				String msg = view.getText() + "\n" + key + ": " + value;
				view.setText(msg);
			}
		});
	}

	private void enableOK()
	{
		_handler.post(new Runnable() {
			public void run()
			{
				((Button)findViewById(R.id.ok)).setEnabled(true);
			}
		});
	}
}
