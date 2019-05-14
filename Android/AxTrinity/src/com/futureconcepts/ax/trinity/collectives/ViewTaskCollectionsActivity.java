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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextView;

import com.futureconcepts.ax.model.data.BaseTable;
import com.futureconcepts.ax.model.data.Collection;
import com.futureconcepts.ax.model.data.CollectionAttributeTactic;
import com.futureconcepts.ax.model.data.Icon;
import com.futureconcepts.ax.model.data.Tactic;
import com.futureconcepts.ax.model.dataset.TacticCollectionViewDataSet;
import com.futureconcepts.ax.sync.client.SyncServiceConnection;
import com.futureconcepts.ax.sync.client.SyncServiceConnection.Client;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.ViewItemActivity;
import com.futureconcepts.ax.trinity.logs.images.GetImageBitmap;
import com.futureconcepts.ax.trinity.logs.images.ImageManager;
import com.futureconcepts.ax.trinity.logs.images.ImageManager.ImageManagerGetImageListener;
import com.futureconcepts.ax.trinity.widget.AlternatingColorCursorAdapter;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch.EditTextWithSearchInterface;

public class ViewTaskCollectionsActivity extends ViewItemActivity implements Client,
	ImageManagerGetImageListener, EditTextWithSearchInterface
{
	private static final String TAG = ViewTaskCollectionsActivity.class.getSimpleName();
	private static final long RESYNC_INTERVAL = 1000 * 60; // 1 minute

	private Tactic _tactic;
	private MyCursor _myCursor;
	private Handler _handler;
	private MyAdapter _adapter;
	private TacticObserver _tacticObserver;
	private CollectionAttributeTacticObserver _collectionAttributeTacticObserver;
	private SyncServiceConnection _syncServiceConnection;
	private boolean _configChanged = false;
	private Timer _resyncIntervalTimer;
	private ImageManager imageManager;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_task_collectives);
        if (savedInstanceState != null)
        {
        	_configChanged = true;
        }
        imageManager = new ImageManager(this);
        _handler = new Handler();
        _tactic = Tactic.query(this, getData());
        if (_tactic != null && _tactic.getCount() == 1)
        {
            _tacticObserver = new TacticObserver(_handler);
            registerContentObserver(getData(), false, _tacticObserver);
            _myCursor = MyCursor.query(this, _tactic.getID());
            _collectionAttributeTacticObserver = new CollectionAttributeTacticObserver(_handler);
            registerContentObserver(CollectionAttributeTactic.CONTENT_URI, true, _collectionAttributeTacticObserver);
	    	setTitle("Task Collectives: " + _tactic.getName());
	        _adapter = new MyAdapter();
	        setListAdapter(_adapter);
	        _adapter.setFilterQueryProvider(new FilterQueryProvider() {
				@Override
				public Cursor runQuery(CharSequence constraint) {
					// TODO Auto-generated method stub
					String text = constraint.toString();
					if(text== null || text.length()==0)
						_myCursor = MyCursor.query(getContext(), _tactic.getID());
					else
						_myCursor = MyCursor.querySearch(getContext(),text, _tactic.getID());
					return _myCursor;
				}
			});
	        addSearchListener();
	        _syncServiceConnection = new SyncServiceConnection(this, this);
	        _syncServiceConnection.connect();
        }
        ((TextView)findViewById(R.id.no_data_view)).setText("No Collectives currently assigned");
    }

    private Context getContext()
    {
    	return this;
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
    	if (_tacticObserver != null)
    	{
    		unregisterContentObserver(_tacticObserver);
    		_tacticObserver = null;
    	}
    	if (_collectionAttributeTacticObserver != null)
    	{
    		unregisterContentObserver(_collectionAttributeTacticObserver);
    		_collectionAttributeTacticObserver = null;
    	}
    	if(_tactic!=null && !_tactic.isClosed())
    		_tactic.close();
    	if(_myCursor!=null && !_myCursor.isClosed())
    		_myCursor.close();
    	
    	if(imageManager!=null){
    		imageManager.clearCache();
    		imageManager.removeImageManagerGetImageListener(this);
    		imageManager.close();
    	}
    	if(_syncServiceConnection!=null)
    		_syncServiceConnection.disconnect();  
    	removeSearchListener();
    	super.onDestroy();
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
	
	public void goBack(View view)
	{
		finish();
	}
	
	public void refresh(View view)
	{
		sync();
	}
	

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		//_myCursor.moveToPosition(position);
		String ID = ((MyCursor)getListView().getItemAtPosition(position)).getID();
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(Collection.CONTENT_URI, ID)));
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
			super(ViewTaskCollectionsActivity.this, R.layout.collection_list_item, _myCursor);
		}

		@Override
		public void bindView(View view, Context context, Cursor c)
		{
			super.bindView(view, context, c);
			try
			{
				Collection collection = (Collection)c;
				if (collection != null) {
					CollectionListBinder.bindView(view, context, collection,imageManager);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	private final class TacticObserver extends ContentObserver
	{
		public TacticObserver(Handler handler)
        {
	        super(handler);
        }
		
		@Override
		public void onChange(boolean selfChange)
		{
			if(_tactic!=null)
			{
				_tactic.requery();
				setTitle("Task Collectives: " + _tactic.getName());
			}				
		}
	}
	private final class CollectionAttributeTacticObserver extends ContentObserver
	{
		public CollectionAttributeTacticObserver(Handler handler)
        {
	        super(handler);
        }
		@Override
		public void onChange(boolean selfChange)
		{
			if (_myCursor != null)
			{
				try
				{
					_myCursor.requery();
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
		// TODO Auto-generated method stu
		Icon i = Icon.query(getApplicationContext(), Uri.withAppendedPath(Icon.CONTENT_URI,imageID));
		byte [] bytes = i.getImage();
		i.close();
		return GetImageBitmap.getScaledBitmapFromBytes(this, bytes);
	}
	
	private void addSearchListener()
    {
    	((EditTextWithSearch)findViewById(R.id.collective_search)).addSearchListener(this);
    }
    private void removeSearchListener()
    {
    	((EditTextWithSearch)findViewById(R.id.collective_search)).removeSearchListener(this);
    }

	@Override
	public void handleSearch(String text) {
		if(_adapter!=null)
			_adapter.getFilter().filter(text);
	}
	
	private static final class MyCursor extends Collection
	{		
		public MyCursor(Context context, Cursor cursor)
		{
			super(context, cursor);
		}
		
		public static MyCursor query(Context context, String TacticID)
		{
			MyCursor result = null;
			String query = "Collection INNER JOIN CollectionAttribute ON (CollectionAttribute.Collection = Collection.ID)"+
			" INNER JOIN CollectionAttributeTactic ON (CollectionAttributeTactic.CollectionAttribute =CollectionAttribute.ID)";
			Uri uri = Uri.parse("content://" + BaseTable.AUTHORITY + "/"+query);
			String[] projection = {
					"Collection._id",
					"Collection.ID",
					"Collection.Type",
					"Collection.Callsign",
					"Collection.ResourceType",
					"Collection.TrackWithAsset",
					"Collection.IsClustered",
					"Collection.Icon",
					"Collection.Description",
					"CollectionAttribute.Collection",
					"CollectionAttributeTactic.CollectionAttribute",
					"CollectionAttributeTactic.Tactic"};
			result = new MyCursor(context,
					context.getContentResolver().query(uri, projection,
					"CollectionAttributeTactic.Tactic=?",
					new String[]{TacticID}, null));
			return result;
		}		
		public static MyCursor querySearch(Context context,String searchText, String TacticID)
		{
			MyCursor result = null;
			String query = "Collection INNER JOIN CollectionAttribute ON (CollectionAttribute.Collection = Collection.ID)"+
			" INNER JOIN CollectionAttributeTactic ON (CollectionAttributeTactic.CollectionAttribute =CollectionAttribute.ID)";
			Uri uri = Uri.parse("content://" + BaseTable.AUTHORITY + "/"+query);
			String[] projection = {
					"Collection._id",
					"Collection.ID",
					"Collection.Type",
					"Collection.Callsign",
					"Collection.ResourceType",
					"Collection.TrackWithAsset",
					"Collection.IsClustered",
					"Collection.Icon",
					"Collection.Description",
					"CollectionAttribute.Collection",
					"CollectionAttributeTactic.CollectionAttribute",
					"CollectionAttributeTactic.Tactic"};
			result = new MyCursor(context,
					context.getContentResolver().query(uri, projection,
					"CollectionAttributeTactic.Tactic=? AND Collection.Callsign Like '%"+searchText+"%'",
					new String[]{TacticID}, null));
			return result;
		}			
	}
}