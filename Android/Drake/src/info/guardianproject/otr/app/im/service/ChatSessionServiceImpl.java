package info.guardianproject.otr.app.im.service;

import com.futureconcepts.drake.client.Contact;
import com.futureconcepts.drake.client.IChatListener;
import com.futureconcepts.drake.client.IOtrChatSession;
import com.futureconcepts.drake.client.IOtrKeyManager;
import com.futureconcepts.drake.client.ImEntity;
import com.futureconcepts.drake.client.Imps;
import com.futureconcepts.drake.client.Message;

import info.guardianproject.otr.OtrChatListener;
import info.guardianproject.otr.OtrChatManager;
import info.guardianproject.otr.OtrChatSessionAdapter;
import info.guardianproject.otr.OtrKeyManagerAdapter;
import info.guardianproject.otr.app.im.engine.ChatGroup;
import info.guardianproject.otr.app.im.engine.ChatSession;
import info.guardianproject.otr.app.im.engine.ImConnection;

import net.java.otr4j.session.SessionID;
import android.content.ContentUris;
import android.os.RemoteException;

public class ChatSessionServiceImpl extends ChatSessionServiceImplBase
{
    //all the otr bits that work per session
    private OtrChatManager mOtrChatManager;
    private OtrKeyManagerAdapter mOtrKeyManager;
    private OtrChatSessionAdapter mOtrChatSession;
    
    private boolean mConvertingToGroupChat;
    
    public ChatSessionServiceImpl(ChatSession chatSession, ImConnectionServiceImpl connection)
    {
    	super(chatSession, connection);
        ImEntity participant = chatSession.getParticipant();
		String localUserId = mConnection.getLoginUser().getAddress();
		String remoteUserId = participant.getAddress();

        mOtrChatManager = service.getOtrChatManager();
		mOtrChatSession = new OtrChatSessionAdapter(localUserId, remoteUserId, mOtrChatManager);
		SessionID sessionId = mOtrChatManager.getSessionId(localUserId, remoteUserId);

		mOtrKeyManager = new OtrKeyManagerAdapter(mOtrChatManager.getKeyManager(), sessionId);
		
        // add OtrChatListener as the intermediary to mListenerAdapter so it can filter OTR msgs
        
        mChatSession.addMessageListener(new OtrChatListener(mOtrChatManager, mMessageListener));
        mChatSession.setOtrChatManager(mOtrChatManager);
        
        init((Contact)participant);
    }

    // IChatSession
    
    @Override
    public IOtrKeyManager getOtrKeyManager () 
    {
        	return mOtrKeyManager;
    }

    @Override
    public IOtrChatSession getOtrChatSession () 
    {
    	return mOtrChatSession;
    }

    private void init(Contact contact)
    {
        ContactListManagerServiceImpl listManager = (ContactListManagerServiceImpl) mConnection.getContactListManager();
        long contactId = listManager.queryOrInsertContact(contact);

        mMessageURI = Imps.Messages.getContentUriByThreadId(contactId);
        mChatURI = ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, contactId);
        insertOrUpdateChat(null);

        mContactStatusMap.put(contact.getName(), contact.getPresence().getStatus());
    }

    @Override
    public String[] getPariticipants()
    {
        return new String[] {mChatSession.getParticipant().getAddress()};
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
        if (mConvertingToGroupChat)
        {
            return;
        }
        mConvertingToGroupChat = true;
   //     new ChatConvertor().convertToGroupChat();
    }

    @Override
    public boolean isGroupChatSession()
    {
        return false;
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

    @Override
    public void sendMessage(String text)
    {
        if (mConnection.getState() != ImConnection.LOGGED_IN) {
            // connection has been suspended, save the message without send it
            insertMessageInDb(null, text, -1, Imps.MessageType.POSTPONED);
            return;
        }
        Message msg = new Message(text);
        // TODO OTRCHAT move setFrom() to ChatSession.sendMessageAsync()
        msg.setFrom(mConnection.getLoginUser().getAddress());
        mChatSession.sendMessageAsync(msg);
        long now = System.currentTimeMillis();
        // TODO remember message ID so we can notify user on receipt (XEP-0184)
        insertMessageInDb(null, text, now, Imps.MessageType.OUTGOING, 0, msg.getID());
//        try
//		{
//			Thread.sleep(3000);
//		}
//		catch (InterruptedException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
 //       mChatSession.onMessageReceipt(msg.getID());
    }

    @Override
    public String getNickName(String username)
    {
    	if (username.equals("System"))
    	{
    		return username;
    	}
    	else
    	{
    		ImEntity participant = mChatSession.getParticipant();
    		return ((Contact)participant).getName();
    	}
    }

    private void onConvertToGroupChatSuccess(ChatGroup group)
    {
    	// how to convert a ChatSessionServiceImpl to a ChatGroupSessionServiceImpl?
//        Contact oldParticipant = (Contact)mAdaptee.getParticipant();
//        String oldAddress = getAddress();
//        mAdaptee.setParticipant(group);
//        mChatSessionManager.updateChatSession(oldAddress, this);

//        Uri oldChatUri = mChatURI;
//        Uri oldMessageUri = mMessageURI;
//        init(group);
//        copyHistoryMessages(oldParticipant);

//        mContentResolver.delete(oldMessageUri, NON_CHAT_MESSAGE_SELECTION, null);
//        mContentResolver.delete(oldChatUri, null, null);

//        mMessageListener.notifyChatSessionConverted();
//        mConvertingToGroupChat = false;
    }

//    class ChatConvertor implements GroupListener, GroupMemberListener {
 //       private ChatGroupManagerAdapter mGroupMgr;
  //      private String mGroupName;

//        public ChatConvertor() {
 //           mGroupMgr = mConnection.getChatGroupManager();
  //      }

//        public void convertToGroupChat() {
//            mGroupMgr.addGroupListener(this);
//            mGroupName = "G" + System.currentTimeMillis();
 //           mGroupMgr.createChatGroupAsync(mGroupName);
 //       }

//        public void onGroupCreated(ChatGroup group) {
 //           if (mGroupName.equalsIgnoreCase(group.getName())) {
  //              mGroupMgr.removeGroupListener(this);
   //             group.addMemberListener(this);
    //            mGroupMgr.inviteUserAsync(group, (Contact)mAdaptee.getParticipant());
      //      }
       // }

       // public void onMemberJoined(ChatGroup group, Contact contact) {
         //   if (mAdaptee.getParticipant().equals(contact)) {
           //     onConvertToGroupChatSuccess(group);
            //}

           // mContactStatusMap.put(contact.getName(), contact.getPresence().getStatus());
      //  }

     //   public void onGroupDeleted(ChatGroup group) {
      //  }

      //  public void onGroupError(int errorType, String groupName, ImErrorInfo error) {
      //  }

      //  public void onJoinedGroup(ChatGroup group) {
       // }

       // public void onLeftGroup(ChatGroup group) {
      //  }

      //  public void onError(ChatGroup group, ImErrorInfo error) {
      //  }

        //public void onMemberLeft(ChatGroup group, Contact contact) {
         //   mContactStatusMap.remove(contact.getName());
       // }
   // }
    
    private void notifyChatSessionConverted()
    {
        final int N = mRemoteListeners.beginBroadcast();
        for (int i = 0; i < N; i++)
        {
            IChatListener listener = mRemoteListeners.getBroadcastItem(i);
            try
            {
                listener.onConvertedToGroupChat(this);
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
