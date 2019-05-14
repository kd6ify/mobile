package com.futureconcepts.ax.trinity.geo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.futureconcepts.ax.model.data.Asset;
import com.futureconcepts.ax.trinity.R;
import com.google.android.maps.ItemizedOverlay;

public class EquipmentOverlay extends ItemizedOverlay<EquipmentOverlayItem>
{
	private static final String TAG = EquipmentOverlay.class.getSimpleName();
	private Context _context;
	private EquipmentViewCursorByCheckIn _equipment;
	
	public EquipmentOverlay(Context context, EquipmentViewCursorByCheckIn cursor)
	{
		super(context.getResources().getDrawable(R.drawable.placemark));
		_context = context;
		_equipment = cursor;
		populate();
	}

	public void repopulate(EquipmentViewCursorByCheckIn equipment)
	{
		_equipment = equipment;
		populate();
	}
	
	@Override
	protected EquipmentOverlayItem createItem(int i)
	{
		_equipment.moveToPosition(i);
		EquipmentOverlayItem item = new EquipmentOverlayItem(_context, _equipment);
		boundCenterBottom(item.getMarker(1));
		return item;
	}

	@Override
	public int size()
	{
		if(!_equipment.isClosed()){
			return _equipment.getCount();
		}else{ 
			return 0;
		}
	}
	
	@Override
	public boolean onTap(int i)
	{
		_equipment.moveToPosition(i);
		if (_context != null)
		{
			Uri uri = Uri.withAppendedPath(Asset.CONTENT_URI, _equipment.getID());
			Log.d(TAG, "tapped on " + uri.toString());
			_context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
		}
		return true;
	}
}
