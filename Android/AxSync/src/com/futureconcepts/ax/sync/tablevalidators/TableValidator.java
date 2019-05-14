package com.futureconcepts.ax.sync.tablevalidators;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

public class TableValidator implements ITableValidate{

	@Override
	public boolean valid(ContentValues values, Context context) {
		// TODO Auto-generated method stub
		Log.e("General Vlidator", "general validator called");
		return true;
	}

}
