package com.futureconcepts.ax.sync.tablevalidators;

import com.futureconcepts.ax.model.data.JournalEntryMedia;
import com.futureconcepts.ax.model.data.Media;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class TableValidatorJournalEntryMedia implements ITableValidate {
		
	public TableValidatorJournalEntryMedia(){};
	
	@Override
	public boolean valid(ContentValues values, Context context) {
		// TODO Auto-generated method stub
		//super.validate(values);
		String mediaToVerify = values.getAsString(JournalEntryMedia.MEDIA);
		Cursor media = Media.queryMediaId(context, mediaToVerify);
		if(media.getCount()<=0){
			//Throw exception because that media do not exist
			media.close();
			return false;
		}else{
			media.close();
			return true;
		}
	}
}
