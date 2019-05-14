package com.futureconcepts.ax.trinity.osm;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidBitmap;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.overlay.Marker;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Icon;
import com.futureconcepts.ax.model.data.Tactic;
import com.futureconcepts.ax.model.data.TacticPriority;
import com.futureconcepts.ax.model.data.TacticStatus;
import com.futureconcepts.ax.trinity.tasks.ViewPriorityTaskActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

public class TasksOverlay extends BaseClassMarker {
	private int TYPE_TASK = 1;
	private int TYPE_PRIORITY_TASK = 2;
	private String ID; 
	private Context context;
	private int Type;
	private String TAG = TasksOverlay.class.getSimpleName();
	
	public TasksOverlay(LatLong latLong, Bitmap bitmap, int horizontalOffset,
			int verticalOffset) {
		super(latLong, bitmap, horizontalOffset, verticalOffset);
		// TODO Auto-generated constructor stub
	}
	
	public boolean onTap(LatLong tapLatLong, Point layerXY, Point tapXY) {
		// TODO Auto-generated method stub
		if (contains(layerXY, tapXY))
		{
			Uri uri = Uri.withAppendedPath(Tactic.CONTENT_URI, getID());
			Log.d(TAG, "tapped on " + uri.toString());
			if (getType() == TYPE_TASK)
			{
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getContext().startActivity(intent);
			}
			else
			{
				Intent intent = new Intent(getContext(), ViewPriorityTaskActivity.class);
				intent.setData(uri);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);				
				getContext().startActivity(intent);
			}
			return true;
		}
		return false;
	}
	
	public static void createTaskItems(Context context, Layers layers,Tactic tactic)
	{
	//	Log.e("asd","Tactic vcount: "+);
		int count = tactic.getCount();
		if(tactic.getCount()>0){
			for (int i = 0; i < count; i++)
			{
				tactic.moveToPosition(i);
				Address address = tactic.getAddress(context);
				if (address != null)
				{
					String wkt = address.getWKT();
					{
						if (wkt != null)
						{
							if (wkt.contains("POINT") || wkt.contains("POLYGON"))
							{
								layers.add(createTacticMarker(tactic,context));
							}
						}
					}
				}
			}
		}
	}
	
	private static TasksOverlay createTacticMarker(Tactic tactic,Context context)
	{
		TacticPriority tacticType = tactic.getPriority(context);
		if (tacticType != null && !tacticType.isClosed())
		{
			if (tactic.getAddress(context) != null &&tactic.getAddress(context).getWKT() != null)
			{
				LatLong point = getGeoPointFromWKT(tactic.getAddress(context));
				Bitmap bitmap = getPriorityIcon(tacticType, context);
				org.mapsforge.core.graphics.Canvas ca  = AndroidGraphicFactory.createGraphicContext(new Canvas());
				Bitmap bitmap2 = AndroidGraphicFactory.INSTANCE.createBitmap(bitmap.getWidth(), bitmap.getHeight(), true);
				ca.setBitmap(bitmap2);
				bitmap2.setBackgroundColor(getPriorityColor(tactic.getStatusID()));
				ca.drawBitmap(bitmap, 0,0);
				TasksOverlay item = new TasksOverlay(point,bitmap2,0,-bitmap2.getHeight()/2);
				item.setID(tactic.getID());
				item.setContext(context);
				item.setTypeFromTactic(tactic);
				return item;
			}					
		}
		return null;
	}
	
	private static int getPriorityColor(String statusID)
	{
		if (statusID != null )
		{
			if (statusID.equals(TacticStatus.ACTIVE))
			{
				return Color.BLUE;
			}
			else if (statusID.equals(TacticStatus.PENDING))
			{
				return Color.RED;
			}
			else if (statusID.equals(TacticStatus.COMPLETE))
			{
				return Color.GREEN;
			}
		}
		return Color.TRANSPARENT;
	}
	
	private static Bitmap getPriorityIcon(TacticPriority tacticType,Context context)
	{
	
			Icon icon = tacticType.getIcon(context);
			if (icon != null)
			{
				byte[] bytes = icon.getImage();
				Drawable image = null;
				image =  new BitmapDrawable(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
				return (AndroidBitmap) AndroidGraphicFactory.convertToBitmap(image);
			}
		return null;
	}
	
	private static LatLong getGeoPointFromWKT(Address address)
	{
		LatLong result = null;
			if(address.getWKT().contains("POINT"))
			{
				String a =  address.getWKT().replaceAll("[()]", "");
				String [] g = a.replaceAll("POINT", "").split(" ");
				result = new LatLong(Double.parseDouble(g[1]), Double.parseDouble(g[0]));
			}else if(address.getWKT().contains("POLYGON"))
			{
				String a =  address.getWKT().replaceAll("[()]", "");
				String [] g = a.replaceAll("POLYGON", "").split(",");
				String [] f = g[0].split(" ");
				result = new LatLong(Double.parseDouble(f[1]), Double.parseDouble(f[0]));
			}
		return result;
	}
	
	private  void setTypeFromTactic(Tactic tactic){
		if(tactic.getPriorityID().equals(TacticPriority.NONE)){
			setType(TYPE_TASK);
		}else
		{
			setType(TYPE_PRIORITY_TASK);
		}
	}
	
	
	public String getID() {
		return ID;
	}


	public void setID(String iD) {
		ID = iD;
	}


	public Context getContext() {
		return context;
	}


	public void setContext(Context context) {
		this.context = context;
	}


	public int getType() {
		return Type;
	}


	public void setType(int type) {
		Type = type;
	}



}
