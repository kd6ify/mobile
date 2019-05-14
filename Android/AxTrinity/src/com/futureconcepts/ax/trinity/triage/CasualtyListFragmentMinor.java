package com.futureconcepts.ax.trinity.triage;

import com.futureconcepts.ax.model.data.Triage;
import com.futureconcepts.ax.trinity.Config;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.gqueue.MercurySettings;

public class CasualtyListFragmentMinor extends CasualtyListFragmentBase
{
	public static final String TAG = CasualtyListFragmentMinor.class.getSimpleName();
	
    public CasualtyListFragmentMinor()
    {
    	_textColor = R.color.green_button_gradient_end;
    }
    
    public static CasualtyListFragmentMinor newInstance()
    {
    	CasualtyListFragmentMinor result = new CasualtyListFragmentMinor();
    	return result;
    }

	@Override
	protected Triage queryCasualties()
	{
		return Triage.queryMinor(getActivity(), MercurySettings.getCurrentIncidentId(getActivity()));
	}
}
