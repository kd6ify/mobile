package com.futureconcepts.database;

import java.io.IOException;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "anonymous.db";
	public static final int DATABASE_VERSION=1;
	private final Context fContext;
		//Data

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME,null,DATABASE_VERSION);
		fContext=context;
		// TODO Auto-generated constructor stub
	}
	
	//method called during creation if the database
	@Override
	public void onCreate(SQLiteDatabase database) {
		// TODO Auto-generated method stub
		 Log.d("in create","in create database");
		AnonymousTable.onCreate(database);
		AnonymousCategoryTable.onCreate(database);
		MediaTable.onCreate(database);
		MediaTypeTable.onCreate(database);
		AnonymousInfoMediaTable.onCreate(database);
		SchoolFeedTable.onCreate(database);
		TableStatus.onCreate(database);

		try {
		SchoolsTable.onCreate(database,fContext);
		SchoolTypeTable.onCreate(database,fContext);		
	    StatesTable.onCreate(database,fContext);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

	//methos called during a upgrade of the database
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		//select and save first all the data of the table you want to upgrade
		//AnonymousTable.onUpgrade(database, oldVersion, newVersion);
		//after the upgrade put the data again in the table.
		
	}

}
