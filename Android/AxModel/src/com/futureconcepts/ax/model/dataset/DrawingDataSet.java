package com.futureconcepts.ax.model.dataset;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.AddressExtraInfo;
import com.futureconcepts.ax.model.data.Drawing;
import com.futureconcepts.ax.model.data.DrawingAddress;
import com.futureconcepts.ax.model.data.IncidentDrawing;

public class DrawingDataSet extends DataSet{
	
	public DrawingDataSet()
	{
		super();
    	addItem(new IncidentDataSetTable("IncidentDrawing", IncidentDrawing.CONTENT_URI));
    	addItem(new IncidentDataSetTable("Drawing", Drawing.CONTENT_URI));
		addItem(new IncidentDataSetTable("DrawingAddress", DrawingAddress.CONTENT_URI));
		addItem(new IncidentDataSetTable("IncidentDrawingAddress", Address.CONTENT_URI));
		addItem(new IncidentDataSetTable("IncidentAddressExtraInfo", AddressExtraInfo.CONTENT_URI));
	}

}
