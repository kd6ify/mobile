package com.futureconcepts.jupiter.compass;

import java.io.Closeable;
import java.util.Date;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.location.Location;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;

public class BaseCompassView extends View implements ICompassView, ICurrentLocationView, IWaypointView, Closeable
{
	private static String TAG = "BaseCompass";
	
	protected Paint line;
	protected Paint fill;
	protected Paint font;
	
	protected int circleWidth = 30;
	
	protected int triWidth = 6;
	protected int triHeight = 16;
	
	protected Location current;
	
	protected final String degreesSymbol = "°";
	
	protected boolean drawTarget;
	
	protected float heading;
	protected float targetHeading;
	protected float targetDistance;
	protected Date targetETA;

	private ParameterRamp _rampHeading;
	private ParameterRamp _rampTargetHeading;
	private ParameterRamp _rampTargetDistance;
	
	private static final int _rampTime = 300;
	private static final int _rampSteps = 40;
	private boolean _rampEnabled = true;
			
	private RampWorker _rampWorker = null;
	
	private class RampWorker extends AsyncTask<BaseCompassView, Void, Void>
	{
		private float value;
		private boolean changed;
		private boolean allComplete;
		private boolean running;
		
		@Override
		protected Void doInBackground(BaseCompassView... params)
		{
			value = 0;
			changed = false;
			allComplete = false;
			running = true;
			
			while(!allComplete && running)
			{
				value = heading;
				doramp(_rampHeading);
				heading = value;
				
				if (drawTarget)
				{
					value = targetHeading;
					doramp(_rampTargetHeading);
					targetHeading = value;
					
					value = targetDistance;
					doramp(_rampTargetDistance);
					targetDistance = value;
				}
				
				if(changed)
				{	
					publishProgress((Void)null);
				}
				
				try
				{
					Thread.sleep(_rampTime / _rampSteps);
				}
				catch(InterruptedException ex) { }
			}
			
			return null;
		}
		
		private void doramp(ParameterRamp ramp)
		{
			if(ramp != null)
			{
				float newv = ramp.updateValue();
				if(newv != this.value)
				{
					this.value = newv;
					this.changed = true;
				}
				
				this.allComplete = this.allComplete && ramp.isComplete();
			}
		}
		
		@Override
		protected void onProgressUpdate(Void... nothing)
		{
			invalidate();
		}
		
		@Override
		protected void onPostExecute(Void nothing)
		{
			_rampWorker = null;
			invalidate();
		}
		
		@Override
		protected void onCancelled()
		{
			running = false;
		}
	};
		
	private void startRamps()
	{
		if(_rampWorker == null)
		{
			_rampWorker = new RampWorker();
			_rampWorker.execute(this);
		}
	}
	
	public BaseCompassView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		onConstructor(context);
	}
	
	public BaseCompassView(Context context)
	{
		super(context);
		onConstructor(context);
	}
	
	private void onConstructor(Context context)
	{
		_rampHeading = new CircularRamp(0, 0, 0);
		_rampTargetHeading = new CircularRamp(0, 0 ,0);
		_rampTargetDistance = new LinearRamp(0, 0, 0);
		
		line = new Paint();
		line.setColor(Color.WHITE);
		line.setFlags(Paint.ANTI_ALIAS_FLAG);
		line.setStrokeWidth(2);
		line.setStyle(Style.STROKE);
	    
	    fill = new Paint();
	    fill.setColor(Color.WHITE);
	    fill.setFlags(Paint.ANTI_ALIAS_FLAG);
	    fill.setStrokeWidth(2);
	    fill.setStyle(Style.FILL_AND_STROKE);
	    
	    font = new Paint();
	    font.setColor(Color.WHITE);
	    font.setFlags(Paint.ANTI_ALIAS_FLAG);
	    font.setStrokeWidth(1);
	    font.setTextAlign(Align.CENTER);
	    font.setTextSize(circleWidth);
	}
	
	public void close()
	{
		if(_rampWorker != null)
		{
			_rampWorker.cancel(true);
			_rampWorker = null;
		}
	}
	
	@Override
	public void setWaypointBearing(float target)
	{
		_rampTargetHeading.retarget(target, _rampEnabled ? _rampTime : 0);
		startRamps();
	}
	
	@Override
	public void setWaypointDistance(float distance)
	{
		_rampTargetDistance.retarget(distance, _rampEnabled ? _rampTime : 0);
		startRamps();
	}
	
	public void setWaypointETA(Date eta)
	{
		//TODO do we need to RAMP the date?
		//TODO show the date
	}
	
	public void setCurrentLocation(Location current)
	{
		this.current = current;
	}
		
	public void setHeading(float degrees)
	{
		_rampHeading.retarget(degrees, _rampEnabled ? _rampTime : 0);
		startRamps();
	}
	
	public void setRampingEnabled(boolean ramp)
	{
		_rampEnabled = ramp;
	}
	
	public boolean getRampingEnabled()
	{
		return _rampEnabled;
	}
		
	protected Path triangleAt(int x, int y)
	{
		Path p = new Path();
		p.moveTo(x, y);
		x += triWidth;
		y += triHeight;
		p.lineTo(x, y);
		x -= triWidth * 2;
		p.lineTo(x, y);
		x += triWidth;
		y -= triHeight;
		p.lineTo(x, y);
		
		return p;
	}

	@Override
	public Location getCurrentLocation() {
		return current;
	}

	@Override
	public float getHeading() {
		return heading;
	}

	@Override
	public boolean getShowWaypoint() {
		return drawTarget;
	}

	@Override
	public void setShowWaypoint(boolean show) {
		drawTarget = show;
	}

	@Override
	public float[] getOrientation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOrientation(float[] values) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getWaypointBearing()
	{
		return targetHeading;
	}

	@Override
	public float getWaypointDistance()
	{
		return targetDistance;
	}

	@Override
	public Date getWaypointETA()
	{
		return targetETA;
	}
}
