package info.guardianproject.otr.app.im.service;

import info.guardianproject.otr.app.im.engine.ChatGroupManager;
import info.guardianproject.otr.app.im.engine.ChatSession;
import info.guardianproject.otr.app.im.engine.MessageListener;

import java.util.HashMap;

import com.futureconcepts.drake.client.IChatListener;
import com.futureconcepts.drake.client.IChatSession;
import com.futureconcepts.drake.client.ImErrorInfo;
import com.futureconcepts.drake.client.Imps;
import com.futureconcepts.drake.client.Message;
import com.futureconcepts.drake.client.Presence;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.util.Log;

public abstract class ChatSessionServiceImplBase extends IChatSession.Stub
{
    /**
     * The registered remote listeners.
     */
    protected final RemoteCallbackList<IChatListener> mRemoteListeners = new RemoteCallbackList<IChatListener>();

    protected ImConnectionServiceImpl mConnection;
    protected ChatSessionManagerServiceImpl mChatSessionManager;
        
    protected ChatSession mChatSession;
    protected MyMessageListener mMessageListener;
    protected StatusBarNotifier mStatusBarNotifier;

    protected ContentResolver mContentResolver;
    /*package*/Uri mChatURI;
    protected Uri mMessageURI;

    protected static final int MAX_HISTORY_COPY_COUNT = 10;

    protected HashMap<String, Integer> mContactStatusMap = new HashMap<String, Integer>();

    protected boolean mHasUnreadMessages;

    protected RemoteImService service = null;
    
    public ChatSessionServiceImplBase(ChatSession chatSession, ImConnectionServiceImpl connection)
    {
        mChatSession = chatSession;
        mConnection = connection;
        
        service = connection.getContext();
        mContentResolver = service.getContentResolver();
        mStatusBarNotifier = service.getStatusBarNotifier();
        mChatSessionManager = (ChatSessionManagerServiceImpl) connection.getChatSessionManager();
        
        mMessageListener = new MyMessageListener();
    }

    public ChatSession getChatSession()
    {
    	return mChatSession;
    }

    public ChatGroupManager getGroupManager()
    {
        return mConnection.getAdaptee().getChatGroupManager();
    }
    
    // IChatSession

    public Uri getChatUri()
    {
        return mChatURI;
    }

    @Override
    public String getName()
    {
        return mChatSession.getParticipant().getAddress();
    }

    protected abstract String getNickName(String username);
    
    public String getAddress()
    {
        return mChatSession.getParticipant().getAddress();
    }

    @Override
    public long getId()
    {
        return ContentUris.parseId(mChatURI);
    }

    @Override
    public void leave()
    {
        mContentResolver.delete(mMessageURI, null, null);
        mContentResolver.delete(mChatURI, null, null);
        mStatusBarNotifier.dismissChatNotification(getAddress());
        mChatSessionManager.closeChatSession(this);
    }

    @Override
    public void leaveIfInactive()
    {
        if (mChatSession.getHistoryMessages().isEmpty())
        {
            leave();
        }
    }

    public void sendPostponedMessages()
    {
        String[] projection = new String[] {
            BaseColumns._ID,
            Imps.Messages.BODY,
            Imps.Messages.DATE,
            Imps.Messages.TYPE,
        };
        Cursor c = mContentResolver.query(mMessageURI, projection, Imps.Messages.TYPE+"='"+Imps.MessageType.POSTPONED+"'", null, null);
        if (c == null)
        {
        	RemoteImService.debug( "Query error while querying postponed messages");
            return;
        }
        while (c.moveToNext())
        {
            String body = c.getString(1);
            Message msg = new Message(body);
            // TODO OTRCHAT move setFrom() to ChatSession.sendMessageAsync()
            msg.setFrom(mConnection.getLoginUser().getAddress());
            mChatSession.sendMessageAsync(msg);

            //TODO c.updateLong(2, System.currentTimeMillis());
            //c.updateInt(3, Imps.MessageType.OUTGOING);
        }
        //c.commitUpdates();
        c.close();
    }

    public void registerChatListener(IChatListener listener)
    {
        if (listener != null)
        {
            mRemoteListeners.register(listener);
        }
    }

    public void unregisterChatListener(IChatListener listener)
    {
        if (listener != null)
        {
            mRemoteListeners.unregister(listener);
        }
    }

    public void markAsRead()
    {
        if (mHasUnreadMessages)
        {
            ContentValues values = new ContentValues(1);
            values.put(Imps.Chats.LAST_UNREAD_MESSAGE, (String) null);
            mConnection.getContext().getContentResolver().update(mChatURI, values, null, null);

            mStatusBarNotifier.dismissChatNotification(getAddress());

            mHasUnreadMessages = false;
        }
    }
    
    protected void insertOrUpdateChat(String message)
    {
        ContentValues values = new ContentValues(2);

        values.put(Imps.Chats.LAST_MESSAGE_DATE, System.currentTimeMillis());
        values.put(Imps.Chats.LAST_UNREAD_MESSAGE, message);
        // ImProvider.insert() will replace the chat if it already exist.
        mContentResolver.insert(mChatURI, values);
    }

    protected void insertPresenceUpdatesMsg(String contact, Presence presence)
    {
        int status = presence.getStatus();

        Integer previousStatus = mContactStatusMap.get(contact);
        if (previousStatus != null && previousStatus == status)
        {
            // don't insert the presence message if it's the same status
            // with the previous presence update notification
            return;
        }
        mContactStatusMap.put(contact, status);
        int messageType;
        switch (status)
        {
            case Presence.AVAILABLE:
                messageType = Imps.MessageType.PRESENCE_AVAILABLE;
                break;

            case Presence.AWAY:
            case Presence.IDLE:
                messageType = Imps.MessageType.PRESENCE_AWAY;
                break;

            case Presence.DO_NOT_DISTURB:
                messageType = Imps.MessageType.PRESENCE_DND;
                break;

            default:
                messageType = Imps.MessageType.PRESENCE_UNAVAILABLE;
                break;
        }
        insertMessageInDb(contact, null, System.currentTimeMillis(), messageType);
    }

    protected void removeMessageInDb(int type)
    {
        mContentResolver.delete(mMessageURI, Imps.Messages.TYPE + "=?",  new String[]{Integer.toString(type)});
    }

    protected Uri insertMessageInDb(String contact, String body, long time, int type)
    {
        return insertMessageInDb(contact, body, time, type, 0/*No error*/, null);
    }

    protected Uri insertMessageInDb(String contact, String body, long time, int type, int errCode, String id)
    {
        ContentValues values = new ContentValues(4);
        values.put(Imps.Messages.BODY, body);
        values.put(Imps.Messages.DATE, time);
        values.put(Imps.Messages.TYPE, type);
        values.put(Imps.Messages.ERROR_CODE, errCode);
        values.put(Imps.Messages.NICKNAME, contact);
        values.put(Imps.Messages.IS_GROUP_CHAT, 1);
        values.put(Imps.Messages.IS_DELIVERED, 0);
        values.put(Imps.Messages.PACKET_ID, id);

        Log.d("ChatSessionServiceImplBase", "Inserted into " + mMessageURI.toString());
        return mContentResolver.insert(mMessageURI, values);
    }

    protected int updateConfirmInDb(String id, int value)
    {
        Uri messageUri = Uri.parse(Imps.Messages.OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID + "/" + id);
        ContentValues values = new ContentValues(1);
        values.put(Imps.Messages.IS_DELIVERED, value);
        return mContentResolver.update(messageUri, values, null, null);
    }

    class MyMessageListener implements MessageListener
    {
        public void onIncomingMessage(ChatSession ses, final Message msg)
        {
            String body = msg.getBody();
            String username = msg.getFrom();
            String nickname = username;
            long time = msg.getDateTime().getTime();
            insertOrUpdateChat(body);
            insertMessageInDb(nickname, body, time, Imps.MessageType.INCOMING);

            int N = mRemoteListeners.beginBroadcast();
            for (int i = 0; i < N; i++)
            {
                IChatListener listener = mRemoteListeners.getBroadcastItem(i);
                try
                {
                    listener.onIncomingMessage(ChatSessionServiceImplBase.this, msg);
                }
                catch (RemoteException e)
                {
                    // The RemoteCallbackList will take care of removing the
                    // dead listeners.
                }
            }
            mRemoteListeners.finishBroadcast();

            mStatusBarNotifier.notifyChat(getId(), username, nickname, "", N > 0);

            mHasUnreadMessages = true;
        }

        public void onSendMessageError(ChatSession ses, final Message msg, final ImErrorInfo error)
        {
            insertMessageInDb(null, null, System.currentTimeMillis(), Imps.MessageType.OUTGOING, error.getCode(), null);
            final int N = mRemoteListeners.beginBroadcast();
            for (int i = 0; i < N; i++)
            {
                IChatListener listener = mRemoteListeners.getBroadcastItem(i);
                try
                {
                    listener.onSendMessageError(ChatSessionServiceImplBase.this, msg, error);
                }
                catch (RemoteException e)
                {
                    // The RemoteCallbackList will take care of removing the
                    // dead listeners.
                }
            }
            mRemoteListeners.finishBroadcast();
        }

		@Override
		public void onIncomingReceipt(ChatSession ses, String id)
		{
			// TODO this just generates a debug message in the chat log.
			// TODO Needs a real implementation.
			updateConfirmInDb(id, 1);

            int N = mRemoteListeners.beginBroadcast();
            for (int i = 0; i < N; i++)
            {
                IChatListener listener = mRemoteListeners.getBroadcastItem(i);
                try
                {
                    listener.onIncomingReceipt(ChatSessionServiceImplBase.this, id);
                }
                catch (RemoteException e)
                {
                    // The RemoteCallbackList will take care of removing the
                    // dead listeners.
                }
            }
            mRemoteListeners.finishBroadcast();
		}

		@Override
		public void onReceiptsExpected(ChatSession ses)
		{
			// TODO
		}

		@Override
		public void onStatusChanged(ChatSession session)
		{
            final int N = mRemoteListeners.beginBroadcast();
            for (int i = 0; i < N; i++)
            {
                IChatListener listener = mRemoteListeners.getBroadcastItem(i);
                try
                {
                    listener.onStatusChanged(ChatSessionServiceImplBase.this);
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
