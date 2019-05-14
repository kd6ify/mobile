package com.futureconcepts.jupiter;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.futureconcepts.jupiter.R;

public class LayersActivity extends Activity
{
	private MyAdapter _adapter;
	private Bundle _extras;
	private Config _config;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.layer_list);
		_extras = getIntent().getExtras();
		_config = Config.getInstance(this);
		ListView listView = (ListView)findViewById(R.id.layer_list);
		_adapter = new MyAdapter();
		listView.setAdapter(_adapter);
		((CheckBox)findViewById(R.id.select_clear_all)).setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked)
			{
		    	for (int i = 0; i < _extras.size(); i++)
		    	{
					_config.setLayerEnabledById(getLayer(i).getString("id"), isChecked);
		    	}
		    	_adapter.notifyDataSetChanged();
			}
		});
	}
        
    private Bundle getLayer(int position)
    {
    	return _extras.getBundle("layer_" + position);
    }

	private class MyAdapter extends BaseAdapter
	{
		private LayoutInflater _inflater;
		private ImageView _iconView;
		private TextView _layerNameView;
		private CheckBox _selectedView;
		
		public MyAdapter()
		{
			_inflater = LayoutInflater.from(LayersActivity.this);
		}

		public int getCount() 
		{
			if (_extras != null)
			{
				return _extras.size();
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
			final Bundle bundle = getLayer(position);
			final String id = bundle.getString("id");
			final String name = bundle.getString("name");
			convertView = _inflater.inflate(R.layout.layer_list_item, null);
			_iconView = (ImageView)convertView.findViewById(R.id.icon);
			_layerNameView = (TextView)convertView.findViewById(R.id.layer_name);
			_selectedView = (CheckBox)convertView.findViewById(R.id.selected_icon);
			try
			{
				_iconView.setBackgroundResource(bundle.getInt("icon"));
				_layerNameView.setText(name);
				_selectedView.setChecked(_config.isLayerEnabledById(id));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			_selectedView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked)
				{
					_config.setLayerEnabledById(id, isChecked);
				}
			});
			return convertView;
		}
	}
}
