package com.futureconcepts.ax.trinity.tasks;

import com.futureconcepts.ax.model.data.Objective;
import com.futureconcepts.ax.trinity.Config;
import com.futureconcepts.ax.trinity.ModelListActivity;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.gqueue.MercurySettings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class ViewObjectivesActivity extends ModelListActivity
{
	private static final String TAG = "ViewObjectivesActivity";
	
	private Objective _objective;
	private MyAdapter _adapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_objectives);
        setData(Objective.CONTENT_URI);
        String operationalPeriodID = MercurySettings.getCurrentOperationalPeriodId(this);
        if (operationalPeriodID != null)
        {
        	setTitle("Objectives: " + Config.getCurrentIncidentName(this));
			String whereClause = Objective.OPERATIONAL_PERIOD + "='" + operationalPeriodID + "'";
			startManagingModel(_objective = Objective.queryWhere(this, whereClause));
	        _adapter = new MyAdapter();
	        setListAdapter(_adapter);
			registerContentObserver(_adapter);
        }
        else
        {
        	onError("Please select an operational period");
		}
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.objectives_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		}
		return false;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		_objective.moveToPosition(position);
		Log.i(TAG, "onListItemClick " + _objective.getName());
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(getData(), _objective.getID()));
		startActivity(intent);
	}

	private void onError(String message)
	{
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("Select Operational Period");
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
		public MyAdapter()
		{
			super(ViewObjectivesActivity.this, R.layout.objective_list_item, _objective);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor)
		{
			Objective objective = (Objective)cursor;
			((TextView)view.findViewById(R.id.name)).setText(objective.getName());
		}
	}
}