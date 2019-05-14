package com.futureconcepts.ax.trinity.tasks;

import com.futureconcepts.ax.model.data.Tactic;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.widget.AlternatingColorCursorAdapter;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch.EditTextWithSearchInterface;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public abstract class TasksFragmentBase extends ListFragment implements EditTextWithSearchInterface
{
	public Tactic _tactic;
	private MyAdapter _adapter;
	private MyObserver _observer;
	private View root;

	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        _tactic = queryTasks();
        _observer = new MyObserver(new Handler());
        getActivity().getContentResolver().registerContentObserver(Tactic.CONTENT_URI, true, _observer);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle)
    {
    	root = inflater.inflate(R.layout.view_tactics, container, false);
    	ListView lv = (ListView)root.findViewById(android.R.id.list);
    	lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    	_adapter = new MyAdapter();
    	setListAdapter(_adapter);
    	addFilter(_adapter);
    	initSearchField(root);
    	addSearchListener();    	
    	return root;
    }
    
    public MyAdapter getAdapter()
    {
    	return _adapter;
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
    	if (_tactic != null)
    	{
    		_tactic.close();
    		_tactic = null;
    	}
    	removeSearchListener();
    }
    
	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		try
		{
			_tactic.moveToPosition(position);
			Tactic tactic = (Tactic)((ListView)root.findViewById(android.R.id.list)).getItemAtPosition(position);
			startViewTasksActivity(Uri.withAppendedPath(Tactic.CONTENT_URI, tactic.getID()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	protected abstract void initSearchField(View root);
	protected abstract Tactic queryTasks();
	protected abstract Tactic querySearch(String text);
	protected abstract void startViewTasksActivity(Uri uri);
	protected abstract void addFilter(MyAdapter myAdapter);
	//@Override
	public void addSearchListener() {
		// TODO Auto-generated method stub
		((EditTextWithSearch)root.findViewById(R.id.task_search)).addSearchListener(this);
	}

	//@Override
	public void removeSearchListener() {
		// TODO Auto-generated method stub
		((EditTextWithSearch)root.findViewById(R.id.task_search)).removeSearchListener(this);		
	}	
	
	public class MyAdapter extends AlternatingColorCursorAdapter
	{
		public MyAdapter()
		{
			super(getActivity(), R.layout.view_tasks_list_item, _tactic);
		}

		@Override
		public void bindView(View view, Context context, Cursor c)
		{
			super.bindView(view, context, c);
			try
			{
				Tactic tactic = (Tactic)c;
				((TextView)view.findViewById(R.id.name)).setText(tactic.getName());
			}
			catch (Exception e)
			{
				e.printStackTrace();
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
				if (_tactic != null)
				{
					_tactic.requery();
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
