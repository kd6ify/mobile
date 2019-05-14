package com.futureconcepts.ax.trinity.logs;

import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.futureconcepts.ax.model.data.Journal;
import com.futureconcepts.ax.model.data.JournalEntry;
import com.futureconcepts.ax.model.data.JournalEntryMedia;
import com.futureconcepts.ax.model.dataset.IncidentJournalDataSet;
import com.futureconcepts.ax.sync.client.SyncServiceConnection;
import com.futureconcepts.ax.sync.client.SyncServiceConnection.Client;
import com.futureconcepts.ax.trinity.Config;
import com.futureconcepts.ax.trinity.ModelListActivity;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.logs.images.EntryImageObjectManager;
import com.futureconcepts.ax.trinity.logs.images.SendImageToServer;
import com.futureconcepts.ax.trinity.logs.images.TextUpdater;
import com.futureconcepts.ax.trinity.widget.AlternatingColorCursorAdapter;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch.EditTextWithSearchInterface;
import com.futureconcepts.gqueue.MercurySettings;
import com.futureconcepts.localmedia.operations.MediaHandler;

public class JournalListActivity extends ModelListActivity implements Client,
	TextUpdater.TextUpdaterGetTextListener, EditTextWithSearchInterface
{
	private static final String TAG = JournalListActivity.class.getSimpleName();
	
	private static final long RESYNC_INTERVAL = 1000 * 60; // 1 minute	

	private String _incidentID;
	private Journal _journal;
	private MyAdapter _adapter;
	private MyObserver _contentObserver;
	private SyncServiceConnection _syncServiceConnection;
	private Bundle _savedInstanceState;
	private Timer _resyncIntervalTimer;	
	private TextUpdater textUpdater;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.journal_list);
        _savedInstanceState = savedInstanceState;
        _incidentID = MercurySettings.getCurrentIncidentId(this);
        textUpdater = new TextUpdater(this);
        if (_incidentID != null)
        {
        	setTitle("Log Categories: " + Config.getCurrentIncidentName(this));
			_contentObserver = new MyObserver(new Handler());
			registerContentObserver(Journal.CONTENT_URI, true, _contentObserver);
			_contentObserver.onChange(true);
			addSearchListener();
        }
        else
        {
        	onError("Please select an incident");
		}
        _syncServiceConnection = new SyncServiceConnection(this, this);
        _syncServiceConnection.connect(); 
        MediaHandler.updateDataBase(this);//check if we need to update the local database     
    }

    @Override
    public void onResume()
    {
    	super.onResume();
        _resyncIntervalTimer = new Timer("ResyncIntervalTimer");
        _resyncIntervalTimer.schedule(new TimerTask()
        {
			@Override
			public void run()
			{
				sync();
			}
        }, 0, RESYNC_INTERVAL);
        //service for send images to server
        verifyMedia();
    }
    
    public void verifyMedia()
    {    
    	new Thread(new Runnable() {
    		public void run() {  	        	
    	     	if(MediaHandler.getAllMedia(getApplicationContext(), MediaHandler.ACTION_UPLOAD).size()>0 && !SendImageToServer.isMyServiceRunning(getApplicationContext()))
    	       	{
    	      		runOnUiThread(new Runnable() {
    	       		    public void run() {
    	       		    	((LinearLayout)findViewById(R.id.resendImages)).setVisibility(View.VISIBLE);
    	       		    }
    	       		});    	       		
    	       	}else
    	       	{
    	       		runOnUiThread(new Runnable() {
    	       		    public void run() {
    	       		    	((LinearLayout)findViewById(R.id.resendImages)).setVisibility(View.GONE);
    	       		    }
    	       		});    	       		
    	       	}    	        	
    	       }
    	 }).start();    	
    }
    
    public void sendImages(View view)
    { 
    	EntryImageObjectManager.callService(getApplicationContext(),EntryImageObjectManager.SendImagesService,null);  
    	((LinearLayout)findViewById(R.id.resendImages)).setVisibility(View.GONE);
    }

    @Override
    public void onPause()
    {
    	super.onPause();    	
    	_resyncIntervalTimer.cancel();
    }
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	textUpdater.close();
    	_syncServiceConnection.disconnect();
    	removeSearchListener();
    	if(_journal!=null && !_journal.isClosed())
    	{
    		_journal.close();
    		_journal = null;
    	}
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.log_summary_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
    	boolean tableExists = false;
    	// don't allow user to add log if table doesn't exist yet
    	if (_journal != null)
    	{
    		tableExists = true;
    	}
		menu.findItem(R.id.menu_add_log).setVisible(tableExists);
    	return true;
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_add_log:
			onMenuAddLog();
			break;
		case R.id.menu_show_master:
			onMenuShowMaster();
			break;
		case R.id.menu_refresh:
			sync();
			break;
		}
		return false;
	}
	
	public void goBack(View view)
	{
		finish();
	}

	public void refresh(View view)
	{
		sync();
	}
	public void displayMenuOptions(View view)
	{
		final String[] options = {"Add Log Category","Master Log"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle("Menu");
		builder.setItems(options,new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if("Add Log Category".equals(options[which]))
						{
							if (_journal != null)
					    	{
								onMenuAddLog();
					    	}else 
					    	{
					    		Toast.makeText(getApplicationContext(),
					    				"Unable to add new Log Category",
					    				Toast.LENGTH_SHORT).show();
					    	}
							
						}else if("Master Log".equals(options[which]))
						{
							onMenuShowMaster();
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
	
	
	protected void sync()
	{
		if (_syncServiceConnection != null)
		{
			_syncServiceConnection.syncDataset(IncidentJournalDataSet.class.getName());
		}
	}
	
	private void onMenuAddLog()
	{
		try
		{
			Uri uri = Journal.createDefault(this, MercurySettings.getCurrentIncidentId(this));
			if (uri != null)
			{
				Intent intent = new Intent(Intent.ACTION_INSERT, uri);
				startActivity(intent);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void onMenuShowMaster()
	{
		Intent intent = new Intent(Intent.ACTION_VIEW, Journal.CONTENT_URI);
		startActivity(intent);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		if (_journal != null)
		{
			_journal.moveToPosition(position);
			//Log.i(TAG, "onListItemClick " + _journal.getName());
			String journalId = _journal.getID();//((Journal)((getListView().getItemAtPosition(position)))).getID();
			if (journalId != null)
			{
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(Journal.CONTENT_URI, journalId));
				startActivity(intent);
			}
		}
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
	
	private class MyAdapter extends AlternatingColorCursorAdapter
	{
	    public MyAdapter()
		{
			super(JournalListActivity.this, R.layout.log_list_item, _journal);
			DateTime.setContext(JournalListActivity.this);
			DateTimeZone.setProvider(null);
		}

		@Override
		public void bindView(View view,Context context, Cursor c)
		{
			super.bindView(view, context, c);
			final Journal journal = (Journal)c;
			try
			{
				view.setTag(journal.getID());
				((TextView)view.findViewById(R.id.title)).setText("Category Name");
				((TextView)view.findViewById(R.id.date)).setText(getFormattedLocalTime(journal.getTime(), "no start"));
				((TextView)view.findViewById(R.id.text1)).setText(journal.getName());
				((TextView)view.findViewById(R.id.num_images)).setTag(journal.getID());
				textUpdater.displayText(journal.getID(), ((TextView)view.findViewById(R.id.num_images)), "");		  
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
			if (_journal != null)
			{
				try
				{
					_journal.requery();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				try
				{
					String whereClause = Journal.INCIDENT + "= '" + _incidentID + "'";
					_journal = Journal.queryWhere(JournalListActivity.this, whereClause);
			        _adapter = new MyAdapter();
			        setListAdapter(_adapter);
			        _adapter.setFilterQueryProvider(new FilterQueryProvider() {						
						@Override
						public Cursor runQuery(CharSequence constraint) {
							// TODO Auto-generated method stub
							String text = constraint.toString();
							if(text==null || text.length()==0){
								String whereClause = Journal.INCIDENT + "= '" + _incidentID + "'";
								_journal = Journal.queryWhere(JournalListActivity.this, whereClause);
							}else{
								String whereClause = Journal.INCIDENT + "= '" + _incidentID + "' AND "+
											Journal.NAME+" Like '%"+text+"%'";
								_journal = Journal.queryWhere(JournalListActivity.this, whereClause);
							}
							return _journal;
						}
					});
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			if (_adapter != null)
			{
				_adapter.notifyDataSetChanged();
			}
		}
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

	@Override
	public String getText(String journalID) {
		// TODO Auto-generated method stub
		int count = 0;
		JournalEntry _journalEntry = JournalEntry.queryJournal(getApplicationContext(),journalID);
		if(_journalEntry!=null)
			count =_journalEntry.getCount();	
		if(_journalEntry != null)
			{
				_journalEntry.close();
				_journalEntry = null;
			}
		return count+"";
	}
		
	public void addSearchListener(){
		((EditTextWithSearch)findViewById(R.id.journal_search)).addSearchListener(this);
	}

	public void removeSearchListener(){
		((EditTextWithSearch)findViewById(R.id.journal_search)).removeSearchListener(this);
	}
	
	@Override
	public void handleSearch(String text) {
		// TODO Auto-generated method stub
		if(_adapter!=null)
			_adapter.getFilter().filter(text);
		
	}
}