package com.futureconcepts.ax.sync;

public class DeleteFailedException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DeleteFailedException(String tablePath)
	{
		super(tablePath);
	}
}
