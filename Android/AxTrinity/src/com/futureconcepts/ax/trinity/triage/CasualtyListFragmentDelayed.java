package com.futureconcepts.ax.trinity.triage;

import com.futureconcepts.ax.model.data.Triage;
import com.futureconcepts.ax.trinity.Config;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.gqueue.MercurySettings;

public class CasualtyListFragmentDelayed extends CasualtyListFragmentBase
{
	public static final String TAG = CasualtyListFragmentDelayed.class.getSimpleName();

	public CasualtyListFragmentDelayed()
	{
		_textColor = R.color.yellow;
	}
	
    public static CasualtyListFragmentDelayed newInstance()
    {
    	CasualtyListFragmentDelayed result = new CasualtyListFragmentDelayed();
    	return result;
    }

	@Override
	protected Triage queryCasualties()
	{
		return Triage.queryDelayed(getActivity(), MercurySettings.getCurrentIncidentId(getActivity()));
	}
}
