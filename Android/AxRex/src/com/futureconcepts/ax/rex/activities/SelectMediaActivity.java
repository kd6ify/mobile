package com.futureconcepts.ax.rex.activities;

import com.futureconcepts.ax.rex.R;
import com.futureconcepts.ax.rex.os.SimpleAlertHandler;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;

public class SelectMediaActivity extends Activity
{
	private static final int REQUEST_PICK_IMAGE = 1;
	
	private SimpleAlertHandler _handler;
	private Uri _data;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_media_activity);
        _data = getIntent().getData();
        _handler = new SimpleAlertHandler(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
    	if (requestCode == REQUEST_PICK_IMAGE)
    	{
    		if (resultCode == Activity.RESULT_OK)
    		{
    			_handler.showAlert("Image attached", "Image attached");
    		}
    	}
    }
        
    public void onAttachImageClick(View view)
    {
		Intent i = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		try
		{
			startActivityForResult(i, REQUEST_PICK_IMAGE);
		}
		catch (Exception e)
		{
			_handler.showAlert("Error launching image picker", e.getMessage());
		}
    }
    
    public void onNextClick(View view)
    {
    	Intent intent = new Intent(this, InputDescriptionActivity.class);
    	intent.setData(_data);
    	startActivity(intent);
    	finish();
    }
    
    public void onDoneClick(View view)
    {
    	Intent intent = new Intent(this, SubmitRequestActivity.class);
    	intent.setData(_data);
    	startActivity(intent);
    	finish();
    }
}
