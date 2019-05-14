package com.futureconcepts.ax.sync.client;

import android.net.Uri;

import com.futureconcepts.ax.sync.client.ISyncListener;
import com.futureconcepts.ax.sync.client.ISyncTransaction;

interface ISyncManager
{
    void registerSyncListener(ISyncListener listener);
    void unregisterSyncListener(ISyncListener listener);
    
    ISyncTransaction getCurrentTransaction();
    
    void setCurrentIncidentID(String incidentID);
    String getCurrentIncidentID();
    
    void startSyncing();
    void syncDataset(String datasetName);
    void deleteDataset(String datasetName);
    void dropDataset(String datasetName);
    void uploadInsert(in Uri uri);
}
