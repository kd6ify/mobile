package com.futureconcepts.jupiter.compass;

import android.location.GpsSatellite;

/**
 * Grabs a snapshot of the values in a GpsSatellite
 * @author kdixon
 */
public class GpsSatStatsSnapshot
{
	private float azimuth, elevation, snr;
	private int prn;
	private boolean almanac, ephemeris, fix;
	
	public GpsSatStatsSnapshot(GpsSatellite source)
	{
		azimuth = source.getAzimuth();
		elevation = source.getElevation();
		snr = source.getSnr();
		
		prn = source.getPrn();
		
		almanac = source.hasAlmanac();
		ephemeris = source.hasEphemeris();
		fix = source.usedInFix();
	}
	
	/**
	 * @return the azimuth of the satellite in degrees.
	 */
	public float getAzimuth()
	{
		return this.azimuth;
	}
	 
	/**
	 * @return the elevation of the satellite in degrees.
	 */
	public float getElevation()
	{
		return this.elevation;
	}

	/**
	 * @return the PRN (pseudo-random number) for the satellite.
	 */
	public int getPrn()
	{
		return this.prn;
	}
	 
	/**
	 * @return the signal to noise ratio for the satellite.
	 */
	public float getSnr()
	{
		return this.snr;
	}

	/**
	 * @return true if the GPS engine has almanac data for the satellite.
	 */
	public boolean hasAlmanac()
	{
		return this.almanac;
	}
	
	/**
	 * @return true if the GPS engine has ephemeris data for the satellite.
	 */
	public boolean hasEphemeris()
	{
		return this.ephemeris;
	}

	/**
	 * @return true if the satellite was used by the GPS engine when calculating the most recent GPS fix.
	 */
	public boolean usedInFix()
	{
		return this.fix;
	}
}
