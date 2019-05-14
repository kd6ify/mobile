package com.futureconcepts.database;

import java.util.ArrayList;


import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class AnonymousCategoryTable {
	//Database Table
		public static final String ANONYMOUS_CATEGORY_TABLE = "AnonymousCategory";
		//Table columns
		public static final String COLUMN_ID="ID";	
		public static final String COLUMN_TYPE="Type";
		public static final String[] ANONYMOUS_CATEGORY_TABLE_PROJECTION = 
			{ COLUMN_ID,COLUMN_TYPE};

		/**
	     * The content:// style URL for this table
	     */
	    public static final Uri CONTENT_URI = Uri.parse("content://" +"com.futureconcepts.contentprovider.anonymous" + "/AnonymousCategory");
	    /**
	     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
	     */
	    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.AnonymousCategory";
	    /**
	     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single row.
	     */
	    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.AnonymousCategory";
	    /**
	     * The default sort order for this table
	     */
	    public static final String DEFAULT_SORT_ORDER = COLUMN_TYPE + "DESC";  
	    
		//Database creation SQL statement
		private static final String DATABASE_CREATE = "create table "
				+ ANONYMOUS_CATEGORY_TABLE
				+"("
				+COLUMN_ID + " varchar(100) primary key, "						
				+COLUMN_TYPE + " varchar(20)"						
				+");";
		
		public static void onCreate(SQLiteDatabase database)
		{
            
			//UUID u =UUID.fromString("00000000-0000-002a-0000-00000000002a");
			database.execSQL(DATABASE_CREATE);
			ArrayList<String> IDcategories= new ArrayList<String>();
			IDcategories.add("1505f8c1-5247-4cd9-af0f-7ef8949994e1");
			IDcategories.add("784b63b0-7f0a-4de2-87b1-eecdeae09e9c");
			IDcategories.add("cd06e9c5-4817-45d6-851c-abd157f86004");
			IDcategories.add("574807a0-f453-47b4-bb1b-3c58ccb3ed4b");
			IDcategories.add("5421944c-a4bb-4903-a8c2-987161bc5d04");
			IDcategories.add("2ff765fb-51c4-4eb2-b7b6-7cdb9af6802a");
			IDcategories.add("45477207-e777-4b7f-8443-5b6ecc27e971");
			IDcategories.add("f28ec29a-855a-442d-9a04-17e9c3d32c0a");
			IDcategories.add("15804aa6-a5b6-42d7-bbf9-c1ccb26f22fd");
			IDcategories.add("6ed6df40-3f11-4b17-8fd2-91b72407a7e3");
			
			ArrayList<String> category= new ArrayList<String>();
			category.add("Weapons");
			category.add("Drugs");
			category.add("Bullying");
			category.add("Violence");
			category.add("Theft");
			category.add("Safety");
			category.add("Vandalism");
			category.add("Threats");
			category.add("Other");
			category.add("Suggestions");
			
			for(int v=0; v<IDcategories.size(); v++){
			String sql ="INSERT INTO AnonymousCategory (ID, Type) VALUES('"+IDcategories.get(v)+"','"+category.get(v)+"')";       
		    database.execSQL(sql);
			}
			
		}
		
		//Upgrade Database version and delete all old data
		public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
		{
			Log.d("upgrade table","upgrade table category");
			Log.w(AnonymousCategoryTable.class.getName(),"Upgrading database from version "+ oldVersion + " to " +newVersion
					+ ", which will destroy all old data");
			database.execSQL("DROP TABLE IF EXISTS " +ANONYMOUS_CATEGORY_TABLE);
			onCreate(database);
		}
		
}
