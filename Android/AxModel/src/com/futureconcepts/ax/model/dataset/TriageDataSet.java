package com.futureconcepts.ax.model.dataset;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Person;
import com.futureconcepts.ax.model.data.Triage;

public class TriageDataSet extends DataSet
{
	public TriageDataSet()
	{
		super();
    	addItem(new IncidentDataSetTable("IncidentTriagePerson", Person.CONTENT_URI));
    	addItem(new IncidentDataSetTable("IncidentTriageAddress", Address.CONTENT_URI));
		addItem(new IncidentDataSetTable("IncidentTriage", Triage.CONTENT_URI));
	}
}
