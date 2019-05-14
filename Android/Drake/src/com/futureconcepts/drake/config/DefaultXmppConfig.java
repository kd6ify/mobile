package com.futureconcepts.drake.config;

import android.content.Context;

public class DefaultXmppConfig extends ConfigBase implements IXMPPConfig
{
	private Context _context;
	
	public DefaultXmppConfig(Context context)
	{
		_context = context;
	}

	public String getUserName()
	{
		return getGlobalStringValue(_context, "xmpp_username", null);
	}
	
	public String getPassword()
	{
		return getGlobalEncryptedStringValue(_context, "xmpp_password", null);
	}
	
	public String getDomain()
	{
		return "gmail.com";
	}
	
	public boolean getAllowPlainAuth()
	{
		return false;
	}

	public boolean getRequireTls()
	{
		return false;
	}

	public boolean getDoDnsSrv()
	{
		return false;
	}

	public boolean getTlsCertVerify()
	{
		return false;
	}

	public String getServer()
	{
		return "talk.google.com";
	}
	
	public int getServerPort()
	{
		return 5222;
	}

	public String getResource()
	{
		return "Drake";
	}
	
	public boolean getConcatUsernameWithDomain()
	{
		return false;
	}
}
