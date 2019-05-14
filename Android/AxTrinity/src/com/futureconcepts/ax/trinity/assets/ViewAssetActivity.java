package com.futureconcepts.ax.trinity.assets;

import org.joda.time.DateTime;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Agency;
import com.futureconcepts.ax.model.data.Asset;
import com.futureconcepts.ax.model.data.AssetStatus;
import com.futureconcepts.ax.model.data.AssetType;
import com.futureconcepts.ax.model.data.Equipment;
import com.futureconcepts.ax.model.data.EquipmentType;
import com.futureconcepts.ax.model.data.Person;
import com.futureconcepts.ax.model.data.User;
import com.futureconcepts.ax.model.data.UserRankType;
import com.futureconcepts.ax.model.data.UserType;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.ViewItemActivity;
import com.futureconcepts.ax.trinity.geo.MainMapActivity;
import com.futureconcepts.ax.trinity.osm.MainMapOSMActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

public class ViewAssetActivity extends ViewItemActivity
{
	private static final String TAG = ViewAssetActivity.class.getSimpleName();
	
	private Asset _asset;
	private MyAdapter _adapter;

	private static final int MY_ADDRESS_TYPE = ITEM_TYPE_SPECIAL + 1;
	private static final int MY_CHECKIN_LOCATION_TYPE = ITEM_TYPE_SPECIAL + 2;
	private static final int MY_ASSET_TYPE = ITEM_TYPE_SPECIAL + 3;
	private static final int MY_ASSET_STATUS = ITEM_TYPE_SPECIAL + 4;
	private static final int MY_EXPECTED_RANK_TYPE = ITEM_TYPE_SPECIAL + 5;
	private static final int MY_PERSON_NAME = ITEM_TYPE_SPECIAL + 6;
	private static final int MY_USER_RANK = ITEM_TYPE_SPECIAL + 7;
	private static final int MY_USER_AGENCY = ITEM_TYPE_SPECIAL + 8;
	private static final int MY_EQUIPMENT_NAME = ITEM_TYPE_SPECIAL + 9;
	private static final int MY_EQUIPMENT_AGENCY = ITEM_TYPE_SPECIAL + 10;
	private static final int MY_EQUIPMENT_TYPE = ITEM_TYPE_SPECIAL + 11;
	private static final int MY_USER_TYPE = ITEM_TYPE_SPECIAL + 12;
	private static final int MY_USER_EMPLOYEENO = ITEM_TYPE_SPECIAL + 13;
	
    private ViewItemDescriptor[] _equipmentItems = {
    		new ViewItemDescriptor( "Name", MY_EQUIPMENT_NAME, null ),
    		new ViewItemDescriptor( "Type", MY_EQUIPMENT_TYPE, null ),
    		new ViewItemDescriptor( "Agency", MY_EQUIPMENT_AGENCY, null),
    		new ViewItemDescriptor( "Status", MY_ASSET_STATUS, null ),
    		new ViewItemDescriptor( "Is Live Tracking?", ITEM_TYPE_BOOLEAN_CHECKBOX, Asset.IS_LIVE_TRACKING ),
    		new ViewItemDescriptor( "Address", MY_ADDRESS_TYPE, Asset.ADDRESS, true ),
        };
    private ViewItemDescriptor[] _userItems = {
    		new ViewItemDescriptor( "Name", MY_PERSON_NAME, null),
    		new ViewItemDescriptor( "Type", MY_USER_TYPE, null),
    		new ViewItemDescriptor( "Callsign", ITEM_TYPE_TEXT, Asset.CALLSIGN ),
    		new ViewItemDescriptor( "EmployeeNo", MY_USER_EMPLOYEENO, null),
    		new ViewItemDescriptor( "Rank", MY_USER_RANK, null),
    		new ViewItemDescriptor( "Agency", MY_USER_AGENCY, null),
    		new ViewItemDescriptor( "Status", MY_ASSET_STATUS, null ),
    		new ViewItemDescriptor( "Address", MY_ADDRESS_TYPE, Asset.ADDRESS, true ),
    		new ViewItemDescriptor( "Is Checked In?", ITEM_TYPE_BOOLEAN_CHECKBOX, Asset.IS_CHECKED_IN ),
    		new ViewItemDescriptor( "Check-In Location", MY_CHECKIN_LOCATION_TYPE, Asset.CHECK_IN_LOCATION, true ),
    		new ViewItemDescriptor( "Check-In Time", ITEM_TYPE_DATE_TIME, Asset.CHECKIN_TIME ),
    		new ViewItemDescriptor( "Check-Out Time", ITEM_TYPE_DATE_TIME, Asset.CHECKOUT_TIME ),
    		new ViewItemDescriptor( "Reporting Time", ITEM_TYPE_DATE_TIME, Asset.CHECKIN_BY_TIME ),
        };
    private ViewItemDescriptor[] _myItems;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_equipment);
		setTitle("View Asset");
        DateTime.setContext(this);
        if (getData() != null)
        {
        	Log.d(TAG, "viewing " + getData().toString());
			startManagingModel(_asset = Asset.query(this, getData()));
			if (_asset.getTypeID().equals(AssetType.EQUIPMENT))
			{
				_myItems = _equipmentItems;
			}
			else if (_asset.getTypeID().equals(AssetType.USER))
			{
				_myItems = _userItems;
			}
			_adapter = new MyAdapter();
			setListAdapter(_adapter);
			registerContentObserver(_adapter);
        }
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.view_asset_options_menu, menu);
	//	return super.onCreateOptionsMenu(menu);
		return true;
	}

    @Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
    	boolean mappable = false;
    	if (_asset != null)
    	{
    		mappable = _asset.isMappable(this);
    	}
		menu.findItem(R.id.menu_show_on_map).setVisible(mappable);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_show_on_map:
			onMenuShowOnMap();
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
		builder.setCancelable(true);
		builder.setTitle("Menu");
		builder.setItems(options,new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if("Show On Map".equals(options[which]))
						{
							if (_asset != null)
					    	{
								if(_asset.isMappable(ViewAssetActivity.this))
									onMenuShowOnMap();
								else
									Toast.makeText(getApplicationContext(), "No address found for this asset.",
											Toast.LENGTH_SHORT).show();					    		
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

	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		ViewItemDescriptor vid = _myItems[position];
		if (vid.type == MY_ASSET_TYPE)
		{
			if (_asset.getTypeID().equals(AssetType.EQUIPMENT))
			{
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(Equipment.CONTENT_URI, _asset.getEquipmentID())));
			}
			else if (_asset.getTypeID().equals(AssetType.USER))
			{
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(User.CONTENT_URI, _asset.getUserID())));
			}
		}
		else if (vid.type == MY_ADDRESS_TYPE)
		{
			String addressID = _asset.getAddressID();
			if (addressID != null)
			{
				Intent a =  new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(Address.CONTENT_URI, _asset.getAddressID()));
				a.putExtra("IsCheckedIn", _asset.getCursorInt(Asset.IS_CHECKED_IN));
				startActivity(a);
			}
		}
		else if (vid.type == MY_CHECKIN_LOCATION_TYPE)
		{
			String addressID = _asset.getCheckInLocationID();
			if (addressID != null)
			{
				Intent a = new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(Address.CONTENT_URI, addressID));
				a.putExtra("IsCheckedIn", _asset.getCursorInt(Asset.IS_CHECKED_IN));
				startActivity(a);
			}
		}
	}

	private void onMenuShowOnMap()
	{
		Intent intent = new Intent(this, MainMapOSMActivity.class);
		intent.setData(Uri.withAppendedPath(Address.CONTENT_URI, _asset.getAddressID()));
		startActivity(intent);
	}
		
	private final class MyAdapter extends ViewItemAdapter
	{
		public MyAdapter()
		{
			super(_asset, _myItems);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ViewItemDescriptor vid = _myItems[position];
			View result = null;
			switch (vid.type)
			{
			case MY_ADDRESS_TYPE:
				result = getAddressView(vid, _asset.getAddress(ViewAssetActivity.this));
				break;
			case MY_CHECKIN_LOCATION_TYPE: 
				result = getAddressView(vid, _asset.getCheckInLocation(ViewAssetActivity.this));
				break;
			case MY_ASSET_TYPE:
				AssetType assetType = _asset.getType(ViewAssetActivity.this);
				if (assetType != null)
				{
					String typeName = assetType.getName();
					if (typeName != null)
					{
						result = getTextView(vid, typeName);
					}
					else
					{
						result = getTextView(vid, _asset.getTypeID());
					}
				}
				else
				{
					result = getTextView(vid, _asset.getTypeID());
				}
				break;
			case MY_ASSET_STATUS:
				AssetStatus assetStatus = _asset.getStatus(ViewAssetActivity.this);
				if (assetStatus != null)
				{
					String statusName = assetStatus.getName();
					if (statusName != null)
					{
						result = getTextView(vid, statusName);
					}
					else
					{
						result = getTextView(vid, _asset.getStatusID());
					}
				}
				else
				{
					result = getTextView(vid, _asset.getStatusID());
				}
				break;
			case MY_EXPECTED_RANK_TYPE:
				result = getIndexedTypeView(vid, _asset.getExpectedRank(ViewAssetActivity.this));
				break;
			case MY_PERSON_NAME:
				result = getTextView(vid, getPersonName());
				break;
			case MY_USER_RANK:
				result = getTextView(vid, getUserRank());
				break;
			case MY_USER_AGENCY:
				result = getTextView(vid, getUserAgency());
				break;
			case MY_EQUIPMENT_NAME:
				result = getTextView(vid, getEquipmentName());
				break;
			case MY_EQUIPMENT_AGENCY:
				result = getTextView(vid, getEquipmentAgency());
				break;
			case MY_EQUIPMENT_TYPE:
				result = getEquipmentTypeView(vid);
				break;
			case MY_USER_TYPE:
				result = getUserTypeView(vid);
				break;
			case MY_USER_EMPLOYEENO:
				result = getTextView(vid,getEmployeeNo());
				break;
			default:
				result = super.getView(position, convertView, parent);
			}
			return result;
		}

		private String getPersonName()
		{
			String result = null;
			User user = _asset.getUser(ViewAssetActivity.this);
			if (user != null)
			{
				Person person = user.getPerson(ViewAssetActivity.this);
				if (person != null)
				{
					result = person.getName();
				}
			}
			return result;
		}
		
		private String getEmployeeNo()
		{
			String result = null;
			User user = _asset.getUser(ViewAssetActivity.this);
			if (user != null)
			{
				return user.getEmployeeNo();
				
			}
			return result;
		}

		private String getUserRank()
		{
			String result = null;
			User user = _asset.getUser(ViewAssetActivity.this);
			if (user != null)
			{
				UserRankType rank = user.getRank(ViewAssetActivity.this);
				if (rank != null && rank.getCount() == 1)
				{
					result = rank.getName();
				}
			}
			return result;
		}

		private String getUserAgency()
		{
			String result = null;
			User user = _asset.getUser(ViewAssetActivity.this);
			if (user != null)
			{
				Agency agency = user.getAgency(ViewAssetActivity.this);
				if (agency != null)
				{
					result = agency.getName();
				}
			}
			return result;
		}
		
		private String getEquipmentName()
		{
			String result = null;
	//		try
			{
				Equipment equip = _asset.getEquipment(ViewAssetActivity.this);
				if (equip != null)
				{
					result = equip.getName();
				}
			}
//			catch (Exception e)
//			{
////				e.printStackTrace();
//				result = "db error";
//			}
			return result;
		}

		private String getEquipmentAgency()
		{
			String result = null;
			Equipment equip = _asset.getEquipment(ViewAssetActivity.this);
			if (equip != null)
			{
				try{
					Agency agency = equip.getAgency(ViewAssetActivity.this);
					if (agency != null)
					{
						result = agency.getName();
					}
				}catch(Exception e)
				{
					return "Agency not synced";
				}
			}
			return result;
		}

		private View getEquipmentTypeView(ViewItemDescriptor vid)
		{
			View result = null;
			Equipment equipment = _asset.getEquipment(ViewAssetActivity.this);
			if (equipment != null)
			{
				EquipmentType equipmentType = equipment.getType(ViewAssetActivity.this);
				if (equipmentType != null)
				{
					result = getIndexedTypeView(vid, equipmentType);
				}
			}
			if (result == null)
			{
				result = getTextView(vid, "");
			}
			return result;
		}
		private View getUserTypeView(ViewItemDescriptor vid)
		{
			View result = null;
			User user = _asset.getUser(ViewAssetActivity.this);
			if (user != null)
			{
				UserType userType = user.getType(ViewAssetActivity.this);
				if (userType != null)
				{
					result = getIndexedTypeView(vid, userType);
				}
			}
			if (result == null)
			{
				result = getTextView(vid, "");
			}
			return result;
		}
	}
}
