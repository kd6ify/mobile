package com.futureconcepts.ax.trinity.assets;

import com.futureconcepts.ax.model.data.BloodType;
import com.futureconcepts.ax.model.data.Person;
import com.futureconcepts.ax.model.data.User;
import com.futureconcepts.ax.trinity.ViewItemActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class ViewUserActivity extends ViewItemActivity
{
	private User _user;
	private Person _person;
	private MyAdapter _adapter;
	
	private static final String PERSON_TABLE = "Person";
	
	private static final int MY_ITEM_TYPE_BLOOD_TYPE = ITEM_TYPE_SPECIAL + 1;
	private static final int MY_ITEM_TYPE_AGENCY = ITEM_TYPE_SPECIAL + 2;
	private static final int MY_ITEM_TYPE_USER_TYPE = ITEM_TYPE_SPECIAL + 3;
	private static final int MY_ITEM_TYPE_USER_RANK_TYPE = ITEM_TYPE_SPECIAL + 4;
		
	private ViewItemDescriptor[] _myItems = {
			// Person fields
		    new ViewItemDescriptor( "Last Name", ITEM_TYPE_TEXT, Person.LAST, PERSON_TABLE),
		    new ViewItemDescriptor( "First Name", ITEM_TYPE_TEXT, Person.FIRST, PERSON_TABLE),
		    new ViewItemDescriptor( "Middle Name", ITEM_TYPE_TEXT, Person.MIDDLE, PERSON_TABLE),
		    new ViewItemDescriptor( "Suffix", ITEM_TYPE_TEXT, Person.SUFFIX, PERSON_TABLE),
		    new ViewItemDescriptor( "Height", ITEM_TYPE_REAL, Person.HEIGHT, PERSON_TABLE),
		    new ViewItemDescriptor( "Weight", ITEM_TYPE_REAL, Person.WEIGHT, PERSON_TABLE),
    		new ViewItemDescriptor( "Blood Type", MY_ITEM_TYPE_BLOOD_TYPE, null),
    		// User fields
		    new ViewItemDescriptor( "Employee Number", ITEM_TYPE_INT, User.EMPLOYEE_NO),
    		new ViewItemDescriptor( "Type", MY_ITEM_TYPE_USER_TYPE, null),
    		new ViewItemDescriptor( "Rank", MY_ITEM_TYPE_USER_RANK_TYPE, null),
    		new ViewItemDescriptor( "Agency", MY_ITEM_TYPE_AGENCY, null),
    };

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getData() != null)
        {
        	startManagingModel(_user = User.query(this, getData()));
        	_person = _user.getPerson(this);
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
    		if (_person != null)
    		{
    			setTitle("User: " + _person.getName());
    		}
    		else if (_user.getEmployeeNo() != null)
    		{
    			setTitle("User: " + _user.getEmployeeNo());
    		}
    		else
    		{
    			setTitle("User: " + _user.getID());
    		}
    	}
    }
    
	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
//		ViewItemDescriptor vid = _myItems[position];
//		if (vid.source2.equals(PERSON_TABLE))
//		{
//			startActivity(new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(Person.CONTENT_URI, _user.getPersonID())));
//		}
	}
    
	private final class MyAdapter extends ViewItemAdapter
	{
		public MyAdapter()
		{
			super(_user, _myItems);
		}
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View result = null;
			ViewItemDescriptor vid = _myItems[position];
			if (vid.source2 != null)
			{
				if (vid.source2.equals(PERSON_TABLE))
				{
					result = getItemView(_person, vid);
				}
			}
			else
			{
				switch (vid.type)
				{
				case MY_ITEM_TYPE_USER_TYPE:
					result = getIndexedTypeView(vid, _user.getType(ViewUserActivity.this));
					break;
				case MY_ITEM_TYPE_AGENCY:
					result = getAgencyView(vid, _user.getAgency(ViewUserActivity.this));
					break;
				case MY_ITEM_TYPE_USER_RANK_TYPE:
					result = getIndexedTypeView(vid, _user.getRank(ViewUserActivity.this));
					break;
				case MY_ITEM_TYPE_BLOOD_TYPE:
					if (_person != null)
					{
						BloodType bloodType = _person.getBloodType(ViewUserActivity.this);
						if (bloodType != null)
						{
							result = getTextView(vid, bloodType.getName());
						}
						else
						{
							result = getTextView(vid, "Unknown");
						}
					}
					else
					{
						result = getTextView(vid, "Unknown");
					}
					break;
				default:
					result = getItemView(_user, vid);
					break;
				}
			}
			return result;
		}
	}
}
