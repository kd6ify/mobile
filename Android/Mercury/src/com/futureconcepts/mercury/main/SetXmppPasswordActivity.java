package com.futureconcepts.mercury.main;

import com.futureconcepts.mercury.Config;
import com.futureconcepts.mercury.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SetXmppPasswordActivity extends Activity
{
	private static final String TAG = SetXmppPasswordActivity.class.getSimpleName();
	private Config _config;
	
	@Override
	public void onCreate(Bundle savedState)
	{
		super.onCreate(savedState);
		_config = Config.getInstance(this);
		//String pwd = _config.getXmppPassword();
		//Log.d(TAG, pwd);
//		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.set_password);
		final TextView passwordView = (TextView)findViewById(R.id.password);
		TextView usernameView = (TextView)findViewById(R.id.username);
		setProgressBarIndeterminate(true);
		((Button)findViewById(R.id.save)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try
				{
					_config.setXmppPassword(passwordView.getText().toString());
					finish();
				}
				catch (Exception e)
				{
					onError(e.getMessage());
				}
			}
		});
		((Button)findViewById(R.id.cancel)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void onError(String message)
	{
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("Login failed");
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
