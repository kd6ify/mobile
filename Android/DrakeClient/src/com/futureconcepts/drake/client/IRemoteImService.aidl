package com.futureconcepts.drake.client;

import com.futureconcepts.drake.client.IImConnection;
import com.futureconcepts.drake.client.IOtrKeyManager;

interface IRemoteImService
{
    /**
     * Get connection
     */
    IImConnection getConnection();

    /**
     * Dismiss all notifications.
     */
    void dismissNotifications();

    /**
     * Dismiss notification for the specified chat.
     */
    void dismissChatNotification(String username);
        
     /**
    * Get OTR Key Manager
    */
    IOtrKeyManager getOtrKeyManager();
}
