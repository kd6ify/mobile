package com.futureconcepts.drake.client.model;

import com.futureconcepts.drake.client.Imps;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class FileTransferCursor extends BaseCursor
{
	public FileTransferCursor(Cursor cursor)
	{
		super(cursor);
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
	
	private void closeReferences()
	{
	}
		
	public String getStreamID()
	{
		return getCursorString(Imps.FileTransferColumns.STREAM_ID);
	}

	public String getPeer()
	{
		return getCursorString(Imps.FileTransferColumns.PEER);
	}

	public String getStatus()
	{
		return getCursorString(Imps.FileTransferColumns.STATUS);
	}
	
	public static FileTransferCursor query(Context context, long id)
	{
		FileTransferCursor result = null;
		try
		{
			result = new FileTransferCursor(context.getContentResolver().query(Uri.withAppendedPath(Imps.FileTransfer.CONTENT_URI, Long.toString(id)), null, null, null, null));
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
