package com.futureconcepts.ax.broadcaster.ui;

import com.futureconcepts.ax.broadcaster.AxBroadcasterApplication;
import com.futureconcepts.ax.broadcaster.R;
import com.futureconcepts.ax.broadcaster.api.CustomRtspClient;
import com.futureconcepts.ax.broadcaster.rtsp.RtspClient;

import net.majorkernelpanic.streaming.SessionBuilder;
import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

public class MainActivity extends Activity
{
	static final public String TAG = MainActivity.class.getSimpleName();

	static public final String WAKELOCK_NAME = "com.futureconcepts.ax.broadcaster.wakelock";
	
	private static final int STATE_IDLE = 1;
	private static final int STATE_BROADCASTING = 2;
	private static final int STATE_RECORDING =3;
	
	private PowerManager.WakeLock mWakeLock;
	private SurfaceView mSurfaceView;
	private View mOpenInstructionsView;
	private SurfaceHolder mSurfaceHolder;
	private AxBroadcasterApplication mApplication;
	private RtspClient mRtspClient;
	private SharedPreferences _settings;
	private int _state = STATE_IDLE;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mApplication = (AxBroadcasterApplication) getApplication();

		setContentView(R.layout.main);
		
		_settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mSurfaceView = (SurfaceView)findViewById(R.id.handset_camera_view);
		mOpenInstructionsView = findViewById(R.id.open_instructions_view);
		mSurfaceHolder = mSurfaceView.getHolder();
		// We still need this line for backward compatibility reasons with android 2
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		SessionBuilder.getInstance().setSurfaceHolder(mSurfaceHolder);

		// Prevents the phone to go to sleep mode
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_NAME);

//		this.startService(new Intent(this, CustomRtspClient.class));
		mSurfaceView.setEnabled(false);
		mOpenInstructionsView.setVisibility(View.INVISIBLE);
//		mOpenInstructionsView.setOnTouchListener(new OnSwipeTouchListener()
//		{
//			@Override
//			public void onSwipeRight()
//			{
//				startBroadcasting();
//				mOpenInstructionsView.setVisibility(View.INVISIBLE);
//			}
//		});
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	public void onStart()
	{
		super.onStart();
//		mOpenInstructionsView.setVisibility(View.VISIBLE);
		mSurfaceView.setEnabled(false);
	}

	@Override
	public void onStop()
	{
		super.onStop();
	}
	
	@Override
	public void onDestroy()
	{
		if (_state == STATE_BROADCASTING)
		{
			setState(STATE_IDLE);
		}
		super.onDestroy();
	}
	
	private void setState(int newState)
	{
		Log.d(TAG, String.format("Changing state from %d to %d", _state, newState));
		if (_state == STATE_IDLE)
		{
			if (newState == STATE_BROADCASTING)
			{
				startBroadcasting();
				mOpenInstructionsView.setVisibility(View.INVISIBLE);
				_state = newState;
				invalidateOptionsMenu();
			}
			else
			{
				assert(false);
			}
		}
		else if (_state == STATE_BROADCASTING)
		{
			if (newState == STATE_IDLE)
			{
				mOpenInstructionsView.setVisibility(View.INVISIBLE);
				mSurfaceView.setEnabled(false);
				stopBroadcasting();
				_state = newState;
				invalidateOptionsMenu();
			}
			else if (newState == STATE_RECORDING)
			{
				_state = newState;
				invalidateOptionsMenu();
			}
			else
			{
				assert(false);
			}
		}
		else if (_state == STATE_RECORDING)
		{
		}
		else
		{
			Log.d(TAG, "invalid state transation");
		}
	}
	
	private void startBroadcasting()
	{
		// Lock screen
		mWakeLock.acquire();

		// Did the user disabled the notification ?
		if (mApplication.notificationEnabled)
		{
			Intent notificationIntent = new Intent(this, MainActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

			Notification.Builder builder = new Notification.Builder(this);
			Notification notification = builder.setContentIntent(pendingIntent)
					.setWhen(System.currentTimeMillis())
					.setTicker(getText(R.string.notification_title))
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle(getText(R.string.notification_title))
					.setContentText(getText(R.string.notification_content)).build();
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			String streamingSound = _settings.getString(OptionsActivity.KEY_STREAMING_SOUND, null);
			if (streamingSound != null)
			{
				Uri streamingSoundUri = Uri.parse(streamingSound);
				if (streamingSoundUri != null)
				{
					notification.sound = streamingSoundUri;
				}
			}
			((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).notify(0,notification);
		}
		else
		{
			removeNotification();
		}
		bindService(new Intent(this,CustomRtspClient.class), mRtspServiceConnection, Context.BIND_AUTO_CREATE);
	}
	
	private void stopBroadcasting()
	{
		// A WakeLock should only be released when isHeld() is true !
		if (mWakeLock.isHeld())
		{
			mWakeLock.release();
		}
		if (mRtspClient != null)
		{
			mRtspClient.removeCallbackListener(mRtspCallbackListener);
		}
		unbindService(mRtspServiceConnection);
		removeNotification();
//		stopService(new Intent(this, CustomRtspClient.class));
	}

	@Override
	public void onResume()
	{
		super.onResume();
		mApplication.applicationForeground = true;
	}

	@Override
	public void onPause()
	{
		super.onPause();
		mApplication.applicationForeground = false;
	}

	@Override    
	public void onBackPressed()
	{
		Intent setIntent = new Intent(Intent.ACTION_MAIN);
		setIntent.addCategory(Intent.CATEGORY_HOME);
		setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(setIntent);
	}

	@Override    
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		if (_state == STATE_IDLE)
		{
			MenuItem toggleBroadcasting = menu.findItem(R.id.toggle_broadcasting);
			if (true)
			{
				toggleBroadcasting.setVisible(true);
				toggleBroadcasting.setIcon(R.drawable.ic_action_video);
			}
			menu.findItem(R.id.toggle_screen).setVisible(false);
			menu.findItem(R.id.options).setVisible(true);
		}
		else if (_state == STATE_BROADCASTING)
		{
			MenuItem toggleBroadcasting = menu.findItem(R.id.toggle_broadcasting);
			if (true)
			{
				toggleBroadcasting.setVisible(true);
				toggleBroadcasting.setIcon(R.drawable.ic_action_video_selected);
			}
			MenuItem toggleScreen =	menu.findItem(R.id.toggle_screen);
			toggleScreen.setVisible(true);
			toggleScreen.setIcon(isKeepScreenOn() ? R.drawable.ic_action_screenon : R.drawable.ic_action_screenoff);
			menu.findItem(R.id.options).setVisible(false);
		}
		else if (_state == STATE_RECORDING)
		{
			menu.findItem(R.id.toggle_broadcasting).setVisible(false);
			menu.findItem(R.id.toggle_screen).setVisible(true);
			menu.findItem(R.id.options).setVisible(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override    
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Intent intent;

		switch (item.getItemId())
		{
		case R.id.toggle_broadcasting:
			if (_state == STATE_IDLE)
			{
				setState(STATE_BROADCASTING);
			}
			else
			{
				setState(STATE_IDLE);
			}
			return true;
		case R.id.toggle_screen:
			if (isKeepScreenOn())
			{
				clearKeepScreenOn();
				invalidateOptionsMenu();
			}
			else
			{
				setKeepScreenOn();
				invalidateOptionsMenu();
			}
			return true;
		case R.id.options:
			// Starts QualityListActivity where user can change the streaming quality
			intent = new Intent(this.getBaseContext(),OptionsActivity.class);
			startActivityForResult(intent, 0);
			return true;
		case R.id.quit:
			quit();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private boolean isKeepScreenOn()
	{
		return (getWindow().getAttributes().flags & LayoutParams.FLAG_KEEP_SCREEN_ON) != 0;
	}
	
	private void setKeepScreenOn()
	{
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	private void clearKeepScreenOn()
	{
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	private void quit()
	{
		// Removes notification
		if (mApplication.notificationEnabled)
		{
			removeNotification();       
		}
		stopService(new Intent(this,CustomRtspClient.class));
		finish();
	}

	private SurfaceHolder.Callback mHolderCallback = new SurfaceHolder.Callback()
	{
		@Override
		public void surfaceDestroyed(SurfaceHolder holder)
		{
		}
		@Override
		public void surfaceCreated(SurfaceHolder holder)
		{
		}
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,int height)
		{
		}
	};
	
	private ServiceConnection mRtspServiceConnection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			mRtspClient = (CustomRtspClient) ((RtspClient.LocalBinder)service).getService();
			mRtspClient.addCallbackListener(mRtspCallbackListener);
			mRtspClient.start();
		}

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
		}
	};

	private RtspClient.CallbackListener mRtspCallbackListener = new RtspClient.CallbackListener()
	{
		@Override
		public void onError(RtspClient client, final Exception e, final String message)
		{
			runOnUiThread(new Runnable() {

				@Override
				public void run()
				{
					new AlertDialog.Builder(MainActivity.this)
					.setTitle("Error")
					.setMessage(String.format("%s: %s", message, e.getMessage()))
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog, final int id) {
							startActivityForResult(new Intent(MainActivity.this, OptionsActivity.class),0);
						}
					})
					.show();
				}
			});
		}

		@Override
		public void onMessage(RtspClient server, int message)
		{
			if (message == RtspClient.MESSAGE_STREAMING_STARTED)
			{
				log("Streaming started");
			}
			else if (message==RtspClient.MESSAGE_STREAMING_STOPPED)
			{
				log("Streaming stopped");
			}
		}
	};	

	private void removeNotification()
	{
		((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(0);
	}

	public void log(final String s)
	{
		this.runOnUiThread(new Runnable() {
			@Override
			public void run()
			{
				Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
			}
		});
	}
}