package com.futureconcepts.drake.config;

import android.content.Context;

public class AXConfig extends ConfigBase implements IXMPPConfig
{
	private Context _context;
	
	public AXConfig(Context context)
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
		return "chat.antaresx.net";
	}
	
	public boolean getAllowPlainAuth()
	{
		return false;
	}

	public boolean getRequireTls()
	{
		return true;
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
		return "205.129.7.33";
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
