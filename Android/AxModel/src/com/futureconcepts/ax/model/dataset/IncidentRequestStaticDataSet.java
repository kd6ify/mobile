package com.futureconcepts.ax.model.dataset;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Incident;
import com.futureconcepts.ax.model.data.IncidentRequestStatus;
import com.futureconcepts.ax.model.data.IncidentType;

public class IncidentRequestStaticDataSet extends DataSet
{
	public IncidentRequestStaticDataSet()
	{
		super();
		
		// General
		
		addItem(new IncidentDataSetTable("IncidentAddress", Address.CONTENT_URI));
		
		// IncidentType
		
		addItem(new DataSetTable(IncidentType.CONTENT_URI));

		// IncidentRequestStatus
		
		addItem(new DataSetTable(IncidentRequestStatus.CONTENT_URI));
		
		// Incident
		
		addItem(new DataSetTable(Incident.CONTENT_URI));
	}
}
