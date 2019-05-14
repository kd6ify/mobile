package info.guardianproject.otr.app.im.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.futureconcepts.drake.client.IChatGroup;
import com.futureconcepts.drake.client.IChatGroupListener;
import com.futureconcepts.drake.client.IChatGroupManager;
import com.futureconcepts.drake.client.IInvitationListener;
import com.futureconcepts.drake.client.ImErrorInfo;
import com.futureconcepts.drake.client.Imps;
import com.futureconcepts.drake.client.Invitation;

import info.guardianproject.otr.app.im.engine.ChatGroup;
import info.guardianproject.otr.app.im.engine.ChatGroupManager;
import info.guardianproject.otr.app.im.engine.GroupListener;
import info.guardianproject.otr.app.im.engine.ImConnection;
import info.guardianproject.otr.app.im.engine.InvitationListener;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

/**
 * manages the chat groups for a given protocol
 */
public class ChatGroupManagerServiceImpl extends IChatGroupManager.Stub
{
	private static final String TAG = ChatGroupManagerServiceImpl.class.getSimpleName();
	
    private RemoteImService mService;
	private Context mContext;
	private HashMap<String, ChatGroupServiceImpl> mChatGroups;
    private ChatGroupManager mGroupManager;
    private final RemoteCallbackList<IChatGroupListener> mRemoteChatGroupListeners = new RemoteCallbackList<IChatGroupListener>();
    
    public ChatGroupManagerServiceImpl(ImConnectionServiceImpl connection, RemoteImService service)
    {
    	mService = service;
    	mContext = connection.getContext();
        ImConnection connAdaptee = connection.getAdaptee();
        if((connAdaptee.getCapability() & ImConnection.CAPABILITY_GROUP_CHAT) != 0)
        {
            mGroupManager = connAdaptee.getChatGroupManager();
            mGroupManager.addGroupListener(new MyChatGroupListener());
            mGroupManager.setInvitationListener(new MyInvitationListener());
        }
        mChatGroups = new HashMap<String, ChatGroupServiceImpl>();
    }

    @Override
    public void registerChatGroupListener(IChatGroupListener listener)
    {
        if (listener != null)
        {
            mRemoteChatGroupListeners.register(listener);
        }
    }

    @Override
    public void unregisterChatGroupListener(IChatGroupListener listener)
    {
        if (listener != null)
        {
            mRemoteChatGroupListeners.unregister(listener);
        }
    }

    public void setInvitationListener(InvitationListener listener)
    {
    	mGroupManager.setInvitationListener(listener);
    }
    
	@Override
	public List getChatGroups() throws RemoteException
	{
        synchronized (mChatGroups)
        {
            return new ArrayList<ChatGroupServiceImpl>(mChatGroups.values());
        }
	}

	@Override
	public IChatGroup getChatGroup(String address) throws RemoteException
	{
		IChatGroup result = null;
		ChatGroup chatGroup = mGroupManager.getChatGroup(address);
		// getChatGroup will create the ChatGroup (if needed) and
		// fire onGroupCreated which will instaniate the ChatGroupServiceImpl
		if (chatGroup != null)
		{
			synchronized (mChatGroups)
			{
				ChatGroupServiceImpl service =  mChatGroups.get(address);
				if (service != null)
				{
					result = ChatGroupServiceImpl.asInterface(service);
				}
			}
		}
		return result;
	}

	@Override
	public void createChatGroupAsync(String address) throws RemoteException
	{
		mGroupManager.createChatGroupAsync(address);
	}

	@Override
	public void deleteChatGroupAsync(String address) throws RemoteException
	{
		mGroupManager.deleteChatGroupAsync(address);
	}
	
	@Override
	public void acceptInvitationAsync(long id) throws RemoteException
	{
		mGroupManager.acceptInvitationAsync(id);
	}

	@Override
	public void rejectInvitationAsync(long id) throws RemoteException
	{
		mGroupManager.rejectInvitationAsync(id);
	}

    private class MyChatGroupListener implements GroupListener
    {
    	@Override
        public void onGroupCreated(ChatGroup group)
        {
            synchronized (mChatGroups)
            {
                mChatGroups.put(group.getAddress(), new ChatGroupServiceImpl(group, 0));
            }
            final int N = mRemoteChatGroupListeners.beginBroadcast();
            for (int i = 0; i < N; i++)
            {
                IChatGroupListener listener = mRemoteChatGroupListeners.getBroadcastItem(i);
                try
                {
                	listener.onChatGroupCreated(group.getAddress());
                }
                catch (RemoteException e)
                {
                	e.printStackTrace();
                    // The RemoteCallbackList will take care of removing the
                    // dead listeners.
                }
            }
            mRemoteChatGroupListeners.finishBroadcast();
        	Log.d(TAG, "onGroupCreated");
        }

    	@Override
        public void onGroupDeleted(ChatGroup group)
        {
        }

    	@Override
        public void onGroupError(int errorType, String name, ImErrorInfo error)
        {
            final int N = mRemoteChatGroupListeners.beginBroadcast();
            for (int i = 0; i < N; i++)
            {
                IChatGroupListener listener = mRemoteChatGroupListeners.getBroadcastItem(i);
                try
                {
                	listener.onChatGroupError(name, error);
                }
                catch (RemoteException e)
                {
                	e.printStackTrace();
                    // The RemoteCallbackList will take care of removing the
                    // dead listeners.
                }
            }
            mRemoteChatGroupListeners.finishBroadcast();
        	Log.d(TAG, "onGroupError");
        }

    	@Override
        public void onJoinedGroup(ChatGroup group)
        {
            final int N = mRemoteChatGroupListeners.beginBroadcast();
            for (int i = 0; i < N; i++)
            {
                IChatGroupListener listener = mRemoteChatGroupListeners.getBroadcastItem(i);
                try
                {
                	listener.onChatGroupJoined(group.getAddress());
                }
                catch (RemoteException e)
                {
                	e.printStackTrace();
                    // The RemoteCallbackList will take care of removing the
                    // dead listeners.
                }
            }
            mRemoteChatGroupListeners.finishBroadcast();
        	Log.d(TAG, "onJoinedGroup");
        }

    	@Override
        public void onLeftGroup(ChatGroup group)
        {
            final int N = mRemoteChatGroupListeners.beginBroadcast();
            for (int i = 0; i < N; i++)
            {
                IChatGroupListener listener = mRemoteChatGroupListeners.getBroadcastItem(i);
                try
                {
                	listener.onChatGroupLeft(group.getAddress());
                }
                catch (RemoteException e)
                {
                	e.printStackTrace();
                    // The RemoteCallbackList will take care of removing the
                    // dead listeners.
                }
            }
            mRemoteChatGroupListeners.finishBroadcast();
        	Log.d(TAG, "onJoinedGroup");
        }
    }
    
    private final class MyInvitationListener implements InvitationListener
    {
        private IInvitationListener mRemoteListener;

        public void onGroupInvitation(Invitation invitation)
        {
            String sender = invitation.getSender();
            ContentValues values = new ContentValues(5);
            values.put(Imps.Invitation.INVITE_ID, invitation.getInviteID());
            values.put(Imps.Invitation.SENDER, sender);
            values.put(Imps.Invitation.GROUP_NAME, invitation.getGroupAddress());
            values.put(Imps.Invitation.NOTE, invitation.getReason());
            if (invitation.getPassword() != null)
            {
            	values.put(Imps.Invitation.PASSWORD, invitation.getPassword());
            }
            values.put(Imps.Invitation.STATUS, Imps.Invitation.STATUS_PENDING);
            ContentResolver resolver = mContext.getContentResolver();
            Uri uri = resolver.insert(Imps.Invitation.CONTENT_URI, values);
            long id = ContentUris.parseId(uri);
            try
            {
                if (mRemoteListener != null)
                {
                    mRemoteListener.onGroupInvitation(id);
                    return;
                }
            }
            catch (RemoteException e)
            {
            	RemoteImService.debug( "onGroupInvitation: dead listener " + mRemoteListener +"; removing",e);
                mRemoteListener = null;
            }
            // No listener registered or failed to notify the listener, send a
            // notification instead.
            mService.getStatusBarNotifier().notifyGroupInvitation(id, sender);
        }
    }
}
