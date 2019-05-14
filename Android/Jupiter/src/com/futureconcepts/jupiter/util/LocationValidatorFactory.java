package com.futureconcepts.jupiter.util;

import android.util.Log;

import com.futureconcepts.awt.geom.Point2D;
import com.futureconcepts.jupiter.Config;
import com.jhlabs.map.Ellipsoid;
import com.jhlabs.map.proj.TransverseMercatorProjection;

public class LocationValidatorFactory
{
	public static LocationValidator getLocationValidator(String format)
	{
		if (format.equals(Config.LOCATION_FORMAT_DEGREES))
		{
			return new WGS84();
		}
		else if (format.equals(Config.LOCATION_FORMAT_NAD27))
		{
			return new NAD27();
		}
		else
		{
			return null;
		}
	}

	public interface LocationValidator
	{
		Point2D.Double validate(String input);
	}

	private static class WGS84 implements LocationValidator
	{
		@Override
		public Point2D.Double validate(String input)
		{
			Point2D.Double value = new Point2D.Double();
			String[] parts = input.split(" ");
			value.y = Double.parseDouble(parts[0]);
			value.x = Double.parseDouble(parts[1]);
			return value;
		}
	}
	
	private static class NAD27 implements LocationValidator
	{
		@Override
		public Point2D.Double validate(String input)
		{
			Point2D.Double value = new Point2D.Double();
			Point2D.Double utmPoint = new Point2D.Double();
			String[] parts = input.split(" ");
			int zone = Integer.parseInt(parts[0]);
			if (parts.length == 3)
			{
				utmPoint.x = Double.parseDouble(parts[1]);
				utmPoint.y = Double.parseDouble(parts[2]);
			}
			else if (parts.length == 4)
			{
				utmPoint.x = Double.parseDouble(parts[2]);
				utmPoint.y = Double.parseDouble(parts[3]);
			}
			TransverseMercatorProjection utm = new TransverseMercatorProjection();
			utm.setEllipsoid(Ellipsoid.UTM_NAD27_ZONE11);
			utm.setUTMZone(zone);
			utm.inverseTransform(utmPoint, value);
			return value;
		}
	}
}
