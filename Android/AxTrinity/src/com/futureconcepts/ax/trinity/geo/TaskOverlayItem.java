package com.futureconcepts.ax.trinity.geo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

import com.futureconcepts.ax.model.data.Icon;
import com.futureconcepts.ax.model.data.Tactic;
import com.futureconcepts.ax.model.data.TacticStatus;
import com.futureconcepts.ax.model.data.TacticType;
import com.futureconcepts.ax.trinity.Config;
import com.google.android.maps.OverlayItem;

public class TaskOverlayItem extends OverlayItem
{
	public TaskOverlayItem(Context context, Tactic tactic)
	{
		super(GeoPointHelper.getGeoPoint(tactic.getAddress(context)), tactic.getName(), tactic.getName());
		boolean markerFound = false;
		TacticType tacticType = tactic.getType(context);
		if (tacticType != null)
		{
			Icon icon = tacticType.getIcon(context);
			if (icon != null)
			{
				BitmapDrawable drawable = new BitmapDrawable(getScaledBitmap(context, icon));
			//	drawable.set
				String statusID = tactic.getStatusID();
				if (statusID != null )
				{
					if (statusID.equals(TacticStatus.ACTIVE))
					{
						Paint paint = new Paint(Color.BLUE);
						ColorFilter filter = new LightingColorFilter(Color.BLUE, 1);
						drawable.setColorFilter(filter);
//						paint.setColor(Color.BLUE);
					}
					else if (statusID.equals(TacticStatus.PENDING))
					{
						Paint paint = new Paint(Color.RED);
						ColorFilter filter = new LightingColorFilter(Color.RED, 1);
						drawable.setColorFilter(filter);
//						paint.setColor(Color.RED);
					}
					else if (statusID.equals(TacticStatus.COMPLETE))
					{
						Paint paint = new Paint(Color.GREEN);
						ColorFilter filter = new LightingColorFilter(Color.GREEN, 1);
						drawable.setColorFilter(filter);
//						paint.setColor(Color.GREEN);
					}
				}
				setMarker(drawable);
				setState(drawable, 1);
				markerFound = true;
			}
		}
		if (markerFound == false)
		{
			Drawable shapeDrawable = new ShapeDrawable(new OvalShape());
			setMarker(shapeDrawable);
			setState(shapeDrawable, 1);
		}
	}
	
	private Bitmap getScaledBitmap(Context context, Icon icon)
	{
		int newWidth = 48;
		int maxHeight = 48;
		byte[] bytes = icon.getImage();
		Bitmap unscaledBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		int unscaledWidth = unscaledBitmap.getWidth();
		int unscaledHeight = unscaledBitmap.getHeight();
		int newHeight = unscaledHeight * newWidth / unscaledWidth;
		if (newHeight > maxHeight)
		{
			// Resize with height instead
			newWidth = unscaledBitmap.getWidth() * maxHeight / unscaledBitmap.getHeight();
			newHeight = maxHeight;
		}
		float scale = context.getResources().getDisplayMetrics().density;
		int bump = Config.getMapIconSizeBump(context);
		int scaledWidth = (int)(newWidth * scale + 0.5f) + bump;
		int scaledHeight = (int)(newHeight * scale + 0.5f) + bump;
		return Bitmap.createScaledBitmap(unscaledBitmap, scaledWidth, scaledHeight, true);
	}
}
