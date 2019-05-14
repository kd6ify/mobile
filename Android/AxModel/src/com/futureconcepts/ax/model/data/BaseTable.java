package com.futureconcepts.ax.model.data;

import java.util.Date;

import org.joda.time.DateTime;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.provider.BaseColumns;

public class BaseTable extends CursorWrapper implements BaseColumns
{
    public static final String AUTHORITY = "com.futureconcepts.ax.sync.provider.icdb";
	
	public static final String ID = "ID";
	public static final String OWNER = "Owner";
	public static final String UPLOAD = "UploadToServer";
	public static final String LAST_MODIFIED = "LastModified";
	
	private Context _context;
    private ContentValues _contentValues;
    protected Uri _contentUri;
    private PropertyChangedListener _propertyChangedListener;
    
	public BaseTable(Context context, Cursor cursor)
    {
	    super(cursor != null ? cursor : new EmptyCursor());
	    _context = context;
    }
    
	public Context getContext()
	{
		return _context;
	}
	
	public void beginEdit()
	{
		_contentValues = new ContentValues();
		if (getCount() != 0)
		{
			_contentValues.put(ID, getCursorString(ID));
			_contentValues.put(OWNER, getCursorString(OWNER));
		}
	}

	public ContentValues endEdit()
	{
		ContentValues result = _contentValues;
		return result;
	}
	
	public ContentValues endEditAndUpload(String uploadAction)
	{
		ContentValues result = _contentValues;
		if (result != null)
		{
			result.put(UPLOAD, uploadAction);
		}
		return result;
	}

	public void setPropertyChangedListener(PropertyChangedListener listener)
	{
		_propertyChangedListener = listener;
	}
	
	public void notifyPropertyChanged(String propertyName, Object newValue)
	{
		if (_propertyChangedListener != null)
		{
			_propertyChangedListener.propertyChanged(propertyName, newValue);
		}
	}
	
	public String getCursorString(String key)
	{
		return getString(getColumnIndex(key));
	}
	
	public DateTime getCursorDateTime(String key)
	{
		DateTime result = null;
		String iso8601 = getString(getColumnIndex(key));
		if (iso8601 != null)
		{
			try
			{
				result = DateTime.parse(iso8601);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return result;
	}

	public String getCursorDateTimeDefaultLocalFormat(String key)
	{
		String result = null;
		DateTime dateTime = getCursorDateTime(key);
		if (dateTime != null)
		{
			result = dateTime.toLocalDateTime().toString("M/d/y H:m:s");
		}
		return result;
	}
	
	public String getCursorGuid(String key)
	{
		return getString(getColumnIndex(key));
	}

	public int getCursorInt(String key)
	{
		return getInt(getColumnIndex(key));
	}
	
	public long getCursorLong(String key)
	{
		return getLong(getColumnIndex(key));
	}
	
	public byte[] getCursorBlob(String key)
	{
		return getBlob(getColumnIndex(key));
	}
	
	public long getCursorDateAsLong(String key)
	{
		return getLong(getColumnIndex(key));
	}
	
	public Date getCursorDateAsDate(String key)
	{
		return new Date(getLong(getColumnIndex(key)));
	}
	
	public boolean getCursorBoolean(String key)
	{
		int value = getInt(getColumnIndex(key));
		return value != 0;
	}
	
	public double getCursorDouble(String key)
	{
		return getDouble(getColumnIndex(key));
	}
	
	public String getModelString(String key)
	{
		if (_contentValues != null)
		{
			return _contentValues.getAsString(key);
		}
		else
		{
			return getString(getColumnIndex(key));
		}
	}

	public DateTime getModelDateTime(String key)
	{
		if (_contentValues != null)
		{
			return DateTime.parse(_contentValues.getAsString(key));
		}
		else
		{
			return getCursorDateTime(key);
		}
	}
	
	public String getModelGuid(String key)
	{
		String result = null;
		if (_contentValues != null)
		{
			result = _contentValues.getAsString(key);
			if (result != null)
			{
				result = result.toUpperCase();
			}
		}
		else
		{
			result = getString(getColumnIndex(key));
			if (result != null)
			{
				result.toUpperCase();
			}
		}
		return result;
	}
	
	public void setModel(String key, String value)
	{
		if (_contentValues.containsKey(key) == false)
		{
			_contentValues.put(key, value);
		}
		else
		{
			Object oldValue = _contentValues.get(key);
			if (oldValue == null || oldValue.equals(value) == false)
			{
				_contentValues.put(key, value);
				notifyPropertyChanged(key, value);
			}
		}
	}
	
	public void setModel(String key, long value)
	{
		if (_contentValues.containsKey(key) == false)
		{
			_contentValues.put(key, value);
		}
		else
		{
			if (_contentValues.get(key).equals(value) == false)
			{
				_contentValues.put(key, value);
				notifyPropertyChanged(key, value);
			}
		}
	}

	public void setModel(String key, int value)
	{
		if (_contentValues.containsKey(key) == false)
		{
			_contentValues.put(key, value);
		}
		else
		{
			if (_contentValues.get(key).equals(value) == false)
			{
				_contentValues.put(key, value);
				notifyPropertyChanged(key, value);
			}
		}
	}
	
	public void setModel(String key, DateTime value)
	{
		if (_contentValues.containsKey(key) == false)
		{
			_contentValues.put(key, value.toString());
		}
		else
		{
			if (_contentValues.get(key).equals(value) == false)
			{
				_contentValues.put(key, value.toString());
				notifyPropertyChanged(key, value);
			}
		}
	}
	
	public int getModelInt(String key)
	{
		if (_contentValues != null)
		{
			return _contentValues.getAsInteger(key);
		}
		else
		{
			return getInt(getColumnIndex(key));
		}
	}
	
	public long getModelLong(String key)
	{
		if (_contentValues != null)
		{
			return _contentValues.getAsLong(key);
		}
		else
		{
			return getLong(getColumnIndex(key));
		}
	}
	
	public String getID()
	{
		String id = getModelGuid(ID);
		if (id != null)
		{
			return id.toUpperCase();
		}
		else
		{
			return null;
		}
	}
	
	public void setID(String value)
	{
		setModel(ID, value);
	}
	
	public void setOwner(String value)
	{
		setModel(OWNER, value);
	}
	
	public void setUpload(String value)
	{
		setModel(UPLOAD, value);
	}
	
	public Uri getContentUri()
	{
		return null;
	}
	
    public String getOwner()
    {
    	return getModelString(OWNER);
    }
}
