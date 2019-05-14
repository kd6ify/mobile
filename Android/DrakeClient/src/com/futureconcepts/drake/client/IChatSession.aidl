package com.futureconcepts.drake.client;

import com.futureconcepts.drake.client.IChatListener;
import com.futureconcepts.drake.client.Message;
import com.futureconcepts.drake.client.IOtrKeyManager;
import com.futureconcepts.drake.client.IOtrChatSession;

interface IChatSession {
    /**
     * Registers a ChatListener with this ChatSession to listen to incoming
     * message and participant change events.
     */
    void registerChatListener(IChatListener listener);

    /**
     * Unregisters the ChatListener so that it won't be notified again.
     */
    void unregisterChatListener(IChatListener listener);

    /**
     * Tells if this ChatSession is a group session.
     */
    boolean isGroupChatSession();

    /**
     * Gets the name of ChatSession.
     */
    String getName();

    /**
     * Gets the id of the ChatSession in content provider.
     */
    long getId();

    /**
     * Gets the participants of this ChatSession.
     */
    String[] getPariticipants();

    /**
     * Convert a single chat to a group chat. If the chat session is already a
     * group chat or it's converting to group chat.
     */
    void convertToGroupChat();

    /**
     * Leaves this ChatSession.
     */
    void leave();

    /**
     * Leaves this ChatSession if there isn't any message sent or received in it.
     */
    void leaveIfInactive();

    /**
     * Sends a message to all participants in this ChatSession.
     */
    void sendMessage(String text);

    /**
     * Mark this chat session as read.
     */
    void markAsRead();   
    
    /**
    * Get OTR Session Manager
    */
    IOtrChatSession getOtrChatSession();
    
     /**
    * Get OTR Key Manager
    */
    IOtrKeyManager getOtrKeyManager();
}
