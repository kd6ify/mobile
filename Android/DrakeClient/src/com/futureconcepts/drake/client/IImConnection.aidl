package com.futureconcepts.drake.client;

import android.location.Location;

import com.futureconcepts.drake.client.IFileTransferManager;
import com.futureconcepts.drake.client.IChatGroupManager;
import com.futureconcepts.drake.client.IChatSessionManager;
import com.futureconcepts.drake.client.IContactListManager;
import com.futureconcepts.drake.client.IInvitationListener;
import com.futureconcepts.drake.client.Presence;

interface IImConnection
{
	void destroy();
	IFileTransferManager getFileTransferManager();
    IContactListManager getContactListManager();
    IChatGroupManager getChatGroupManager();
    IChatSessionManager getChatSessionManager();

    /**
     * Login the IM server.
     *
     * @Param one time password not to be saved, for use if password is not persisted
     * @param autoLoadContacts if true, contacts will be loaded from the server
     *          automatically after the user successfully login; otherwise, the
     *          client must load contacts manually.
     */
    void login(String passwordTempt, boolean autoLoadContacts, boolean retry);
    void logout();
    void cancelLogin();

    Presence getUserPresence();
    int updateUserPresence(in Presence newPresence);

    /**
     * Gets an array of presence status which are supported by the IM provider.
     */
    int[] getSupportedPresenceStatus();

    int getState();

    /**
     * Gets the count of active ChatSessions of this connection.
     */
    int getChatSessionCount();

    void sendHeartbeat();
    
    void publishLocation(in Location location);
    
    void setProxy(String type, String host, int port);
}
