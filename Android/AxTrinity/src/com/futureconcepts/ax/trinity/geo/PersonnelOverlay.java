package com.futureconcepts.ax.trinity.geo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.futureconcepts.ax.model.data.Asset;
import com.futureconcepts.ax.trinity.R;
import com.google.android.maps.ItemizedOverlay;

public class PersonnelOverlay extends ItemizedOverlay<PersonnelOverlayItem>
{
	private static final String TAG = PersonnelOverlay.class.getSimpleName();
	private Context _context;
	private PersonnelViewCursorByCheckIn _personnel;
	
	public PersonnelOverlay(Context context, PersonnelViewCursorByCheckIn cursor)
	{
		super(context.getResources().getDrawable(R.drawable.placemark));
		_context = context;
		_personnel = cursor;
		populate();
	}

	public void repopulate()
	{
		populate();
	}
	
	public void repopulate(PersonnelViewCursorByCheckIn cursor)
	{
		_personnel = cursor;
		populate();
	}
	
	@Override
	protected PersonnelOverlayItem createItem(int i)
	{
		_personnel.moveToPosition(i);
		PersonnelOverlayItem item = new PersonnelOverlayItem(_context, _personnel);
		boundCenterBottom(item.getMarker(1));
		return item;
	}

	@Override
	public int size()
	{
		return _personnel.getCount();
	}
	
	@Override
	public boolean onTap(int i)
	{
		_personnel.moveToPosition(i);
		if (_context != null)
		{
			Uri uri = Uri.withAppendedPath(Asset.CONTENT_URI, _personnel.getID());
			Log.d(TAG, "tapped on " + uri.toString());
			_context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
		}
		return true;
	}
}
