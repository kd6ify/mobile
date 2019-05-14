package com.futureconcepts.mercury.update;

import com.futureconcepts.mercury.R;
import com.futureconcepts.mercury.download.Downloads;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;

public class ViewDownloadStateActivity extends ViewItemActivity
{
//	private static final String TAG = ViewDownloadStateActivity.class.getSimpleName();
		
	private Cursor _cursor;
	
	private MyAdapter _adapter;
		
	private ViewItemDescriptor[] _myItems = {
    		new ViewItemDescriptor( "Title", ITEM_TYPE_TEXT, Downloads.Impl.TITLE ),
    		new ViewItemDescriptor( "Description", ITEM_TYPE_TEXT, Downloads.Impl.DESCRIPTION),
    		new ViewItemDescriptor( "URI", ITEM_TYPE_TEXT, Downloads.Impl.URI),
    		new ViewItemDescriptor( "Status", ITEM_TYPE_DOWNLOAD_STATUS, Downloads.Impl.STATUS ),
    		new ViewItemDescriptor( "Total Bytes", ITEM_TYPE_INT, Downloads.Impl.TOTAL_BYTES),
    		new ViewItemDescriptor( "Current Bytes", ITEM_TYPE_INT, Downloads.Impl.CURRENT_BYTES),
    };
    	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_download_state_activity);
        setDefaultOptionsMenu(false);
        Intent intent = getIntent();
        if (intent != null)
        {
        	Uri data = intent.getData();
        	if (data != null)
        	{
        		_cursor = getContentResolver().query(data, null, null, null, null);
        		startManagingCursor(_cursor);
				if (_cursor.getCount() == 1)
				{
					_cursor.moveToFirst();
					int ci = _cursor.getColumnIndex(Downloads.Impl.TITLE);
		    		setTitle("Download State: " + _cursor.getString(ci));
	    			_adapter = new MyAdapter();
	    			setListAdapter(_adapter);
		    	}
        	}
        }
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.view_download_state_activity_menu, menu);
	//	return super.onCreateOptionsMenu(menu);
		return true;

	}

    protected void onMenuDeleteItem()
	{
		try
		{
			Intent intent = getIntent();
			if (intent != null)
			{
				Uri data = intent.getData();
				if (data != null)
				{
					getContentResolver().delete(data, null, null);
					finish();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private final class MyAdapter extends ViewItemAdapter
	{
		public MyAdapter()
		{
			super(_cursor, _myItems);
		}
	}
}
