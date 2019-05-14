package com.futureconcepts.drake.client.model;

import com.futureconcepts.drake.client.Imps;

import android.database.Cursor;

public class ContactCursor extends BaseCursor
{
	public ContactCursor(Cursor cursor)
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
		
	public String getUsername()
	{
		return getCursorString(Imps.Contacts.USERNAME);
	}

	public String getNickname()
	{
		return getCursorString(Imps.Contacts.NICKNAME);
	}

	public String getContactList()
	{
		return getCursorString(Imps.Contacts.CONTACTLIST);
	}
	
	public int getType()
	{
		return getCursorInt(Imps.Contacts.TYPE);
	}
	
	public int getSubscriptionStatus()
	{
		return getCursorInt(Imps.Contacts.SUBSCRIPTION_STATUS);
	}

	public int getSubscriptionType()
	{
		return getCursorInt(Imps.Contacts.SUBSCRIPTION_TYPE);
	}

	public int getQuickContact()
	{
		return getCursorInt(Imps.Contacts.QUICK_CONTACT);
	}

	public int getRejected()
	{
		return getCursorInt(Imps.Contacts.REJECTED);
	}

	public int getOTR()
	{
		return getCursorInt(Imps.Contacts.OTR);
	}
}