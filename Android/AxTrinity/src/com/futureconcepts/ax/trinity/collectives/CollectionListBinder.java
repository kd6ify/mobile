package com.futureconcepts.ax.trinity.collectives;

import com.futureconcepts.ax.model.data.Collection;
import com.futureconcepts.ax.model.data.CollectionType;
import com.futureconcepts.ax.model.data.Icon;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.logs.images.ImageManager;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class CollectionListBinder
{
	public static void bindView(View view, Context context, Collection collection, ImageManager imageManager)
	{
		TextView callsignView = (TextView)view.findViewById(R.id.callsign);
		String callsign = collection.getCallsign();
		if (callsign != null)
		{
			callsignView.setText(callsign);
		}
		else
		{
			callsignView.setText("");
		}
		Icon icon = collection.getIcon(context);
		String iconID = null;
		if(icon!=null)
			iconID = icon.getIconID();
		if (collection.getTypeID() != null)
		{
			CollectionType collectionType = collection.getType(context);
			((TextView)view.findViewById(R.id.type_name)).setText(collectionType.getName());
			if (icon == null)
			{
				iconID = collectionType.getIconID();
			}
		}
		//icon.getID();
		view.findViewById(R.id.type_icon).setTag(iconID);
		imageManager.displayImage(iconID, ((ImageView)view.findViewById(R.id.type_icon)), android.R.drawable.ic_menu_gallery,null);
	}
}
