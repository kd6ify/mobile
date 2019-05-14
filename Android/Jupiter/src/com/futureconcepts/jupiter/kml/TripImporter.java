package com.futureconcepts.jupiter.kml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.futureconcepts.jupiter.data.Placemark;
import com.futureconcepts.jupiter.data.Folder;
import com.futureconcepts.jupiter.data.Track;
import com.futureconcepts.jupiter.kml.KmlParser.IconStyle;
import com.futureconcepts.jupiter.kml.KmlParser.LineStyle;
import com.futureconcepts.jupiter.kml.KmlParser.Style;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class TripImporter
{
	private static String TAG = "TripParser";

	private Context _context;
	private String _tripId;
	private Uri _tripUri;
	private String _parentId;
	private ZipFile _zipFile;
	private int _nextWaypointSequence = 0;
	private int _nextPathSequence = 0;
	
	public TripImporter(Context context)
	{
		_context = context;
	}
	
	public Uri parse(ZipFile zipFile) throws XmlPullParserException, IOException
	{
		_zipFile = zipFile;
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements())
		{
			ZipEntry zipEntry = entries.nextElement();
			parse(zipEntry);
		}
		return _tripUri;
	}
	
	public void parse(ZipEntry zipEntry) throws XmlPullParserException, IOException
	{
		String name = zipEntry.getName();
		Log.d(TAG, "parse ZipEntry name=" + name);
		if (name.contains(".kml"))
		{
			parseInputStream(_zipFile.getInputStream(zipEntry));
		}
		else if (name.contains("Images/"))
		{
			importMedia(zipEntry);
		}
	}

	private void importMedia(ZipEntry zipEntry) throws IOException
	{
		File outPath = new File(Environment.getExternalStorageDirectory(), "FutureConcepts/.trips/" + _tripId);
		File outFile = new File(outPath, zipEntry.getName());
		outFile.getParentFile().mkdirs();
		copyStream(_zipFile.getInputStream(zipEntry), new FileOutputStream(outFile));
	}

	private void copyStream(InputStream inStream, OutputStream outStream) throws IOException
	{
		int bytesRead = 0;
		int accumulatedBytes = 0;
		byte[] bytes = new byte[512];
		while ((bytesRead = inStream.read(bytes)) != -1)
		{
			accumulatedBytes += bytesRead;
			outStream.write(bytes, 0, bytesRead);
		}
		outStream.flush();
		outStream.close();
	}
	
	public void parseInputStream(InputStream inputStream) throws XmlPullParserException, IOException
	{
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
		Log.d(TAG, "parse complete");
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
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("Folder"))
				{
					parseTrip(xpp);
				}
				else if (xpp.getName().equals("Style"))
				{
					parseStyle(xpp);
				}
			}
			eventType = xpp.next();
		}
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
				else if (xpp.getName().equals("BalloonStyle"))
				{
					parseBalloonStyle(xpp);
				}
			}
			eventType = xpp.next();
		}
		if (id != null)
		{
//			_styleMap.add("#"+id, style);
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
				String name = xpp.getName();
				if (name.equals("color"))
				{
					style.color = Color.parseColor("#"+xpp.nextText());
				}
				else if (name.equals("colorMode"))
				{
					xpp.nextText();
//
				}
				else if (name.equals("width"))
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
				String name = xpp.getName();
				if (name.equals("color"))
				{
					style.color = Color.parseColor("#"+xpp.nextText());
				}
				else if (name.equals("colorMode"))
				{
					xpp.nextText();
				}
				else if (xpp.getName().equals("Icon"))
				{
					parseIcon(xpp);
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

	private void parseBalloonStyle(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equals("text"))
				{
					xpp.nextText();
				}
			}
			eventType = xpp.next();
		}
	}

	private void parseTrip(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		Folder tripsFolder = Folder.getFolderByParentId(_context, null, Folder.NAME_TRIPS);
		if (tripsFolder != null)
		{
			ContentValues values = new ContentValues();
			_tripId = xpp.getAttributeValue(null, "id");
			_parentId = _tripId;
			if (_tripId == null)
			{
				throw new IllegalArgumentException("tripId");
			}
			values.put(Folder.ID, _tripId);
			values.put(Folder.PARENT_ID, tripsFolder.getId());
			int eventType = xpp.next();
			while (eventType != XmlPullParser.END_TAG)
			{
				if (eventType == XmlPullParser.START_TAG)
				{
					String name = xpp.getName().toLowerCase();
					if (name.equals("name"))
					{
						values.put(Folder.NAME, xpp.nextText());
					}
					else if (name.equals("description"))
					{
						values.put(Folder.DESCRIPTION, xpp.nextText());
					}
					else if (name.equals("folder"))
					{
						String folderId = xpp.getAttributeValue(null, "id");
						if (folderId != null)
						{
							if (folderId.equals(Folder.NAME_PLACEMARKS))
							{
								parseTopFolder(xpp, folderId);
							}
							else if (folderId.equals(Folder.NAME_ROUTES))
							{
								parseTopFolder(xpp, folderId);
							}
							else if (folderId.equals(Folder.NAME_TRACKS))
							{
								parseTopFolder(xpp, folderId);
							}
							else if (folderId.equals(Folder.NAME_MEDIA))
							{
								parseTopFolder(xpp, folderId);
							}
							else
							{
								throw new IllegalArgumentException("unexpected folder id " + folderId);
							}
						}
					}
				}
				eventType = xpp.next();
			}
			values.put(Folder.LAST_MODIFIED_TIME, System.currentTimeMillis());
			_tripUri = _context.getContentResolver().insert(Folder.CONTENT_URI, values);
			updateFolderTimeStamp(tripsFolder);
			tripsFolder.close();
		}
	}

	private void updateFolderTimeStamp(Folder folder)
	{
		ContentValues values = new ContentValues();
		values.put(Folder.LAST_MODIFIED_TIME, System.currentTimeMillis());
		Uri uri = Uri.withAppendedPath(Folder.CONTENT_URI, folder.get_ID());
		int updated = _context.getContentResolver().update(uri, values, null, null);
		if (updated != 1)
		{
			Log.d(TAG, "error");
		}
	}
	
	private void parseTopFolder(XmlPullParser xpp, String folderName) throws XmlPullParserException, IOException
	{
		Folder folder = Folder.getFolderByParentId(_context, _tripId, folderName);
		if (folder != null)
		{
			String holdParentId = _parentId;
			_parentId = folder.getId();
			int eventType = xpp.next();
			while (eventType != XmlPullParser.END_TAG)
			{
				if (eventType == XmlPullParser.START_TAG)
				{
					String name = xpp.getName().toLowerCase();
					if (name.equals("name"))
					{
						xpp.nextText();
					}
					else if (name.equals("folder"))
					{
						parseRoute(xpp.getAttributeValue(null, "id"), xpp);
					}
					else if (name.equals("placemark"))
					{
						parsePlacemark(xpp);
					}
				}
				eventType = xpp.next();
			}
			updateFolderTimeStamp(folder);
			folder.close();
			folder = null;
			_parentId = holdParentId;
		}
	}
	
	private void parseRoute(String routeId, XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		ContentValues values = new ContentValues();
		values.put(Folder.ID, routeId);
		values.put(Folder.PARENT_ID, _parentId);
		String holdParentId = _parentId;
		_parentId = routeId;
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				String name = xpp.getName().toLowerCase();
				if (name.equals("name"))
				{
					values.put(Folder.NAME, xpp.nextText());
				}
				if (name.equals("description"))
				{
					values.put(Folder.DESCRIPTION, xpp.nextText());
				}
				else if (name.equals("placemark"))
				{
					parsePlacemark(xpp);
				}
			}
			eventType = xpp.next();
		}
		_context.getContentResolver().insert(Folder.CONTENT_URI, values);
		_parentId = holdParentId;
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

	private void parsePlacemark(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		ContentValues values = new ContentValues();
		String id = xpp.getAttributeValue(null, "id");
		if (id == null)
		{
			id = UUID.randomUUID().toString().toLowerCase();
		}
		values.put(Placemark.ID, id);
		values.put(Placemark.TIME, _nextWaypointSequence++);
		values.put(Placemark.PARENT_ID, _parentId);
		String holdParentId = _parentId;
		_parentId = id;
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				String name = xpp.getName().toLowerCase();
				if (name.equals("name"))
				{
					values.put(Placemark.NAME, xpp.nextText());
				}
				else if (name.equals("description"))
				{
					values.put(Placemark.DESCRIPTION, xpp.nextText());
				}
				else if (name.equals("styleurl"))
				{
					xpp.nextText();
				}
				else if (name.equals("address"))
				{
					values.put(Placemark.ADDRESS, xpp.nextText());
				}
				else if (name.equals("point"))
				{
					PointF point = parsePoint(xpp);
					values.put(Placemark.LATITUDE, point.y);
					values.put(Placemark.LONGITUDE, point.x);
				}
				else if (name.equals("linestring"))
				{
					parseLineString(xpp);
				}
				else if (name.equals("extendeddata"))
				{
					parsePlacemarkExtendedData(xpp, values);
				}
			}
			eventType = xpp.next();
		}
		_context.getContentResolver().insert(Placemark.CONTENT_URI, values);
		_parentId = holdParentId;
	}
	
	private void parsePlacemarkExtendedData(XmlPullParser xpp, ContentValues values) throws XmlPullParserException, IOException
	{
		boolean writeIt = false;
		ContentValues mValues = new ContentValues();
		mValues.put(MediaStore.MediaColumns.DISPLAY_NAME, values.getAsString(Placemark.NAME));
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				String name = xpp.getName().toLowerCase();
				if (name.equals("data"))
				{
					String key = xpp.getAttributeValue(null, "name");
					String value = parsePlacemarkData(xpp);
					if (key.equals("Path"))
					{
						File outPath = new File(Environment.getExternalStorageDirectory(), "FutureConcepts/.trips/" + _tripId);
						File outFile = new File(outPath, value);
						mValues.put(MediaStore.MediaColumns.DATA, outFile.getPath());
						writeIt = true;
					}
					else if (key.equals("MimeType"))
					{
						mValues.put(MediaStore.MediaColumns.MIME_TYPE, value);
					}
				}
			}
			eventType = xpp.next();
		}
		if (writeIt)
		{
			try
			{
				Uri uri = _context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mValues);
				if (uri != null)
				{
					values.put(Placemark.MEDIA_URL, uri.toString());
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private String parsePlacemarkData(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		String value = null;
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				String name = xpp.getName().toLowerCase();
				if (name.equals("value"))
				{
					value = xpp.nextText();
				}
			}
			eventType = xpp.next();
		}
		return value;
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

	private void parseLineString(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		_nextPathSequence = 0;
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
						values.put(Track.PARENT_ID, _parentId);
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
}
