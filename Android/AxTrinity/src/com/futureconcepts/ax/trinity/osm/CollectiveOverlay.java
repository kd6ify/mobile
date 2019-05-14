package com.futureconcepts.ax.trinity.osm;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.layer.Layers;

import android.content.Context;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Collection;
import com.futureconcepts.ax.model.data.Tactic;

public class CollectiveOverlay extends BaseClassMarker{

	public CollectiveOverlay(LatLong latLong, Bitmap bitmap,
			int horizontalOffset, int verticalOffset) {
		super(latLong, bitmap, horizontalOffset, verticalOffset);
		// TODO Auto-generated constructor stub
	}
	
	public static void createCollectiveItems(Context context, Layers layers,Collection collective)
	{
		int count = collective.getCount();
		if(collective.getCount()>0){
			for (int i = 0; i < count; i++)
			{
				collective.moveToPosition(i);
				Address address = collective.getAddress(context);
				if (address != null)
				{
					String wkt = address.getWKT();
					{
						if (wkt != null)
						{
							if (wkt.contains("POINT") || wkt.contains("POLYGON"))
							{
								//layers.add(createTacticMarker(tactic,context));
							}
						}
					}
				}
			}
		}
	}

}
