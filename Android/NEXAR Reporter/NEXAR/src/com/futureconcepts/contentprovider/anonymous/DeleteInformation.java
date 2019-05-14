package com.futureconcepts.contentprovider.anonymous;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.futureconcepts.database.AnonymousInfoMediaTable;
import com.futureconcepts.database.AnonymousTable;
import com.futureconcepts.database.DatabaseHelper;
import com.futureconcepts.database.MediaTable;
import com.futureconcepts.database.TableStatus;


public class DeleteInformation extends Activity {

	  public void deleteAllInfo(Context con){
		  DatabaseHelper dbHelper = new DatabaseHelper(con);
			SQLiteDatabase newDB = dbHelper.getWritableDatabase();
		    Cursor Csus = newDB.rawQuery("Select * from AnonymousInfo",null);
			Cursor Cmed = newDB.rawQuery("Select * from Media",null);
			Cursor CsusA = newDB.rawQuery("Select * from AnonymousInfoMedia",null);
	
			 if(Csus.getCount()!=0){
					con.getContentResolver().delete(AnonymousTable.CONTENT_URI, null,null);
			 }
			 
			 if(Cmed.getCount()!=0){
					con.getContentResolver().delete(MediaTable.CONTENT_URI, null,null);
			 }
			 
			 if(CsusA.getCount()!=0){
					con.getContentResolver().delete(AnonymousInfoMediaTable.CONTENT_URI, null,null);
			 }
	

			 Csus.close();
			 Cmed.close();
			 CsusA.close();

			 


	  }
	  
	  public void deleteReport(Context con,String ID){
		  DatabaseHelper dbHelper = new DatabaseHelper(con);
			SQLiteDatabase newDB = dbHelper.getWritableDatabase();
			
			Cursor Csus = newDB.rawQuery("Select * from AnonymousInfo WHERE ID=?", new String[]{ID});
			Cursor Cmed = newDB.rawQuery("Select m.ID from Media AS m INNER JOIN AnonymousInfoMedia AS aim on m.ID = aim.Media AND aim.NEXARInfo=?",new String[]{ID});
			Cursor CsusA = newDB.rawQuery("Select * from AnonymousInfoMedia WHERE NEXARInfo=?", new String[]{ID});
			Cursor CTS = newDB.rawQuery("Select * from TableStatus WHERE ID=?", new String[]{ID});
			//Log.d("IAMGESFOUND TO DELETE"," NUMBER: "+Cmed.getCount());
			Log.d("Images to delete"," : "+Cmed.getCount());
			String IDMed = null;
			 if(Csus.getCount()!=0){
				 Csus.moveToFirst();
					con.getContentResolver().delete(AnonymousTable.CONTENT_URI, "ID=?",new String[] { ID });
			 }
			 			 
			 if(CsusA.getCount()!=0){
				 CsusA.moveToFirst();
					con.getContentResolver().delete(AnonymousInfoMediaTable.CONTENT_URI,"NEXARInfo=?",new String[] { ID });
			 }
			 
			 if(Cmed.getCount()!=0){				
				 Cmed.moveToFirst();			
	    			do{ 
	    			    IDMed=Cmed.getString(Cmed.getColumnIndex("ID"));
	    				con.getContentResolver().delete(MediaTable.CONTENT_URI, "ID=?",new String[] {IDMed});
	    			}while(Cmed.moveToNext());	
			 }			 

			 if(CTS.getCount()!=0){
				 CTS.moveToFirst();
					con.getContentResolver().delete(TableStatus.CONTENT_URI, "ID=?",new String[] { ID });
			 }
			 
			 Csus.close();
			 Cmed.close();
			 CsusA.close();
			 CTS.close();


	  }
	  
	 
	
}
