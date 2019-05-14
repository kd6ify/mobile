package com.futureconcepts.ax.sync;

public class UpdateFailedException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public UpdateFailedException(String database, String table, String id)
	{
		super(String.format("%s.%s.%s", database, table, id));
	}
}
