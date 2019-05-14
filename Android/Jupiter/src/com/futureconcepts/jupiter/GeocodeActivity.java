package com.futureconcepts.jupiter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.futureconcepts.jupiter.R;

public class GeocodeActivity extends Activity
{
//	private static final String TAG = "GeocodeActivity";
	public static final String EXTRA_LONGITUDE = "longitude";
	public static final String EXTRA_LATITUDE = "latitude";
	private EditText _address;
	private List<Address> _result;
	private MyAdapter _adapter;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setResult(Activity.RESULT_CANCELED);
		setContentView(R.layout.select_address);
		ListView listView = (ListView)findViewById(R.id.address_list);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id)
			{
				Intent intent = new Intent();
				Address selected = _result.get(position);
				intent.putExtra(EXTRA_LONGITUDE, selected.getLongitude());
				intent.putExtra(EXTRA_LATITUDE, selected.getLatitude());
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		});
		_adapter = new MyAdapter();
		listView.setAdapter(_adapter);
		_address = (EditText)findViewById(R.id.address);
		findViewById(R.id.search).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				new FetchAddressesTask().execute(_address.getText().toString());
			}
		});
	}
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    }

	private class MyAdapter extends BaseAdapter
	{
		private LayoutInflater _inflater;
		
		public MyAdapter()
		{
			_inflater = LayoutInflater.from(GeocodeActivity.this);
		}

		public int getCount() 
		{
			if (_result != null)
			{
				return _result.size();
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
			convertView = _inflater.inflate(R.layout.address_list_item, null);
			TextView addressView = (TextView)convertView.findViewById(R.id.address);
			try
			{
				Address address = _result.get(position);
				int count = address.getMaxAddressLineIndex();
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < count; i++)
				{
					sb.append(address.getAddressLine(i));
					sb.append("\r\n");
				}
				addressView.setText(sb.toString());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return convertView;
		}
	}
	
	private class FetchAddressesTask extends AsyncTask<String, Integer, List<Address>>
	{
		private ProgressDialog _progressDialog;
		
		@Override
		protected void onPreExecute()
		{
			// present spinning progress wheel
	        _progressDialog = ProgressDialog.show(GeocodeActivity.this, "Please wait", "fetching results");
		}

		@Override
		protected List<Address> doInBackground(String... params)
		{
			Geocoder geocoder = new Geocoder(GeocodeActivity.this);
			try
			{
				return geocoder.getFromLocationName(params[0], 50);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return new ArrayList<Address>();
			}
		}
		
		@Override
		protected void onPostExecute(List<Address> result)
		{
			_result = result;
			_adapter.notifyDataSetChanged();
			_progressDialog.dismiss();
		}
	}
}
