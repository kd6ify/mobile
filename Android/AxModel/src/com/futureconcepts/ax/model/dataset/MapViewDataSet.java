package com.futureconcepts.ax.model.dataset;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Incident;

public class MapViewDataSet extends AssetViewDataSet
{
	public MapViewDataSet()
	{
		super();
		addItem(new AssetViewDataSet());
		addItem(new TriageDataSet());
		addItem(new TacticViewDataSet());
		addItem(new IncidentDataSetTable("IncidentAddress", Address.CONTENT_URI));
		addItem(new DataSetTable("Incident", Incident.CONTENT_URI));
		addItem(new DrawingDataSet());
	}
}
