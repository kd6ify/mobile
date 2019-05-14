package com.futureconcepts.database;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class MediaTable {
	//Database Table
		public static final String MEDIA_TABLE = "Media";
		//Table columns
		public static final String COLUMN_ID="ID";	
		public static final String COLUMN_NAME="Name";
		public static final String COLUMN_FILE="File";
		public static final String COLUMN_TYPE="Type";
		public static final String COLUMN_SIZE = "Size";
		public static final String COLUMN_Status="Status";
		public static final String COLUMN_PartsSent="PartsSent";
		public static final String COLUMN_Date="Date";
		
		/**
	     * The content:// style URL for this table
	     */
	    public static final Uri CONTENT_URI = Uri.parse("content://" +"com.futureconcepts.contentprovider.anonymous" + "/Media");
	    /**
	     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
	     */
	    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.Media";
	    /**
	     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single row.
	     */
	    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.Media";
	   	
		
		public static final String[] MEDIA_ACTIVITY_TABLE_PROJECTION = 
			{ COLUMN_ID,COLUMN_NAME,COLUMN_FILE,COLUMN_TYPE,COLUMN_SIZE,COLUMN_Status,COLUMN_PartsSent,COLUMN_Date };

		//Database creation SQL statement
		private static final String DATABASE_CREATE = "create table "
							+ MEDIA_TABLE
							+"("
							+COLUMN_ID + " varchar(100) primary key, "						
							+COLUMN_NAME + " varchar(40),"						
							+COLUMN_FILE + " varchar(20),"
							+COLUMN_TYPE + " varchar(100),"
							+COLUMN_SIZE + " varchar(20),"
							+COLUMN_Status + " int(2),"
							+COLUMN_PartsSent + " int(255),"
							+COLUMN_Date + " varchar(20)"
							+");";
		
		public static void onCreate(SQLiteDatabase database)
		{
			database.execSQL(DATABASE_CREATE);
			
		}
		
		//Upgrade Database version and delete all old data
		public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
		{
			Log.w(MediaTable.class.getName(),"Upgrading database from version "+ oldVersion + " to " +newVersion
					+ ", which will destroy all old data");
			database.execSQL("DROP TABLE IF EXISTS " +MEDIA_TABLE);
			onCreate(database);
		}
}
