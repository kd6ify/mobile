package com.futureconcepts.anonymous;



import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

public class MyOnClickListener implements AdapterView.OnItemClickListener {

	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		String category = "";
		switch (position) {
		case 0:
			category = "Weapons";
			Log.d("Main Activity", "Weapons");
			Intent a = new Intent(v.getContext(), AnonymousActivity.class);
			a.putExtra("Category", category);
			v.getContext().startActivity(a);
			break;

		case 1:
			category = "Drugs";
			Log.d("Main Activity", "Drugs");
			Intent b = new Intent(v.getContext(), AnonymousActivity.class);
			b.putExtra("Category", category);
			v.getContext().startActivity(b);
			break;

		case 2:
			category = "Bullying";
			Log.d("Main Activity", "Bullying");
			Intent c = new Intent(v.getContext(), AnonymousActivity.class);
			c.putExtra("Category", category);
			v.getContext().startActivity(c);
			break;
		case 3:
			category = "Violence";
			Log.d("Main Activity", "Violence");
			Intent d = new Intent(v.getContext(), AnonymousActivity.class);
			d.putExtra("Category", category);
			v.getContext().startActivity(d);
			break;
		case 4:
			category = "Thef";
			Log.d("Main Activity", "Thef");
			Intent e = new Intent(v.getContext(), AnonymousActivity.class);
			e.putExtra("Category", category);
			v.getContext().startActivity(e);
			break;
		case 5:
			category = "Safety";
			Log.d("Main Activity", "Safety");
			Intent f = new Intent(v.getContext(), AnonymousActivity.class);
			f.putExtra("Category", category);
			v.getContext().startActivity(f);
			break;
		case 6:
			category = "Vandalism";
			Log.d("Main Activity", "Vandalism");
			Intent g = new Intent(v.getContext(), AnonymousActivity.class);
			g.putExtra("Category", category);
			v.getContext().startActivity(g);
			break;
		case 7:
			category = "Threats";
			Log.d("Main Activity", "Threats");
			Intent h = new Intent(v.getContext(), AnonymousActivity.class);
			h.putExtra("Category", category);
			v.getContext().startActivity(h);
			break;
		case 8:
			category = "Other";
			Log.d("Main Activity", "Other");
			Intent i = new Intent(v.getContext(), AnonymousActivity.class);
			i.putExtra("Category", category);
			v.getContext().startActivity(i);
			break;
		case 9:
			category = "Suggestions";
			Log.d("Main Activity", "Suggestions");
			Intent j = new Intent(v.getContext(), AnonymousActivity.class);
			j.putExtra("Category", category);
			v.getContext().startActivity(j);
			break;

		}

	}
}
