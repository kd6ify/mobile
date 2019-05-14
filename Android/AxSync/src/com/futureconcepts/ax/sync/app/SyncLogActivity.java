package com.futureconcepts.ax.sync.app;

import com.futureconcepts.ax.model.dataset.StaticDataSet;
import com.futureconcepts.ax.sync.R;
import com.futureconcepts.ax.sync.SyncTransaction;
import com.futureconcepts.ax.sync.client.ISyncListener;
import com.futureconcepts.ax.sync.client.ISyncManager;
import com.futureconcepts.ax.sync.client.ISyncTransaction;
import com.futureconcepts.ax.sync.client.ISyncTransactionListener;
import com.futureconcepts.ax.sync.client.SyncError;
import com.futureconcepts.ax.sync.client.SyncServiceConnection;
import com.futureconcepts.ax.sync.client.SyncServiceConnection.Client;
import com.futureconcepts.ax.sync.config.Config;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SyncLogActivity extends Activity implements Client, OnSharedPreferenceChangeListener
{
	private SyncServiceConnection _syncClient;
	private MySyncListener _syncListener;
	private MyTransactionListener _syncTransactionListener;
	private SyncTransaction _syncTransaction;
	private boolean _syncListenerRegistered = false;
	private TextView _serviceStatusView;
	private ImageView _syncStatusIcon;
	private TextView _actionView;
	private TextView _datasetView;
	private TextView _tableView;
	private ViewGroup _progressView;
	private TextView _transactionStatusView;
	private ProgressBar _progressBar;
	private View _errorView;
	private TextView _errorTitle;
	private TextView _errorMessage;
	private AlertDialog _rescheduledDialog;
	private Handler _handler;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sync_log);
        onSharedPreferenceChanged(Config.getPreferences(this), Config.KEY_LAST_SYNC_ERROR_MESSAGE);
        onFinishInflate();
        _handler = new Handler();
        _syncTransactionListener = new MyTransactionListener();
        _syncClient = new SyncServiceConnection(this, this);
        _syncClient.connect();
    }
    
    private void onFinishInflate()
    {
        _serviceStatusView = (TextView)findViewById(R.id.service_status);
        _actionView = (TextView)findViewById(R.id.action);
        _datasetView = (TextView)findViewById(R.id.dataset);
        _tableView = (TextView)findViewById(R.id.table);
        _syncStatusIcon = (ImageView)findViewById(R.id.icon);
        _progressView = (ViewGroup)findViewById(R.id.progress_view);
        _progressView.setVisibility(View.GONE);
        _transactionStatusView = (TextView)findViewById(R.id.transaction_status);
        _progressBar = (ProgressBar)findViewById(R.id.progress);
        _errorView = findViewById(R.id.error_view);
        _errorTitle = (TextView)findViewById(R.id.title);
        _errorMessage = (TextView)findViewById(R.id.message);
        _errorView.setVisibility(View.GONE);
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
        Config.getPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
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
    	Config.getPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy()
    {
		_syncClient.unregisterSyncListener(_syncListener);
		_syncListener = null;
		_syncClient.disconnect();
    	super.onDestroy();
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.show_sync_status_menu, menu);
	//	return super.onCreateOptionsMenu(menu);
		return true;
	}
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
//		case R.id.menu_sync_static:
//			onMenuSyncStatic();
//			break;
		case R.id.menu_start_service:
			onMenuStartService();
			break;
		case R.id.menu_reset_data:
			onMenuResetData();
			break;
		}
		return false;
	}

	private void onMenuSyncStatic()
	{
		if (_syncClient != null && _syncClient.isConnected())
		{
			_syncClient.syncDataset(StaticDataSet.class.getName());
		}
	}
	
	private void onMenuStartService()
	{
		if (_syncClient != null && _syncClient.isConnected())
		{
			_syncClient.startSyncing();
		}
	}

	private void onMenuResetData()
	{
		if (_syncClient.isConnected())
		{
            AlertDialog.Builder b = new AlertDialog.Builder(SyncLogActivity.this);
            b.setTitle("WARNING: About to reset data!");
            b.setCancelable(false);
            b.setMessage("All incident data will be erased and resynced.  Are you sure you want to do this?");
            b.setPositiveButton("Yes", new DialogInterface.OnClickListener()
            {
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					_syncClient.setCurrentIncidentID("30B83A9F-9D45-4453-96B3-05484FD04D91"); // test ID -- doesn't matter
					_syncClient.dropDataset(StaticDataSet.class.getName());
		        	_syncClient.syncDataset(StaticDataSet.class.getName());
					_syncClient.startSyncing();
					dialog.dismiss();
				}
            });
            b.setNegativeButton("No", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
            b.show();
		}
		else
		{
	        Toast.makeText(this, "sync service not running--please try again later", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if (key.equals(Config.KEY_LAST_SYNC_ERROR_MESSAGE))
		{
			String message = Config.getLastSyncErrorMessage(this);
			if (message != null)
			{
				ViewGroup viewGroup = (ViewGroup)this.findViewById(R.id.error_view);
				viewGroup.setVisibility(View.VISIBLE);
				TextView titleView = (TextView)viewGroup.findViewById(R.id.title);
				titleView.setText("Last synchronizer error:");
				TextView messageView = (TextView)viewGroup.findViewById(R.id.message);
				messageView.setText(message);
			}
			else
			{
				ViewGroup viewGroup = (ViewGroup)this.findViewById(R.id.error_view);
				viewGroup.setVisibility(View.GONE);
			}
		}
	}
	
	@Override
	public void onSyncServiceConnected()
	{
		_serviceStatusView.setText("Synchronizer Service is running");
		_syncListener = new MySyncListener();
		_syncClient.registerSyncListener(_syncListener);
		ISyncTransaction currentTransaction = _syncClient.getCurrentTransaction();
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
	}

	@Override
	public void onSyncServiceDisconnected()
	{
		_serviceStatusView.setText("Disconnected");
		unregisterSyncListener(_syncListener);
	}
	
	private void registerSyncListener(ISyncListener listener)
	{
		if (_syncListenerRegistered == false)
		{
			if (_syncClient != null)
			{
				try
				{
					_syncClient.registerSyncListener(listener);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			_syncListenerRegistered = true;
		}
	}
	
	public void unregisterSyncListener(ISyncListener listener)
	{
		if (_syncListenerRegistered == true)
		{
	    	if (_syncClient != null)
	    	{
	    		try
				{
					_syncClient.unregisterSyncListener(listener);
					_syncListenerRegistered = false;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
	    	}
		}
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
			_handler.post(new Runnable()
			{
				@Override
				public void run()
				{
					if (_rescheduledDialog != null)
					{
						_rescheduledDialog.dismiss();
						_rescheduledDialog = null;
					}
					_syncStatusIcon.setVisibility(View.VISIBLE);
				}
			});
		}

		@Override
		public void onRescheduled() throws RemoteException
		{
        	_handler.post(new Runnable() {
				@Override
				public void run()
				{
		            AlertDialog.Builder b = new AlertDialog.Builder(SyncLogActivity.this);
		            b.setTitle("Sync Rescheduled");
		            b.setCancelable(false);
		            b.setMessage("There is no network connectivity.  Syncing is rescheduled to run later.  Would you like to try again now?");
		            b.setPositiveButton("Yes", new DialogInterface.OnClickListener()
		            {
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							_syncClient.startSyncing();
							dialog.dismiss();
						}
		            });
		            b.setNegativeButton("No", new DialogInterface.OnClickListener()
		            {
		                public void onClick(DialogInterface dialog, int which)
		                {
		                    dialog.dismiss();
		                }
		            });
		            _rescheduledDialog = b.show();
				}
        	});
		}
		
		@Override
		public void onStop() throws RemoteException
		{
			_handler.post(new Runnable()
			{
				@Override
				public void run()
				{
					_syncStatusIcon.setVisibility(View.GONE);
				}
			});
		}

		@Override
		public void onTransaction(final ISyncTransaction transaction) throws RemoteException
		{
			try
			{
				transaction.registerListener(_syncTransactionListener);
			}
			catch (RemoteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			_handler.post(new Runnable()
			{
				@Override
				public void run()
				{
					_progressView.setVisibility(View.VISIBLE);
					_progressBar.setVisibility(View.VISIBLE);
					try
					{
						_datasetView.setText(transaction.getDataset());
					}
					catch (RemoteException e)
					{
						_datasetView.setText("Unknown");
					}
					try
					{
						_tableView.setText(transaction.getTable());
					}
					catch (RemoteException e)
					{
						_tableView.setText("Unknown");
					}
					_transactionStatusView.setText("");
				}
			});
		}
		
		@Override
		public void onTransactionComplete(final ISyncTransaction transaction) throws RemoteException
		{
			_handler.post(new Runnable()
			{
				@Override
				public void run()
				{
					_progressView.setVisibility(View.GONE);
					_progressBar.setVisibility(View.GONE);
					_actionView.setText("");
					_datasetView.setText("");
					_tableView.setText("");
					_transactionStatusView.setText("Success");
				}
			});
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
					_actionView.setText(action);
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
					_datasetView.setText(dataset);
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
					_tableView.setText(table);
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
					_progressBar.setIndeterminate(true);
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
					_progressBar.setIndeterminate(false);
				}
			});
		}

		@Override
		public void onError(final SyncError syncError) throws RemoteException
		{
			_handler.post(new Runnable()
			{
				@Override
				public void run()
				{	
					_errorTitle.setText("fixme"); // TODO
					_errorMessage.setText("fixme"); // TODO
				}
			});
		}

		@Override
		public void onStatusChanged(final String status) throws RemoteException
		{
			_handler.post(new Runnable()
			{
				@Override
				public void run()
				{
					_transactionStatusView.setText(status);
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
					_progressBar.setMax(count);
					_progressBar.setProgress(position);
				}
			});
		}
	}
}
