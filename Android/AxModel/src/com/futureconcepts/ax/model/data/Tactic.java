package com.futureconcepts.ax.model.data;

import org.joda.time.DateTime;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class Tactic extends AddressedTable
{
	public static final String TAG = Tactic.class.getSimpleName();
	
    public static final String NAME = "Name";
    public static final String CALLSIGN = "Callsign";
    public static final String ADDRESS = "Address";
    public static final String REPORT_BY = "ReportBy";
    public static final String STATUS = "Status";
    public static final String INCIDENT = "Incident";
    public static final String PRIORITY = "Priority";
    public static final String TYPE = "Type";
    public static final String NOTES = "Notes";
    public static final String START = "Start";
    public static final String END = "End";
    public static final String SUGGESTED_ROUTE = "SuggestedRoute";
    public static final String ESTIMATED_DURATION = "EstimatedDuration";
    public static final String SPECIAL_EQUIPMENT = "SpecialEquipment";
    public static final String REQUIRES_RADIO = "RequiresRadio";
    public static final String REQUIRES_VEHICLE = "RequiresVehicle";
    public static final String REQUIRES_OTHER = "RequiresOther";
    public static final String REQUIREMENT_DESCRIPTION = "RequirementDescription";   
    public static final String OPERATIONAL_PERIOD = "OperationalPeriod";
    public static final String OBJECTIVE = "Objective";

	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + Objective.AUTHORITY + "/Tactic");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.Tactic";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.Tactic";

    private TacticType _type;
    private TacticStatus _status;
    private Incident _incident;
    private TacticPriority _priority;
    private Address _address;
    
	public Tactic(Context context, Cursor cursor)
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
		if (_type != null)
		{
			try
			{
				_type.close();
				_type = null;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if (_status != null)
		{
			try
			{
				_status.close();
				_status = null;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if (_incident != null)
		{
			try
			{
				_incident.close();
				_incident = null;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if (_priority != null)
		{
			try
			{
				_priority.close();
				_priority = null;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if (_address != null)
		{
			try
			{
				_address.close();
				_address = null;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void beginEdit() {
		// TODO Auto-generated method stub
		super.beginEdit();
		if (getCount() != 0)
		{
			setName(getCursorString(NAME));//Not null
			setCallsign(getCursorString(CALLSIGN));//null
			setAddressID(getCursorGuid(ADDRESS));//null
			setReportBy(getCursorString(REPORT_BY));//null
			setStatusID(getCursorGuid(STATUS));//Not null
			setTypeID(getCursorGuid(TYPE));//null
			setNotes(getCursorString(NOTES));//null
			//setStart(getCursorDateTime(START));//null
			//setEnd(getCursorDateTime(END));//null
			setRequiresVehicle(getCursorInt(REQUIRES_VEHICLE));// not null
			setRequiresRadio(getCursorInt(REQUIRES_RADIO));// not null
			setRequiresOther(getCursorInt(REQUIRES_OTHER));// not null
			setOperationalPeriod(getCursorGuid(OPERATIONAL_PERIOD));// null
			setIncident(getCursorGuid(INCIDENT));//null
			setPriority(getCursorGuid(PRIORITY));// not null
		}
	}
	
	@Override
	public Uri getContentUri()
	{
		return CONTENT_URI;
	}
	
	public void setName(String value)
	{
		setModel(NAME, value);
	}
	
	public String getName()
	{
		return getCursorString(NAME);
	}
	
	public void setCallsign(String value)
	{
		setModel(CALLSIGN, value);
	}

	public String getCallsign()
	{
		return getCursorString(CALLSIGN);
	}
	
	public void setReportBy(String value)
	{
		setModel(REPORT_BY, value);
	}

	public DateTime getReportBy()
	{
		return getCursorDateTime(REPORT_BY);
	}

	public String getObjectiveID()
	{
		return getCursorGuid(OBJECTIVE);
	}

	public void setTypeID(String value)
	{
		setModel(TYPE, value);
	}
	public String getTypeID()
	{
		return getCursorGuid(TYPE);
	}

	public TacticType getType(Context context)
	{
		if (_type != null)
		{
			if (_type.getID().equals(getTypeID()) == false)
			{
				_type.close();
				_type = null;
			}
		}
		
		if (_type == null)
		{
			_type = TacticType.query(context, Uri.withAppendedPath(TacticType.CONTENT_URI, getTypeID()));
		}
		return _type;
	}
	
	public void setStatusID(String value)
	{
		setModel(STATUS,value);
	}

	public String getStatusID()
	{
		return getCursorGuid(STATUS);
	}

	public TacticStatus getStatus(Context context)
	{
		if (_status != null)
		{
			if (_status.getID().equals(getStatusID()) == false)
			{
				_status.close();
				_status = null;
			}
		}
		
		if (_status == null)
		{
			_status = TacticStatus.query(context, Uri.withAppendedPath(TacticStatus.CONTENT_URI, getStatusID()));
		}
		return _status;
	}
	public void setOperationalPeriod(String value)
	{
		setModel(OPERATIONAL_PERIOD,value);
	}
	
	public String getOperationalPeriodId()
	{
		return getCursorGuid(OPERATIONAL_PERIOD);
	}
	
	public void setIncident(String value)
	{
		setModel(INCIDENT,value);
	}
	public String getIncidentID()
	{
		return getCursorGuid(INCIDENT);
	}

	public Incident getIncident(Context context)
	{
		if (_incident != null)
		{
			if (_incident.getID().equals(getIncidentID()) == false)
			{
				_incident.close();
				_incident = null;
			}
		}
		
		if (_incident == null)
		{
			_incident = Incident.query(context, Uri.withAppendedPath(Incident.CONTENT_URI, getIncidentID()));
		}
		return _incident;
	}
	
	public String getPriorityID()
	{
		return getCursorGuid(PRIORITY);
	}

	public void setPriority(String value){
		setModel(PRIORITY,value);
	}
	
	public TacticPriority getPriority(Context context)
	{
		if (_priority != null)
		{
			if (_priority.getID().equals(getPriorityID()) == false)
			{
				_priority.close();
				_priority = null;
			}
		}
		
		if (_priority == null)
		{
			_priority = TacticPriority.query(context, Uri.withAppendedPath(TacticPriority.CONTENT_URI, getPriorityID()));
		}
		return _priority;
	}

	public void setNotes(String value)
	{
		setModel(NOTES,value);
	}
	public String getNotes()
	{
		return getCursorString(NOTES);
	}

	public void setStart(DateTime dateTime)
	{
		setModel(START,dateTime);
	}
	public DateTime getStart()
	{
		return getCursorDateTime(START);
	}
	
	public void setEnd(DateTime value)
	{
		setModel(END,value);
	}
	
	public DateTime getEnd()
	{
		return getCursorDateTime(END);
	}

	public String getSuggestedRoute()
	{
		return getCursorString(SUGGESTED_ROUTE);
	}

	public int getEstimatedDuration()
	{
		return getCursorInt(ESTIMATED_DURATION);
	}
	
	public void setSpecialEquipment(String value)
	{
		setModel(SPECIAL_EQUIPMENT,value);
	}
	public String getSpecialEquipment()
	{
		return getCursorString(SPECIAL_EQUIPMENT);
	}
	
	public void setRequiresRadio(int value)
	{
		setModel(REQUIRES_RADIO,value);
	}
	public boolean requiresRadio()
	{
		return getCursorBoolean(REQUIRES_RADIO);
	}
	
	public void setRequiresVehicle(int value)
	{
		setModel(REQUIRES_VEHICLE,value);
	}
	
	public boolean requiresVehicle()
	{
		return getCursorBoolean(REQUIRES_VEHICLE);
	}
	
	public void setRequiresOther(int value)
	{
		setModel(REQUIRES_OTHER,value);
	}
	public boolean requiresOther()
	{
		return getCursorBoolean(REQUIRES_OTHER);
	}
	
	public void setRequirementsDescription(String value)
	{
		setModel(REQUIREMENT_DESCRIPTION,value);
	}
	
	public String getRequirementsDescription()
	{
		return getCursorString(REQUIREMENT_DESCRIPTION);
	}

	public static Tactic query(Context context, String whereClause)
	{
		Tactic result = null;
		try
		{
			result = new Tactic(context, context.getContentResolver().query(CONTENT_URI, null, whereClause, null, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public static Tactic query(Context context, Uri uri)
	{
		Tactic result = null;
		try
		{
			result = new Tactic(context, context.getContentResolver().query(uri, null, null, null, null));
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
	public static Tactic queryTasks(Context context, String operationalPeriodID)
	{
		Tactic result = null;
		try
		{
			result = new Tactic(context, context.getContentResolver().query(CONTENT_URI, null, Tactic.PRIORITY+"=='"+TacticPriority.NONE+"' AND "+Tactic.OPERATIONAL_PERIOD+"='"+operationalPeriodID+"'", null, "UPPER("+Tactic.NAME+") ASC"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	public static Tactic queryPriorityTasks(Context context, String incidentID)
	{
		Tactic result = null;
		try
		{
			result = new Tactic(context, context.getContentResolver().query(CONTENT_URI, null, Tactic.PRIORITY+"!='"+TacticPriority.NONE+"' AND "+Tactic.INCIDENT+"='"+incidentID+"'", null, "UPPER("+Tactic.NAME+") ASC"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	public static Uri createDefaultPriorityTask(Context context, String incidentID, String period)
	{
		Tactic pTask = new Tactic(context, null);
		pTask.beginEdit();
		pTask.setID(Guid.newGuid().toString());
		pTask.setIncident(incidentID);
		pTask.setOperationalPeriod(period);
		pTask.setName("");
		//pTask.setCallsign("");
		pTask.setStatusID(TacticStatus.PENDING);
		pTask.setPriority(TacticPriority.NORMAL);
		return context.getContentResolver().insert(CONTENT_URI, pTask.endEdit());
	}
	
	public static Uri createDefaultTask(Context context, String incidentID, String period)
	{
		Tactic task = new Tactic(context, null);
		task.beginEdit();
		task.setID(Guid.newGuid().toString());
		task.setIncident(incidentID);
		task.setOperationalPeriod(period);
		task.setName("");
		task.setTypeID("921EDEC2-27FE-D36D-6925-59087A37D58D");//Patrol Type
		task.setPriority(TacticPriority.NONE);
		task.setStatusID(TacticStatus.ACTIVE);
		return context.getContentResolver().insert(CONTENT_URI, task.endEdit());
	}

}
