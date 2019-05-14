package com.futureconcepts.ax.trinity.triage;

import com.futureconcepts.ax.model.data.Triage;
import com.futureconcepts.ax.trinity.Config;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.gqueue.MercurySettings;

public class CasualtyListFragmentImmediate extends CasualtyListFragmentBase
{
	public static final String TAG = CasualtyListFragmentImmediate.class.getSimpleName();

	public CasualtyListFragmentImmediate()
	{
		_textColor = R.color.red;
	}
	
    public static CasualtyListFragmentImmediate newInstance()
    {
    	CasualtyListFragmentImmediate result = new CasualtyListFragmentImmediate();
    	return result;
    }

	@Override
	protected Triage queryCasualties()
	{
		return Triage.queryImmediate(getActivity(), MercurySettings.getCurrentIncidentId(getActivity()));
	}
}
