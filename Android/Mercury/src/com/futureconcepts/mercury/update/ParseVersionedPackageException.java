package com.futureconcepts.mercury.update;

import org.xmlpull.v1.XmlPullParserException;

public class ParseVersionedPackageException extends XmlPullParserException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 433871009278918303L;

	public ParseVersionedPackageException(String s, int row, int column)
	{
		super(s);
		this.row = row;
		this.column = column;
	}
}
