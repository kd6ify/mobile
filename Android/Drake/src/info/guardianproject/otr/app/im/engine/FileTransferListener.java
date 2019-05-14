package info.guardianproject.otr.app.im.engine;

import com.futureconcepts.drake.client.FileTransferParcel;
import com.futureconcepts.drake.client.FileTransferRequestParcel;

/**
 * Interface that allows the implementing class to listen to invitation from
 * other users.
 */
public interface FileTransferListener
{
    public void onFileTransferRequest(FileTransferRequestParcel fileTransferRequestParcel);
    public void onFileTransferStatusChanged(FileTransferParcel fileTransferParcel);
}
