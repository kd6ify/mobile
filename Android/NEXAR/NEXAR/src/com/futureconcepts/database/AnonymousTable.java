package com.futureconcepts.database;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class AnonymousTable {
	//Database Table
			public static final String ANONYMOUS_INFO_TABLE = "AnonymousInfo";
			//Table columns
			public static final String COLUMN_ID="ID";	
			public static final String COLUMN_NAME="Name";
			public static final String COLUMN_DETAILS="Details";
			public static final String COLUMN_DATE="Date";	
			public static final String COLUMN_TYPE="Type";
			public static final String COLUMN_SCHOOL="School";
			public static final String[] ANONYMOUS_TABLE_PROJECTION = 
				{ COLUMN_ID,COLUMN_NAME,COLUMN_DETAILS,COLUMN_DATE,COLUMN_TYPE,COLUMN_SCHOOL};

			/**
		     * The content:// style URL for this table
		     */
		    public static final Uri CONTENT_URI = Uri.parse("content://" +"com.futureconcepts.contentprovider.anonymous" + "/AnonymousInfo");
		    /**
		     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
		     */
		    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.AnonymousInfo";
		    /**
		     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single row.
		     */
		    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.AnonymousInfo";
		    /**
		     * The default sort order for this table
		     */
		    public static final String DEFAULT_SORT_ORDER = COLUMN_TYPE + "DESC";  
		    
			//Database creation SQL statement
			private static final String DATABASE_CREATE = "create table "
					+ ANONYMOUS_INFO_TABLE
					+"("
					+COLUMN_ID + " varchar(100) primary key, "
					+COLUMN_NAME + " varchar(100),"	
					+COLUMN_DETAILS + " varchar(500),"	
					+COLUMN_DATE + " varchar(20),"	
					+COLUMN_TYPE + " varchar(20),"
					+COLUMN_SCHOOL + " varchar(100)"	
					+");";
			
			public static void onCreate(SQLiteDatabase database)
			{				
				database.execSQL(DATABASE_CREATE);
			}
			
			//Upgrade Database version and delete all old data
			public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
			{
				Log.w(AnonymousCategoryTable.class.getName(),"Upgrading database from version "+ oldVersion + " to " +newVersion
						+ ", which will destroy all old data");
				database.execSQL("DROP TABLE IF EXISTS" +ANONYMOUS_INFO_TABLE);
				onCreate(database);
			}
}
