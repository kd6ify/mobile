package com.futureconcepts.ax.trinity.ar;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback
{
	private SurfaceHolder holder;
	private Camera camera;
	
	public CameraSurfaceView(Context context, AttributeSet attrs)
	{
		super(context);
		
		holder = this.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		try
		{
			camera = Camera.open();
			if (camera != null)
			{
				camera.setPreviewDisplay(holder);
			}
		}
		catch(IOException e)
		{
			e.printStackTrace(System.out);
		}
	}

	@Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
    {
		if (camera != null)
		{
            camera.startPreview();
		}
    }
	
	@Override
    public void surfaceDestroyed(SurfaceHolder holder) 
    {
		if (camera != null)
		{
            camera.stopPreview();
            camera.release();
            camera = null;
		}
    }
}
