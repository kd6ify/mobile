package com.futureconcepts.mercury.update;

import java.io.IOException;
import java.io.StringWriter;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import com.futureconcepts.mercury.Config;
import com.futureconcepts.mercury.R;
import com.futureconcepts.mercury.download.Downloads;
import com.futureconcepts.mercury.download.Downloads.ByUri;
import com.futureconcepts.mercury.sync.TransactionException;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class CheckUpdateActivity extends ListActivity
{
	private static final String TAG = CheckUpdateActivity.class.getSimpleName();
	
	private static final String UPDATE_SET_TAG = "ArrayOfKeyValueOfstringArrayOfVersionedPackage";
	private static final String UPDATE_ACTION_TAG = "KeyValueOfstringArrayOfVersionedPackage";
	private static final String TAG_KEY = "Key";
	private static final String TAG_VALUE = "Value";
	
	private List<VersionedPackage> _actions;
	private MyAdapter _adapter;

	private ProgressDialog _progressDialog;

	private Config _config;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_update);
        _config = Config.getInstance(this);
        hookupUpdateNowButton();
        hookupCancelButton();
        _adapter = new MyAdapter();
        setListAdapter(_adapter);
		try
		{
	        _progressDialog = ProgressDialog.show(this, "Please wait...", "Checking for updates");
	        new CheckUpdateAsyncTask().execute();
//	        CheckUpdateAsyncTask t = new CheckUpdateAsyncTask();
//	        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//	        factory.setNamespaceAware(true);
//	        XmlPullParser p = factory.newPullParser();
//	        InputStream stream = getAssets().open("updates.xml");
//	        p.setInput(stream, null);
//	        t.processResponse(p);
//	        t.onPostExecute(null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	if(_progressDialog!=null)
    	{
    		_progressDialog.dismiss();
    		_progressDialog=null;
    	}
    }
    
    private void hookupUpdateNowButton()
    {
	    final View view = findViewById(R.id.btn_update_now);
	    if (view != null)
	    {
		    view.setEnabled(false);
		    view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v)
				{
					view.setEnabled(false);
					findViewById(R.id.btn_cancel).setEnabled(false);
					onMenuUpdateNow();
				}
		    });
	    }
    }
    
    private void hookupCancelButton()
    {
    	final View view = findViewById(R.id.btn_cancel);
    	if (view != null)
    	{
		    view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v)
				{
					finish();
				}
		    });
    	}
    }
        
	private void onMenuUpdateNow()
	{
		if (_actions != null)
		{
			VersionedPackage[] packages = new VersionedPackage[_actions.size()];
			int i = 0;
			for (VersionedPackage thePackage : _actions)
			{
				packages[i++] = thePackage;
			}
			new DownloadUpdateAsyncTask().execute(packages);
		}
	}

	public void onError(String message)
	{
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("Error getting data");
		ab.setMessage(message);
		ab.setNeutralButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				finish();
			}
		});
		AlertDialog dialog = ab.create();
		try{
			dialog.show();
		}catch(Exception e)
		{
			e.printStackTrace();
			 // WindowManager$BadTokenException
		}
	}

	private final class MyAdapter extends BaseAdapter
	{
		public MyAdapter()
		{
			
		}
		public int getCount() 
		{
			if (_actions != null)
			{
				return _actions.size();
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

		@Override
		public boolean isEnabled(int position)
		{
			return false;
		}
		
		public View getView(int position, View convertView, ViewGroup parent)
		{
			VersionedPackage vp = _actions.get(position);
			View view = getLayoutInflater().inflate(R.layout.view_item_list_item, null);
			setTextView(view, R.id.name, vp.action);
			setTextView(view, R.id.value, vp.friendlyName);
			return view;
		}
		
		private void setTextView(View root, int resId, String text)
		{
			TextView view = (TextView)root.findViewById(resId);
			view.setText(text);
		}
	}
	
	private final class CheckUpdateAsyncTask extends AsyncTask<Void, Integer, List<VersionedPackage>>
	{
		private MyHttpClient _client;
		private String _errorMessage;
		private ArrayList<VersionedPackage> _computedActions = new ArrayList<VersionedPackage>();

		@Override
		protected List<VersionedPackage> doInBackground(Void... arg0)
		{
			List<VersionedPackage> result = null;
			try
			{
				String installedPackages = getInstalledPackages();
				_client = new MyHttpClient(CheckUpdateActivity.this);
				doServerTransaction(installedPackages);
				result = _computedActions;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				_errorMessage = e.getMessage();
			}
			return result;
		}

		@Override
		protected void onProgressUpdate(Integer... progress)
		{
		}

		@Override
		protected void onPostExecute(List<VersionedPackage> result)
		{
			_actions = _computedActions;
			if (_actions.size() > 0)
			{
				findViewById(R.id.btn_update_now).setEnabled(true);
			}
			_adapter.notifyDataSetChanged();
			if(_progressDialog!=null){
				_progressDialog.dismiss();
			}
			if (_errorMessage != null)
			{
				onError(_errorMessage);
			}
	    }
				
		private String getInstalledPackages()
		{
			String result = null;
			PackageManager packageManager = getPackageManager();
			List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
			StringWriter w = new StringWriter();
			XmlSerializer g = Xml.newSerializer();
			try
			{
				g.setOutput(w);
			}
			catch (IllegalArgumentException e1)
			{
				e1.printStackTrace();
			}
			catch (IllegalStateException e1)
			{
				e1.printStackTrace();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
			try
			{
				ArrayList<PackageInfo> consideredPackageInfos = new ArrayList<PackageInfo>();
				for (PackageInfo packageInfo : packageInfos)
				{
					if (packageInfo.packageName.contains("com.futureconcepts"))
					{
						consideredPackageInfos.add(packageInfo);
					}
				}
				VersionedPackages vp= new VersionedPackages(consideredPackageInfos);
				vp.deviceName = _config.getDeviceName().replace("'", "");
				vp.osFamily = "Android";
				vp.osDescription = "Android";
				vp.osMajorVersion = android.os.Build.VERSION.SDK_INT;
				vp.serialize(g);
				g.flush();
				result = w.toString();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			return result;
		}
		
		private void doServerTransaction(String content) throws ClientProtocolException, IOException, IllegalArgumentException, HttpException, TransactionException, IllegalStateException, XmlPullParserException
		{
			String myEquipmentId = _config.getMyEquipmentId();
			String serverUrl = String.format("%s/WSUSBridge/client/%s/updates", _config.getWsusServiceAddress(), myEquipmentId);
			HttpPut put = new HttpPut(serverUrl);
			put.addHeader("Content-Type", "text/xml; charset=utf-8");
			put.getParams().setIntParameter("http.socket.timeout", 60 * 1000 * 20); // 20 minute
			put.addHeader("DeviceId", _config.getDeviceId());
			if (content != null)
			{
				put.setEntity(new StringEntity(content));
			}
			Log.i(TAG, put.getRequestLine().toString());
			HttpResponse response = _client.doExecute(put);
			HttpEntity ent = response.getEntity();
			if (ent != null)
			{
				Header responseContentType = ent.getContentType();
				if (responseContentType != null)
				{
					XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
					factory.setNamespaceAware(true);
					XmlPullParser p = factory.newPullParser();
					p.setInput(ent.getContent(), null);
					processResponse(p);
				}
				ent.consumeContent();
			}
			StatusLine statusLine = response.getStatusLine();
			Log.i(TAG, statusLine.toString());
			int statusCode = statusLine.getStatusCode();
			if (statusCode != 200)
			{
				 // handle gateway timeout (504) and temp unavailable (503) like a socket exception
				// socket exceptions are auto retried
				if (statusCode == 504 || statusCode == 503)
				{
					throw new SocketException();
				}
				else
				{
					throw new HttpException(statusLine.toString());
				}
			}
		}
		
		private void processResponse(XmlPullParser p) throws XmlPullParserException, IOException
		{
			p.nextToken();
			if (p.getEventType() == XmlPullParser.START_TAG && p.getName().contains(UPDATE_SET_TAG))
			{
				processUpdateSet(p);
			}
		}
		
		private void processUpdateSet(XmlPullParser p) throws XmlPullParserException, IOException
		{
			while (p.nextToken() != XmlPullParser.END_TAG)
			{
				if (p.getName().contains(UPDATE_ACTION_TAG))
				{
					processUpdateAction(p);
				}
			}
		}
		
		private void processUpdateAction(XmlPullParser p) throws NumberFormatException, ParseUpdateException, XmlPullParserException, IOException
		{
			String action = null;
			VersionedPackage versionedPackage = null;
			while (p.nextToken() != XmlPullParser.END_TAG)
			{
				if (p.getName().equals(TAG_KEY))
				{
					action = p.nextText();
				}
				else if (p.getName().equals(TAG_VALUE))
				{
					while (p.nextToken() != XmlPullParser.END_TAG)
					{
						if (p.getName().equals(VersionedPackage.TAG_VERSIONED_PACKAGE))
						{
							versionedPackage = new VersionedPackage(p);
							versionedPackage.action = action;
							_computedActions.add(versionedPackage);
						}
					}
				}
			}
		}
	}
	
	private final class DownloadUpdateAsyncTask extends AsyncTask<VersionedPackage, Integer, Integer>
	{
		@Override
		protected Integer doInBackground(VersionedPackage... packages)
		{
			Integer result = null;
			try
			{
				for (VersionedPackage vp : packages)
				{
					applyAction(vp);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return result;
		}

		private void applyAction(VersionedPackage vp)
		{
			if (vp.action.equals("INSTALL"))
			{
				applyInstall(vp);
			}
			else if (vp.action.equals("UNINSTALL"))
			{
				applyUninstall(vp);
			}
		}

		private void applyInstall(VersionedPackage vp)
		{
			String url = String.format("%s/WSUSBridge/package/%s", _config.getWsusServiceAddress(), vp.packageId);
			ByUri.startDownloadByUri(CheckUpdateActivity.this,
	                url,
	                null,	// cookie
	                true,	// show download
	                Downloads.DOWNLOAD_DESTINATION_EXTERNAL, // download to SDCARD
	                true,	// allow roaming
	                false,	// skipIntegrityCheck,
	                vp.friendlyName,
	                "com.futureconcepts.mercury", // DownloadCompleteReceiver package...
	                "com.futureconcepts.mercury.update.DownloadCompleteReceiver", // ...and class
	                null // extras
	                );
		}
		
		private void applyUninstall(VersionedPackage vp)
		{
			Uri packageUri = Uri.parse(String.format("package:%s", vp.packageName));
			Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
			startActivity(uninstallIntent);
		}

		@Override
		protected void onProgressUpdate(Integer... progress)
		{
		}

		@Override
		protected void onPostExecute(Integer result)
		{
			Toast.makeText(CheckUpdateActivity.this, "Updates are scheduled to be downloaded", Toast.LENGTH_LONG).show();
			finish();
	    }
	}	
}