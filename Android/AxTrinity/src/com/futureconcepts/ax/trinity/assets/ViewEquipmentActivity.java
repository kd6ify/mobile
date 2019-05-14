package com.futureconcepts.ax.trinity.assets;

import com.futureconcepts.ax.model.data.Equipment;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.ViewItemActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

public class ViewEquipmentActivity extends ViewItemActivity
{
	private Equipment _equipment;
	private MyAdapter _adapter;

	private static final int MY_AGENCY_TYPE = ITEM_TYPE_SPECIAL + 1;
	
    private ViewItemDescriptor[] _myItems = {
    		new ViewItemDescriptor( "Name", ITEM_TYPE_TEXT, Equipment.NAME ),
    		new ViewItemDescriptor( "Type", ITEM_TYPE_INDEXED_TYPE, Equipment.TYPE ),
    		new ViewItemDescriptor( "Agency", MY_AGENCY_TYPE, Equipment.AGENCY),
        };
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_equipment);
        if (getData() != null)
        {
			startManagingModel(_equipment = Equipment.query(this, getIntent().getData()));
			if (_equipment.getCount() == 1)
			{
				_adapter = new MyAdapter();
				setListAdapter(_adapter);
				registerContentObserver(_adapter);
			}
        }
    }

    @Override
    public void onResume()
    {
    	super.onResume();
    	if (getCount() == 1)
    	{
    		setTitle("Equipment: " + _equipment.getName());
    	}
    	else
    	{
    		setTitle("Equipment: " + getIntent().getData().getPathSegments().get(1));
    	}
    }
    
	private final class MyAdapter extends ViewItemAdapter
	{
		public MyAdapter()
		{
			super(_equipment, _myItems);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ViewItemDescriptor vid = _myItems[position];
			View result = null;
			switch (vid.type)
			{
			case MY_AGENCY_TYPE:
				result = getAgencyView(vid, _equipment.getAgency(ViewEquipmentActivity.this));
				break;
			case ITEM_TYPE_INDEXED_TYPE:
				result = getIndexedTypeView(vid, _equipment.getType(ViewEquipmentActivity.this));
				break;
			default:
				result = super.getView(position, convertView, parent);
			}
			return result;
		}
	}
}
