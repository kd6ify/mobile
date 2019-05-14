package com.futureconcepts.ax.trinity.logs;

import com.futureconcepts.ax.model.data.Intelligence;
import com.futureconcepts.ax.model.data.IntelligenceStatus;
import com.futureconcepts.ax.trinity.DefaultOnItemSelectedListener;
import com.futureconcepts.ax.trinity.DefaultTextWatcher;
import com.futureconcepts.ax.trinity.EditModelActivity;
import com.futureconcepts.ax.trinity.R;

import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.AdapterView;

public class EditIntelActivity extends EditModelActivity
{
	private Intelligence _intel;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	setEditContentView(R.layout.edit_intel);
    	startManagingModel(_intel = Intelligence.query(this, _data));
    	if (beginEditReady())
    	{
			setTitle("Edit: " + _intel.getName());
			setTextView(R.id.title, _intel.getName());
			setTextView(R.id.comments, _intel.getComments());
			setupIndexedTypeSpinner(R.id.intel_status, _intel.getStatus(this));
			setTextListener(R.id.title, new DefaultTextWatcher() {
		    	@Override
		    	public void afterTextChanged(Editable s)
		    	{
		    		_intel.setName(s.toString());
		    	}
			});
			setTextListener(R.id.comments, new DefaultTextWatcher() {
			   @Override
			   public void afterTextChanged(Editable s)
			   {
				   _intel.setComments(s.toString());
			   }
			});
			setSpinnerListener(R.id.intel_status, new DefaultOnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
					IntelligenceStatus status = _intel.getStatus(EditIntelActivity.this);
					if (status != null)
					{
						status.moveToPosition(position);
						_intel.setStatusID(status.getID());
					}
				}
			});
			listenForChanges();
        }
    }
}
