package com.futureconcepts.ax.trinity.person;

import com.futureconcepts.ax.model.data.BloodType;
import com.futureconcepts.ax.model.data.Person;
import com.futureconcepts.ax.trinity.ViewItemActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

public class ViewPersonActivity extends ViewItemActivity
{
	private Person _person;
	private MyAdapter _adapter;
	
	private static final int MY_ITEM_TYPE_BLOOD_TYPE = ITEM_TYPE_SPECIAL + 1;
		
	private ViewItemDescriptor[] _myItems = {
		    new ViewItemDescriptor( "Full Name", ITEM_TYPE_TEXT, Person.NAME),
		    new ViewItemDescriptor( "Last Name", ITEM_TYPE_TEXT, Person.LAST),
		    new ViewItemDescriptor( "First Name", ITEM_TYPE_TEXT, Person.FIRST),
		    new ViewItemDescriptor( "Middle Name", ITEM_TYPE_TEXT, Person.MIDDLE),
		    new ViewItemDescriptor( "Suffix", ITEM_TYPE_TEXT, Person.SUFFIX),
		    new ViewItemDescriptor( "Height", ITEM_TYPE_REAL, Person.HEIGHT),
		    new ViewItemDescriptor( "Weight", ITEM_TYPE_REAL, Person.WEIGHT),
    		new ViewItemDescriptor( "Blood Type", MY_ITEM_TYPE_BLOOD_TYPE, null),
    };

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getData() != null)
        {
        	startManagingModel(_person = Person.query(this, getData()));
        }
		_adapter = new MyAdapter();
		setListAdapter(_adapter);
		registerContentObserver(_adapter);
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	if (getCount() == 1)
    	{
			setTitle("Person: " + _person.getName());
    	}
    }
    
	private final class MyAdapter extends ViewItemAdapter
	{
		public MyAdapter()
		{
			super(_person, _myItems);
		}
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View result = null;
			ViewItemDescriptor vid = _myItems[position];
			switch (vid.type)
			{
			case MY_ITEM_TYPE_BLOOD_TYPE:
				result = getBloodTypeView(vid, _person.getBloodType(ViewPersonActivity.this));
				break;
			default:
				result = super.getView(position, convertView, parent);
				break;
			}
			return result;
		}
		
		private View getBloodTypeView(ViewItemDescriptor vid, BloodType bloodType)
		{
			View result = null;
			if (bloodType != null)
			{
				result = getTextView(vid, bloodType.getName());
			}
			else
			{
				result = getTextView(vid, "Not specified");
			}
			return result;
		}
	}
}
