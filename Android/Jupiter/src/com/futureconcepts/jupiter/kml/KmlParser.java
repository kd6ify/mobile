package com.futureconcepts.jupiter.kml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.UUID;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.futureconcepts.jupiter.data.Placemark;
import com.futureconcepts.jupiter.data.Folder;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.net.Uri;
import android.util.Log;

public class KmlParser
{
	private static String TAG = "KmlRouteParser";
	
	private Context _context;
	private String _routeId;
	private StyleMap _styleMap = new StyleMap();
	
	public KmlParser(Context context)
	{
		_context = context;
	}

	public Uri parse(InputStream inputStream) throws XmlPullParserException, IOException
	{
		Uri result = null;
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		XmlPullParser xpp = factory.newPullParser();
		xpp.setInput(new InputStreamReader(inputStream));
		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("kml"))
				{
					result = parseKml(xpp);
				}
			}
			eventType = xpp.next();
		}
		return result;
	}

	private Uri parseKml(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		Uri result = null;
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("Document"))
				{
					result = parseDocument(xpp);
				}
			}
			eventType = xpp.next();
		}
		return result;
	}
	
	private Uri parseDocument(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		ContentValues values = new ContentValues();
		_routeId = UUID.randomUUID().toString().toLowerCase();
		values.put(Folder.ID, _routeId);
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("name"))
				{
					values.put(Folder.NAME, xpp.nextText());
				}
				else if (xpp.getName().equals("Style"))
				{
					parseStyle(xpp);
				}
				else if (xpp.getName().equals("Snippet"))
				{
					parseSnippet(xpp);
				}
				else if (xpp.getName().equals("Placemark"))
				{
					parsePlacemark(xpp);
				}
			}
			eventType = xpp.next();
		}
		return _context.getContentResolver().insert(Folder.CONTENT_URI, values);
	}

	private Style parseStyle(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		String id = xpp.getAttributeValue(null, "id");
		Style style = null;
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("LineStyle"))
				{
					style = parseLineStyle(id, xpp);
				}
				else if (xpp.getName().equals("IconStyle"))
				{
					style = parseIconStyle(id, xpp);
				}
				else if (xpp.getName().equals("ListStyle"))
				{
					parseListStyle(id, xpp);
				}
			}
			eventType = xpp.next();
		}
		if (id != null)
		{
			_styleMap.add("#"+id, style);
		}
		return style;
	}

	private Style parseLineStyle(String id, XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		LineStyle style = new LineStyle();
		style.id = id;
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("color"))
				{
					style.color = Color.parseColor("#"+xpp.nextText());
				}
				else if (xpp.getName().equals("width"))
				{
					style.width = Integer.parseInt(xpp.nextText());
				}
			}
			eventType = xpp.next();
		}
		return style;
	}

	private IconStyle parseIconStyle(String id, XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		IconStyle style = new IconStyle();
		style.id = id;
		style.scale = 1.0f;
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("Icon"))
				{
		//			style.icon = parseIcon(xpp);
				}
				else if (xpp.getName().equals("scale"))
				{
					style.scale = Float.parseFloat(xpp.nextText());
				}
				else if (xpp.getName().equals("hotSpot"))
				{
					style.hotSpotX = Float.parseFloat(xpp.getAttributeValue(null, "x"));
					style.hotSpotY = Float.parseFloat(xpp.getAttributeValue(null, "y"));
					style.xunits = xpp.getAttributeValue(null, "xunits");
					style.yunits = xpp.getAttributeValue(null, "yunits");
					xpp.next();
				}
			}
			eventType = xpp.next();
		}
		return style;
	}

	private void parseListStyle(String id, XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("ItemIcon"))
				{
				//	parseIcon(xpp);
				}
			}
			eventType = xpp.next();
		}
	}

	private void parseIcon(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("href"))
				{
					xpp.nextText();
				}
			}
			eventType = xpp.next();
		}
	}

	private void parseSnippet(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			String snippet = xpp.getText();
			Log.d(TAG, "snippet=" + snippet);
			eventType = xpp.next();
		}
	}

	private void parsePlacemark(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		ContentValues values = new ContentValues();
		values.put(Placemark.ID, xpp.getAttributeValue(null, "id"));
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("name"))
				{
					values.put(Placemark.NAME, xpp.nextText());
				}
				else if (xpp.getName().equals("description"))
				{
					values.put(Placemark.DESCRIPTION, xpp.nextText());
				}
				else if (xpp.getName().equals("address"))
				{
					values.put(Placemark.ADDRESS, xpp.nextText());
				}
				else if (xpp.getName().equals("StyleMap"))
				{
					parseStyleMap(xpp);
				}
				else if (xpp.getName().equals("Point"))
				{
					PointF point = parsePoint(xpp);
					values.put(Placemark.LATITUDE, point.y);
					values.put(Placemark.LONGITUDE, point.x);
				}
				else if (xpp.getName().equals("LookAt"))
				{
					parseLookAt(xpp);
				}
				else if (xpp.getName().equals("GeometryCollection"))
				{
					parseMultiGeometry(xpp);
				}
				else if (xpp.getName().equals("styleUrl"))
				{
					StyleSelector styleSelector = _styleMap.get(xpp.nextText());
				}
			}
			eventType = xpp.next();
		}
	}

	private StyleMap parseStyleMap(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		StyleMap result = new StyleMap();
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("Pair"))
				{
					Pair pair = parsePair(xpp);
					result.add(pair.getKey(), pair.getStyle());
				}
			}
			eventType = xpp.next();
		}
		return result;
	}

	private PointF parsePoint(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		PointF result = new PointF();
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("coordinates"))
				{
					String coordinates = xpp.nextText();
					String[] parts = coordinates.split(",");
					result.x = Float.parseFloat(parts[0]);
					result.y = Float.parseFloat(parts[1]);
				}
			}
			eventType = xpp.next();
		}
		return result;
	}

	private void parseLookAt(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("longitude"))
				{
					float longitude = Float.parseFloat(xpp.nextText());
					Log.d(TAG, "lookAt longitude=" + longitude);
				}
				else if (xpp.getName().equals("latitude"))
				{
					float latitude = Float.parseFloat(xpp.nextText());
					Log.d(TAG, "lookAt latitude=" + latitude);
				}
				else if (xpp.getName().equals("range"))
				{
					float range = Float.parseFloat(xpp.nextText());
					Log.d(TAG, "lookAt range=" + range);
				}
				else if (xpp.getName().equals("tilt"))
				{
					float tilt = Float.parseFloat(xpp.nextText());
					Log.d(TAG, "lookAt tilt=" + tilt);
				}
				else if (xpp.getName().equals("heading"))
				{
					float heading = Float.parseFloat(xpp.nextText());
					Log.d(TAG, "lookAt heading=" + heading);
				}
			}
			eventType = xpp.next();
		}
	}

	private Pair parsePair(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		Pair result = new Pair();
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("key"))
				{
					result.setKey(xpp.nextText());
				}
				else if (xpp.getName().equals("Style"))
				{
					result.setStyle(parseStyle(xpp));
				}
			}
			eventType = xpp.next();
		}
		return result;
	}

	private void parseMultiGeometry(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("LineString"))
				{
					parseLineString(xpp);
				}
			}
			eventType = xpp.next();
		}
	}

	private void parseLineString(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("coordinates"))
				{
					String coordinates = xpp.nextText();
					String[] parts = coordinates.split(" ");
					for (int i = 0; i < parts.length; i++)
					{
						String[] xyparts = parts[i].split(",");
						PointF point = new PointF();
						point.x = Float.parseFloat(xyparts[0]);
						point.y = Float.parseFloat(xyparts[1]);
					}
				}
			}
			eventType = xpp.next();
		}
	}
	
	private class Pair
	{
		private String _key;
		private Style _style;
		
		public void setKey(String value)
		{
			_key = value;
		}
		
		public String getKey()
		{
			return _key;
		}
		
		public void setStyle(Style value)
		{
			_style = value;
		}
		
		public Style getStyle()
		{
			return _style;
		}
	}
	
	public static class StyleSelector
	{
		public String id;
	}
	
	public static class Style extends StyleSelector
	{
	}

	public static class ColorStyle extends Style
	{
		public int color;
		public String colorMode;
	}
	
	public static class LineStyle extends ColorStyle
	{
		public int width;
	}	

	public static class IconStyle extends ColorStyle
	{
		public float scale;
		public int heading;
		public float hotSpotX;
		public float hotSpotY;
		public String xunits;
		public String yunits;
	}
	
	public static class StyleMap extends StyleSelector
	{
		private Hashtable<String, Style> _map = new Hashtable<String, Style>();
		
		public void add(String key, Style style)
		{
			_map.put(key, style);
		}
		
		public Style get(String key)
		{
			return _map.get(key);
		}
	}
	
	public static class Feature
	{
		public String id;
		public String name;
		public String address;
		public String description;
		public StyleSelector styleSelector;
	}
	
	public double distanceInMeters(double lat1, double lon1, double lat2, double lon2)
	{	
		double a1 = Math.toRadians(lat1);
		double a2 = Math.toRadians(lon1);
		double b1 = Math.toRadians(lat2);
		double b2 = Math.toRadians(lon2);

		double cosa1 = Math.cos(a1);
		double cosb1 = Math.cos(b1);
		
		double t1 = cosa1 * Math.cos(a2) * cosb1 * Math.cos(b2);
		
		double t2 = cosa1 * Math.sin(a2) * cosb1 * Math.sin(b2);

		double t3 = Math.sin(a1) * Math.sin(b1);

		double tt = Math.acos( t1 + t2 + t3 );

		return 6378140.0d * tt; // constant is radius of earth at equator in meters
	}
}
