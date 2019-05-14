package com.futureconcepts.jupiter.util;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.location.Location;

import com.futureconcepts.awt.geom.Point2D;
import com.futureconcepts.awt.geom.Point2D.Double;
import com.futureconcepts.jupiter.Config;
import com.jhlabs.map.Ellipsoid;
import com.jhlabs.map.proj.TransverseMercatorProjection;

public class FormatterFactory
{	
	private Config _config;
	
	/**
	 * The token to display to the user first (left in LTR, right in RTL cultures)
	 */
	public final static int LOCATION_TOKEN_FIRST = 0;
	/**
	 * The token to display to the user second (left in LTR, right in RTL cultures)
	 */
	public final static int LOCATION_TOKEN_SECOND = 1;
	/**
	 * Any additional token to display after the SECOND token
	 */
	public final static int LOCATION_TOKEN_EXTRA = 2;
	
	public interface ScalarFormatter
	{
		String format(double value);
	}
	public interface LocationFormatter
	{
		String format(Point2D.Double value);
	}
	public interface LocationTokenFormatter
	{
		void format(Point2D.Double value);
		
		boolean hasToken(int tokenType);
		String getLabel(int tokenType);
		String getValue(int tokenType);
	}
		
	private abstract class AbstractLocationTokenFormatter implements LocationTokenFormatter
	{	
		protected Map<Integer,String> labels = new HashMap<Integer,String>();
		protected Map<Integer,String> values = new HashMap<Integer,String>();

		public abstract void format(Point2D.Double value);
		
		@Override
		public boolean hasToken(int tokenType)
		{
			return values.containsKey(new Integer(tokenType));
		}
		
		@Override
		public String getLabel(int tokenType)
		{
			return labels.get(new Integer(tokenType));
		}
		
		@Override
		public String getValue(int tokenType)
		{
			return values.get(new Integer(tokenType));
		}
	}


	public FormatterFactory(Context context)
	{
		_config = Config.getInstance(context);
	}
	
	public ScalarFormatter getDistanceFormatter()
	{
		return getDistanceFormatter(_config.getDistanceFormat());
	}
	
	public ScalarFormatter getDistanceFormatter(String units)
	{	
		if (units.equals(Config.DISTANCE_FORMAT_METERS))
		{
			return new ScalarFormatter() {
				@Override
				public String format(double meters)
				{
					return String.format("%.2f m", meters);
				}
			};
		}
		else if (units.equals(Config.DISTANCE_FORMAT_FEET))
		{
			return new ScalarFormatter() {
				@Override
				public String format(double meters)
				{
					return String.format("%.2f ft", meters * 3.2808398950131235);
				}
			};
		}
		else if (units.equals(Config.DISTANCE_FORMAT_MILES))
		{
			return new ScalarFormatter() {
				@Override
				public String format(double meters)
				{
					return String.format("%.2f mi", meters / 1609.344);
				}
			};
		}
		else
		{
			throw new IllegalArgumentException("DistanceFormat");
		}
	}
	
	public ScalarFormatter getVelocityFormatter()
	{
		return getVelocityFormatter(_config.getVelocityFormat());
	}
	
	public ScalarFormatter getVelocityFormatter(String units)
	{
		if (units.equals(Config.VELOCITY_FORMAT_KPH))
		{
			return new ScalarFormatter() {
				@Override
				public String format(double meters_per_second)
				{
					return String.format("%.1f kph", meters_per_second * 3.6);
				}
			};
		}
		else if (units.equals(Config.VELOCITY_FORMAT_MPH))
		{
			return new ScalarFormatter() {
				@Override
				public String format(double meters_per_second)
				{
					return String.format("%.1f mph", meters_per_second * 2.2369362920544025d);
				}
			};
		}
		else if (units.equals(Config.VELOCITY_FORMAT_METERS_PER_SECOND))
		{
			return new ScalarFormatter() {
				@Override
				public String format(double meters_per_second)
				{
					return String.format("%.4f m/s", meters_per_second);
				}
			};
		}
		else
		{
			throw new IllegalArgumentException("VelocityFormat");
		}
	}

	public LocationFormatter getLocationFormatter()
	{
		return getLocationFormatter(_config.getLocationFormat());
	}
	
	public LocationFormatter getLocationFormatter(String format)
	{
		if (format.equals(Config.LOCATION_FORMAT_DEGREES))
		{
			return new LocationFormatter() {
				@Override
				public String format(Point2D.Double value)
				{
					StringBuilder sb = new StringBuilder();
					sb.append(Location.convert(value.y, Location.FORMAT_DEGREES));
					sb.append(" ");
					sb.append(Location.convert(value.x, Location.FORMAT_DEGREES));
					return sb.toString();
				}
			};
		}
		else if (format.equals(Config.LOCATION_FORMAT_MINUTES))
		{
			return new LocationFormatter() {
				@Override
				public String format(Point2D.Double value)
				{
					StringBuilder sb = new StringBuilder();
					sb.append(Location.convert(value.y, Location.FORMAT_MINUTES));
					sb.append(" ");
					sb.append(Location.convert(value.x, Location.FORMAT_MINUTES));
					return sb.toString();
				}
			};
		}
		else if (format.equals(Config.LOCATION_FORMAT_SECONDS))
		{
			return new LocationFormatter() {
				@Override
				public String format(Point2D.Double value)
				{
					StringBuilder sb = new StringBuilder();
					sb.append(Location.convert(value.y, Location.FORMAT_SECONDS));
					sb.append(" ");
					sb.append(Location.convert(value.x, Location.FORMAT_SECONDS));
					return sb.toString();
				}
			};
		}
		else if (format.equals(Config.LOCATION_FORMAT_NAD27))
		{
			return new LocationFormatter() {
			    private static final String _letterMap = "_ABCDEFGHJKLMNPQRSTUVWXYZ";
				@Override
				public String format(Point2D.Double value)
				{
					StringBuilder sb = new StringBuilder();
					TransverseMercatorProjection utm = new TransverseMercatorProjection();
					utm.setEllipsoid(Ellipsoid.UTM_NAD27_ZONE11);
					int zone = utm.getZoneFromNearestMeridian(Math.toRadians(value.x));
					sb.append(zone);
					sb.append(" ");
					sb.append(_letterMap.charAt(utm.getRowFromNearestParallel(Math.toRadians(value.y))));
					sb.append(" ");
					utm.setUTMZone(zone);
					Point2D.Double dst = new Point2D.Double();
					utm.transform(value, dst);
					sb.append(String.format("%07d", (int)Math.floor(dst.x)));
					sb.append(" ");
					sb.append(String.format("%07d", (int)Math.floor(dst.y)));
					return sb.toString();
				}
			};
		}
		else
		{
			throw new IllegalArgumentException("LocationFormatter");
		}
	}
	
	public LocationTokenFormatter getLocationTokenFormatter()
	{
		return getLocationTokenFormatter(_config.getLocationFormat());
	}
	
	public LocationTokenFormatter getLocationTokenFormatter(String format)
	{
		if (format.equals(Config.LOCATION_FORMAT_DEGREES))
		{
			return new AbstractLocationTokenFormatter() {		
				@Override
				public void format(Double value)
				{
					labels.clear();
					values.clear();
				
					labels.put(FormatterFactory.LOCATION_TOKEN_FIRST, "Latitude");
					values.put(FormatterFactory.LOCATION_TOKEN_FIRST, Location.convert(value.y, Location.FORMAT_DEGREES));
				
					labels.put(FormatterFactory.LOCATION_TOKEN_SECOND, "Longitude");
					values.put(FormatterFactory.LOCATION_TOKEN_SECOND, Location.convert(value.x, Location.FORMAT_DEGREES));
				}
			};
		}
		else if (format.equals(Config.LOCATION_FORMAT_MINUTES))
		{
			return new AbstractLocationTokenFormatter() {
				@Override
				public void format(Point2D.Double value)
				{
					labels.clear();
					values.clear();
					
					labels.put(FormatterFactory.LOCATION_TOKEN_FIRST, "Latitude");
					values.put(FormatterFactory.LOCATION_TOKEN_FIRST, Location.convert(value.y, Location.FORMAT_MINUTES));
					
					labels.put(FormatterFactory.LOCATION_TOKEN_SECOND, "Longitude");
					values.put(FormatterFactory.LOCATION_TOKEN_SECOND, Location.convert(value.x, Location.FORMAT_MINUTES));
				}
			};
		}
		else if (format.equals(Config.LOCATION_FORMAT_SECONDS))
		{
			return new AbstractLocationTokenFormatter() {
				@Override
				public void format(Point2D.Double value)
				{
					labels.clear();
					values.clear();
					
					labels.put(FormatterFactory.LOCATION_TOKEN_FIRST, "Latitude");
					values.put(FormatterFactory.LOCATION_TOKEN_FIRST, Location.convert(value.y, Location.FORMAT_SECONDS));
					
					labels.put(FormatterFactory.LOCATION_TOKEN_SECOND, "Longitude");
					values.put(FormatterFactory.LOCATION_TOKEN_SECOND, Location.convert(value.x, Location.FORMAT_SECONDS));
				}
			};
		}
		else if (format.equals(Config.LOCATION_FORMAT_NAD27))
		{
			return new AbstractLocationTokenFormatter() {
			    private static final String _letterMap = "_ABCDEFGHJKLMNPQRSTUVWXYZ";
				@Override
				public void format(Point2D.Double value)
				{
					labels.clear();
					values.clear();
					
					TransverseMercatorProjection utm = new TransverseMercatorProjection();
					utm.setEllipsoid(Ellipsoid.UTM_NAD27_ZONE11);
					int zone = utm.getZoneFromNearestMeridian(Math.toRadians(value.x));
					utm.setUTMZone(zone);
					
					Point2D.Double dst = new Point2D.Double();
					utm.transform(value, dst);

					labels.put(FormatterFactory.LOCATION_TOKEN_FIRST, "Easting");
					values.put(FormatterFactory.LOCATION_TOKEN_FIRST, String.format("%07d", (int)Math.floor(dst.x)));
					
					labels.put(FormatterFactory.LOCATION_TOKEN_SECOND, "Northing");
					values.put(FormatterFactory.LOCATION_TOKEN_SECOND, String.format("%07d", (int)Math.floor(dst.y)));
					
					labels.put(FormatterFactory.LOCATION_TOKEN_EXTRA, "Zone");
					values.put(FormatterFactory.LOCATION_TOKEN_EXTRA, zone + " " + _letterMap.charAt(utm.getRowFromNearestParallel(Math.toRadians(value.y))));
				}
			};
		}
		else
		{
			throw new IllegalArgumentException("LocationTokenFormatter");
		}
	}
}
