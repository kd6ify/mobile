package com.futureconcepts.drake.client;

import com.futureconcepts.drake.client.ImErrorInfo;

oneway interface IChatGroupListener {
    /**
     * This method is called when a new ChatGroup is created.
     */
    void onChatGroupCreated(String address);

    /**
     * This method is called when an error occurs on a chat group--most likely an asynchronous operation. 
     * 
     *
     * @param address the chat group
     * @param error detail error,
     */
    void onChatGroupError(String name, in ImErrorInfo error);
    void onChatGroupJoined(String address);
    void onChatGroupLeft(String address);
}
