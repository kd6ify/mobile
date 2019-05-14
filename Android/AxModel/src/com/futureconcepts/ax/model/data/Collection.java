package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class Collection extends AddressedTable
{
    public static final String TYPE = "Type";
    public static final String CALLSIGN = "Callsign";
    public static final String RESOURCE_TYPE = "ResourceType";
    public static final String TRACK_WITH_ASSET = "TrackWithAsset";
    public static final String IS_CLUSTERED = "IsClustered";
    public static final String ICON = "Icon";
    public static final String DESCRIPTION = "Description";
    
    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/Collection");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.Collection";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.Collection";
            
    private CollectionType _type;
    private Icon _icon;
    
	public Collection(Context context, Cursor cursor)
	{
		super(context, cursor);
	}

	@Override
	public void close()
	{
		closeReferences();
		super.close();
	}
	
	@Override
	public boolean requery()
	{
		closeReferences();
		return super.requery();
	}

	@Override
	public boolean moveToPosition(int position)
	{
		closeReferences();
		return super.moveToPosition(position);
	}
	
	private void closeReferences()
	{
		if (_type != null)
		{
			_type.close();
			_type = null;
		}
		if (_icon != null)
		{
			_icon.close();
			_icon = null;
		}
	}
	
	@Override
	public Uri getContentUri()
	{
		return Uri.withAppendedPath(CONTENT_URI, getID());
	}
	
	public String getCallsign()
	{
		return getCursorString(CALLSIGN);
	}

	
	public String getTypeID()
	{
		return getCursorGuid(TYPE);
	}
	
	public CollectionType getType(Context context)
	{
		String id = getTypeID();
		if (_type == null && id != null)
		{
			_type = CollectionType.query(context, Uri.withAppendedPath(CollectionType.CONTENT_URI, id));
		}
		return _type;
	}
	
	public String getIconID()
	{
		return getCursorGuid(ICON);
	}
	
	public Icon getIcon(Context context)
	{
		String id = getIconID();
		if (_icon == null && id != null)
		{
			_icon = Icon.query(context, Uri.withAppendedPath(Icon.CONTENT_URI, id));
		}
		return _icon;
	}
	
	public static Collection query(Context context, Uri uri)
	{
		Collection result = null;
		try
		{
			result = new Collection(context, context.getContentResolver().query(uri, null, null, null, null));
			if (result.getCount() == 1)
			{
				result.moveToFirst();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}
