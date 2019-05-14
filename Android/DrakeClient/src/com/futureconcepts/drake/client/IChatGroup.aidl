package com.futureconcepts.drake.client;

import com.futureconcepts.drake.client.IChatListener;
import com.futureconcepts.drake.client.Contact;
import com.futureconcepts.drake.client.Message;
import com.futureconcepts.drake.client.IOtrKeyManager;
import com.futureconcepts.drake.client.IOtrChatSession;

interface IChatGroup
{
    /**
     * Gets the name of ChatGroup.
     */
    String getName();

    /**
     * Gets the id of the ChatGroup in content provider.
     */
    long getId();
    
	void addGroupMemberAsync(in Contact contact);
	void removeGroupMemberAsync(in Contact contact);
	void setPassword(String password);
	void joinAsync();
	void leaveAsync();
	boolean isJoined();

    /**
     * Invites a contact to join this ChatGroup. The user can only invite
     * contacts to join this ChatGroup if it's a group session.
     */
	void inviteUserAsync(in Contact invitee);
}
