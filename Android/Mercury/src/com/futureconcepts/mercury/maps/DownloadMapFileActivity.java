package com.futureconcepts.mercury.maps;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.w3c.util.InvalidDateException;

import com.futureconcepts.mercury.Config;
import com.futureconcepts.mercury.R;
import com.futureconcepts.mercury.main.DownloadConfigurationActivity;
import com.futureconcepts.mercury.maps.DownloadMapFileService.DonwloadMapFileNotifier;
import com.futureconcepts.mercury.maps.ServerRequest.ServerRequestAddParameters;
import com.futureconcepts.mercury.sync.TransactionException;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadMapFileActivity extends ListActivity implements ServerRequestAddParameters, DonwloadMapFileNotifier {

    private static final String TAG = DownloadMapFileActivity.class.getSimpleName();
    private List<String> mapFilesList = new ArrayList<String>();
    private MapAdapter mapAdapter;
    private String rootLocalDirectoryFolderPathForMaps = 
			Environment.getExternalStorageDirectory()+"/Trinity/Maps/";
    private final int MAP_DOWNLOADED = 0;
    private final  int MAP_PARTIALLY_DOWNLOADED=1;
    private final  int MAP_NEW = 2;
    private String  currentDownloadingMapName="";
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download_map_file);
	}	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		DownloadMapFileService.removeNotifier();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(!isServerSettingsAvailable())
		{
			showDownloadConfiguration();
		}else
		{
			new LoadMapFiles().execute();
			DownloadMapFileService.setNotifier(this);
		}
	}
	
	private void showDownloadConfiguration()
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Information");
		alert.setMessage("You are missing some configuaration values. " +
						"Please select Download Configuration");
		alert.setCancelable(true);
		alert.setPositiveButton("Download Configuration", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	            dialog.cancel();
	            startActivity(new Intent(DownloadMapFileActivity.this, DownloadConfigurationActivity.class));
	        }
	    });
		alert.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	            dialog.cancel();
	        }
	    });
		alert.show();
	}
	
	private boolean isServerSettingsAvailable ()
	{
		Config _config = Config.getInstance(this);
	    if(_config.getMediaImagesServerAddress()!=null &&
	    		_config.getMediaImagesServerUser()!=null &&
	    		_config.getMediaImagesServerPassword()!=null){
	    	return true;
	    }
	    return false;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		if(currentDownloadingMapName.equals(mapFilesList.get(position))){
			Toast.makeText(getApplicationContext(), mapFilesList.get(position)+" in process", Toast.LENGTH_SHORT).show();
		}else
		{
			showOptionsForMapItem(position);
		}
	}
	
	
	private final class LoadMapFiles extends AsyncTask<Void, Integer, Exception>
	{
		private ProgressDialog _loadOverlayDataProgressDialog;
		
		@Override
		protected void onPreExecute()
		{
			mapFilesList.clear();
			_loadOverlayDataProgressDialog = ProgressDialog.show(DownloadMapFileActivity.this, "Loading", "Loading data...", true, false);
		}
		
	    protected Exception doInBackground(Void... params)
	    {
	    	Exception result = null;
	    	try
	    	{
	    		if(isNetworkAvailable(getApplicationContext())){
	    			ServerRequest serverRequest = new ServerRequest(ServerRequest.ACTION_GET_MAP_FILES,DownloadMapFileActivity.this);
	    			parse(serverRequest.madeRequestToServer(DownloadMapFileActivity.this));//getMapFilesFromServer();
	    		}else
	    		{
	    			mapFilesList.add("No internet conection found.");
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
	    	 _loadOverlayDataProgressDialog.dismiss();
	    	// ArrayAdapter<String> adapter = new ArrayAdapter<String>(DownloadMapFileActivity.this,
			//	        android.R.layout.simple_list_item_1, mapFilesList);
	    	 mapAdapter = new MapAdapter();
			 getListView().setAdapter(mapAdapter);
	     }
	}
	
	
	public void parse(JsonParser p) throws JsonParseException, IOException, InvalidDateException, TransactionException
	{
		p.nextToken();
		while (p.nextToken() != JsonToken.END_OBJECT)
		{
			String name = p.getCurrentName();
			if (name.equals("result"))
			{
				ignoreValue(p);
			}
			else if (name.equals("values"))
			{
				parseItems(p);
			}
			else if (name.equals("error"))
			{
				ignoreValue(p);
				throw new TransactionException(p.getText());
			}
			else
			{
				ignoreValue(p);
			}			
		}
	}
	
	
	public void parseItems(JsonParser p) throws JsonParseException, IOException, InvalidDateException
	{
		if(p.getCurrentToken() == JsonToken.START_OBJECT)
		{
			while (p.nextToken() != JsonToken.END_OBJECT)
			{
				if (p.getCurrentToken() == JsonToken.FIELD_NAME)
				{
					String name = p.getCurrentName();
					p.nextToken();
					if (name.contains("Item"))
					{
						String a = p.getText();
						mapFilesList.add(a);
					}
					else
					{
						ignoreValue(p);
					}
				}
			}			
		}
	}
	
	private void ignoreValue(JsonParser p) throws JsonParseException, IOException
	{
		p.nextToken();
		Log.d(TAG, p.getText());
		if (p.getCurrentToken() == JsonToken.START_OBJECT)
		{
			while (p.nextToken() != JsonToken.END_OBJECT)
			{
			}
		}
	}
	
	public void getListFiles(View view)
	{
		if(!isServerSettingsAvailable())
		{
			showDownloadConfiguration();
		}else
		{
			new LoadMapFiles().execute();
		}		
	}
	
	/**
	 * 
	 * @param context: context of the activity.
	 * @return true: if has connection to Internet and is connected.
	 */
	public static  boolean isNetworkAvailable(Context context) {
	    ConnectivityManager cm = (ConnectivityManager) 
	      context.getSystemService(Context.CONNECTIVITY_SERVICE);
	     NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    // if no network is available networkInfo will be null
	    // otherwise check if we are connected
	    if (netInfo != null && netInfo.isConnected()) {
	        return true;
	    }
	    return false;
	}
	
	/**
	 * 
	 * @param context: context of the activity.
	 * @return true: if the connection is WIFI.
	 */
	public static  boolean isNetworkWIFI(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (mWifi.isConnected()) {
		    return true;
		}	    
	    return false;
	}

	@Override
	public void addParameters(HttpPost post) {
		// TODO Auto-generated method stub			
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();	
		nameValuePairs.add(new BasicNameValuePair("action", ServerRequest.ACTION_GET_MAP_FILES));
		try {
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	@Override
	public void downloadFileServiceStart(final String mapName) {
		// TODO Auto-generated method stub
				final View container = (View)findViewById(R.id.ProgressContainer);	
				final TextView txt = (TextView)container.findViewById(R.id.progressText);
				final ProgressBar progressBar = (ProgressBar)container.findViewById(R.id.downloadProgress);
				if(txt!=null)
				txt.post(new Runnable() {
					@Override
					public void run() {
								if(txt!=null && container!=null && progressBar!= null)
								container.setVisibility(View.VISIBLE);
								txt.setVisibility(View.VISIBLE);
							    txt.setText("Downloading map: "+mapName+ "\n0%");
							    progressBar.setVisibility(View.VISIBLE);
							    progressBar.setProgress(0);							    
						
					}
				});	    
				currentDownloadingMapName = mapName;
	}

	@Override
	public void downloadProgress(final int progress, final String mapName) {
		// TODO Auto-generated method stub
		final View container = (View)findViewById(R.id.ProgressContainer);	
		final TextView txt = (TextView)container.findViewById(R.id.progressText);
		final ProgressBar progressBar = (ProgressBar)container.findViewById(R.id.downloadProgress);
		if(txt!=null)
		txt.post(new Runnable() {
			@Override
			public void run() {
				if(progress<=100)
				{
					if(txt!=null && container!=null && progressBar!= null)
						container.setVisibility(View.VISIBLE);
						txt.setVisibility(View.VISIBLE);
					    txt.setText("Downloading map: "+mapName+ "\n"+progress+"%");
					    progressBar.setVisibility(View.VISIBLE);
					    progressBar.setProgress(progress);							    
				}
			}
		});	   
		currentDownloadingMapName = mapName;
	}

	@Override
	public void downloadFail(final String errorMessage, final String mapName) {
		// TODO Auto-generated method stub
		final View container = (View)findViewById(R.id.ProgressContainer);	
		final TextView txt = (TextView)container.findViewById(R.id.progressText);
		final ProgressBar progressBar = (ProgressBar)container.findViewById(R.id.downloadProgress);
		if(txt!=null)
		txt.post(new Runnable() {
			@Override
			public void run() {
					if(container!=null)
						container.setVisibility(View.VISIBLE);
						txt.setVisibility(View.VISIBLE);
					    txt.setText("Fail to download "+mapName+":\n"+errorMessage);
					    progressBar.setVisibility(View.GONE);	
					    mapAdapter.notifyDataSetChanged();
			}
			
		});	    
	}

	@Override
	public void downloadComplete(final String mapName) {
		// TODO Auto-generated method stub
		final View container = (View)findViewById(R.id.ProgressContainer);	
		final TextView txt = (TextView)container.findViewById(R.id.progressText);
		final ProgressBar progressBar = (ProgressBar)container.findViewById(R.id.downloadProgress);
		if(txt!=null)
		txt.post(new Runnable() {
			@Override
			public void run() {
					currentDownloadingMapName ="";
					if(container!=null)
						container.setVisibility(View.VISIBLE);
						txt.setVisibility(View.VISIBLE);
					    txt.setText("File map successfully downloaded: "+mapName);
					    progressBar.setVisibility(View.GONE);
					    mapAdapter.notifyDataSetChanged();						    
			}
			
		});	    
	}
	
	private void downloadMapFile(final int position)
	{
		if(isNetworkAvailable(DownloadMapFileActivity.this))
		{
			if(isNetworkWIFI(DownloadMapFileActivity.this)){
				requestMap(position);
			}else{
				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setTitle("Information");
				alert.setMessage("No WIFI connection detected. Do you want to download this" +
						"file over cellular data?");
				alert.setCancelable(true);
				alert.setPositiveButton("Download", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) {
			        	requestMap(position);
			        }
			    });
				alert.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) {
			            dialog.cancel();
			        }
			    });
				alert.show();
			}
		}
	
	}
	
	private void requestMap(int position)
	{
		Intent getMap = new Intent (this, DownloadMapFileService.class);
		getMap.putExtra("mapFile",mapFilesList.get(position));
		startService(getMap);		
	
	}
	
	private void deleteFileMap(int position)
	{
		boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if (isSDPresent) {
			String filePath = null;
			int status = getMapFileStatus(position);
			if(status== MAP_DOWNLOADED)
			{
				filePath =  rootLocalDirectoryFolderPathForMaps+mapFilesList.get(position);
			}else if (status== MAP_PARTIALLY_DOWNLOADED)
			{
				filePath = rootLocalDirectoryFolderPathForMaps+"TEMP"+mapFilesList.get(position);
			}
			File mapFile = new File(filePath);
			if(mapFile.exists())
			{
				mapFile.delete();//delete the file because is corrupted	
				mapFile = null;
				Toast.makeText(getApplicationContext(), "Map file successfully deleted.", Toast.LENGTH_SHORT).show();
				
			}else
			{
				Toast.makeText(getApplicationContext(), "Cannot delete file: file not found.", Toast.LENGTH_SHORT).show();	
			}
		
		}else
		{
			Toast.makeText(getApplicationContext(), "SD card is not available", Toast.LENGTH_SHORT).show();
		}
	}
	
	private int getMapFileStatus(int position){
		if(new File(rootLocalDirectoryFolderPathForMaps+mapFilesList.get(position)).exists()){
			return MAP_DOWNLOADED;
		}
		else if(new File(rootLocalDirectoryFolderPathForMaps+"TEMP"+mapFilesList.get(position)).exists())
		{
			return MAP_PARTIALLY_DOWNLOADED;
		}		
		return MAP_NEW;
	}
	
	private String[] getOptiosForFile(int position){
		switch(getMapFileStatus(position))
		{
		case MAP_DOWNLOADED:
				return new String[] {"Delete File"};
		case MAP_PARTIALLY_DOWNLOADED:
				return new String[] {"Download","Delete File"};
		default:
				return new String[] {"Download"};
		}		
	}
	
	private void showOptionsForMapItem(final int position){
		final String [] a = getOptiosForFile(position);
		AlertDialog.Builder mapItemOptions = new AlertDialog.Builder(this);
		mapItemOptions.setTitle("Actions for this item.");
		mapItemOptions.setCancelable(true);
		mapItemOptions.setItems(a, new DialogInterface.OnClickListener() {

	        public void onClick(DialogInterface dialog, int which) {
	            if(a[which]=="Download")
	            {
	            	downloadMapFile(position);
	            }else if(a[which]=="Delete File")
	            {
	            	deleteFileMap(position);
	            }
	            mapAdapter.notifyDataSetChanged();
	        }
	    });
//	     onlineUser.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//
//	        public void onClick(DialogInterface dialog, int which) {
//	            dialog.cancel();
//
//	        }
//	    });
		mapItemOptions.show();
	 }

	private class MapAdapter extends BaseAdapter
	{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mapFilesList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mapFilesList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if(convertView==null){
				convertView = getLayoutInflater().inflate(R.layout.download_map_list_item, null);
			}			
			((TextView)convertView.findViewById(R.id.fileText)).setText(mapFilesList.get(position));
			if (!mapFilesList.get(position).contains("internet")) {
				File completeFile = new File(rootLocalDirectoryFolderPathForMaps+ mapFilesList.get(position));// rootLocalDirectoryFolderPathForMaps
				File partially = new File(rootLocalDirectoryFolderPathForMaps+"TEMP"+mapFilesList.get(position));
					TextView statusText = (TextView) convertView.findViewById(R.id.statusText);
				if (completeFile.exists()) {
					statusText.setText("Downloaded");
					statusText.setTextColor(Color.GREEN);
				} else if (partially.exists()) {
					statusText.setText("Partially Downloaded");
					statusText.setTextColor(Color.YELLOW);
				} else {
					statusText.setText("New");
					statusText.setTextColor(Color.WHITE);
				}
				completeFile = null;
				partially = null;
			} else {
				((TextView) convertView.findViewById(R.id.statusText)).setVisibility(View.GONE);
				((TextView) convertView.findViewById(R.id.Name)).setVisibility(View.GONE);
				((TextView) convertView.findViewById(R.id.statusName)).setVisibility(View.GONE);
			}
			 return convertView;
		}
		
	}
	
}
