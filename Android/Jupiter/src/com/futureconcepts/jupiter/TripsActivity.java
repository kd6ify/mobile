package com.futureconcepts.jupiter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.futureconcepts.jupiter.R;
import com.futureconcepts.jupiter.data.Folder;
import com.futureconcepts.jupiter.filemanager.Intents;
import com.futureconcepts.jupiter.kml.TripImporter;
import com.futureconcepts.jupiter.kml.TripExporter;
import com.futureconcepts.jupiter.util.FileUtils;

public class TripsActivity extends Activity
{
	private static final String TAG = "TripsActivity";

	private static final int ACTIVITY_IMPORT = 8;
	private static final int ACTIVITY_EXPORT = 9;

	private static final String DEFAULT_TRIPS_IMPORT_FOLDER = "/sdcard/FutureConcepts/.trips/import";
	private static final String DEFAULT_TRIPS_EXPORT_FOLDER = "/sdcard/FutureConcepts/.trips/export";
	
	private MyAdapter _adapter;
	private Folder _tripsFolder;
	private Folder _trip;
	private MyObserver _observer;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.trip_list);
		findViewById(R.id.btn_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		ListView listView = (ListView)findViewById(R.id.list);
		_adapter = new MyAdapter();
		listView.setAdapter(_adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				_trip.moveToPosition(arg2);
				Intent intent = new Intent(TripsActivity.this, EditTripActivity.class);
				intent.setData(Uri.withAppendedPath(Folder.CONTENT_URI, _trip.get_ID()));
				startActivity(intent);
			}
		});
		_tripsFolder = Folder.getFolderByParentId(this, null, Folder.NAME_TRIPS);
		if (_tripsFolder != null)
		{
			_trip = Folder.getFoldersByParentId(this, _tripsFolder.getId());
			int count = _trip.getCount();
			Log.d(TAG, "got " + count + " trips");
			_observer = new MyObserver(new Handler());
			getContentResolver().registerContentObserver(Uri.withAppendedPath(Folder.CONTENT_URI, _tripsFolder.get_ID()), false, _observer);
		}
	}
        
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	if (_tripsFolder != null)
    	{
    		_tripsFolder.close();
    	}
    	if (_observer != null)
    	{
    		getContentResolver().unregisterContentObserver(_observer);
    		_observer = null;
    	}
    	if (isFinishing())
    	{
    		setResult(Activity.RESULT_OK);
    	}
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.trips_options_menu, menu);
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
		case R.id.menu_new:
			onMenuNew();
			break;
		case R.id.menu_import:
			onMenuImport();
			break;
		case R.id.menu_export:
			onMenuExport();
			break;
		}
		return false;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == ACTIVITY_IMPORT)
		{
			if (resultCode == RESULT_OK)
			{
				new ImportTripTask().execute(data.getData());
			}
		}
		else if (requestCode == ACTIVITY_EXPORT)
		{
			if (resultCode == RESULT_OK)
			{
				new ExportTripTask().execute(data.getData());
			}
		}
	}

	private void onMenuNew()
	{
		Intent intent = new Intent(this, EditTripActivity.class);
		intent.setData(createNewTrip());
		startActivity(intent);
	}

	private void onMenuImport()
	{
		Intent intent = new Intent(Intents.ACTION_OPEN_FILE);
		File importFolder = new File(DEFAULT_TRIPS_IMPORT_FOLDER);
		importFolder.mkdirs();
		intent.setData(Uri.fromFile(importFolder));
		intent.putExtra(Intents.EXTRA_TITLE, "Select Trip File");
		startActivityForResult(intent, ACTIVITY_IMPORT);
	}

	private void onMenuExport()
	{
		Intent intent = new Intent(Intents.ACTION_SAVE_FILE);
		File exportFolder = new File(DEFAULT_TRIPS_EXPORT_FOLDER);
		exportFolder.mkdirs();
		intent.setData(Uri.fromFile(exportFolder));
		intent.putExtra(Intents.EXTRA_TITLE, "Select Trip Filename");
		startActivityForResult(intent, ACTIVITY_EXPORT);
	}
	
	private Uri createNewTrip()
	{
		Uri result = null;
		if (_tripsFolder != null)
		{
			SimpleDateFormat format = new SimpleDateFormat();
			ContentValues values = new ContentValues();
			String id = UUID.randomUUID().toString().toLowerCase();
			values.put(Folder.ID, id);
			values.put(Folder.PARENT_ID, _tripsFolder.getId());
			values.put(Folder.NAME, "Trip " + format.format(new Date(System.currentTimeMillis())));
			values.put(Folder.LAST_MODIFIED_TIME, System.currentTimeMillis());
			result = getContentResolver().insert(Folder.CONTENT_URI, values);
		}
		return result;
	}
	
	private class ImportTripTask extends AsyncTask<Uri, Integer, Integer>
	{
		private ProgressDialog _progressDialog;
		private Exception _exception;
		
		@Override
		protected void onPreExecute()
		{
			// present spinning progress wheel
	        _progressDialog = ProgressDialog.show(TripsActivity.this, "Please wait", "loading trip");
		}

		@Override
		protected Integer doInBackground(Uri... params)
		{
			TripImporter parser = new TripImporter(TripsActivity.this);
			try
			{
				parser.parse(FileUtils.getZipFile(params[0]));
			}
			catch (Exception e)
			{
				e.printStackTrace();
				_exception = e;
			}
			return 0;
		}
		
		@Override
		protected void onPostExecute(Integer result)
		{
			if (_progressDialog != null)
			{
				_progressDialog.dismiss();
			}
			if (_exception != null)
			{
				AlertDialog.Builder b = new AlertDialog.Builder(TripsActivity.this);
				b.setMessage(_exception.getMessage());
				b.setCancelable(true);
				b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				});
				b.show();
			}
		}
	}
	
	private class ExportTripTask extends AsyncTask<Uri, Integer, Integer>
	{
		private ProgressDialog _progressDialog;
		
		@Override
		protected void onPreExecute()
		{
			// present spinning progress wheel
	        _progressDialog = ProgressDialog.show(TripsActivity.this, "Please wait", "saving trip...");
		}

		@Override
		protected Integer doInBackground(Uri... params)
		{
			try
			{
				TripExporter exporter = new TripExporter(TripsActivity.this, _trip, FileUtils.getFile(params[0]));
				exporter.writeTrip();
				exporter.close();
				return 0;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return -1;
			}
		}
		
		@Override
		protected void onPostExecute(Integer result)
		{
			if (_progressDialog != null && _progressDialog.isShowing())
			{
				_progressDialog.dismiss();
			}
		}
	}

    private void setTextView(View root, int resId, String value)
	{
		TextView view = (TextView)root.findViewById(resId);
		view.setText(value);
	}

    private void setImageViewBackgroundResource(View root, int id, int resId)
	{
		ImageView imageView = (ImageView)root.findViewById(id);
		imageView.setBackgroundResource(resId);
	}

	private class MyAdapter extends BaseAdapter
	{
		private LayoutInflater _inflater;
		
		public MyAdapter()
		{
			_inflater = LayoutInflater.from(TripsActivity.this);
		}

		public int getCount() 
		{
			if (_trip != null)
			{
				return _trip.getCount();
			}
			else
			{
				return 0;
			}
		}

		public Object getItem(int position)
		{
			return position;
		}

		public long getItemId(int position)
		{
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			_trip.moveToPosition(position);
			convertView = _inflater.inflate(R.layout.trip_list_item, null);
			setTextView(convertView, R.id.name, _trip.getName());
			String description = _trip.getDescription();
			if (description != null)
			{
				String[] parts = _trip.getDescription().split("<br/>");
				setTextView(convertView, R.id.description, Html.fromHtml(parts[0]).toString());
			}
			setImageViewBackgroundResource(convertView, R.id.icon, R.drawable.blue_marker_t);
			return convertView;
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
			try
			{
				if (_trip != null)
				{
					_trip.requery();
//					TripsActivity.this.runOnUiThread(new Runnable() {
//						@Override
//						public void run() {
							_adapter.notifyDataSetChanged();
//						}
//					});
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
