package com.futureconcepts.mercury.update;

import java.io.IOException;
import java.util.List;

import org.xmlpull.v1.XmlSerializer;

import android.content.pm.PackageInfo;

public class VersionedPackages
{
	public static final String NS = "http://com.futureconcepts.schemas.update.bridge";	
	private static final String TAG_DEVICE_NAME = "DeviceName";
	private static final String TAG_OS_FAMILY = "OSFamily";
	private static final String TAG_OS_DESCRIPTION = "OSDescription";
	private static final String TAG_OS_MAJOR_VERSION = "OSMajorVersion";
    public String deviceName;
    public String osFamily;
    public String osDescription;
    public int osMajorVersion;
	public List<PackageInfo> packageInfos;

	public VersionedPackages(List<PackageInfo> packageInfos)
	{
		this.packageInfos = packageInfos;
	}
	
	public void serialize(XmlSerializer s) throws IllegalArgumentException, IllegalStateException, IOException
	{
//		s.setPrefix("a", UPDATE_BRIDGE_CONTRACT_NS);
//		s.startTag(UPDATE_BRIDGE_CONTRACT_NS,  PACKAGES_TAG);
		s.startTag(NS,  "VersionedPackages");
		if (deviceName != null)
		{
			s.startTag(NS, TAG_DEVICE_NAME);
			s.text(deviceName);
			s.endTag(NS,  TAG_DEVICE_NAME);
		}
		if (osFamily != null)
		{
			s.startTag(NS, TAG_OS_FAMILY);
			s.text(osFamily);
			s.endTag(NS,  TAG_OS_FAMILY);
		}
		if (osDescription != null)
		{
			s.startTag(NS, TAG_OS_DESCRIPTION);
			s.text(osDescription);
			s.endTag(NS,  TAG_OS_DESCRIPTION);
		}
		s.startTag(NS, TAG_OS_MAJOR_VERSION);
		s.text(Integer.toString(osMajorVersion));
		s.endTag(NS,  TAG_OS_MAJOR_VERSION);
		s.startTag(NS,  "Packages");
		for (PackageInfo packageInfo : packageInfos)
		{
			VersionedPackage versionedPackage = new VersionedPackage(packageInfo);
			versionedPackage.serialize(s);
		}
//		s.endTag(UPDATE_BRIDGE_CONTRACT_NS, PACKAGES_TAG);
		s.endTag(NS, "Packages");
		s.endTag(NS, "VersionedPackages");
//		s.setPrefix("", UPDATE_BRIDGE_CONTRACT_NS);
	}
}
