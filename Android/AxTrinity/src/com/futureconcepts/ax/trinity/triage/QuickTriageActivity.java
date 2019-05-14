package com.futureconcepts.ax.trinity.triage;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Person;
import com.futureconcepts.ax.model.data.Triage;
import com.futureconcepts.ax.model.data.TriageColor;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.app.EditModelFragmentActivity;

import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class QuickTriageActivity extends EditModelFragmentActivity
{
	private Triage _triage;
	private Triage.Content _content;
	private Uri _personUri;
	private Person.Content _personContent;
	private Uri _addressUri;
	private Address.Content _addressContent;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setEditContentView(R.layout.quick_triage);
        setButtonColor(R.id.btn_green, 0xFF00FF00);
        setButtonColor(R.id.btn_yellow, 0xFFFFFF00);
        setButtonColor(R.id.btn_red, 0xFEFE0000);
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
	    		Address address = _triage.getAddress(this);
	    		if (address != null && address.getCount() == 1)
	    		{
	    			_addressUri = Uri.withAppendedPath(Address.CONTENT_URI, address.getID());
	    			_addressContent = new Address.Content(this, address);
	    		}
	    		initializeViewsFromModel();
	            setTitle("Quick Triage");
	    		_content.setPropertyChangedListener(this);
	    	}
        }
    }
    private void initializeViewsFromModel()
    {
    	TextView text1 = (TextView)findViewById(R.id.text1);
    	text1.setText(_triage.getTrackingID());
    }
    private void setButtonColor(int resid, int color)
    {
    	Button button = (Button)findViewById(resid);
    	button.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }
    public void onGreenClicked(View view)
    {
    	_content.setColorID(TriageColor.GREEN);
    	showInsertedDialogAndFinish();
    }
    public void onYellowClicked(View view)
    {
    	_content.setColorID(TriageColor.YELLOW);
    	showInsertedDialogAndFinish();
    }
    public void onRedClicked(View view)
    {
    	_content.setColorID(TriageColor.RED);
    	showInsertedDialogAndFinish();
    }
    public void onBlackClicked(View view)
    {
    	_content.setColorID(TriageColor.BLACK);
    	showInsertedDialogAndFinish();
    }
    
    @Override
    public void onCommitContent()
    {
		try
		{
			if (_personContent != null)
			{
				_personContent.prepareForUpload(getAction());
				_personContent.update(_personUri);
			}
			if (_addressContent != null)
			{
				_addressContent.prepareForUpload(getAction());
				_addressContent.update(_addressUri);
			}
			_content.prepareForUpload(getAction());
			try
			{
				_content.update(getData());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			onError(e.getMessage());
		}
    }
    
    private void showInsertedDialogAndFinish()
    {
    	Toast toast = Toast.makeText(this, "Triage successfully inserted", Toast.LENGTH_LONG);
    	toast.setGravity(Gravity.CENTER, 0, 0);
    	toast.show();
		finish();
    }
}
