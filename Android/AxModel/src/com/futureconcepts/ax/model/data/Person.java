package com.futureconcepts.ax.model.data;

import org.joda.time.DateTime;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class Person extends BaseTable
{
    public static final String NAME = "Name";
    public static final String LAST = "Last";
    public static final String FIRST = "First";
    public static final String MIDDLE = "Middle";
    public static final String SUFFIX = "Suffix";
    public static final String TITLE = "Title";
    public static final String WEIGHT = "Weight";
    public static final String HEIGHT = "Height";
    public static final String DOB = "DOB";
    public static final String NOTES = "Notes";
    public static final String DISTINGUISHING_MARKS = "DistinguishingMarks";
    public static final String EYE_COLOR = "EyeColor";
    public static final String GENDER = "Gender";
    public static final String HAIR_COLOR = "HairColor";
    public static final String ETHNICITY = "Ethnicity";
    public static final String RELIGION = "Religion";
    public static final String BLOOD_TYPE = "BloodType";

	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/Person");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.Person";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.Person";

    private BloodType _bloodType;
    private Gender _gender;
    
	public Person(Context context, Cursor cursor)
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
		if (_bloodType != null)
		{
			_bloodType.close();
			_bloodType = null;
		}
		if (_gender != null)
		{
			_gender.close();
			_gender = null;
		}
	}
	
	@Override
	public Uri getContentUri()
	{
		return CONTENT_URI;
	}

	public String getName()
	{
		return getCursorString(NAME);
	}
	
	public String getLast()
	{
		return getCursorString(LAST);
	}
	
	public String getFirst()
	{
		return getCursorString(FIRST);
	}
	
	public String getMiddle()
	{
		return getCursorString(MIDDLE);
	}

	public String getSuffix()
	{
		return getCursorString(SUFFIX);
	}

	public String getTitle()
	{
		return getCursorString(TITLE);
	}

	public double getWeight()
	{
		return getCursorDouble(WEIGHT);
	}

	public double getHeight()
	{
		return getCursorDouble(HEIGHT);
	}
	
	public DateTime getDOB()
	{
		return getCursorDateTime(DOB);
	}
	
	public String getNotes()
	{
		return getCursorString(NOTES);
	}
	
	public String getDistingishingMarks()
	{
		return getCursorString(DISTINGUISHING_MARKS);
	}
	
	public String getEyeColorID()
	{
		return getCursorGuid(EYE_COLOR);
	}
	
	public String getGenderID()
	{
		return getCursorGuid(GENDER);
	}
	
	public Gender getGender(Context context)
	{
		String id = getGenderID();
		if (_gender == null && id != null)
		{
			_gender = Gender.query(context, Uri.withAppendedPath(Gender.CONTENT_URI, id));
		}
		return _gender;
	}
	
	public String getHairColorID()
	{
		return getCursorGuid(HAIR_COLOR);
	}
	
	public String getEthnicity()
	{
		return getCursorGuid(ETHNICITY);
	}
	
	public String getReligion()
	{
		return getCursorGuid(RELIGION);
	}
	
	public String getBloodTypeID()
	{
		return getCursorGuid(BLOOD_TYPE);
	}

	public BloodType getBloodType(Context context)
	{
		String id = getBloodTypeID();
		if (_bloodType == null && id != null)
		{
			_bloodType = BloodType.query(context, Uri.withAppendedPath(BloodType.CONTENT_URI, getBloodTypeID()));
		}
		return _bloodType;
	}
		
	public static Person query(Context context, Uri uri)
	{
		Person result = null;
		try
		{
			result = new Person(context, context.getContentResolver().query(uri, null, null, null, null));
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
		
	public static class Content extends ContentBase
	{
		public Content(Context context)
		{
			super(context);
			setID(Guid.newGuid().toString());
			setGenderID(Gender.UNKNOWN);
		}

		public Content(Context context, Person person)
		{
			super(context);
			if (person != null && person.getCount() == 1)
			{
				person.moveToFirst();
				setID(person.getID());
				setGenderID(person.getGenderID());
			}
		}
		
		public void setID(String value)
		{
			_values.put(ID, value);
		}
		
		public void setLast(String value)
		{
			_values.put(LAST, value);
		}
		
		public void setFirst(String value)
		{
			_values.put(FIRST, value);
		}

		public void setMiddle(String value)
		{
			_values.put(MIDDLE, value);
		}

		public void setSuffix(String value)
		{
			_values.put(SUFFIX, value);
		}

		public void setTitleID(String value)
		{
			_values.put(TITLE, value);
		}
		
		public void setWeight(Double value)
		{
			_values.put(WEIGHT, value);
		}
		
		public void setHeight(Double value)
		{
			_values.put(HEIGHT, value);
		}
		
		public void setDOB(DateTime value)
		{
			if (value != null)
			{
				_values.put(DOB, value.toString());
			}
			else
			{
				_values.putNull(DOB);
			}
		}
		
		public void setNotes(String value)
		{
			_values.put(NOTES, value);
		}

		public void setDistinguishingMarks(String value)
		{
			_values.put(DISTINGUISHING_MARKS, value);
		}
		
		public void setEyeColorID(String value)
		{
			_values.put(EYE_COLOR, value);
		}
		
		public void setGenderID(String value)
		{
			_values.put(GENDER, value);
		}
		
		public void setHairColorID(String value)
		{
			_values.put(HAIR_COLOR, value);
		}
		
		public void setEthnicityID(String value)
		{
			_values.put(ETHNICITY, value);
		}
		
		public void setReligionID(String value)
		{
			_values.put(RELIGION, value);
		}
	}
}
