package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class CollectionAttribute extends BaseTable
{
    public static final String PARENT = "Parent";
    public static final String OPERATIONAL_PERIOD = "OperationalPeriod";
    public static final String COLLECTION = "Collection";
    public static final String LEADER = "Leader";
    public static final String STATUS = "Status";

    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/CollectionAttribute");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.CollectionAttribute";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.CollectionAttribute";
        
    private Collection _collection;
    
	public CollectionAttribute(Context context, Cursor cursor)
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
		if (_collection != null)
		{
			_collection.close();
			_collection = null;
		}
	}
	
	public String getCollectionID()
	{
		return getCursorGuid(COLLECTION);
	}
	
	public Collection getCollection(Context context)
	{
		String id = getCollectionID();
		if (_collection == null && id != null)
		{
			_collection = Collection.query(context, Uri.withAppendedPath(Collection.CONTENT_URI, id));
		}
		return _collection;
	}

	public String getOperationalPeriodID()
	{
		return getCursorGuid(OPERATIONAL_PERIOD);
	}
	
	public static CollectionAttribute query(Context context, String whereClause)
	{
		CollectionAttribute result = new CollectionAttribute(context, context.getContentResolver().query(CONTENT_URI, null, whereClause, null, null));
		return result;
	}

	public static CollectionAttribute query(Context context, Uri uri)
	{
		CollectionAttribute result = null;
		try
		{
			result = new CollectionAttribute(context, context.getContentResolver().query(uri, null, null, null, null));
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
