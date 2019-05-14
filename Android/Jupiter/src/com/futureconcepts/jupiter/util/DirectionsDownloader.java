package com.futureconcepts.jupiter.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.os.Environment;
import android.util.Log;

public class DirectionsDownloader extends Thread
{
	private static final String TAG = "DirectionsDownloader";
	
	private static final String _f1 = "http://maps.google.com/maps?f=d&hl=en&saddr=%f,%f&daddr=%f,%f&ie=UTF8&0&om=0&output=dragdir";  

	private String _url;
	private DefaultHttpClient _client = new DefaultHttpClient();
	private HttpContext _httpContext = new BasicHttpContext();
	private File _outFile;
	
	public DirectionsDownloader(double from_latitude, double from_longitude, double to_latitude, double to_longitude)
	{
		_url = String.format(_f1, from_latitude, from_longitude, to_latitude, to_longitude);
	}

	public void setOutputFile(File file)
	{
		_outFile = file;
	}
	
	@Override
	public void run()
    {
		try
		{
			HttpGet get = new HttpGet(_url);
			get.getParams().setIntParameter("http.socket.timeout", 60 * 1000 * 5); // 5 minute
			Log.i(TAG, get.getRequestLine().toString());
			HttpResponse response = _client.execute(get, _httpContext);
			HttpEntity ent = response.getEntity();
			if (ent != null)
			{
				copyStream(ent.getContent(), new FileOutputStream(_outFile));
			}
			StatusLine statusLine = response.getStatusLine();
			Log.i(TAG, statusLine.toString());
			int statusCode = statusLine.getStatusCode();
			if (statusCode != 200)
			{
				 // handle gateway timeout (504) and temp unavailable (503) like a socket exception
				// socket exceptions are auto retried
				if (statusCode == 504 || statusCode == 503)
				{
					throw new SocketException();
				}
				else
				{
					throw new HttpException(statusLine.toString());
				}
			}
			else
			{
//				_listener.onComplete();
			}
			Log.d(TAG, "directions available");
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}
		catch (SocketTimeoutException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage());
			Throwable cause = e.getCause();
			while (cause != null)
			{
				sb.append("/Cause: ");
				sb.append(cause.getMessage());
				cause = cause.getCause();
			}
		}
		finally
		{
			_client.getConnectionManager().shutdown();
		}
    }
	
	private File getDownloadPath() throws IOException
	{
		File root = Environment.getExternalStorageDirectory();
		if (root.canWrite())
		{
			return new File(root, "directions.kml");
		}
		else
		{
			throw new IOException("SDCARD is not writeable");
		}
	}
	
	private void copyStream(InputStream inStream, OutputStream outStream) throws IOException
	{
		int bytesRead = 0;
		int accumulatedBytes = 0;
		byte[] bytes = new byte[512];
		while ((bytesRead = inStream.read(bytes)) != -1)
		{
			accumulatedBytes += bytesRead;
			outStream.write(bytes, 0, bytesRead);
		}
		outStream.flush();
		outStream.close();
	}
}
