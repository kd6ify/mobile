package com.futureconcepts.ax.trinity.main;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.futureconcepts.ax.globalclases.IncidentCategory;
import com.futureconcepts.ax.globalclases.LruImageCache;
import com.futureconcepts.ax.model.data.BaseTable;
import com.futureconcepts.ax.model.data.Icon;
import com.futureconcepts.ax.model.data.Incident;
import com.futureconcepts.ax.model.data.IncidentStatus;
import com.futureconcepts.ax.model.data.OperationalPeriod;
import com.futureconcepts.ax.model.dataset.IncidentDataSet;
import com.futureconcepts.ax.model.dataset.IncidentListViewDataSet;
import com.futureconcepts.ax.sync.client.SyncServiceConnection;
import com.futureconcepts.ax.sync.client.SyncServiceConnection.Client;
import com.futureconcepts.ax.trinity.ModelListActivity;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.logs.images.CustomAlertDialog;
import com.futureconcepts.ax.trinity.logs.images.EntryImageObjectManager;
import com.futureconcepts.ax.trinity.logs.images.GetImageBitmap;
import com.futureconcepts.ax.trinity.logs.images.ImageManager;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch.EditTextWithSearchInterface;
import com.futureconcepts.gqueue.MercurySettings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class IncidentSelectorActivity extends ModelListActivity implements Client,
ImageManager.ImageManagerGetImageListener, EditTextWithSearchInterface
{
	private static final String TAG = IncidentSelectorActivity.class.getSimpleName();

	private static final int ACTIVITY_AUTHORIZE_INCIDENT = 1;
	
	private LoadAsyncTask _loadAsyncTask;
	private MyCursor _myCursor;
	private int _selectedPosition;
	private OperationalPeriod _operationalPeriod;
	private MyAdapter _adapter;
	private MyObserver _observer;
	private ProgressDialog _loadProgressDialog;
	private SyncServiceConnection _syncServiceConnection;
	private Bundle _savedInstanceState;
	private ImageManager imageManager;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        _savedInstanceState = savedInstanceState;
        setContentView(R.layout.new_incident_list_activity);
        imageManager = new ImageManager(this);  
       // _myCursor = MyCursor.query(getContext());
		_loadAsyncTask = new LoadAsyncTask();
		_loadAsyncTask.execute();       
		
		_syncServiceConnection = new SyncServiceConnection(this, this);
		_syncServiceConnection.connect();
		addSearchListener();
    }
    
    private void addSearchListener()
    {
    	((EditTextWithSearch)findViewById(R.id.incident_search)).addSearchListener(this);
    }
    private void removeSearchListener()
    {
    	((EditTextWithSearch)findViewById(R.id.incident_search)).removeSearchListener(this);
    }
    
    @Override
    protected boolean isIncidentNull() {
  	// TODO Auto-generated method stub
    	return false;
    }
    
    @Override
    public void onDestroy()
    {
    	removeSearchListener();
    	try
    	{
	    	if (_myCursor != null)
	    	{
	    		_myCursor.close();
	    		_myCursor = null;
	    	}
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    	_syncServiceConnection.disconnect();
    	imageManager.removeImageManagerGetImageListener(this);
    	imageManager.clearCache();
    	imageManager.close();
    	super.onDestroy();
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.incident_selector_options_menu, menu);
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
	protected void onListItemClick(ListView l, View v, final int position, long id)
	{
		 String[] buttons = {"Cancel","OK"};
		 // Create an instance of the dialog fragment and show it
		  CustomAlertDialog customDialog = new CustomAlertDialog(this, buttons,"Information","Are you sure you want to select this incident?",android.R.drawable.ic_menu_info_details,
				 new CustomAlertDialog.DialogButtonClickListener() {
					@Override
					public void onDialogButtonClick(View v) {
						// TODO Auto-generated method stub
						if("OK".equals(v.getTag()))
						{
							_selectedPosition = position;
							//_myCursor.moveToPosition(position);
							_myCursor = (MyCursor)getListView().getItemAtPosition(position);
							//MyCursor cursor = (MyCursor)getListView().getItemAtPosition(position);
							byte[] password = _myCursor.getPassword();
							if (password != null)
							{
								doAuthorizeIncident();
							}
							else
							{
								doSelectIncident();
							}							
						}					
					}
				});
		 customDialog.show();
	}
	
	private Context getContext()
	{
		return this;
	}
	
	private void doAuthorizeIncident()
	{
		Intent intent = new Intent(this, AuthorizeIncidentActivity.class);
		intent.setData(Uri.withAppendedPath(Incident.CONTENT_URI, _myCursor.getID()));
		startActivityForResult(intent, ACTIVITY_AUTHORIZE_INCIDENT);
	}
	
	private void sync()
	{
		if (_syncServiceConnection != null)
		{
			_syncServiceConnection.syncDataset(IncidentListViewDataSet.class.getName());
		}
	}
	
	private void doSelectIncident()
	{
		try
		{
			//MercurySettings.setCurrentIncidentId(this, _myCursor.getID());
			//_syncServiceConnection.getCurrentTransaction().abort();	        
			_syncServiceConnection.setCurrentIncidentID(_myCursor.getID());	
			_syncServiceConnection.deleteDataset(IncidentDataSet.class.getName());
			_syncServiceConnection.syncDataset(IncidentDataSet.class.getName());
			_syncServiceConnection.startSyncing();
			startManagingModel(_operationalPeriod = OperationalPeriod.queryIncident(this, _myCursor.getID()));
			if (_operationalPeriod != null)
			{
				if (_operationalPeriod.getCount() == 0)
				{
					MercurySettings.setCurrentOperationalPeriodId(this, null);
					
				}
				else if (_operationalPeriod.getCount() == 1)
				{
					_operationalPeriod.moveToFirst();
					MercurySettings.setCurrentOperationalPeriodId(this, _operationalPeriod.getID());
					
				}
				else
				{
					Intent intent = new Intent(this, OperationalPeriodSelectorActivity.class);
					startActivity(intent);					
				}
			}
			else
			{
				MercurySettings.setCurrentOperationalPeriodId(this, null);
			}
			//Intent i = new Intent();
			setResult(RESULT_OK);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
			finish();
	}

	protected void onSync()
	{
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		if (requestCode == ACTIVITY_AUTHORIZE_INCIDENT)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				_myCursor.requery();
				_myCursor.moveToPosition(_selectedPosition);
				doSelectIncident();
			}
		}
	}
	
	private class MyAdapter extends ResourceCursorAdapter
	{
		public MyAdapter()
		{
			super(getContext(), R.layout.incident_list_item, _myCursor);
			DateTime.setContext(getApplicationContext());
			DateTimeZone.setProvider(null);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor)
		{
			MyCursor myCursor = (MyCursor)cursor;
			try
			{
				((TextView)view.findViewById(R.id.text1)).setText(myCursor.getName());
				DateTime startTime = myCursor.getStartTime();
				((TextView)view.findViewById(R.id.date)).setText(getFormattedLocalTime(startTime, "no start"));
				((TextView)view.findViewById(R.id.type_name)).setText(myCursor.getIncidentTypeName());
				String iconID = myCursor.getIncidentIconTypeID();		
				((ImageView)view.findViewById(R.id.type_icon)).setTag(iconID);
				imageManager.displayImage(iconID, ((ImageView)view.findViewById(R.id.type_icon)), android.R.drawable.ic_menu_gallery,null);
			//	((ImageView)view.findViewById(R.id.type_icon)).setImageBitmap(lruImageCache.getBitmapIconFromMemCache(iconID,context));
//				byte[] blob = myCursor.getIncidentTypeIcon();
//				if (blob != null)
//				{
//					setImageBitmap(view, R.id.type_icon, blob);
//				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		private void setImageBitmap(View convertView, int resid, byte[] blob)
		{
			if (blob != null)
			{
				ImageView view = (ImageView)convertView.findViewById(resid);
				view.setImageBitmap(BitmapFactory.decodeByteArray(blob, 0, blob.length));
			}
		}
	}

	private final class LoadAsyncTask extends AsyncTask<Void, Integer, Exception>
	{
		@Override
		protected void onPreExecute()
		{
			_loadProgressDialog = new ProgressDialog(getContext());
			_loadProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			_loadProgressDialog.setIndeterminate(true);
			_loadProgressDialog.setTitle("Loading Incidents");
			_loadProgressDialog.setMessage("loading...");
			_loadProgressDialog.show();
		}
		
	    protected Exception doInBackground(Void... params)
	    {
	    	Exception result = null;
	    	try
	    	{
		    	if (isCancelled() == false)
		    	{
		    		try{
		    			_myCursor = MyCursor.query(getContext());
		    		}catch(Exception e)
		    		{
		    			e.printStackTrace();
		    		}
		    	}
	    	}
	    	catch (Exception e)
	    	{
	    		result = e;
	    	}
	    	return result;
	     }

	     protected void onPostExecute(Exception result)
	     {
	    	 if (_loadProgressDialog != null && _loadProgressDialog.isShowing())
	    	 {
	    		 _loadProgressDialog.dismiss();
	    		 _loadProgressDialog = null;
	    	 }
	    	 if ((result == null) && (_myCursor != null) && (isFinishing() == false))
	    	 {
		        _adapter = new MyAdapter();
		        setListAdapter(_adapter);		       
		        	   
		        _adapter.setFilterQueryProvider(new FilterQueryProvider() {
		        	public Cursor runQuery(CharSequence constraint) {		        	        	 
		        		_myCursor =
		        				MyCursor.getIncidentsFromSearch(getContext(),constraint.toString());
		        		return _myCursor;
		        	}
		        });
		        _observer = new MyObserver(new Handler());
				registerContentObserver(Incident.CONTENT_URI, true, _observer);
				_observer.onChange(true);
	    	 }
	    	 else{
	    		 onError("Please Reset Data"); 
	    	 }
	     }
	}
	
	private void onError(String message)
	{
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("Information");
		ab.setMessage(message);
		ab.setNeutralButton("OK", new OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				finish();
			}
		});
		AlertDialog dialog = ab.create();
		dialog.show();
	}
	
	public static class MyCursor extends Incident
	{
		private static final String COLUMN_INCIDENT_TYPE_NAME = "IncidentTypeName";
		private static final String COLUMN_INCIDENT_TYPE_ICON = "IncidentTypeIcon";
		private static final String COLUMN_INCIDENT_ICON_ID = "IncidentTypeIconID";
		
		public MyCursor(Context context, Cursor cursor)
		{
			super(context, cursor);
		}
		
		public static MyCursor query(Context context)
		{
			MyCursor result = null;
			Uri uri = Uri.parse("content://" + BaseTable.AUTHORITY + "/query_incident_selector_list");
			String[] projection = {
					"Incident._id",
					"Incident.ID",
					Incident.STATUS,
					Incident.START_TIME,
					Incident.PASSWORD,
					"IncidentType.NAME as " + COLUMN_INCIDENT_TYPE_NAME,
					"Incident.NAME as Name",
					"IncidentType.Icon as "+ COLUMN_INCIDENT_ICON_ID};//,
					//"Icon.Image as " + COLUMN_INCIDENT_TYPE_ICON };
			result = new MyCursor(context,
					context.getContentResolver().query(uri, projection,
					Incident.STATUS+"!='"+IncidentStatus.ARCHIVED+"' AND "
					+Incident.CATEGORY+"!='"+IncidentCategory.categoryTemplate+"'",
					null,
					Incident.START_TIME + " DESC"));
			return result;
		}
		
		public static MyCursor getIncidentsFromSearch(Context context ,String text)
		{
			  if (text == null  ||  text.length () == 0) 
				  return MyCursor.query(context);
			  else
				  return (MyCursor)MyCursor.querySearch(context,text);
		}
		
		public static MyCursor querySearch(Context context,String incidentName)
		{
			MyCursor result = null;
			Uri uri = Uri.parse("content://" + BaseTable.AUTHORITY + "/query_incident_selector_list");
			String[] projection = {
					"Incident._id",
					"Incident.ID",
					Incident.STATUS,
					Incident.START_TIME,
					Incident.PASSWORD,
					"IncidentType.NAME as " + COLUMN_INCIDENT_TYPE_NAME,
					"Incident.NAME as Name",
					"IncidentType.Icon as "+ COLUMN_INCIDENT_ICON_ID};//,
					//"Icon.Image as " + COLUMN_INCIDENT_TYPE_ICON };
			result = new MyCursor(context,
					context.getContentResolver().query(uri, projection,
					Incident.STATUS+"!='"+IncidentStatus.ARCHIVED+"' AND "
					+Incident.CATEGORY+"!='"+IncidentCategory.categoryTemplate+"'"+
					" AND Incident.Name Like '%"+incidentName+"%'",
					null,
					Incident.START_TIME + " DESC"));
			return result;
		}
		
		
		
		public String getIncidentIconTypeID()
		{
			return getCursorString(COLUMN_INCIDENT_ICON_ID);
		}
		public String getIncidentTypeName()
		{
			return getCursorString(COLUMN_INCIDENT_TYPE_NAME);
		}
		
		public byte[] getIncidentTypeIcon()
		{
			return getCursorBlob(COLUMN_INCIDENT_TYPE_ICON);
		}
	}
	
	private class MyObserver extends ContentObserver
	{

		public MyObserver(Handler handler)
		{
			super(handler);
		}
		
		@Override
		public void onChange(boolean selfChange)
		{
			if (_myCursor != null && !_myCursor.isClosed())
			{
				_myCursor.requery();				
			}else
			{
				_myCursor = MyCursor.query(getContext());
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
		Icon i = Icon.query(getApplicationContext(), Uri.withAppendedPath(Icon.CONTENT_URI,imageID));
		byte [] bytes = i.getImage();
		i.close();
		return GetImageBitmap.getScaledBitmapFromBytes(this, bytes);// BitmapFactory.decodeByteArray(bytes, 0, bytes.length);		
	}

	@Override
	public void handleSearch(final String text) {
		if (_adapter != null)
			_adapter.getFilter().filter(text);
		// _adapter.notifyDataSetChanged();

	}
}
