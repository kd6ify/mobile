package com.futureconcepts.drake.config;

import android.content.Context;

public class XMPPConfigFactory extends ConfigBase
{
	private static final String CONFIG_PROFILE = "xmpp_config_profile";
	
	public static IXMPPConfig get(Context context)
	{
		String profileName = getGlobalStringValue(context, CONFIG_PROFILE, "google-talk");
		if (profileName.equals("google-talk"))
		{
			return new GoogleTalkConfig(context);
		}
		else if (profileName.equals("ax"))
		{
			return new AXConfig(context);
		}
		else
		{
			return new DefaultXmppConfig(context);
		}
	}
}
