package com.futureconcepts.ax.model.dataset;

import com.futureconcepts.ax.model.data.Incident;
import com.futureconcepts.ax.model.data.IncidentRequest;
import com.futureconcepts.ax.model.data.IncidentRequestStatus;
import com.futureconcepts.ax.model.data.IncidentType;

public class IncidentRequestDataSet extends DataSet
{
	public IncidentRequestDataSet()
	{
		super();
		
		// General
		
		// IncidentType
		
		addItem(new DataSetTable(IncidentType.CONTENT_URI));

		// IncidentRequestStatus
		
		addItem(new DataSetTable(IncidentRequestStatus.CONTENT_URI));
		
		// Incident
		
		addItem(new DataSetTable(Incident.CONTENT_URI));
		
		// IncidentRequest
		
		addItem(new OwnerDataSetTable(IncidentRequest.CONTENT_URI));
	}
}
