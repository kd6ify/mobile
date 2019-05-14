package com.futureconcepts.ax.trinity.geo;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Icon;
import com.futureconcepts.ax.model.data.Tactic;
import com.futureconcepts.ax.model.data.TacticPriority;
import com.futureconcepts.ax.model.data.TacticStatus;
import com.futureconcepts.ax.trinity.tasks.ViewPriorityTaskActivity;
import com.google.android.maps.MapView;

public class TaskLayer extends Layer
{
	private static final String TAG = TaskLayer.class.getSimpleName();
	private Tactic _tactic;
	private int _size;
	private int[] _positionMap;
	private int _itemCount = -1;
	
	public TaskLayer(MapView mapView, Tactic tactic)
	{
		super(mapView);
		_tactic = tactic;
		_size = size();
	}

	@Override
	protected void populate()
	{
		for (int i = 0; i < _size; i++)
		{
			addChildView(createMarker(i));
		}
	}

	@Override
	public boolean onTap(int i)
	{
		int position = _positionMap[i];
		_tactic.moveToPosition(position);
		Uri uri = Uri.withAppendedPath(Tactic.CONTENT_URI, _tactic.getID());
		Log.d(TAG, "tapped on " + uri.toString());
		if (_tactic.getPriorityID().equals(TacticPriority.NONE))
		{
			getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri));
		}
		else
		{
			Intent intent = new Intent(getContext(), ViewPriorityTaskActivity.class);
			intent.setData(uri);
			getContext().startActivity(intent);
		}
		return true;
	}
	
	private int size()
	{
		if (_itemCount == -1)
		{
			_itemCount = 0;
			int count = _tactic.getCount();
			_positionMap = new int[count];
			for (int i = 0; i < count; i++)
			{
				_tactic.moveToPosition(i);
				Address address = _tactic.getAddress(getContext());
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
		return _itemCount; 
	}
	
	private View createMarker(final int i)
	{
		int position = _positionMap[i];
		_tactic.moveToPosition(position);
		ImageView imageView = new ImageView(getContext());
		MapView.LayoutParams params = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT, MapView.LayoutParams.WRAP_CONTENT,
				GeoPointHelper.getGeoPoint(_tactic.getAddress(getContext())), MapView.LayoutParams.BOTTOM_CENTER);
		imageView.setLayoutParams(params);
		TacticPriority tacticPriority = _tactic.getPriority(getContext());
		if (tacticPriority != null)
		{
			Icon icon = tacticPriority.getIcon(getContext());
			if (icon != null)
			{
				byte[] bytes = icon.getImage();
				if (bytes != null)
				{
					imageView.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
					String statusID = _tactic.getStatusID();
					if (statusID.equals(TacticStatus.ACTIVE))
					{
						imageView.setBackgroundColor(Color.BLUE);
					}
					else if (statusID.equals(TacticStatus.PENDING))
					{
						imageView.setBackgroundColor(Color.RED);
					}
					else if (statusID.equals(TacticStatus.COMPLETE))
					{
						imageView.setBackgroundColor(Color.GREEN);
					}
					imageView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v)
						{
							onTap(i);
						}
					});
				}
			}
// FIXME: see mantis 6374
//			String priorityID = _tactic.getPriorityID();
//			if (priorityID.equals(TacticPriority.IMMEDIATE))
//			{
//				AlphaAnimation alpha = new AlphaAnimation(0.1f, 1.0f);
//				alpha.setDuration(2000);
//				alpha.setRepeatMode(Animation.RESTART);
//				alpha.setRepeatCount(Animation.INFINITE);
//				imageView.startAnimation(alpha);
//			}
		}
		return imageView;
	}
}
