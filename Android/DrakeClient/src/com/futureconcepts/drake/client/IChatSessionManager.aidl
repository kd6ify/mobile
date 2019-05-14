package com.futureconcepts.drake.client;

import com.futureconcepts.drake.client.IChatSession;
import com.futureconcepts.drake.client.IChatSessionListener;

interface IChatSessionManager
{
    void registerChatSessionListener(IChatSessionListener listener);
    void unregisterChatSessionListener(IChatSessionListener listener);

    /**
     * Create a ChatSession with the specified contact. If the contact does not exist in any
     * of the user's contact lists, it will be added to the temporary list.
     *
     * @param contactAddress the address of the contact.
     */
    IChatSession createChatSession(String contactAddress);

    /**
     * Get the ChatSession that is associated with the specified contact or group.
     *
     * @param the address of the contact or group.
     * @return the ChatSession with the contact or group or <code>null</code> if
     *       there isn't any active ChatSession with the contact or group.
     */
    IChatSession getChatSession(String address);

    /**
     * Get a list of all active ChatSessions.
     *
     * @return a list of IBinders of all active ChatSessions.
     */
    List getActiveChatSessions();
}
