package com.futureconcepts.ax.trinity.tasks;

import com.futureconcepts.ax.model.data.Tactic;
import com.futureconcepts.ax.model.dataset.TacticViewDataSet;
import com.futureconcepts.ax.sync.client.SyncServiceConnection;
import com.futureconcepts.ax.sync.client.SyncServiceConnection.Client;
import com.futureconcepts.ax.trinity.Config;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.ViewItemActivity;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public abstract class ViewTasksBaseActivity extends ViewItemActivity implements Client
{
	private static final String TAG = ViewTasksBaseActivity.class.getSimpleName();

	private Tactic _tactic;
	private MyAdapter _adapter;
	private MyObserver _observer;
	private SyncServiceConnection _syncServiceConnection;
	private Bundle _savedInstanceState;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        _savedInstanceState = savedInstanceState;
        setContentView(R.layout.view_tactics);
    	setTitle("Tasks: " + Config.getCurrentIncidentName(this));
        _observer = new MyObserver(new Handler());
        registerContentObserver(Tactic.CONTENT_URI, false, _observer);
        _observer.onChange(true);
        _syncServiceConnection = new SyncServiceConnection(this, this);
        _syncServiceConnection.connect();
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.view_tactics_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

    @Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_refresh:
			sync();
			break;
		}
		return false;
	}

	private void sync()
	{
		if (_syncServiceConnection != null)
		{
			_syncServiceConnection.syncDataset(TacticViewDataSet.class.getName());
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		_tactic.moveToPosition(position);
		Log.i(TAG, "onListItemClick " + _tactic.getName());
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(Tactic.CONTENT_URI, _tactic.getID())));
	}

	@Override
	public void onSyncServiceConnected()
	{
		if (_savedInstanceState == null)
		{
			sync();
		}
	}
	
	@Override
	public void onSyncServiceDisconnected()
	{
	}

	protected abstract Tactic queryTasks();
	
	public class MyAdapter extends ResourceCursorAdapter
	{
		public MyAdapter()
		{
			super(ViewTasksBaseActivity.this, R.layout.view_tactics_list_item, _tactic);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor)
		{
			Tactic tactic = (Tactic)cursor;
			((TextView)view.findViewById(R.id.name)).setText(tactic.getName());
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
			if (_tactic == null)
			{
				startManagingModel(_tactic = queryTasks());
		    	if (_tactic != null)
		    	{
			        _adapter = new MyAdapter();
			        setListAdapter(_adapter);
		    	}
			}
			else
			{
				_tactic.requery();
				if (_adapter != null)
				{
					_adapter.notifyDataSetChanged();
				}
			}
		}
	}
}