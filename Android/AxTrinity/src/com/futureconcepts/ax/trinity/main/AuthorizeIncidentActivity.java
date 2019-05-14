package com.futureconcepts.ax.trinity.main;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import com.futureconcepts.ax.model.data.Incident;
import com.futureconcepts.ax.trinity.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AuthorizeIncidentActivity extends Activity
{
	private static final String TAG = AuthorizeIncidentActivity.class.getSimpleName();
	private Incident _incident;
	
	@Override
	public void onCreate(Bundle savedState)
	{
		super.onCreate(savedState);
		setContentView(R.layout.authorize_incident);
		_incident = Incident.query(this, getIntent().getData());
		if (_incident != null && _incident.getCount() == 1)
		{
			TextView messageView = (TextView)findViewById(R.id.message);
			if (_incident.getName() != null)
			{
				messageView.setText(String.format("The incident '%s' is password-protected.  To access this incident, you must supply a password.", _incident.getName()));
			}
			final TextView passwordView = (TextView)findViewById(R.id.password);
			((Button)findViewById(R.id.authorize)).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					try
					{
						byte[] incidentPasswordHash = _incident.getPassword();
						byte[] hash = getHash(new String(_incident.getID().toLowerCase() + passwordView.getText().toString()).getBytes("UTF8"));
						if (Arrays.equals(hash, incidentPasswordHash))
						{
							setResult(Activity.RESULT_OK);
							finish();
						}
						else
						{
							throw new Exception("Invalid password");
						}
					}
					catch (Exception e)
					{
						onError(e.getMessage());
					}
				}
			});
			((Button)findViewById(R.id.cancel)).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					setResult(Activity.RESULT_CANCELED);
					finish();
				}
			});
		}
		else
		{
			setResult(Activity.RESULT_CANCELED);
			finish();
		}		
	}

	public byte[] getHash(byte[] password)
	{
		MessageDigest digest = null;
	    try
	    {
	        digest = MessageDigest.getInstance("SHA-256");
	    }
	    catch (NoSuchAlgorithmException e1)
	    {
	        // TODO Auto-generated catch block
	        e1.printStackTrace();
	    }
	    digest.reset();
	    return digest.digest(password);
    }
	
	private void onError(String message)
	{
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("authorization failed");
		ab.setMessage(message);
		ab.setNeutralButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
//				finish();
			}
		});
		AlertDialog dialog = ab.create();
		dialog.show();
	}
}
