package com.futureconcepts.drake.client;

import com.futureconcepts.drake.client.IChatSession;
import com.futureconcepts.drake.client.Contact;
import com.futureconcepts.drake.client.ImErrorInfo;
import com.futureconcepts.drake.client.Message;

oneway interface IChatListener
{
    /**
     * This method is called when a new message of the ChatSession has arrived.
     */
    void onIncomingMessage(IChatSession ses, in Message msg);

    /**
     * This method is called when an error is found to send a message in the ChatSession.
     */
    void onSendMessageError(IChatSession ses, in Message msg, in ImErrorInfo error);

    /**
     * This method is called when the chat is converted to a group chat.
     */
    void onConvertedToGroupChat(IChatSession ses);

    /**
     * This method is called when a new contact has joined into this ChatSession.
     */
    void onContactJoined(IChatSession ses, in Contact contact);

    /**
     * This method is called when a contact in this ChatSession has left.
     */
    void onContactLeft(IChatSession ses, in Contact contact);

    /**
     * This method is called when an error is found to invite a contact to join
     * this ChatSession.
     */
    void onInviteError(IChatSession ses, in ImErrorInfo error);

    /**
     * This method is called when a new receipt has arrived.
     */
    void onIncomingReceipt(IChatSession ses, in String packetId);

	/** This method is called when OTR status changes */
	void onStatusChanged(IChatSession ses);
}
