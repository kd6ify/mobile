package com.futureconcepts.jupiter.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class IconScaler
{
	public static Bitmap getScaledBitmapFromResource(Context context, int id, double widthDip, double heightDip)
	{
		Bitmap unscaledBitmap = BitmapFactory.decodeResource(context.getResources(), id);
		float scale = context.getResources().getDisplayMetrics().density;
		int scaledWidth = (int)(widthDip * scale + 0.5f);
		int scaledHeight = (int)(heightDip * scale + 0.5f);
		return Bitmap.createScaledBitmap(unscaledBitmap, scaledWidth, scaledHeight, true);
	}
}
