package com.futureconcepts.ax.trinity.triage;

import com.futureconcepts.ax.model.data.Triage;
import com.futureconcepts.ax.trinity.Config;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.gqueue.MercurySettings;

public class CasualtyListFragmentDeceased extends CasualtyListFragmentBase
{
	public static final String TAG = CasualtyListFragmentDeceased.class.getSimpleName();
	
	public CasualtyListFragmentDeceased()
	{
		_textColor = R.color.grey_button_gradient_start;
	}
	
    public static CasualtyListFragmentDeceased newInstance()
    {
    	CasualtyListFragmentDeceased result = new CasualtyListFragmentDeceased();
    	return result;
    }

	@Override
	protected Triage queryCasualties()
	{
		Triage result = Triage.queryDeceased(getActivity(), MercurySettings.getCurrentIncidentId(getActivity()));
		return result;
	}
}
