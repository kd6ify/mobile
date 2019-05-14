package com.futureconcepts.ax.sync.client;

import com.futureconcepts.ax.sync.client.ISyncTransactionListener;
import com.futureconcepts.ax.sync.client.SyncError;

interface ISyncTransaction
{
    void registerListener(ISyncTransactionListener listener);
    void unregisterListener(ISyncTransactionListener listener);
    void abort();
    
    String getAction();
    String getDataset();
    String getTable();
 	SyncError getError();
}
