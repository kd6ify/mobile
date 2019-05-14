package com.futureconcepts.ax.trinity.address;

import com.futureconcepts.ax.model.data.ISO3166;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.ViewItemActivity;

import android.os.Bundle;

public class ViewCountryActivity extends ViewItemActivity
{
	private ISO3166 _iso3166;
	private MyAdapter _adapter;

    private ViewItemDescriptor[] _myItems = {
    		new ViewItemDescriptor( "Code", ITEM_TYPE_TEXT, ISO3166.CODE ),
    		new ViewItemDescriptor( "Name", ITEM_TYPE_TEXT, ISO3166.NAME ),
    		new ViewItemDescriptor( "IDD", ITEM_TYPE_INT, ISO3166.IDD ),
        };
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_equipment);
        if (getData() != null)
        {
			startManagingModel(_iso3166 = ISO3166.query(this, getData()));
			_adapter = new MyAdapter();
			setListAdapter(_adapter);
			registerContentObserver(_adapter);
        }
    }

    @Override
    public void onResume()
    {
    	super.onResume();
    	if (getCount() == 1)
    	{
    		setTitle("ISO3166: " + _iso3166.getID());
    	}
    }
    
	private final class MyAdapter extends ViewItemAdapter
	{
		public MyAdapter()
		{
			super(_iso3166, _myItems);
		}
	}
}
