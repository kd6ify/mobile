package com.futureconcepts.ax.trinity.triage;

import com.futureconcepts.ax.trinity.R;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TriageInsertedDialogFragment extends DialogFragment
{
	public static TriageInsertedDialogFragment newInstance()
	{
		TriageInsertedDialogFragment result = new TriageInsertedDialogFragment();
		return result;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.triage_inserted_dialog, container, false);
		TextView text1 = (TextView)v.findViewById(R.id.text1);
		text1.setText("Triage successfully inserted");
		return v;
	}
}
