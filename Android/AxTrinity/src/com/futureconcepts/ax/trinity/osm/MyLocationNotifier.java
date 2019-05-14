package com.futureconcepts.ax.trinity.osm;

public interface MyLocationNotifier {

	void registerListener(MyLocationObserver listener);
	void unRegisterListener(MyLocationObserver listener);
	void notifyMyLocationHasChange();
}
