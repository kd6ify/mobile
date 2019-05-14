package info.guardianproject.otr.app.im.engine;

import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;

import com.futureconcepts.drake.client.FileTransferParcel;
import com.futureconcepts.drake.client.FileTransferRequestParcel;

/**
 * The FileTransferManager keeps track of all file transfers.
 * 
 */
public abstract class EngineFileTransferManager
{
    private FileTransferListener _listener;

    /**
     * Registers a FileTransferListener with the FileTransferManager
     *
     * @param listener the listener
     */
    public void setFileTransferListener(FileTransferListener listener)
    {
    	_listener = listener;
    }

    /**
     * Notifies the FileTransferListener
     *
     */
    public synchronized void notifyFileTransferRequest(FileTransferRequestParcel requestParcel)
    {
        if (_listener != null)
        {
        	_listener.onFileTransferRequest(requestParcel);
        }
    }

    /**
     * Notifies the Listener that the filetransfer status has changed
     * 
     */
    public synchronized void notifyFileTransferStatusChanged(FileTransferParcel fileTransferParcel)
    {
        if (_listener != null)
        {
        	_listener.onFileTransferStatusChanged(fileTransferParcel);
        }
    }
    
    public abstract FileTransferRequestParcel getFileTransferRequest(String streamID);
    
    public abstract FileTransferParcel getFileTransferStatus(String streamID);
    
    /**
     * accept file transfer request.
     *
     * @param id the original file transfer request id (content provider row id)
     */
    public abstract void acceptFileTransferRequest(String requestId);

    /**
     * reject file transfer request.
     *
     * @param id the original file transfer request id (content provider row id)
     */
    public abstract void rejectFileTransferRequest(String requestId);

    /**
     * Sends a file asynchronously.
     *
     * @param path the file to send.
     */
    public abstract void sendFileAsync(ChatSession session, String path);
}
