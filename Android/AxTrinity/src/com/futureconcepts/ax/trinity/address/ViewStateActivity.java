package com.futureconcepts.ax.trinity.address;

import com.futureconcepts.ax.model.data.INCITS38200x;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.ViewItemActivity;

import android.os.Bundle;

public class ViewStateActivity extends ViewItemActivity
{
	private INCITS38200x _state;
	private MyAdapter _adapter;

    private ViewItemDescriptor[] _myItems = {
    		new ViewItemDescriptor( "Name", ITEM_TYPE_TEXT, INCITS38200x.NAME ),
    		new ViewItemDescriptor( "Number Code", ITEM_TYPE_INT, INCITS38200x.NUM_CODE ),
    		new ViewItemDescriptor( "State Abbreviation", ITEM_TYPE_TEXT, INCITS38200x.STATE_CODE ),
        };
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_equipment);
        if (getData() != null)
        {
			startManagingModel(_state = INCITS38200x.query(this, getData()));
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
    		setTitle("State: " + _state.getStateCode());
    	}
    }
    
	private final class MyAdapter extends ViewItemAdapter
	{
		public MyAdapter()
		{
			super(_state, _myItems);
		}
	}
}
