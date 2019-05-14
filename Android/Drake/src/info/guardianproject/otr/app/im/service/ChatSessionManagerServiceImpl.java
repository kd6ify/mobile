package info.guardianproject.otr.app.im.service;

import info.guardianproject.otr.OtrChatManager;
import info.guardianproject.otr.app.im.engine.ChatSession;
import info.guardianproject.otr.app.im.engine.ChatSessionListener;
import info.guardianproject.otr.app.im.engine.ChatSessionManager;
import info.guardianproject.otr.app.im.engine.ImConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.futureconcepts.drake.client.IChatSession;
import com.futureconcepts.drake.client.IChatSessionListener;
import com.futureconcepts.drake.client.IChatSessionManager;
import com.futureconcepts.drake.client.ImEntity;
import com.futureconcepts.drake.client.ImErrorInfo;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

/**
 * manages the chat sessions for a given protocol
 */
public class ChatSessionManagerServiceImpl extends IChatSessionManager.Stub
{
    ImConnectionServiceImpl mConnection;
    ChatSessionManager mChatSessionManager;
    HashMap<String, ChatSessionServiceImplBase> mActiveChatSessionAdapters;
    MyChatSessionListener mChatSessionListener;
    OtrChatManager mOtrChatManager;
    final RemoteCallbackList<IChatSessionListener> mRemoteListeners = new RemoteCallbackList<IChatSessionListener>();

    public ChatSessionManagerServiceImpl(ImConnectionServiceImpl connection)
    {
        mConnection = connection;
        ImConnection connAdaptee = connection.getAdaptee();
        mChatSessionManager = connAdaptee.getChatSessionManager();
        mActiveChatSessionAdapters = new HashMap<String, ChatSessionServiceImplBase>();
        mChatSessionListener = new MyChatSessionListener();
        mChatSessionManager.addChatSessionListener(mChatSessionListener);
        RemoteImService service = connection.getContext();
        mOtrChatManager = service.getOtrChatManager();
    }

    public ChatSessionManager getChatSessionManager()
    {
            return mChatSessionManager;
    }

    public IChatSession createChatSession(String contactAddress)
    {
        ContactListManagerServiceImpl listManager = (ContactListManagerServiceImpl) mConnection.getContactListManager();
        ImEntity entity = listManager.getContactByAddress(contactAddress);
        if(entity == null)
        {
            try
            {
                entity = listManager.createTemporaryContact(contactAddress);
            }
            catch (IllegalArgumentException e)
            {
                mChatSessionListener.notifyChatSessionCreateFailed(contactAddress,
                        new ImErrorInfo(ImErrorInfo.ILLEGAL_CONTACT_ADDRESS,
                                "Invalid contact address:" + contactAddress));
                return null;
            }
        }
        ImConnection imConnection = mConnection.getAdaptee();
        String userName = imConnection.getLoginUserName();
        ChatSession session = mChatSessionManager.createChatSession(entity);
            
        if (mOtrChatManager == null)
        {
        	RemoteImService.debug("mOtrChatManager == null");
        }
        else
        {
        	RemoteImService.debug( "mOtrChatManager.startSession("+userName+", "+contactAddress+")");
        	//mOtrChatManager.startSession(userName, contactAddress);
        }
    	return getChatSessionServiceImpl(session);
    }

    public void closeChatSession(ChatSessionServiceImplBase adapter)
    {
        synchronized (mActiveChatSessionAdapters)
        {
            ChatSession session = adapter.getChatSession();
            mChatSessionManager.closeChatSession(session);
            mActiveChatSessionAdapters.remove(adapter.getAddress());
        }
    }

    public void closeAllChatSessions()
    {
        synchronized (mActiveChatSessionAdapters)
        {
            ArrayList<IChatSession> adapters = new ArrayList<IChatSession>(mActiveChatSessionAdapters.values());
            for (IChatSession adapter : adapters)
            {
            	try
            	{
					adapter.leave();
				}
            	catch (RemoteException e)
            	{
					e.printStackTrace();
				}
            }
        }
    }

    public void updateChatSession(String oldAddress, ChatSessionServiceImpl adapter)
    {
        synchronized (mActiveChatSessionAdapters)
        {
            mActiveChatSessionAdapters.remove(oldAddress);
            mActiveChatSessionAdapters.put(adapter.getAddress(), adapter);
        }
    }

    public IChatSession getChatSession(String address)
    {
        synchronized (mActiveChatSessionAdapters)
        {
            return mActiveChatSessionAdapters.get(address);
        }
    }

    public List getActiveChatSessions()
    {
        synchronized (mActiveChatSessionAdapters)
        {
            return new ArrayList<IChatSession>(mActiveChatSessionAdapters.values());
        }
    }

    public int getChatSessionCount()
    {
        synchronized (mActiveChatSessionAdapters)
        {
            return mActiveChatSessionAdapters.size();
        }
    }

    public void registerChatSessionListener(IChatSessionListener listener)
    {
    	if (listener != null)
    	{
            mRemoteListeners.register(listener);
        }
    }

    public void unregisterChatSessionListener(IChatSessionListener listener)
    {
        if (listener != null)
        {
            mRemoteListeners.unregister(listener);
        }
    }

    IChatSession getChatSessionServiceImpl(ChatSession session)
    {
    	ChatSessionServiceImplBase result = null;
        synchronized (mActiveChatSessionAdapters)
        {
            String key = session.getParticipant().getAddress();
            result = mActiveChatSessionAdapters.get(key);
            if (result == null)
            {
            	if (session.isGroupSession())
            	{
            		result = new ChatGroupSessionServiceImpl(session, mConnection);
            	}
            	else
            	{
            		result = new ChatSessionServiceImpl(session, mConnection);
            	}
                mActiveChatSessionAdapters.put(key, result);
            }
        }
        return result;
    }

    private class MyChatSessionListener implements ChatSessionListener
    {
        public void onChatSessionCreated(ChatSession session)
        {
            final IChatSession sessionAdapter = getChatSessionServiceImpl(session);
            final int N = mRemoteListeners.beginBroadcast();
            for (int i = 0; i < N; i++)
            {
                IChatSessionListener listener = mRemoteListeners.getBroadcastItem(i);
                try
                {
                    listener.onChatSessionCreated(sessionAdapter);
                }
                catch (RemoteException e)
                {
                    // The RemoteCallbackList will take care of removing the
                    // dead listeners.
                }
            }
            mRemoteListeners.finishBroadcast();
        }

        public void notifyChatSessionCreateFailed(final String name, final ImErrorInfo error)
        {
            final int N = mRemoteListeners.beginBroadcast();
            for (int i = 0; i < N; i++)
            {
                IChatSessionListener listener = mRemoteListeners.getBroadcastItem(i);
                try
                {
                    listener.onChatSessionCreateError(name, error);
                }
                catch (RemoteException e)
                {
                    // The RemoteCallbackList will take care of removing the
                    // dead listeners.
                }
            }
            mRemoteListeners.finishBroadcast();
        }

    }
}
