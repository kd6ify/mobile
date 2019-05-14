package com.futureconcepts.ax.trinity.assets;

import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
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

import com.futureconcepts.ax.model.data.Asset;
import com.futureconcepts.ax.model.data.AssetType;
import com.futureconcepts.ax.model.data.BaseTable;
import com.futureconcepts.ax.model.data.Icon;
import com.futureconcepts.ax.model.data.OperationalPeriod;
import com.futureconcepts.ax.model.dataset.UserViewDataSet;
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

public class UserListActivity extends ViewItemActivity implements Client,
	ImageManager.ImageManagerGetImageListener, EditTextWithSearchInterface
{
	private static final long RESYNC_INTERVAL = 1000 * 60; // 1 minute
	
	private MyCursor _cursor;
	private MyAdapter _adapter;
	private MyObserver _observer;
	private SyncServiceConnection _syncServiceConnection;
	private boolean _configChanged = false;
	private Timer _resyncIntervalTimer;
	private Handler _handler;
	private ImageManager imageManager;
//	private LruImageCache lruImageCache;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        DateTime.setContext(this);
        imageManager = new ImageManager(this);
        _handler = new Handler();
        setContentView(R.layout.user_list);
		setTitle("Personnel Assets: " + Config.getCurrentIncidentName(this));
        setProgressBarVisibility(true);
        String incidentID = MercurySettings.getCurrentIncidentId(this);
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
    //	lruImageCache.clearCache();
    	imageManager.clearCache();
    	imageManager.removeImageManagerGetImageListener(this);
    	imageManager.close();
    	_syncServiceConnection.disconnect();
    	removeSearchListener();
    	try
    	{
	    	if (_cursor != null)
	    	{
	    		_cursor.close();
	    		_cursor = null;
	    	}
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
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

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		//_cursor.moveToPosition(position);
		String assetId = ((MyCursor)getListView().getItemAtPosition(position)).getID();//_cursor.getID();
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
	    		result = MyCursor.query(UserListActivity.this);
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
		        	public Cursor runQuery(CharSequence constraint) {	
		        		String text = constraint.toString();
		        		if( text == null  || text.length() == 0 )
		        		{
		        			_cursor = MyCursor.query(getContext());
		        		}else{
		        			_cursor = MyCursor.querySearch(getContext(), text);
		        		}
		        		return _cursor;//_myCursor;
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
					if(!_cursor.isClosed())
			 		setTitle(String.format("Personnel Assets: %s (%d)", Config.getCurrentIncidentName(UserListActivity.this), _cursor.getCount()));
				}
			});
		}
	}
	
	public final class MyAdapter extends ResourceCursorAdapter 
	{
		public MyAdapter()
		{
			super(UserListActivity.this, R.layout.asset_list_item, _cursor);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor)
		{			
			MyCursor c = (MyCursor)cursor;			
			TextView callsignView = (TextView)view.findViewById(R.id.callsign);
			String callsign = c.getCallsign();
			
			if (callsign != null)
			{				
				callsignView.setVisibility(View.VISIBLE);
				callsignView.setText(callsign);
				((TextView)view.findViewById(R.id.callsignTitle)).setVisibility(View.VISIBLE);
			}
			else
			{				
				callsignView.setText("");
			}		
			((TextView)view.findViewById(R.id.name)).setText(c.getPersonName());
			((TextView)view.findViewById(R.id.type_name)).setText(c.getUserTypeName());
			String iconID = c.getIC();		
			((ImageView)view.findViewById(R.id.type_icon)).setTag(iconID);
		//	((ImageView)view.findViewById(R.id.type_icon)).setImageBitmap(lruImageCache.getBitmapIconFromMemCache(iconID,context));
			imageManager.displayImage(iconID, ((ImageView)view.findViewById(R.id.type_icon)), android.R.drawable.ic_menu_gallery, null);
			//			if (bytes != null)
//			{
				//((ImageView)view.findViewById(R.id.type_icon)).setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
			//}
		}		
	}

	private static final class MyCursor extends Asset
	{
		private static final String COLUMN_USER_TYPE_NAME = "UserTypeName";
		private static final String COLUMN_USER_TYPE_ICON = "UserTypeIcon";
		private static final String COLUMN_PERSON_NAME = "PersonName";
		private static final String COLUMN_ICON = "icon";
		
		public MyCursor(Context context, Cursor cursor)
		{
			super(context, cursor);
		}
		
		public static MyCursor query(Context context)
		{
			MyCursor result = null;
			Uri uri = Uri.parse("content://" + BaseTable.AUTHORITY + "/query_distinct_user_list");
			String[] projection = {
					"Asset._id",
					"Asset.ID",
					"Asset.Callsign",
					"OperationalPeriod.ID as OperationalPeriodID",
					"Asset.Type as AssetType",
					"UserType.Name as " + COLUMN_USER_TYPE_NAME,
					"Person.Name as " + COLUMN_PERSON_NAME,
					"UserType.Icon as "+ COLUMN_ICON};
					//"Icon.Image as " + COLUMN_USER_TYPE_ICON };
			result = new MyCursor(context,
					context.getContentResolver().query(uri, projection,
					"OperationalPeriodID=? AND AssetType=?",
					new String[]{MercurySettings.getCurrentOperationalPeriodId(context),
					AssetType.USER}, "PersonName ASC"));
			 //Cursor icon = _cursor.getUserTypeFix bug 8816.
			return result;
		}
		
		public static MyCursor querySearch(Context context, String text)
		{
			MyCursor result = null;
			Uri uri = Uri.parse("content://" + BaseTable.AUTHORITY + "/query_distinct_user_list");
			String[] projection = {
					"Asset._id",
					"Asset.ID",
					"Asset.Callsign",
					"OperationalPeriod.ID as OperationalPeriodID",
					"Asset.Type as AssetType",
					"UserType.Name as " + COLUMN_USER_TYPE_NAME,
					"Person.Name as " + COLUMN_PERSON_NAME,
					"UserType.Icon as "+ COLUMN_ICON};
					//"Icon.Image as " + COLUMN_USER_TYPE_ICON };
			result = new MyCursor(context,
					context.getContentResolver().query(uri, projection,
					"OperationalPeriodID=? AND AssetType=? AND "+COLUMN_PERSON_NAME+" Like '%"+text+"%'",
					new String[]{MercurySettings.getCurrentOperationalPeriodId(context),
					AssetType.USER}, "PersonName ASC"));
			 //Cursor icon = _cursor.getUserTypeFix bug 8816.
			return result;
		}		
		
		public String getUserTypeName()
		{
			return getCursorString(COLUMN_USER_TYPE_NAME);
		}
		
		public String getIC()
		{
			return getCursorString(COLUMN_ICON);
		}
		
		public byte[] getUserTypeIcon()
		{
			return getCursorBlob(COLUMN_USER_TYPE_ICON);
		}
		
		public String getPersonName()
		{
			return getCursorString(COLUMN_PERSON_NAME);
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

	private void sync()
	{
		if (_syncServiceConnection != null)
		{
			_syncServiceConnection.syncDataset(UserViewDataSet.class.getName());
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
	
	public void goBack(View view)
	{
		finish();
	}
	public void refresh(View view)
	{
		sync();
	}

	@Override
	public Bitmap getImage(String imageID, String filePath, int defaultDrawable) {
		// TODO Auto-generated method stub
		Icon i = Icon.query(getApplicationContext(), Uri.withAppendedPath(Icon.CONTENT_URI,imageID));
		byte [] bytes = i.getImage();
		i.close();
		return  GetImageBitmap.getScaledBitmapFromBytes(this, bytes);//BitmapFactory.decodeByteArray(bytes, 0, bytes.length);	
		//return null;
	}

	private void addSearchListener()
    {
    	((EditTextWithSearch)findViewById(R.id.period_search)).addSearchListener(this);
    }
    private void removeSearchListener()
    {
    	((EditTextWithSearch)findViewById(R.id.period_search)).removeSearchListener(this);
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