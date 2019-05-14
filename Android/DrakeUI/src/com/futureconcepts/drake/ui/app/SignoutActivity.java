package com.futureconcepts.drake.ui.app;

import com.futureconcepts.drake.client.IImConnection;
import com.futureconcepts.drake.client.MessengerServiceConnection;
import com.futureconcepts.drake.ui.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class SignoutActivity extends Activity implements MessengerServiceConnection.Client
{
	private static final String LOG_TAG = SignoutActivity.class.getSimpleName();
    private MessengerServiceConnection _serviceConnection;

    @Override
    protected void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);

        _serviceConnection = new MessengerServiceConnection(this, this);
    	_serviceConnection.connect();
    }

	@Override
	public void onMessengerServiceConnected()
	{
        try
        {	
            IImConnection conn = _serviceConnection.getConnection();
            if (conn != null)
            {
                conn.logout();
            }
        }
        catch (RemoteException ex)
        {
            Log.e(LOG_TAG, "signout: caught ", ex);
        }
        finally
        {
           finish();
           Toast.makeText(this, getString(R.string.signed_out_prompt), Toast.LENGTH_LONG).show();
        }
	}

	@Override
	public void onMessengerServiceDisconnected()
	{
		finish();
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

    @Override
    protected void onStop()
    {
        super.onStop();

        // always call finish here, because we don't want to be in the backlist ever, and
        // we don't handle onRestart()
    }
}
