package info.guardianproject.otr.app.im.engine;

import com.futureconcepts.drake.client.ImErrorInfo;
import com.futureconcepts.drake.client.Message;

/**
 * Interface that allows for implementing classes to listen for new message.
 * Listeners are registered with ChatSession objects.
 */
public interface MessageListener
{
    /**
     * Calls when a new message has arrived.
     *
     * @param ses the ChatSession.
     * @param msg the incoming message.
     */
    public void onIncomingMessage(ChatSession ses, Message msg);

    /**
     * Calls when an error occurs to send a message.
     *
     * @param ses the ChatSession.
     * @param msg the message which was sent.
     * @param error the error information.
     */
    public void onSendMessageError(ChatSession ses, Message msg, ImErrorInfo error);

    /**
     * Called when a message receipt was received.
     * 
     * @param ses the ChatSession.
     * @param id the message ID.
     */
    public void onIncomingReceipt(ChatSession ses, String id);
    
    /**
     * Called when we determine that the remote supports message delivery receipts.
     * 
     * <br>XEP-0184
     * 
     * @param ses the ChatSession.
     */
    public void onReceiptsExpected(ChatSession ses);

    /** Called when OTR status changes */
	public void onStatusChanged(ChatSession session);
}
