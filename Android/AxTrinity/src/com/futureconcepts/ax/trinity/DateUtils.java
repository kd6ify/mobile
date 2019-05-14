package com.futureconcepts.ax.trinity;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import org.json.JSONException;

public class DateUtils
{
    public static Calendar getCalendarFromJSONDate(String jsondate) throws JSONException
	{
		StringTokenizer st = new StringTokenizer(jsondate, "/(-)");
		Calendar calendar = new GregorianCalendar();
		calendar.clear();
		try
		{
			String firstToken = st.nextToken();
			// look for /Date
			if (!firstToken.equals("Date"))
			{
				throw new JSONException("JSON Date invalid");
			}
			Long millis = Long.parseLong(st.nextToken());
//			int tzOffset = Integer.parseInt(st.nextToken()) * 36000;
			//calendar.setTimeInMillis(millis + tzOffset);
			// Currently, AntaresX stores datetimes in the SQL database as local time (PST).  LINQ to SQL and WCF
			// are assuming datetimes in the db are stored as UTC times.  When WCF serializes the DateTime using JsonDataContractSerializer
			// it is appending a timezone offset, such as "/Date(34234234234-0700)".  
			// Until AntaresX stores DateTimes in UTC time, the quick fix here is to ignore the timezone offset.
			calendar.setTimeInMillis(millis);
		}
		catch (Exception exc)
		{
			throw new JSONException("failed to parse JSON Date: " );
		}
		return calendar;
	}
    
	public static long getMillisFromJsonDate(String value)
	{
		long retVal = 0;
		try
		{
			Calendar cal = getCalendarFromJSONDate(value);
			retVal = cal.getTimeInMillis();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return retVal;
	}
}
