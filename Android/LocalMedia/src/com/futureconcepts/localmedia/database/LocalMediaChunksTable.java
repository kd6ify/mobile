package com.futureconcepts.localmedia.database;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class LocalMediaChunksTable {
	// Database Table
	public static final String MEDIA_CHUNKS_TABLE = "LocalMediaChunks";
	// Table columns
	public static final String COLUMN_ID = "ID";
	public static final String COLUMN_CHUNK = "chunk";
	public static final String COLUMN_POSITION = "position";
	public static final String COLUMN_FILE_SIZE = "fileSize";
	/**
	 * The content:// style URL for this table
	 */
	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ "com.futureconcepts.localmedia.database" + "/LocalMediaChunks");
	/**
	 * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
	 */
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.LocalMediaChunks";
	/**
	 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single row.
	 */
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.LocalMediaChunks";

	public static final String[] MEDIA_ACTIVITY_CHUNKS_TABLE_PROJECTION = { COLUMN_ID,
		COLUMN_CHUNK, COLUMN_POSITION, COLUMN_FILE_SIZE,};

	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table " + MEDIA_CHUNKS_TABLE
			+ "(" + COLUMN_ID + " varchar(100),"
			+ COLUMN_CHUNK + " varchar," + COLUMN_POSITION+" INT," +COLUMN_FILE_SIZE + " BIGINT);";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);

	}

	// Upgrade Database version and delete all old data
	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		// Log.w(SuspiciousActivityTable.class.getName(),"Upgrading database from version "+
		// oldVersion + " to " +newVersion
		// + ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + MEDIA_CHUNKS_TABLE);
		onCreate(database);
	}
}