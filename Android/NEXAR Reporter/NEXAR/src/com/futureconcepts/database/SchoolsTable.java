package com.futureconcepts.database;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.futureconcepts.anonymous.R;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class SchoolsTable {
	//Database Table
	public static final String SCHOOLS_TABLE = "Schools";
	//Table columns
	public static final String COLUMN_ID="ID";	
	public static final String COLUMN_NAME="Name";
	public static final String COLUMN_STATE="State";
	public static final String COLUMN_TYPE="Type";	
	public static final String[] SCHOOLS_TABLE_PROJECTION = 
		{ COLUMN_ID,COLUMN_NAME,COLUMN_STATE,COLUMN_TYPE};

	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" +"com.futureconcepts.contentprovider.anonymous" + "/Schools");
    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.Schools";
    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single row.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.Schools";
    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = COLUMN_NAME + "DESC";  
    
	//Database creation SQL statement
	private static final String DATABASE_CREATE = "create table "
			+ SCHOOLS_TABLE
			+"("
			+COLUMN_ID + " varchar(100) primary key, "
			+COLUMN_NAME + " varchar(100),"	
			+COLUMN_STATE + " varchar(100),"	
			+COLUMN_TYPE + " varchar(100)"						
			+");";
	
	public static void onCreate(SQLiteDatabase database,Context con) throws IOException
	{		
		database.execSQL(DATABASE_CREATE);

		
//		 ContentValues _Values = new ContentValues();
//		 Resources res = con.getResources();
//		 XmlResourceParser _xml = res.getXml(R.xml.available_schools);
//		
//		 try
//	        {
//	            //Check for end of document
//	            int eventType = _xml.getEventType();
//	            while (eventType != XmlPullParser.END_DOCUMENT) {
//	                //Search for record tags
//	                if ((eventType == XmlPullParser.START_TAG) &&(_xml.getName().equals("record"))){
//	                    //Record tag found, now get values and insert record
//	                    String _ID = _xml.getAttributeValue(null,  SchoolsTable.COLUMN_ID);
//	                    String _Name = _xml.getAttributeValue(null,SchoolsTable.COLUMN_NAME);
//	                    String _State = _xml.getAttributeValue(null,  SchoolsTable.COLUMN_STATE);
//	                    String _TypeSchool = _xml.getAttributeValue(null,SchoolsTable.COLUMN_TYPE);
//	                    _Values.put(SchoolsTable.COLUMN_ID, _ID);
//	                    _Values.put(SchoolsTable.COLUMN_NAME, _Name);
//	                    _Values.put(SchoolsTable.COLUMN_STATE, _State);
//	                    _Values.put(SchoolsTable.COLUMN_TYPE, _TypeSchool);
//	                    database.insert(SchoolsTable.SCHOOLS_TABLE, null, _Values);   
//	                   // Log.d("TYPE"+""+);
//	                }
//	                eventType = _xml.next();
//	            }
//	          
//	        }
//	        //Catch errors
//	        catch (XmlPullParserException e)
//	        {      
//	            Log.e("error", e.getMessage(), e);
//	        }         
//	        finally
//	        {          
//	            //Close the xml file
//	            _xml.close();
//	        }
		 
	}
	
	
	//Upgrade Database version and delete all old data
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) throws IOException
	{
		Log.w(SchoolsTable.class.getName(),"Upgrading database from version "+ oldVersion + " to " +newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " +SCHOOLS_TABLE);
		onCreate(database,null);
	}
}
