package com.futureconcepts.database;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class TableStatus {
	//Database Table
			public static final String TABLE_STATUS = "TableStatus";
			//Table columns
			public static final String COLUMN_ID="ID";//ID OF the report
			public static final String COLUMN_NEXARINFO_TABLE="NexarInfoTable";
			public static final String COLUMN_MEDIA_TABLE="MediaTable";
			public static final String COLUMN_NEXARINFOMEDIA_TABLE="NexarInfoMediaTable";
			
			/**
		     * The content:// style URL for this table
		     */
		    public static final Uri CONTENT_URI = Uri.parse("content://" +"com.futureconcepts.contentprovider.anonymous" + "/TableStatus");
		    /**
		     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
		     */
		    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.TableStatus";
		    /**
		     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single row.
		     */
		    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.TableStatus";
		   	
			
			public static final String[] MEDIA_ACTIVITY_TABLE_PROJECTION = 
				{ COLUMN_NEXARINFO_TABLE,COLUMN_ID,COLUMN_MEDIA_TABLE,COLUMN_NEXARINFOMEDIA_TABLE };

			//Database creation SQL statement
			private static final String DATABASE_CREATE = "create table "
								+ TABLE_STATUS
								+"("
								+COLUMN_ID + " varchar(100) primary key,"						
								+COLUMN_NEXARINFO_TABLE + " int(2),"						
								+COLUMN_MEDIA_TABLE + " int(2),"
								+COLUMN_NEXARINFOMEDIA_TABLE + " int(2)"
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
				database.execSQL("DROP TABLE IF EXISTS " +TABLE_STATUS);
				onCreate(database);
			}
}
