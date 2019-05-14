package com.futureconcepts.jupiter.compass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;

public class CompassRose extends BaseCompassView
{
	int marks = 360 / 12;
	
	Paint green;
	Paint greenFont;
	
	public CompassRose(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		onConstructor(context);
	}
	
	public CompassRose(Context context)
	{
		super(context);
		onConstructor(context);
	}
	
	private void onConstructor(Context context)
	{
		green = new Paint();
		green.setColor(Color.GREEN);
		green.setFlags(Paint.ANTI_ALIAS_FLAG);
		green.setStrokeWidth(2);
		green.setStyle(Style.FILL_AND_STROKE);
		
		greenFont = new Paint();
		greenFont.setColor(Color.GREEN);
		greenFont.setFlags(Paint.ANTI_ALIAS_FLAG);
		greenFont.setStrokeWidth(1);
		greenFont.setStyle(Style.STROKE);
		greenFont.setTextSize(circleWidth);
		greenFont.setTextAlign(Align.CENTER);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int width = 0, height = 0;
		int wMode = MeasureSpec.getMode(widthMeasureSpec);
		if((wMode == MeasureSpec.AT_MOST) || (wMode == MeasureSpec.EXACTLY))
		{
			width = MeasureSpec.getSize(widthMeasureSpec);
		}
		
		int hMode = MeasureSpec.getMode(heightMeasureSpec);
		if((hMode == MeasureSpec.AT_MOST) || (hMode == MeasureSpec.EXACTLY))
		{
			height = MeasureSpec.getSize(heightMeasureSpec);
		}
		
		if((width == 0) || (height == 0))
		{
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
		else
		{
			setMeasuredDimension(Math.min(width, height), Math.min(width, height));
		}
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{			 
		int width = this.getWidth();
		int height = this.getHeight();
		
	    int heightHalf = height / 2;
	    int widthHalf = width / 2;
	    
	    int diameter = Math.min(height, width) - 10;
	    int diameterHalf = diameter / 2;
	    
	    canvas.drawCircle(widthHalf, heightHalf, diameterHalf, line);
	    canvas.drawCircle(widthHalf, heightHalf, diameterHalf - circleWidth, line);

	    int wrapheading = (int)Util.wrapTo360(heading);
	    
	    canvas.drawText(wrapheading + degreesSymbol, widthHalf, heightHalf + (drawTarget ? -10 : 10), font);
	    
	    if(drawTarget)
	    {
	    	int wraptargetheading = (int)Util.wrapTo360(targetHeading) + 360;
	    	if(Math.abs(wraptargetheading - (wrapheading + 360)) < 2)
	    	{
	    		canvas.drawText(wrapheading + degreesSymbol, widthHalf, heightHalf - 10, greenFont);
	    	}
	    	
	    	//(int)Util.wrapTo360(targetHeading) + degreesSymbol + " / " + 
	    	canvas.drawText((int)(float)targetDistance + " m",
	    					widthHalf, heightHalf + 20, greenFont);
	    }

	    canvas.drawLine(widthHalf, heightHalf - diameterHalf + 2, widthHalf, heightHalf - diameterHalf - 5, line);
	    
	   /* 
	    if(drawTarget)
	    {
	    	float angle = heading + targetHeading;
	    	
	    	canvas.rotate(angle, widthHalf, heightHalf);
	    	
	    	Path p = triangleAt(widthHalf, heightHalf - (diameterHalf) + circleWidth + 1);
	    	
	    	canvas.drawPath(p, green);
	    	
	    	canvas.rotate(angle * -1, widthHalf, heightHalf);
	    }*/
	    
	    //north arrow
		Path p = triangleAt(widthHalf, heightHalf - (diameterHalf) + circleWidth + 1);
	    
		canvas.rotate(heading * -1, widthHalf, heightHalf);
		
		canvas.drawPath(p, fill);
		
		if(drawTarget)
		{
			canvas.rotate(targetHeading, widthHalf, heightHalf);
			//target arrow
			canvas.drawPath(p, green);
			
			canvas.rotate(targetHeading * -1, widthHalf, heightHalf);
		}
		
		for (int i = 0; i < 360; i += marks)
		{
			if ((i % 90) == 0)
			{
				String dir = "N";
				if(i == 90) dir = "E";
				else if(i == 180) dir = "S";
				else if(i == 270) dir = "W";

				canvas.drawText(dir, widthHalf, heightHalf - diameterHalf + circleWidth - 4, font);
			}
			else	
			{
				canvas.drawLine(widthHalf, heightHalf - diameterHalf, widthHalf, heightHalf - diameterHalf + circleWidth, line);
			}
			
			canvas.rotate(marks, widthHalf, heightHalf);
		}

		super.onDraw(canvas);
	}

}
