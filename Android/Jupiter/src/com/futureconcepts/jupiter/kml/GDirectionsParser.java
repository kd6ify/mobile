package com.futureconcepts.jupiter.kml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.futureconcepts.jupiter.data.Placemark;
import com.futureconcepts.jupiter.data.Folder;
import com.futureconcepts.jupiter.data.Track;
import com.futureconcepts.jupiter.layers.StyleManager;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.PointF;
import android.net.Uri;
import android.util.Log;

public class GDirectionsParser
{
	private static String TAG = "KmlToRouteParser";
	private static int PLACEMARK_STATE_START = 0;
	private static int PLACEMARK_STATE_WAYPOINT = 1;
	private static int PLACEMARK_STATE_FINISH = 2;
	private static int PLACEMARK_STATE_ROUTE = 3;
	
	private Context _context;
	private String _routeId;
	private String _waypointId;
	private ContentValues _routeValues;
	private int _nextWaypointSequence = 0;
	private int _nextPathSequence = 0;
	private int _placemarkState;
	
	public GDirectionsParser(Context context)
	{
		_context = context;
		_routeValues = new ContentValues();
		_placemarkState = PLACEMARK_STATE_START;
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
					parseKml(xpp);
				}
			}
			eventType = xpp.next();
		}
		result = _context.getContentResolver().insert(Folder.CONTENT_URI, _routeValues);
		Log.d(TAG, "parse result: " + result.toString());
		return result;
	}

	private void parseKml(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("Document"))
				{
					parseDocument(xpp);
				}
			}
			eventType = xpp.next();
		}
	}
	
	private void parseDocument(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		_routeId = UUID.randomUUID().toString().toLowerCase();
		_routeValues.put(Folder.ID, _routeId);
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("name"))
				{
					_routeValues.put(Folder.NAME, xpp.nextText());
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
	}

	private void parseStyle(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("LineStyle"))
				{
					parseLineStyle(xpp);
				}
				else if (xpp.getName().equals("IconStyle"))
				{
					parseIconStyle(xpp);
				}
				else if (xpp.getName().equals("ListStyle"))
				{
					parseListStyle(xpp);
				}
			}
			eventType = xpp.next();
		}
	}

	private void parseLineStyle(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("color"))
				{
					xpp.nextText();
				}
				else if (xpp.getName().equals("width"))
				{
					xpp.nextText();
				}
			}
			eventType = xpp.next();
		}
	}

	private void parseIconStyle(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("Icon"))
				{
					parseIcon(xpp);
				}
				else if (xpp.getName().equals("scale"))
				{
					xpp.nextText();
				}
				else if (xpp.getName().equals("hotSpot"))
				{
					xpp.next();
				}
			}
			eventType = xpp.next();
		}
	}

	private void parseListStyle(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("ItemIcon"))
				{
					parseIcon(xpp);
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
			eventType = xpp.next();
		}
	}

	private void parsePlacemark(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		ContentValues values = new ContentValues();
		String description = null;
		_waypointId = xpp.getAttributeValue(null, "id");
		if (_waypointId == null)
		{
			_waypointId = UUID.randomUUID().toString().toLowerCase();
		}
		values.put(Placemark.ID, _waypointId);
		values.put(Placemark.TIME, _nextWaypointSequence++);
		values.put(Placemark.PARENT_ID, _routeId);
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("name"))
				{
					String name = xpp.nextText();
					if (name.contains("Arrive at"))
					{
						_placemarkState = PLACEMARK_STATE_FINISH;
					}
					values.put(Placemark.NAME, name);
				}
				else if (xpp.getName().equals("description"))
				{
					description = xpp.nextText();
					values.put(Placemark.DESCRIPTION, description);
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
					xpp.nextText();
				}
			}
			eventType = xpp.next();
		}
		if (_placemarkState == PLACEMARK_STATE_START)
		{
			values.put(Placemark.STYLE, StyleManager.STYLE_ROUTE_START);
			_placemarkState = PLACEMARK_STATE_WAYPOINT;
		}
		else if (_placemarkState == PLACEMARK_STATE_WAYPOINT)
		{
			values.put(Placemark.STYLE, StyleManager.STYLE_ROUTE_WAYPOINT);
		}
		else if (_placemarkState == PLACEMARK_STATE_FINISH)
		{
			values.put(Placemark.STYLE, StyleManager.STYLE_ROUTE_FINISH);
			_placemarkState = PLACEMARK_STATE_ROUTE;
		}
		else if (_placemarkState == PLACEMARK_STATE_ROUTE)
		{
			if (description != null)
			{
				_routeValues.put(Folder.DESCRIPTION, description);
			}
			values.put(Placemark.STYLE, StyleManager.STYLE_ROUTE_ROUTE);
		}
		_context.getContentResolver().insert(Placemark.CONTENT_URI, values);
	}
		
	private void parseStyleMap(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("Pair"))
				{
					parsePair(xpp);
				}
			}
			eventType = xpp.next();
		}
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
					xpp.nextText();
				}
				else if (xpp.getName().equals("latitude"))
				{
					xpp.nextText();
				}
				else if (xpp.getName().equals("range"))
				{
					xpp.nextText();
				}
				else if (xpp.getName().equals("tilt"))
				{
					xpp.nextText();
				}
				else if (xpp.getName().equals("heading"))
				{
					xpp.nextText();
				}
			}
			eventType = xpp.next();
		}
	}

	private void parsePair(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("key"))
				{
					xpp.nextText();
				}
				else if (xpp.getName().equals("Style"))
				{
					parseStyle(xpp);
				}
			}
			eventType = xpp.next();
		}
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
						ContentValues values = new ContentValues();
						String[] xyparts = parts[i].split(",");
						values.put(Track.PARENT_ID, _waypointId);
						values.put(Track.SEQUENCE, _nextPathSequence++);
						values.put(Track.LATITUDE, Float.parseFloat(xyparts[1]));
						values.put(Track.LONGITUDE, Float.parseFloat(xyparts[0]));
						_context.getContentResolver().insert(Track.CONTENT_URI, values);
					}
				}
			}
			eventType = xpp.next();
		}
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
