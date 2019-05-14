package com.futureconcepts.ax.trinity.assets;

import java.util.Timer;
import java.util.TimerTask;

import com.futureconcepts.ax.model.data.Asset;
import com.futureconcepts.ax.model.data.AssetAttributeTactic;
import com.futureconcepts.ax.model.data.AssetType;
import com.futureconcepts.ax.model.data.BaseTable;
import com.futureconcepts.ax.model.data.Icon;
import com.futureconcepts.ax.model.data.Tactic;
import com.futureconcepts.ax.model.dataset.TacticViewDataSet;
import com.futureconcepts.ax.sync.client.SyncServiceConnection;
import com.futureconcepts.ax.sync.client.SyncServiceConnection.Client;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.ViewItemActivity;
import com.futureconcepts.ax.trinity.logs.images.GetImageBitmap;
import com.futureconcepts.ax.trinity.logs.images.ImageManager;
import com.futureconcepts.ax.trinity.logs.images.ImageManager.ImageManagerGetImageListener;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch.EditTextWithSearchInterface;

import android.content.Context;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class ViewTaskAssetsActivity extends ViewItemActivity implements Client, ImageManagerGetImageListener,
	EditTextWithSearchInterface
{
	private static final String TAG = ViewTaskAssetsActivity.class.getSimpleName();
	private static final long RESYNC_INTERVAL = 1000 * 60; // 1 minute

	private MyCursor _myCursor;
	private Tactic _tactic;
	private Handler _handler;
	private MyAdapter _adapter;
	private MyTacticObserver _observer;
	private MyAssetAttributeTacticObserver _assetTacticObserver;
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
        imageManager = new ImageManager(this);
        if (savedInstanceState != null)
        {
        	_configChanged = true;
        }
        _handler = new Handler();
       _tactic = Tactic.query(this, getData());
        if (_tactic != null && _tactic.getCount() == 1)
        {
        	//_tactic.moveToFirst();
        	Log.e("","Tactic ID= "+_tactic.getID());
        	_myCursor = MyCursor.query(this,_tactic.getID());
        	Log.e("","Cursor size= "+_myCursor.getCount());
			//startManagingModel(_assetAttributeTactic = AssetAttributeTactic.query(this, AssetAttributeTactic.TACTIC+"='"+_tactic.getID()+"'"));
		    _observer = new MyTacticObserver(_handler);
		    _assetTacticObserver = new MyAssetAttributeTacticObserver(_handler);
			registerContentObserver(getData(), false, _observer);
		    registerContentObserver(AssetAttributeTactic.CONTENT_URI, true, _assetTacticObserver);
			//if (_assetAttributeTactic != null)
			//{
		    	setTitle("Task Assets: " + _tactic.getName());
		        _adapter = new MyAdapter();
		 
		        setListAdapter(_adapter);
		        _adapter.setFilterQueryProvider(new FilterQueryProvider() {
					@Override
					public Cursor runQuery(CharSequence constraint) {
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
			//}
        }
     ((LinearLayout)findViewById(R.id.menu_options_container)).setVisibility(View.VISIBLE);
     ((TextView)findViewById(R.id.no_data_view)).setText("No Assets currently assigned");
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
    	if (_observer != null)
    	{
    		unregisterContentObserver(_observer);
    	}
    	if (_assetTacticObserver != null)
    	{
    		unregisterContentObserver(_assetTacticObserver);
    	}    	
    	imageManager.clearCache();
    	imageManager.removeImageManagerGetImageListener(this);
    	imageManager.close();
    	_syncServiceConnection.disconnect();
    	removeSearchListener();
    	super.onDestroy();
    }
    
    private Context getContext()
    {
    	return this;
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.view_tactic_assets_options_menu, menu);
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
		_myCursor.moveToPosition(position);
		//Log.i(TAG, "onListItemClick " + _assetAttributeTactic.getID());
		Log.i(TAG, "onListItemClick " + _myCursor.getID());
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(Asset.CONTENT_URI,_myCursor.getID())));
	}

	private void sync()
	{
		if (_syncServiceConnection != null)
		{
			_syncServiceConnection.syncDataset(TacticViewDataSet.class.getName());
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
	
	public class MyAdapter extends ResourceCursorAdapter
	{
		public MyAdapter()
		{
			super(ViewTaskAssetsActivity.this, R.layout.asset_list_item, _myCursor);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor)
		{
			MyCursor myCursor = (MyCursor)cursor;
			TextView callsignView = (TextView)view.findViewById(R.id.callsign);
			String callsign = myCursor.getCallsign();
			if (callsign != null)
			{
				callsignView.setText(callsign);
			}
			else
			{
				callsignView.setText("");
			}
			String assetTypeID = myCursor.getAssetType();
			if (assetTypeID != null)
			{
				if (assetTypeID.equals(AssetType.USER))
				{
					((TextView)view.findViewById(R.id.name)).setText(myCursor.getPersonName());
					((TextView)view.findViewById(R.id.type_name)).setText(myCursor.getUserTypeName());
					String iconID =myCursor.getUserIcon();
					view.findViewById(R.id.type_icon).setTag(iconID);
					imageManager.displayImage( iconID, ((ImageView)view.findViewById(R.id.type_icon)),
							android.R.drawable.ic_menu_gallery,null);
				}
				if (assetTypeID.equals(AssetType.EQUIPMENT))
				{
					((TextView)view.findViewById(R.id.name)).setText(myCursor.getEquipmentName());
					((TextView)view.findViewById(R.id.type_name)).setText(myCursor.getEquipmentTypeName());
					String iconID =myCursor.getUserIcon();
					view.findViewById(R.id.type_icon).setTag(iconID);
					imageManager.displayImage( iconID, ((ImageView)view.findViewById(R.id.type_icon)),
							android.R.drawable.ic_menu_gallery,null);
				}
			}
		}
	}
	
	public final class MyTacticObserver extends ContentObserver
	{
		public MyTacticObserver(Handler handler)
        {
	        super(handler);
        }
		
		@Override
		public void onChange(boolean selfChange)
		{
			if(_tactic!=null)
			{
				_tactic.requery();
				Log.e("asd","DATA CHANGED tactic");
				setTitle("Task Collectives: " + _tactic.getName());
			}	
		}
	}
	public final class MyAssetAttributeTacticObserver extends ContentObserver
	{
		public MyAssetAttributeTacticObserver(Handler handler)
        {
	        super(handler);
        }
		
		@Override
		public void onChange(boolean selfChange)
		{
			if(_myCursor!=null && !_myCursor.isClosed())
				_myCursor.requery();			
			if (_adapter != null)
			{
				_adapter.notifyDataSetChanged();
			}
		}
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
    	((EditTextWithSearch)findViewById(R.id.collective_search)).setHint("Search Assets...");
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
	
	private static final class MyCursor extends Asset
	{		
		private static final String COLUMN_USER_TYPE_NAME = "UserTypeName";
		private static final String COLUMN_USER_ICON = "UserIcon";
		private static final String COLUMN_EQUIPMENT_TYPE_NAME = "EquipmentTypeName";
		private static final String COLUMN_EQUIPMENT_TYPE_ICON = "EquipmentTypeIcon";
		private static final String COLUMN_EQUIPMENT_NAME = "EquipmentName";
		private static final String COLUMN_ASSET_NAME="AssetName";
		private static final String COLUMN_ASSET_TYPE="AssetType";
//		static String query = "Asset INNER JOIN AssetAttribute ON (AssetAttribute.Asset = Asset.ID) "+
//				"INNER JOIN AssetAttributeTactic ON (AssetAttributeTactic.AssetAttribute =AssetAttribute.ID) "+
//				"LEFT JOIN User ON (Asset.User=User.ID) "+
//					"INNER JOIN UserType ON (User.Type=UserType.ID) "+
//				"INNER JOIN Person ON (User.Person=Person.ID) "+
//				"LEFT JOIN EquipmentType ON (Asset.EquipmentType=EquipmentType.ID) "+
//				"LEFT JOIN Equipment ON (Asset.Equipment=Equipment.ID) "+
//				"INNER JOIN Icon ON (EquipmentType.Icon=Icon.ID  OR UserType.Icon=Icon.ID)";
		
		public MyCursor(Context context, Cursor cursor)
		{
			super(context, cursor);
		}
		
		public String getEquipmentTypeName()
		{
			return getCursorString(COLUMN_EQUIPMENT_TYPE_NAME);
		}
		
		public String getEquipmentName()
		{
			return getCursorString(COLUMN_EQUIPMENT_NAME);
		}
		
		public String getUserTypeName()
		{
			return getCursorString(COLUMN_USER_TYPE_NAME);
		}
		
		public String getUserIcon()
		{
			return getCursorString(COLUMN_USER_ICON);
		}
		
		public String getPersonName()
		{
			return getCursorString(COLUMN_ASSET_NAME);
		}
		public String getAssetType()
		{
			return getCursorString(COLUMN_ASSET_TYPE);
		}
		
		public static MyCursor query(Context context, String TacticID)
		{
			MyCursor result = null;
			
			Uri uri = Uri.parse("content://" + BaseTable.AUTHORITY + "/query_distinct_user_tactic_list");
			String[] projection = {
					"Asset._id",
					"Asset.ID",
					"Asset.Callsign",
					"Asset.Type as "+COLUMN_ASSET_TYPE,
					"UserType.Name as " + COLUMN_USER_TYPE_NAME,
					"Person.Name as " + COLUMN_ASSET_NAME,
					"UserType.Icon as "+ COLUMN_USER_ICON,
					"EquipmentType.Name as " + COLUMN_EQUIPMENT_TYPE_NAME,
					"Equipment.Name as " + COLUMN_ASSET_NAME,
					"EquipmentType.Icon as "+ COLUMN_EQUIPMENT_TYPE_ICON};
			result = new MyCursor(context,
					context.getContentResolver().query(uri, projection,
					"AssetAttributeTactic.Tactic=?" ,
					new String[]{TacticID}, null));
			return result;
		}		
		public static MyCursor querySearch(Context context,String text, String TacticID)
		{
			MyCursor result = null;
			
			Uri uri = Uri.parse("content://" + BaseTable.AUTHORITY + "/query_distinct_user_tactic_list");
			String[] projection = {
					"Asset._id",
					"Asset.ID",
					"Asset.Callsign",
					"Asset.Type as "+COLUMN_ASSET_TYPE,
					"UserType.Name as " + COLUMN_USER_TYPE_NAME,
					"Person.Name as " + COLUMN_ASSET_NAME,
					"UserType.Icon as "+ COLUMN_USER_ICON,
					"EquipmentType.Name as " + COLUMN_EQUIPMENT_TYPE_NAME,
					"Equipment.Name as " + COLUMN_ASSET_NAME,
					"EquipmentType.Icon as "+ COLUMN_EQUIPMENT_TYPE_ICON};
			result = new MyCursor(context,
					context.getContentResolver().query(uri, projection,
					"AssetAttributeTactic.Tactic=? AND "+COLUMN_ASSET_NAME+" Like '%"+text+"%'" ,
					new String[]{TacticID}, null));
			return result;
		}	
	}
}