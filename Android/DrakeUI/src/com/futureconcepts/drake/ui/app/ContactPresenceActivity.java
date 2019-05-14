package com.futureconcepts.drake.ui.app;

import com.futureconcepts.drake.client.IOtrChatSession;
import com.futureconcepts.drake.client.IOtrKeyManager;
import com.futureconcepts.drake.client.Imps;
import com.futureconcepts.drake.client.MessengerServiceConnection;
import com.futureconcepts.drake.ui.R;
import com.futureconcepts.drake.ui.utils.PresenceUtils;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ContactPresenceActivity extends Activity implements MessengerServiceConnection.Client
{
	private final static String LOG_TAG = ContactPresenceActivity.class.getSimpleName();

	private MessengerServiceConnection _serviceConnection;
	private String remoteFingerprint;
	private boolean remoteFingerprintVerified = false;
	private String remoteAddress;
	
	private String localFingerprint;
		
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);

        setContentView(R.layout.contact_presence_activity);

     //   ImageView imgAvatar = (ImageView) findViewById(R.id.imgAvatar);
        TextView txtName = (TextView) findViewById(R.id.txtName);
        TextView txtStatus = (TextView) findViewById(R.id.txtStatus);
        TextView txtCustomStatus = (TextView) findViewById(R.id.txtStatusText);
  

        Intent i = getIntent();
        Uri uri = i.getData();
        if(uri == null) {
            warning("No data to show");
            finish();
            return;
        }
        

        if (i.getExtras() != null)
        {
	        remoteFingerprint = i.getExtras().getString("remoteFingerprint");
	        
	        if (remoteFingerprint != null)
	        {
	        	remoteFingerprintVerified = i.getExtras().getBoolean("remoteVerified");
	        	localFingerprint = i.getExtras().getString("localFingerprint");
	        }
	        
	        
        }
        
        updateUI();
        
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(uri, null, null, null, null);
        if(c == null) {
            warning("Database error when query " + uri);
            finish();
            return;
        }

        if(c.moveToFirst()) {
            remoteAddress = c.getString(c.getColumnIndexOrThrow(Imps.Contacts.USERNAME));
//            String nickname   = c.getString(c.getColumnIndexOrThrow(Imps.Contacts.NICKNAME));
            int status    = c.getInt(c.getColumnIndexOrThrow(Imps.Contacts.PRESENCE_STATUS));
//            int clientType = c.getInt(c.getColumnIndexOrThrow(Imps.Contacts.CLIENT_TYPE));
            String customStatus = c.getString(c.getColumnIndexOrThrow(Imps.Contacts.PRESENCE_CUSTOM_STATUS));

            setTitle(getString(R.string.contact_profile_title));

//            Drawable avatar = DatabaseUtils.getAvatarFromCursor(c,
//                    c.getColumnIndexOrThrow(Imps.Contacts.AVATAR_DATA));
            /*
            if (avatar != null) {
                imgAvatar.setImageDrawable(avatar);
            } else {
                imgAvatar.setImageResource(R.drawable.avatar_unknown);
            }*/

            txtName.setText(remoteAddress);

            String statusString = getString(PresenceUtils.getStatusStringRes(status));
            SpannableString s = new SpannableString("+ " + statusString);
            Drawable statusIcon = getResources().getDrawable(PresenceUtils.getStatusIconId(status));
            statusIcon.setBounds(0, 0, statusIcon.getIntrinsicWidth(),
                    statusIcon.getIntrinsicHeight());
            s.setSpan(new ImageSpan(statusIcon), 0, 1,
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            txtStatus.setText(s);

       //     txtClientType.setText(getClientTypeString(clientType));

            if (!TextUtils.isEmpty(customStatus)) {
                txtCustomStatus.setVisibility(View.VISIBLE);
                txtCustomStatus.setText("\"" + customStatus + "\"");
            } else {
                txtCustomStatus.setVisibility(View.GONE);
            }
        }
        c.close();
        _serviceConnection = new MessengerServiceConnection(this, this);
    }

	@Override
	public void onMessengerServiceConnected()
	{
		try
		{
	    	IOtrKeyManager okm = _serviceConnection.getConnection().getChatSessionManager().getChatSession(remoteAddress).getOtrKeyManager();
	    	okm.verifyKey(remoteAddress);
	    	remoteFingerprintVerified = true;
	    	updateUI ();
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
		}
		updateUI();
	}

	@Override
	public void onMessengerServiceDisconnected()
	{
		// TODO Auto-generated method stub
		
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
    
    private void updateUI ()
    {
       
        TextView lblFingerprintLocal = (TextView) findViewById(R.id.labelFingerprintLocal);
        TextView lblFingerprintRemote = (TextView) findViewById(R.id.labelFingerprintRemote);
        TextView txtFingerprintRemote = (TextView) findViewById(R.id.txtFingerprintRemote);
        TextView txtFingerprintLocal = (TextView) findViewById(R.id.txtFingerprintLocal);


        if (remoteFingerprint != null)
        {
        	txtFingerprintRemote.setText(remoteFingerprint);
        	
        	if (remoteFingerprintVerified)
        	{
        		lblFingerprintRemote.setText("Their Fingerprint (Verified)");
        		txtFingerprintRemote.setBackgroundColor(Color.GREEN);
        	}
        	else
        		txtFingerprintRemote.setBackgroundColor(Color.YELLOW);

        	txtFingerprintRemote.setTextColor(Color.BLACK);
        	
        	txtFingerprintLocal.setText(localFingerprint);
        }
        else
        {
        	txtFingerprintRemote.setVisibility(View.GONE);
        	txtFingerprintLocal.setVisibility(View.GONE);
        	lblFingerprintRemote.setVisibility(View.GONE);
        	lblFingerprintLocal.setVisibility(View.GONE);
	     }
        
        
       
    }

//    private String getClientTypeString(int clientType) {
//        Resources res = getResources();
//        switch (clientType) {
//            case Imps.Contacts.CLIENT_TYPE_MOBILE:
//                return res.getString(R.string.client_type_mobile);
//
//            default:
//                return res.getString(R.string.client_type_computer);
//        }
//    }

    private static void warning(String msg)
    {
        Log.w(LOG_TAG, msg);
    }
    
    private void confirmVerify ()
    {
    	String message = "Are you sure you want to confirm this key?";
    	
    	new AlertDialog.Builder(this)
        .setTitle("Verify key?")
        .setMessage(message)
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                verifyRemoteFingerprint();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        }).show();
    }
    
    private void verifyRemoteFingerprint ()
    {
    	Toast.makeText(this, "The remote key fingerprint has been verified!", Toast.LENGTH_SHORT).show();
	
		_serviceConnection.connect();
    }
    
    public void startScan ()
    {
    	IntentIntegrator.initiateScan(this);
    }
    
    public void displayQRCode (String text)
    {
    	IntentIntegrator.shareText(this, text);
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
	     
    	IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
	     
	     if (scanResult != null) {
	        
	    	 String otherFingerprint = scanResult.getContents();
	    	 
	    	 if (otherFingerprint != null && otherFingerprint.equals(remoteFingerprint))
	    	 {
	    		 verifyRemoteFingerprint();
	    	 }
	    	 
	    	
	      }
	     
    // else continue with any other code you need in the method
     }
    
    public boolean onCreateOptionsMenu(Menu menu) {
    	
    	if (remoteFingerprint != null)
    	{
    		MenuInflater inflater = getMenuInflater();
    		inflater.inflate(R.menu.contact_info_menu, menu);
    	}
    	
       
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	       	case R.id.menu_scan:
	        	startScan();
	        	return true;
	
	        case R.id.menu_fingerprint:
	        	if (remoteFingerprint!=null)
	        		displayQRCode(localFingerprint);
	            return true;
	            
	        case R.id.menu_verify_fingerprint:
	        	if (remoteFingerprint!=null)
	        		confirmVerify();
	        	return true;

	        case R.id.menu_verify_secret:
	        	if (remoteFingerprint!=null)
	        		initSmpUI();
	        	return true;
	        }
        return super.onOptionsItemSelected(item);
    }
    
    private void initSmpUI ()
    {
    	// Set an EditText view to get user input 
    	//final EditText input = new EditText(this);
    	String message = "Enter a question? and an answer.";
    	
    	LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
    	final View viewSmp = inflater.inflate(R.layout.smp_question_dialog, null, false);

    	new AlertDialog.Builder(this)
        .setTitle("OTR Q&A Verification")
        .setView(viewSmp)
        .setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
          
            	EditText eiQuestion = (EditText)viewSmp.findViewById(R.id.editSmpQuestion);
            	EditText eiAnswer = (EditText)viewSmp.findViewById(R.id.editSmpAnswer);
            	 String question = eiQuestion.getText().toString();
                 String answer = eiAnswer.getText().toString();
                initSmp (question, answer);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        }).show();
    }
    
    private void initSmp (final String question, final String answer)
    {
		_serviceConnection.connect();
//		{
//			@Override
//			public void run()
//			{
//				try
//				{
//			     	IOtrChatSession iOtrSession = _binder.getConnection().getChatSessionManager().getChatSession(remoteAddress).getOtrChatSession();
//	 				iOtrSession.initSmpVerification(question, answer);
//				}
//				catch (RemoteException e)
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		});
    }
}
