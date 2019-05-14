package com.futureconcepts.ax.trinity;

import com.futureconcepts.ax.model.data.Icon;
import com.futureconcepts.ax.model.data.IndexedType;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class IndexedTypeAdapter extends ResourceCursorAdapter
{
	private int _iconBump;
	
	public IndexedTypeAdapter(Context context, IndexedType type)
	{
		super(context, R.layout.simple_list_item_2, type);
		setDropDownViewResource(R.layout.spinner_dropdown_item_2);
		_iconBump = Config.getMapIconSizeBump(context);
	}

	public IndexedTypeAdapter(Context context, IndexedType type, int res1, int res2)
	{
		super(context, res1, type);
		setDropDownViewResource(res2);
		_iconBump = Config.getMapIconSizeBump(context);
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor)
	{
		IndexedType type = (IndexedType)cursor;
		TextView textView = (TextView)view.findViewById(R.id.text1);
		textView.setText(type.getName());
		Icon icon = type.getIcon(context);
		if (icon != null)
		{
			((ImageView)view.findViewById(R.id.image)).setImageBitmap(getScaledBitmap(context, icon));
		}
		else
		{
			((ImageView)view.findViewById(R.id.image)).setImageBitmap(null);
		}
	}

	private Bitmap getScaledBitmap(Context context, Icon icon)
	{
		Bitmap result = null;
		byte[] bytes = icon.getImage();
		if (bytes != null)
		{
			int newWidth = 32;
			int maxHeight = 32;
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
			int scaledWidth = (int)(newWidth * scale + 0.5f) + _iconBump;
			int scaledHeight = (int)(newHeight * scale + 0.5f) + _iconBump;
			result = Bitmap.createScaledBitmap(unscaledBitmap, scaledWidth, scaledHeight, true);
		}
		return result;
	}
}
