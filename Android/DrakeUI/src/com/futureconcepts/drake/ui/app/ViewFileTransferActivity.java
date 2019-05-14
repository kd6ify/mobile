package com.futureconcepts.drake.ui.app;

import com.futureconcepts.drake.client.IFileTransferManager;
import com.futureconcepts.drake.client.MessengerServiceConnection;
import com.futureconcepts.drake.ui.R;
import com.futureconcepts.drake.ui.os.SimpleAlertHandler;
import com.futureconcepts.drake.ui.widget.FileTransferView;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

public class ViewFileTransferActivity extends Activity implements MessengerServiceConnection.Client
{
	private static final String TAG = ViewFileTransferActivity.class.getSimpleName();
	
    private MessengerServiceConnection _serviceConnection;

    private MyFileTransferViewListener _fileTransferViewListener;
    
    private FileTransferView _fileTransferView;

    private SimpleAlertHandler _handler;
            
    @Override
    protected void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.file_transfer_view);
        
        _fileTransferView = (FileTransferView)findViewById(R.id.fileTransferView);

        _handler = new SimpleAlertHandler(this);

        _serviceConnection = new MessengerServiceConnection(this, this);
    	_serviceConnection.connect();
    }

	@Override
	public void onMessengerServiceConnected()
	{
		resolveIntent(getIntent());
	}

	@Override
	public void onMessengerServiceDisconnected()
	{
	}

    @Override
    protected void onResume()
    {
        super.onResume();
  //      if (mChatGroupManager != null)
//        {
 //       	try
//			{
//				mChatGroupManager.registerChatGroupListener(mChatGroupListener);
//			}
//			catch (RemoteException e)
//			{
//				e.printStackTrace();
//			}
//        }
    }

    @Override
    protected void onPause()
    {
//        if (mChatGroupManager != null)
 //       {
  //      	if (mChatGroupListener != null)
   //     	{
	//        	try
	//			{
	//				mChatGroupManager.unregisterChatGroupListener(mChatGroupListener);
	//			}
	//			catch (RemoteException e)
	//			{
	//				e.printStackTrace();
	//			}
     //   	}
      //  }
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        resolveIntent(intent);
    }

    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	if (_serviceConnection != null)
    	{
    		_serviceConnection.disconnect();
    	}
    }
    
    private void resolveIntent(Intent intent)
    {
        _fileTransferView.bindFileTransfer(ContentUris.parseId(intent.getData()));
        _fileTransferViewListener = new MyFileTransferViewListener();
        _fileTransferView.setListener(_fileTransferViewListener);
 //       try
//		{
	//		mChatGroupManager.registerChatGroupListener(mChatGroupListener);
//		}
	//	catch (RemoteException e)
//		{
	//		e.printStackTrace();
		//	mHandler.showAlert("Problem viewing invitation", e.getMessage());
	//	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.invitation_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        }
        return super.onOptionsItemSelected(item);
    }
    
    private class MyFileTransferViewListener implements FileTransferView.Listener
    {
		@Override
		public void onFileTransferAccepted(long id)
		{
	        try
	        {
            	IFileTransferManager ftm = _serviceConnection.getConnection().getFileTransferManager();
                ftm.acceptFileTransferRequest(id);
	        }
	        catch (RemoteException e)
	        {
	            _handler.showServiceErrorAlert();
	        }
		}

		@Override
		public void onFileTransferRejected(long id)
		{
	        try
	        {
            	IFileTransferManager ftm = _serviceConnection.getConnection().getFileTransferManager();
                ftm.rejectFileTransferRequest(id);
	        }
	        catch (RemoteException e)
	        {
	            _handler.showServiceErrorAlert();
	        }
			finish();
		}
    }
}