package com.futureconcepts.ax.trinity.osm;

public class BombData {
	private String Name;
	private float MandatoryEvacuationDistance;//in feet
	private float ShelterInPlaceZone;//in feet
	private float PreferredDistance;
//	<Name>Semi-Trailer (60000 lbs)</Name>
//	  <MandatoryEvacuationDistance>1570</MandatoryEvacuationDistance>
//	  <ShelterInPlaceZone>9299</ShelterInPlaceZone>
//	  <PreferredDistance>9300</PreferredDistance>
	/**
	 * @return the name
	 */
	public String getName() {
		return Name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		Name = name;
	}
	/**
	 * @return the mandatoryEvacuationDistance
	 */
	public float getMandatoryEvacuationDistance() {
		return MandatoryEvacuationDistance;
	}
	/**
	 * @param mandatoryEvacuationDistance the mandatoryEvacuationDistance to set
	 */
	public void setMandatoryEvacuationDistance(float mandatoryEvacuationDistance) {
		MandatoryEvacuationDistance = mandatoryEvacuationDistance;
	}
	/**
	 * @return the shelterInPlaceZone
	 */
	public float getShelterInPlaceZone() {
		return ShelterInPlaceZone;
	}
	/**
	 * @param shelterInPlaceZone the shelterInPlaceZone to set
	 */
	public void setShelterInPlaceZone(float shelterInPlaceZone) {
		ShelterInPlaceZone = shelterInPlaceZone;
	}
	/**
	 * @return the preferredDistance
	 */
	public float getPreferredDistance() {
		return PreferredDistance;
	}
	/**
	 * @param preferredDistance the preferredDistance to set
	 */
	public void setPreferredDistance(float preferredDistance) {
		PreferredDistance = preferredDistance;
	}
}
