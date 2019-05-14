package com.futureconcepts.ax.rex.activities;

import org.joda.time.DateTime;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Agency;
import com.futureconcepts.ax.model.data.Icon;
import com.futureconcepts.ax.model.data.IndexedType;
import com.futureconcepts.ax.rex.R;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewItemActivity extends ListActivity
{
//	private static final String TAG = ViewItemActivity.class.getSimpleName();
	
	public static final int ITEM_TYPE_TEXT = 0;
	public static final int ITEM_TYPE_INDEXED_TYPE = 1;
	public static final int ITEM_TYPE_LOCATION = 5;
	public static final int ITEM_TYPE_DATE_TIME = 6;
	public static final int ITEM_TYPE_DATE_TIME_RANGE = 7;
	public static final int ITEM_TYPE_INT = 8;
	public static final int ITEM_TYPE_REAL = 9;
	public static final int ITEM_TYPE_BOOLEAN_CHECKBOX = 11;
	public static final int ITEM_TYPE_BLOB_AS_STRING = 12;
	public static final int ITEM_TYPE_ADDRESS = 13;
	public static final int ITEM_TYPE_SPECIAL = 1000;

	private boolean _useDefaultOptionsMenu = false;
	
	private View[] _viewCache;
	
	private Uri _data;
		
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
	    super.onCreate(savedInstanceState);
	    _data = getIntent().getData();
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		if (useDefaultOptionsMenu())
		{
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.view_item_options_menu, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_edit:
			onMenuEditItem();
			break;
		case R.id.menu_delete:
			onMenuDeleteItem();
			break;
		}
		return false;
	}

	public Uri getData()
	{
		return _data;
	}
	
	public boolean useDefaultOptionsMenu()
    {
    	return _useDefaultOptionsMenu;
    }
    
    public void setDefaultOptionsMenu(boolean value)
    {
    	_useDefaultOptionsMenu = value;
    }  
	
	protected void onMenuEditItem()
	{
		startActivity(new Intent(Intent.ACTION_EDIT, _data));
	}

	protected void onMenuDeleteItem()
	{
		try
		{
			if (_data != null)
			{
				getContentResolver().delete(_data, null, null);
				finish();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	protected void clearViewCache()
	{
		for (int i = 0; i < _viewCache.length; i++)
		{
			_viewCache[i] = null;
		}
	}
	
	public class ViewItemDescriptor
	{
		public String displayName;
		public int type;
		public String source1;
		public String source2;
		public boolean isClickable;
		
		public ViewItemDescriptor(String displayName, int type, String source1, String source2)
		{
			this.displayName = displayName;
			this.type = type;
			this.source1 = source1;
			this.source2 = source2;
			this.isClickable = false;
		}
		public ViewItemDescriptor(String displayName, int type, String source1, boolean isClickable)
		{
			this(displayName, type, source1, null);
			this.isClickable = isClickable;
		}
		public ViewItemDescriptor(String displayName, int type, String source1)
		{
			this(displayName, type, source1, null);
		}
	}
    
	public class ViewItemAdapter extends BaseAdapter
	{
		private ViewItemDescriptor[] _items;
		private Cursor _cursor;
		private ViewStub _viewStub;
		
		public ViewItemAdapter(Cursor cursor, ViewItemDescriptor[] items)
		{
			_cursor = cursor;
			_items = items;
			_viewStub = new ViewStub(ViewItemActivity.this);
			_viewStub.setEnabled(false);
			_viewCache = new View[items.length];
		}

		@Override
		public int getCount() 
		{
			if (_cursor.getCount() == 0)
			{
				return 0;
			}
			else
			{
				return _items.length;
			}
		}

		@Override
		public Object getItem(int position)
		{
			return position;
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (_viewCache[position] == null)
			{
				_viewCache[position] = getItemView(_cursor, _items[position]);
			}
			return _viewCache[position];
		}
		
		@Override
		public boolean isEnabled(int position)
		{
			return _items[position].isClickable;
		}
		
		protected View getItemView(Cursor cursor, ViewItemDescriptor vid)
		{
			View result = _viewStub;
			try
			{
				int ci = cursor.getColumnIndex(vid.source1);
				if (ci != -1)
				{
					switch (vid.type)
					{
						case ITEM_TYPE_TEXT:
							result = getTextView(vid, cursor.getString(ci));
							break;
						case ITEM_TYPE_DATE_TIME:
							String strVal = cursor.getString(ci);
							if (strVal != null)
							{
								DateTime val = DateTime.parse(strVal);
								result = getDateTimeView(vid, val);
							}
							else
							{
								result = getTextView(vid, "");
							}
							break;
						case ITEM_TYPE_DATE_TIME_RANGE:
							result = getDateTimeRangeView(vid, cursor.getString(ci), cursor.getString(cursor.getColumnIndex(vid.source2)));
							break;
						case ITEM_TYPE_INT:
							result = getIntView(vid, cursor.getInt(ci));
							break;
						case ITEM_TYPE_REAL:
							result = getDoubleView(vid, cursor.getDouble(ci));
							break;
						case ITEM_TYPE_BOOLEAN_CHECKBOX:
							int val2 = cursor.getInt(ci);
							result = getBooleanCheckboxView(vid, val2 == 1);
							break;
						case ITEM_TYPE_BLOB_AS_STRING:
							result = getTextView(vid, new String(cursor.getBlob(ci)));
							break;
					}
				}
				if (result != null)
				{
					if (vid.isClickable)
					{
						ImageView imageView = (ImageView)result.findViewById(R.id.clickable);
						if (imageView != null)
						{
							imageView.setImageResource(android.R.drawable.ic_menu_more);
						}
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return result;
		}
						
		protected View getTextView(ViewItemDescriptor vid, String value)
		{
			View view = getNameValueListItem();
			((TextView)view.findViewById(R.id.name)).setText(vid.displayName);
			((TextView)view.findViewById(R.id.value)).setText(value);
			return view;
		}

		protected View getIndexedTypeView(ViewItemDescriptor vid, IndexedType type)
		{
			View view = getLayoutInflater().inflate(R.layout.view_indexed_type, null);
			((TextView)view.findViewById(R.id.label)).setText(vid.displayName);
			if (type != null)
			{
				((TextView)view.findViewById(R.id.value)).setText(type.getName());
				String iconID = type.getIconID();
				if (iconID != null)
				{
					setIconImage(view, type.getIcon(ViewItemActivity.this));
				}
			}
			return view;
		}
		
		protected View getLocationView(double latitude, double longitude)
		{
			View view = getNameValueListItem();
			((TextView)view.findViewById(R.id.name)).setText("Location");
			if (latitude != 0)
			{
				StringBuilder sb = new StringBuilder();
				sb.append(Location.convert(latitude, Location.FORMAT_DEGREES));
				sb.append(" ");
				sb.append(Location.convert(longitude, Location.FORMAT_DEGREES));
				((TextView)view.findViewById(R.id.value)).setText(sb.toString());
			}
			return view;
		}
		
		protected View getDateTimeView(ViewItemDescriptor vid, DateTime datetime)
		{
			View view = getNameValueListItem();
			((TextView)view.findViewById(R.id.name)).setText(vid.displayName);
			TextView valueView = (TextView)view.findViewById(R.id.value);
			valueView.setText(getFormattedLocalTime(datetime, null));
			return view;
		}
		
	    protected String getFormattedLocalTime(DateTime datetime, String nullText)
	    {
	    	String result = nullText;
	    	if (datetime != null)
	    	{
	    		result = datetime.toLocalDateTime().toString("MM/dd/yy HH:mm:ss");
	    	}
	    	return result;
	    }
	    
		protected View getDateTimeRangeView(ViewItemDescriptor vid, String startStr, String endStr)
		{
			View result = getNameValueListItem();
			TextView nameView = (TextView)result.getTag(R.id.name);
			TextView valueView = (TextView)result.getTag(R.id.value);
			nameView.setText(vid.displayName);
			if (startStr == null && endStr == null)
			{
				valueView.setText("None specified");
			}
			else
			{
				StringBuilder sb = new StringBuilder();
				if (startStr != null)
				{
					sb.append(getFormattedLocalTime(DateTime.parse(startStr), "?"));
				}
				sb.append(" to ");
				if (endStr != null)
				{
					sb.append(getFormattedLocalTime(DateTime.parse(endStr), "?"));
				}
				valueView.setText(sb);
			}
			return result;
		}
		
		protected View getIntView(ViewItemDescriptor vid, int value)
		{
			View result = getNameValueListItem();
			TextView nameView = (TextView)result.getTag(R.id.name);
			TextView valueView = (TextView)result.getTag(R.id.value);
			nameView.setText(vid.displayName);
			valueView.setText(Integer.toString(value));
			return result;
		}

		protected View getFloatView(ViewItemDescriptor vid, Float value)
		{
			View result = getNameValueListItem();
			TextView nameView = (TextView)result.getTag(R.id.name);
			TextView valueView = (TextView)result.getTag(R.id.value);
			nameView.setText(vid.displayName);
			valueView.setText(Float.toString(value));
			return result;
		}

		protected View getDoubleView(ViewItemDescriptor vid, double value)
		{
			View result = getNameValueListItem();
			TextView nameView = (TextView)result.getTag(R.id.name);
			TextView valueView = (TextView)result.getTag(R.id.value);
			nameView.setText(vid.displayName);
			valueView.setText(Double.toString(value));
			return result;
		}

		protected View getBooleanCheckboxView(ViewItemDescriptor vid, boolean value)
		{
			View view = getLayoutInflater().inflate(R.layout.boolean_as_checkbox_list_item, null);
			((TextView)view.findViewById(R.id.name)).setText(vid.displayName);
			CheckBox checkbox = (CheckBox)view.findViewById(R.id.value);
			if (checkbox != null)
			{
				checkbox.setChecked(value);
				checkbox.setClickable(false);
			}
			return view;
		}
		
		protected View getAgencyView(ViewItemDescriptor vid, Agency agency)
		{
			View result = this._viewStub;
			if (agency != null)
			{
				String agencyName = agency.getName();
				if (agencyName != null)
				{
					result = getTextView(vid, agencyName);
				}
				else
				{
					result = getTextView(vid, agency.getID());
				}
			}
			else
			{
				result = getTextView(vid,  "NULL");
			}
			return result;
		}
		protected View getAddressView(ViewItemDescriptor vid, Address address)
		{
			View result = null;
			if (address != null)
			{
				result = getTextView(vid, address.getMailingLabel(ViewItemActivity.this));
				result.findViewById(R.id.clickable).setVisibility(View.VISIBLE);
			}
			else
			{
				result = getTextView(vid, "None");
			}
			return result;
		}
		private void setIconImage(View view, Icon icon)
		{
			if (icon != null)
			{
				byte[] bytes = icon.getImage();
				if (bytes != null)
				{
					((ImageView)view.findViewById(R.id.icon)).setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length ));
				}
			}
		}
		
		private View getNameValueListItem()
		{
			View view = getLayoutInflater().inflate(R.layout.view_item_list_item, null);
			view.setTag(R.id.name, view.findViewById(R.id.name));
			view.setTag(R.id.value, view.findViewById(R.id.value));
			return view;
		}
	}
}
