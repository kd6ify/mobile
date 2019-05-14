package com.futureconcepts.ax.model.dataset;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.AssetAttributeTactic;
import com.futureconcepts.ax.model.data.Collection;
import com.futureconcepts.ax.model.data.CollectionAttribute;
import com.futureconcepts.ax.model.data.CollectionAttributeTactic;
import com.futureconcepts.ax.model.data.Tactic;

public class TacticViewDataSet extends DataSet
{
	public TacticViewDataSet()
	{
		super();
		addItem(new IncidentDataSetTable("IncidentTacticAddress", Address.CONTENT_URI));
		addItem(new IncidentDataSetTable("IncidentTacticAsset", AssetAttributeTactic.CONTENT_URI));
		addItem(new IncidentDataSetTable("IncidentCollection", Collection.CONTENT_URI));
		addItem(new IncidentDataSetTable("IncidentCollectionAttribute", CollectionAttribute.CONTENT_URI));
		addItem(new IncidentDataSetTable("IncidentTacticCollection", CollectionAttributeTactic.CONTENT_URI));
		addItem(new IncidentDataSetTable("IncidentTactic", Tactic.CONTENT_URI));
	}
}
