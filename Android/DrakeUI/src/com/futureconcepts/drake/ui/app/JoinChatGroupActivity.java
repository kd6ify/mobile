package com.futureconcepts.drake.ui.app;

import com.futureconcepts.drake.client.DrakeIntent;
import com.futureconcepts.drake.client.IChatGroup;
import com.futureconcepts.drake.client.IChatGroupListener;
import com.futureconcepts.drake.client.IChatGroupManager;
import com.futureconcepts.drake.client.IChatSession;
import com.futureconcepts.drake.client.IChatSessionManager;
import com.futureconcepts.drake.client.ImErrorInfo;
import com.futureconcepts.drake.client.Imps;
import com.futureconcepts.drake.client.MessengerServiceConnection;
import com.futureconcepts.drake.client.model.ContactCursor;
import com.futureconcepts.drake.ui.R;
import com.futureconcepts.drake.ui.os.SimpleAlertHandler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class JoinChatGroupActivity extends Activity implements MessengerServiceConnection.Client
{
	private static final String TAG = JoinChatGroupActivity.class.getSimpleName();

	private MessengerServiceConnection _serviceConnection;
    private SimpleAlertHandler _handler;
    private ContactCursor _contact;
    private IChatGroupManager _chatGroupManager;
    private MyChatGroupListener _chatGroupListener;
    private ProgressDialog _progressDialog;
    
    @Override
    protected void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);

        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        
        setContentView(R.layout.join_chat_group_view);
        _handler = new SimpleAlertHandler(this);
    	_serviceConnection = new MessengerServiceConnection(this, this);
    	_serviceConnection.connect();
    }

	@Override
	public void onMessengerServiceConnected()
	{
		try
		{
			_chatGroupManager = _serviceConnection.getConnection().getChatGroupManager();
	        _chatGroupListener = new MyChatGroupListener();
			_chatGroupManager.registerChatGroupListener(_chatGroupListener);
	        if (!resolveIntent())
	        {
	            finish();
	            return;
	        }
		}
		catch (RemoteException e)
		{
			// TODO Auto-generated catch block
			finish();
			e.printStackTrace();
			return;
		}
	}

	@Override
	public void onMessengerServiceDisconnected()
	{
		// TODO Auto-generated method stub
		
	}

    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	if (_chatGroupListener != null)
    	{
    		try
			{
				_chatGroupManager.unregisterChatGroupListener(_chatGroupListener);
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
			}
    	}
    	if (_serviceConnection != null)
    	{
    		_serviceConnection.disconnect();
    	}
    }
    
    private boolean resolveIntent()
    {
    	Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri != null)
        {
	    	ContentResolver resolver = getContentResolver();    	
	    	_contact = new ContactCursor(resolver.query(uri, null, null, null, null));
	    	startManagingCursor(_contact);
	    	if (_contact.moveToFirst())
	    	{
	    		String roomJid = _contact.getUsername();
	    		setTextView(R.id.room_jid, roomJid);
	    		tryJoining();
	    	}
    	}
    	return true;
    }

    private void setTextView(int resid, String text)
    {
    	TextView view = (TextView)findViewById(resid);
    	if (view != null)
    	{
    		view.setText(text);
    	}
    }

    private String getTextView(int resid)
    {
    	TextView view = (TextView)findViewById(resid);
    	if (view != null)
    	{
    		return view.getText().toString();
    	}
    	else
    	{
    		return null;
    	}
    }
    
    public void onBtnJoinClicked(View view)
    {
    	tryJoining();
    }

    private void tryJoining()
    {
    	String roomJid = getTextView(R.id.room_jid);
    	String password = getTextView(R.id.password);
		try
		{
			IChatGroup chatGroup = _chatGroupManager.getChatGroup(roomJid);
			if (password != null && password.length() > 0)
			{
				chatGroup.setPassword(password);
			}
			String roomName = _contact.getNickname();
			if (roomName == null || roomName.length() == 0)
			{
				roomName = "Chat Group";
			}
			showProgress("joining " + roomName);
			chatGroup.joinAsync();
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
			_handler.showAlert("Remote Exception", e.getMessage());
		}
    }
    
    private void startChat() throws RemoteException
    {
        long id = _contact.get_ID();
        IChatSessionManager manager = _serviceConnection.getConnection().getChatSessionManager();
        IChatSession session = manager.getChatSession(_contact.getUsername());
        if (session == null)
        {
    		session = manager.createChatSession(_contact.getUsername());
        }
        Uri data = ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, id);
        Intent i = new Intent(Intent.ACTION_VIEW, data);
        i.addCategory(DrakeIntent.CATEGORY_IMPS);
        startActivity(i);
    }

    private void showProgress(String text)
    {
    	_progressDialog = ProgressDialog.show(this, "join", text);
    }
    
    private void clearProgress()
    {
		if (_progressDialog != null)
		{
			_progressDialog.dismiss();
			_progressDialog = null;
		}
    }
    
    private class MyChatGroupListener extends IChatGroupListener.Stub
    {
		@Override
		public void onChatGroupCreated(String address) throws RemoteException
		{
			Log.d(TAG, "onChatGroupCreated " + address);
		}
	
		@Override
		public void onChatGroupError(String name, ImErrorInfo error) throws RemoteException
		{
			Log.d(TAG, "onChatGroupError");
			clearProgress();
			String description = error.getDescription();
			findViewById(R.id.btnJoin).setVisibility(View.VISIBLE);
			if (description != null && description.contains("not-authorized"))
			{
				findViewById(R.id.password).setVisibility(View.VISIBLE);
				StringBuilder friendlyDescription = new StringBuilder(name);
				friendlyDescription.append(" is a password-protected chat room.  ");
				if (description.contains("Incorrect Password"))
				{
					friendlyDescription.append("The password entered was not correct.  ");
				}
				else
				{
					friendlyDescription.append("A password is required to enter this room.  ");
				}
				friendlyDescription.append("Please enter a valid password or tap on the BACK button to join a different room");
				_handler.showAlert("Password-protected", friendlyDescription.toString());
			}
			else
			{
				_handler.showAlert("Chat Group Error", error.getDescription());
			}
		}
		
		@Override
		public void onChatGroupJoined(String address)
		{
			Log.d(TAG, "onChatGroupJoined " + address);
			clearProgress();
			String room_jid = getTextView(R.id.room_jid);
			if (address.equals(room_jid))
			{
				try
				{
					startChat();
					finish();
				}
				catch (RemoteException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onChatGroupLeft(String address)
		{
			Log.d(TAG, "onChatGroupLeft");
		}
    }
}
