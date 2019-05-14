package com.futureconcepts.ax.model.dataset;

import com.futureconcepts.ax.model.data.Collection;
import com.futureconcepts.ax.model.data.CollectionAttribute;
import com.futureconcepts.ax.model.data.CollectionAttributeTactic;

public class TacticCollectionViewDataSet extends DataSet
{
	public TacticCollectionViewDataSet()
	{
		super();
		addItem(new IncidentDataSetTable("IncidentCollection", Collection.CONTENT_URI));
		addItem(new IncidentDataSetTable("IncidentCollectionAttribute", CollectionAttribute.CONTENT_URI));
		addItem(new IncidentDataSetTable("IncidentTacticCollection", CollectionAttributeTactic.CONTENT_URI));
	}
}
