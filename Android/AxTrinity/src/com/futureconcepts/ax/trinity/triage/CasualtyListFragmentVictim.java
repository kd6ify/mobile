package com.futureconcepts.ax.trinity.triage;

import com.futureconcepts.ax.model.data.Triage;
import com.futureconcepts.ax.trinity.R;

public class CasualtyListFragmentVictim extends CasualtyListFragmentBase
{
	public static final String TAG = CasualtyListFragmentVictim.class.getSimpleName();
	
    public CasualtyListFragmentVictim()
    {
    	_textColor = R.color.blue;
    }
    
    public static CasualtyListFragmentVictim newInstance()
    {
    	CasualtyListFragmentVictim result = new CasualtyListFragmentVictim();
    	return result;
    }

	@Override
	protected Triage queryCasualties()
	{
		return Triage.queryVictim(getActivity());
	}
}
