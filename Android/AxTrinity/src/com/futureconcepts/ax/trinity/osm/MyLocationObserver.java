package com.futureconcepts.ax.trinity.osm;

import android.location.Location;

public interface MyLocationObserver {
	void locationHasChange(boolean snapToLocationEnabled,Location location);
}
