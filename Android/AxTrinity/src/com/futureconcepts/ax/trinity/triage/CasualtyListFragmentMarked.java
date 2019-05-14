package com.futureconcepts.ax.trinity.triage;

import com.futureconcepts.ax.model.data.Triage;
import com.futureconcepts.ax.trinity.R;

public class CasualtyListFragmentMarked extends CasualtyListFragmentBase
{
	public static final String TAG = CasualtyListFragmentMarked.class.getSimpleName();

	public CasualtyListFragmentMarked()
    {
    	_textColor = R.color.orange;
    }
	
    public static CasualtyListFragmentMarked newInstance()
    {
    	CasualtyListFragmentMarked result = new CasualtyListFragmentMarked();
    	return result;
    }
    
	@Override
	protected Triage queryCasualties()
	{
		return Triage.queryMarked(getActivity());
	}
}
