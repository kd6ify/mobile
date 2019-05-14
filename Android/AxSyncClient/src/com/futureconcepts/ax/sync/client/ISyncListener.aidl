package com.futureconcepts.ax.sync.client;

import com.futureconcepts.ax.sync.client.ISyncTransaction;

oneway interface ISyncListener
{
	void onStart();
	void onRescheduled();
	void onStop();
	void onTransaction(ISyncTransaction transaction);
	void onTransactionComplete(ISyncTransaction transaction);
}
