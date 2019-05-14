package com.futureconcepts.jupiter;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.futureconcepts.awt.geom.Point2D;
import com.futureconcepts.jupiter.compass.MainCompassActivity;
import com.futureconcepts.jupiter.data.Folder;
import com.futureconcepts.jupiter.data.Placemark;
import com.futureconcepts.jupiter.util.FormatterFactory;
import com.futureconcepts.jupiter.util.FormatterFactory.LocationFormatter;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ViewPlacemarkActivity extends ListActivity
{
//	private static final String TAG = "ViewPlacemarkActivity";
	private Uri _uri;
	private Placemark _placemark;
	private MyAdapter _adapter;
	private FormatterFactory _formatterFactory;
	private LocationFormatter _formatter;
	
    public static final int ITEM_NAME = 0;
    public static final int ITEM_DESCRIPTION = 1;
    public static final int ITEM_LOCATION = 2;
    public static final int ITEM_MEDIA = 3;
    public static final int ITEM_COUNT = 4;
		
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_placemark);
        _formatterFactory = new FormatterFactory(this);
        _formatter = _formatterFactory.getLocationFormatter();
        _uri = getIntent().getData();
        if (_uri != null)
        {
			_placemark = new Placemark(managedQuery(_uri, null, null, null, null));
        }
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
		if (_placemark.getCount() == 1)
		{
			_placemark.moveToFirst();
        	setTitle("Placemark: " + _placemark.getName());
        	if (_adapter == null)
        	{
		        _adapter = new MyAdapter();
		        setListAdapter(_adapter);
        	}
        	else
        	{
        		_adapter.notifyDataSetChanged();
        	}
		}
    }
        
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.view_placemark_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_edit:
			onMenuEditPlacemark();
			break;
		case R.id.menu_delete:
			onMenuDeletePlacemark();
			break;
		case R.id.menu_location_format_degrees:
			setLocationFormat(Config.LOCATION_FORMAT_DEGREES);
			break;
		case R.id.menu_location_format_minutes:
			setLocationFormat(Config.LOCATION_FORMAT_MINUTES);
			break;
		case R.id.menu_location_format_seconds:
			setLocationFormat(Config.LOCATION_FORMAT_SECONDS);
			break;
		case R.id.menu_location_format_utm_nad27_conus:
			setLocationFormat(Config.LOCATION_FORMAT_NAD27);
			break;
		case R.id.menu_goto_using_compass:
			onMenuGotoUsingCompass();
			break;
		}
		return false;
	}

	private void setLocationFormat(String format)
	{
        _formatter = _formatterFactory.getLocationFormatter(format);
        if (_adapter != null)
        {
        	_adapter.notifyDataSetChanged();
        }
	}
	
	private void onMenuEditPlacemark()
	{
		Intent intent = new Intent(this, EditPlacemarkActivity.class);
		intent.setData(_uri);
		startActivity(intent);
	}
	
	private void onMenuDeletePlacemark()
	{
    	try
    	{
    		ContentResolver resolver = getContentResolver();
			if (_uri != null)
			{
				getContentResolver().delete(_uri, null, null);
				String parentId = _placemark.getParentId();
	            if (parentId != null)
	            {
	            	Folder parent = Folder.getFolderById(this, parentId);
	            	if (parent != null)
	            	{
	            		ContentValues values = new ContentValues();
	            		values.put(Folder.LAST_MODIFIED_TIME, System.currentTimeMillis());
	            		resolver.update(Uri.withAppendedPath(Folder.CONTENT_URI, parent.get_ID()), values, null, null);
	            		parent.close();
	            		parent = null;
	            		finish();
	            	}
	            }
			}
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    }

	private void onMenuGotoUsingCompass()
	{
		Intent intent = new Intent(this, MainCompassActivity.class);
		intent.setData(_uri);
		intent.setAction(Intent.ACTION_VIEW);
		startActivity(intent);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		if (position == ITEM_LOCATION)
		{
			Intent newIntent = new Intent(Intent.ACTION_VIEW);
			newIntent.addCategory(JIntent.CATEGORY_MAP);
			newIntent.setData(_uri);
			newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(newIntent);
		}
		else if (position == ITEM_MEDIA)
		{
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(_placemark.getMediaUrl()));
			startActivity(intent);
		}
	}
	
	private final class MyAdapter extends BaseAdapter
	{
		private SimpleDateFormat mDateFormat = new SimpleDateFormat();
		
		public MyAdapter()
		{
		}

		public int getCount() 
		{
			return ITEM_COUNT;
		}

		public Object getItem(int position)
		{
			return position;
		}

		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public int getItemViewType(int position)
		{
			if (position == ITEM_LOCATION)
			{
				if (_placemark.getLatitude() == 0.0d)
				{
					return Adapter.IGNORE_ITEM_VIEW_TYPE;
				}
			}
			return 0;
		}
		
		@Override
		public boolean isEnabled(int position)
		{
			if (position == ITEM_LOCATION)
			{
				if (_placemark.getLatitude() == 0.0d)
				{
					return false;
				}
				return true;
			}
			else if (position == ITEM_MEDIA)
			{
				if (_placemark.getMediaUrl() != null)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}
		
		public View getView(int position, View convertView, ViewGroup parent)
		{
			switch (position)
			{
			case ITEM_NAME:
				convertView = getText(convertView, "Name", _placemark.getName());
				break;
			case ITEM_DESCRIPTION:
				convertView = getText(convertView, "Description", _placemark.getName());
				break;
			case ITEM_LOCATION:
				if (_placemark.getLatitude() != 0.0d)
				{
					Point2D.Double point = new Point2D.Double(_placemark.getLongitude(), _placemark.getLatitude());
					convertView = getText(convertView, "Location", _formatter.format(point));
				}
				else
				{
					convertView = getText(convertView, "Location", "");
				}
				break;
			case ITEM_MEDIA:
				convertView = getText(convertView, "Media", _placemark.getMediaUrl() != null ? "Show" : "None");
				break;
			default:
				convertView = getText(convertView, "?", "?");
				break;
			}
			return convertView;
		}
		
		private View getText(View convertView, String name, String value)
		{
			if (convertView == null)
			{
				convertView = getNameValueListItem();
			}
			TextView nameView = (TextView)convertView.getTag(R.id.name);
			TextView valueView = (TextView)convertView.getTag(R.id.value);
			nameView.setText(name);
			valueView.setText(value);
			return convertView;
		}
		
		private View getDateTime(View convertView, String name, long time)
		{
			if (convertView == null)
			{
				convertView = getNameValueListItem();
			}
			TextView nameView = (TextView)convertView.getTag(R.id.name);
			TextView valueView = (TextView)convertView.getTag(R.id.value);
			nameView.setText(name);
			if (time != 0)
			{
				valueView.setText(mDateFormat.format(new Date(time)));
			}
			else
			{
				valueView.setText(null);
			}
			return convertView;
		}

		private View getTimeRange(View convertView, String name, long start, long end)
		{
			if (convertView == null)
			{
				convertView = getNameValueListItem();
			}
			TextView nameView = (TextView)convertView.getTag(R.id.name);
			TextView valueView = (TextView)convertView.getTag(R.id.value);
			nameView.setText(name);
			if (start == 0 && end == 0)
			{
				valueView.setText("None specified");
			}
			else
			{
				StringBuilder sb = new StringBuilder();
				if (start != 0)
				{
					sb.append(mDateFormat.format(new Date(start)));
				}
				else
				{
					sb.append("?");
				}
				sb.append(" to ");
				if (end != 0)
				{
					sb.append(mDateFormat.format(new Date(end)));
				}
				else
				{
					sb.append("?");
				}
				valueView.setText(sb);
			}
			return convertView;
		}
		
		private View getInt(View convertView, String name, int value)
		{
			if (convertView == null)
			{
				convertView = getNameValueListItem();
			}
			TextView nameView = (TextView)convertView.getTag(R.id.name);
			TextView valueView = (TextView)convertView.getTag(R.id.value);
			nameView.setText(name);
			valueView.setText(Integer.toString(value));
			return convertView;
		}

		private View getFloat(View convertView, String name, Float value)
		{
			if (convertView == null)
			{
				convertView = getNameValueListItem();
			}
			TextView nameView = (TextView)convertView.getTag(R.id.name);
			TextView valueView = (TextView)convertView.getTag(R.id.value);
			nameView.setText(name);
			valueView.setText(Float.toString(value));
			return convertView;
		}

		private View getDouble(View convertView, String name, Double value)
		{
			if (convertView == null)
			{
				convertView = getNameValueListItem();
			}
			TextView nameView = (TextView)convertView.getTag(R.id.name);
			TextView valueView = (TextView)convertView.getTag(R.id.value);
			nameView.setText(name);
			valueView.setText(Double.toString(value));
			return convertView;
		}

		private View getNameValueListItem()
		{
			View view = getLayoutInflater().inflate(R.layout.view_placemark_list_item, null);
			view.setTag(R.id.name, view.findViewById(R.id.name));
			view.setTag(R.id.value, view.findViewById(R.id.value));
			return view;
		}
	}
}