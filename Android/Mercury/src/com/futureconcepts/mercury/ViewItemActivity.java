package com.futureconcepts.mercury;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ListActivity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class ViewItemActivity extends ListActivity
{
	protected Uri _uri;
	private SimpleDateFormat _dateFormat;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
	    super.onCreate(savedInstanceState);
	    _uri = getIntent().getData();
	    _dateFormat = new SimpleDateFormat();
	}

	@Override
	public void onResume()
	{
		super.onResume();
	}
    

	protected View getText(String name, String value)
	{
		View view = getNameValueListItem();
		((TextView)view.findViewById(R.id.name)).setText(name);
		((TextView)view.findViewById(R.id.value)).setText(value);
		return view;
	}

	protected View getDateTime(String name, long time)
	{
		View view = getNameValueListItem();
		((TextView)view.findViewById(R.id.name)).setText(name);
		TextView valueView = (TextView)view.findViewById(R.id.value);
		if (time != 0)
		{
			valueView.setText(_dateFormat.format(new Date(time)));
		}
		else
		{
			valueView.setText(null);
		}
		return view;
	}

	protected View getTimeRange(String name, long start, long end)
	{
		View result = getNameValueListItem();
		TextView nameView = (TextView)result.getTag(R.id.name);
		TextView valueView = (TextView)result.getTag(R.id.value);
		nameView.setText(name);
		if (start == 0 && end == 0)
		{
			valueView.setText("None specified");
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			if (start != 0)
			{
				sb.append(_dateFormat.format(new Date(start)));
			}
			else
			{
				sb.append("?");
			}
			sb.append(" to ");
			if (end != 0)
			{
				sb.append(_dateFormat.format(new Date(end)));
			}
			else
			{
				sb.append("?");
			}
			valueView.setText(sb);
		}
		return result;
	}
	
	protected View getInt(String name, int value)
	{
		View result = getNameValueListItem();
		TextView nameView = (TextView)result.getTag(R.id.name);
		TextView valueView = (TextView)result.getTag(R.id.value);
		nameView.setText(name);
		valueView.setText(Integer.toString(value));
		return result;
	}

	protected View getFloat(View convertView, String name, Float value)
	{
		if (convertView == null)
		{
			convertView = getNameValueListItem();
		}
		TextView nameView = (TextView)convertView.getTag(R.id.name);
		TextView valueView = (TextView)convertView.getTag(R.id.value);
		nameView.setText(name);
		valueView.setText(Float.toString(value));
		return convertView;
	}

	protected View getDouble(View convertView, String name, Double value)
	{
		if (convertView == null)
		{
			convertView = getNameValueListItem();
		}
		TextView nameView = (TextView)convertView.getTag(R.id.name);
		TextView valueView = (TextView)convertView.getTag(R.id.value);
		nameView.setText(name);
		valueView.setText(Double.toString(value));
		return convertView;
	}

	protected View getBooleanAsCheckbox(String name, boolean value)
	{
		View view = getLayoutInflater().inflate(R.layout.boolean_as_checkbox_list_item, null);
		((TextView)view.findViewById(R.id.name)).setText(name);
		CheckBox checkbox = (CheckBox)view.findViewById(R.id.value);
		if (checkbox != null)
		{
			checkbox.setChecked(value);
			checkbox.setEnabled(false);
		}
		return view;
	}
	
	protected View getNameValueListItem()
	{
		View view = getLayoutInflater().inflate(R.layout.view_item_list_item, null);
		view.setTag(R.id.name, view.findViewById(R.id.name));
		view.setTag(R.id.value, view.findViewById(R.id.value));
		return view;
	}
}
