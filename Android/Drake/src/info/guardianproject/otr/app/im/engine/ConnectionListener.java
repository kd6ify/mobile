package info.guardianproject.otr.app.im.engine;

import com.futureconcepts.drake.client.ImErrorInfo;

/**
 * Interface that allows the implementing classes to listen to connection
 * relative events. Listeners are registered with ImConnection.
 */
public interface ConnectionListener
{
    /**
     * Called when the connection's state has changed.
     *
     * @param state
     *        the new state of the connection.
     * @param error
     *        the error which caused the state change or <code>null</code>
     *        it's a normal state change.
     */
    public void onStateChanged(int state, ImErrorInfo error);

    public void onUserPresenceUpdated();

    public void onUpdatePresenceError(ImErrorInfo error);
}
