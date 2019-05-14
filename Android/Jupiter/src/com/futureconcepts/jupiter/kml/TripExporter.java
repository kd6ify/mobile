package com.futureconcepts.jupiter.kml;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.xmlpull.v1.XmlSerializer;

import com.futureconcepts.jupiter.data.Placemark;
import com.futureconcepts.jupiter.data.Folder;
import com.futureconcepts.jupiter.data.Track;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Xml;

public class TripExporter implements Closeable
{
	private Context _context;
	private Folder _trip;
	private ZipOutputStream _zipOutStream;
	private OutputStreamWriter _writer;
	private XmlSerializer _serializer;
	private Stack<String> _tagStack;
	private ArrayList<String> _mediaFileNames;
	
	public TripExporter(Context context, Folder trip, File file) throws FileNotFoundException
	{
		_context = context;
		_trip = trip;
		_zipOutStream = new ZipOutputStream(new FileOutputStream(file));
		_mediaFileNames = new ArrayList<String>();
	}

	@Override
	public void close() throws IOException
	{
		if (_writer != null)
		{
			_writer.close();
			_writer = null;
		}
	}

	public void writeTrip() throws IOException
	{
		_zipOutStream.putNextEntry(new ZipEntry("trip.kml"));
		_writer = new OutputStreamWriter(_zipOutStream);
		_serializer = Xml.newSerializer();
		_tagStack = new Stack<String>();
    	try
    	{
			_serializer.setOutput(_writer);
	    	_serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
		}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
		writeStartTag("kml");
		writeAttribute("xmlns", "http://www.opengis.net/kml/2.2");
		writeStartTag("Document");
		writeStyles();
		writeStartTag("Folder");
		writeElement("name", _trip.getName());
		writeFolder(Folder.NAME_PLACEMARKS);
		writeFolder(Folder.NAME_ROUTES);
		writeFolder(Folder.NAME_TRACKS);
		writeFolder(Folder.NAME_MEDIA);
		writeEndTag(); // trip folder
		writeEndTag(); // document
		writeEndTag(); // kml
		_serializer.flush();
		_serializer = null;
		_tagStack = null;
		_writer.flush();
		_zipOutStream.closeEntry();
		writeMediaFiles();
	}
	
	private void writeStyles()
	{
		
	}
	
	private void writeFolder(String name)
	{
		writeStartTag("Folder");
		writeElement("name", name);
		Folder folder = Folder.getFolderByParentIdAndName(_context, _trip.getId(), name);
		if (folder != null)
		{
			if (folder.getCount() == 1)
			{
				Folder routeFolder = Folder.getFoldersByParentId(_context, folder.getId());
				if (routeFolder != null)
				{
					int count = routeFolder.getCount();
					for (int i = 0; i < count; i++)
					{
						routeFolder.moveToPosition(i);
						writeRoute(routeFolder);
					}
					routeFolder.close();
					routeFolder = null;
				}
				writePlacemarks(folder.getId());
			}
			folder.close();
			folder = null;
		}
		writeEndTag(); // route folder
	}
	
	private void writeRoute(Folder route)
	{
		writeStartTag("Folder");
		writeAttribute("id", route.getId());
		writeElement("name", route.getName());
		writePlacemarks(route.getId());
		writeEndTag();
	}
	
	private void writePlacemarks(String parentId)
	{
		Placemark placemark = Placemark.getPlacemarksByParentId(_context, parentId);
		if (placemark != null)
		{
			if (placemark.getCount() > 0)
			{
				int count = placemark.getCount();
				for (int i = 0; i < count; i++)
				{
					placemark.moveToPosition(i);
					writePlacemark(placemark);
				}
			}
			placemark.close();
			placemark = null;
		}
	}
	
	private void writePlacemark(Placemark placemark)
	{
		writeStartTag("Placemark");
		String id = placemark.getId();
		if (id != null)
		{
			writeAttribute("id", id);
		}
		String name = placemark.getName();
		if (name != null)
		{
			writeElement("name", name);
		}
		String description = placemark.getDescription();
		if (description != null)
		{
			writeElement("description", description);
		}
		double lat = placemark.getLatitude();
		if (lat != 0)
		{
			double lon = placemark.getLongitude();
			writeStartTag("Point");
			writeStartTag("coordinates");
			writeText(lon + "," + lat);
			writeEndTag();
			writeEndTag();
		}
		writeTrack(placemark.getId());
		if (placemark.getMediaUrl() != null)
		{
			_mediaFileNames.add(placemark.getMediaUrl());
			writePlacemarkMediaInfo(placemark);
		}
		writeEndTag();
	}
	
	private void writePlacemarkMediaInfo(Placemark placemark)
	{
		writeStartTag("ExtendedData");
		writeStartTag("Data");
		writeAttribute("name", "Path");
		writeElement("value", placemark.getMediaUrl());
		writeEndTag();
		writeStartTag("Data");
		writeAttribute("name", "MimeType");
		writeElement("value", "image/jpg");
		writeEndTag();
		writeEndTag();
	}
	
	private void writeTrack(String placemarkId)
	{
		ContentResolver resolver = _context.getContentResolver();
		Track track = new Track(resolver.query(Track.CONTENT_URI, null, Track.PARENT_ID + "='" + placemarkId + "'", null, Track.SEQUENCE + " ASC"));
		if (track != null)
		{
			int count = track.getCount();
			if (count > 0)
			{
				writeStartTag("LineString");
				writeStartTag("coordinates");
				for (int i = 0; i < count; i++)
				{
					track.moveToPosition(i);
					writeText(track.getLongitude() + "," + track.getLatitude() + " ");
				}
				writeEndTag();
				writeEndTag();
			}
			track.close();
			track = null;
		}
	}
	
	
	public void writeStartTag(String tag)
	{
		try
		{
			_serializer.startTag(null, tag);
			_tagStack.push(tag);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void writeEndTag()
	{
		try
		{
			String tag = _tagStack.pop();
			_serializer.endTag(null, tag);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void writeAttribute(String name, String value)
	{
		try
		{
			_serializer.attribute(null, name, value);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void writeText(String text)
	{
		try
		{
			_serializer.text(text);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void writePoint(String style, int width)
	{
		writeStartTag("Point");
		writeAttribute("style", style);
		writeAttribute("width", Integer.toString(width));
		writeEndTag();
	}

	public void writeElement(String tag, String value)
	{
		writeStartTag(tag);
		writeText(value);
		writeEndTag();
	}

	private void writeMediaFiles() throws IOException
	{
		for (String fileName : _mediaFileNames)
		{
			writeMediaFile(fileName);
		}
	}
	
	private void writeMediaFile(String fileName) throws IOException
	{
		File file = new File(new File("/sdcard/FutureConcepts/.trips/" + _trip.getId()), fileName);
		long length = file.length();
		if (length > 0)
		{
			_zipOutStream.putNextEntry(new ZipEntry(fileName));
			copyStream(new FileInputStream(file), _zipOutStream, length);
			_zipOutStream.closeEntry();
		}
	}

	private void copyStream(InputStream inStream, OutputStream outStream, long size) throws IOException
	{
		int bytesRead = 0;
		int accumulatedBytes = 0;
		byte[] bytes = new byte[512];
		while ((bytesRead = inStream.read(bytes)) != -1)
		{
			accumulatedBytes += bytesRead;
			outStream.write(bytes, 0, bytesRead);
//			updateSyncProgress(SYNC_DIRECTION_DOWN, accumulatedBytes, size);
		}
		outStream.flush();
//		outStream.close();
	}
}
