package com.futureconcepts.jupiter;

import com.futureconcepts.jupiter.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DatumView extends LinearLayout
{
	private String _type;
	private TextView _headerView;
	private TextView _contentView;
	
	public DatumView(Context context, AttributeSet attrs)
    {
	    super(context, attrs);
	    setOrientation(LinearLayout.VERTICAL);
	    setBackgroundResource(R.drawable.border1);
	    _headerView = createHeader(context, attrs.getAttributeValue("jupiter", "type"));
	    _contentView = createContent(context);
		addView(_headerView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		addView(_contentView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }
	
	private TextView createHeader(Context context, String text)
	{
		TextView view = new TextView(context);
		view.setTextAppearance(context, R.style.DatumHeader);
		view.setText(text);
		return view;
	}

	private TextView createContent(Context context)
	{
		TextView view = new TextView(context);
		view.setTextAppearance(context, R.style.DatumText);
		return view;
	}
	
	public int getType2()
	{
//		return _type;
		return 0;
	}
	
	public void setContentView(String text)
	{
		_contentView.setText(text);
	}
}
