package com.futureconcepts.ax.sync.tablevalidators;

import android.content.ContentValues;
import android.content.Context;

public interface ITableValidate {

	public boolean valid(ContentValues values, Context context);
}
