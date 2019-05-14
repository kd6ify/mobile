package com.futureconcepts.ax.trinity.assets;

import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;

import com.futureconcepts.ax.globalclases.LruImageCache;
import com.futureconcepts.ax.model.data.Asset;
import com.futureconcepts.ax.model.data.AssetType;
import com.futureconcepts.ax.model.data.BaseTable;
import com.futureconcepts.ax.model.data.Icon;
import com.futureconcepts.ax.model.dataset.EquipmentViewDataSet;
import com.futureconcepts.ax.sync.client.SyncServiceConnection;
import com.futureconcepts.ax.sync.client.SyncServiceConnection.Client;
import com.futureconcepts.ax.trinity.Config;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.ViewItemActivity;
import com.futureconcepts.ax.trinity.logs.images.GetImageBitmap;
import com.futureconcepts.ax.trinity.logs.images.ImageManager;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch.EditTextWithSearchInterface;
import com.futureconcepts.gqueue.MercurySettings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class EquipmentListActivity extends ViewItemActivity implements Client,
	ImageManager.ImageManagerGetImageListener,EditTextWithSearchInterface
{
	private static final long RESYNC_INTERVAL = 1000 * 60; // 1 minute
		
	private MyCursor _cursor;
	private MyAdapter _adapter;
	private MyObserver _observer;
	private SyncServiceConnection _syncServiceConnection;
	private boolean _configChanged = false;
	private Timer _resyncIntervalTimer;
	private Handler _handler;
	//private LruImageCache lruImageCache;
	private ImageManager imageManager;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        DateTime.setContext(this);
        _handler = new Handler();
        setContentView(R.layout.equipment_list);
		setTitle("Equipment Assets: " + Config.getCurrentIncidentName(this));
        setProgressBarVisibility(true);
        String incidentID = MercurySettings.getCurrentIncidentId(this);
        imageManager = new ImageManager(this);  
        if (incidentID != null)
        {
        	setProgressBarIndeterminateVisibility(true);
        	new LoadTask().execute((Void)null);
        }
        else
        {
        	onError("Please select incident");
		}
        if (savedInstanceState != null)
        {
        	_configChanged = true;
        }
        _syncServiceConnection = new SyncServiceConnection(this, this);
        _syncServiceConnection.connect();
        addSearchListener();
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
    	super.onDestroy();
    	imageManager.clearCache();
    	imageManager.removeImageManagerGetImageListener(this);
    	imageManager.close();
    	_syncServiceConnection.disconnect();
    	removeSearchListener();
    	if (_cursor != null)
    	{
    		_cursor.close();
    		_cursor = null;
    	}
    }
    
    @Override
    protected void onNewIntent(Intent intent)
    {
//    	Log.d("EquipmentListActivity", "onNewIntent");
//    	Log.d("EquipmentListActivity", intent.getAction());
//    	#Intent;action=android.intent.action.SEARCH;launchFlags=0x10000000;component=com.futureconcepts.trinity/.assets.EquipmentListActivity;S.query=airjd;S.user_query=airjd;end    	
    	
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.equipment_list_options_menu, menu);
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
	
	private void sync()
	{
		if (_syncServiceConnection != null)
		{
			_syncServiceConnection.syncDataset(EquipmentViewDataSet.class.getName());
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
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		_cursor.moveToPosition(position);
		String assetId = _cursor.getID();
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(Asset.CONTENT_URI, assetId)));
	}

	private void onError(String message)
	{
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("Incident Required");
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
	
	private Context getContext()
	{
		return this;
	}
	
	private final class LoadTask extends AsyncTask<Void, Integer, MyCursor>
	{
	    protected MyCursor doInBackground(Void... params)
	    {
	    	MyCursor result = null;
	    	try
	    	{
	    		result = MyCursor.query(EquipmentListActivity.this);
	    	}
	    	catch (Exception e)
	    	{
	    		e.printStackTrace();
	    	}
	    	return result;
	     }

//	     protected void onProgressUpdate(Integer... progress)
//	     {
//	         setProgressPercent(progress[0]);
//	     }

	     protected void onPostExecute(MyCursor result)
	     {
	    	 setProgressBarIndeterminateVisibility(false);
	    	 if (result != null && isFinishing() == false)
	    	 {
		    	 _cursor = result;
		    	 onDataChanged();
				//startManagingCursor(_cursor);
		        _adapter = new MyAdapter();
		        setListAdapter(_adapter);
		        _adapter.setFilterQueryProvider(new FilterQueryProvider() {					
					@Override
					public Cursor runQuery(CharSequence constraint) {
						// TODO Auto-generated method stub
						String text = constraint.toString();
						if(text==null || text.length() ==0)
						{
							_cursor = MyCursor.query(getContext());
						}else{
							_cursor = MyCursor.querySearch(getContext(), text);
						}
						return _cursor;
					}
				});
				_observer = new MyObserver(new Handler());
				registerContentObserver(Asset.CONTENT_URI, true, _observer);
	    	 }
	    	 else
	    	 {
	    		 Log.d("AssetListActivity", "load task finished but found activity finished");
	    	 }
	     }
	}

	public void onDataChanged()
	{
		if (_cursor != null)
		{
			_handler.post(new Runnable() {
				@Override
				public void run()
				{
					setTitle(String.format("Equipment Assets: %s (%d)", Config.getCurrentIncidentName(EquipmentListActivity.this), _cursor.getCount()));
				}
			});
		}
	}
	
	public final class MyAdapter extends ResourceCursorAdapter
	{
		public MyAdapter()
		{
			super(EquipmentListActivity.this, R.layout.asset_list_item, _cursor);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor)
		{
			MyCursor c = (MyCursor)cursor;
			TextView callsignView = (TextView)view.findViewById(R.id.callsign);
			String callsign = c.getCallsign();
			if (callsign != null)
			{
				callsignView.setText(callsign);
			}
			else
			{
				callsignView.setText("");
			}
			((TextView)view.findViewById(R.id.name)).setText(c.getEquipmentName());
			((TextView)view.findViewById(R.id.type_name)).setText(c.getEquipmentTypeName());
			String iconID = c.getIcon();
			((ImageView)view.findViewById(R.id.type_icon)).setTag(iconID);
			imageManager.displayImage(iconID, ((ImageView)view.findViewById(R.id.type_icon)), android.R.drawable.ic_menu_gallery, null);
//			byte[] bytes = c.getEquipmentTypeIcon();
//			if (bytes != null)
//			{
//				((ImageView)view.findViewById(R.id.type_icon)).setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
//			}
		}
	}
	
	private static final class MyCursor extends Asset
	{
		private static final String COLUMN_EQUIPMENT_TYPE_NAME = "EquipmentTypeName";
		private static final String COLUMN_EQUIPMENT_TYPE_ICON = "EquipmentTypeIcon";
		private static final String COLUMN_EQUIPMENT_NAME = "EquipmentName";
		
		public MyCursor(Context context, Cursor cursor)
		{
			super(context, cursor);
		}
		
		public static MyCursor query(Context context)
		{
			MyCursor result = null;
			Uri uri = Uri.parse("content://" + BaseTable.AUTHORITY + "/query_distinct_equipment_list");
			String[] projection = {
					"Asset._id",
					"Asset.ID",
					"Asset.Callsign",
					"OperationalPeriod.Incident as Incident",
					"Asset.Type as AssetType",
					"EquipmentType.Name as " + COLUMN_EQUIPMENT_TYPE_NAME,
					"Equipment.Name as " + COLUMN_EQUIPMENT_NAME,
					"EquipmentType.Icon as "+ COLUMN_EQUIPMENT_TYPE_ICON};
					//"Icon.Image as " + COLUMN_EQUIPMENT_TYPE_ICON };
			result = new MyCursor(context,
					context.getContentResolver().query(uri,
							projection,
							"Incident=? AND AssetType=?",
							new String[]{MercurySettings.getCurrentIncidentId(context), AssetType.EQUIPMENT},
							"EquipmentName ASC"));
			return result;
		}
		
		public static MyCursor querySearch(Context context, String text)
		{
			MyCursor result = null;
			Uri uri = Uri.parse("content://" + BaseTable.AUTHORITY + "/query_distinct_equipment_list");
			String[] projection = {
					"Asset._id",
					"Asset.ID",
					"Asset.Callsign",
					"OperationalPeriod.Incident as Incident",
					"Asset.Type as AssetType",
					"EquipmentType.Name as " + COLUMN_EQUIPMENT_TYPE_NAME,
					"Equipment.Name as " + COLUMN_EQUIPMENT_NAME,
					"EquipmentType.Icon as "+ COLUMN_EQUIPMENT_TYPE_ICON};
					//"Icon.Image as " + COLUMN_EQUIPMENT_TYPE_ICON };
			result = new MyCursor(context,
					context.getContentResolver().query(uri,
							projection,
							"Incident=? AND AssetType=? AND "+COLUMN_EQUIPMENT_NAME+" Like '%"+text+"%'",
							new String[]{MercurySettings.getCurrentIncidentId(context), AssetType.EQUIPMENT},
							"EquipmentName ASC"));
			return result;
		}
		
		public String getIcon()
		{
			return getCursorString(COLUMN_EQUIPMENT_TYPE_ICON);
		}
		
		public String getEquipmentTypeName()
		{
			return getCursorString(COLUMN_EQUIPMENT_TYPE_NAME);
		}
		
		public byte[] getEquipmentTypeIcon()
		{
			return getCursorBlob(COLUMN_EQUIPMENT_TYPE_ICON);
		}
		
		public String getEquipmentName()
		{
			return getCursorString(COLUMN_EQUIPMENT_NAME);
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
			if (_cursor != null)
			{
				try
				{
					_cursor.requery();
					if (_adapter != null)
					{
						_adapter.notifyDataSetChanged();
					}
					onDataChanged();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public Bitmap getImage(String imageID, String filePath, int defaultDrawable) {
		// TODO Auto-generated method stub
		Icon i = Icon.query(getApplicationContext(), Uri.withAppendedPath(Icon.CONTENT_URI,imageID));
		byte [] bytes = i.getImage();
		i.close();
		return GetImageBitmap.getScaledBitmapFromBytes(this, bytes);//Bitmap.createScaledBitmap(unscaledBitmap, scaledWidth, scaledHeight, true);
		//Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);	
		//bytes = null;
		//return bitmap;
	}
	
	private void addSearchListener()
    {
    	((EditTextWithSearch)findViewById(R.id.equipment_search)).addSearchListener(this);
    }
    private void removeSearchListener()
    {
    	((EditTextWithSearch)findViewById(R.id.equipment_search)).removeSearchListener(this);
    }
	
	@Override
	public void handleSearch(final String text) {
		Handler mHandler = new Handler(getMainLooper());
	    mHandler.post(new Runnable() {
	        @Override
	        public void run() {	      
	        	if(_adapter!=null)
	        		_adapter.getFilter().filter(text);
	        }
	    });	   
		
	}
}