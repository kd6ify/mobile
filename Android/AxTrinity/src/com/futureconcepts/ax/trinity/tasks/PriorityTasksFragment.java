package com.futureconcepts.ax.trinity.tasks;

import com.futureconcepts.ax.model.data.Tactic;
import com.futureconcepts.ax.model.data.TacticPriority;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.widget.EditTextWithSearch;
import com.futureconcepts.gqueue.MercurySettings;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.widget.FilterQueryProvider;
import android.widget.TextView;

public class PriorityTasksFragment extends TasksFragmentBase
{
	public static final String TAG = PriorityTasksFragment.class.getSimpleName();
	
    public static PriorityTasksFragment newInstance()
    {
    	PriorityTasksFragment result = new PriorityTasksFragment();
    	return result;
    }

	@Override
	protected Tactic queryTasks()
	{
		return Tactic.queryPriorityTasks(getActivity(), MercurySettings.getCurrentIncidentId(getActivity()));
	}

	@Override
	protected void startViewTasksActivity(Uri uri)
	{
		Intent intent = new Intent(getActivity(), ViewPriorityTaskActivity.class);
		intent.setData(uri);
		startActivity(intent);
	}

	@Override
	public void handleSearch(String text) {
		// TODO Auto-generated method stub
		if(getAdapter()!=null)
    		getAdapter().getFilter().filter(text);		
	}

	@Override
	protected Tactic querySearch(String text) {
		String where = Tactic.PRIORITY+"!='"+TacticPriority.NONE+"' AND "+
					Tactic.INCIDENT+"='"+ MercurySettings.getCurrentIncidentId(getActivity())+"'"+
				" AND "+Tactic.NAME+" Like '%"+text+"%'";
		return Tactic.query(getActivity(), where);
	}

	@Override
	protected void addFilter(MyAdapter myAdapter) {
		// TODO Auto-generated method stub
		myAdapter.setFilterQueryProvider(new FilterQueryProvider() {			
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
	protected void initSearchField(View root) {
		// TODO Auto-generated method stub
		((EditTextWithSearch)root.findViewById(R.id.task_search)).requestFocus();
		((EditTextWithSearch)root.findViewById(R.id.task_search)).setHint("Search Priority Tasks...");
		((TextView)root.findViewById(R.id.empty_data)).setText("No Priority Task Found");
	}
	
}
