package com.futureconcepts.contentprovider.anonymous;

import java.util.Arrays;
import java.util.HashSet;

import com.futureconcepts.database.AnonymousCategoryTable;
import com.futureconcepts.database.AnonymousTable;
import com.futureconcepts.database.DatabaseHelper;
import com.futureconcepts.database.SchoolTypeTable;
import com.futureconcepts.database.SchoolsTable;
import com.futureconcepts.database.StatesTable;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class MyContentProvider extends ContentProvider {
	 
	// database
	  private DatabaseHelper database;
	  private static final String PROVIDER_NAME = "com.futureconcepts.contentprovider.anonymous";
	 	  
	  // Used for the UriMacher
	  private static final int ANONYMOUS_CATEGORY = 10;
	  private static final int ANONYMOUS_CATEGORY_ID = 11;
	  private static final int ANONYMOUS_INFO = 12;
	  private static final int ANONYMOUS_INFO_ID = 13;
	  private static final int SCHOOLS = 14;
	  private static final int SCHOOLS_ID = 15;
	  private static final int SCHOOL_TYPE = 16;
	  private static final int  SCHOOL_TYPE_ID = 17;
	  private static final int STATES = 18;
	  private static final int STATES_ID = 19;
	
	  private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	  static {
		 //uris for SuspiciousActivity table
	    sURIMatcher.addURI(PROVIDER_NAME, "AnonymousCtegoryTable",ANONYMOUS_CATEGORY);
	    sURIMatcher.addURI(PROVIDER_NAME, "AnonymousCtegoryTable" + "/#", ANONYMOUS_CATEGORY_ID);
	    
	    sURIMatcher.addURI(PROVIDER_NAME, "AnonymousInfo",ANONYMOUS_INFO );
	    sURIMatcher.addURI(PROVIDER_NAME, "AnonymousInfo" + "/#", ANONYMOUS_INFO_ID);
	    
	    sURIMatcher.addURI(PROVIDER_NAME, "Schools",SCHOOLS );
	    sURIMatcher.addURI(PROVIDER_NAME, "Schools" + "/#", SCHOOLS_ID);
	    
	    sURIMatcher.addURI(PROVIDER_NAME, "SchoolTypes",SCHOOL_TYPE );
	    sURIMatcher.addURI(PROVIDER_NAME, "SchoolsTypes" + "/#", SCHOOL_TYPE_ID);
	    
	    sURIMatcher.addURI(PROVIDER_NAME, "States",STATES );
	    sURIMatcher.addURI(PROVIDER_NAME, "States" + "/#", STATES_ID);
	    
	    }
	  @Override
		public boolean onCreate() {
		   database = new DatabaseHelper(getContext());
			return false;
		}

	  @Override
	  public Cursor query(Uri uri, String[] projection, String selection,
	      String[] selectionArgs, String sortOrder) {

		  // Uisng SQLiteQueryBuilder instead of query() method
		  SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		  // Check if the caller has requested a column which does not exists
		  checkColumns(projection);
		  // Set the tables
		  queryBuilder.setTables(AnonymousCategoryTable.ANONYMOUS_CATEGORY_TABLE);
		 // queryBuilder.setTables(MediaTable.MEDIA_TABLE);
	    
		  int uriType = sURIMatcher.match(uri);
		  switch (uriType) {
		  case ANONYMOUS_CATEGORY:
			  break;
		  case ANONYMOUS_CATEGORY_ID:
			  // Adding the ID to the original query
			  queryBuilder.appendWhere(AnonymousCategoryTable.COLUMN_ID + "="
	          + uri.getLastPathSegment());
			  break;	
		  default:
			  throw new IllegalArgumentException("Unknown URI on Cursor Query method: " + uri);	      
	    }

	    SQLiteDatabase db = database.getWritableDatabase();
	    Cursor cursor = queryBuilder.query(db, projection, selection,
	        selectionArgs, null, null, sortOrder);
	    // Make sure that potential listeners are getting notified
	    cursor.setNotificationUri(getContext().getContentResolver(), uri);
	    return cursor;
	  }
	  @Override
	  public String getType(Uri uri) {
		  final int match = sURIMatcher.match(uri);
	        switch (match) {
	        case ANONYMOUS_CATEGORY:
	            return AnonymousCategoryTable.CONTENT_TYPE;
	        case ANONYMOUS_CATEGORY_ID:
	            return AnonymousCategoryTable.CONTENT_ITEM_TYPE;  
	        case ANONYMOUS_INFO:
	            return AnonymousTable.CONTENT_TYPE;
	        case ANONYMOUS_INFO_ID:
	            return AnonymousTable.CONTENT_ITEM_TYPE; 	            
	        case SCHOOLS:
	            return SchoolsTable.CONTENT_TYPE;
	        case SCHOOLS_ID:
	            return SchoolsTable.CONTENT_ITEM_TYPE;	            
	        case SCHOOL_TYPE:
	            return SchoolTypeTable.CONTENT_TYPE;
	        case SCHOOL_TYPE_ID:
	            return SchoolTypeTable.CONTENT_ITEM_TYPE;
	        case STATES:
	            return StatesTable.CONTENT_TYPE;
	        case STATES_ID:
	            return StatesTable.CONTENT_ITEM_TYPE;
	        default:
	            throw new UnsupportedOperationException("Unknown uri on getType method: " + uri);
	        }
	  }
	  
	  
	  @Override
	  public Uri insert(Uri uri, ContentValues values) {
	    //get the correct uri
		int uriType = sURIMatcher.match(uri);		
	    SQLiteDatabase sqlDB = database.getWritableDatabase();
	    int rowsDeleted = 0;
	    long id = 0;
	    //insert into the correct table
	    switch (uriType) {
	    case ANONYMOUS_CATEGORY:
	      id = sqlDB.insert(AnonymousCategoryTable.ANONYMOUS_CATEGORY_TABLE, null, values);
	      getContext().getContentResolver().notifyChange(uri, null);
	      return uri = Uri.parse("AnonymousCategoryTable"+"/"+id);	 
	      
	    case ANONYMOUS_INFO:
		      id = sqlDB.insert(AnonymousTable.ANONYMOUS_INFO_TABLE, null, values);
		      getContext().getContentResolver().notifyChange(uri, null);
		      return uri = Uri.parse("AnonymousTable"+"/"+id);
		      
	    case SCHOOLS:
		      id = sqlDB.insert(SchoolsTable.SCHOOLS_TABLE, null, values);
		      getContext().getContentResolver().notifyChange(uri, null);
		      return uri = Uri.parse("SchoolsTable"+"/"+id);
		      
	    case SCHOOL_TYPE:
		      id = sqlDB.insert(SchoolTypeTable.SCHOOL_TYPES_TABLE, null, values);
		      getContext().getContentResolver().notifyChange(uri, null);
		      return uri = Uri.parse("SchoolTypeTable"+"/"+id);
		      
	    case STATES:
		      id = sqlDB.insert(StatesTable.STATES_TABLE, null, values);
		      getContext().getContentResolver().notifyChange(uri, null);
		      return uri = Uri.parse("StatesTable"+"/"+id);
	    default:
	      throw new IllegalArgumentException("Unknown URI on insert method: " + uri);
	    }	    
	  }//END INSERT
	  
	  
	  @Override
	  public int delete(Uri uri, String selection, String[] selectionArgs) {
	    int uriType = sURIMatcher.match(uri);
	    SQLiteDatabase sqlDB = database.getWritableDatabase();
	    int rowsDeleted = 0;
	    switch (uriType) {
	    case ANONYMOUS_CATEGORY:
	      rowsDeleted = sqlDB.delete(AnonymousCategoryTable.ANONYMOUS_CATEGORY_TABLE, selection,
	          selectionArgs);
	      break;
	    case ANONYMOUS_CATEGORY_ID:
	      String id = uri.getLastPathSegment();
	      if (TextUtils.isEmpty(selection)) {
	    	  rowsDeleted = sqlDB.delete(AnonymousCategoryTable.ANONYMOUS_CATEGORY_TABLE,
	    			  AnonymousCategoryTable.COLUMN_ID + "=" + id, 
	    	            null);
	       } else {
	    	        rowsDeleted = sqlDB.delete(AnonymousCategoryTable.ANONYMOUS_CATEGORY_TABLE,
	    	        		AnonymousCategoryTable.COLUMN_ID + "=" + id 
	    	            + " and " + selection,
	    	            selectionArgs);
	    	       }
	    	 break;
	    	 
	    case ANONYMOUS_INFO:
		      rowsDeleted = sqlDB.delete(AnonymousTable.ANONYMOUS_INFO_TABLE, selection,
		          selectionArgs);
		      break;
		    case ANONYMOUS_INFO_ID:
		      String idInfo = uri.getLastPathSegment();
		      if (TextUtils.isEmpty(selection)) {
		    	  rowsDeleted = sqlDB.delete(AnonymousTable.ANONYMOUS_INFO_TABLE,
		    			  AnonymousTable.COLUMN_ID + "=" + idInfo, 
		    	            null);
		       } else {
		    	        rowsDeleted = sqlDB.delete(AnonymousTable.ANONYMOUS_INFO_TABLE,
		    	        		AnonymousTable.COLUMN_ID + "=" + idInfo
		    	            + " and " + selection,
		    	            selectionArgs);
		    	       }
		    	 break;
		    	 
		    case SCHOOLS:
			      rowsDeleted = sqlDB.delete(SchoolsTable.SCHOOLS_TABLE, selection,
			          selectionArgs);
			      break;
			    case SCHOOLS_ID:
			      String idSchool = uri.getLastPathSegment();
			      if (TextUtils.isEmpty(selection)) {
			    	  rowsDeleted = sqlDB.delete(SchoolsTable.SCHOOLS_TABLE,
			    			  SchoolsTable.COLUMN_ID + "=" + idSchool, 
			    	            null);
			       } else {
			    	        rowsDeleted = sqlDB.delete(SchoolsTable.SCHOOLS_TABLE,
			    	        		SchoolsTable.COLUMN_ID + "=" + idSchool
			    	            + " and " + selection,
			    	            selectionArgs);
			    	       }
			    	 break;
			    	 
			    case SCHOOL_TYPE:
				      rowsDeleted = sqlDB.delete(SchoolTypeTable.SCHOOL_TYPES_TABLE, selection,
				          selectionArgs);
				      break;
				    case SCHOOL_TYPE_ID:
				      String idSchoolType = uri.getLastPathSegment();
				      if (TextUtils.isEmpty(selection)) {
				    	  rowsDeleted = sqlDB.delete(SchoolTypeTable.SCHOOL_TYPES_TABLE,
				    			  SchoolTypeTable.COLUMN_ID + "=" + idSchoolType, 
				    	            null);
				       } else {
				    	        rowsDeleted = sqlDB.delete(SchoolTypeTable.SCHOOL_TYPES_TABLE,
				    	        		SchoolTypeTable.COLUMN_ID + "=" + idSchoolType
				    	            + " and " + selection,
				    	            selectionArgs);
				    	       }
				    	 break;
				    	 
				    case STATES:
					      rowsDeleted = sqlDB.delete(StatesTable.STATES_TABLE, selection,
					          selectionArgs);
					      break;
					    case STATES_ID:
					      String idStates = uri.getLastPathSegment();
					      if (TextUtils.isEmpty(selection)) {
					    	  rowsDeleted = sqlDB.delete(StatesTable.STATES_TABLE,
					    			  StatesTable.COLUMN_ID + "=" + idStates, 
					    	            null);
					       } else {
					    	        rowsDeleted = sqlDB.delete(StatesTable.STATES_TABLE,
					    	        		StatesTable.COLUMN_ID + "=" + idStates
					    	            + " and " + selection,
					    	            selectionArgs);
					    	       }
					    	 break;
     
	     default:
	    	      throw new IllegalArgumentException("Unknown URI on delete method: " + uri);
	    	    }
	    	    getContext().getContentResolver().notifyChange(uri, null);
	    	    return rowsDeleted;
	    	  }//END DELETE
	
	  @Override
 	 public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

 	    int uriType = sURIMatcher.match(uri);
 	    SQLiteDatabase sqlDB = database.getWritableDatabase();
 	    int rowsUpdated = 0;
 	    switch (uriType) {
 	    	case ANONYMOUS_CATEGORY:
 	    		rowsUpdated = sqlDB.update(AnonymousCategoryTable.ANONYMOUS_CATEGORY_TABLE, 
 	    				values, 
 	    				selection,
 	    				selectionArgs);
 	      
 	    	break;
 	    	case ANONYMOUS_CATEGORY_ID:
 	    		String id = uri.getLastPathSegment();
 	    		if (TextUtils.isEmpty(selection)) {
 	    			rowsUpdated = sqlDB.update(AnonymousCategoryTable.ANONYMOUS_CATEGORY_TABLE, 
 	    					values,
 	    					AnonymousCategoryTable.COLUMN_ID + "=" + id, 
 	    					null);
 	    		} else {
 	    			rowsUpdated = sqlDB.update(AnonymousCategoryTable.ANONYMOUS_CATEGORY_TABLE, 
 	    					values,
 	    					AnonymousCategoryTable.COLUMN_ID + "=" + id 
 	    					+ " and " 
 	    					+ selection,
 	    					selectionArgs);
 	    		}
 	    		break;
 	    	case ANONYMOUS_INFO:
 	    		rowsUpdated = sqlDB.update(AnonymousTable.ANONYMOUS_INFO_TABLE, 
 	    				values, 
 	    				selection,
 	    				selectionArgs);
 	      
 	    	break;
 	    	case ANONYMOUS_INFO_ID:
 	    		String idInfo = uri.getLastPathSegment();
 	    		if (TextUtils.isEmpty(selection)) {
 	    			rowsUpdated = sqlDB.update(AnonymousTable.ANONYMOUS_INFO_TABLE, 
 	    					values,
 	    					AnonymousTable.COLUMN_ID + "=" + idInfo, 
 	    					null);
 	    		} else {
 	    			rowsUpdated = sqlDB.update(AnonymousTable.ANONYMOUS_INFO_TABLE, 
 	    					values,
 	    					AnonymousTable.COLUMN_ID + "=" + idInfo 
 	    					+ " and " 
 	    					+ selection,
 	    					selectionArgs);
 	    		}
 	    		break;
 	    		
 	   	case SCHOOLS:
	    		String idSchool = uri.getLastPathSegment();
	    		if (TextUtils.isEmpty(selection)) {
	    			rowsUpdated = sqlDB.update(SchoolsTable.SCHOOLS_TABLE, 
	    					values,
	    					SchoolsTable.COLUMN_ID + "=" + idSchool, 
	    					null);
	    		} else {
	    			rowsUpdated = sqlDB.update(SchoolsTable.SCHOOLS_TABLE, 
	    					values,
	    					SchoolsTable.COLUMN_ID + "=" + idSchool 
	    					+ " and " 
	    					+ selection,
	    					selectionArgs);
	    		}
	    		break;
	    		
 	   	case SCHOOL_TYPE:
    		String idSchoolType = uri.getLastPathSegment();
    		if (TextUtils.isEmpty(selection)) {
    			rowsUpdated = sqlDB.update(SchoolTypeTable.SCHOOL_TYPES_TABLE, 
    					values,
    					SchoolTypeTable.COLUMN_ID + "=" + idSchoolType, 
    					null);
    		} else {
    			rowsUpdated = sqlDB.update(SchoolTypeTable.SCHOOL_TYPES_TABLE, 
    					values,
    					SchoolTypeTable.COLUMN_ID + "=" + idSchoolType 
    					+ " and " 
    					+ selection,
    					selectionArgs);
    		}
    		break;
    		
 	  	case STATES:
    		String idStates = uri.getLastPathSegment();
    		if (TextUtils.isEmpty(selection)) {
    			rowsUpdated = sqlDB.update(StatesTable.STATES_TABLE, 
    					values,
    					StatesTable.COLUMN_ID + "=" + idStates, 
    					null);
    		} else {
    			rowsUpdated = sqlDB.update(StatesTable.STATES_TABLE, 
    					values,
    					StatesTable.COLUMN_ID + "=" + idStates 
    					+ " and " 
    					+ selection,
    					selectionArgs);
    		}
    		break;
 	    default:
 	      throw new IllegalArgumentException("Unknown URI on update method: " + uri);
 	    }
 	    getContext().getContentResolver().notifyChange(uri, null);
 	    return rowsUpdated;
 	  }//END UPDATE
	  
	  private void checkColumns(String[] projection) {	    	   
  	    if (projection != null) {
  	      HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
  	      HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(projection));
  	      // Check if all columns which are requested are available
  	      if (!availableColumns.containsAll(requestedColumns)) {
  	        throw new IllegalArgumentException("Unknown columns in projection");
  	      }
  	    }
  	  }//END CHECK COLUMNS
	  

}
