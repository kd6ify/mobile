package com.futureconcepts.ax.trinity.geo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Asset;
import com.futureconcepts.ax.trinity.R;
import com.google.android.maps.ItemizedOverlay;

public class AssetOverlay extends ItemizedOverlay<AssetOverlayItem>
{
	private static final String TAG = AssetOverlay.class.getSimpleName();
	private Context _context;
	private Asset _asset;
	private int[] _positionMap;
	private int _itemCount = -1;
	
	public AssetOverlay(Context context, Asset asset)
	{
		super(context.getResources().getDrawable(R.drawable.placemark));
		_context = context;
		_asset = asset;
		populate();
	}

	public void repopulate()
	{
		_itemCount = -1;
		populate();
	}
	
	@Override
	protected AssetOverlayItem createItem(int i)
	{
		int position = _positionMap[i];
		_asset.moveToPosition(position);
		AssetOverlayItem item = new AssetOverlayItem(_context, _asset);
		boundCenterBottom(item.getMarker(1));
		return item;
	}

	@Override
	public int size()
	{
		try
		{
			if (_itemCount == -1)
			{
				_itemCount = 0;
				int count = _asset.getCount();
				_positionMap = new int[count];
				for (int i = 0; i < count; i++)
				{
					_asset.moveToPosition(i);
					Address address = _asset.getAddress(_context);
					if (address != null)
					{
						String wkt = address.getWKT();
						{
							if (wkt != null)
							{
								if (wkt.contains("POINT") || wkt.contains("POLYGON"))
								{
									_positionMap[_itemCount++] = i;
								}
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			_itemCount = 0;
		}
		return _itemCount; 
	}
	
	@Override
	public boolean onTap(int i)
	{
		int position = _positionMap[i];
		_asset.moveToPosition(position);
		if (_context != null)
		{
			Uri uri = Uri.withAppendedPath(Asset.CONTENT_URI, _asset.getID());
			Log.d(TAG, "tapped on " + uri.toString());
			_context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
		}
		return true;
	}
}
