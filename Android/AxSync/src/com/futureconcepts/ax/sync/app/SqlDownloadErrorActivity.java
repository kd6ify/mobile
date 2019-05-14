package com.futureconcepts.ax.sync.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.futureconcepts.ax.sync.R;
import com.futureconcepts.ax.sync.SqlDownloadErrorQueueService;
import com.futureconcepts.ax.sync.SyncService;
import com.futureconcepts.gqueue.GQueue;

public class SqlDownloadErrorActivity extends ViewItemActivity
{
	private GQueue _queue;
	
	private MyAdapter _adapter;
	
	private Uri _data;
		
	private AlertDialog _retryDialog;
	private AlertDialog _ignoreDialog;
	
	private ViewItemDescriptor[] _myItems = {
    		new ViewItemDescriptor( "Message", ITEM_TYPE_TEXT, GQueue.NOTIFICATION_MESSAGE ),
    		new ViewItemDescriptor( "Server Url", ITEM_TYPE_TEXT, GQueue.SERVER_URL ),
    		new ViewItemDescriptor( "Action", ITEM_TYPE_TEXT, GQueue.ACTION ),
    		new ViewItemDescriptor( "Exception Type", ITEM_TYPE_TEXT, GQueue.EXCEPTION_TYPE ),
    		new ViewItemDescriptor( "Param1", ITEM_TYPE_TEXT, GQueue.PARAM1 ),
    		new ViewItemDescriptor( "Content", ITEM_TYPE_BLOB_AS_STRING, GQueue.CONTENT )
    };
    	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        _data = getIntent().getData();
        setContentView(R.layout.download_error_activity);
        setDefaultOptionsMenu(false);
        if (_data != null)
        {
			_queue = GQueue.query(this, _data, null, null);
			if (_queue != null)
			{
				startManagingCursor(_queue);
				if (_queue.getCount() == 1)
				{
					_queue.moveToFirst();
		    		setTitle("Error: " + _queue.getNotificationMessage());
	    			_adapter = new MyAdapter();
	    			setListAdapter(_adapter);
		    	}
	        }
			else
			{
				finish();
			}
        }
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.download_error_activity_menu, menu);
	//	return super.onCreateOptionsMenu(menu);
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_delete:
			onMenuIgnore();
			break;
		case R.id.menu_retry:
			onMenuRetryAllDownloadErrors();
			break;
		}
		return false;
	}

	private void onMenuIgnore()
	{
        AlertDialog.Builder b = new AlertDialog.Builder(SqlDownloadErrorActivity.this);
        b.setTitle("WARNING: Deleting download transaction!");
        b.setCancelable(true);
        b.setMessage("You are about to ignore a download transaction.  Data loss will occur. Are you sure you want to do this?");
        b.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				getContentResolver().delete(_data, null, null);
				finish();
			}
        });
        b.setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        _ignoreDialog = b.show();
	}
	
	private void onMenuRetryAllDownloadErrors()
	{
		final Uri mainQueueUri = GQueue.getServiceQueueUri(this, SyncService.class);
		Uri errorQueueUri = GQueue.getServiceQueueUri(this, SqlDownloadErrorQueueService.class);
		final GQueue errorQueue = GQueue.query(this, errorQueueUri, null, null);
		if (errorQueue != null)
		{
			final int count = errorQueue.getCount();
            AlertDialog.Builder b = new AlertDialog.Builder(SqlDownloadErrorActivity.this);
            b.setTitle("WARNING: retry download transaction!");
            b.setCancelable(true);
            b.setMessage(String.format("%d failed download transactions will now be attempted again.  If this exception persists, please contact technical support. Are you sure you want to do this?", count));
            b.setPositiveButton("Yes", new DialogInterface.OnClickListener()
            {
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					for (int i = 0; i < count; i++)
					{
						errorQueue.moveToPosition(i);
						errorQueue.moveToQueue(mainQueueUri, "", "");
					}
					dialog.dismiss();
					finish();
				}
            });
            b.setNegativeButton("No", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
            _retryDialog = b.show();
		}
	}
	
	private final class MyAdapter extends ViewItemAdapter
	{
		public MyAdapter()
		{
			super(_queue, _myItems);
		}
	}
}
