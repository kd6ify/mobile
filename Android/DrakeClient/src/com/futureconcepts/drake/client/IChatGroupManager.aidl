package com.futureconcepts.drake.client;

import com.futureconcepts.drake.client.IChatGroupListener;
import com.futureconcepts.drake.client.IChatGroup;
import com.futureconcepts.drake.client.Contact;
import com.futureconcepts.drake.client.Invitation;

interface IChatGroupManager
{
    void registerChatGroupListener(IChatGroupListener listener);
    void unregisterChatGroupListener(IChatGroupListener listener);

 //   void registerSubscriptionListener(ISubscriptionListener listener);
 //   void unregisterSubscriptionListener(ISubscriptionListener listener);

	List getChatGroups();
	IChatGroup getChatGroup(String address);
	void createChatGroupAsync(String name);
	void deleteChatGroupAsync(String address);
	void acceptInvitationAsync(in long id);
	void rejectInvitationAsync(in long id);
}
