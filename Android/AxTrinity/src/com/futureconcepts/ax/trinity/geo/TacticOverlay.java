package com.futureconcepts.ax.trinity.geo;

import java.io.Closeable;
import java.io.IOException;

import android.content.Intent;
import android.net.Uri;

import com.futureconcepts.ax.model.data.Tactic;
import com.futureconcepts.ax.trinity.R;
import com.google.android.maps.ItemizedOverlay;

public class TacticOverlay extends ItemizedOverlay<TacticOverlayItem> implements Closeable
{
	private Tactic _tactic;
	private MainMapActivity _context;

	public TacticOverlay(MainMapActivity context, Tactic tactic)
	{
		super(context.getResources().getDrawable(R.drawable.placemark));
		_context = context;
		_tactic = tactic;
		populate();
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	protected TacticOverlayItem createItem(int i)
	{
		_tactic.moveToPosition(i);
		TacticOverlayItem item = new TacticOverlayItem(_context, _tactic);
		boundCenterBottom(item.getMarker(1));
		return item;
	}

	@Override
	public int size()
	{
		return _tactic.getCount();
	}
	
	@Override
	public boolean onTap(int i)
	{
		_tactic.moveToPosition(i);
		_context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(Tactic.CONTENT_URI, _tactic.getID())));
		return true;
	}
}
