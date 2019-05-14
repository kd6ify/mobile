package com.futureconcepts.ax.sync;

public class InsertFailedException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public InsertFailedException(String database, String table, String id)
	{
		super(String.format("%s.%s.%s", database, table, id));
	}
}
