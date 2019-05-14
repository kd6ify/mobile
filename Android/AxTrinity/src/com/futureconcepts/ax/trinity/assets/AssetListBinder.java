package com.futureconcepts.ax.trinity.assets;

import com.futureconcepts.ax.model.data.Asset;
import com.futureconcepts.ax.model.data.AssetType;
import com.futureconcepts.ax.model.data.CollectionType;
import com.futureconcepts.ax.model.data.Equipment;
import com.futureconcepts.ax.model.data.Icon;
import com.futureconcepts.ax.model.data.IndexedType;
import com.futureconcepts.ax.model.data.Person;
import com.futureconcepts.ax.model.data.User;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.logs.images.ImageManager;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AssetListBinder
{
	public static void bindView(View view, Context context, Asset asset, ImageManager imageManager)
	{
		TextView callsignView = (TextView)view.findViewById(R.id.callsign);
		String callsign = asset.getCallsign();
		if (callsign != null)
		{
			callsignView.setText(callsign);
		}
		else
		{
			callsignView.setText("");
		}
		String assetTypeID = asset.getTypeID();
		IndexedType specificType = null;
		if (assetTypeID != null)
		{
			if (assetTypeID.equals(AssetType.EQUIPMENT))
			{
				if (asset.getEquipmentID() != null)
				{
					Equipment equipment = asset.getEquipment(context);
					specificType = equipment.getType(context);
				}
				else
				{
					specificType = asset.getEquipmentType(context);
				}
				((TextView)view.findViewById(R.id.name)).setText(getEquipmentName(context, asset));
			}
			else if (assetTypeID.equals(AssetType.USER))
			{
				if (asset.getUserID() != null)
				{
					User user = asset.getUser(context);
					specificType = user.getType(context);
				}
				else
				{
					specificType = asset.getUserType(context);
				}
				((TextView)view.findViewById(R.id.name)).setText(getPersonnelName(context, asset));
			}
		}
		if (specificType != null && specificType.getCount() == 1)
		{
			((TextView)view.findViewById(R.id.type_name)).setText(specificType.getName());
			//Icon icon = specificType.getIcon(context);
//			if (icon != null)
//			{
//				byte[] bytes = icon.getImage();
//				((ImageView)view.findViewById(R.id.type_icon)).setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
//			}
			imageManager.displayImage( specificType.getIconID(), ((ImageView)view.findViewById(R.id.type_icon)), android.R.drawable.ic_menu_gallery,null);
//			Icon icon = collection.getIcon(context);
//			String iconID = null;
//			if(icon!=null)
//				iconID = icon.getIconID();
//			if (collection.getTypeID() != null)
//			{
//				CollectionType collectionType = collection.getType(context);
//				((TextView)view.findViewById(R.id.type_name)).setText(collectionType.getName());
//				if (icon == null)
//				{
//					iconID = collectionType.getIconID();
//				}
//			}
//			//icon.getID();
//			view.findViewById(R.id.type_icon).setTag(iconID);
//			imageManager.displayImage(iconID, ((ImageView)view.findViewById(R.id.type_icon)), android.R.drawable.ic_menu_gallery,null);
//		
		}
	}
	private static String getEquipmentName(Context context, Asset asset)
	{
		String result = "";
		Equipment equipment = asset.getEquipment(context);
		if (equipment != null && equipment.getCount() == 1)
		{
			result = equipment.getName();
		}
		return result;
	}
	private static String getPersonnelName(Context context, Asset asset)
	{
		String result = "";
		User user = asset.getUser(context);
		if (user != null)
		{
			Person person = user.getPerson(context);
			if (person != null)
			{
				result = person.getName();
			}
		}
		return result;
	}
}
