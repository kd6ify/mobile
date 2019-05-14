package com.futureconcepts.ax.model.data;

import java.util.Hashtable;

import android.content.Context;
import android.database.Cursor;

public class IndexedType extends BaseTable
{
    public static final String NAME = "Name";
    public static final String ICON = "Icon";
    public static final String TOOLTIP = "ToolTip";
    public static final String SORT = "Sort";
	
    protected Hashtable<String, Integer> _dict;
    
    private Icon _icon;
    
	public IndexedType(Context context, Cursor cursor)
	{
		super(context, cursor);
		_dict = new Hashtable<String, Integer>();
		for (int i = 0; i < getCount(); i++)
		{
			moveToPosition(i);
			String id = getID();
			_dict.put(id, Integer.valueOf(i));
		}
		moveToPosition(0);
	}

	public void close()
	{
		if (_icon != null)
		{
			_icon.close();
			_icon = null;
		}
		super.close();
	}
	
	public String getName()
	{
		int idx = getColumnIndex(NAME);
		assert(idx != -1);
		return getString(idx);
	}
	
	public String getIconID()
	{
		return getString(getColumnIndex(ICON));
	}

	public Icon getIcon(Context context)
	{
		if (_icon == null)
		{
			_icon = Icon.query(context);
		}
		String iconID = getIconID();
		if (iconID != null)
		{
			_icon.moveToPosition(iconID);
			return _icon;
		}
		else
		{
			return null;
		}
	}
	
	public String getToolTip()
	{
		return getString(getColumnIndex(TOOLTIP));
	}
	
	public int getSort()
	{
		return getInt(getColumnIndex(SORT));
	}
	
	public void moveToPosition(String id)
	{
		Integer position = _dict.get(id);
		if (position != null)
		{
			moveToPosition(position.intValue());
		}
	}
}
