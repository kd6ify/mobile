package com.futureconcepts.ax.broadcaster.rtsp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.futureconcepts.ax.broadcaster.AxBroadcasterApplication;

import net.majorkernelpanic.streaming.MediaStream;
import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.Camera.CameraInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Implementation of a subset of the RTSP protocol (RFC 2326).
 * 
 */
public class RtspClient extends Service
{
	public final static String TAG = RtspClient.class.getSimpleName();

	/** The agent name that will appear in RTSP requests. */
	public static String AGENT_NAME = "AntaresX Broadcaster";

	/** Port already in use. */
	public final static int ERROR_BIND_FAILED = 0x00;

	/** A stream could not be started. */
	public final static int ERROR_START_FAILED = 0x01;

	/** Streaming started. */
	public final static int MESSAGE_STREAMING_STARTED = 0X00;
	
	/** Streaming stopped. */
	public final static int MESSAGE_STREAMING_STOPPED = 0X01;
	
	protected SessionBuilder mSessionBuilder;
	protected SharedPreferences mSharedPreferences;
	private Session mSession;
	private String mSessionId;
	private int mCamera;
	private String mHostAddress;
	private int mHostPort;
	private String mStreamName;
	private Socket mClient;
	private OutputStream mOutput;
	private BufferedReader mInput;
	
	private final IBinder mBinder = new LocalBinder();
	private boolean mRestart = false;
	private final LinkedList<CallbackListener> mListeners = new LinkedList<CallbackListener>();

	private RtspState mState;
	private ThreadPoolExecutor _executor;
	
	/** Be careful: those callbacks won't necessarily be called from the ui thread ! */
	public interface CallbackListener
	{
		/** Called when an error occurs. */
		void onError(RtspClient server, Exception e, String error);

		/** Called when streaming starts/stops. */
		void onMessage(RtspClient server, int message);
	}

	/**
	 * See {@link RtspClient.CallbackListener} to check out what events will be fired once you set up a listener.
	 * @param listener The listener
	 */
	public void addCallbackListener(CallbackListener listener)
	{
		synchronized (mListeners)
		{
			mListeners.add(listener);			
		}
	}

	/**
	 * Removes the listener.
	 * @param listener The listener
	 */
	public void removeCallbackListener(CallbackListener listener)
	{
		synchronized (mListeners)
		{
			mListeners.remove(listener);				
		}
	}

	private String getDeviceId()
	{
		String value = null;
		if ("google_sdk".equals(Build.PRODUCT))
		{
//			value = "000000000000000";
			value = "A0000015D648A8";
		}
		else
		{
			TelephonyManager tmgr = (TelephonyManager)getSystemService(Service.TELEPHONY_SERVICE);
			if (tmgr != null)
			{
				value = tmgr.getDeviceId();
				if (value == null)
				{
					WifiManager wifiMan = (WifiManager)getSystemService(Context.WIFI_SERVICE);
					WifiInfo wifiInf = wifiMan.getConnectionInfo();
					value = wifiInf.getMacAddress().replace(":", "");
				}
			}
		}
		return value;
	}
	
	/** Starts (or restart if needed) the RTSP client. */
	public void start()
	{
		if (mRestart)
		{
			stop();
		}
		if (mState != null)
		{
			mState.setup();
			mRestart = false;
		}
	}

	public void stop()
	{
		if (mState != null)
		{
			mState.teardown();
		}
	}

	public boolean isStreaming()
	{
		return mSession.isStreaming();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		return START_STICKY;
	}

	@Override
	public void onCreate()
	{
		// Let's restore the state of the service 
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		// If the configuration is modified, the server will adjust
		mSharedPreferences.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);

		String cameraString = mSharedPreferences.getString("camera", "back");
		if (cameraString != null)
		{
			if (cameraString.equals("front"))
			{
				mCamera = CameraInfo.CAMERA_FACING_FRONT;
			}
			else if (cameraString.equals("back"))
			{
				mCamera = CameraInfo.CAMERA_FACING_BACK;
			}
		}
		String s = mSharedPreferences.getString("wowza_host_address", null);
		if (s != null)
		{
			String[] split = s.split(":");
			if (split != null && split.length == 2)
			{
				mHostAddress = split[0];
				mHostPort = Integer.parseInt(split[1]);
				mStreamName = "device"+getDeviceId();
				if (mStreamName != null)
				{
					_executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
					mState = new StateIdle();
				}
			}
		}
	}

	@Override
	public void onDestroy()
	{
		stop();
		mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
	}

	private OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener()
	{
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
		{
		}
	};

	/** The Binder you obtain when a connection with the Service is established. */
	public class LocalBinder extends Binder
	{
		public RtspClient getService()
		{
			return RtspClient.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return mBinder;
	}

	protected void postMessage(int id)
	{
		synchronized (mListeners)
		{
			if (mListeners.size() > 0)
			{
				for (CallbackListener cl : mListeners)
				{
					cl.onMessage(this, id);
				}
			}			
		}
	}	
	
	protected void postError(Exception exception, String message)
	{
		synchronized (mListeners)
		{
			if (mListeners.size() > 0)
			{
				for (CallbackListener cl : mListeners)
				{
					cl.onError(this, exception, message);
				}
			}			
		}
	}

	private boolean isAudioEnabled()
	{
		return mSharedPreferences.getBoolean("stream_audio", false);
	}
	
	public String getPublishUrl()
	{
		return String.format("rtsp://%s:%d/axbro/%s", mHostAddress, mHostPort, mStreamName);
	}
	
	public String getAudioTrackPublishUrl()
	{
		return String.format("rtsp://%s:%d/axbro/%s/trackid=0", mHostAddress, mHostPort, mStreamName);
	}
	public String getVideoTrackPublishUrl()
	{
		return String.format("rtsp://%s:%d/axbro/%s/trackid=1", mHostAddress, mHostPort, mStreamName);
	}
	
	private void changeStateAndSetup(RtspState state)
	{
		mState = state;
		mState.setup();
	}
	private void changeStateAndTeardown(RtspState state)
	{
		mState = state;
		mState.teardown();
	}
		
	private final class StateIdle implements RtspState
	{
		@Override
		public void setup()
		{
			_executor.execute(new Runnable() {
				@Override
				public void run()
				{
					try
					{
						mClient = new Socket(mHostAddress, mHostPort);
						mInput = new BufferedReader(new InputStreamReader(mClient.getInputStream()));
						mOutput = mClient.getOutputStream();
						SessionBuilder sessionBuilder = SessionBuilder.getInstance();
						sessionBuilder.setCamera(mCamera);
						sessionBuilder.setOrigin(mClient.getLocalAddress());
						sessionBuilder.setDestination(mClient.getInetAddress());
						sessionBuilder.setVideoEncoder(SessionBuilder.VIDEO_H264);
						sessionBuilder.setVideoQuality(AxBroadcasterApplication.getInstance().videoQuality);
						if (isAudioEnabled())
						{
							sessionBuilder.setAudioEncoder(SessionBuilder.AUDIO_AAC);
						}
						else
						{
							sessionBuilder.setAudioEncoder(SessionBuilder.AUDIO_NONE);
						}
						mSession = sessionBuilder.build();
						changeStateAndSetup(new StateAnnounce());
					}
					catch (Exception e)
					{
						e.printStackTrace();
						postError(e, String.format("error connecting to host %s:%d", mHostAddress, mHostPort));
						teardown();
					}
				}
			});
		}
		@Override
		public void teardown()
		{
			try
			{
				if (mSession != null)
				{
					mSession.stop();
					mSession.flush();
					mSession = null;
				}
				if (mClient != null)
				{
					mClient.close();
					mClient = null;
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	private final class StateAnnounce implements RtspState
	{
		@Override
		public void setup()
		{
			_executor.execute(new Runnable() {
				@Override
				public void run()
				{
					RtspRequest request = new RtspRequest(RtspRequest.METHOD_ANNOUNCE, getPublishUrl());
					request.setUserAgent(String.format("%s/%s", AGENT_NAME, getDeviceId()));
					try
					{
						request.content = mSession.getSessionDescription();
						request.send(mOutput);
						RtspResponse response = RtspResponse.parse(mInput);
						if (response.statusCode == 200)
						{
							changeStateAndSetup(new StateSetup());
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
						postError(e, "Error during RTSP ANNOUNCE");
						teardown();
					}
				}
			});
		}
		@Override
		public void teardown()
		{
			changeStateAndTeardown(new StateIdle());
		}
	}
	private final class StateSetup implements RtspState
	{
		@Override
		public void setup()
		{
			_executor.execute(new Runnable() {
				@Override
				public void run()
				{
					try
					{
						mSessionId = doSetupTrack(mSession.getVideoTrack(), getVideoTrackPublishUrl());
						if (isAudioEnabled())
						{
							mSessionId = doSetupTrack(mSession.getAudioTrack(), getAudioTrackPublishUrl());
						}
						changeStateAndSetup(new StateRecord());
					}
					catch (Exception e)
					{
						e.printStackTrace();
						postError(e, "error occurred while performing RTSP SETUP");
						teardown();
					}
				}
			});
		}
		@Override
		public void teardown()
		{
			mSessionId = null;
			changeStateAndTeardown(new StateIdle());
		}
		private String doSetupTrack(MediaStream mediaStream, String publishUrl) throws IOException
		{
			String result = null;
			int src[];
			RtspRequest request = new RtspRequest(RtspRequest.METHOD_SETUP, publishUrl);
			src = mediaStream.getLocalPorts();
			request.setTransport(String.format("RTP/AVP;unicast;client_port=%d-%d;mode=record", src[0], src[1]));
			request.setUserAgent(AGENT_NAME);
			request.setAcceptLanguage(RtspRequest.ACCEPT_LANGUAGE_EN_US);
			request.send(mOutput);
			RtspResponse response = RtspResponse.parse(mInput);
			if (response.statusCode == 200)
			{
				String transport = response.headers.get("transport");
				if (transport != null)
				{
					Pattern p = Pattern.compile("server_port=(\\d+)-(\\d+)",Pattern.CASE_INSENSITIVE);
					Matcher m = p.matcher(transport);
					if (!m.find())
					{
						Log.d(TAG, "TODO");
					}
					int p1 = Integer.parseInt(m.group(1));
					int p2 = Integer.parseInt(m.group(2));
					mediaStream.setDestinationPorts(p1, p2);
				}
				String sessionId = response.headers.get("session");
				if (sessionId != null)
				{
					Pattern p = Pattern.compile("(\\s)(\\w+);timeout=(\\d+)",Pattern.CASE_INSENSITIVE);
					Matcher m = p.matcher(sessionId);
					if (!m.find())
					{
						Log.d(TAG, "TODO");
					}
					result = m.group(2);
				}
			}
			return result;
		}
	}
	private final class StateRecord implements RtspState
	{
		@Override
		public void setup()
		{
			_executor.execute(new Runnable() {
				@Override
				public void run()
				{
					try
					{
						doRecord();
						changeStateAndSetup(new StateStreaming());
					}
					catch (Exception e)
					{
						e.printStackTrace();
						postError(e, "Error occured during RTSP RECORD");
						teardown();
					}
				}
			});
		}
		private void doRecord() throws IOException
		{
			RtspRequest request = new RtspRequest(RtspRequest.METHOD_RECORD, getPublishUrl());
			request.setSession(mSessionId);
			request.setUserAgent(AGENT_NAME);
			request.send(mOutput);
			RtspResponse response = RtspResponse.parse(mInput);
			if (response.statusCode == 200)
			{
				Log.d(TAG, "doRecordTrack SUCCESS");
			}
		}
		@Override
		public void teardown()
		{
			changeStateAndTeardown(new StateIdle());
		}
	}
	private final class StateStreaming implements RtspState
	{
		@Override
		public void setup()
		{
			try
			{
				mSession.start();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				postError(e, "Error occurred while starting stream");
				teardown();
			}
			postMessage(MESSAGE_STREAMING_STARTED);
		}
		@Override
		public void teardown()
		{
			_executor.execute(new Runnable() {
				@Override
				public void run()
				{
					try
					{
						doPause();
						mSession.stop();
						postMessage(MESSAGE_STREAMING_STOPPED);
						changeStateAndTeardown(new StatePaused());
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
		}
		private void doPause() throws IOException
		{
			RtspRequest request = new RtspRequest(RtspRequest.METHOD_PAUSE, getPublishUrl());
			request.setSession(mSessionId);
			request.setUserAgent(AGENT_NAME);
			request.send(mOutput);
			RtspResponse response = RtspResponse.parse(mInput);
			if (response.statusCode == 200)
			{
				Log.d(TAG, "doPauseTrack SUCCESS");
			}
		}
	}
	private final class StatePaused implements RtspState
	{
		@Override
		public void setup()
		{
		}
		@Override
		public void teardown()
		{
			_executor.execute (new Runnable() {
				@Override
				public void run()
				{
					try
					{
						doTeardown();
						mSession.flush();
						mClient.close();
						changeStateAndTeardown(new StateIdle());
					}
					catch (Exception e)
					{
						changeStateAndTeardown(new StateIdle());
					}
				}
			});
		}
		private void doTeardown() throws IOException
		{
			RtspRequest request = new RtspRequest(RtspRequest.METHOD_TEARDOWN, getPublishUrl());
			request.setSession(mSessionId);
			request.setUserAgent(AGENT_NAME);
			request.send(mOutput);
			RtspResponse response = RtspResponse.parse(mInput);
			if (response.statusCode == 200)
			{
				Log.d(TAG, "doTeardownTrack SUCCESS");
			}
		}
	}
}
