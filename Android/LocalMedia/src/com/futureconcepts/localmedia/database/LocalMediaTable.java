package com.futureconcepts.localmedia.database;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class LocalMediaTable {
	//Database Table
		public static final String MEDIA_TABLE = "LocalMedia";
		//Table columns
		public static final String COLUMN_ID="ID";	
		public static final String COLUMN_BYTES_STORED="bytesStored";
		public static final String COLUMN_FILEPATH="filePath";
		public static final String COLUMN_FILE_SIZE = "fileSize";
		public static final String COLUMN_LASTMODIFIED_DATE="lastModified";
		public static final String COLUMN_ACTION="action";
		public static final String COLUMN_STATUS="status";
		public static final String COLUMN_ENTRY_ID="entry";	
		public static final String COLUMN_FAIL="fail";	
		/**
	     * The content:// style URL for this table
	     */
	    public static final Uri CONTENT_URI = Uri.parse("content://" +"com.futureconcepts.localmedia.database" + "/LocalMedia");
	    /**
	     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
	     */
	    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.LocalMedia";
	    /**
	     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single row.
	     */
	    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.LocalMedia";
	   	
		
		public static final String[] MEDIA_ACTIVITY_TABLE_PROJECTION = 
			{ COLUMN_ID,COLUMN_BYTES_STORED,COLUMN_FILEPATH,COLUMN_FILE_SIZE,COLUMN_LASTMODIFIED_DATE,COLUMN_ACTION,COLUMN_STATUS };

		//Database creation SQL statement
		private static final String DATABASE_CREATE = "create table "
							+ MEDIA_TABLE
							+"("
							+COLUMN_ID + " varchar(100) primary key, "						
							+COLUMN_BYTES_STORED + " BIGINT,"					
							+COLUMN_FILE_SIZE + " BIGINT,"
						    +COLUMN_FILEPATH + " varchar,"
							+COLUMN_LASTMODIFIED_DATE + " varchar(20),"
							+COLUMN_ACTION +" varchar(10),"
							+COLUMN_STATUS +" varchar(10),"
							+COLUMN_ENTRY_ID + " varchar(100),"
							+COLUMN_FAIL + " varchar(4) DEFAULT \'no\'"
							+");";
		
		public static void onCreate(SQLiteDatabase database)
		{
			database.execSQL(DATABASE_CREATE);
			
		}
		
		//Upgrade Database version and delete all old data
		public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
		{
//			Log.w(SuspiciousActivityTable.class.getName(),"Upgrading database from version "+ oldVersion + " to " +newVersion
//					+ ", which will destroy all old data");
			database.execSQL("DROP TABLE IF EXISTS " +MEDIA_TABLE);
			onCreate(database);
		}
}
