package com.futureconcepts.ax.broadcaster.rtsp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Set;

import android.util.Log;

public class RtspRequest
{
	private static final String TAG = RtspRequest.class.getSimpleName();
	public static final String METHOD_ANNOUNCE = "ANNOUNCE";
	public static final String METHOD_SETUP = "SETUP";
	public static final String METHOD_RECORD = "RECORD";
	public static final String METHOD_PAUSE = "PAUSE";
	public static final String METHOD_TEARDOWN = "TEARDOWN";
	public static final String HEADER_CSEQ = "CSeq";
	public static final String HEADER_SESSION = "Session";
	public static final String HEADER_USER_AGENT = "User-Agent";
	public static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
	public static final String HEADER_TRANSPORT = "Transport";
	public static final String ACCEPT_LANGUAGE_EN_US = "en-US";
	public String _method;
	public String _uri;
	private HashMap<String,String> _headers = new HashMap<String,String>();
	public String content;
	private static int _cseq = 1;

	public RtspRequest(String method, String uri)
	{
		_method = method;
		_uri = uri;
		_headers.put(HEADER_CSEQ, Integer.toString(_cseq++));
	}
	
	public void setUserAgent(String userAgent)
	{
		_headers.put(HEADER_USER_AGENT, userAgent);
	}
	
	public void setSession(String id)
	{
		_headers.put(HEADER_SESSION, id);
	}
	
	public void setTransport(String value)
	{
		_headers.put(HEADER_TRANSPORT, value);
	}
	
	public void setAcceptLanguage(String value)
	{
		_headers.put(HEADER_ACCEPT_LANGUAGE, value);
	}

	public void send(OutputStream stream) throws IOException
	{
		sendCommand(stream);
		Set<String> keys = _headers.keySet();
		for (String key : keys)
		{
			sendHeader(stream, key, _headers.get(key));
		}
		if (content != null)
		{
			sendHeader(stream, "Content-Length", Integer.toString(content.length()));
		}
		sendLine(stream, null);
		if (content != null)
		{
			sendContent(stream);
		}
	}
	
	private void sendLine(OutputStream stream, String s) throws IOException
	{
		if (s != null && s.length() > 0)
		{
			stream.write(s.getBytes());
		}
		stream.write("\r\n".getBytes());
	}
	
	private void sendCommand(OutputStream stream) throws IOException
	{
		String line = String.format("%s %s RTSP/1.0", _method, _uri); 
		Log.d(TAG, "Send RTSP command: " + line);
		sendLine(stream, line);
	}
	
	private void sendHeader(OutputStream stream, String key, String value) throws IOException
	{
		String line = String.format("%s:%s", key, value);
		Log.d(TAG, "Send RTSP header: " + line);
		sendLine(stream, line);
	}
	
	private void sendContent(OutputStream stream) throws IOException
	{
		Log.d(TAG, "Send RTSP content: " + content);
		stream.write(content.getBytes());
	}
}
