package com.futureconcepts.drake.client.model;

import com.futureconcepts.drake.client.Imps;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class InvitationCursor extends BaseCursor
{
	public InvitationCursor(Cursor cursor)
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
		
	public String getInviteID()
	{
		return getCursorString(Imps.InvitationColumns.INVITE_ID);
	}

	public String getSender()
	{
		return getCursorString(Imps.InvitationColumns.SENDER);
	}

	public String getGroupName()
	{
		return getCursorString(Imps.InvitationColumns.GROUP_NAME);
	}

	public String getNote()
	{
		return getCursorString(Imps.InvitationColumns.NOTE);
	}

	public int getStatus()
	{
		return getCursorInt(Imps.InvitationColumns.STATUS);
	}
	
	public static InvitationCursor query(Context context, long id)
	{
		InvitationCursor result = null;
		try
		{
			result = new InvitationCursor(context.getContentResolver().query(Uri.withAppendedPath(Imps.Invitation.CONTENT_URI, Long.toString(id)), null, null, null, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}
