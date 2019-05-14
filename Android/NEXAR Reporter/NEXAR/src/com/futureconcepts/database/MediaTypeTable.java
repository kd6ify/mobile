package com.futureconcepts.database;

import java.util.UUID;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class MediaTypeTable {
	//Database Table
		public static final String MEDIA_TYPE_TABLE = "MediaType";
		//Table columns
		public static final String COLUMN_ID ="ID";	
		public static final String COLUMN_NAME="Name";		
		
		/**
	     * The content:// style URL for this table
	     */
	    public static final Uri CONTENT_URI = Uri.parse("content://" +"com.futureconcepts.contentprovider.anonymous" + "/MediaType");
	    /**
	     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
	     */
	    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.MediaType";
	    /**
	     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single row.
	     */
	    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.MediaType";
	   			
		public static final String[] MEDIA_ACTIVITY_TABLE_PROJECTION = 
			{ COLUMN_ID,COLUMN_NAME };

		//Database creation SQL statement
		private static final String DATABASE_CREATE = "create table "
							+ MEDIA_TYPE_TABLE
							+"("
							+COLUMN_ID + " varchar(100) primary key, "						
							+COLUMN_NAME + " varchar(100) "
							+");";
		private static UUID IDimage= new UUID((5000000*200),(5000000*201));
		private static UUID IDvideo= new UUID((2000000*3000), (3000000*8000));
		public static void onCreate(SQLiteDatabase database)
		{
			database.execSQL(DATABASE_CREATE);
			
			//inserting the types of files on MediaType Table
			String sql ="INSERT INTO MediaType (ID, Name) VALUES('"+IDimage+"','Image')";       
		    database.execSQL(sql);
			String sql2 ="INSERT INTO MediaType (ID, Name) VALUES('"+IDvideo+"','Video')";       
		    database.execSQL(sql2);
			
		}
		
		//Upgrade Database version and delete all old data
		public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
		{
			Log.w(MediaTypeTable.class.getName(),"Upgrading database from version "+ oldVersion + " to " +newVersion
					+ ", which will destroy all old data");
			database.execSQL("DROP TABLE IF EXISTS " +MEDIA_TYPE_TABLE);
			onCreate(database);
		}
}
