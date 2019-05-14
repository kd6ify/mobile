package com.futureconcepts.ax.video.viewer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class SourceListActivity extends ListActivity implements OnSharedPreferenceChangeListener
{
	private static final String TAG = SourceListActivity.class.getSimpleName();

	private int _nextRow = 1;
	private MatrixCursor _sources;
	private MyAdapter _adapter;
	private Bundle _savedInstanceState;
	private LoadSourcesAsyncTask _loadSourcesAsyncTask;
	private SharedPreferences _sharedPreferences;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.source_list);
        _savedInstanceState = savedInstanceState;
		_adapter = new MyAdapter();
        setListAdapter(_adapter);
    	_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    	invalidateList();
    	_sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume()
    {
    	super.onResume();
    	Log.d(TAG, "onResume");
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    	Log.d(TAG, "onPause");
    }
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	Log.d(TAG, "onDestroy");
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.source_list_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
    	return super.onPrepareOptionsMenu(menu);
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			break;
		}
		return false;
	}
		
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		if (_sources != null)
		{
			_sources.moveToPosition(position);
			//Log.i(TAG, "onListItemClick " + _journal.getName());
			String streamId = _sources.getString(1);
			String name = _sources.getString(2);
			String description = _sources.getString(3);
			if (streamId != null)
			{
				try
				{
//					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					Intent intent = new Intent(this, AuthenticateStreamActivity.class);
					intent.setAction(Intent.ACTION_VIEW);
					intent.putExtra("ID", streamId);
					intent.putExtra("Name", name);
					intent.putExtra("Description", description);
					startActivity(intent);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public Context getContext()
	{
		return this;
	}
	
	private void invalidateList()
	{
		new LoadSourcesAsyncTask().execute();
		Log.d(TAG, "invalidateList");
	}
	
	private void onError(String message)
	{
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("Select Incident");
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
	
	private void parseGroups(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		int eventType = xpp.getEventType();
		if (eventType == XmlPullParser.START_DOCUMENT)
		{
			while (eventType != XmlPullParser.END_DOCUMENT)
			{
				if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("SourceGroups"))
				{
					while (eventType != XmlPullParser.END_TAG)
					{
						if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("SourceGroup"))
						{
							parseGroup(xpp);
						}
						eventType = xpp.next();
					}
				}
				eventType = xpp.next();
			}
		}
	}
	
	private void parseGroup(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_TAG)
		{
			if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("StreamSourceInfo"))
			{
				parseStream(xpp);
			}
			eventType = xpp.next();
		}
	}

	private int parseStream(XmlPullParser xpp) throws XmlPullParserException, IOException
	{
		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_TAG)
		{
			String id = xpp.getAttributeValue(null, "StreamID");
			String name = xpp.getAttributeValue(null, "Name");
			String description = xpp.getAttributeValue(null, "Description");
	        _sources.addRow(new Object[] { _nextRow++, id, name, description } );
	        eventType = xpp.next();
		}
        return xpp.getEventType();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if (key.equals(Settings.KEY_WEB_SERVICE_ADDRESS))
		{
			Log.d(TAG, "onSharedPreferenceChanged web_service_address");
//			invalidateList();
		}
	}
	
	private class MyAdapter extends ResourceCursorAdapter
	{
	    public MyAdapter()
		{
			super(SourceListActivity.this, R.layout.source_list_item, _sources, 0);
		}

		@Override
		public void bindView(View view,Context context, Cursor c)
		{
			try
			{
//				((TextView)view.findViewById(R.id.date)).setText(getFormattedLocalTime(journal.getTime(), "no start"));
				((TextView)view.findViewById(R.id.text1)).setText(c.getString(2));
				//get all media for the Journal
				//Get All media For journal in a different thread.
//			    ((TextView)view.findViewById(R.id.num_images)).post(new Runnable() {
//			        public void run() {
//			        	int total = getTotalMediaOfJournal(context,journal.getID());				
//					    ((TextView)view.findViewById(R.id.num_images)).setText(""+total);
//			        }
//			    });
			  //  Log.d("ViewJournal","I get This value of media: "+ total);
			  
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private final class LoadSourcesAsyncTask extends AsyncTask<Void, Integer, Exception>
	{
		private ProgressDialog _loadSourcesProgressDialog;
		
		@Override
		protected void onPreExecute()
		{
			_loadSourcesProgressDialog = ProgressDialog.show(SourceListActivity.this, "Loading", "Loading data...", true, false);
			if (_sources != null)
			{
				_sources.close();
			}
	        _sources = new MatrixCursor(new String[] { "_ID", "ID", "Name", "Description" });
	        _adapter.changeCursor(_sources);
		}
		
		@Override
	    protected Exception doInBackground(Void... params)
	    {
	    	Exception result = null;
	        try
	        {
	        	String webServiceAddress = Settings.getWebServiceAddress(getContext());
	        	String webServiceUrlString = String.format("http://%s:8080/index?DeviceID=%s", webServiceAddress, Settings.getDeviceId(getContext()));
	        	URL url = new URL(webServiceUrlString);
	        	HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
//	        	urlConnection.setConnectTimeout(3000);
	        	InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				XmlPullParser xpp = factory.newPullParser();
				xpp.setInput(stream, null);
				parseGroups(xpp);
		        
				urlConnection.disconnect();
			}
	        catch (IOException e)
	        {
				e.printStackTrace();
				result = e;
			}
	        catch (XmlPullParserException e)
	        {
	        	e.printStackTrace();
	        	result = e;
	        }
	    	return result;
	     }
	    
//	     protected void onProgressUpdate(Integer... progress)
//	     {
//	         setProgressPercent(progress[0]);
//	     }

	     protected void onPostExecute(Exception result)
	     {
	    	 if (_loadSourcesProgressDialog != null)
	    	 {
				_loadSourcesProgressDialog.dismiss();
				_loadSourcesProgressDialog = null;
	    	 }
	    	 if (result == null && isFinishing() == false)
	    	 {
	    		 _adapter.notifyDataSetChanged();
	    	 }
	     }
	}
}
