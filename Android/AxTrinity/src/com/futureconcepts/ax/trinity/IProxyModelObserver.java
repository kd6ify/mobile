package com.futureconcepts.ax.trinity;

public interface IProxyModelObserver
{
	void onDataReady();
	
	void onError(String message);
}
