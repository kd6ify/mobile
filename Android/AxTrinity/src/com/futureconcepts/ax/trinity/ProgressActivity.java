package com.futureconcepts.ax.trinity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class ProgressActivity extends Activity
{
	private ProgressDialog mProgressDialog;
	
	@Override 
	public void onDestroy()
	{
		dismissProgress();
		super.onDestroy();
	}
	
	public void showProgress(String title, String message)
	{
        mProgressDialog = ProgressDialog.show(this, title, message);
	}

	public void dismissProgress()
	{
		if (mProgressDialog != null)
		{
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}
		
	public void onError(String message)
	{
		if (mProgressDialog != null)
		{
			mProgressDialog.dismiss();
		}
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("Error getting data");
		ab.setMessage(message);
		ab.setNeutralButton("OK", new OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				finish();
			}
		});
		AlertDialog dialog = ab.create();
		dialog.show();
	}
}
