package com.futureconcepts.mercury.update;

import com.futureconcepts.mercury.R;
import com.futureconcepts.mercury.download.Downloads;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
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
	public static final int ITEM_TYPE_DOWNLOAD_STATUS = 13;
	public static final int ITEM_TYPE_SPECIAL = 1000;

	private boolean _useDefaultOptionsMenu = false;
	
	private View[] _viewCache;
		
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
	    super.onCreate(savedInstanceState);
	}
    
	public boolean useDefaultOptionsMenu()
    {
    	return _useDefaultOptionsMenu;
    }
    
    public void setDefaultOptionsMenu(boolean value)
    {
    	_useDefaultOptionsMenu = value;
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
						case ITEM_TYPE_DOWNLOAD_STATUS:
							result = getDownloadStatusView(vid, cursor.getInt(ci));
							break;
					}
				}
				if (result != null)
				{
					if (vid.isClickable)
					{
//						ImageView imageView = (ImageView)result.findViewById(R.id.clickable);
//						if (imageView != null)
//						{
//							imageView.setImageResource(android.R.drawable.ic_menu_more);
//						}
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
				
		protected View getDownloadStatusView(ViewItemDescriptor vid, int status)
		{
			String value = null;
			View result = getNameValueListItem();
			TextView nameView = (TextView)result.getTag(R.id.name);
			TextView valueView = (TextView)result.getTag(R.id.value);
			nameView.setText(vid.displayName);
			switch (status)
			{
			case 404:
				value = "Package was not found.";
				break;
			case Downloads.STATUS_PENDING:
				value = "This download hasn't started yet";
				break;
			case Downloads.STATUS_RUNNING:
				value = "This download has started";
				break;
			case Downloads.STATUS_NOT_ACCEPTABLE:
				value = "This download can't be performed because the content type cannot be handled";
				break;
			case Downloads.STATUS_UNKNOWN_ERROR:
				value = "Unknown Error: This download has completed with an error";
				break;
			case Downloads.STATUS_UNHANDLED_REDIRECT:
				value = "This download couldn't be completed because of an HTTP " +
						"redirect response that the download manager couldn't handle";
				break;
			case Downloads.STATUS_INSUFFICIENT_SPACE_ERROR:
				value = "This download couldn't be completed due to insufficient storage space. " +
						"Typically, this is because the SD card is full.";
				break;
			case Downloads.STATUS_DEVICE_NOT_FOUND_ERROR:
				value = "This download couldn't be completed because no external storage " +
						"device was found.  Typically, this is because the SD card is not mounted.";
				break;
			default:
				value = "?";
				break;
			}
			valueView.setText(value);
			return result;
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
