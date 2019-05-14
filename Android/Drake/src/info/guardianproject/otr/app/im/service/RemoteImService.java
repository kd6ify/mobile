package info.guardianproject.otr.app.im.service;

import com.futureconcepts.drake.R;
import com.futureconcepts.drake.client.DrakeIntent;
import com.futureconcepts.drake.client.IImConnection;
import com.futureconcepts.drake.client.IOtrKeyManager;
import com.futureconcepts.drake.client.IRemoteImService;
import com.futureconcepts.drake.client.constants.ImServiceConstants;
import com.futureconcepts.drake.config.IXMPPConfig;
import com.futureconcepts.drake.config.XMPPConfigFactory;

import info.guardianproject.otr.OtrChatManager;
import info.guardianproject.otr.OtrKeyManagerAdapter;
import info.guardianproject.otr.app.NetworkConnectivityListener;
import info.guardianproject.otr.app.NetworkConnectivityListener.State;
import info.guardianproject.otr.app.im.engine.ConnectionFactory;
import info.guardianproject.otr.app.im.engine.ImConnection;
import info.guardianproject.otr.app.im.engine.ImException;
import info.guardianproject.otr.app.im.engine.HeartbeatService.Callback;

import java.util.ArrayList;
import java.util.List;

import net.java.otr4j.OtrEngineListener;
import net.java.otr4j.OtrPolicy;
import net.java.otr4j.session.SessionID;
import net.java.otr4j.session.SessionStatus;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class RemoteImService extends Service implements OtrEngineListener
{
    private static final int EVENT_SHOW_TOAST = 100;
    private static final int EVENT_NETWORK_STATE_CHANGED = 200;
	private static final long HEARTBEAT_INTERVAL = 1000 * 60;

    private StatusBarNotifier mStatusBarNotifier;
    private Handler mServiceHandler;
    NetworkConnectivityListener mNetworkConnectivityListener;
    private int mNetworkType;
    private boolean mNeedCheckAutoLogin;

    private boolean mBackgroundDataEnabled;

    private SettingsMonitor mSettingsMonitor;
    private OtrChatManager mOtrChatManager;
    
    ImConnectionServiceImpl mConnection;
    
	public RemoteImService()
	{
	}
	
	private static final String TAG = RemoteImService.class.getSimpleName();

	public static void debug (String msg)
	{
		Log.d(TAG, msg);
	}
	
	public static void debug (String msg, Exception e)
	{
		Log.e(TAG, msg, e);
	}
	
	private synchronized void initOtr()
	{
    	int otrPolicy = convertPolicy();
		
		if (mOtrChatManager == null)
		{
	        
	        try
	        {
		     // TODO OTRCHAT add support for more than one connection type (this is a kludge)
		        mOtrChatManager = OtrChatManager.getInstance(otrPolicy, this);
		        mOtrChatManager.addOtrEngineListener(this);
	        }
	        catch (Exception e)
	        {
	        	debug( "can't get otr manager",e);
	        }
		} else {
			mOtrChatManager.setPolicy(otrPolicy);
		}
    }

	private int convertPolicy() {
		int otrPolicy = OtrPolicy.OPPORTUNISTIC;	        	
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
		
		String otrModeSelect = prefs.getString("pref_security_otr_mode", "auto");
		
		if (otrModeSelect.equals("auto"))
		{
			otrPolicy = OtrPolicy.OPPORTUNISTIC;
		}
		else if (otrModeSelect.equals("disabled"))
		{
			otrPolicy = OtrPolicy.NEVER;

		}
		else if (otrModeSelect.equals("force"))
		{
			otrPolicy = OtrPolicy.OTRL_POLICY_ALWAYS;

		}
		else if (otrModeSelect.equals("requested"))
		{
			otrPolicy = OtrPolicy.OTRL_POLICY_MANUAL;
		}
		return otrPolicy;
	}

    @Override
    public void onCreate()
    {
//    	android.os.Debug.waitForDebugger(); -- bad bad for release builds!
        debug( "ImService started");
        mStatusBarNotifier = new StatusBarNotifier(this);
        mServiceHandler = new ServiceHandler();
        mNetworkConnectivityListener = new NetworkConnectivityListener();
        mNetworkConnectivityListener.registerHandler(mServiceHandler, EVENT_NETWORK_STATE_CHANGED);
        mNetworkConnectivityListener.startListening(this);

        mSettingsMonitor = new SettingsMonitor();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED);
        registerReceiver(mSettingsMonitor, intentFilter);

        ConnectivityManager manager
            = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        setBackgroundData(manager.getBackgroundDataSetting());

        AndroidSystemService.getInstance().initialize(this);
        AndroidSystemService.getInstance().getHeartbeatService().startHeartbeat(new HeartbeatHandler(), HEARTBEAT_INTERVAL);
        
        // Have the heartbeat start autoLogin, unless onStart turns this off
        mNeedCheckAutoLogin = true;
    }
    
    class HeartbeatHandler implements Callback
    {
		@SuppressWarnings("finally")
		@Override
		public long sendHeartbeat()
		{
			try
			{
				if (mNeedCheckAutoLogin && mNetworkConnectivityListener.getState() != State.NOT_CONNECTED) {
					debug( "autoLogin from heartbeat");
					autoLogin();
				}
				if (mConnection != null)
				{
					mConnection.sendHeartbeat();
				}
			}
			finally
			{
				return HEARTBEAT_INTERVAL;
			}
		}
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        super.onStart(intent, startId);
        // NOTE: intent may be NULL!!  See java Service.onStart()
        if (mConnection != null)
        {
        	return;
        }
        if (intent != null)
        {
        	mNeedCheckAutoLogin = intent.getBooleanExtra(ImServiceConstants.EXTRA_CHECK_AUTO_LOGIN, false);
        }
        else
        {
        	mNeedCheckAutoLogin = true;
        }

        // Check and login accounts if network is ready, otherwise it's checked
        // when the network becomes available.
        if (mNeedCheckAutoLogin &&
                mNetworkConnectivityListener.getState() != State.NOT_CONNECTED) {
            mNeedCheckAutoLogin = false;
            autoLogin();
        }
    }

    private void autoLogin()
    {
        debug( "autoLogin");
        IXMPPConfig config = XMPPConfigFactory.get(this);
        IImConnection conn = createConnection();
        try
        {
            conn.login(config.getPassword(), true, true);
        }
        catch (RemoteException e)
        {
            Log.w(TAG, "Logging error while automatically login!");
        }
    }
    
    @Override
    public void onDestroy()
    {
        Log.w(TAG, "ImService stopped.");
        if (mConnection != null)
        {
            mConnection.logout();
            try
			{
				mConnection.destroy();
			}
			catch (RemoteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        AndroidSystemService.getInstance().shutdown();

        mNetworkConnectivityListener.unregisterHandler(mServiceHandler);
        mNetworkConnectivityListener.stopListening();
        mNetworkConnectivityListener = null;

        unregisterReceiver(mSettingsMonitor);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    public void showToast(CharSequence text, int duration)
    {
        Message msg = Message.obtain(mServiceHandler, EVENT_SHOW_TOAST, duration, 0, text);
        msg.sendToTarget();
    }

    public StatusBarNotifier getStatusBarNotifier()
    {
        return mStatusBarNotifier;
    }

	public OtrChatManager getOtrChatManager()
	{
		initOtr();
		
		return mOtrChatManager;
	}

    public void scheduleReconnect(long delay)
    {
        if (!isNetworkAvailable())
        {
            // Don't schedule reconnect if no network available. We will try to
            // reconnect when network state become CONNECTED.
            return;
        }
        mServiceHandler.postDelayed(new Runnable()
        {
            public void run()
            {
                reestablishConnection();
            }
        }, delay);
    }

    ImConnectionServiceImpl createConnection()
    {
        ConnectionFactory factory = ConnectionFactory.getInstance();
        try
        {
            ImConnection conn = factory.createConnection(this);
            ImConnectionServiceImpl imConnectionAdapter = new ImConnectionServiceImpl(conn, this);
            mConnection = imConnectionAdapter;
            initOtr();
            mOtrChatManager.setConnection(imConnectionAdapter);
            sendBroadcast(new Intent(DrakeIntent.EVENT_CONNECTION_CREATED));
            return imConnectionAdapter;
        }
        catch (ImException e)
        {
            debug( "Error creating connection", e);
            return null;
        }
    }

    private boolean isNetworkAvailable() {
        return mNetworkConnectivityListener.getState() == State.CONNECTED;
    }

    private boolean isBackgroundDataEnabled() {
        return mBackgroundDataEnabled;
    }

    private void setBackgroundData(boolean flag) {
        mBackgroundDataEnabled = flag;
    }

    void handleBackgroundDataSettingChange()
    {
        if (!isBackgroundDataEnabled())
        {
        	if (mConnection != null)
        	{
                mConnection.logout();
            }
        }
    }

    void networkStateChanged()
    {
        if (mNetworkConnectivityListener == null)
        {
            return;
        }
        NetworkInfo networkInfo = mNetworkConnectivityListener.getNetworkInfo();
        NetworkInfo.State state = networkInfo.getState();

        debug( "networkStateChanged:" + state);

        int oldType = mNetworkType;
        mNetworkType = networkInfo.getType();

        // Notify the connection that network type has changed. Note that this
        // only work for connected connections, we need to reestablish if it's
        // suspended.
        if (mNetworkType != oldType && isNetworkAvailable())
        {
        	if (mConnection != null)
        	{
                mConnection.networkTypeChanged();
            }
        }
        
        switch (state) {
            case CONNECTED:
                if (mNeedCheckAutoLogin)
                {
                    mNeedCheckAutoLogin = false;
                    autoLogin();
                    break;
                }
                reestablishConnection();
                break;

            case DISCONNECTED:
                if (!isNetworkAvailable())
                {
                    suspendConnection();
                }
                break;
        }
    }

    // package private for inner class access
    void reestablishConnection()
    {
        if (!isNetworkAvailable())
        {
            return;
        }
        if (mConnection != null)
        {
            int connState = mConnection.getState();
            if (connState == ImConnection.SUSPENDED)
            {
                mConnection.reestablishSession();
            }
        }
       // showToast(getString(R.string.error_reestablish_connection), Toast.LENGTH_LONG);
    }

    void closeConnection()
    {
    	mConnection = null;
    }
    
    private void suspendConnection()
    {
    	if (mConnection != null)
    	{
            if (mConnection.getState() == ImConnection.LOGGED_IN)
            {
                mConnection.suspend();
            }
        }
        
      //  showToast(getString(R.string.error_suspended_connection), Toast.LENGTH_LONG);
    }

    private final IRemoteImService.Stub mBinder = new IRemoteImService.Stub()
    {

        public IImConnection getConnection()
        {
            return IImConnection.Stub.asInterface(mConnection);
        }

        public void dismissNotifications()
        {
        	try
        	{
        		mStatusBarNotifier.dismissNotifications();
        	}
        	catch (Exception e)
        	{
        		e.printStackTrace();
        	}
        }

        public void dismissChatNotification(String username)
        {
            mStatusBarNotifier.dismissChatNotification(username);
        }

		@Override
		public IOtrKeyManager getOtrKeyManager() throws RemoteException
		{
			return new OtrKeyManagerAdapter(mOtrChatManager.getKeyManager(), null);
		}
    };

    private final class SettingsMonitor extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if (ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED.equals(action)) {
                ConnectivityManager manager =
                    (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                setBackgroundData(manager.getBackgroundDataSetting());
                handleBackgroundDataSettingChange();
            }
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler() {
        }

        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case EVENT_SHOW_TOAST:
                    Toast.makeText(RemoteImService.this,
                            (CharSequence) msg.obj, msg.arg1).show();
                    break;

                case EVENT_NETWORK_STATE_CHANGED:
                    networkStateChanged();
                    break;

                default:
            }
        }
    }

	@Override
	public void sessionStatusChanged(SessionID sessionID) {
		
		initOtr();
		
		SessionStatus sStatus = mOtrChatManager.getSessionStatus(sessionID);
		
		String msg = "";
		
		if (sStatus == SessionStatus.PLAINTEXT)
		{
			msg = getString(R.string.otr_session_status_plaintext);
			
		}
		else if (sStatus == SessionStatus.ENCRYPTED)
		{
			msg = getString(R.string.otr_session_status_encrypted);
			
		}
		else if (sStatus == SessionStatus.FINISHED)
		{
			msg = getString(R.string.otr_session_status_finished);
		}
		
		//showToast(msg, Toast.LENGTH_SHORT); // TODO ??
	}
	
	public static final class Receiver extends BroadcastReceiver
	{
		@Override
	    public void onReceive(Context context, Intent intent)
	    {
			String action = intent.getAction();
			if (action != null)
			{
				Log.d(TAG, "received " + action);
				if ( action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(DrakeIntent.ACTION_START_SERVICE))
				{
		            Intent serviceIntent = new Intent();
		            serviceIntent.setComponent(ImServiceConstants.IM_SERVICE_COMPONENT);
		            serviceIntent.putExtra(ImServiceConstants.EXTRA_CHECK_AUTO_LOGIN, true);
		            context.startService(serviceIntent);
				}
			}
	    }
	}
}
