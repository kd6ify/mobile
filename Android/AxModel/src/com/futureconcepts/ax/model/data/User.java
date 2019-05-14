package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class User extends BaseTable
{
    public static final String EMPLOYEE_NO = "EmployeeNo";
    public static final String PERSON = "Person";
    public static final String TYPE = "Type";
    public static final String RANK = "Rank";
    public static final String AGENCY = "Agency";
    public static final String BUREAU = "Bureau";
    public static final String AUTHENTICATION_LEVEL = "AuthenticationLevel";
    public static final String ORGANIZATION_UNIT = "OrganizationalUnit";

	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/User");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.User";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.User";

    private Person _person;
    private UserType _type;
    private UserRankType _rank;
    private Agency _agency;
    private Bureau _bureau;
    
	public User(Context context, Cursor cursor)
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
		if (_person != null)
		{
			_person.close();
			_person = null;
		}
		if (_type != null)
		{
			_type.close();
		}
		if (_rank != null)
		{
			_rank.close();
			_rank = null;
		}
		if (_agency != null)
		{
			_agency.close();
			_agency = null;
		}
		if (_bureau != null)
		{
			_bureau.close();
			_bureau = null;
		}
	}
	
	@Override
	public Uri getContentUri()
	{
		return CONTENT_URI;
	}
		
	public String getPersonID()
	{
		return getCursorGuid(PERSON);
	}
	
	public Person getPerson(Context context)
	{
		String id = getPersonID();
		if (_person == null && id != null)
		{
			_person = Person.query(context, Uri.withAppendedPath(Person.CONTENT_URI, id));
		}
		return _person;
	}

	public String getTypeID()
	{
		return getCursorGuid(TYPE);
	}

	public UserType getType(Context context)
	{
		String id = getTypeID();
		if (_type == null && id != null)
		{
			_type = UserType.query(context, Uri.withAppendedPath(UserType.CONTENT_URI, id));
		}
		return _type;
	}
		
	public String getAgencyID()
	{
		return getString(getColumnIndex(AGENCY));
	}

	public Agency getAgency(Context context)
	{
		String id = getAgencyID();
		if (_agency == null && id != null)
		{
			_agency = Agency.query(context, Uri.withAppendedPath(Agency.CONTENT_URI, id));
		}
		return _agency;
	}
	
	public String getRankID()
	{
		return getString(getColumnIndex(RANK));
	}

	public UserRankType getRank(Context context)
	{
		String id = getRankID();
		if (_rank == null && id != null)
		{
			_rank = UserRankType.query(context, Uri.withAppendedPath(UserRankType.CONTENT_URI, id));
		}
		return _rank;
	}
	
	public String getEmployeeNo()
	{
		return getString(getColumnIndex(EMPLOYEE_NO));
	}
	
	public static User query(Context context, Uri uri)
	{
		User result = null;
		try
		{
			result = new User(context, context.getContentResolver().query(uri, null, null, null, null));
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
