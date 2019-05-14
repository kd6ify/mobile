package info.guardianproject.otr.app.im.service;

import com.futureconcepts.drake.client.Contact;
import com.futureconcepts.drake.client.DrakeIntent;
import com.futureconcepts.drake.client.IChatGroupManager;
import com.futureconcepts.drake.client.IChatSessionManager;
import com.futureconcepts.drake.client.IContactListManager;
import com.futureconcepts.drake.client.IFileTransferManager;
import com.futureconcepts.drake.client.IImConnection;
import com.futureconcepts.drake.client.ImErrorInfo;
import com.futureconcepts.drake.client.Imps;
import com.futureconcepts.drake.client.Presence;

import info.guardianproject.otr.app.im.engine.ConnectionListener;
import info.guardianproject.otr.app.im.engine.ContactListManager;
import info.guardianproject.otr.app.im.engine.ImConnection;
import info.guardianproject.otr.app.im.engine.ImException;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Parcelable;
import android.os.RemoteException;

public class ImConnectionServiceImpl extends IImConnection.Stub
{

    private static final String[] SESSION_COOKIE_PROJECTION = {
        Imps.SessionCookies.NAME,
        Imps.SessionCookies.VALUE,
    };

    private static final int COLUMN_SESSION_COOKIE_NAME = 0;
    private static final int COLUMN_SESSION_COOKIE_VALUE = 1;

    private ImConnection mConnection;
    private ConnectionListenerAdapter mConnectionListener;

    private FileTransferManagerServiceImpl mFileTransferManager;
    private ChatSessionManagerServiceImpl mChatSessionManager;
    private ChatGroupManagerServiceImpl mChatGroupManager;
    private ContactListManagerServiceImpl mContactListManager;

    private RemoteImService mService;

    private boolean mAutoLoadContacts;
    private int mConnectionState = ImConnection.DISCONNECTED;
    
    private StatusBarNotifier mStatusBarNotifier;

    public ImConnectionServiceImpl(ImConnection connection, RemoteImService service)
    {
        mConnection = connection;
        mService = service;
        mConnectionListener = new ConnectionListenerAdapter();
        mConnection.addConnectionListener(mConnectionListener);
        mStatusBarNotifier = new StatusBarNotifier(service);
    }

	@Override
	public void destroy() throws RemoteException
	{
		if (mConnection != null)
		{
			mConnection.close();
		}
	}
    
    public ImConnection getAdaptee()
    {
        return mConnection;
    }

    public RemoteImService getContext()
    {
        return mService;
    }

    public int[] getSupportedPresenceStatus()
    {
        return mConnection.getSupportedPresenceStatus();
    }

    public void networkTypeChanged()
    {
        mConnection.networkTypeChanged();
    }

    void reestablishSession()
    {
        mConnectionState = ImConnection.LOGGING_IN;

        ContentResolver cr = mService.getContentResolver();
        if ((mConnection.getCapability() & ImConnection.CAPABILITY_SESSION_REESTABLISHMENT) != 0)
        {
            Map<String, String> cookie = querySessionCookie(cr);
            if (cookie != null)
            {
                RemoteImService.debug("re-establish session");
                try
                {
                    mConnection.reestablishSessionAsync(cookie);
                }
                catch (IllegalArgumentException e)
                {
                	RemoteImService.debug( "Invalid session cookie, probably modified by others.");
                    clearSessionCookie(cr);
                }
            }
        }
    }

    @Override
    public void login(String passwordTemp, boolean autoLoadContacts, boolean retry)
    {
        mAutoLoadContacts = autoLoadContacts;
        mConnectionState = ImConnection.LOGGING_IN;
        mChatSessionManager = new ChatSessionManagerServiceImpl(this);
        mContactListManager = new ContactListManagerServiceImpl(this);
        if ((mConnection.getCapability() & ImConnection.CAPABILITY_GROUP_CHAT) != 0)
        {
            mChatGroupManager = new ChatGroupManagerServiceImpl(this, mService);
        }
        mConnection.loginAsync(passwordTemp, retry);
        mFileTransferManager = new FileTransferManagerServiceImpl(this);
    }
    
    @Override
    public void sendHeartbeat() throws RemoteException
    {
    	mConnection.sendHeartbeat();
    }
    
    @Override
    public void setProxy (String type, String host, int port) throws RemoteException
    {
    	mConnection.setProxy(type, host, port);
    }

    private HashMap<String, String> querySessionCookie(ContentResolver cr)
    {
        Cursor c = cr.query(Imps.SessionCookies.CONTENT_URI, SESSION_COOKIE_PROJECTION, null, null, null);
        if (c == null)
        {
            return null;
        }
        HashMap<String, String> cookie = null;
        if (c.getCount() > 0)
        {
            cookie = new HashMap<String, String>();
            while(c.moveToNext())
            {
                cookie.put(c.getString(COLUMN_SESSION_COOKIE_NAME), c.getString(COLUMN_SESSION_COOKIE_VALUE));
            }
        }
        c.close();
        return cookie;
    }

    public void logout()
    {
        mConnectionState = ImConnection.LOGGING_OUT;
        mConnection.logoutAsync();
    }

    public synchronized void cancelLogin()
    {
        if (mConnectionState >= ImConnection.LOGGED_IN)
        {
            // too late
            return;
        }
        mConnectionState = ImConnection.LOGGING_OUT;
        mConnection.logout();
    }

    void suspend()
    {
        mConnectionState = ImConnection.SUSPENDING;
        mConnection.suspend();
    }

	@Override
	public IChatGroupManager getChatGroupManager() throws RemoteException
	{
		return (IChatGroupManager)mChatGroupManager;
	}

	@Override
	public IFileTransferManager getFileTransferManager() throws RemoteException
	{
		return (IFileTransferManager)mFileTransferManager;
	}
	
	@Override
    public IChatSessionManager getChatSessionManager()
	{
        return (IChatSessionManager)mChatSessionManager;
    }

    public IContactListManager getContactListManager()
    {
        return (IContactListManager)mContactListManager;
    }

    public int getChatSessionCount()
    {
        if (mChatSessionManager == null)
        {
            return 0;
        }
        return mChatSessionManager.getChatSessionCount();
    }

    public Contact getLoginUser()
    {
        return mConnection.getLoginUser();
    }

    public Presence getUserPresence()
    {
        return mConnection.getUserPresence();
    }

    public int updateUserPresence(Presence newPresence)
    {
        try
        {
            mConnection.updateUserPresenceAsync(newPresence);
        }
        catch (ImException e)
        {
            return e.getImError().getCode();
        }
        return ImErrorInfo.NO_ERROR;
    }

    public int getState()
    {
        return mConnectionState;
    }

    @Override
    public void publishLocation(Location location)
    {
    	try
    	{
    		mConnection.publishLocationAsync(location);
    	}
    	catch (Exception e)
    	{
    		
    	}
    }
    
    void saveSessionCookie(ContentResolver cr) {
        Map<String, String> cookies = mConnection.getSessionContext();

        int i = 0;
        ContentValues[] valuesList = new ContentValues[cookies.size()];

        for(Map.Entry<String,String> entry : cookies.entrySet()){
            ContentValues values = new ContentValues(2);

            values.put(Imps.SessionCookies.NAME, entry.getKey());
            values.put(Imps.SessionCookies.VALUE, entry.getValue());

            valuesList[i++] = values;
        }

        cr.bulkInsert(Imps.SessionCookies.CONTENT_URI, valuesList);
    }

    void clearSessionCookie(ContentResolver cr) {
        cr.delete(Imps.SessionCookies.CONTENT_URI, null, null);
    }

    final class ConnectionListenerAdapter implements ConnectionListener
    {
        public void onStateChanged(final int state, final ImErrorInfo error)
        {
        	if (error != null)
        	{
        		mStatusBarNotifier.notifyError(error);
        	}
        	Intent broadcastEvent = new Intent();
        	if (state == ImConnection.LOGGED_IN)
        	{
        		broadcastEvent.setAction(DrakeIntent.EVENT_CONNECTION_LOGGED_IN);
        	}
        	else if (state == ImConnection.DISCONNECTED)
        	{
        		broadcastEvent.setAction(DrakeIntent.EVENT_CONNECTION_DISCONNECTED);
        	}
        	else if (state == ImConnection.LOGGING_OUT)
        	{
        		broadcastEvent.setAction(DrakeIntent.EVENT_CONNECTION_LOGGING_OUT);
        	}
        	else if (state == ImConnection.LOGGING_IN)
        	{
        		broadcastEvent.setAction(DrakeIntent.EVENT_CONNECTION_LOGGING_IN);
        	}
        	else if (state == ImConnection.SUSPENDING)
        	{
        		broadcastEvent.setAction(DrakeIntent.EVENT_CONNECTION_SUSPENDING);
        	}
        	else if (state == ImConnection.SUSPENDED)
        	{
        		broadcastEvent.setAction(DrakeIntent.EVENT_CONNECTION_SUSPENDED);
        	}
        	mService.sendBroadcast(broadcastEvent);
            synchronized (this)
            {
                if (state == ImConnection.LOGGED_IN && mConnectionState == ImConnection.LOGGING_OUT)
                {
                	// A bit tricky here. The engine did login successfully
                    // but the notification comes a bit late; user has already
                    // issued a cancelLogin() and that cannot be undone. Here
                    // we have to ignore the LOGGED_IN event and wait for
                    // the upcoming DISCONNECTED.
                    return;
                }
                if (state != ImConnection.DISCONNECTED)
                {
                    mConnectionState = state;
                }
            }
            ContentResolver cr = mService.getContentResolver();
            if(state == ImConnection.LOGGED_IN)
            {
                if ((mConnection.getCapability() & ImConnection.CAPABILITY_SESSION_REESTABLISHMENT) != 0)
                {
                    saveSessionCookie(cr);
                }
                if(mAutoLoadContacts && mContactListManager.getState() != ContactListManager.LISTS_LOADED)
                {
                    mContactListManager.loadContactLists();
                }
                for (ChatSessionServiceImplBase session : mChatSessionManager.mActiveChatSessionAdapters.values())
                {
                    session.sendPostponedMessages();
                }
                mService.getStatusBarNotifier().notifyConnected();
            }
            else if (state == ImConnection.LOGGING_OUT)
            {
                // The engine has started to logout the connection, remove it
                // from the active connection list.
                mService.closeConnection();
            }
            else if(state == ImConnection.DISCONNECTED)
            {
                mService.closeConnection();
                clearSessionCookie(cr);
                // mContactListManager might still be null if we fail
                // immediately in loginAsync (say, an invalid host URL)
                if (mContactListManager != null)
                {
                    mContactListManager.clearOnLogout();
                }
                if (mChatSessionManager != null)
                {
                    mChatSessionManager.closeAllChatSessions();
                }
                mService.getStatusBarNotifier().notifyDisconnected();
                mConnectionState = state;
            }
            else if (state == ImConnection.SUSPENDED && error != null)
            {
                // re-establish failed, schedule to retry
                mService.scheduleReconnect(5000);
            }
        }

        public void onUserPresenceUpdated()
        {
        	mService.sendBroadcast(new Intent(DrakeIntent.EVENT_USER_PRESENCE_UPDATED));
        }

        public void onUpdatePresenceError(final ImErrorInfo error)
        {
        	Intent intent = new Intent(DrakeIntent.EVENT_UPDATE_USER_PRESENCE_ERROR);
        	intent.putExtra("Error", (Parcelable)error);
        }
    }
}
