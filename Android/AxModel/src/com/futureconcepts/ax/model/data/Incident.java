package com.futureconcepts.ax.model.data;

import org.joda.time.DateTime;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
//import android.util.Log;

public class Incident extends AddressedTable
{
    public static final String NAME = "Name";
    public static final String TYPE = "Type";
    public static final String STATUS = "Status";
    public static final String SEVERITY = "Severity";
    public static final String DETAILS = "Details";
    public static final String NOTES = "Notes";
    public static final String START_TIME = "StartTime";
    public static final String END_TIME = "EndTime";
    public static final String CREATED_TIME = "Created";
    public static final String COMPLEXITY = "Complexity";
    public static final String PASSWORD = "Password";
    public static final String STATE = "State";
    public static final String SOURCE = "Source";
    public static final String SOURCE_DATE = "SourceDate";
    public static final String REGION = "Region";
    public static final String EXPECTED_CONTAINMENT = "ExpectedContainment";
    public static final String ESTIMATED_CONTROLLED = "EstimatedControlled";
    public static final String DECLARED_CONTROLLED = "DeclaredControlled";
    public static final String PERCENT_CONTROLLED = "PercentControlled";
    public static final String CAUSE = "Cause";
    public static final String AREA_INVOLVED = "AreaInvolved";
    public static final String CURRENT_THREAT = "CurrentThreat";
    public static final String CONTROL_PROBLEMS = "ControlProblems";
    public static final String ESTIMATED_LOSS = "EstimatedLoss";
    public static final String ESTIMATED_SAVING = "EstimatedSaving";
    public static final String INJURIES_REPORT_PERIOD = "InjuriesReportPeriod";
    public static final String INJURIES_TO_DATE = "InjuriesToDate";
    public static final String FATALITIES = "Fatalities";
    public static final String CONTROLLER_NO = "ControllerNo";
    public static final String OVERTIME_CODE = "OverTimeCode";
    public static final String DRIVE_TIME_HOURS = "DriveTimeHours";
    public static final String OWNER = "Owner";
    public static final String CATEGORY = "Category";
    public static final String SPECIAL_INSTRUCTIONS = "SpecialInstructions";
    public static final String EVENT_SUMMARY = "EventSummary";
    public static final String ADDRESS_LIST = "AddressList";
    public static final String COMMUNICATION_LIST = "CommunicationList";
    public static final String DRAWING_LIST = "DrawingList";
    public static final String INCIDENT_LIST = "IncidentList";
    public static final String LEAD_SHEET_LIST = "LeadSheetList";
    public static final String MEDIA_LIST = "MediaList";
    public static final String RESOURCE_ORDER_LIST = "ResourceOrderList";

	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/Incident");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.Incident";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.Incident";

    private IncidentType _type;
    
    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = LAST_MODIFIED + " DESC";

	public Incident(Context context, Cursor cursor)
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
	public Uri getContentUri()
	{
		return CONTENT_URI;
	}

	private void closeReferences()
	{
		if (_type != null)
		{
			_type.close();
			_type = null;
		}
	}
	
	public String getName()
	{
		int idx = getColumnIndex(NAME);
		assert(idx != -1);
		return getString(idx);
	}

	public String getLabel()
	{
		return getName();
	}
	
	public String getNotes()
	{
		return getString(getColumnIndex(NOTES));
	}
	
	public String getTypeID()
	{
		return getString(getColumnIndex(TYPE));
	}
	
	public IncidentType getType(Context context)
	{
		if (_type == null)
		{
			_type = IncidentType.query(context);
		}
		_type.moveToPosition(getTypeID());
		return _type;
	}
	
	public byte[] getPassword()
	{
		return getCursorBlob(PASSWORD);
	}
	
	public DateTime getStartTime()
	{
		return getCursorDateTime(START_TIME);
	}
	
	public DateTime getEndTime()
	{
		return getCursorDateTime(END_TIME);
	}
	
	public static Incident query(Context context)
	{
		return query(context, null);
	}
	public static Incident queryWhere(Context context, String whereClause)
	{
		Incident result = null;
		try
		{
			result = new Incident(context, context.getContentResolver().query(CONTENT_URI, null, Incident.STATUS+"!='"+IncidentStatus.ARCHIVED+"'", null, START_TIME + " DESC"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	public static Incident query(Context context, Uri uri)
	{
		Incident result = null;
		try
		{
			result = new Incident(context, context.getContentResolver().query(uri, null, null, null, null));
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
