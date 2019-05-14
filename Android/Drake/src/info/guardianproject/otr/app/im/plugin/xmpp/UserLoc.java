package info.guardianproject.otr.app.im.plugin.xmpp;

import org.jivesoftware.smackx.packet.PEPItem;

import android.location.Location;

public class UserLoc extends PEPItem
{
	private Location _location;
	public static final String NAMESPACE = "http://jabber.org/protocol/geoloc"; 

	public UserLoc(String id, Location location)
	{
		super(id);
		_location = location;
	}

	@Override
	public String getNode()
	{
		return NAMESPACE;
	}

	@Override
	public String getItemDetailsXML()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<geoloc xmlns='http://jabber.org/protocol/geoloc' xml:lang='en'>");

		// TODO <time>
		
		if (_location.hasAccuracy())
		{
			sb.append("<accuracy>");
			sb.append(Float.toString(_location.getAccuracy()));
			sb.append("</accuracy>");
		}
		if (_location.hasAltitude())
		{
			sb.append("<alt>");
			sb.append(Double.toString(_location.getAltitude()));
			sb.append("</alt>");
		}
		if (_location.hasBearing())
		{
			sb.append("<bearing>");
			sb.append(Float.toString(_location.getBearing()));
			sb.append("</bearing>");
		}
		if (_location.hasSpeed())
		{
			sb.append("<speed>");
			sb.append(Float.toString(_location.getSpeed()));
			sb.append("</speed>");
		}
		sb.append("<lat>");
		sb.append(Double.toString(_location.getLatitude()));
		sb.append("</lat>");
		
		sb.append("<lon>");
		sb.append(Double.toString(_location.getLongitude()));
		sb.append("</lon>");
		sb.append("</geoloc>");

		return sb.toString();
	}
}
