package com.futureconcepts.ax.trinity.address;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.AddressType;
import com.futureconcepts.ax.model.data.INCITS38200x;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.ViewItemActivity;
import com.futureconcepts.ax.trinity.logs.images.CustomAlertDialog;
import com.futureconcepts.ax.trinity.osm.MainMapOSMActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class ViewAddressActivity extends ViewItemActivity
{
	private Address _address;
	private MyAdapter _adapter;
	private int isAssestCheckedIn=-1;
	private static final int MY_STATE_TYPE = ITEM_TYPE_SPECIAL + 1;
	private static final int MY_ADDRESS_TYPE = ITEM_TYPE_SPECIAL + 2;
	private CustomAlertDialog customDialog ;
    private ViewItemDescriptor[] _myItems = {
    		new ViewItemDescriptor( "Name", ITEM_TYPE_TEXT, Address.NAME ),
    		new ViewItemDescriptor( "Street 1", ITEM_TYPE_TEXT, Address.STREET1 ),
    		new ViewItemDescriptor( "Street 2", ITEM_TYPE_TEXT, Address.STREET2 ),
    		new ViewItemDescriptor( "City", ITEM_TYPE_TEXT, Address.CITY ),
//    		new ViewItemDescriptor( "Type", MY_ADDRESS_TYPE, null ),
    		new ViewItemDescriptor( "State", MY_STATE_TYPE, Address.STATE ),
    		new ViewItemDescriptor( "ZIP", ITEM_TYPE_INT, Address.ZIP ),
    		new ViewItemDescriptor( "ZIP4", ITEM_TYPE_INT, Address.ZIP4 ),
    		new ViewItemDescriptor( "Upper Left Latitude", ITEM_TYPE_REAL, Address.ULLAT ),
    		new ViewItemDescriptor( "Upper Left Longitude", ITEM_TYPE_REAL, Address.ULLON ),
    		new ViewItemDescriptor( "Lower Left Latitude", ITEM_TYPE_REAL, Address.LRLAT ),
    		new ViewItemDescriptor( "Lower Right Longitude", ITEM_TYPE_REAL, Address.LRLON ),
    		new ViewItemDescriptor( "WKT", ITEM_TYPE_TEXT, Address.WKT ),
        };
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_equipment);
        if (getData() != null)
        {
			startManagingModel(_address = Address.query(this, getData()));
			setTitle("Address Details");
			_adapter = new MyAdapter();
			setListAdapter(_adapter);
			registerContentObserver(_adapter);
        }
        if( getIntent().getExtras()!=null){isAssestCheckedIn = getIntent().getExtras().getInt("IsCheckedIn");}
        
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.view_address_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_show_on_map:
			if(isAssestCheckedIn != -1 &&  isAssestCheckedIn==0){
				displayAlert();
			}else{
				onMenuShowOnMap();
			}
			break;
		}
		return false;
	}
	
	public void goBack(View view)
	{
		finish();
	}
	public void displayMenuOptions(View view)
	{
		final String[] options = {"Show On Map"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Menu");
		builder.setCancelable(true);
		builder.setItems(options,new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if("Show On Map".equals(options[which]))
						{
							if(isAssestCheckedIn != -1 &&  isAssestCheckedIn==0){
								displayAlert();
							}else{
								onMenuShowOnMap();
							}
						}
						dialog.dismiss();
					}
				});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {					
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
    
	private void displayAlert()
	{
		String[] buttons = {"OK"};
		 // Create an instance of the dialog fragment and show it
		 customDialog = new CustomAlertDialog(this, buttons,
				  "Information ","This asset must be checked-in to be displayed on the map.",
				  android.R.drawable.ic_menu_info_details,
				 new CustomAlertDialog.DialogButtonClickListener() {
					@Override
					public void onDialogButtonClick(View v) {					
						customDialog.dismiss();
						customDialog = null;
					}
				});
		 customDialog.show();
	}
	
	private void onMenuShowOnMap()
	{
		Intent intent = new Intent(this, MainMapOSMActivity.class);
		intent.setData(getData());
		startActivity(intent);
	}
	    
    @Override
    public void onListItemClick(ListView listView, View view, int position, long id)
    {
    	String source1 = _myItems[position].source1;
    	if (source1 != null)
    	{
	    	if (source1.equals(Address.STATE))
	    	{
	    		String stateID = _address.getStateID();
	    		if (stateID != null)
	    		{
	    			startActivity(new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(INCITS38200x.CONTENT_URI, stateID)));
	    		}
	    	}
    	}
    }
    
	private final class MyAdapter extends ViewItemAdapter
	{
		public MyAdapter()
		{
			super(_address, _myItems);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ViewItemDescriptor vid = _myItems[position];
			View result = null;
			switch (vid.type)
			{
			case MY_STATE_TYPE:
				result = getStateView(vid, _address.getState(ViewAddressActivity.this));
				break;
			case MY_ADDRESS_TYPE:
				result = getAddressTypeView(vid, _address.getType(ViewAddressActivity.this));
				break;
			default:
				result = super.getView(position, convertView, parent);
			}
			return result;
		}
		protected View getStateView(ViewItemDescriptor vid, INCITS38200x state)
		{
			View result = null;
			if (state != null)
			{
				String abbr = state.getStateCode();
				result = getTextView(vid, abbr);
			}
			else
			{
				result = getTextView(vid, "None");
			}
			return result;
		}
		protected View getAddressTypeView(ViewItemDescriptor vid, AddressType type)
		{
			View result = null;
			if (type != null)
			{
				String name = type.getName();
				if (name != null)
				{
					result = getTextView(vid, name);
				}
				else
				{
					result = getTextView(vid, "unknown");
				}
			}
			else
			{
				result = getTextView(vid, "not specified");
			}
			return result;
		}
	}
}
