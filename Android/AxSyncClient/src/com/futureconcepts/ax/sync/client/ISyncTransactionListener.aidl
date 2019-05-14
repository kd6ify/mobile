package com.futureconcepts.ax.sync.client;

import com.futureconcepts.ax.sync.client.SyncError;

oneway interface ISyncTransactionListener
{
	void onActionChanged(String action);
	void onDatasetChanged(String dataset);
	void onTableChanged(String table);
	void onStatusChanged(String status);
	void onServerFetch();
	void onServerFetchDone();
	void onProgress(int position, int count);
	void onError(in SyncError error);
}
