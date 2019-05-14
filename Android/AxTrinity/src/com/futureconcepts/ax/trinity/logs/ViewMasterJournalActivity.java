package com.futureconcepts.ax.trinity.logs;

import java.util.Timer;
import java.util.TimerTask;

import com.futureconcepts.ax.model.data.Icon;
import com.futureconcepts.ax.model.data.Journal;
import com.futureconcepts.ax.model.data.JournalEntry;
import com.futureconcepts.ax.model.data.JournalEntryMedia;
import com.futureconcepts.ax.model.data.JournalEntryPriorityBinding;
import com.futureconcepts.ax.model.data.SourceType;
import com.futureconcepts.ax.model.dataset.IncidentJournalDataSet;
import com.futureconcepts.ax.sync.client.SyncServiceConnection;
import com.futureconcepts.ax.sync.client.SyncServiceConnection.Client;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.ViewItemActivity;
import com.futureconcepts.ax.trinity.widget.AlternatingColorCursorAdapter;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch.EditTextWithSearchInterface;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
//import android.util.Log;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class ViewMasterJournalActivity extends ViewItemActivity implements Client, EditTextWithSearchInterface
{
	private static final long RESYNC_INTERVAL = 1000 * 60; // 1 minute
	
	private JournalEntry _journalEntry;
	private MyObserver _observer;
	private MyAdapter _adapter;
	private SyncServiceConnection _syncServiceConnection;
	private Bundle _savedInstanceState;
	private Timer _resyncIntervalTimer;
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_master_log);
        _savedInstanceState = savedInstanceState;
        try
        {
	        	setTitle("Master Log");
	        	_journalEntry = JournalEntry.queryMaster(this);
	        	//startManagingModel(_journalEntry);
				_adapter = new MyAdapter();
		        setListAdapter(_adapter);
		        _adapter.setFilterQueryProvider(new FilterQueryProvider() {						
					@Override
					public Cursor runQuery(CharSequence constraint) {
						// TODO Auto-generated method stub
						String text = constraint.toString();
						if(text==null || text.length()==0){
							_journalEntry = JournalEntry.queryMaster(ViewMasterJournalActivity.this);
						}else{
							String whereClause = JournalEntry.TEXT+" Like '%"+text+"%'";
							_journalEntry = JournalEntry.queryWhere(ViewMasterJournalActivity.this, whereClause);
						}
						return _journalEntry;
					}
				});
		        addSearchListener();
		        _observer = new MyObserver(new Handler());
		        registerContentObserver(JournalEntry.CONTENT_URI, true, _observer);
//		        setDefaultOptionsMenu(true);
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }
        _syncServiceConnection = new SyncServiceConnection(this, this);
        _syncServiceConnection.connect();
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
    }

    @Override
    public void onPause()
    {
    	super.onPause();
    	_resyncIntervalTimer.cancel();
    }
    
    @Override
    public void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	if(_journalEntry !=null && !_journalEntry.isClosed())
    		_journalEntry.close();
    	removeSearchListener();
    	
    }
    
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		try
		{
			_journalEntry.moveToPosition(position);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.withAppendedPath(JournalEntry.CONTENT_URI, _journalEntry.getID()));
			startActivity(intent);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.view_master_journal_options_menu, menu);
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

	public void goBack(View view)
	{
		finish();
	}
	public void refresh(View view)
	{
		sync();
	}
	
	public void sync()
	{
		if (_syncServiceConnection != null)
		{
			_syncServiceConnection.syncDataset(IncidentJournalDataSet.class.getName());
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
	
	
	private class MyAdapter extends ResourceCursorAdapter
	{
		public MyAdapter()
		{
			super(ViewMasterJournalActivity.this, R.layout.master_log_list_item, _journalEntry);
		}

		@Override
		public void bindView(View view, Context context, Cursor c)
		{
//			super.bindView(view, context, c);
			try
			{
				JournalEntry journalEntry = (JournalEntry)c;
				setTextView(view, R.id.date, getFormattedLocalTime(journalEntry.getJournalTime(), "no entry time"));
//				SourceType sourceType = journalEntry.getSourceType(ViewMasterJournalActivity.this);
//				if (sourceType != null)
//				{
//					setTextView(view, R.id.source, sourceType.getName());
//				}
				Journal journal = journalEntry.getJournal(ViewMasterJournalActivity.this);
				if (journal != null)
				{
					setTextView(view, R.id.log_name,journal.getName());
				}
				setTextView(view, R.id.text1, journalEntry.getText());
				((TextView)view.findViewById(R.id.text1)).setTextColor(JournalEntryPriorityBinding.getPriorityColor(null,journalEntry.getPriority()));
				JournalEntryMedia _journalEntryMedia;
				startManagingCursor(_journalEntryMedia = JournalEntryMedia.queryJournalEntryMedia(getApplicationContext(), journalEntry.getID(),JournalEntryMedia.JOURNAL_ENTRY));
			    ((TextView)view.findViewById(R.id.num_images)).setText(""+_journalEntryMedia.getCount());				
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private void setTextView(View view, int resid, String text)
	{
		TextView textView = (TextView)view.findViewById(resid);
		if (textView != null)
		{
			textView.setText(text);
		}
	}
	
	private void setTextViewUnderlined(View view, int resid, String text)
	{
		TextView textView = (TextView)view.findViewById(resid);
		if (textView != null)
		{
			SpannableString content = new SpannableString(text);
			content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
			textView.setText(content);
		}
	}
	
	private void setImageViewIcon(View view, int resid, Icon icon)
	{
		ImageView imageView = (ImageView)view.findViewById(resid);
		if (icon != null)
		{
			byte[] bytes = icon.getImage();
			if (bytes != null)
			{
				imageView.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
			}
		}
	}
	
	private final class MyObserver extends ContentObserver
	{
		public MyObserver(Handler handler)
        {
	        super(handler);
        }
		
		@Override
		public void onChange(boolean selfChange)
		{
			if (_journalEntry != null)
			{
				try
				{
					_journalEntry.requery();
					if (_adapter != null)
					{
						_adapter.notifyDataSetChanged();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public void addSearchListener(){
		((EditTextWithSearch)findViewById(R.id.master_log_search)).addSearchListener(this);
	}

	public void removeSearchListener(){
		((EditTextWithSearch)findViewById(R.id.master_log_search)).removeSearchListener(this);
	}
	
	@Override
	public void handleSearch(String text) {
		// TODO Auto-generated method
		if(_adapter!=null)
			_adapter.getFilter().filter(text);
	}
}
