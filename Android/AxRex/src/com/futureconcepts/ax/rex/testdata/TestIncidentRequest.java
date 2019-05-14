package com.futureconcepts.ax.rex.testdata;

import com.futureconcepts.ax.model.data.IncidentRequest;

import android.database.MatrixCursor;

public class TestIncidentRequest extends MatrixCursor
{
	private static String[] _sColumns = { IncidentRequest._ID, IncidentRequest.ID };
	
	public TestIncidentRequest()
	{
		super(_sColumns);
		addRow(new Object[] { 1, "abc"});
		addRow(new Object[] { 2, "def" });
	}
}
