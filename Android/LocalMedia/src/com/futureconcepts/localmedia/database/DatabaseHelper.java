package com.futureconcepts.localmedia.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "LocalMedia.db";
	public static final int DATABASE_VERSION = 5;
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME,null,DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}
	
	//method called during creation if the database
	@Override
	public void onCreate(SQLiteDatabase database) {
		// TODO Auto-generated method stub	
		LocalMediaTable.onCreate(database);
		LocalMediaChunksTable.onCreate(database);
	}

	//methos called during a upgrade of the database
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		//select and save first all the data of the table you want to upgrade
				//after the upgrade put the data again in the table.		
		LocalMediaTable.onUpgrade(database, oldVersion, newVersion);
		LocalMediaChunksTable.onUpgrade(database, oldVersion, newVersion);
	}

}
