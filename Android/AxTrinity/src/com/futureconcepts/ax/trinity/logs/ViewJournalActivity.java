package com.futureconcepts.ax.trinity.logs;

import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.futureconcepts.ax.model.data.Icon;
import com.futureconcepts.ax.model.data.Journal;
import com.futureconcepts.ax.model.data.JournalEntry;
import com.futureconcepts.ax.model.data.JournalEntryMedia;
import com.futureconcepts.ax.model.data.JournalEntryPriorityBinding;
import com.futureconcepts.ax.model.data.JournalStatus;
import com.futureconcepts.ax.model.dataset.IncidentJournalDataSet;
import com.futureconcepts.ax.sync.client.SyncServiceConnection;
import com.futureconcepts.ax.sync.client.SyncServiceConnection.Client;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.ViewItemActivity;
import com.futureconcepts.ax.trinity.logs.images.EntryImageObjectManager;
import com.futureconcepts.ax.trinity.logs.images.SendImageToServer;
import com.futureconcepts.ax.trinity.logs.images.TextUpdater;
import com.futureconcepts.ax.trinity.widget.AlternatingColorCursorAdapter;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch.EditTextWithSearchInterface;
import com.futureconcepts.localmedia.operations.MediaHandler;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ViewJournalActivity extends ViewItemActivity implements Client,
	TextUpdater.TextUpdaterGetTextListener,EditTextWithSearchInterface
{
	private static final long RESYNC_INTERVAL = 1000 * 60; // 1 minute	
	
	private Journal _journal;
	private JournalEntry _journalEntry;
	private MyObserver _observer;
	private MyAdapter _adapter;
	private SyncServiceConnection _syncServiceConnection;
	private Bundle _savedInstanceState;
	private Timer _resyncIntervalTimer;	
	private TextUpdater textUpdater;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_journal);
        _savedInstanceState = savedInstanceState;
        textUpdater= new TextUpdater(this);
        try
        {
        	startManagingModel(_journal = Journal.query(this, getData()));
        	if (moveToFirstIfOneRow())
        	{
	        	setTitle(_journal.getName());
	        	_journalEntry = JournalEntry.queryJournal(this, _journal.getID());
				_adapter = new MyAdapter();
		        setListAdapter(_adapter);
		        _adapter.setFilterQueryProvider(new FilterQueryProvider() {					
					@Override
					public Cursor runQuery(CharSequence constraint) {
						String text = constraint.toString();
						if(text==null || text.length()==0){
							_journalEntry = JournalEntry.queryJournal(ViewJournalActivity.this, _journal.getID());
						}else{
							String where= JournalEntry.JOURNAL+"='"+_journal.getID()+"' AND "+
									JournalEntry.TEXT+" Like '%"+text+"%'";
							_journalEntry = JournalEntry.queryWhere(ViewJournalActivity.this, where);
						}
						return _journalEntry;
					}
				});
		        addSearchListener();
		        _observer = new MyObserver(new Handler());
		        registerContentObserver(getData(), false, _observer);
//		        setDefaultOptionsMenu(true);
        	}       
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
    	if (_journal != null)
    	{
    		if (_journal.getCount() == 1)
    		{
    			_journal.moveToFirst();
    		}
    	}
    	if(_adapter!=null)
    	{
    		_adapter.notifyDataSetChanged();
    	}
    	//clear images
    	EntryImageObjectManager.images.clear();
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
//    	if(_journal != null && !_journal.isClosed()){
//    		_journal.close();
//    	}
    	if(_journalEntry != null && !_journalEntry.isClosed()){
    		_journalEntry.close();
    	}
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.view_journal_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_add:
			onMenuAddLogEntry();
			break;
		case R.id.menu_edit:
			startActivity(new Intent(Intent.ACTION_EDIT, getData()));
			break;
		case R.id.menu_delete:
			onMenuDelete();
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
		final String[] options = {"Add Entry","Edit Category"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle("Menu");
		builder.setItems(options,new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if("Add Entry".equals(options[which]))
						{
							onMenuAddLogEntry();
						}else if("Edit Category".equals(options[which]))
						{
							startActivity(new Intent(Intent.ACTION_EDIT, getData()));
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
		
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		try
		{
			_journalEntry.moveToPosition(position);
			//String journalEntryID = ((JournalEntry)getListView().getItemAtPosition(position)).getID();
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.withAppendedPath(JournalEntry.CONTENT_URI, _journalEntry.getID()));
			startActivity(intent);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void sync()
	{
		if (_syncServiceConnection != null)
		{
			_syncServiceConnection.syncDataset(IncidentJournalDataSet.class.getName());
			
		}
	}
	
	private void onMenuAddLogEntry()
	{
		try
		{
			Uri data = _journal.createDefaultEntry(this);
			if (data != null)
			{
				startActivity(new Intent(Intent.ACTION_INSERT, data));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void onMenuDelete()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to delete this category and all log entries?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    	   public void onClick(DialogInterface dialog, int id)
		    	   {
		    		   ContentResolver resolver = getContentResolver();
		    		   for (int i = 0; i < _journalEntry.getCount(); i++)
		    		   {
		    			   _journalEntry.moveToPosition(i);
		    			   resolver.delete(Uri.withAppendedPath(JournalEntry.CONTENT_URI, _journalEntry.getID()),null,null);
		    			  // Log.d("ID OF FELETE JOURNAL ENTRY IS: ",_journalEntry.getID());
		    		   }
		    		   getContentResolver().delete(getData(), null, null);
		    		   finish();
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id)
		           {
		        	   dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
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
	
	private class MyAdapter extends AlternatingColorCursorAdapter
	{
		public MyAdapter()
		{
			super(ViewJournalActivity.this, R.layout.log_entry_list_item, _journalEntry);
			DateTime.setContext(ViewJournalActivity.this);
			DateTimeZone.setProvider(null);
		}

		@Override
		public void bindView(View view, Context context, Cursor c)
		{
			super.bindView(view, context, c);
			try
			{
				JournalEntry journalEntry = (JournalEntry)c;				
				((TextView)view.findViewById(R.id.date)).setText(getFormattedLocalTime(journalEntry.getJournalTime(), "no entry time"));
				((TextView)view.findViewById(R.id.text1)).setText(journalEntry.getText());
				((TextView)view.findViewById(R.id.text1)).setTextColor(JournalEntryPriorityBinding.getPriorityColor(null,journalEntry.getPriority()));
				JournalStatus status = journalEntry.getStatus(ViewJournalActivity.this);				
				if (status != null)
				{
					setImageViewIcon(view, R.id.status_icon, status.getIcon(ViewJournalActivity.this));
				}
			    ((TextView)view.findViewById(R.id.num_images)).setTag(journalEntry.getID());
				textUpdater.displayText(journalEntry.getID(),((TextView)view.findViewById(R.id.num_images)), "");
//			    JournalType journalType = journalEntry.getType(ViewJournalActivity.this);
//				if (journalType != null)
//				{
//					setImageViewIcon(view, R.id.type_icon, journalType.getIcon(ViewJournalActivity.this));
//				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
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
			if (_journal != null)
			{
				try
				{
					_journal.requery();
					if (_journal.getCount() == 1)
					{
						_journal.moveToFirst();
						if (getTitle().equals(_journal.getName()) == false)
						{
							setTitle(_journal.getName());
						}
					}
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

	@Override
	public String getText(String ID) {
		// TODO Auto-generated method stub
		JournalEntryMedia _journalEntryMedia = JournalEntryMedia.queryJournalEntryMedia(getApplicationContext(), ID,JournalEntryMedia.JOURNAL_ENTRY);
		int count = _journalEntryMedia.getCount();
		_journalEntryMedia.close();
		return ""+count;
	}
	
	private void addSearchListener(){
		((EditTextWithSearch)findViewById(R.id.journal_entry_search)).addSearchListener(this);
	}
	
	private void removeSearchListener(){
		((EditTextWithSearch)findViewById(R.id.journal_entry_search)).removeSearchListener(this);
	}
	@Override
	public void handleSearch(String text) {
		// TODO Auto-generated method stub
		if(_adapter!=null)
			_adapter.getFilter().filter(text);
	}
}
