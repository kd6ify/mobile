package com.futureconcepts.mercury.gqueue;

public class OnReceiveFatalException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1072613330907965321L;

	public OnReceiveFatalException(Exception innerException)
	{
		super(innerException);
	}

}
