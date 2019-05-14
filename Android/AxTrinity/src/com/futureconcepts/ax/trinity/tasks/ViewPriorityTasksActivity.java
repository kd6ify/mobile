package com.futureconcepts.ax.trinity.tasks;

import com.futureconcepts.ax.model.data.Tactic;
import com.futureconcepts.ax.trinity.Config;
import com.futureconcepts.gqueue.MercurySettings;

public class ViewPriorityTasksActivity extends ViewTasksBaseActivity
{
	@Override
	protected Tactic queryTasks()
	{
		return Tactic.queryPriorityTasks(this, MercurySettings.getCurrentIncidentId(this));
	}
}