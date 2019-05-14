package com.futureconcepts.jupiter.compass;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.location.GpsSatellite;
import android.util.AttributeSet;
import android.view.View;

public class GpsSatBarGraph extends View implements IGpsSatelliteView
{
	private Paint line;
	private Paint fixSat;
	private Paint badSat;
	private Paint text;
	
	private float[] tickmarks;
	
	private GpsSatStatsSnapshot[] satdata = new GpsSatStatsSnapshot[32];
	
	private boolean show = true;
	
	private float barwidth = 0;
	
	public GpsSatBarGraph(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		line = new Paint();
		line.setColor(Color.LTGRAY);
		//line.setFlags(Paint.ANTI_ALIAS_FLAG);
		line.setStrokeWidth(0.5f);
		line.setStyle(Style.STROKE);
		line.setTextAlign(Align.CENTER);
		line.setTextSize(15);
	    
		fixSat = new Paint();
		fixSat.setColor(Color.GREEN);
		fixSat.setFlags(Paint.ANTI_ALIAS_FLAG);
		fixSat.setStrokeWidth(1);
		fixSat.setStyle(Style.FILL_AND_STROKE);
	    
		badSat = new Paint();
		badSat.setColor(Color.GRAY);
		badSat.setFlags(Paint.ANTI_ALIAS_FLAG);
		badSat.setStrokeWidth(1);
		badSat.setStyle(Style.FILL_AND_STROKE);
		
		text = new Paint();
		text.setColor(Color.LTGRAY);
		text.setTextAlign(Align.CENTER);
		text.setFlags(Paint.ANTI_ALIAS_FLAG);
		text.setTextSize(20);
		text.setStrokeWidth(1);
		text.setStyle(Style.FILL_AND_STROKE);
		
	}

	@Override
	protected void onMeasure(int widthInfo, int heightInfo)
	{
	//	super.onMeasure(widthInfo, heightInfo);
		setMeasuredDimension(MeasureSpec.getSize(widthInfo), 20);
	}
	
	@Override
	protected void onSizeChanged(int width, int height, int oldW, int oldH)
	{
		genTickMarks(width, height);
		
		super.onSizeChanged(width, height, oldW, oldH);
	}
	
	private void genTickMarks(int width, int height)
	{
		tickmarks = new float[31 * 4];
		
		barwidth = width / 32.0f;
		
		float heightQuarter = height - height / 4.0f;
		float heightHalf = height / 2.0f;
		float x = barwidth;
		for(int i = 0, count = 1; i < tickmarks.length; i += 4, count++, x += barwidth)
		{
			tickmarks[i] = x;
			tickmarks[i + 1] = height;
			tickmarks[i + 2] = x;
			if(count % 5 == 0)
			{
				tickmarks[i + 3] = heightHalf;
			}
			else
			{
				tickmarks[i + 3] = heightQuarter;
			}
		}
	}
	
	private float width, height, maxsnr, minsnr, dynamicrange;
	
	@Override
	public void onDraw(Canvas canvas)
	{
		width = getWidth();
		height = getHeight();
		
		if((satdata == null) || (!show))
		{
			canvas.drawText("-- no satellites --", width / 2.0f, height - 2, text);
			return;
		}
				
		for(int i = 0; i < satdata.length; i++)
		{
			if(satdata[i] == null) continue;
			
			float h = ((satdata[i].getSnr() - minsnr) / dynamicrange) * height;
			if(h < 1) h = 1.0f;
			
			Paint p = badSat;
			if(satdata[i].usedInFix()) p = fixSat;
			
			canvas.drawRect(i * barwidth, height - h, (i + 1) * barwidth, height, p); 
		}
		
		canvas.drawLines(tickmarks, line);
	}

	@Override
	public boolean getShowSatelliteLocations()
	{
		return show;
	}

	public List<GpsSatStatsSnapshot> getSatelliteLocations()
	{
		List<GpsSatStatsSnapshot> snapshot = new ArrayList<GpsSatStatsSnapshot>();
		
		for(int i = 0; i < satdata.length; i++)
		{
			if(satdata[i] != null)
			{
				snapshot.add(satdata[i]);
			}
		}
		
		return snapshot;
	}
	
	//TODO this is for debugging only
	boolean trigger = false;
	
	@Override
	public void setSatelliteLocations(List<GpsSatStatsSnapshot> satellites)
	{
		//TODO this is for debugging only
		if(trigger) return;
		trigger = true;
		
		for(int i = 0; i < satdata.length; i++)
		{
			satdata[i] = null;
		}
		
		for(GpsSatStatsSnapshot s : satellites)
		{
			satdata[s.getPrn()] = s;
		}
		
		//pre compute SNR ratios
		maxsnr = 0;
		minsnr = Float.MAX_VALUE;
		
		for(GpsSatStatsSnapshot s : satdata)
		{		
			if(s == null) continue;
			
			maxsnr = Math.max(maxsnr, s.getSnr());
			minsnr = Math.min(minsnr, s.getSnr());
		}
		
		dynamicrange = maxsnr - minsnr;
		
		this.postInvalidate();
	}

	
	@Override
	public void setShowSatelliteLocations(boolean show)
	{
		if(show != this.show)
		{
			//TODO this is a hack
			this.show = true; //show;
			this.postInvalidate();
		}
	}
	
}
