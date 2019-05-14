package com.futureconcepts.mercury.sync;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.futureconcepts.mercury.R;

public class ShowSyncUpdateActivity extends ListActivity
{
	private static final String TAG = "ShowSyncUpdateActivity";
	
	private Intent mIntent;
	
	private ArrayList<Entry<String, Object>> mChangedColumns = new ArrayList<Entry<String, Object>>();
	
	private MyAdapter mAdapter;
			
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_sync_update);
        mIntent = getIntent();
        String message = mIntent.getStringExtra("message");
        if (message != null)
        {
        	((TextView)findViewById(R.id.message)).setText(message);
        }
        String name = mIntent.getStringExtra("name");
        {
        	((TextView)findViewById(R.id.name)).setText(name);
        }
        ((Button)findViewById(R.id.view)).setOnClickListener(new OnClickListener() {
			public void onClick(View v)
            {
				onViewClick(getIntent().getData());
            }
        });
        ((Button)findViewById(R.id.ok)).setOnClickListener(new OnClickListener() {
			public void onClick(View v)
            {
				onGoBackClick();
            }
        });
        ContentValues values = getIntent().getParcelableExtra("deltaValues");
        Log.i(TAG, "deltaValues: " + values.toString());
        Iterator<Entry<String, Object>> iter = values.valueSet().iterator();
        while (iter.hasNext())
        {
        	mChangedColumns.add(iter.next());
        }
        mAdapter = new MyAdapter();
        setListAdapter(mAdapter);
    }
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    }

    private void onViewClick(Uri data)
    {
    	try
    	{
	    	Intent viewIntent = new Intent(Intent.ACTION_VIEW, data);
	    	startActivity(viewIntent);
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    }
    
    private void onGoBackClick()
    {
    	finish();
    }
    
	public class MyAdapter extends BaseAdapter
	{
		private SimpleDateFormat mDateFormat;
		private LayoutInflater mInflater;
		
		public MyAdapter()
		{
//			mBitmapDictionary = new BitmapDictionary(Intel.Status.CONTENT_URI, mStatusCursor);
			mDateFormat = new SimpleDateFormat();
			mInflater = LayoutInflater.from(ShowSyncUpdateActivity.this);
		}

		public int getCount() 
		{
			return mChangedColumns.size();
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
			Entry<String, Object> entry = mChangedColumns.get(position);
			if (convertView == null)
			{
				convertView = mInflater.inflate(R.layout.show_sync_update_list_item, null);
			}
			try
			{
				String columnName = entry.getKey();
				Object columnValue = entry.getValue();
				((TextView)convertView.findViewById(R.id.name)).setText(columnName);
				if (columnName.contains("Time"))
				{
					long time = Long.parseLong(columnValue.toString());
					((TextView)convertView.findViewById(R.id.value)).setText(mDateFormat.format(new Date(time)));
				}
				else
				{
					((TextView)convertView.findViewById(R.id.value)).setText(columnValue.toString());
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return convertView;
		}
	}
}
