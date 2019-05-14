package com.futureconcepts.ax.trinity;

import java.io.StringWriter;
import java.util.Stack;
import java.util.UUID;

import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

public class DrawingSerializer
{
	private XmlSerializer _serializer;
	private StringWriter _writer;
	private Stack<String> _tagStack;
	
	public DrawingSerializer()
	{
    	_serializer = Xml.newSerializer();
		_writer = new StringWriter();
		_tagStack = new Stack<String>();
    	try
    	{
			_serializer.setOutput(_writer);
//	    	_serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
		}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
	}
	
	@Override
	public String toString()
	{
		try
		{
			_serializer.flush();
			_writer.flush();
			return _writer.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
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
	
	public void writePointStyle(String style, int width, String text)
    {
		writeStartTag("FCPointSymbol");
    	writePoint(style, width);
    	writeTextTag(text);
    	writeSolidBrush(100, 0, 0, 0);
    	writeEndTag();
    }
	
	public void writePoint(String style, int width)
	{
		writeStartTag("Point");
		writeAttribute("style", style);
		writeAttribute("width", Integer.toString(width));
		writeEndTag();
	}

	public void writeTextTag(String text)
	{
		writeStartTag("Text");
		writeAttribute("value", text);
		writeEndTag();
	}

	public void writeSolidBrush(int alpha, int red, int green, int blue)
	{
		writeStartTag("SolidBrush");
		writeStartTag("BrushColor");
		writeAttribute("a", Integer.toString(alpha));
		writeAttribute("r", Integer.toString(red));
		writeAttribute("g", Integer.toString(green));
		writeAttribute("b", Integer.toString(blue));
		writeEndTag();
		writeEndTag();
	}

	public void writePointShape(double lat, double lon)
	{
		writeShape("PointShape");
		writeKey();
		writeSelected(false);
		writeStartTag("X");
		writeText(Double.toString(lon));
		writeEndTag();
		writeStartTag("Y");
		writeText(Double.toString(lat));
		writeEndTag();
		writeEndTag();
	}
	
	public void writeShape(String type)
	{
		writeStartTag(type);
		writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		writeAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
	}
	
	public void writeKey()
	{
		writeStartTag("Key");
		writeText(UUID.randomUUID().toString());
		writeEndTag();
	}

	public void writeSelected(boolean isSelected)
	{
		writeStartTag("Selected");
		writeText(Boolean.toString(isSelected));
		writeEndTag();
	}
}
