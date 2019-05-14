package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class Triage extends AddressedTable
{
    public static final String DESTINATION = "Destination";
    public static final String STATUS = "Status";
    public static final String TRACKING_ID = "TrackingID";
    public static final String IS_CHILD = "IsChild";
    public static final String COLOR = "Color";
    public static final String PERSON = "Person";
    public static final String INCIDENT = "Incident";

	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/Triage");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.Triage";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.Triage";

    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = "modified DESC";

    private TriageColor _color;
    private TriageStatus _status;
    private Person _person;
    
	public Triage(Context context, Cursor cursor)
	{
		super(context, cursor);
	}

	@Override
	public void close()
	{
		super.close();
		closeReferences();
	}
	
	@Override
	public Uri getContentUri()
	{
		return CONTENT_URI;
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
		if (_color != null)
		{
			_color.close();
			_color = null;
		}
		if (_status != null)
		{
			_status.close();
			_status = null;
		}
		if (_person != null)
		{
			_person.close();
			_person = null;
		}
	}
	
	public String getDestinationID()
	{
		return getModelString(DESTINATION);
	}

	public String getColorID()
	{
		return getModelGuid(COLOR);
	}

	public TriageColor getColor(Context context)
	{
		if (_color == null)
		{
			_color = TriageColor.query(context);
		}
		_color.moveToPosition(getColorID());
		return _color;
	}
	
	public String getStatusID()
	{
		return getModelGuid(STATUS);
	}

	public TriageStatus getStatus(Context context)
	{
		if (_status == null)
		{
			_status = TriageStatus.query(context);
		}
		_status.moveToPosition(getStatusID());
		return _status;
	}
		
	public String getIncidentID()
	{
		return getModelString(INCIDENT);
	}
	
	public String getPersonID()
	{
		return getModelGuid(PERSON);
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
	
	public String getTrackingID()
	{
		return getModelString(TRACKING_ID);
	}
	
	public static Triage queryWhere(Context context, String whereClause)
	{
		Triage result = null;
		try
		{
			result = new Triage(context, context.getContentResolver().query(CONTENT_URI, null, whereClause, null, COLOR+" ASC"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public static Triage query(Context context, Uri uri)
	{
		Triage result = null;
		try
		{
			result = new Triage(context, context.getContentResolver().query(uri, null, null, null, null));
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
	
	public static Uri findUri(Context context, String whereClause)
	{
		Uri result = null;
		Triage triage = queryWhere(context, whereClause);
		if (triage != null)
		{
			if (triage.getCount() == 1)
			{
				triage.moveToFirst();
				result = Uri.withAppendedPath(CONTENT_URI, triage.getID());
			}
			triage.close();
		}
		return result;
	}
	
	public static Triage queryIncident(Context context, String incidentID)
	{
		Triage result = null;
		try
		{
			String whereClause = Triage.INCIDENT+"='"+incidentID+"'";
			result = new Triage(context, context.getContentResolver().query(CONTENT_URI, null, whereClause, null, COLOR+" ASC"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	public static Triage queryImmediate(Context context, String incidentID)
	{
		Triage result = null;
		try
		{
			String whereClause = Triage.INCIDENT+"='"+incidentID+"' AND "+Triage.COLOR+"='"+TriageColor.RED+"'";
			result = new Triage(context, context.getContentResolver().query(CONTENT_URI, null, whereClause, null, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	public static Triage queryDelayed(Context context, String incidentID)
	{
		Triage result = null;
		try
		{
			String whereClause = Triage.INCIDENT+"='"+incidentID+"' AND "+Triage.COLOR+"='"+TriageColor.YELLOW+"'";
			result = new Triage(context, context.getContentResolver().query(CONTENT_URI, null, whereClause, null, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	public static Triage queryMinor(Context context, String incidentID)
	{
		Triage result = null;
		try
		{
			String whereClause = Triage.INCIDENT+"='"+incidentID+"' AND "+Triage.COLOR+"='"+TriageColor.GREEN+"'";
			result = new Triage(context, context.getContentResolver().query(CONTENT_URI, null, whereClause, null, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	public static Triage queryDeceased(Context context, String incidentID)
	{
		Triage result = null;
		try
		{
			String whereClause = Triage.INCIDENT+"='"+incidentID+"' AND "+Triage.COLOR+"='"+TriageColor.BLACK+"'";
			result = new Triage(context, context.getContentResolver().query(CONTENT_URI, null, whereClause, null, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	public static Triage queryVictim(Context context)
	{
		return null;
	}
	public static Triage queryMarked(Context context)
	{
		return null;
	}
	
	public static class Content extends ContentBase
	{
		public Content(Context context, String deviceID, String incidentID)
		{
			super(context);
			initialize(deviceID);
			setIncidentID(incidentID);
			setTrackingID("0");
			setColorID(TriageColor.GREEN);
			setStatusID(TriageStatus.UNKNOWN);
		}

		public Content(Context context, Triage triage)
		{
			super(context);
			if (triage != null && triage.getCount() == 1)
			{
				setID(triage.getID());
				setIncidentID(triage.getIncidentID());
				setTrackingID(triage.getTrackingID());
				setColorID(triage.getColorID());
				setStatusID(triage.getStatusID());
				setPersonID(triage.getPersonID());
				setAddressID(triage.getAddressID());
			}
		}
		
		public void setID(String value)
		{
			_values.put(ID, value);
			notifyPropertyChanged(ID, value);
		}
		
		public void setIncidentID(String value)
		{
			_values.put(INCIDENT, value);
			notifyPropertyChanged(INCIDENT, value);
		}
		
		public void setTrackingID(String value)
		{
			_values.put(TRACKING_ID, value);
			notifyPropertyChanged(TRACKING_ID, value);
		}

		public void setAddressID(String value)
		{
			_values.put(ADDRESS, value);
			notifyPropertyChanged(ADDRESS, value);
		}

		public void setAddressID(Uri uri)
		{
			if (uri != null)
			{
				String id = uri.getPathSegments().get(1);
				if (id != null)
				{
					_values.put(ADDRESS, id);
					notifyPropertyChanged(ADDRESS, id);
				}
			}
			else
			{
				_values.putNull(ADDRESS);
				notifyPropertyChanged(ADDRESS, null);
			}
		}

		public void setPersonID(String value)
		{
			_values.put(PERSON, value);
			notifyPropertyChanged(PERSON, value);
		}

		public void setPersonID(Uri uri)
		{
			if (uri != null)
			{
				String id = uri.getPathSegments().get(1);
				if (id != null)
				{
					_values.put(PERSON, id);
					notifyPropertyChanged(PERSON, id);
				}
			}
			else
			{
				_values.putNull(PERSON);
				notifyPropertyChanged(PERSON, null);
			}
		}
		
		public void setColorID(String value)
		{
			_values.put(COLOR, value);
			notifyPropertyChanged(COLOR, value);
		}

		public void setStatusID(String value)
		{
			_values.put(STATUS, value);
			notifyPropertyChanged(STATUS, value);
		}
		
	}
}
