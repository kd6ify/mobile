package com.futureconcepts.database;

import java.io.IOException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class SchoolFeedTable {
	//Database Table
	public static final String SCHOOL_FEED_TABLE = "SchoolFeed";
	//Table columns
	public static final String COLUMN_ID="ID";	
	public static final String COLUMN_DETAILS="Details";
	public static final String COLUMN_DATE="Date";
	public static final String COLUMN_TIME="Time";	
	public static final String[] SCHOOLS_FEED_TABLE_PROJECTION = 
		{ COLUMN_ID,COLUMN_DETAILS,COLUMN_DATE,COLUMN_TIME};

	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" +"com.futureconcepts.contentprovider.anonymous" + "/SchoolFeed");
    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.SchoolFeed";
    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single row.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.SchoolFeed";
    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = COLUMN_DATE + "DESC";  
    
	//Database creation SQL statement
	private static final String DATABASE_CREATE = "create table "
			+ SCHOOL_FEED_TABLE
			+"("
			+COLUMN_ID + " varchar(100) primary key, "
			+COLUMN_DETAILS + " varchar(500),"	
			+COLUMN_DATE + " varchar(20),"	
			+COLUMN_TIME + " varchar(20)"						
			+");";
	
	public static void onCreate(SQLiteDatabase database) 
	{		
		database.execSQL(DATABASE_CREATE);		 
	}
	
	
	//Upgrade Database version and delete all old data
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) throws IOException
	{
		Log.w(SchoolFeedTable.class.getName(),"Upgrading database from version "+ oldVersion + " to " +newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " +SCHOOL_FEED_TABLE);
		onCreate(database);
	}
}
