package com.futureconcepts.ax.trinity.collectives;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Collection;
import com.futureconcepts.ax.model.data.CollectionAttribute;
import com.futureconcepts.ax.model.data.CollectionType;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.ViewItemActivity;
import com.futureconcepts.ax.trinity.assets.ViewUserActivity;

public class ViewCollectionActivity extends ViewItemActivity
{
	private Collection _collection;
//	private CollectionAttribute _collectionAtribute;
	private Address _address;
	private MyAdapter _adapter;

	private static final int MY_ADDRESS_TYPE = ITEM_TYPE_SPECIAL + 1;
	private static final int MY_COLLECTION_TYPE = ITEM_TYPE_SPECIAL + 2;
	
    private ViewItemDescriptor[] _myItems = {
    		new ViewItemDescriptor( "Callsign", ITEM_TYPE_TEXT, Collection.CALLSIGN ),
    		new ViewItemDescriptor( "Type", MY_COLLECTION_TYPE, null ),
    		new ViewItemDescriptor( "Is Clustered", ITEM_TYPE_BOOLEAN_CHECKBOX, Collection.IS_CLUSTERED ),
    		new ViewItemDescriptor( "Description", ITEM_TYPE_TEXT, Collection.DESCRIPTION),
        };
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_collective);
        if (getData() != null)
        {
			startManagingModel(_collection = Collection.query(this, getData()));
		//	startManagingCursor(_collectionAtribute = CollectionAttribute.query(this,CollectionAttribute.COLLECTION+"='"+_collection.getID()+"'"));
		//	Log.e("asd","asda====: "+_collectionAtribute.getColumnName(_collectionAtribute.getColumnIndex(CollectionAttribute.LEADER)));
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
    		moveToFirst();
    		setTitle("Collective: " + _collection.getCallsign());
    	}
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.view_collection_options_menu, menu);
	//	return super.onCreateOptionsMenu(menu);
		return true;
	}

    @Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
	//	super.onPrepareOptionsMenu(menu);
//		menu.findItem(R.id.menu_show_on_map).setVisible(_equipment.getLocation() != null);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		}
		return false;
	}
	
	public void goBack(View view)
	{
		finish();
	}
	public void displayMenuOptions(View view)
	{
		final String[] options = {"No Options"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle("Menu");
		builder.setItems(options,new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
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
	}

	private final class MyAdapter extends ViewItemAdapter
	{
		public MyAdapter()
		{
			super(_collection, _myItems);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ViewItemDescriptor vid = _myItems[position];
			View result = null;
			switch (vid.type)
			{
			case MY_ADDRESS_TYPE:
				result = getAddressView(vid, _address);
				break;
			case MY_COLLECTION_TYPE:
				CollectionType collectionType = _collection.getType(ViewCollectionActivity.this);
				if (collectionType != null)
				{
					result = getIndexedTypeView(vid, collectionType);
//					String typeName = collectionType.getName();
//					if (typeName != null)
//					{
//						result = getTextView(vid, typeName);
//					}
//					else
//					{
//						result = getTextView(vid, _collection.getTypeID());
//					}
				}
				else
				{
					result = getTextView(vid, _collection.getTypeID());
				}
				break;
			default:
				result = super.getView(position, convertView, parent);
			}
			return result;
		}
	}
}
