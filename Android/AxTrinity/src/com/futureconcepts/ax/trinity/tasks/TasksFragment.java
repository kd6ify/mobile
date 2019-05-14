package com.futureconcepts.ax.trinity.tasks;

import com.futureconcepts.ax.model.data.Tactic;
import com.futureconcepts.ax.model.data.TacticPriority;
import com.futureconcepts.ax.trinity.Config;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch.EditTextWithSearchInterface;
import com.futureconcepts.gqueue.MercurySettings;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.FilterQueryProvider;
import android.widget.TextView;

public class TasksFragment extends TasksFragmentBase
{
	public static final String TAG = TasksFragment.class.getSimpleName();
	
	public static TasksFragment newInstance()
	{
    	return new TasksFragment();
	}
	
	@Override
	protected Tactic queryTasks()
	{ 
		return Tactic.queryTasks(getActivity(), MercurySettings.getCurrentOperationalPeriodId(getActivity()));
	}
	
	@Override
	protected Tactic querySearch(String text)
	{
		String where = Tactic.PRIORITY+"=='"+TacticPriority.NONE+"' AND "+
				Tactic.OPERATIONAL_PERIOD+"='"+MercurySettings.getCurrentOperationalPeriodId(getActivity())+"'"+
				" AND "+Tactic.NAME+" Like '%"+text+"%'";
		return Tactic.query(getActivity(), where);
	}

	@Override
	protected void startViewTasksActivity(Uri uri)
	{
		Intent intent = new Intent(getActivity(), ViewTaskActivity.class);
		intent.setData(uri);
		startActivity(intent);
	}


	@Override
	protected void addFilter(MyAdapter _adapter) {
		// TODO Auto-generated method stub
		_adapter.setFilterQueryProvider(new FilterQueryProvider() {			
			@Override
			public Cursor runQuery(CharSequence constraint) {
				// TODO Auto-generated method stub
				String text = constraint.toString();
				if(text==null || text.length()==0)
				{
					_tactic = queryTasks();
				}else{
					_tactic = querySearch(text);
				}
				return _tactic;
			}
		});
	}

	@Override
	public void handleSearch(String text) {
		if (getAdapter() != null)
			getAdapter().getFilter().filter(text);

	}

	@Override
	protected void initSearchField(View root) {
		((EditTextWithSearch)root.findViewById(R.id.task_search)).requestFocus();
		((EditTextWithSearch)root.findViewById(R.id.task_search)).setHint("Search Tasks...");
		((TextView)root.findViewById(R.id.empty_data)).setText("No Task Found");		
	}

	
}
