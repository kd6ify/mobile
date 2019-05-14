package com.futureconcepts.ax.rex.widget;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.View;
import android.widget.ResourceCursorAdapter;

public class AlternatingColorCursorAdapter extends ResourceCursorAdapter
{
    private int[] _colors = new int[] { Color.DKGRAY, Color.GRAY };
	
	public AlternatingColorCursorAdapter(Context context, int layout, Cursor c)
	{
		super(context, layout, c);
	}

	@Override
	public void bindView(View view, Context context, Cursor c)
	{
	    int colorPos = c.getPosition() % _colors.length;
	    view.setBackgroundColor(_colors[colorPos]);
	}
}
