package com.futureconcepts.ax.trinity.main;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.futureconcepts.ax.model.data.BaseTable;
import com.futureconcepts.ax.model.data.Incident;
import com.futureconcepts.ax.model.dataset.IncidentDataSet;
import com.futureconcepts.ax.model.dataset.IncidentListViewDataSet;
import com.futureconcepts.ax.model.dataset.StaticDataSet;
import com.futureconcepts.ax.sync.client.ISyncListener;
import com.futureconcepts.ax.sync.client.ISyncTransaction;
import com.futureconcepts.ax.sync.client.ISyncTransactionListener;
import com.futureconcepts.ax.sync.client.SyncError;
import com.futureconcepts.ax.sync.client.SyncServiceConnection;
import com.futureconcepts.ax.sync.client.SyncServiceConnection.Client;
import com.futureconcepts.ax.trinity.CheckIncidentNotNull;
import com.futureconcepts.ax.trinity.Config;
import com.futureconcepts.ax.trinity.ModelActivity;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.UploadImageReceiver;
import com.futureconcepts.ax.trinity.ar.TriageARDemoActivity;
import com.futureconcepts.ax.trinity.assets.EquipmentListActivity;
import com.futureconcepts.ax.trinity.assets.UserListActivity;
import com.futureconcepts.ax.trinity.collectives.CollectiveListActivity;
import com.futureconcepts.ax.trinity.logs.IntelSummaryActivity;
import com.futureconcepts.ax.trinity.logs.JournalListActivity;
import com.futureconcepts.ax.trinity.osm.MainMapOSMActivity;
import com.futureconcepts.ax.trinity.tasks.ViewTasksMainActivity;
import com.futureconcepts.ax.trinity.triage.TriageMainActivity;
import com.futureconcepts.ax.trinity.widget.SyncProgressView;
import com.futureconcepts.gqueue.MercurySettings;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Launcher extends ModelActivity implements OnItemClickListener, Client
{
	private static final String TAG = Launcher.class.getSimpleName();
	
	private static final String ACTIVITY_PACKAGE_MESSENGER = "com.futureconcepts.drake.ui";
	
	private static final String ACTIVITY_CLASS_MESSENGER = "com.futureconcepts.drake.ui.app.ContactListActivity";

	private static final String ACTIVITY_PACKAGE_BROADCASTER = "com.futureconcepts.ax.broadcaster"; 
	
	private static final String ACTIVITY_CLASS_BROADCASTER = "com.futureconcepts.ax.broadcaster.ui.MainActivity";
	
	// actions
	public static final String ACTION_START_SERVICES = "com.futureconcepts.action.START_SERVICES";
	public static final String ACTION_RESTART = "com.futureconcepts.action.RESTART";
	public static final String ACTION_STATE_CHANGED = "com.futureconcepts.action.STATE_CHANGED";

	private PackageManager _pm;	
	private MyAdapter _adapter;
	
	private ArrayList<CustomActivityInfo> _activities;
	private OnSharedPreferenceChangeListener _sharedPreferenceChangeListener;
	private Handler _handler = new Handler();
//	private ProgressDialog _syncDialog;
	private Toast _rescheduledDialog;
	private SyncServiceConnection _syncServiceConnection;
	private Bundle _savedInstanceState;
	private MySyncListener _syncListener;
	private MyTransactionListener _syncTransactionListener;
	private ISyncTransaction _syncTransaction;
	private ViewGroup _headerGroupView;
	private GridView _gridView;
	private SyncProgressView _progressView;
	private IncidentAndPeriodUpdater incidentUpdater;
	private Timer _resyncIntervalTimer;	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.launcher_main);
		_gridView = (GridView)findViewById(R.id.grid);
		_headerGroupView = (ViewGroup)findViewById(R.id.header_group);
		_progressView = (SyncProgressView)findViewById(R.id.progress_view);
		hideSyncProgressView();
		_savedInstanceState = savedInstanceState;
		_pm = getPackageManager();
        _activities = new ArrayList<CustomActivityInfo>();
        addActivity(UserListActivity.class, R.drawable.launcher_personnel_disabled);
        addActivity(EquipmentListActivity.class, R.drawable.launcher_equipment_disabled);
        addActivity(ViewTasksMainActivity.class,R.drawable.launcher_tasks_disabled);
        addActivity(CollectiveListActivity.class,R.drawable.launcher_collectives_disabled);
        addActivity(IntelSummaryActivity.class,R.drawable.launcher_intel_disabled);
        addActivity(TriageMainActivity.class,R.drawable.launcher_triage_disabled);
        addActivity(JournalListActivity.class,R.drawable.launcher_logs_disabled);
        addActivity(MainMapOSMActivity.class,R.drawable.launcher_mapping_disabled);
        //addActivity(MainMapActivity.class);       
        addActivity(TriageARDemoActivity.class,R.drawable.launcher_augmented_reality_disabled);
        addActivity(ACTIVITY_PACKAGE_MESSENGER, ACTIVITY_CLASS_MESSENGER);
        addActivity(ACTIVITY_PACKAGE_BROADCASTER, ACTIVITY_CLASS_BROADCASTER);
     //   addActivity(Prefs.class);
        
        _adapter = new MyAdapter();
        _gridView.setAdapter(_adapter);
        _gridView.setOnItemClickListener(this);
        
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);        
    	setIncidentPeriod();
        findViewById(R.id.menu_bar).setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
				displayMenuOptions();
			}
        });
        findViewById(R.id.back_bar).setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
				finish();
			}
        });
        _syncServiceConnection = new SyncServiceConnection(this, this);
        _syncServiceConnection.connect();       
       incidentUpdater = new IncidentAndPeriodUpdater();
        scheduleImagesUploaderService();
	}
	
	private OnClickListener incidentBarClick = new OnClickListener()
    {
		@Override
		public void onClick(View v)
		{
			//final Animation animation = new AlphaAnimation(1.0f, 0.0f);
			//animation.setDuration(2000);
			//v.startAnimation(animation);
			onIncidentBarClick();
			removeTopButtonsListeners();
		}
    };
    
    private OnClickListener operationalBarClick = new OnClickListener()
    {
		@Override
		public void onClick(View v)
		{
			onOperationalPeriodBarClick();
			removeTopButtonsListeners();
		}
    };
    
    private void removeTopButtonsListeners()
    {
    	 findViewById(R.id.incident_bar).setOnClickListener(null);
	     findViewById(R.id.operationalperiod_bar).setOnClickListener(null);
    }
	
	@Override
	public void onResume()
	{
		super.onResume();
		findViewById(R.id.incident_bar).setOnClickListener(incidentBarClick);
        findViewById(R.id.operationalperiod_bar).setOnClickListener(operationalBarClick);
		setIncidentPeriod();
		Config.getPreferences(this).registerOnSharedPreferenceChangeListener(_sharedPreferenceChangeListener);
		if(!isServerSettingsAvailable())
		{
			showDownloadConfiguration();
		}
		if(incidentUpdater != null)
			incidentUpdater.createResyncTimer();			
	}
	
	private void scheduleImagesUploaderService(){		
		UploadImageReceiver.scheduleAlarms(this);
	}
	private boolean isServerSettingsAvailable ()
	{
	    if(MercurySettings.getMediaImagesServerAddress(getApplicationContext())!=null &&
	    	MercurySettings.getMediaImagesServerPassword(getApplicationContext())!=null &&
	    	MercurySettings.getMediaImagesServerUser(getApplicationContext())!=null){
	    	return true;
	    }
	    return false;
	}
	
	private void showDownloadConfiguration()
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Information");
		alert.setCancelable(false);
		alert.setMessage("Please open Mercury and select Download Configuration");
		alert.setPositiveButton("Open Mercury", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	            dialog.cancel();
	            Intent intent = new Intent();
	    		intent.setClassName("com.futureconcepts.mercury", "com.futureconcepts.mercury.main.SettingsActivity");
	    		startActivity(intent);
	        }
	    });
		alert.show();
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		Config.getPreferences(this).unregisterOnSharedPreferenceChangeListener(_sharedPreferenceChangeListener);
		if(incidentUpdater!=null)
			incidentUpdater.cancelResyncTimer();//_resyncIntervalTimer.cancel();
		//hideSyncProgressView();
//		if (_rescheduledDialog != null)
//		{
//			_rescheduledDialog.dismiss();
//			_rescheduledDialog = null;
//		}
	}
		
	@Override
	public void onDestroy()
	{
		clearSyncTransaction();
		_syncServiceConnection.unregisterSyncListener(_syncListener);
		_syncListener = null;
		_syncServiceConnection.disconnect();
		incidentUpdater.destroyIncidentUpdaterConnection();
		super.onDestroy();
	}
	
	public void displayMenuOptions()
	{
		final String[] options = {"About Trikorder"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle("Menu");
		builder.setItems(options,new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if("About Trikorder".equals(options[which]))
						{
							viewAbout();
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
	
	
	private void clearSyncTransaction()
	{
		if (_syncTransaction != null)
		{
			if (_syncTransactionListener != null)
			{
				try
				{
					_syncTransaction.unregisterListener(_syncTransactionListener);
					_syncTransactionListener = null;
				}
				catch (RemoteException e)
				{
					e.printStackTrace();
				}
			}
			_syncTransaction = null;
		}
	}
	
	private void setIncidentPeriod()
	{
		if (MercurySettings.getCurrentIncidentId(this) != null)
		{
			String incidentName = Config.getCurrentIncidentName(this);
			if (incidentName != null)
			{
				setIncidentPeriod(incidentName+" - "+Config.getCurrentOperationalPeriodName(this));
			}
			else
			{
				setIncidentPeriod("PLEASE SELECT INCIDENT");
			}
		}
		else
		{
			setIncidentPeriod("PLEASE SELECT INCIDENT");
		}
	}

	private void setIncidentPeriod(String title)
	{
		TextView view = (TextView)findViewById(R.id.incident_period);
		view.setText(title);
		view.setSelected(true);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode ==  RESULT_OK)
		{
			if(_syncServiceConnection.getCurrentTransaction()!=null){
				showSyncProgressView();
			}			
			
		}
	}
	
	private void onIncidentBarClick()
	{
		Intent intent = new Intent(this, IncidentSelectorActivity.class);
		startActivityForResult(intent,20);
	}

	private void onOperationalPeriodBarClick()
	{
		Intent intent = new Intent(this, OperationalPeriodSelectorActivity.class);
		startActivity(intent);
	}

	private void addActivity(Class<?> theClass,int disableIcon)
	{
		try
		{
			ActivityInfo activityInfo = _pm.getActivityInfo(new ComponentName(getPackageName(), theClass.getCanonicalName()), 0);
			_activities.add(new CustomActivityInfo(activityInfo,disableIcon));
		}
		catch (Exception e){}
	}
	
	private void addActivity(String packageName, String className)
	{
		try
		{
			ActivityInfo activityInfo = _pm.getActivityInfo(new ComponentName(packageName, className), 0);
			_activities.add(new CustomActivityInfo(activityInfo,0));
		}
		catch (Exception e) {}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.launcher_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_about:
			viewAbout();
			break;
		}
		return false;
	}

	private void performInitialConfiguration()
	{
        Toast.makeText(this, "performing initial configuration--please relaunch later", Toast.LENGTH_LONG).show();
        if (_syncServiceConnection.isConnected())
        {
			//_syncServiceConnection.dropDataset(StaticDataSet.class.getName());
        	_syncServiceConnection.syncDataset(StaticDataSet.class.getName());
        }
	}
	
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
	{
		ActivityInfo activityInfo = _activities.get(position).getActivityInfo();
		Intent intent = new Intent();
		intent.setClassName(activityInfo.packageName, activityInfo.name);
		startActivity(intent);
	}
	
	public static void checkInToIncident(Context context)
	{
	//	GQueue.insertServerMessage(context, null, Intent.ACTION_INSERT, "server://Incident/" +  config.getCurrentIncidentId() + "/CheckIn");
//		Toast.makeText(context, "Check In queued", Toast.LENGTH_LONG);
	}

	public static void checkOutFromIncident(Context context)
	{
	//	GQueue.insertServerMessage(context, null, Intent.ACTION_INSERT, "server://Incident/" +  config.getCurrentIncidentId() + "/CheckOut");
//		Toast.makeText(context, "Check Out queued", Toast.LENGTH_LONG);
	}
	
	private void viewAbout()
	{
		startActivity(new Intent(this, AboutActivity.class));
	}
	
	private boolean isIncidentNull()
	{
		if (CheckIncidentNotNull.isIncidentNull(this))
			return true;
		else
			return false;
	}
	
	private class MyAdapter extends BaseAdapter
	{
		public int getCount() 
		{
			return _activities.size();
		}

		public Object getItem(int position)
		{
			return position;
		}

		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public boolean areAllItemsEnabled()
		{
//			return false;
			return true;
		}
		
		@Override
		public boolean isEnabled(int position)
		{
			ActivityInfo activityInfo = _activities.get(position).getActivityInfo();
			return activityInfo.enabled;
		}
		
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View row = convertView;

			 if(row==null){
			  LayoutInflater inflater=getLayoutInflater();
			  row=inflater.inflate(R.layout.launcher_list_item, parent, false);
			 }
			ImageView imageView = (ImageView)row.findViewById(R.id.icon);
			try
			{
				ActivityInfo activityInfo = _activities.get(position).getActivityInfo();
//				ActivityInfo Logs = _pm.getActivityInfo(new ComponentName(getPackageName(), JournalListActivity.class.getCanonicalName()), 0);
//				if((EntryImageObjectManager.isLiteVersion && !activityInfo.name.equals(Logs.name)))
//				{						
//					activityInfo.enabled=false;						
//				}				
				if (activityInfo.icon != 0)
				{
					if(isIncidentNull())
					{
						activityInfo.enabled=false;		
						if(_activities.get(position).getDisabledIconResource()!=0)
							imageView.setImageResource(_activities.get(position).getDisabledIconResource());	
					}else 
					{
						activityInfo.enabled=true;
						imageView.setImageResource(activityInfo.icon);
					}
				}
				else
				{
					if (activityInfo.name.equals(ACTIVITY_CLASS_MESSENGER))
					{
						imageView.setImageResource(R.drawable.launcher_messenger);
					}
					else if (activityInfo.name.equals(ACTIVITY_CLASS_BROADCASTER))
					{
						imageView.setImageResource(R.drawable.launcher_broadcaster);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return row;
		}
	}

	private void hideSyncProgressView()
	{
		_progressView.hide();
		_gridView.setVisibility(View.VISIBLE);
		_headerGroupView.setVisibility(View.VISIBLE);
		if(_adapter!=null)
			_adapter.notifyDataSetChanged();
	}
	
	private void showSyncProgressView()
	{
		_progressView.show();
		_gridView.setVisibility(View.GONE);
		_headerGroupView.setVisibility(View.GONE);
	}
	
	@Override
	public void onSyncServiceConnected()
	{
		Log.d(TAG, "onSyncServiceConnected");
		if (_syncListener == null)
		{
			_syncListener = new MySyncListener();
			_syncServiceConnection.registerSyncListener(_syncListener);
		}
		ISyncTransaction currentTransaction = _syncServiceConnection.getCurrentTransaction();
		if (currentTransaction != null)
		{
			try
			{
				_syncListener.onTransaction(currentTransaction);
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
			}
		}
		sendBroadcast(new Intent(ACTION_START_SERVICES));
        if (Config.isFirstApplicationLaunch(this))
        {
        	performInitialConfiguration();
        }
	}

	@Override
	public void onSyncServiceDisconnected()
	{
		Log.d(TAG, "onSyncServiceDisconnected");
	}
	
	private class MySyncListener extends ISyncListener.Stub
	{
		@Override
		public IBinder asBinder()
		{
			return this;
		}

		@Override
		public void onStart() throws RemoteException
		{
		}

		@Override
		public void onRescheduled() throws RemoteException
		{
			_handler.post(new Runnable() {
				@Override
				public void run() {
				// TODO Auto-generated method stub
					if(_rescheduledDialog==null)
					{
						LayoutInflater inflater = getLayoutInflater();
						View layout = inflater.inflate(R.layout.rescheduled_sync_toast,
						                               (ViewGroup) findViewById(R.id.toast_layout_root));

						_rescheduledDialog = new Toast(getApplicationContext());
						_rescheduledDialog.setGravity(Gravity.BOTTOM, 0, 70);
						_rescheduledDialog.setDuration(Toast.LENGTH_LONG);
						_rescheduledDialog.setView(layout);
						_rescheduledDialog.show();
					}
					hideSyncProgressView();
					_rescheduledDialog.show();
				}});
			
//			if(_rescheduledDialog==null){
//				_handler.post(new Runnable() {
//					@Override
//					public void run()
//					{
//						try
//						{
//							if(!isFinishing()){
//								AlertDialog.Builder b = new AlertDialog.Builder(Launcher.this);
//								b.setTitle("Sync Rescheduled");
//								b.setCancelable(true);
//								b.setMessage("There is no network connectivity.  Syncing is rescheduled to run later.  Would you like to try again now?");
//								b.setPositiveButton("Yes", new DialogInterface.OnClickListener()
//								{
//									@Override
//									public void onClick(DialogInterface dialog, int which)
//									{
//										_syncServiceConnection.startSyncing();
//										_rescheduledDialog.dismiss();
//										_rescheduledDialog = null;
//									}
//								});
//								b.setNegativeButton("No", new DialogInterface.OnClickListener()
//								{
//									public void onClick(DialogInterface dialog, int which)
//									{
//										_rescheduledDialog.dismiss();
//										_rescheduledDialog =null;
//									}
//								});
//								_rescheduledDialog = b.create();
//								_rescheduledDialog.show();							
//							}
//						}
//						catch (Exception e)
//						{
//							e.printStackTrace();
//						}
//					}
//				});
//			}
		}
		
		@Override
		public void onStop() throws RemoteException
		{
		}

		@Override
		public void onTransaction(final ISyncTransaction transaction) throws RemoteException
		{
			try
			{
				clearSyncTransaction();
				String dataset = transaction.getDataset();
				Log.d(TAG, "onTransaction dataset=" + dataset);
				if (dataset.equals(StaticDataSet.class.getName()) || dataset.equals(IncidentDataSet.class.getName()))
				{
					_syncTransaction = transaction;
					_syncTransactionListener = new MyTransactionListener();
					transaction.registerListener(_syncTransactionListener);
					_handler.post(new Runnable()
					{
						@Override
						public void run()
						{
							try
							{
								showSyncProgressView();
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					});
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		@Override
		public void onTransactionComplete(final ISyncTransaction transaction) throws RemoteException
		{
			try
			{
				String dataset = transaction.getDataset();
				Log.d(TAG, "onTransactionComplete dataset=" + dataset);
				if (dataset.equals(StaticDataSet.class.getName()) || dataset.equals(IncidentDataSet.class.getName()))
				{
					_syncTransaction = null;
					if (_syncTransactionListener != null)
					{
						transaction.unregisterListener(_syncTransactionListener);
						_syncTransactionListener = null;
					}
					_handler.post(new Runnable()
					{
						@Override
						public void run()
						{
							try
							{
								hideSyncProgressView();
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					});
				}
				
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private class MyTransactionListener extends ISyncTransactionListener.Stub
	{
		@Override
		public void onActionChanged(final String action) throws RemoteException
		{
			_handler.post(new Runnable()
			{
				@Override
				public void run()
				{
					_progressView.setAction(action);
				}
			});
		}
				
		@Override
		public void onDatasetChanged(final String dataset) throws RemoteException
		{
			_handler.post(new Runnable()
			{
				@Override
				public void run()
				{
					_progressView.setDataset(dataset);
				}
			});
		}

		@Override
		public void onTableChanged(final String table) throws RemoteException
		{
			_handler.post(new Runnable()
			{

				@Override
				public void run()
				{
					_progressView.setTable(table);
				}
			});
		}

		@Override
		public void onServerFetch() throws RemoteException
		{
			_handler.post(new Runnable()
			{
				@Override
				public void run()
				{
					_progressView.setDownloading(true);
				}
			});
		}

		@Override
		public void onServerFetchDone() throws RemoteException
		{
			_handler.post(new Runnable()
			{
				@Override
				public void run()
				{
					_progressView.setDownloading(false);
				}
			});
		}

		@Override
		public void onError(final SyncError syncError) throws RemoteException
		{
//			_handler.post(new Runnable()
//			{
//				@Override
//				public void run()
//				{
//					_errorTitle.setText("fixme"); // TODO
//					_errorMessage.setText("fixme"); // TODO
//				}
//			});
		}

		@Override
		public void onStatusChanged(final String status) throws RemoteException
		{
			_handler.post(new Runnable()
			{
				@Override
				public void run()
				{
//					_transactionStatusView.setText(status);
				}
			});
		}

		@Override
		public void onProgress(final int position, final int count) throws RemoteException
		{
			_handler.post(new Runnable()
			{
				@Override
				public void run()
				{
					_progressView.setProgress(position, count);
				}
			});
		}
	}

	public  static class MyCustomCursor extends Incident
	{
	//	private static final String COLUMN_INCIDENT_TYPE_NAME = "IncidentTypeName";
		
		public MyCustomCursor(Context context, Cursor cursor)
		{
			super(context, cursor);
		}
		
		public static MyCustomCursor query(Context context)
		{
			MyCustomCursor result = null;
			Uri uri = Uri.parse("content://" + BaseTable.AUTHORITY+ "/Incident"); //+ "/query_incident_selector_list");
			String[] projection = {
				"Incident._id",
				"Incident.ID",
				"Incident.NAME as Name",};
			result = new MyCustomCursor(context,
				context.getContentResolver().query(uri, projection,
				Incident.ID+"='"+MercurySettings.getCurrentIncidentId(context)+"'",
				null,
				null));
			return result;
		}	
		public String getIncidentName()
		{
			return getCursorString(Incident.NAME);
		}
	}
	
	public void syncIncident(View view)
	{
		incidentUpdater.sync();
	}
		
	private class IncidentAndPeriodUpdater implements Client {
		public MyCustomCursor _myCursor;
		public MyObserver _observer;
		public SyncServiceConnection _syncServiceIncidentConnection;
		private static final long RESYNC_INTERVAL = 2000 * 60; // 2 minutes
		private boolean updaterInitialized = false;
		
		public IncidentAndPeriodUpdater() {
			Log.e("Incident Updater","IncidentUpdater Created");
			_observer = new MyObserver(new Handler());
			registerContentObserver(Incident.CONTENT_URI, true, _observer);
			_observer.onChange(true);
				initializateUpdater();			
		}

		private void initializateUpdater()
		{
			if (!isIncidentNull() &&  _syncServiceConnection.getCurrentTransaction() == null) {
				Log.e("Custom updater","Updater Has Been Inizializate");
				_syncServiceIncidentConnection = new SyncServiceConnection(getApplicationContext(), IncidentAndPeriodUpdater.this);
				_syncServiceIncidentConnection.connect();
				_myCursor = MyCustomCursor.query(getApplicationContext());
				updaterInitialized = true;
			}
		}
		
		private void createResyncTimer()
		{
			_resyncIntervalTimer = new Timer("ResyncIntervalTimer");
	        _resyncIntervalTimer.schedule(new TimerTask()
	        {
				@Override
				public void run()
				{
					if(updaterInitialized)
						sync();
					else
						initializateUpdater();
				}
	        }, 0, RESYNC_INTERVAL);
		}
		private void cancelResyncTimer()
		{
			_resyncIntervalTimer.cancel();
			Log.e("Custom updater","Synctimer cancelled");
		}
		private void  destroyIncidentUpdaterConnection()
		{
			if(updaterInitialized){
				if ( _myCursor != null)
	    		{
	    			_myCursor.close();
	    			_myCursor = null;
	    		}			
				_syncServiceIncidentConnection.disconnect();
				updaterInitialized =false;
			}
		}
		

		private void sync() {
			//if(updaterInitialized){
				Log.e("Custom updater","Sync called");
				_syncServiceIncidentConnection.syncDataset(IncidentListViewDataSet.class.getName());
			//}
		}

		private class MyObserver extends ContentObserver {

			public MyObserver(Handler handler) {
				super(handler);
			}

			@Override
			public void onChange(boolean selfChange) {
				if (_myCursor != null && !isIncidentNull()) {
					_myCursor.requery();
					setIncidentPeriod();
					Log.e("Custom updater","Data changed");
				}
			}
		}

		@Override
		public void onSyncServiceConnected() {
			// TODO Auto-generated method stub
			//sync();
		}

		@Override
		public void onSyncServiceDisconnected() {
			// TODO Auto-generated method stub

		}
	}
}
