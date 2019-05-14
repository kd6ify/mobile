package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class CollectionAttributeTactic extends BaseTable
{
    public static final String COLLECTION_ATTRIBUTE = "CollectionAttribute";
    public static final String TACTIC = "Tactic";

    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/CollectionAttributeTactic");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.CollectionAttributeTactic";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.CollectionAttributeTactic";
        
    private CollectionAttribute _collectionAttribute;
    private Tactic _tactic;
    
	public CollectionAttributeTactic(Context context, Cursor cursor)
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
		if (_collectionAttribute != null)
		{
			_collectionAttribute.close();
			_collectionAttribute = null;
		}
		if (_tactic != null)
		{
			_tactic.close();
			_tactic = null;
		}
	}
	
	public String getCollectionAttributeID()
	{
		return getCursorGuid(COLLECTION_ATTRIBUTE);
	}
	
	public CollectionAttribute getCollectionAttribute(Context context)
	{
		String id = getCollectionAttributeID();
		if (_collectionAttribute == null && id != null)
		{
			_collectionAttribute = CollectionAttribute.query(context, Uri.withAppendedPath(CollectionAttribute.CONTENT_URI, id));
		}
		return _collectionAttribute;
	}
	
	public static CollectionAttributeTactic query(Context context, Uri uri)
	{
		CollectionAttributeTactic result = null;
		try
		{
			result = new CollectionAttributeTactic(context, context.getContentResolver().query(uri, null, null, null, null));
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
	public static CollectionAttributeTactic query(Context context, String whereClause)
	{
		CollectionAttributeTactic result = null;
		try
		{
			result = new CollectionAttributeTactic(context, context.getContentResolver().query(CONTENT_URI, null, whereClause, null, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}
