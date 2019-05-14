package com.futureconcepts.ax.model.dataset;

import com.futureconcepts.ax.model.data.Incident;

public class IncidentListViewDataSet extends DataSet
{
	public IncidentListViewDataSet()
	{
		super();
		addItem(new OperationalPeriodViewDataSet());
		addItem(new DataSetTable("Incident", Incident.CONTENT_URI));
	}
}
