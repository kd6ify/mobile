package com.futureconcepts.ax.model.dataset;

import java.util.ArrayList;
import java.util.List;

public class DataSet
{
	private ArrayList<DataSetTable> _list;
	
	protected DataSet()
	{
		_list = new ArrayList<DataSetTable>();
	}
	
	public List<DataSetTable> getList()
	{
		return _list;
	}
	
	protected void addItem(DataSetTable item)
	{
		_list.add(item);
	}
	
	protected void addItem(DataSet baseSet)
	{
		for (int i = 0; i < baseSet._list.size(); i++)
		{
			addItem(baseSet._list.get(i));
		}
	}
}
