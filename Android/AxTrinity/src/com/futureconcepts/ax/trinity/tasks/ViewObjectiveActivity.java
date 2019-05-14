package com.futureconcepts.ax.trinity.tasks;

import com.futureconcepts.ax.model.data.Objective;
import com.futureconcepts.ax.trinity.IntentCategory;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.ViewItemActivity;
import com.futureconcepts.ax.trinity.ViewItemActivity.ViewItemDescriptor;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class ViewObjectiveActivity extends ViewItemActivity
{
	private Objective _objective;
	private MyAdapter _adapter;
	
    private ViewItemDescriptor[] _myItems = {
    		new ViewItemDescriptor( "Name", ITEM_TYPE_TEXT, Objective.NAME ),
    		new ViewItemDescriptor( "Status", ITEM_TYPE_INDEXED_TYPE, null ),
    		new ViewItemDescriptor( "Projected Start/End", ITEM_TYPE_DATE_TIME_RANGE, Objective.PROJECTED_START, Objective.PROJECTED_END ),
    		new ViewItemDescriptor( "Actual Start/End", ITEM_TYPE_DATE_TIME_RANGE, Objective.ACTUAL_START, Objective.ACTUAL_END),
    		new ViewItemDescriptor( "Percent Complete", ITEM_TYPE_REAL, Objective.PERCENT_COMPLETE ),
    		new ViewItemDescriptor( "Notes", ITEM_TYPE_TEXT, Objective.NOTES ),
    		new ViewItemDescriptor( "Tactics", ITEM_TYPE_TEXT, null ),
    };
			
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_objective);
        startManagingModel(_objective = Objective.query(this, getData()));
        if (moveToFirstIfOneRow())
        {
	    	setTitle("Objective: " + _objective.getName());
	        _adapter = new MyAdapter();
	        setListAdapter(_adapter);
	        registerContentObserver(_adapter);
        }
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.view_objective_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		}
		return false;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		Intent intent = new Intent(this, ViewTasksActivity.class);
		intent.setData(getIntent().getData());
		startActivity(intent);
	}

	private void showTactics()
	{
		Intent intent = new Intent(Intent.ACTION_VIEW, getData());
		intent.addCategory(IntentCategory.TACTICS);
		intent.setData(getData());
		startActivity(intent);
	}
	
	private final class MyAdapter extends ViewItemAdapter
	{
		public MyAdapter()
		{
			super(_objective, _myItems);
		}
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View result = null;
			ViewItemDescriptor vid = _myItems[position];
			if (vid.type == ViewItemActivity.ITEM_TYPE_INDEXED_TYPE)
			{
				result = getIndexedTypeView(vid, _objective.getStatus(ViewObjectiveActivity.this));
			}
			else
			{
				result = super.getView(position, convertView, parent);
			}
			return result;
		}
	}
}