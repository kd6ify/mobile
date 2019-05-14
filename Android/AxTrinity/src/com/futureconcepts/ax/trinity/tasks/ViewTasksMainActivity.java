package com.futureconcepts.ax.trinity.tasks;

import java.util.Timer;
import java.util.TimerTask;

import com.futureconcepts.ax.model.data.Journal;
import com.futureconcepts.ax.model.data.Tactic;
import com.futureconcepts.ax.model.dataset.TacticViewDataSet;
import com.futureconcepts.ax.sync.client.SyncServiceConnection;
import com.futureconcepts.ax.sync.client.SyncServiceConnection.Client;
import com.futureconcepts.ax.trinity.CheckIncidentNotNull;
import com.futureconcepts.ax.trinity.Config;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.gqueue.MercurySettings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ViewTasksMainActivity extends FragmentActivity implements Client
{
	private static final String TAG = ViewTasksMainActivity.class.getSimpleName();

	private static final long RESYNC_INTERVAL = 1000 * 60; // 1 minute
	
	private SyncServiceConnection _syncServiceConnection;
	private Bundle _savedInstanceState;
	private Timer _resyncIntervalTimer;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        _savedInstanceState = savedInstanceState;
        setContentView(R.layout.view_tasks);
		setTitle("Tasks: " + Config.getCurrentIncidentName(this));
        if (MercurySettings.getCurrentIncidentId(this) != null)
        {
        	if (_savedInstanceState == null)
        	{
        		ToggleButton rdioButton = (ToggleButton)findViewById(R.id.btn_priority_tasks);
        		rdioButton.setChecked(true);
        	}
        	addInitialTasksFragment();
        	_syncServiceConnection = new SyncServiceConnection(this, this);
        	_syncServiceConnection.connect();
        }
        else
        {
        	onError("Please select an incident");
		}
    }
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	if (_syncServiceConnection != null)
    	{
    		_syncServiceConnection.disconnect();
    	}
    }

    @Override
    public void onResume()
    {
    	super.onResume();
    	CheckIncidentNotNull.destroyActivityIfIncidentIsNull(this,true);
        _resyncIntervalTimer = new Timer("ResyncIntervalTimer");
        _resyncIntervalTimer.schedule(new TimerTask()
        {
			@Override
			public void run()
			{
				sync();
			}
        }, 0, RESYNC_INTERVAL);
    }

    @Override
    public void onPause()
    {
    	super.onPause();
    	_resyncIntervalTimer.cancel();
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.view_tactics_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
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
	
	
	
	public void displayMenuOptions(View view)
	{
		final String[] options = {"Add Task","Add Priority Task"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle("Menu");
		builder.setItems(options,new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if("Add Task".equals(options[which]))
						{
							String incidentID = MercurySettings.getCurrentIncidentId(getContext());
							String period = MercurySettings.getCurrentOperationalPeriodId(getContext());
							Uri uri= Tactic.createDefaultTask(getContext(), incidentID,period);
							Log.e("","Task  uir: "+uri);
							if (uri != null)
							{
								Intent intent = new Intent(ViewTasksMainActivity.this,AddEditTaskActivity.class);
								intent.setAction(Intent.ACTION_INSERT);
								intent.setData(uri);
								startActivity(intent);
							}				
						}else if("Add Priority Task".equals(options[which]))
						{
							String incidentID = MercurySettings.getCurrentIncidentId(getContext());
							String period = MercurySettings.getCurrentOperationalPeriodId(getContext());
							Uri uri= Tactic.createDefaultPriorityTask(getContext(), incidentID,period);
							if (uri != null)
							{
								Intent intent = new Intent(ViewTasksMainActivity.this,AddEditPriorityTaskActivity.class);
								intent.setAction(Intent.ACTION_INSERT);
								intent.setData(uri);
								startActivity(intent);
							}
						}
						dialog.dismiss();
					}
				});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {					
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	private Context getContext()
	{
		return this;
	}
//	
//	private void onMenuAddTask()
//	{
//		//try
//		//{
//			//Uri uri = null;//Tactic.queryTasks(context, operationalPeriodID)//Journal.createDefault(this, MercurySettings.getCurrentIncidentId(this));
//			//if (uri != null)
//			//{
//				Intent intent = new Intent(this,AddEditTaskActivity.class);
//				startActivity(intent);
//			//}
//		//}
//		//catch (Exception e)
//		//{
//		//	e.printStackTrace();
//		//}
//	}
    
	private void sync()
	{
		if (_syncServiceConnection != null)
		{
			_syncServiceConnection.syncDataset(TacticViewDataSet.class.getName());
		}
	}
	
	public void goBack(View view)
	{
		finish();
	}
	public void refresh(View view)
	{
		sync();
	}
	
    private void addInitialTasksFragment()
    {
        FragmentManager fragMgr = getSupportFragmentManager();
        FragmentTransaction xact = fragMgr.beginTransaction();
        if (((ToggleButton)findViewById(R.id.btn_priority_tasks)).isChecked())
        {
            Fragment fragment = fragMgr.findFragmentByTag(PriorityTasksFragment.TAG);
            if (fragment == null)
            {
            	xact.add(R.id.task_list, PriorityTasksFragment.newInstance(), PriorityTasksFragment.TAG);
            }
            else
            {
            	xact.replace(R.id.task_list, PriorityTasksFragment.newInstance(), PriorityTasksFragment.TAG);
            }
        }
        else
        {
        	Fragment fragment = fragMgr.findFragmentByTag(TasksFragment.TAG);
        	if (fragment == null)
        	{
        		xact.add(R.id.task_list, TasksFragment.newInstance(), TasksFragment.TAG);
        	}
        	else
        	{
        		xact.replace(R.id.task_list, TasksFragment.newInstance(), TasksFragment.TAG);
        	}
        }
        xact.commit();
    }

    public void onPriorityTasksClicked(View view)
    {
    	Log.d(TAG, "onPriorityTasksClicked");
    	ToggleButton btnPriorityTasks = (ToggleButton)findViewById(R.id.btn_priority_tasks);
    	ToggleButton btnTasks = (ToggleButton)findViewById(R.id.btn_tasks);
    	btnPriorityTasks.setBackgroundResource(R.drawable.ptask_bar_down);
    	btnTasks.setBackgroundResource(R.drawable.task_bar);
        FragmentManager fragMgr = getSupportFragmentManager();
        FragmentTransaction xact = fragMgr.beginTransaction();
        Fragment fragment = fragMgr.findFragmentByTag(PriorityTasksFragment.TAG);
        if (fragment == null)
        {
        	xact.replace(R.id.task_list, PriorityTasksFragment.newInstance(), PriorityTasksFragment.TAG);
        }
        else
        {
        	xact.replace(R.id.task_list, fragment);
        }
        xact.commit();
    }

    public void onTasksClicked(View view)
    {
    	Log.d(TAG, "onTasksClicked");
    	ToggleButton btnPriorityTasks = (ToggleButton)findViewById(R.id.btn_priority_tasks);
    	ToggleButton btnTasks = (ToggleButton)findViewById(R.id.btn_tasks);
    	btnPriorityTasks.setBackgroundResource(R.drawable.ptask_bar);
    	btnTasks.setBackgroundResource(R.drawable.task_bar_down);
        FragmentManager fragMgr = getSupportFragmentManager();
        FragmentTransaction xact = fragMgr.beginTransaction();
        Fragment fragment = fragMgr.findFragmentByTag(TasksFragment.TAG);
        if (fragment == null)
        {
        	xact.replace(R.id.task_list, TasksFragment.newInstance(), TasksFragment.TAG);
        }
        else
        {
        	xact.replace(R.id.task_list, fragment);
        }
        xact.commit();
    }

    private void onError(String message)
	{
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("Select Incident");
		ab.setMessage(message);
		ab.setCancelable(false);
		ab.setNeutralButton("OK", new OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				finish();
			}
		});
		AlertDialog dialog = ab.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
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
		// TODO Auto-generated method stub
		
	}
}
