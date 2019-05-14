package com.futureconcepts.ax.broadcaster.rtsp;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class RtspResponse
{
	// Status code definitions
	public static final String STATUS_OK = "200 OK";
	public static final String STATUS_BAD_REQUEST = "400 Bad Request";
	public static final String STATUS_NOT_FOUND = "404 Not Found";
	public static final String STATUS_INTERNAL_SERVER_ERROR = "500 Internal Server Error";

	// Parse a response status line
	public static final Pattern rexegResponseStatus = Pattern.compile("RTSP/1.0 (\\S+) (\\S+)", Pattern.CASE_INSENSITIVE);
	// Parse a response header
	public static final Pattern rexegHeader = Pattern.compile("(\\S+):(.+)",Pattern.CASE_INSENSITIVE);
	
	public String version;
	public int statusCode;
	public String statusText;
	public HashMap<String,String> headers = new HashMap<String,String>();

	public RtspResponse()
	{
	}

	public static RtspResponse parse(BufferedReader input) throws IOException
	{
		final String TAG = RtspResponse.class.getSimpleName();
		RtspResponse result = new RtspResponse();
		String line;
		Matcher matcher;

		// Parsing request method & uri
		if ((line = input.readLine()) == null)
		{
			throw new SocketException("Server disconnected");
		}
		matcher = rexegResponseStatus.matcher(line);
		matcher.find();
		result.statusCode = Integer.parseInt(matcher.group(1));
		result.statusText = matcher.group(2);
		Log.d(TAG, String.format("got Status %d %s", result.statusCode, result.statusText));
		// Parsing headers of the response
		while ( (line = input.readLine()) != null && line.length()>3 )
		{
			matcher = rexegHeader.matcher(line);
			matcher.find();
			result.headers.put(matcher.group(1).toLowerCase(Locale.US), matcher.group(2));
			Log.d(TAG, String.format("got Header %s: %s", matcher.group(1), matcher.group(2)));
		}
		if (line==null)
		{
			throw new SocketException("Server disconnected");
		}
		return result;
	}
}
