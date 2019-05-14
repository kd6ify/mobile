package com.futureconcepts.ax.model.dataset;

import com.futureconcepts.ax.model.data.OperationalPeriod;

public class OperationalPeriodViewDataSet extends DataSet
{
	public OperationalPeriodViewDataSet()
	{
		super();
		addItem(new DataSetTable("OperationalPeriod", OperationalPeriod.CONTENT_URI));
	}
}
