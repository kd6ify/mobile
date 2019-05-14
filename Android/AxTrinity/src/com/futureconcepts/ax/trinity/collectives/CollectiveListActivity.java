package com.futureconcepts.ax.trinity.collectives;

import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FilterQueryProvider;
import android.widget.ListView;

import com.futureconcepts.ax.model.data.Collection;
import com.futureconcepts.ax.model.data.CollectionAttributeTactic;
import com.futureconcepts.ax.model.data.Icon;
import com.futureconcepts.ax.model.dataset.TacticCollectionViewDataSet;
import com.futureconcepts.ax.sync.client.SyncServiceConnection;
import com.futureconcepts.ax.sync.client.SyncServiceConnection.Client;
import com.futureconcepts.ax.trinity.Config;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.ViewItemActivity;
import com.futureconcepts.ax.trinity.logs.images.GetImageBitmap;
import com.futureconcepts.ax.trinity.logs.images.ImageManager;
import com.futureconcepts.ax.trinity.logs.images.ImageManager.ImageManagerGetImageListener;
import com.futureconcepts.ax.trinity.widget.AlternatingColorCursorAdapter;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch.EditTextWithSearchInterface;

public class CollectiveListActivity extends ViewItemActivity implements Client,
	ImageManagerGetImageListener, EditTextWithSearchInterface{
	private static final String TAG = ViewTaskCollectionsActivity.class.getSimpleName();
	private static final long RESYNC_INTERVAL = 1000 * 60; // 1 minute

	private Collection _collectives;
	private Handler _handler;
	private MyAdapter _adapter;
	private CollectionObserver _collectionObserver;
	private SyncServiceConnection _syncServiceConnection;
	private boolean _configChanged = false;
	private Timer _resyncIntervalTimer;
	private ImageManager imageManager;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collective_list_activity);
        if (savedInstanceState != null)
        {
        	_configChanged = true;
        }
        imageManager = new ImageManager(this);
        _handler = new Handler();
        _collectives = Collection.query(this,Collection.CONTENT_URI);
        if (_collectives != null)
        {	
        	onDataChanged();
            _collectionObserver = new CollectionObserver(_handler);
            registerContentObserver(CollectionAttributeTactic.CONTENT_URI, false, _collectionObserver);	
	        _adapter = new MyAdapter();
	        addFilterToAdapter();
	        setListAdapter(_adapter);
	        _syncServiceConnection = new SyncServiceConnection(this, this);
	        _syncServiceConnection.connect();
        }
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
    public void onDestroy()
    {    	
    	removeSearchListener();
    	if (_collectionObserver != null)
    	{
    		unregisterContentObserver(_collectionObserver);
    		_collectionObserver = null;
    	}
    	
    	if(_collectives!=null && !_collectives.isClosed())
    		_collectives.close();
    	
    	if(imageManager!=null){
    		imageManager.clearCache();
    		imageManager.removeImageManagerGetImageListener(this);
    		imageManager.close();
    	}
    	if(_syncServiceConnection!=null)
    		_syncServiceConnection.disconnect();      	
    	super.onDestroy();
    }
    
    private Context getContext()
    {
    	return this;
    }
    public void onDataChanged()
	{
		if (_collectives != null)
		{
			_handler.post(new Runnable() {
				@Override
				public void run()
				{
					setTitle(String.format("Collectives: %s (%d)", Config.getCurrentIncidentName(CollectiveListActivity.this), _collectives.getCount()));
				}
			});
		}
	}
 
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.view_tactic_collectives_options_menu, menu);
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
	
	 
    public void refresh(View view)
    {
    	sync();
    }
	public void goBack(View view)
	{
		finish();
	}
	public void displayMenuOptions(View view)
	{
		final String[] options = {"Refresh"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle("Menu");
		builder.setItems(options,new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if("Refresh".equals(options[which]))
						{
							sync();
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
		_collectives.moveToPosition(position);
		Log.i(TAG, "onListItemClick " + _collectives.getID());
		if (_collectives != null)
		{
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(Collection.CONTENT_URI, _collectives.getID())));
		}
	}

	public void sync()
	{
		if (_syncServiceConnection != null)
		{
			_syncServiceConnection.syncDataset(TacticCollectionViewDataSet.class.getName());
		}
	}
	
	public class MyAdapter extends AlternatingColorCursorAdapter
	{
		public MyAdapter()
		{
			super(CollectiveListActivity.this, R.layout.collection_list_item, _collectives);
		}

		@Override
		public void bindView(View view, Context context, Cursor c)
		{
			super.bindView(view, context, c);
			try
			{
				Collection collection = (Collection)c;
				if (collection != null)
				{
					CollectionListBinder.bindView(view, context, collection, imageManager);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	

	private final class CollectionObserver extends ContentObserver
	{
		public CollectionObserver(Handler handler)
        {
	        super(handler);
        }
		@Override
		public void onChange(boolean selfChange)
		{
			if (_collectives != null)
			{
				try
				{
					_collectives.requery();
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
	public void onSyncServiceConnected()
	{
		if (_configChanged == false)
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
	public Bitmap getImage(String imageID, String filePath, int defaultDrawable) {
		Icon i = Icon.query(getApplicationContext(), Uri.withAppendedPath(Icon.CONTENT_URI,imageID));
		byte [] bytes = i.getImage();
		i.close();
		return  GetImageBitmap.getScaledBitmapFromBytes(this, bytes);//BitmapFactory.decodeByteArray(bytes, 0, bytes.length);	
	}
	
	private void addFilterToAdapter()
	{
		_adapter.setFilterQueryProvider(new FilterQueryProvider() {				
			@Override
			public Cursor runQuery(CharSequence constraint) {
				String text = constraint.toString();
				String where = Collection.CALLSIGN+" Like '%"+text+"%'";
				if(text==null || text.length()==0)
					 _collectives = Collection.query(getContext(),Collection.CONTENT_URI);
				else
					_collectives = new Collection(getContext(),
					      getContext().getContentResolver().query(
					    		  Collection.CONTENT_URI, null,where , null, null));
				return _collectives;
			}
		});
		addSearchListener();
	}
	
	private void addSearchListener()
	{
		((EditTextWithSearch)findViewById(R.id.collectives_search)).addSearchListener(this);
	}
	
	private void removeSearchListener()
	{
		((EditTextWithSearch)findViewById(R.id.collectives_search)).removeSearchListener(this);
	}
	@Override
	public void handleSearch(String text) {
		// TODO Auto-generated method stub
		if(_adapter!=null)
			_adapter.getFilter().filter(text);
	}
	
	

}
