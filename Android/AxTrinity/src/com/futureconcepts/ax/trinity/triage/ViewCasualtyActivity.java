package com.futureconcepts.ax.trinity.triage;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Gender;
import com.futureconcepts.ax.model.data.Icon;
import com.futureconcepts.ax.model.data.Person;
import com.futureconcepts.ax.model.data.Triage;
import com.futureconcepts.ax.model.data.TriageColor;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.ViewItemActivity;
import com.futureconcepts.ax.trinity.osm.MainMapOSMActivity;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ViewCasualtyActivity extends ViewItemActivity
{
	private Triage _triage;
	private MyObserver _observer;
	private MyAdapter _adapter;

	private static final int MY_GENDER_TYPE = ITEM_TYPE_SPECIAL + 1;
	private static final int MY_ADDRESS_TYPE = ITEM_TYPE_SPECIAL + 2;
	
	private ViewItemDescriptor[] _myItems = {
    		new ViewItemDescriptor( "Tracking ID", ITEM_TYPE_TEXT, Triage.TRACKING_ID ),
    		new ViewItemDescriptor( "Color", ITEM_TYPE_INDEXED_TYPE, Triage.COLOR ),
    		new ViewItemDescriptor( "Status", ITEM_TYPE_INDEXED_TYPE, Triage.STATUS ),
    		new ViewItemDescriptor( "Gender", MY_GENDER_TYPE, null),
    		new ViewItemDescriptor( "Address", MY_ADDRESS_TYPE, null, true),
    };
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_casualty);
        try
        {
        	startManagingModel(_triage = Triage.query(this, getData()));
        	if (moveToFirstIfOneRow())
        	{
	        	setTitle("Triage Casualty Information");
				_adapter = new MyAdapter();
		        setListAdapter(_adapter);
		        _observer = new MyObserver(new Handler());
		        registerContentObserver(getData(), false, _observer);
		        Person person = _triage.getPerson(this);
		        if (person != null && person.getCount() == 1)
		        {
		        	Uri personUri = Uri.withAppendedPath(Person.CONTENT_URI, person.getID());
		        	registerContentObserver(personUri, false, _observer);
		        }
//		        setDefaultOptionsMenu(true);
        	}
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }
    }

    @Override
    public void onResume()
    {
    	super.onResume();
    	if (_triage != null)
    	{
    		if (_triage.getCount() == 1)
    		{
    			_triage.moveToFirst();
    		}
    	}
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.view_casualty_options_menu, menu);
		if(_triage.getAddress(ViewCasualtyActivity.this)==null){
			MenuItem item = menu.findItem(R.id.menu_show_on_map);
		    item.setVisible(false);
		}
		return super.onCreateOptionsMenu(menu);
	}
    

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_edit:
			startActivity(new Intent(Intent.ACTION_EDIT, getData()));
			break;
		case R.id.menu_delete:
			onMenuDelete();
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
		final String[] options = {"Edit","Show On Map"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle("Menu");
		builder.setItems(options,new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if("Edit".equals(options[which]))
						{
							startActivity(new Intent(Intent.ACTION_EDIT, getData()));
						}else if("Show On Map".equals(options[which]))
						{
							if(_triage.getAddress(ViewCasualtyActivity.this)==null){
								Toast.makeText(getApplicationContext(),
										"No location found for this Casualty", Toast.LENGTH_SHORT).show();
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
	

	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		ViewItemDescriptor vid = _myItems[position];
		if (vid.type == MY_ADDRESS_TYPE)
		{
			String addressID = _triage.getAddressID();
			if (addressID != null)
			{
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(Address.CONTENT_URI, _triage.getAddressID())));
			}
		}
	}
				
	private void onMenuDelete()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to delete this casualty record?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    	   public void onClick(DialogInterface dialog, int id)
		    	   {
		    		   ContentResolver resolver = getContentResolver();
		    		   resolver.delete(getData(), null, null);
		    		   finish();
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id)
		           {
		        	   dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void onMenuShowOnMap()
	{		
		Intent intent = new Intent(this, MainMapOSMActivity.class);
		intent.setData(Uri.withAppendedPath(Address.CONTENT_URI, _triage.getAddressID()));
		startActivity(intent);
	}
		
	private class MyAdapter extends ViewItemAdapter
	{
		public MyAdapter()
		{
			super(_triage, _myItems);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View result = null;
			ViewItemDescriptor vid = _myItems[position];
			if ( (_triage != null) && (_triage.getCount() == 1))
			{
				if (vid.type == ITEM_TYPE_INDEXED_TYPE)
				{
					if (vid.displayName.equals("Color"))
					{
						TriageColor triageColor = _triage.getColor(ViewCasualtyActivity.this);
						if (triageColor != null)
						{
							result = super.getIndexedTypeView(vid, _triage.getColor(ViewCasualtyActivity.this));
							setValueTextColor(result, _triage.getColorID());
						}
						else
						{
							result = getTextView(vid, "not specified");
						}
					}
					else if (vid.displayName.equals("Status"))
					{
						result = super.getIndexedTypeView(vid, _triage.getStatus(ViewCasualtyActivity.this));
					}
				}
				else if (vid.type == MY_GENDER_TYPE)
				{
					Person person = _triage.getPerson(ViewCasualtyActivity.this);
					if (person != null && person.getCount() == 1)
					{
						Gender gender = person.getGender(ViewCasualtyActivity.this);
						if (gender != null && gender.getCount() == 1)
						{
							result = super.getIndexedTypeView(vid, gender);
						}
						else
						{
							result = getTextView(vid, "not specified");
						}
					}
					else
					{
						result = getTextView(vid, "no person");
					}
				}
				else if (vid.type == MY_ADDRESS_TYPE)
				{
					result = getAddressView(vid, _triage.getAddress(ViewCasualtyActivity.this));
				}
				else
				{
					result = super.getView(position, convertView, parent);
				}
			}
			else
			{
				result = this.getTextView(vid, "entry deleted");
			}
			return result;
		}
	}
	
	private void setValueTextColor(View view, String colorID)
	{
		TextView textView = (TextView)view.findViewById(R.id.value);
		if (textView != null)
		{
			if (colorID.equals(TriageColor.YELLOW))
			{
				textView.setTextColor(Color.YELLOW);
			}
			else if (colorID.equals(TriageColor.RED))
			{
				textView.setTextColor(Color.RED);
			}
			else if (colorID.equals(TriageColor.GREEN))
			{
				textView.setTextColor(Color.GREEN);
			}
		}
	}
	private void setImageViewIcon(View view, int resid, Icon icon)
	{
		ImageView imageView = (ImageView)view.findViewById(resid);
		if (icon != null)
		{
			byte[] bytes = icon.getImage();
			if (bytes != null)
			{
				imageView.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
			}
		}
		else
		{
			imageView.setImageResource(R.drawable.unknown);
		}
	}
	
	private final class MyObserver extends ContentObserver
	{
		public MyObserver(Handler handler)
        {
	        super(handler);
        }
		
		@Override
		public void onChange(boolean selfChange)
		{
			if (_triage != null)
			{
				try
				{
					_triage.requery();
					if (_triage.getCount() == 1)
					{
						_triage.moveToFirst();
						if (getTitle().equals(_triage.getTrackingID()) == false)
						{
							setTitle(_triage.getTrackingID());
						}
					}
					if (_adapter != null)
					{
						clearViewCache();
						_adapter.notifyDataSetChanged();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
