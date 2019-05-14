package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class AssetAttribute extends BaseTable
{
    public static final String ASSET = "Asset";
    public static final String OPERATIONAL_PERIOD = "OperationalPeriod";

    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/AssetAttribute");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.AssetAttribute";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.AssetAttribute";
        
    private Asset _asset;
    
	public AssetAttribute(Context context, Cursor cursor)
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
		if (_asset != null)
		{
			_asset.close();
			_asset = null;
		}
	}
	
	public String getAssetID()
	{
		return getCursorGuid(ASSET);
	}
	
	public Asset getAsset(Context context)
	{
		String id = getAssetID();
		if (_asset == null && id != null)
		{
			_asset = Asset.query(context, Uri.withAppendedPath(Asset.CONTENT_URI, id));
		}
		return _asset;
	}

	public String getOperationalPeriodID()
	{
		return getCursorGuid(OPERATIONAL_PERIOD);
	}
	
	public static AssetAttribute query(Context context, String whereClause)
	{
		AssetAttribute result = null;
		try
		{
			result = new AssetAttribute(context, context.getContentResolver().query(CONTENT_URI, null, whereClause, null, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public static AssetAttribute query(Context context, Uri uri)
	{
		AssetAttribute result = null;
		try
		{
			result = new AssetAttribute(context, context.getContentResolver().query(uri, null, null, null, null));
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
