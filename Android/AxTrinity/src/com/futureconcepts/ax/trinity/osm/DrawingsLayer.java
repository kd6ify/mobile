package com.futureconcepts.ax.trinity.osm;

import java.io.StringReader;

import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.overlay.Polygon;
import org.mapsforge.map.layer.overlay.Polyline;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.net.Uri;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.AddressExtraInfo;
import com.futureconcepts.ax.model.data.Drawing;
import com.futureconcepts.ax.model.data.DrawingAddress;

public class DrawingsLayer{
	
	static int currentDrawingType = 0;
	static int typePolygon = 1;
	static int typeLineString = 2;
	static int typeUnknown = 0;
	
	public static void displayDrawings(Context context, Drawing drawings, Layers layers)
	{
		if(	drawings.moveToFirst()){	
		 do{
			 DrawingAddress drawingAddress = DrawingAddress.queryDrawingAddress(context,drawings.getID());
			 if(drawingAddress.moveToFirst())
			 {
				 do{
					 Address address = drawingAddress.getAddress(context);//Address.query(context, Uri.withAppendedPath(Address.CONTENT_URI,drawingAddress.getAddressID()));
					if( address.moveToFirst())
					{					
					 String[] points = getGeoPointsFromWKT(address.getWKT());
					 DrawingStyle style = parseXMLData(getAddressExtraInfoXMLData(context,address.getExtraInfo()));						
					 address.close();
					 if(currentDrawingType == typePolygon)
					 {
						 layers.add(drawPolygon(points,style));
					 }else if(currentDrawingType==typeLineString)
					 {
						 layers.add( drawLineString(points,style));
					 }
					} 
				 }while(drawingAddress.moveToNext());
			 }
			 drawingAddress.close();			 
		 }while(drawings.moveToNext());
		}
	}
	
	private static Polygon drawPolygon(String [] points, DrawingStyle style)
	{		
		 Paint paintFill = Utils.createPaint(Utils.createARGBColorFromString(style.getBrushColor()),
				 2, Style.FILL);
		 Paint paintStroke = Utils.createPaint(Utils.createARGBColorFromString(style.getPenColor()),
				 style.getPenThickness(), Style.STROKE);
		 Polygon polygon = new Polygon(paintFill, paintStroke, AndroidGraphicFactory.INSTANCE);
		 for(String point : points)
		 {
			 String [] pointSepatare =point.split(" ");
			 polygon.getLatLongs().add(new LatLong(Double.parseDouble(pointSepatare[1]),Double.parseDouble(pointSepatare[0])));
		 }		 
		 return polygon;
	}
	
	private static  Polyline drawLineString(String [] points, DrawingStyle style)
	{ 
		Polyline polyline = new Polyline(Utils.createPaint(Utils.createARGBColorFromString(style.getPenColor()),
				 style.getPenThickness(), Style.STROKE), AndroidGraphicFactory.INSTANCE);
		 for(String point : points)
		 {
			 String [] pointSepatare =point.split(" ");
			 polyline.getLatLongs().add(new LatLong(Double.parseDouble(pointSepatare[1]),Double.parseDouble(pointSepatare[0])));
		 }
		 return polyline;
	}
	
	private static String getAddressExtraInfoXMLData(Context context ,String ID)
	{
		AddressExtraInfo axi =  AddressExtraInfo.getAddressExtraInfoCursor(context,ID);
		if(axi!=null && axi.getCount()>0){
			axi.moveToFirst();
			String xml =  axi.getStyle();
			axi.close();
			return xml;
		}else
		{
			return null;
		}
	}
	
	private static String[] getGeoPointsFromWKT(String address)
	{
		String [] result = null;
		if(address.contains("POLYGON"))
		{	currentDrawingType = typePolygon;
			String a =  address.replaceAll("[()]", "");
			result = a.replaceAll("POLYGON", "").split(",");
		}else if(address.contains("LINESTRING"))
		{
			currentDrawingType = typeLineString;
			String a =  address.replaceAll("[()]", "");
			result = a.replaceAll("LINESTRING", "").split(",");
		}else
		{
			currentDrawingType =  typeUnknown;
		}
		return result;
	}	
	
	private static DrawingStyle parseXMLData(String xmlData)
	{
		DrawingStyle style = new DrawingStyle();
		if(xmlData==null)
		{
			return style;
		}
		try{
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new StringReader (xmlData));
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
        	if(eventType == XmlPullParser.START_TAG) {
            	if(xpp.getName().equals("Pen")){
            		style.setPenColor(xpp.getAttributeValue(null,"Color"));
            		style.setPenThickness(Integer.valueOf(xpp.getAttributeValue(null,"Thickness")));
            	}else if(xpp.getName().equals("Brush")){
            		style.setBrushColor( xpp.getAttributeValue(null,"Color"));
            		style.setBrushStyle(xpp.getAttributeValue(null,"Thickness"));
            	} 	
        	} 
        	eventType = xpp.next();
        } 
		}catch(Exception e)
		{
			e.printStackTrace();	
		}
        return style;
	}
	
	
}
