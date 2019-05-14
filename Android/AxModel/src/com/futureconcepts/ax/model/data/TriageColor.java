package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class TriageColor extends IndexedType
{
	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/TriageColor");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.TriageColor";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.TriageColor";

    public static final String GREEN = new String("617B480D-1164-4453-BDCD-EFBCED03D4DA");
    public static final String YELLOW = new String("0A3C8B53-4647-4793-975E-68CCBBACAFE4");
    public static final String RED = new String("47FB865D-2A3F-48A1-95FA-AEB485109083");
    public static final String BLACK = new String("14D7AD60-DA12-42D3-9BBE-A296B8CBCB6D");
    
	public TriageColor(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	public static TriageColor query(Context context)
	{
		TriageColor result = null;
		try
		{
			result = new TriageColor(context, context.getContentResolver().query(CONTENT_URI, null, null, null, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	@Override
	public Uri getContentUri()
	{
		return CONTENT_URI;
	}
	public static TriageColor query(Context context, Uri uri)
	{
		TriageColor result = null;
		try
		{
			result = new TriageColor(context, context.getContentResolver().query(uri, null, null, null, null));
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
