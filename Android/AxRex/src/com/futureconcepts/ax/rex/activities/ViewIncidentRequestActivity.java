package com.futureconcepts.ax.rex.activities;

import org.joda.time.DateTime;

import com.futureconcepts.ax.model.data.IncidentRequest;
import com.futureconcepts.ax.rex.R;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

public class ViewIncidentRequestActivity extends ViewItemActivity
{
	private IncidentRequest _incidentRequest;
	private MyAdapter _adapter;

    private ViewItemDescriptor[] _myItems =
    	{
    		new ViewItemDescriptor( "Incident Name", ITEM_TYPE_TEXT, IncidentRequest.INCIDENT_NAME ),
    		new ViewItemDescriptor( "Type", ITEM_TYPE_INDEXED_TYPE, IncidentRequest.TYPE ),
    		new ViewItemDescriptor( "Status", ITEM_TYPE_INDEXED_TYPE, IncidentRequest.STATUS ),
    		new ViewItemDescriptor( "Description", ITEM_TYPE_TEXT, IncidentRequest.DESCRIPTION ),
    		new ViewItemDescriptor( "Has Victims?", ITEM_TYPE_BOOLEAN_CHECKBOX, IncidentRequest.HAS_VICTIMS ),
    		new ViewItemDescriptor( "Created", ITEM_TYPE_DATE_TIME, IncidentRequest.CREATED )
    };
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_incident_request);
        DateTime.setContext(this);
        getListView().setBackgroundResource(R.color.view_incident_request_background);
        if (getData() != null)
        {
			startManagingCursor(_incidentRequest = IncidentRequest.query(this, getIntent().getData()));
			if (_incidentRequest.getCount() == 1)
			{
	    		setTitle("Incident Name: " + _incidentRequest.getIncidentName());
				_adapter = new MyAdapter();
				setListAdapter(_adapter);
			}
        }
    }

	private final class MyAdapter extends ViewItemAdapter
	{
		public MyAdapter()
		{
			super(_incidentRequest, _myItems);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ViewItemDescriptor vid = _myItems[position];
			View result = null;
			switch (vid.type)
			{
			case ITEM_TYPE_INDEXED_TYPE:
				if (vid.source1.equals(IncidentRequest.TYPE))
				{
					result = getIndexedTypeView(vid, _incidentRequest.getType(ViewIncidentRequestActivity.this));
				}
				else if (vid.source1.equals(IncidentRequest.STATUS))
				{
					result = getIndexedTypeView(vid, _incidentRequest.getStatus(ViewIncidentRequestActivity.this));
				}
				break;
			default:
				result = super.getView(position, convertView, parent);
			}
			return result;
		}
	}
}
