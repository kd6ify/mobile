package com.futureconcepts.drake.client;

import com.futureconcepts.drake.client.IFileTransferListener;
import com.futureconcepts.drake.client.FileTransferParcel;
import com.futureconcepts.drake.client.FileTransferRequestParcel;

interface IFileTransferManager
{
    void registerFileTransferListener(IFileTransferListener listener);
    void unregisterFileTransferListener(IFileTransferListener listener);

    /**
     * Sends a file to all participants in this ChatSession.
     */
    void sendFile(String path);

    /**
     * Accept a pending file transfer request
     *
     */
    void acceptFileTransferRequest(long dbid);

    /**
     * Reject a pending file transfer request
     *
     */
    void rejectFileTransferRequest(long dbid);
}
