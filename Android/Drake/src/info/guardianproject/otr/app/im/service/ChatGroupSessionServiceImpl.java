package info.guardianproject.otr.app.im.service;

import info.guardianproject.otr.app.im.engine.ChatGroup;
import info.guardianproject.otr.app.im.engine.ChatGroupManager;
import info.guardianproject.otr.app.im.engine.ChatSession;
import info.guardianproject.otr.app.im.engine.ContactListManager;
import info.guardianproject.otr.app.im.engine.GroupMemberListener;
import info.guardianproject.otr.app.im.engine.ImConnection;

import java.util.ArrayList;
import java.util.List;

import com.futureconcepts.drake.client.Contact;
import com.futureconcepts.drake.client.IChatListener;
import com.futureconcepts.drake.client.IOtrChatSession;
import com.futureconcepts.drake.client.IOtrKeyManager;
import com.futureconcepts.drake.client.ImEntity;
import com.futureconcepts.drake.client.ImErrorInfo;
import com.futureconcepts.drake.client.Imps;
import com.futureconcepts.drake.client.Message;
import com.futureconcepts.drake.client.Presence;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

public class ChatGroupSessionServiceImpl extends ChatSessionServiceImplBase
{
	private ChatGroupManager _chatGroupManager;
	private GroupMemberListener _groupMemberListener;
	
    public ChatGroupSessionServiceImpl(ChatSession chatSession, ImConnectionServiceImpl connection)
    {
    	super(chatSession, connection);
    	_chatGroupManager = connection.getAdaptee().getChatGroupManager();
        ImEntity participant = chatSession.getParticipant();
    	chatSession.addMessageListener(mMessageListener);
    	ChatGroup chatGroup = null;
    	String address = participant.getAddress();
    	long groupId = getGroupContactId(address);
    	if (participant instanceof Contact)
    	{
    		chatGroup = _chatGroupManager.getChatGroup(address);
    	}
    	else if (participant instanceof ChatGroup)
    	{
    		chatGroup = (ChatGroup)participant;
    	}
        _groupMemberListener = new MyGroupMemberListener();
        chatGroup.addMemberListener(_groupMemberListener);
        mMessageURI = Imps.Messages.getContentUriByThreadId(groupId);
        mChatURI = ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, groupId);
        insertOrUpdateChat(null);
        if (chatGroup.getMembers() != null)
        {
	        for (Contact c : chatGroup.getMembers())
	        {
	            mContactStatusMap.put(c.getName(), c.getPresence().getStatus());
	        }
        }
    }

    // IChatSession
    
    @Override
    public IOtrKeyManager getOtrKeyManager () 
    {
    	return null;
    }

    @Override
    public IOtrChatSession getOtrChatSession () 
    {
    	return null;
    }

    @Override
    public String[] getPariticipants()
    {
        Contact self = mConnection.getLoginUser();
        ChatGroup group = (ChatGroup)mChatSession.getParticipant();
        List<Contact> members = group.getMembers();
        String[] result = new String[members.size() - 1];
        int index = 0;
        for (Contact c : members)
        {
            if (!c.equals(self))
            {
                result[index++] = c.getAddress();
            }
        }
        return result;
    }

    /**
     * Convert this chat session to a group chat. If it's already a group chat,
     * nothing will happen. The method works in async mode and the registered
     * listener will be notified when it's converted to group chat successfully.
     *
     * Note that the method is not thread-safe since it's always called from
     * the UI and Android uses single thread mode for UI.
     */
    @Override
    public void convertToGroupChat()
    {
    	// impossible--already a group chat
    }

    @Override
    public boolean isGroupChatSession()
    {
    	return true;
    }

    @Override
    public void leave()
    {
    	ChatGroup chatGroup = _chatGroupManager.getChatGroup(mChatSession.getParticipant().getAddress());
    	if (chatGroup != null)
    	{
    		chatGroup.leaveAsync();
    	}
        super.leave();
    }

    @Override
    public void sendMessage(String text)
    {
        if (mConnection.getState() == ImConnection.SUSPENDED) {
            // connection has been suspended, save the message without send it
            insertMessageInDb(null, text, -1, Imps.MessageType.POSTPONED);
            return;
        }
        Message msg = new Message(text);
        String from = mConnection.getLoginUser().getAddress();
        msg.setFrom(from);
    	msg.setType(Message.Type.GROUPCHAT);
        mChatSession.sendMessageAsync(msg);
    }

    @Override
    public String getNickName(String username)
    {
        ImEntity participant = mChatSession.getParticipant();
        ChatGroup group = (ChatGroup)participant;
        List<Contact> members = group.getMembers();
        for (Contact c : members)
        {
            if (username.equals(c.getAddress()))
            {
                return c.getName();
            }
        }
        // not found, impossible
        return username;
    }

    private long insertGroupContactInDb(ChatGroup group)
    {
        // Insert a record in contacts table
        ContentValues values = new ContentValues(4);
        values.put(Imps.Contacts.USERNAME, group.getAddress());
        values.put(Imps.Contacts.NICKNAME, group.getName());
        values.put(Imps.Contacts.CONTACTLIST, ContactListManagerServiceImpl.FAKE_TEMPORARY_LIST_ID);
        values.put(Imps.Contacts.TYPE, Imps.Contacts.TYPE_GROUP);
        
        Uri uri = mContentResolver.insert(Imps.Contacts.CONTENT_URI, values);
        Log.d("contact", "Inserted into" + uri.toString());
        Log.d("contact", values.toString());

        long id = ContentUris.parseId(uri);

        Presence presence = new Presence();
        presence.setStatus(Presence.AVAILABLE);
        ContentValues presenceValues = ContactListManagerServiceImpl.getPresenceValues(id, presence);
        mContentResolver.insert(Imps.Presence.CONTENT_URI, presenceValues);

        ArrayList<ContentValues> memberValues = new ArrayList<ContentValues>();
        Contact self = mConnection.getLoginUser();
        for (Contact member : group.getMembers())
        {
            if (!member.equals(self))
            { // avoid to insert the user himself
                ContentValues memberValue = new ContentValues(2);
                memberValue.put(Imps.GroupMembers.USERNAME, member.getAddress());
                memberValue.put(Imps.GroupMembers.NICKNAME, member.getName());
                memberValues.add(memberValue);
            }
        }
        if (!memberValues.isEmpty())
        {
            ContentValues[] result = new ContentValues[memberValues.size()];
            memberValues.toArray(result);
            Uri memberUri = ContentUris.withAppendedId(Imps.GroupMembers.CONTENT_URI, id);
            mContentResolver.bulkInsert(memberUri, result);
        }
        return id;
    }

    private long getGroupContactId(String address)
    {
    	long result = -1;
    	String[] projection = { Imps.Contacts._ID };
        Cursor cursor = mContentResolver.query(Imps.Contacts.CONTENT_URI, projection, Imps.Contacts.USERNAME + "=?", new String[] { address }, null);
        if (cursor != null)
        {
        	if (cursor.moveToFirst())
        	{
        		result = cursor.getLong(0);
        	}
        	cursor.close();
        	cursor = null;
        }
        return result;
    }
    
    private void insertGroupMemberInDb(Contact member)
    {
        ContentValues values1 = new ContentValues(2);
        values1.put(Imps.GroupMembers.USERNAME, member.getAddress());
        values1.put(Imps.GroupMembers.NICKNAME, member.getName());
        ContentValues values = values1;

        long groupId = ContentUris.parseId(mChatURI);
        Uri uri = ContentUris.withAppendedId(Imps.GroupMembers.CONTENT_URI, groupId);
        mContentResolver.insert(uri, values);

        // Mantis 7777
        //        insertMessageInDb(member.getName(), null, System.currentTimeMillis(), Imps.MessageType.PRESENCE_AVAILABLE);
       
    }

    private void deleteGroupMemberInDb(Contact member)
    {
        String where = Imps.GroupMembers.USERNAME + "=?";
        String[] selectionArgs = { member.getAddress() };
        long groupId = ContentUris.parseId(mChatURI);
        Uri uri = ContentUris.withAppendedId(Imps.GroupMembers.CONTENT_URI, groupId);
        mContentResolver.delete(uri, where, selectionArgs);
        
        // Mantis 7777
        //      insertMessageInDb(member.getName(), null, System.currentTimeMillis(), Imps.MessageType.PRESENCE_UNAVAILABLE);
    }
    
    private final class MyGroupMemberListener implements GroupMemberListener
    {
		@Override
		public void onMemberJoined(ChatGroup group, Contact contact)
		{
            insertGroupMemberInDb(contact);
            final int N = mRemoteListeners.beginBroadcast();
            for (int i = 0; i < N; i++)
            {
                IChatListener listener = mRemoteListeners.getBroadcastItem(i);
                try
                {
                    listener.onContactJoined(ChatGroupSessionServiceImpl.this, contact);
                }
                catch (RemoteException e)
                {
                	e.printStackTrace();
                    // The RemoteCallbackList will take care of removing the
                    // dead listeners.
                }
            }
            mRemoteListeners.finishBroadcast();
		}

		@Override
		public void onMemberLeft(ChatGroup group, Contact contact)
		{
            deleteGroupMemberInDb(contact);

            final int N = mRemoteListeners.beginBroadcast();
            for (int i = 0; i < N; i++)
            {
                IChatListener listener = mRemoteListeners.getBroadcastItem(i);
                try
                {
                    listener.onContactLeft(ChatGroupSessionServiceImpl.this, contact);
                }
                catch (RemoteException e)
                {
                	e.printStackTrace();
                    // The RemoteCallbackList will take care of removing the
                    // dead listeners.
                }
            }
            mRemoteListeners.finishBroadcast();
		}

		@Override
		public void onError(ChatGroup group, ImErrorInfo error)
		{
            // TODO: insert an error message?
            final int N = mRemoteListeners.beginBroadcast();
            for (int i = 0; i < N; i++)
            {
                IChatListener listener = mRemoteListeners.getBroadcastItem(i);
                try
                {
                    listener.onInviteError(ChatGroupSessionServiceImpl.this, error);
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
