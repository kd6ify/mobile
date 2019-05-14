package com.futureconcepts.ax.trinity.tasks;

import com.futureconcepts.ax.model.dataset.TacticViewDataSet;
import com.futureconcepts.ax.sync.client.SyncServiceConnection;
import com.futureconcepts.ax.sync.client.SyncServiceConnection.Client;

import android.app.TabActivity;
import android.os.Bundle;
import android.widget.TabHost;
import android.content.Intent;

/**
 * An example of tab content that launches an activity via {@link android.widget.TabHost.TabSpec#setContent(android.content.Intent)}
 */
public class TasksTabActivity extends TabActivity implements Client
{
	private SyncServiceConnection _syncServiceConnection;
	private Bundle _savedInstanceState;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        _savedInstanceState = savedInstanceState;

        final TabHost tabHost = getTabHost();

        tabHost.addTab(tabHost.newTabSpec("tab1")
                .setIndicator("Priority Tasks", null)
                .setContent(new Intent(this, ViewPriorityTasksActivity.class)));

        tabHost.addTab(tabHost.newTabSpec("tab2")
                .setIndicator("Non-Priority Tasks", null)
                .setContent(new Intent(this, ViewTasksActivity.class)));

        _syncServiceConnection = new SyncServiceConnection(this, this);
        _syncServiceConnection.connect();
        
        // This tab sets the intent flag so that it is recreated each time
        // the tab is clicked.
//        tabHost.addTab(tabHost.newTabSpec("tab3")
  //              .setIndicator("destroy")
    //            .setContent(new Intent(this, Controls2.class)
      //                  .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
    }

	@Override
	public void onSyncServiceConnected()
	{
		if (_savedInstanceState == null)
		{
			_syncServiceConnection.syncDataset(TacticViewDataSet.class.getName());
		}
	}

	@Override
	public void onSyncServiceDisconnected()
	{
		// TODO Auto-generated method stub
		
	}
    
}
