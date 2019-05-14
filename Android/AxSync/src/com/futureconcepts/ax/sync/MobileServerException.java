package com.futureconcepts.ax.sync;

public class MobileServerException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String _tableName;
	private long _version;
	private int _errorCode;
	
	public MobileServerException(String message)
	{
		super(message);
	}
	
	public void setTableName(String value)
	{
		_tableName = value;
	}
	
	public void setVersion(long value)
	{
		_version = value;
	}
	
	public void setErrorCode(int value)
	{
		_errorCode = value;
	}
}
