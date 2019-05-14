package com.futureconcepts.drake.client;

oneway interface IFileTransferListener
{
    /**
     * Called when a new file transfer request is received.
     *
     * @param id the id of the file transfer request in the content provider.
     */
    void onFileTransferRequest(long dbid);

	/** this method is called when a file transfer status has changed.
	*/
	void onFileTransferStatusChanged(long dbid);
}
