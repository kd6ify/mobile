package com.futureconcepts.ax.trinity.osm;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.util.Log;

public class ChemicalSpillData
{
	private boolean IsEquipmentWeather;
	private String WeatherId;
	private String GuideID;
	private boolean IsCompressed;
	private String Name;
	private int GuideNo;
	private float SmallIIZM;
	private float SmallPadDayKm;
	private float SmallPadNightKm;
	private float LargeIIZM;
	private float LargePADDayKm;
	private float LargePadNightKm;
	private boolean FireEvacuation;
	private String LastModified;
	private boolean IsLargeSpill;
	private int WindDirection;
	
	public void printAllValues()
	{
		Log.e("ChemicalSpillData", "WindDirection: "+WindDirection);
		Log.e("ChemicalSpillData", "Name: "+Name);
		Log.e("ChemicalSpillData", "IsLargeSpill: "+IsLargeSpill);
		Log.e("ChemicalSpillData", "LargeIIZM: "+LargeIIZM);
		Log.e("ChemicalSpillData", "LargePADDayKm: "+LargePADDayKm);		
		Log.e("ChemicalSpillData", "LargePadNightKm: "+LargePadNightKm);
		Log.e("ChemicalSpillData", "LargeIIZM: "+SmallIIZM);
		Log.e("ChemicalSpillData", "LargePADDayKm: "+SmallPadDayKm);		
		Log.e("ChemicalSpillData", "LargePadNightKm: "+SmallPadNightKm);
	}
	
	public  float getChemicalSpillArea()
	{
		if(getIsLargeSpill())
		{
			return getLargeIIZM();
		}else
		{
			return getSmallIIZM();
		}
	}
	
	public  float getChemicalProactiveDistance()
	{
		Calendar cal = Calendar.getInstance();
    	cal.getTime();
    	SimpleDateFormat sdf = new SimpleDateFormat("HH");
    	int currentTime =Integer.parseInt(sdf.format(cal.getTime()));
		if(getIsLargeSpill())
		{
			if(currentTime>=17 || currentTime<=5 )
			{
				return getLargePadNightKm();
				
			}
			return getLargePADDayKm();
		}else
		{
			if(currentTime>=17 || currentTime<=5 )
			{
				return getSmallPadNightKm();
			}
			return getSmallPadDayKm();
		}
	}
	

	/**
	 * @return the isEquipmentWeather
	 */
	public boolean getIsEquipmentWeather() {
		return IsEquipmentWeather;
	}
	/**
	 * @param isEquipmentWeather the isEquipmentWeather to set
	 */
	public void setIsEquipmentWeather(boolean isEquipmentWeather) {
		IsEquipmentWeather = isEquipmentWeather;
	}
	/**
	 * @return the weatherId
	 */
	public String getWeatherId() {
		return WeatherId;
	}
	/**
	 * @param weatherId the weatherId to set
	 */
	public void setWeatherId(String weatherId) {
		WeatherId = weatherId;
	}
	/**
	 * @return the iD
	 */
	public String getGuideID() {
		return GuideID;
	}
	/**
	 * @param iD the iD to set
	 */
	public void setGuideID(String iD) {
		GuideID = iD;
	}
	/**
	 * @return the isCompressed
	 */
	public boolean getIsCompressed() {
		return IsCompressed;
	}
	/**
	 * @param isCompressed the isCompressed to set
	 */
	public void setIsCompressed(boolean isCompressed) {
		IsCompressed = isCompressed;
	}
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
	 * @return the guideNo
	 */
	public int getGuideNo() {
		return GuideNo;
	}
	/**
	 * @param guideNo the guideNo to set
	 */
	public void setGuideNo(int guideNo) {
		GuideNo = guideNo;
	}
	/**
	 * @return the smallIIZM
	 */
	public float getSmallIIZM() {
		return SmallIIZM;
	}
	/**
	 * @param smallIIZM the smallIIZM to set
	 */
	public void setSmallIIZM(float smallIIZM) {
		SmallIIZM = smallIIZM;
	}
	/**
	 * @return the smallPadDayKm
	 */
	public float getSmallPadDayKm() {
		return SmallPadDayKm;
	}
	/**
	 * @param smallPadDayKm the smallPadDayKm to set
	 */
	public void setSmallPadDayKm(float smallPadDayKm) {
		SmallPadDayKm = smallPadDayKm;
	}
	/**
	 * @return the smallPadNightKm
	 */
	public float getSmallPadNightKm() {
		return SmallPadNightKm;
	}
	/**
	 * @param smallPadNightKm the smallPadNightKm to set
	 */
	public void setSmallPadNightKm(float smallPadNightKm) {
		SmallPadNightKm = smallPadNightKm;
	}
	/**
	 * @return the largeIIZM
	 */
	public float getLargeIIZM() {
		return LargeIIZM;
	}
	/**
	 * @param largeIIZM the largeIIZM to set
	 */
	public void setLargeIIZM(float largeIIZM) {
		LargeIIZM = largeIIZM;
	}
	/**
	 * @return the largePADDayKm
	 */
	public float getLargePADDayKm() {
		return LargePADDayKm;
	}
	/**
	 * @param largePADDayKm the largePADDayKm to set
	 */
	public void setLargePADDayKm(float largePADDayKm) {
		LargePADDayKm = largePADDayKm;
	}
	/**
	 * @return the largePadNightKm
	 */
	public float getLargePadNightKm() {
		return LargePadNightKm;
	}
	/**
	 * @param largePadNightKm the largePadNightKm to set
	 */
	public void setLargePadNightKm(float largePadNightKm) {
		LargePadNightKm = largePadNightKm;
	}
	/**
	 * @return the fireEvacuation
	 */
	public boolean getFireEvacuation() {
		return FireEvacuation;
	}
	/**
	 * @param fireEvacuation the fireEvacuation to set
	 */
	public void setFireEvacuation(boolean fireEvacuation) {
		FireEvacuation = fireEvacuation;
	}
	/**
	 * @return the lastModified
	 */
	public String getLastModified() {
		return LastModified;
	}
	/**
	 * @param lastModified the lastModified to set
	 */
	public void setLastModified(String lastModified) {
		LastModified = lastModified;
	}
	/**
	 * @return the isLargeSpill
	 */
	public boolean getIsLargeSpill() {
		return IsLargeSpill;
	}
	/**
	 * @param isLargeSpill the isLargeSpill to set
	 */
	public void setIsLargeSpill(boolean isLargeSpill) {
		IsLargeSpill = isLargeSpill;
	}
	/**
	 * @return the windDirection
	 */
	public int getWindDirection() {
		return WindDirection;
	}
	/**
	 * @param windDirection the windDirection to set
	 */
	public void setWindDirection(int windDirection) {
		WindDirection = windDirection;
	}	
	
}
