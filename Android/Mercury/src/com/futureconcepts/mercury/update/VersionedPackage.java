package com.futureconcepts.mercury.update;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.content.pm.PackageInfo;

public class VersionedPackage
{
	private static final String NS = "http://com.futureconcepts.schemas.update.bridge";

	public static final String TAG_VERSIONED_PACKAGE = "VersionedPackage";
	public static final String PACKAGE_NAME = "PackageName";
	public static final String VERSION_CODE = "VersionCode";
	public static final String PACKAGE_ID = "PackageId";
	public static final String FRIENDLY_NAME = "FriendlyName";
	
	public String packageName;
	public int versionCode;
	public String packageId;
	public String friendlyName;
	public String action;

	public VersionedPackage()
	{
	}
	
	public VersionedPackage(PackageInfo packageInfo)
	{
		packageName = packageInfo.packageName;
		versionCode = packageInfo.versionCode;
	}
	
	public VersionedPackage(XmlPullParser p) throws NumberFormatException, XmlPullParserException, IOException
	{
		while (p.nextToken() != XmlPullParser.END_TAG)
		{
			if (p.getName().contains(FRIENDLY_NAME))
			{
				friendlyName = p.nextText();
			}
			else if (p.getName().contains(PACKAGE_ID))
			{
				packageId = p.nextText();
			}
			else if (p.getName().contains(PACKAGE_NAME))
			{
				packageName = p.nextText();
			}
			else if (p.getName().contains(VERSION_CODE))
			{
				versionCode = Integer.parseInt(p.nextText());
			}
		}
	}
	
	public void serialize(XmlSerializer s) throws IllegalArgumentException, IllegalStateException, IOException
	{
//		s.startTag(UPDATE_BRIDGE_CONTRACT_NS,  VERSIONED_PACKAGE);
		s.startTag(NS,  TAG_VERSIONED_PACKAGE);
		if (friendlyName != null)
		{
			s.startTag(NS, FRIENDLY_NAME);
			s.text(friendlyName);
			s.endTag(NS, FRIENDLY_NAME);
		}
		if (packageId != null)
		{
			s.startTag(NS, PACKAGE_ID);
			s.text(packageId);
			s.endTag(NS, PACKAGE_ID);
		}
		if (packageName != null)
		{
			s.startTag(NS,  PACKAGE_NAME);
			s.text(packageName);
			s.endTag(NS,  PACKAGE_NAME);
		}
		s.startTag(NS, VERSION_CODE);
		s.text(Integer.toString(versionCode));
		s.endTag(NS, VERSION_CODE);
		s.endTag(NS, TAG_VERSIONED_PACKAGE);
	}
		
	@Override
	public String toString()
	{
		return "VersionedPackage: " + " " + packageName + " " + versionCode;
	}
}
