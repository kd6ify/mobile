package com.futureconcepts.ax.model.dataset;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Journal;
import com.futureconcepts.ax.model.data.JournalEntry;
import com.futureconcepts.ax.model.data.JournalEntryMedia;
import com.futureconcepts.ax.model.data.Media;
import com.futureconcepts.ax.model.data.MediaAddress;

public class IncidentJournalDataSet extends DataSet
{
	public IncidentJournalDataSet()
	{
		super();
		addItem(new IncidentDataSetTable("IncidentJournal", Journal.CONTENT_URI));
    	addItem(new IncidentDataSetTable("IncidentJournalEntry", JournalEntry.CONTENT_URI));
    	//this piece for log Images
    	addItem(new IncidentDataSetTable("Media",Media.CONTENT_URI));
		addItem(new IncidentDataSetTable("IncidentJournalEntryMedia",JournalEntryMedia.CONTENT_URI));
		addItem(new IncidentDataSetTable("IncidentJournalEntryAddress",Address.CONTENT_URI));
		//Get Media Address Table
		//addItem(new IncidentDataSetTable("IncidentMediaAddress",MediaAddress.CONTENT_URI));
	}
}
