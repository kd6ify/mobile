package com.futureconcepts.mercury.sync;

import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.Service;
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
import com.futureconcepts.mercury.gqueue.GQueue;

public class ShowQueueErrorActivity extends ListActivity
{
	private static final String TAG = "ShowQueueErrorActivity";
		
	private Uri _queueUri;
	
	private GQueue _queue;
	
//	private SimpleDateFormat mDateFormat = new SimpleDateFormat();
	
	private NotificationManager mNotificationManager;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.queue_error);
        mNotificationManager = (NotificationManager)getSystemService(Service.NOTIFICATION_SERVICE);
        _queueUri = getIntent().getData();
        _queue = GQueue.query(this, _queueUri, null, null);
        if (_queue != null)
        {
	        int count = _queue.getCount();
	        if (count == 1)
	        {
	        	_queue.moveToFirst();
		        setListAdapter(new MyAdapter());
		        Button ignoreButton = (Button)findViewById(R.id.ignore_button);
		        ignoreButton.setOnClickListener(new OnClickListener() {
					public void onClick(View v)
		            {
						int deleted = _queue.delete();
						Log.d(TAG, "deleted " + deleted + " item from queue");
						mNotificationManager.cancel(R.layout.queue_error);
						finish();
		            }
		        });
		        Button tryAgainButton = (Button)findViewById(R.id.try_again);
		        tryAgainButton.setOnClickListener(new OnClickListener() {
					public void onClick(View v)
		            {
						Uri mainQueueUri = Uri.parse(_queueUri.toString().replace("_error", ""));
						_queue.moveToQueue(mainQueueUri, _queue.getNotificationMessage(), _queue.getExceptionType());
						mNotificationManager.cancel(R.layout.queue_error);
						finish();
		            }
		        });
	        }
        }
        else
        {
        	mNotificationManager.cancel(R.layout.queue_error);
        	finish();
        }
    }
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	if (_queue != null)
    	{
    		_queue.close();
    		_queue = null;
    	}
    }
    
	public class MyAdapter extends BaseAdapter
	{
//		private SimpleDateFormat mDateFormat;
		private LayoutInflater mInflater;
		
		public MyAdapter()
		{
//			mDateFormat = new SimpleDateFormat();
			mInflater = LayoutInflater.from(ShowQueueErrorActivity.this);
		}

		public int getCount() 
		{
			return 6;
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
			if (convertView == null)
			{
				convertView = mInflater.inflate(android.R.layout.two_line_list_item, null);
			}
			TextView text1 = (TextView)convertView.findViewById(android.R.id.text1);
			TextView text2 = (TextView)convertView.findViewById(android.R.id.text2);
			switch (position)
			{
			case 0:
				text1.setText("Message");
				text2.setText(getTranslatedNotificationMessage());
				break;
			case 1:
				text1.setText(GQueue.CONTENT);
				String mimeType = _queue.getContentMimeType();
				if (mimeType.equals("text/plain"))
				{
					text2.setText(new String(_queue.getContent()));
				}
				else
				{
					text2.setText("BLOB");
				}
				break;
			case 2:
				if (_queue.getServerUrl() != null)
				{
					text1.setText(GQueue.SERVER_URL);
					text2.setText(_queue.getServerUrl());
				}
				break;
			case 3:
				text1.setText(GQueue.ACTION);
				text2.setText(_queue.getAction());
				break;
			case 4:
				text1.setText(GQueue.EXCEPTION_TYPE);
				text2.setText(_queue.getExceptionType());
				break;
			default:
				break;
			}
			return convertView;
		}
		
		private String getTranslatedNotificationMessage()
		{
			String notificationMessage = _queue.getNotificationMessage();
			if (notificationMessage != null && notificationMessage.equals("HTTP/1.1 403 Forbidden"))
			{
				return "invalid username or password (" + notificationMessage + ")";
			}
			else
			{
				return notificationMessage;
			}
		}
	}
}
