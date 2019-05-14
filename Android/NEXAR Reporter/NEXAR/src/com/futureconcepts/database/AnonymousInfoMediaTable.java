package com.futureconcepts.database;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class AnonymousInfoMediaTable {

	//Database Table
	public static final String ANONYMOUS_ACTIVITY_MEDIA_TABLE = "AnonymousInfoMedia";
	//Table columns
	public static final String COLUMN_ID_ANONYMOUS_ACTIVITY ="NEXARInfo";	
	public static final String COLUMN_ID_MEDIA="Media";
	public static final String COLUMN_TYPE="Type";//Temporary, will not be sent, just for pending reports reference.
	
	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" +"com.futureconcepts.contentprovider.anonymous" + "/AnonymousInfoMedia");
    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.AnonymousInfoMedia";
    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single row.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.AnonymousInfoMedia";
    /**
     * The default sort order for this table
     */
	
	public static final String[] MEDIA_ACTIVITY_TABLE_PROJECTION = 
		{ COLUMN_ID_ANONYMOUS_ACTIVITY,COLUMN_ID_MEDIA,COLUMN_TYPE };
		
	//Database creation SQL statement
	private static final String DATABASE_CREATE = "create table "
						+ ANONYMOUS_ACTIVITY_MEDIA_TABLE
						+"("
						+COLUMN_ID_ANONYMOUS_ACTIVITY + " varchar(100),"						
						+COLUMN_ID_MEDIA + " varchar(100),"
						+COLUMN_TYPE + " varchar(20)"
						+");";
	
	public static void onCreate(SQLiteDatabase database)
	{
		database.execSQL(DATABASE_CREATE);
		
	}
	
	//Upgrade Database version and delete all old data
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
	{
		Log.w(AnonymousInfoMediaTable.class.getName(),"Upgrading database from version "+ oldVersion + " to " +newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " +ANONYMOUS_ACTIVITY_MEDIA_TABLE);
		onCreate(database);
	}
}
