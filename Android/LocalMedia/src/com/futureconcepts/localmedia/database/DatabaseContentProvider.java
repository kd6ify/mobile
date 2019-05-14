package com.futureconcepts.localmedia.database;

import java.util.Arrays;
import java.util.HashSet;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class DatabaseContentProvider extends ContentProvider {

	// database
	private DatabaseHelper database;
	private static final String PROVIDER_NAME = "com.futureconcepts.localmedia.database";

	private static final int MEDIA_ACTIVITY = 20;
	private static final int MEDIA_ACTIVITY_ID = 21;
	private static final int MEDIA_CHUNKS_ACTIVITY = 30;
	private static final int MEDIA_CHUNKS_ACTIVITY_ID = 31;

	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		// uris for Media table
		sURIMatcher.addURI(PROVIDER_NAME, LocalMediaTable.MEDIA_TABLE, MEDIA_ACTIVITY);
		sURIMatcher.addURI(PROVIDER_NAME, LocalMediaTable.MEDIA_TABLE + "/#", MEDIA_ACTIVITY_ID);
		// uris for MediaChunks table
		sURIMatcher.addURI(PROVIDER_NAME, LocalMediaChunksTable.MEDIA_CHUNKS_TABLE, MEDIA_CHUNKS_ACTIVITY);
		sURIMatcher.addURI(PROVIDER_NAME, LocalMediaChunksTable.MEDIA_CHUNKS_TABLE + "/#", MEDIA_CHUNKS_ACTIVITY_ID);
	}

	@Override
	public boolean onCreate() {
		//Log.d(PROVIDER_NAME, "On create called on tpovider lets look how many times");
		database = new DatabaseHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,String[] selectionArgs, String sortOrder) {
		// Uisng SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		// Check if the caller has requested a column which does not exists
		checkColumns(projection);
		// Set the tables		 
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case MEDIA_ACTIVITY:
			queryBuilder.setTables(LocalMediaTable.MEDIA_TABLE);
			break;
		case MEDIA_ACTIVITY_ID:
			// Adding the ID to the original query
			 queryBuilder.appendWhere(LocalMediaTable.COLUMN_ID + "="+ uri.getLastPathSegment());
			break;
		case MEDIA_CHUNKS_ACTIVITY:
			queryBuilder.setTables(LocalMediaChunksTable.MEDIA_CHUNKS_TABLE);
			break;
		case MEDIA_CHUNKS_ACTIVITY_ID:
			// Adding the ID to the original query
			 queryBuilder.appendWhere(LocalMediaChunksTable.COLUMN_ID + "="+ uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI on Cursor Query method: " + uri);
		}
		SQLiteDatabase db = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,selectionArgs, null, null, sortOrder);
		// Make sure that potential listeners are getting notified
		//cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
		
	}

	@Override
	public String getType(Uri uri) {
		final int match = sURIMatcher.match(uri);
		switch (match) {
		case MEDIA_ACTIVITY:
			return LocalMediaTable.CONTENT_TYPE;
		case MEDIA_ACTIVITY_ID:
            return LocalMediaTable.CONTENT_ITEM_TYPE;
		case MEDIA_CHUNKS_ACTIVITY:
			return LocalMediaTable.CONTENT_TYPE;
		case MEDIA_CHUNKS_ACTIVITY_ID:
            return LocalMediaTable.CONTENT_ITEM_TYPE;
		default:
			throw new UnsupportedOperationException(
					"Unknown uri on getType method: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// get the correct uri
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		long id = 0;
		// insert into the correct table
		switch (uriType) {
		case MEDIA_ACTIVITY:
			id = sqlDB.insert(LocalMediaTable.MEDIA_TABLE, null, values);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri = Uri.parse("Media" + "/" + id);
		case MEDIA_CHUNKS_ACTIVITY:
			id = sqlDB.insert(LocalMediaChunksTable.MEDIA_CHUNKS_TABLE, null, values);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri = Uri.parse("Media" + "/" + id);
		default:
			throw new IllegalArgumentException("Unknown URI on insert method: "
					+ uri);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case MEDIA_ACTIVITY:
			rowsDeleted = sqlDB.delete(LocalMediaTable.MEDIA_TABLE, selection,
					selectionArgs);
			break;
		case MEDIA_ACTIVITY_ID:
			String idMedia = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(LocalMediaTable.MEDIA_TABLE,
						LocalMediaTable.COLUMN_ID + "=" + idMedia, null);
			} else {
				rowsDeleted = sqlDB.delete(LocalMediaTable.MEDIA_TABLE,
						LocalMediaTable.COLUMN_ID + "=" + idMedia + " and "
								+ selection, selectionArgs);
			}
			break;
			
		case MEDIA_CHUNKS_ACTIVITY:
			rowsDeleted = sqlDB.delete(LocalMediaChunksTable.MEDIA_CHUNKS_TABLE, selection,
					selectionArgs);
			break;
		case MEDIA_CHUNKS_ACTIVITY_ID:
			String idMediaChunk = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(LocalMediaChunksTable.MEDIA_CHUNKS_TABLE,
						LocalMediaChunksTable.COLUMN_ID + "=" + idMediaChunk, null);
			} else {
				rowsDeleted = sqlDB.delete(LocalMediaChunksTable.MEDIA_CHUNKS_TABLE,
						LocalMediaChunksTable.COLUMN_ID + "=" + idMediaChunk + " and "
								+ selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI on delete method: "
					+ uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsUpdated = 0;
		switch (uriType) {
		case MEDIA_ACTIVITY:
			rowsUpdated = sqlDB.update(LocalMediaTable.MEDIA_TABLE, values,
					selection, selectionArgs);
			break;			
		case MEDIA_ACTIVITY_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(LocalMediaTable.MEDIA_TABLE, values,
						LocalMediaTable.COLUMN_ID + "=" + id, null);
			} else {
				rowsUpdated = sqlDB.update(LocalMediaTable.MEDIA_TABLE, values,
						LocalMediaTable.COLUMN_ID + "=" + id + " and " + selection,
						selectionArgs);
			}
			break;
			
		case MEDIA_CHUNKS_ACTIVITY:
			rowsUpdated = sqlDB.update(LocalMediaChunksTable.MEDIA_CHUNKS_TABLE, values,
					selection, selectionArgs);
			break;
		case MEDIA_CHUNKS_ACTIVITY_ID:
			String idChunk = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(LocalMediaChunksTable.MEDIA_CHUNKS_TABLE, values,
						LocalMediaChunksTable.COLUMN_ID + "=" + idChunk, null);
			} else {
				rowsUpdated = sqlDB.update(LocalMediaChunksTable.MEDIA_CHUNKS_TABLE, values,
						LocalMediaChunksTable.COLUMN_ID + "=" + idChunk + " and " + selection,
						selectionArgs);
			}
			break;	
		default:
			throw new IllegalArgumentException("Unknown URI on update method: "
					+ uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	private void checkColumns(String[] projection) {
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(
					Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(
					Arrays.asList(projection));
			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException(
						"Unknown columns in projection");
			}
		}
	}

}
