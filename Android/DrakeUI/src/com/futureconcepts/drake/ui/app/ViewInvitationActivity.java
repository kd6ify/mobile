package com.futureconcepts.drake.ui.app;

import com.futureconcepts.drake.client.DrakeIntent;
import com.futureconcepts.drake.client.IChatGroupListener;
import com.futureconcepts.drake.client.IChatGroupManager;
import com.futureconcepts.drake.client.ImErrorInfo;
import com.futureconcepts.drake.client.Imps;
import com.futureconcepts.drake.client.MessengerServiceConnection;
import com.futureconcepts.drake.client.model.ContactCursor;
import com.futureconcepts.drake.ui.R;
import com.futureconcepts.drake.ui.os.SimpleAlertHandler;
import com.futureconcepts.drake.ui.widget.InvitationView;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

public class ViewInvitationActivity extends Activity implements MessengerServiceConnection.Client
{
	private static final String TAG = ViewInvitationActivity.class.getSimpleName();
	
    private MessengerServiceConnection _serviceConnection;

    private InvitationView mInvitationView;

    private SimpleAlertHandler mHandler;
    
    private IChatGroupManager mChatGroupManager;
    private MyChatGroupListener mChatGroupListener;
    
    private InvitationView.Listener mInvitationViewListener;
    
    @Override
    protected void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.invitation_view);
        
        mInvitationView = (InvitationView)findViewById(R.id.invitationView);

        mHandler = new SimpleAlertHandler(this);

        mChatGroupListener = new MyChatGroupListener();
        
        _serviceConnection = new MessengerServiceConnection(this, this);
    	_serviceConnection.connect();
    }

	@Override
	public void onMessengerServiceConnected()
	{
		try
		{
			mChatGroupManager = _serviceConnection.getConnection().getChatGroupManager();
			resolveIntent(getIntent());
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
			mHandler.showAlert("Encountered problem", e.getMessage());
		}
	}

	@Override
	public void onMessengerServiceDisconnected()
	{
		// TODO Auto-generated method stub
		
	}

    @Override
    protected void onResume()
    {
        super.onResume();
        if (mChatGroupManager != null)
        {
        	try
			{
				mChatGroupManager.registerChatGroupListener(mChatGroupListener);
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
			}
        }
    }

    @Override
    protected void onPause()
    {
        if (mChatGroupManager != null)
        {
        	if (mChatGroupListener != null)
        	{
	        	try
				{
					mChatGroupManager.unregisterChatGroupListener(mChatGroupListener);
				}
				catch (RemoteException e)
				{
					e.printStackTrace();
				}
        	}
        }
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        resolveIntent(intent);
    }

    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	if (_serviceConnection != null)
    	{
    		_serviceConnection.disconnect();
    	}
    }
    
    private void resolveIntent(Intent intent)
    {
        mInvitationView.bindInvitation(ContentUris.parseId(intent.getData()));
        mInvitationViewListener = new MyInvitationViewListener();
        mInvitationView.setListener(mInvitationViewListener);
        try
		{
			mChatGroupManager.registerChatGroupListener(mChatGroupListener);
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
			mHandler.showAlert("Problem viewing invitation", e.getMessage());
		}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.invitation_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void startChat()
    {
        try
        {
        	String selection = Imps.Contacts.USERNAME + "=?";
            String[] selectionArgs = { mInvitationView.getRoomJid() };
            ContactCursor contactCursor = new ContactCursor(getContentResolver().query(Imps.Contacts.CONTENT_URI, null, selection, selectionArgs, null));
            startManagingCursor(contactCursor);
            if (contactCursor.moveToFirst())
            {
	            Uri data = ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, contactCursor.get_ID());
	            Intent i = new Intent(Intent.ACTION_VIEW, data);
	            i.addCategory(DrakeIntent.CATEGORY_IMPS);
	            startActivity(i);
            }
        }
        catch (Exception e)
        {
        	e.printStackTrace();
            mHandler.showServiceErrorAlert();
        }
    }
    
    private class MyChatGroupListener extends IChatGroupListener.Stub
    {
		@Override
		public void onChatGroupCreated(final String address) throws RemoteException
		{
			mHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					Toast.makeText(ViewInvitationActivity.this, "Created " + address, Toast.LENGTH_LONG).show();
				}
			});
		}

		@Override
		public void onChatGroupError(final String name, final ImErrorInfo error) throws RemoteException
		{
			mHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					mHandler.showAlert(name + " error", error.getDescription());
				}
			});
			Log.d(TAG, "onChatGroupError");
		}

		@Override
		public void onChatGroupJoined(final String address) throws RemoteException
		{
			if (mInvitationView.getRoomJid().equals(address))
			{
				mHandler.post(new Runnable()
				{
					@Override
					public void run()
					{
						startChat();
						finish();
					}
				});
			}
		}

		@Override
		public void onChatGroupLeft(String address) throws RemoteException
		{
		}
    }
    
    private class MyInvitationViewListener implements InvitationView.Listener
    {
		@Override
		public void onInvitationAccepted(long id, String roomJid)
		{
	        try
	        {
            	IChatGroupManager cgm = _serviceConnection.getConnection().getChatGroupManager();
                cgm.acceptInvitationAsync(id);
	        }
	        catch (RemoteException e)
	        {
	            mHandler.showServiceErrorAlert();
	        }
		}

		@Override
		public void onInvitationDeclined(long id, String roomJid)
		{
	        try
	        {
            	IChatGroupManager cgm = _serviceConnection.getConnection().getChatGroupManager();
                cgm.rejectInvitationAsync(id);
	        }
	        catch (RemoteException e)
	        {
	            mHandler.showServiceErrorAlert();
	        }
			finish();
		}
    }
}
