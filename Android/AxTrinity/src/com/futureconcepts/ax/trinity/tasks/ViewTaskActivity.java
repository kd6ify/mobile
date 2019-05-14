package com.futureconcepts.ax.trinity.tasks;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Tactic;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.ViewItemActivity;
import com.futureconcepts.ax.trinity.assets.ViewTaskAssetsActivity;
import com.futureconcepts.ax.trinity.collectives.ViewTaskCollectionsActivity;
import com.futureconcepts.ax.trinity.osm.MainMapOSMActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

public class ViewTaskActivity extends ViewItemActivity
{
	private Tactic _tactic;
	private MyAdapter _adapter;

	private static final int MY_ADDRESS_TYPE = ITEM_TYPE_SPECIAL + 1;

    private ViewItemDescriptor[] _myItems = {
		new ViewItemDescriptor ( "Name", ITEM_TYPE_TEXT, Tactic.NAME ),
		new ViewItemDescriptor( "Type", ITEM_TYPE_INDEXED_TYPE, null),
		new ViewItemDescriptor( "Start", ITEM_TYPE_DATE_TIME, Tactic.START),
		new ViewItemDescriptor( "End", ITEM_TYPE_DATE_TIME, Tactic.END),
		
		new ViewItemDescriptor( "Requires Radio", ITEM_TYPE_BOOLEAN_CHECKBOX, Tactic.REQUIRES_RADIO),
		new ViewItemDescriptor( "Requires Vehicle", ITEM_TYPE_BOOLEAN_CHECKBOX, Tactic.REQUIRES_VEHICLE),
		new ViewItemDescriptor( "Vehicle Requirements", ITEM_TYPE_TEXT, Tactic.SPECIAL_EQUIPMENT),
		new ViewItemDescriptor( "Requires Other", ITEM_TYPE_BOOLEAN_CHECKBOX, Tactic.REQUIRES_OTHER),
		new ViewItemDescriptor( "Requirements Description", ITEM_TYPE_TEXT, Tactic.REQUIREMENT_DESCRIPTION),
		new ViewItemDescriptor( "Description", ITEM_TYPE_TEXT, Tactic.NOTES),
		new ViewItemDescriptor( "Address", MY_ADDRESS_TYPE, null, true)
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_tactic);
        if (getData() != null)
        {
			startManagingModel(_tactic = new Tactic(this, getContentResolver().query(getData(), null, null, null, null)));
			if (moveToFirstIfOneRow())
	    	{
	    		setTitle("Task: " + _tactic.getName());
    			_adapter = new MyAdapter();
    			setListAdapter(_adapter);
    			registerContentObserver(_adapter);
	    	}
        }
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.view_tactic_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

    @Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
    	boolean mappable = false;
    	if (_tactic != null)
    	{
    		mappable = _tactic.isMappable(this);
    	}
		menu.findItem(R.id.menu_show_on_map).setVisible(mappable);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_assets:
			onMenuAssets();
			break;
		case R.id.menu_collectives:
			onMenuCollectives();
			break;
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
		final String[] options = {"Assets","Collectives","Show On Map","Edit Task"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle("Menu");
		builder.setItems(options,new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if("Edit Task".equals(options[which]))
						{
							Intent edit = new Intent(ViewTaskActivity.this ,AddEditTaskActivity.class);
							edit.setAction(Intent.ACTION_EDIT);
							edit.setData(getData());
							startActivity(edit);
							finish();
						}else if("Assets".equals(options[which]))
						{
							onMenuAssets();
						}else if("Collectives".equals(options[which]))
						{
							onMenuCollectives();
						}else if("Show On Map".equals(options[which]))
						{
							if (_tactic != null)
					    	{
								if(_tactic.isMappable(ViewTaskActivity.this))
								{
									onMenuShowOnMap();
								}else{
								Toast.makeText(getApplicationContext(), "No address found for this Task.",
										Toast.LENGTH_SHORT).show();	
								}
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
		if (vid.type == MY_ADDRESS_TYPE)
		{
			String addressID = _tactic.getAddressID();
			if (addressID != null)
			{
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(Address.CONTENT_URI, _tactic.getAddressID())));
			}
		}
	}
    
	private void onMenuAssets()
	{
		Intent intent = new Intent(this, ViewTaskAssetsActivity.class);
		intent.setData(getData());
		startActivity(intent);
	}

	private void onMenuCollectives()
	{
		Intent intent = new Intent(this, ViewTaskCollectionsActivity.class);
		intent.setData(getData());
		startActivity(intent);
	}

	private void onMenuShowOnMap()
	{
		Intent intent = new Intent(this, MainMapOSMActivity.class);
		intent.setData(Uri.withAppendedPath(Address.CONTENT_URI, _tactic.getAddressID()));
		startActivity(intent);
	}
	
    private final class MyAdapter extends ViewItemAdapter
    {
    	public MyAdapter()
    	{
    		super(_tactic, _myItems);
    		DateTime.setContext(ViewTaskActivity.this);
    		DateTimeZone.setProvider(null);
    	}
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View result = null;
			ViewItemDescriptor vid = _myItems[position];
			if (vid.type == MY_ADDRESS_TYPE)
			{
				result = getAddressView(vid, _tactic.getAddress(ViewTaskActivity.this));
			}
			else if (vid.type == ITEM_TYPE_INDEXED_TYPE)
			{
				if (vid.displayName.equals("Type"))
				{
					result = getIndexedTypeView(vid, _tactic.getType(ViewTaskActivity.this));
				}
				else if (vid.displayName.equals("Priority"))
				{
					result = getIndexedTypeView(vid, _tactic.getPriority(ViewTaskActivity.this));
				}
				else
				{
					result = getIndexedTypeView(vid, _tactic.getStatus(ViewTaskActivity.this));
				}
			}
			else
			{
				result = super.getView(position, convertView, parent);
			}
			return result;
		}
    }
}