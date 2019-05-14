package com.futureconcepts.drake.config;

public interface IXMPPConfig
{
	String getUserName();
	String getPassword();
	String getDomain();
	boolean getAllowPlainAuth();
	boolean getRequireTls();
	boolean getDoDnsSrv();
	boolean getTlsCertVerify();
	String getServer();
	int getServerPort();
	String getResource();
	boolean getConcatUsernameWithDomain();
}
