package com.futureconcepts.ax.trinity.logs;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.futureconcepts.ax.model.data.Icon;
import com.futureconcepts.ax.model.data.Intelligence;
import com.futureconcepts.ax.model.data.IntelligenceStatus;
import com.futureconcepts.ax.trinity.Config;
import com.futureconcepts.ax.trinity.ModelListActivity;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.gqueue.MercurySettings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class IntelSummaryActivity extends ModelListActivity
{
	private static final String TAG = "IntelSummaryActivity";
	
	private Intelligence _intel;
	private MyAdapter _adapter;
	private MyObserver _observer;
		
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intel_summary);
        String currentIncidentId = MercurySettings.getCurrentIncidentId(this);
        if (currentIncidentId != null)
        {
        	setTitle("Intel: " + Config.getCurrentIncidentName(this));
			String whereClause = Intelligence.INCIDENT + "='" + currentIncidentId + "'";
			try
			{
				startManagingModel(_intel = Intelligence.queryWhere(this, whereClause));
				_observer = new MyObserver(new Handler());
				registerContentObserver(Intelligence.CONTENT_URI, true, _observer);
		        _adapter = new MyAdapter();
		        setListAdapter(_adapter);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				_intel = null;
			}
        }
        else
        {
        	onError("Please select an incident");
		}
    }
    
    @Override
    public void onDestroy()
    {
    	if (_observer != null)
    	{
    		unregisterContentObserver(_observer);
    	}
    	super.onDestroy();
    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		_intel.moveToPosition(position);
		String intelId = _intel.getID();
		Uri data = Uri.withAppendedPath(Intelligence.CONTENT_URI, _intel.getID());
		Log.d(TAG, intelId + " was selected");
		startActivity(new Intent(Intent.ACTION_VIEW, data));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.intel_summary_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_add:
			onMenuAdd();
			break;
		}
		return false;
	}

	private void onMenuAdd()
	{
		Uri data = Intelligence.createDefault(this, MercurySettings.getCurrentIncidentId(this));
		if (data != null)
		{
			Intent intent = new Intent(Intent.ACTION_INSERT, data);
			startActivity(intent);
		}
	}

	private void onError(String message)
	{
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("Select Incident");
		ab.setMessage(message);
		ab.setNeutralButton("OK", new OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				finish();
			}
		});
		AlertDialog dialog = ab.create();
		dialog.show();
	}

	public class MyAdapter extends ResourceCursorAdapter
	{
		private SimpleDateFormat _dateFormat;
		
		public MyAdapter()
		{
			super(IntelSummaryActivity.this, R.layout.log_list_item, _intel);
			_dateFormat = new SimpleDateFormat();
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor)
		{
			Intelligence intel = (Intelligence)cursor;
			try
			{
				String titleStr = intel.getName();
				if (titleStr != null)
				{
					TextView titleView = (TextView)view.findViewById(R.id.text1);
					titleView.setText(titleStr);
				}
				long time = intel.getEntryTime();
				if (time != 0)
				{
					String entryTimeFormatted = _dateFormat.format(new Date(time));
					TextView tview = (TextView)view.findViewById(R.id.date);
					tview.setText(entryTimeFormatted);
				}
				ImageView statusIcon = (ImageView)view.findViewById(R.id.status_icon);
				IntelligenceStatus status = intel.getStatus(IntelSummaryActivity.this);
				if (status != null)
				{
					Icon icon = status.getIcon(IntelSummaryActivity.this);
					if (icon != null)
					{
						byte[] bytes = icon.getImage();
						statusIcon.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
					}
					else
					{
						statusIcon.setImageResource(R.drawable.unknown);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public final class MyObserver extends ContentObserver
	{
		public MyObserver(Handler handler)
        {
	        super(handler);
        }
		
		@Override
		public void onChange(boolean selfChange)
		{
			if (_intel != null)
			{
				_intel.requery();
				if (_adapter != null)
				{
					_adapter.notifyDataSetChanged();
				}
			}
		}
	}
}
