package com.futureconcepts.ax.video.viewer;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.MediaController;
import android.widget.VideoView;

public class ViewVideoActivity extends Activity implements OnCompletionListener, OnErrorListener, OnPreparedListener
{
	public static final String TAG = ViewVideoActivity.class.getSimpleName();
	
	private VideoView _videoView;
	private MediaController _mediaController;
	private String _streamID;
	private String _streamName;
	private Uri _uri;
	
    public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		//Create a VideoView widget in the layout file
		//use setContentView method to set content of the activity to the layout file which contains videoView
		setContentView(R.layout.view_video);

		_streamID = getIntent().getStringExtra("ID");
		_streamName = getIntent().getStringExtra("Name");
		_uri = getIntent().getData();
		startViewer();
    }

    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    }
    
	@Override
	public void onPrepared(MediaPlayer mediaPlayer)
	{
		Log.d(TAG, "VideoView.onPrepared");
	}

	@Override
	public boolean onError(MediaPlayer mediaPlayer, int what, int extra)
	{
		Log.d(TAG, "VideoView.onError " + what + " " + extra);
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mediaPlayer)
	{
		Log.d(TAG, "VideoView.onCompletion");
	}

	private void startViewer()
	{
		Log.d(TAG, "startViewer Uri: " + _uri.toString());
		try
		{
			_videoView = (VideoView)this.findViewById(R.id.videoView);
			_videoView.setOnCompletionListener(this);
			_videoView.setOnErrorListener(this);
			_videoView.setOnPreparedListener(this);
			
			//add controls to a MediaPlayer like play, pause.
			_mediaController = new MediaController(this);
			_mediaController.setMediaPlayer(_videoView);
			_videoView.setMediaController(_mediaController);
			
			//Set the path of Video or URI
			_videoView.setVideoURI(_uri);
			//
			
			//Set the focus
			_videoView.requestFocus();
			_videoView.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
