package com.futureconcepts.ax.trinity.logs.images;


import com.futureconcepts.ax.trinity.Config;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class GetImageBitmap {
	//Generates the thumbail of the selected image. 
	public static Bitmap lessResolution(String filePath, int reqHeight, int reqWidth) {
		BitmapFactory.Options options = new BitmapFactory.Options();
			// First decode with inJustDecodeBounds=true to check dimensions
			options.inJustDecodeBounds = true;
			options.inPurgeable = true;
			BitmapFactory.decodeFile(filePath, options);
			// Calculate inSampleSize
			options.inSampleSize = calculateInSampleSize(options, reqWidth,reqHeight);
			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			return BitmapFactory.decodeFile(filePath, options);
	}

	private static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {

		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			// Calculate ratios of height and width to requested height and
			// width
		//	final int heightRatio = Math.round((float) height/ (float) reqHeight);
			//final int widthRatio = Math.round((float) width / (float) reqWidth);
			 final int halfHeight = height / 2;
		        final int halfWidth = width / 2;
			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			//inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		     // Calculate the largest inSampleSize value that is a power of 2 and keeps both
		        // height and width larger than the requested height and width.
		        while ((halfHeight / inSampleSize) > reqHeight
		                && (halfWidth / inSampleSize) > reqWidth) {
		            inSampleSize *= 2;
		        }
		}
		return inSampleSize;
	}
	
	
	public static Bitmap getScaledBitmapFromBytes(Context context, byte[] bytes)
	{
		int newWidth = 48;
		int maxHeight = 48;
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
		int bump = Config.getMapIconSizeBump(context);
		int scaledWidth = (int)(newWidth * scale + 0.5f) + bump;
		int scaledHeight = (int)(newHeight * scale + 0.5f) + bump;
		return Bitmap.createScaledBitmap(unscaledBitmap, scaledWidth, scaledHeight, true);
	}

}
