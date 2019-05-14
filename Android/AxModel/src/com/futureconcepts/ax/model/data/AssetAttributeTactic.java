package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class AssetAttributeTactic extends BaseTable
{
    public static final String ASSET_ATTRIBUTE = "AssetAttribute";
    public static final String TACTIC = "Tactic";

    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/AssetAttributeTactic");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.AssetAttributeTactic";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.AssetAttributeTactic";
        
    private AssetAttribute _assetAttribute;
    private Tactic _tactic;
    
	public AssetAttributeTactic(Context context, Cursor cursor)
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
		if (_assetAttribute != null)
		{
			_assetAttribute.close();
			_assetAttribute = null;
		}
		if (_tactic != null)
		{
			_tactic.close();
			_tactic = null;
		}
	}
	
	public String getAssetAttributeID()
	{
		return getCursorGuid(ASSET_ATTRIBUTE);
	}
	
	public AssetAttribute getAssetAttribute(Context context)
	{
		String id = getAssetAttributeID();
		if (_assetAttribute == null && id != null)
		{
			_assetAttribute = AssetAttribute.query(context, Uri.withAppendedPath(AssetAttribute.CONTENT_URI, id));
		}
		return _assetAttribute;
	}

	public static AssetAttributeTactic query(Context context, String whereClause)
	{
		AssetAttributeTactic result = null;
		try
		{
			result = new AssetAttributeTactic(context, context.getContentResolver().query(CONTENT_URI, null, whereClause, null, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}
