package com.futureconcepts.jupiter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.futureconcepts.jupiter.R;
import com.futureconcepts.jupiter.data.EmptyCursor;
import com.futureconcepts.jupiter.data.Folder;
import com.futureconcepts.jupiter.data.Placemark;

public class ViewFolderActivity extends Activity
{
	private Uri _uri;
	private MyAdapter _adapter;
	private Folder _topFolder;
	private Folder _folders;
	private Placemark _placemarks;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setTheme(android.R.style.Theme_Black);
		setContentView(R.layout.folder_list);
		_uri = getIntent().getData();
		_topFolder = Folder.getFolder(this, _uri);
		if (_topFolder != null)
		{
			_folders = new Folder(managedQuery(Folder.CONTENT_URI, null, Folder.PARENT_ID + "='" + _topFolder.getId() + "'", null, null));
			_placemarks = new Placemark(managedQuery(Placemark.CONTENT_URI, null, Placemark.PARENT_ID + "='" + _topFolder.getId() + "'", null, null));
		}
		else
		{
			_topFolder = new Folder(new EmptyCursor());
			_folders = new Folder(new EmptyCursor());
			_placemarks = new Placemark(new EmptyCursor());
		}
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
		if (_topFolder != null)
		{
			_topFolder.moveToFirst();
			setTitle(_topFolder.getName());
			setTextView(R.id.name, _topFolder.getName());
		}
        if (_adapter == null)
        {
			ListView listView = (ListView)findViewById(R.id.list);
			_adapter = new MyAdapter();
			listView.setAdapter(_adapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
				{
					if (position < _folders.getCount())
					{
						_folders.moveToPosition(position);
						Intent intent = new Intent(ViewFolderActivity.this, ViewFolderActivity.class);
						intent.setData(Uri.withAppendedPath(Folder.CONTENT_URI, _folders.get_ID()));
						startActivity(intent);
					}
					else
					{
						_placemarks.moveToPosition(position - _folders.getCount());
						Intent intent = new Intent(ViewFolderActivity.this, ViewPlacemarkActivity.class);
						intent.setData(Uri.withAppendedPath(Placemark.CONTENT_URI, _placemarks.get_ID()));
						startActivity(intent);
					}
				}
			});
		}
        else
        {
        	_adapter.notifyDataSetChanged();
        }
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.view_folder_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_edit:
			onMenuEdit();
			break;
		case R.id.menu_delete:
			onMenuDelete();
			break;
		case R.id.menu_show_on_map:
			onMenuShowOnMap();
			break;
		}
		return false;
	}

	private void onMenuEdit()
	{
		Intent intent = new Intent(this, EditFolderActivity.class);
		intent.setData(_uri);
		startActivity(intent);
	}
	
	private void onMenuDelete()
	{
    	try
    	{
			if (_uri != null)
			{
				getContentResolver().delete(_uri, null, null);
			}
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    }
        
    private void onMenuShowOnMap()
    {
		Intent intent = new Intent(this, MainMapActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(_uri);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
    }
    
    private void setTextView(int resId, String value)
	{
		TextView view = (TextView)findViewById(resId);
		view.setText(value);
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
			_inflater = LayoutInflater.from(ViewFolderActivity.this);
		}

		public int getCount() 
		{
			return (_folders.getCount() + _placemarks.getCount());
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
			if (position < _folders.getCount())
			{
				return getFoldersView(position, convertView, parent);
			}
			else
			{
				return getPlacemarksView(position - _folders.getCount(), convertView, parent);
			}
		}

		private View getFoldersView(int position, View convertView, ViewGroup parent)
		{
			_folders.moveToPosition(position);
			convertView = _inflater.inflate(R.layout.folder_list_item, null);
			setTextView(convertView, R.id.name, _folders.getName());
			setTextView(convertView, R.id.description, _folders.getDescription());
			setImageViewBackgroundResource(convertView, R.id.icon, R.drawable.blue_marker_r);
			return convertView;
		}

		private View getPlacemarksView(int position, View convertView, ViewGroup parent)
		{
			_placemarks.moveToPosition(position);
			convertView = _inflater.inflate(R.layout.placemark_list_item, null);
			setTextView(convertView, R.id.name, _placemarks.getName());
			setTextView(convertView, R.id.description, _placemarks.getDescription());
			setImageViewBackgroundResource(convertView, R.id.icon, R.drawable.blue_marker_r);
			return convertView;
		}
	}
}
