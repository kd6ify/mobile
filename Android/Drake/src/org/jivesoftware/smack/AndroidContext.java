package org.jivesoftware.smack;

import android.content.Context;

public class AndroidContext
{
	private static AndroidContext _instance;
	private Context _context;
	
	public static synchronized AndroidContext getInstance(Context context)
	{
		if (_instance == null)
		{
			_instance = new AndroidContext(context);
		}
		return _instance;
	}

	private AndroidContext(Context context)
	{
		_context = context;
	}
	
	public Context getContext()
	{
		return _context;
	}
}
