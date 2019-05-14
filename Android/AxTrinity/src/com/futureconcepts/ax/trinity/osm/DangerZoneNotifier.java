package com.futureconcepts.ax.trinity.osm;

public interface DangerZoneNotifier {
	void registerDangerListener(DangerZone addListener);
	void unRegisterDangerListener(DangerZone removeListener);
	void notifyDangerZone(boolean isDangerZone);

}
