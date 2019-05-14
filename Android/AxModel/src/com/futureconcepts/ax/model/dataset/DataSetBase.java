package com.futureconcepts.ax.model.dataset;

import java.util.ArrayList;

import android.content.Context;

public class DataSetBase
{
	private ArrayList<DataSetItem> _list;
	
	protected DataSetBase(Context context)
	{
		_list = new ArrayList<DataSetItem>();
	}
	
	protected void addItem(DataSetItem item)
	{
		_list.add(item);
	}
	
	protected void addItem(DataSetBase baseSet)
	{
		for (int i = 0; i < baseSet._list.size(); i++)
		{
			addItem(baseSet._list.get(i));
		}
	}
}
