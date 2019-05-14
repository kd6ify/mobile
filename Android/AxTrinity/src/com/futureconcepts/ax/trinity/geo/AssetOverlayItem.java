package com.futureconcepts.ax.trinity.geo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Asset;
import com.futureconcepts.ax.model.data.AssetType;
import com.futureconcepts.ax.model.data.Equipment;
import com.futureconcepts.ax.model.data.EquipmentType;
import com.futureconcepts.ax.model.data.Icon;
import com.futureconcepts.ax.model.data.Person;
import com.futureconcepts.ax.model.data.User;
import com.futureconcepts.ax.model.data.UserType;
import com.futureconcepts.ax.trinity.Config;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class AssetOverlayItem extends OverlayItem
{
	public AssetOverlayItem(Context context, Asset asset)
	{
		super(getGeoPointFromWKT(asset.getAddress(context)), getTitle(context, asset), getSnippet(asset));
		boolean markerFound = false;
		if (asset.getTypeID().equals(AssetType.EQUIPMENT))
		{
			Equipment equipment = asset.getEquipment(context);
			if (equipment != null)
			{
				EquipmentType equipmentType = equipment.getType(context);
				if (equipmentType != null)
				{
					Icon icon = equipmentType.getIcon(context);
					if (icon != null)
					{
						Drawable drawable = new BitmapDrawable(getScaledBitmap(context, icon));
						setMarker(drawable);
						setState(drawable, 1);
						markerFound = true;
					}
				}
			}
		}
		else if (asset.getTypeID().equals(AssetType.USER))
		{
			User user = asset.getUser(context);
			if (user != null)
			{
				UserType userType = user.getType(context);
				if (userType != null)
				{
					Icon icon = userType.getIcon(context);
					if (icon != null)
					{
						Drawable drawable = new BitmapDrawable(getScaledBitmap(context, icon));
						setMarker(drawable);
						setState(drawable, 1);
						markerFound = true;
					}
				}
			}
		}
		if (markerFound == false)
		{
			Drawable shapeDrawable = new ShapeDrawable(new OvalShape());
			setMarker(shapeDrawable);
			setState(shapeDrawable, 1);
		}
	}
	
	private static GeoPoint getGeoPointFromWKT(Address address)
	{
		GeoPoint result = null;
		if (address != null && address.getWKT() != null)
		{
			Point point = address.getWKTAsPoint();
			result = new GeoPoint(point.y, point.x);
		}
		else
		{
			result = new GeoPoint(0,0);
		}
		return result;
	}
	
	private static String getTitle(Context context, Asset asset)
	{
		String result = asset.getCallsign();
		if (result == null)
		{
			if (asset.getType(context).equals(Asset.EQUIPMENT_TYPE))
			{
				Equipment equipment = asset.getEquipment(context);
				if (equipment != null)
				{
					result = equipment.getName();
				}
			}
			else if (asset.getType(context).equals(Asset.USER_TYPE))
			{
				User user = asset.getUser(context);
				if (user != null)
				{
					result = user.getEmployeeNo();
					if (result == null)
					{
						Person person = user.getPerson(context);
						if (person != null)
						{
							result = person.getName();
						}
					}
				}
			}
		}
		return result;
	}
	
	private static String getSnippet(Asset asset)
	{
		return null;
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
