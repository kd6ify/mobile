package com.futureconcepts.ax.trinity.triage;

import com.futureconcepts.ax.model.data.Triage;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.widget.AlternatingColorCursorAdapter;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

public abstract class CasualtyListFragmentBase extends ListFragment
{
	private Triage _triage;
	private MyAdapter _adapter;
	private MyObserver _observer;
	protected int _textColor;

	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        _triage = queryCasualties();
        _observer = new MyObserver(new Handler());
        getActivity().getContentResolver().registerContentObserver(Triage.CONTENT_URI, true, _observer);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle)
    {
    	View result = inflater.inflate(R.layout.casualty_list, container, false);
    	ListView lv = (ListView)result.findViewById(android.R.id.list);
    	lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    	_adapter = new MyAdapter();
    	setListAdapter(_adapter);
    	return result;
    }

    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	if (_observer != null)
    	{
    		getActivity().getContentResolver().unregisterContentObserver(_observer);
    		_observer = null;
    	}
    	if (_triage != null)
    	{
    		_triage.close();
    		_triage = null;
    	}
    }
    
	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		try
		{
			_triage.moveToPosition(position);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.withAppendedPath(Triage.CONTENT_URI, _triage.getID()));
			startActivity(intent);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void onTrackingIdClick(View view)
	{
		Log.d("test", "here");
	}
	
	protected abstract Triage queryCasualties();
	
	public static CasualtyListFragmentBase create(String tag)
	{
		CasualtyListFragmentBase result = null;
		if (tag.equals(CasualtyListFragmentDeceased.TAG))
		{
			result = new CasualtyListFragmentDeceased();
		}
		else if (tag.equals(CasualtyListFragmentDelayed.TAG))
		{
			result = new CasualtyListFragmentDelayed();
		}
		else if (tag.equals(CasualtyListFragmentImmediate.TAG))
		{
			result = new CasualtyListFragmentImmediate();
		}
		else if (tag.equals(CasualtyListFragmentMarked.TAG))
		{
			result = new CasualtyListFragmentMarked();
		}
		else if (tag.equals(CasualtyListFragmentMinor.TAG))
		{
			result = new CasualtyListFragmentMinor();
		}
		else if (tag.equals(CasualtyListFragmentVictim.TAG))
		{
			result = new CasualtyListFragmentVictim();
		}
		return result;
	}
	
	public class MyAdapter extends AlternatingColorCursorAdapter
	{
		public MyAdapter()
		{
			super(getActivity(), R.layout.casualty_list_item, _triage);
		}

		@Override
		public void bindView(View view, Context context, Cursor c)
		{
			super.bindView(view, context, c);
			try
			{
				Triage triage = (Triage)c;
				Button button = (Button)view.findViewById(R.id.tracking_id);
				if (_textColor != 0)
				{
					button.setTextColor(getActivity().getResources().getColor(_textColor));
					button.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							onListItemClick(null, v, _triage.getPosition(), 0);
						}
					});
				}
				button.setText(triage.getTrackingID());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		@Override
		public void notifyDataSetChanged()
		{
			super.notifyDataSetChanged();
			if (_triage != null)
			{
//				generateSummary(_summaryView, _triage.getCount());
			}
		}
	}
	
	public final class MyObserver extends ContentObserver
	{
		public MyObserver(Handler handler)
        {
	        super(handler);
        }
		
		@Override
		public void onChange(boolean selfChange)
		{
			try
			{
				if (_triage != null)
				{
					_triage.requery();
					if (_adapter != null)
					{
						_adapter.notifyDataSetChanged();
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}	
}
