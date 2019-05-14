package com.futureconcepts.ax.trinity.triage;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Gender;
import com.futureconcepts.ax.model.data.Person;
import com.futureconcepts.ax.model.data.Triage;
import com.futureconcepts.ax.model.data.TriageColor;
import com.futureconcepts.ax.model.data.TriageStatus;
import com.futureconcepts.ax.trinity.DefaultOnItemSelectedListener;
import com.futureconcepts.ax.trinity.DefaultTextWatcher;
import com.futureconcepts.ax.trinity.EditModelActivity;
import com.futureconcepts.ax.trinity.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.AdapterView;

public class EditCasualtyActivity extends EditModelActivity
{
	private String _action;
	private Triage _triage;
	private Triage.Content _content;
	private Boolean _personChanged = false;
	private Uri _personUri;
	private Person.Content _personContent;
	private Boolean _addressChanged = false;
	private Uri _addressUri;
	private Address.Content _addressContent;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setEditContentView(R.layout.edit_triage);
        _action = getIntent().getAction();
        _triage = Triage.query(this, getData());
        if (_triage != null)
        {
	    	startManagingCursor(_triage);
	    	if (_triage.getCount() == 1)
	    	{
	    		_content = new Triage.Content(this, _triage);
	    		Person person = _triage.getPerson(this);
	    		if (person != null && person.getCount() == 1)
	    		{
	    			_personUri = Uri.withAppendedPath(Person.CONTENT_URI, person.getID());
	    			_personContent = new Person.Content(this, person);
	    		}
	    		else
	    		{
	    			_personContent = new Person.Content(this);
	    			_personContent.setLast("Doe");
	    			_personUri = _personContent.insert(Person.CONTENT_URI);
	    			_personChanged = true;
	    			_content.setPersonID(_personUri);
	    		}
	    		Address address = _triage.getAddress(this);
	    		if (address != null && address.getCount() == 1)
	    		{
	    			_addressUri = Uri.withAppendedPath(Address.CONTENT_URI, address.getID());
	    			_addressContent = new Address.Content(this, address);
	    		}
	    		initializeViewsFromModel();
	            setupColorSpinner();
	            setupStatusSpinner();
	            setupGenderSpinner();
	            setupTrackingIDListener();
	            setTitle("Edit Casualty");
	    		_content.setPropertyChangedListener(this);
	    	}
        }
    }
    @Override
    protected void onCommitContent()
    {
		try
		{
			if (_personContent != null && (_personChanged == true || _action.equals(Intent.ACTION_INSERT)))
			{
				_personContent.prepareForUpload(_action);
				_personContent.update(_personUri);
			}
			if (_addressContent != null && (_addressChanged == true || _action.equals(Intent.ACTION_INSERT)))
			{
				_addressContent.prepareForUpload(_action);
				_addressContent.update(_addressUri);
			}
			_content.prepareForUpload(_action);
			_content.update(getData());
		}
		catch (Exception exception)
		{
			onError(exception.getMessage());
		}
    }
    
    private void initializeViewsFromModel()
    {
        setTextView(R.id.text1, _triage.getTrackingID());
    }
    private void setupColorSpinner()
    {
    	TriageColor color = _triage.getColor(this);
    	if (color != null)
    	{
    		color.moveToPosition(_triage.getColorID());
    	}
        setupIndexedTypeSpinner(R.id.color, _triage.getColor(this), R.layout.triage_spinner, R.layout.triage_spinner_dropdown);
		setSpinnerListener(R.id.color, new DefaultOnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
			{
				TriageColor color = _triage.getColor(EditCasualtyActivity.this);
				color.moveToPosition(position);
				_content.setColorID(color.getID());
			}
        });
    }
    private void setupStatusSpinner()
    {
    	TriageStatus status = _triage.getStatus(this);
    	if (status != null)
    	{
    		status.moveToPosition(_triage.getStatusID());
    	}
    	setupIndexedTypeSpinner(R.id.status, _triage.getStatus(this), R.layout.triage_spinner, R.layout.triage_spinner_dropdown);
		setSpinnerListener(R.id.status, new DefaultOnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
			{
				TriageStatus status = _triage.getStatus(EditCasualtyActivity.this);
				status.moveToPosition(position);
				_content.setStatusID(status.getID());
			}
        });
    }
    private void setupGenderSpinner()
    {
    	final Gender gender = Gender.query(this);
    	if (gender != null)
    	{
    		Person person = _triage.getPerson(this);
    		if (person != null && person.getCount() == 1)
    		{
    			person.moveToFirst();
    			gender.moveToPosition(person.getGenderID());
    		}
    		else
    		{
    			gender.moveToPosition(0);
    		}
			setupIndexedTypeSpinner(R.id.gender, gender, R.layout.triage_spinner, R.layout.triage_spinner_dropdown);
			setSpinnerListener(R.id.gender, new DefaultOnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
				{
					gender.moveToPosition(position);
					_personContent.setGenderID(gender.getID());
					_personChanged = true;
				}
	        });
    	}
    }
    private void setupTrackingIDListener()
    {
        setTextListener(R.id.text1, new DefaultTextWatcher() {
			@Override
			public void afterTextChanged(Editable s)
			{
				_content.setTrackingID(s.toString());
			}
		});
    }
}
