package com.futureconcepts.ax.model.dataset;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Incident;
import com.futureconcepts.ax.model.data.JournalEntryMedia;
import com.futureconcepts.ax.model.data.Media;

public class IncidentDataSet extends DataSet
{
	public IncidentDataSet()
	{
		super();		
		addItem(new IncidentDataSetTable("IncidentAddress", Address.CONTENT_URI));
		addItem(new IncidentJournalDataSet());
		addItem(new TacticViewDataSet());
		addItem(new AssetViewDataSet());
		addItem(new TacticCollectionViewDataSet());
		addItem(new TriageDataSet());
		addItem(new DrawingDataSet());
	}
}
