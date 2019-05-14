package com.futureconcepts.ax.trinity.tasks;

import com.futureconcepts.ax.model.data.Tactic;
import com.futureconcepts.ax.trinity.Config;
import com.futureconcepts.gqueue.MercurySettings;

public class ViewTasksActivity extends ViewTasksBaseActivity
{
	@Override
	protected Tactic queryTasks()
	{
		return Tactic.queryTasks(this, MercurySettings.getCurrentIncidentId(this));
	}
}