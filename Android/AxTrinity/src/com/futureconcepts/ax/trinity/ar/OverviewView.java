package com.futureconcepts.ax.trinity.ar;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Triage;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.gqueue.MercurySettings;
import com.google.android.maps.GeoPoint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

public class OverviewView extends View
{
	private float HORIZONTAL_VIEW_ANGLE;
	private float VERTICAL_VIEW_ANGLE;
	private float ROTATION;
	private final int ICON_SIZE = 80;
	private final float MAX_DISPLAY_DISTANCE = 1000;
	private final float PERSON_SIZE_METERS = 1.75F;
	
	private LocationManager locman;
	private SensorManager sensman;
	
	private float direction = (float) 0;
	private float inclination = (float) 0;
	private float latitude = (float) 0;
	private float longitude = (float) 0;
	
	private Triage triage;
	
	private Context ctxt;
	
	private SensorEventListener slistener = new SensorEventListener()
	{
		public void onAccuracyChanged(Sensor arg0, int arg1)
		{
			
		}
		
		public void onSensorChanged(SensorEvent evt)
		{
			float vals[] = evt.values;
			if(evt.sensor.getType() == Sensor.TYPE_ORIENTATION)
			{
				float ndirection = vals[0] + 90;
				if(ndirection > 360)
					ndirection -= 360;
				
				if(Math.abs(ndirection - direction) > 1.5)
					direction = Math.round(ndirection + ROTATION);
				
				if(direction > 360)
					direction -= 360;
				if(direction < 0)
					direction += 360;
				
				UpdateText();
			}
			else if(evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			{
				float ninclination = vals[2];
				if(Math.abs(ninclination - inclination) > .5)
					inclination = ninclination;
				
				UpdateText();
			}
		}
	};
	
	private LocationListener llistener = new LocationListener() 
	{    
		public void onLocationChanged(Location location) 
		{      
			latitude = (float)location.getLatitude();
			longitude = (float)location.getLongitude();
			
			UpdateText();
		}    
		
		public void onStatusChanged(String provider, int status, Bundle extras) {}    
		
		public void onProviderEnabled(String provider) {}    
		
		public void onProviderDisabled(String provider) {}  
		
	};
	
	public OverviewView(Context context) 
	{
		super(context);
		
		ctxt = context;
		
		locman = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		locman.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, llistener);
		
		sensman = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		sensman.registerListener(slistener, sensman.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
		sensman.registerListener(slistener, sensman.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
	
		SetupDeviceSettings();
		
		//this.HORIZONTAL_VIEW_ANGLE = MercurySettings.getCameraHorizontalViewAngle(context);
		//this.VERTICAL_VIEW_ANGLE = MercurySettings.getCameraHorizontalViewAngle(context);
		
		AddLocations();
	}
	
	public OverviewView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		
		ctxt = context;
		
		locman = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		locman.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, llistener);
		
		sensman = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		sensman.registerListener(slistener, sensman.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
		sensman.registerListener(slistener, sensman.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
	
		SetupDeviceSettings();
		
		//this.HORIZONTAL_VIEW_ANGLE = MercurySettings.getCameraHorizontalViewAngle(context);
		//this.VERTICAL_VIEW_ANGLE = MercurySettings.getCameraHorizontalViewAngle(context);
		
		AddLocations();
	}
	
	private void SetupDeviceSettings()
	{
		String model = Build.MODEL;
		
		// Alternative model names found at http://www.glbenchmark.com
		if(model.equals("DROID RAZR") || model.equals("DROID BIONIC"))
		{
			this.HORIZONTAL_VIEW_ANGLE = 62.1F;
			this.VERTICAL_VIEW_ANGLE = 48.2F;
			this.ROTATION = 0F;
		}
		else if(model.equals("Xoom") || model.equals("MZ601") || model.equals("MZ604") || model.equals("MZ606"))
		{
			this.HORIZONTAL_VIEW_ANGLE = 55.7F;
			this.VERTICAL_VIEW_ANGLE = 43.23F;
			this.ROTATION = -90;
		}
		else if(model.equals("MB865") || model.equals("MB860"))
		{
			this.HORIZONTAL_VIEW_ANGLE = 48.2F;
			this.VERTICAL_VIEW_ANGLE = 26.01F;
			this.ROTATION = 0F;
		}
		//else if(model.equals("T-Mobile G1") || model.equals("HTC Dream"))
		//{
			
		//}
		else
		{
			// Set some defaults
			this.HORIZONTAL_VIEW_ANGLE = 50F;
			this.VERTICAL_VIEW_ANGLE = 40F;
			this.ROTATION = 0F;
		}
	}
	
	public void AddLocations()
	{
		String currentIncidentId = MercurySettings.getCurrentIncidentId(ctxt);
		triage = Triage.queryIncident(ctxt, currentIncidentId);
	}
	
	private void UpdateText()
	{
		this.invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		Paint p = new Paint();
		p.setColor(Color.GRAY);
		p.setAlpha(150);
		
		Paint p2 = new Paint();
		p2.setColor(Color.WHITE);
		p2.setAlpha(150);
		
		Paint red = new Paint();
		red.setColor(Color.RED);
		
		Paint text = new Paint();
		text.setColor(Color.WHITE);
		text.setTextSize(24);
		text.setShadowLayer(5, 1, 1, Color.BLACK);
		text.setTextAlign(Align.CENTER);
		
		canvas.drawArc(new RectF(getWidth() - 196, getHeight() - 196, getWidth() - 2, getHeight() - 2), (HORIZONTAL_VIEW_ANGLE / 2F) - (float)90, (float)360 - HORIZONTAL_VIEW_ANGLE, true, p);
		canvas.drawArc(new RectF(getWidth() - 196, getHeight() - 196, getWidth() - 2, getHeight() - 2), 0 - (HORIZONTAL_VIEW_ANGLE / 2F) - (float)90, HORIZONTAL_VIEW_ANGLE, true, p2);
	
		if(this.latitude != 0 && this.longitude != 0)
		{
			for(int i = 0; i < triage.getCount(); ++i)
			{
				triage.moveToPosition(i);
				
				Location l2 = new Location("");
				l2.setLatitude(latitude);
				l2.setLongitude(longitude);

				Address a = triage.getAddress(ctxt);
				
				if(a != null)
				{
// google maps dependencies removed from data model project
//					GeoPoint gpt = triage.getAddress(ctxt).getWKTAsGeoPoint();
					Point point = triage.getAddress(ctxt).getWKTAsPoint();
					GeoPoint gpt = new GeoPoint(point.y, point.x);
					Location me = new Location("");
					me.setLatitude((float)gpt.getLatitudeE6() / (float)1000000);
					me.setLongitude((float)gpt.getLongitudeE6() / (float)1000000);
					
					Bitmap bb = null;
					
					Paint color = new Paint();
					
					if(triage.getColorID().equals("617B480D-1164-4453-BDCD-EFBCED03D4DA"))
					{
						bb = BitmapFactory.decodeResource(getResources(), R.drawable.triage_green_indicator_icon);
						color.setColor(Color.GREEN);
					}
					else if(triage.getColorID().equals("0A3C8B53-4647-4793-975E-68CCBBACAFE4"))
					{
						bb = BitmapFactory.decodeResource(getResources(), R.drawable.triage_yellow_indicator_icon);
						color.setColor(Color.YELLOW);
					}
					else if(triage.getColorID().equals("47FB865D-2A3F-48A1-95FA-AEB485109083"))
					{
						bb = BitmapFactory.decodeResource(getResources(), R.drawable.triage_red_indicator_icon);
						color.setColor(Color.RED);
					}
					else if(triage.getColorID().equals("14D7AD60-DA12-42D3-9BBE-A296B8CBCB6D"))
					{
						bb = BitmapFactory.decodeResource(getResources(), R.drawable.triage_black_indicator_icon);
						color.setColor(Color.DKGRAY);
					}
					
					float distance = l2.distanceTo(me);
					if(distance < MAX_DISPLAY_DISTANCE)
					{
						// Calculate draw distance from center of circle
						float drawDist = distance / MAX_DISPLAY_DISTANCE * 100;
						
						float bearing = l2.bearingTo(me);
						bearing = bearing - direction;
						if(bearing < 0)
							bearing += 360;
						
						float b = bearing;
						if(bearing >= 90 && bearing < 180)
							b -= 90;
						if(bearing < 0 && bearing >= -90)
							b += 90;
						if(bearing < -90)
							b += 180;
						
						float xxx = (float)Math.sin(b * Math.PI / 180) * drawDist;
						float yyy = (float)Math.cos(b * Math.PI / 180) * drawDist;
						
						float x = xxx;
						float y = yyy;
						
						if(bearing >= 90 && bearing < 180)
						{
							x = yyy;
							y = xxx * -1;
						}
						else if(bearing < -90)
						{
							y = yyy * -1;
							x = xxx * -1;
						}
						else if(bearing < 0 && bearing >= -90)
						{
							x = yyy * -1;
							y = xxx;
						}
						
						canvas.drawRect(getWidth() - 100 + x - 5, getHeight() - 100 - y - 5, getWidth() - 100 + x + 5, getHeight() - 100 - y + 5, color);
					
						// See if this location is within my viewing area
						float absb = bearing;
						if(absb > 180)
							absb -= 360;
						
						float offset = absb;// - direction;
						
						
						if(Math.abs(offset) < (HORIZONTAL_VIEW_ANGLE / 2F))
						{
							offset = offset + (HORIZONTAL_VIEW_ANGLE / 2F);
							float per = offset / HORIZONTAL_VIEW_ANGLE;
							
							float distper = distance / MAX_DISPLAY_DISTANCE;
							float sizedif = ICON_SIZE * distper;
							
							float xx = getWidth() * per;
							float yy = getHeight() / 2 - (ICON_SIZE / 2);
							
							double iconAngle = Math.atan(PERSON_SIZE_METERS / distance) * 180F / Math.PI;
							float curAngle = inclination * 9;
							float angdif = (float)iconAngle - curAngle;
							if(angdif < (VERTICAL_VIEW_ANGLE / -2F))
								yy = (ICON_SIZE - sizedif) / 2;
							else if(angdif > (VERTICAL_VIEW_ANGLE / 2F))
								yy = getHeight() - 17 - ((ICON_SIZE - sizedif) / 2);
							else
							{
								float vper = angdif / (VERTICAL_VIEW_ANGLE / 2F);
								yy = Math.min((getHeight() / 2) + (getHeight() / 2 * vper) - 17, getHeight() - 17 - ((ICON_SIZE - sizedif) / 2));
							}
							
							
							Bitmap scaled = Bitmap.createScaledBitmap(bb, ICON_SIZE - (int)sizedif, ICON_SIZE - (int)sizedif, true);
							
							String toDraw = Math.round(distance) + "m";
							
							canvas.drawBitmap(scaled, xx - ((ICON_SIZE - sizedif) / 2), Math.max(0, yy - ((ICON_SIZE - sizedif) / 2)), new Paint());
							canvas.drawText(toDraw, xx, yy + 17 + ((ICON_SIZE - sizedif) / 2), text);
						}
					}
				}
			}
		}
		else
			canvas.drawText("No GPS Signal", getWidth() / 2F, canvas.getHeight() / 2F, text);
		
		Paint text2 = new Paint();
		text2.setColor(Color.WHITE);
		text2.setTextSize(24);
		text2.setShadowLayer(5, 1, 1, Color.BLACK);
		text2.setTextAlign(Align.LEFT);

		String display = "Lat: " + latitude + " Long: " + longitude + " Heading: " + direction;
		canvas.drawText(display, 0, 20, text2);
	}
}
