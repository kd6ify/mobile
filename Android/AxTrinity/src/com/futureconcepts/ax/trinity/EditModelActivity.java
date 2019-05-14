package com.futureconcepts.ax.trinity;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Guid;
import com.futureconcepts.ax.model.data.IndexedType;
import com.futureconcepts.ax.model.data.PropertyChangedListener;
import com.futureconcepts.ax.trinity.logs.images.EntryImageObjectManager;

public class EditModelActivity extends ModelActivity implements PropertyChangedListener
{
	protected String _action;
	protected boolean addLocation = false;
	private View _btnDone;
	private boolean _dataUnchanged = true;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_frame);
        _action = getIntent().getAction();
        _btnDone = findViewById(R.id.btn_done);
        _btnDone.setEnabled(false);
        setOnClickListener(R.id.btn_done, new OnClickListener() {
			public void onClick(View v)
            {
				finish();
            }
        });
        setOnClickListener(R.id.btn_discard, new OnClickListener() {
			public void onClick(View v)
            {
				if (_action.equals(Intent.ACTION_INSERT))
				{
					getContentResolver().delete(_data, null, null);
				}
				_data = null;
				finish();
            }
        });
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	CheckIncidentNotNull.destroyActivityIfIncidentIsNull(this,true);
    }
        
    @Override
    public void onBackPressed() {
		if (_action.equals(Intent.ACTION_INSERT))
		{
			getContentResolver().delete(_data, null, null);
		}
		_data = null;
		finish();
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    	if (isFinishing())
    	{
    		if (_data == null)
    		{
    			Toast.makeText(this, "All changes discarded", Toast.LENGTH_LONG).show();
    		}
    		else
    		{
    			if (_btnDone.isEnabled() == false && _action.equals(Intent.ACTION_INSERT))
    			{
    				getContentResolver().delete(_data, null, null);
        			Toast.makeText(this, "Nothing saved--record deleted", Toast.LENGTH_LONG).show();
    			}
    			else
    			{
    				if (_model != null)
    				{
		    			try
		    			{
		    				if(addLocation){
			    				String ID = Guid.newGuid().toString();
			    				Address.insertAddress(getApplicationContext(), GPS.lastKnownLocation, ID, _action);
			    				_model.setModel("Address", ID);//add Address ID
							}		    				
//							getContentResolver().update(_data, _model.endEditAndUpload(_action), null, null);
//							//Added here to make sure journalEntry ID is saved first.							
//							new Thread(new Runnable() {
//							    public void run() {							    	
//							    	EntryImageObjectManager.insertMedia(getApplicationContext(), _model.getID(),_action );
//							    }
//						      }).run();		
		    				List<String> medias = EntryImageObjectManager.insertMedia(getApplicationContext(), _model.getID(),_action );
		    				getContentResolver().update(_data, _model.endEditAndUpload(_action), null, null);						
		    				EntryImageObjectManager.insertJournalEntryMedia(getApplicationContext(), medias, _model.getID(), _action);
		    				// send images to server;		
		    				EntryImageObjectManager.callService(getApplicationContext(),EntryImageObjectManager.SendImagesService,null);
		    			}
		    			catch (Exception exception)
		    			{
		    				//onError(exception.getMessage());
		    				exception.printStackTrace();
		    			}
    				}
    				else
    				{
    					onCommitContent();
    				}
    			}
    		}
    	}    	
    }
    
    protected boolean beginEditReady()
    {
    	if (_model.getCount() == 1)
    	{
    		_model.moveToFirst();
    		_model.beginEdit();
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }
    
    
    protected void onCommitContent()
    {
    }
    
    protected void listenForChanges()
    {
    	_model.setPropertyChangedListener(this);
    }
    
    protected void setEditContentView(int resid)
    {
    	getLayoutInflater().inflate(resid, (ViewGroup)findViewById(R.id.editors));
    }
    
    protected void setTextListener(int resid, TextWatcher watcher)
    {
    	TextView view = (TextView)findViewById(resid);
    	view.addTextChangedListener(watcher);
    }
    
    protected void setOnClickListener(int resid, OnClickListener listener)
    {
        View view = findViewById(resid);
        view.setOnClickListener(listener);
    }
    
    protected void setupIndexedTypeSpinner(int resid, IndexedType type)
    {
    	int position = type.getPosition();
    	IndexedTypeAdapter adapter = new IndexedTypeAdapter(this, type);
    	Spinner spinner = (Spinner)findViewById(resid);
        spinner.setAdapter(adapter);
        spinner.setSelection(position);
    }

    protected void setupIndexedTypeSpinner(int resid, IndexedType type, int res1, int res2)
    {
    	int position = type.getPosition();
    	IndexedTypeAdapter adapter = new IndexedTypeAdapter(this, type, res1, res2);
    	Spinner spinner = (Spinner)findViewById(resid);
        spinner.setAdapter(adapter);
        spinner.setSelection(position);
    }

    protected void setSpinnerListener(int resid, OnItemSelectedListener listener)
    {
    	Spinner spinner = (Spinner)findViewById(resid);
        spinner.setOnItemSelectedListener(listener);
    }
    	    
	protected void onError(String message)
	{
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("failed edit");
		if (message != null)
		{
			ab.setMessage(message);
		}
		ab.setNeutralButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				finish();
			}
		});
		AlertDialog dialog = ab.create();
		dialog.show();
	}

	// PropertyChangedListener
	
	@Override
	public void propertyChanged(String propertyName, Object value)
	{
		if (_dataUnchanged)
		{
			_btnDone.setEnabled(true);
			_dataUnchanged = false;
		}
	}
}
