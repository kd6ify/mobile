package com.futureconcepts.mercury.update;

import org.xmlpull.v1.XmlPullParserException;

public class ParseUpdateException extends XmlPullParserException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 433871009278918302L;

	public ParseUpdateException(String s, int row, int column)
	{
		super(s);
		this.row = row;
		this.column = column;
	}
}
