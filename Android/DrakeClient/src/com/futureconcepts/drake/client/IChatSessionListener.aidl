package com.futureconcepts.drake.client;

import com.futureconcepts.drake.client.IChatSession;
import com.futureconcepts.drake.client.ImErrorInfo;

oneway interface IChatSessionListener {
    /**
     * This method is called when a new ChatSession is created. A ChatSession
     * will be created either when the user called explicitly or an incoming
     * message which doesn't belong to any active sessions arrived.
     */
    void onChatSessionCreated(IChatSession session);

    /**
     * This method is called when it failed to create a new ChatSession.
     *
     * @param name the name of the ChatSession failed to create. It's either the
     *      name of the contact or the group.
     * @param error detail error,
     */
    void onChatSessionCreateError(String name, in ImErrorInfo error);
}
