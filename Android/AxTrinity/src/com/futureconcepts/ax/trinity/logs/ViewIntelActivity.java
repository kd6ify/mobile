package com.futureconcepts.ax.trinity.logs;

import com.futureconcepts.ax.model.data.Intelligence;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.ViewItemActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

public class ViewIntelActivity extends ViewItemActivity
{
	private Intelligence _intel;
	private MyAdapter _adapter;
	
	private ViewItemDescriptor[] _myItems = {
    		new ViewItemDescriptor( "Name", ITEM_TYPE_TEXT, Intelligence.NAME ),
    		new ViewItemDescriptor( "Status", ITEM_TYPE_INDEXED_TYPE, null ),
    		new ViewItemDescriptor( "Comments", ITEM_TYPE_TEXT, Intelligence.COMMENTS )
//    		new ViewItemDescriptor( "Entry Time", ITEM_TYPE_DATE_TIME, Intelligence.ENTRY_TIME)
    };
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_intel);
        setDefaultOptionsMenu(true);
        if (getData() != null)
        {
			startManagingModel(_intel = Intelligence.query(this, getData()));
			if (moveToFirstIfOneRow())
	    	{
	    		setTitle("Intel: " + _intel.getName());
    			_adapter = new MyAdapter();
    			setListAdapter(_adapter);
    			registerContentObserver(_adapter);
	    	}
        }
    }

	private final class MyAdapter extends ViewItemAdapter
	{
		public MyAdapter()
		{
			super(_intel, _myItems);
		}
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View result = null;
			ViewItemDescriptor vid = _myItems[position];
			if (vid.type == ITEM_TYPE_INDEXED_TYPE)
			{
				result = getIndexedTypeView(vid, _intel.getStatus(ViewIntelActivity.this));
		//		result = super.getTextView("debug", "debug");
			}
			else
			{
				result = super.getView(position, convertView, parent);
			}
			return result;
		}
	}
}
