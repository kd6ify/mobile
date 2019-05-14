package com.futureconcepts.gqueue;

public class OnRetryException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1072613330907965321L;

	private int _retryMillis;
	
	public OnRetryException(Exception innerException, int retryMillis)
	{
		super(innerException);
		_retryMillis = retryMillis;
	}
	
	public int getRetryMillis()
	{
		return _retryMillis;
	}
}
