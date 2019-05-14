package com.futureconcepts.ax.trinity.logs;

import com.futureconcepts.ax.model.data.Journal;
import com.futureconcepts.ax.trinity.DefaultTextWatcher;
import com.futureconcepts.ax.trinity.EditModelActivity;
import com.futureconcepts.ax.trinity.R;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;

public class EditJournalActivity extends EditModelActivity
{
	private Journal _journal;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setEditContentView(R.layout.edit_log);
    	startManagingModel(_journal = Journal.query(this, _data));
        if (beginEditReady())
    	{
        	setTextListener(R.id.title, new DefaultTextWatcher() {
				@Override
				public void afterTextChanged(Editable s)
				{
					_journal.setName(s.toString());
				}
    		});
        	setTextView(R.id.title, _journal.getName());
            if (_action != null)
            {
            	if (_action.equals(Intent.ACTION_INSERT))
            	{
            		setTitle("Add Log Category");
            	}
            	else if (_action.equals(Intent.ACTION_EDIT))
            	{
            		setTitle("Edit: " + _journal.getName());
            	}
            }
    		listenForChanges();
    	}
    }
}
