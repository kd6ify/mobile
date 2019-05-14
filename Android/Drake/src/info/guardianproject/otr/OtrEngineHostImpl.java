package info.guardianproject.otr;

//import info.guardianproject.otr.app.im.app.WarningDialogActivity;
import info.guardianproject.otr.app.im.engine.ChatSessionManager;
import info.guardianproject.otr.app.im.service.ChatSessionServiceImpl;
import info.guardianproject.otr.app.im.service.ChatSessionManagerServiceImpl;
import info.guardianproject.otr.app.im.service.ImConnectionServiceImpl;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Date;

import com.futureconcepts.drake.client.Message;

import net.java.otr4j.OtrEngineHost;
import net.java.otr4j.OtrKeyManagerListener;
import net.java.otr4j.OtrPolicy;
import net.java.otr4j.session.SessionID;
import android.content.Context;
import android.content.Intent;

/* OtrEngineHostImpl is the connects this app and the OtrEngine
 * http://code.google.com/p/otr4j/wiki/QuickStart
 */
public class OtrEngineHostImpl implements OtrEngineHost {
	
	private ImConnectionServiceImpl mConnection;
	private OtrPolicy mPolicy;
    
	private OtrAndroidKeyManagerImpl mOtrKeyManager;

	private final static String OTR_KEYSTORE_PATH ="otr_keystore";
	
	private Context mContext;
	
	public OtrEngineHostImpl(OtrPolicy policy, Context context) throws IOException 
	{
		mPolicy = policy;
		mContext = context;
		
		File storeFile = new File(context.getFilesDir(), OTR_KEYSTORE_PATH);		
		mOtrKeyManager = OtrAndroidKeyManagerImpl.getInstance(storeFile.getAbsolutePath());
		
		mOtrKeyManager.addListener(new OtrKeyManagerListener() {
			public void verificationStatusChanged(SessionID session) {
				
				String msg = session + ": verification status=" + mOtrKeyManager.isVerified(session);
				
				OtrDebugLogger.log( msg);
			}
		});
	}
	
	public void setConnection (ImConnectionServiceImpl imConnectionAdapter)
	{
		this.mConnection = imConnectionAdapter;

	}
	
	public OtrAndroidKeyManagerImpl getKeyManager ()
	{
		return mOtrKeyManager;
	}
	
	public void storeRemoteKey (SessionID sessionID, PublicKey remoteKey)
	{
		mOtrKeyManager.savePublicKey(sessionID, remoteKey);
	}
	
	public boolean isRemoteKeyVerified (SessionID sessionID)
	{
		return mOtrKeyManager.isVerified(sessionID);
	}
	
	public String getLocalKeyFingerprint (SessionID sessionID)
	{
		return mOtrKeyManager.getLocalFingerprint(sessionID);
	}
	
	public String getRemoteKeyFingerprint (SessionID sessionID)
	{
		return mOtrKeyManager.getRemoteFingerprint(sessionID);
	}
	
	@Override
	public KeyPair getKeyPair(SessionID sessionID) {
		KeyPair kp = null;
		kp = mOtrKeyManager.loadLocalKeyPair(sessionID);
		
		if (kp == null)
		{
         	mOtrKeyManager.generateLocalKeyPair(sessionID);	
         	kp = mOtrKeyManager.loadLocalKeyPair(sessionID);
		}
     	return kp;
	}

	@Override
	public OtrPolicy getSessionPolicy(SessionID sessionID) {
		return mPolicy;
	}
	
	public void setSessionPolicy (OtrPolicy policy)
	{
		mPolicy = policy;
	}
	
	private void sendMessage (SessionID sessionID, String body)
	{
		ChatSessionManagerServiceImpl chatSessionManagerAdapter = (ChatSessionManagerServiceImpl)mConnection.getChatSessionManager();
		ChatSessionServiceImpl chatSessionAdapter = (ChatSessionServiceImpl)chatSessionManagerAdapter.getChatSession(sessionID.getUserID());
		ChatSessionManager chatSessionManager = chatSessionManagerAdapter.getChatSessionManager();
		
		Message msg = new Message(body);
		
		msg.setFrom(mConnection.getLoginUser().getAddress());
		msg.setTo(chatSessionAdapter.getChatSession().getParticipant().getAddress());
		msg.setDateTime(new Date());
		msg.setID(msg.getFrom() + ":" + msg.getDateTime().getTime());
		chatSessionManager.sendMessageAsync(chatSessionAdapter.getChatSession(), msg);
	}
	
	private void sendLocalMessage (SessionID sessionID, String body)
	{
		ChatSessionManagerServiceImpl chatSessionManagerAdapter = (ChatSessionManagerServiceImpl)mConnection.getChatSessionManager();
		ChatSessionServiceImpl chatSessionAdapter = (ChatSessionServiceImpl)chatSessionManagerAdapter.getChatSession(sessionID.getUserID());
		ChatSessionManager chatSessionManager = chatSessionManagerAdapter.getChatSessionManager();
		
		Message msg = new Message(body);
		
		msg.setTo(mConnection.getLoginUser().getAddress());
		msg.setFrom(chatSessionAdapter.getChatSession().getParticipant().getAddress());
		msg.setDateTime(new Date());
		msg.setID(msg.getFrom() + ":" + msg.getDateTime().getTime());
		chatSessionManager.sendMessageAsync(chatSessionAdapter.getChatSession(), msg);
	}
	
	@Override
	public void injectMessage(SessionID sessionID, String text) {
		OtrDebugLogger.log( sessionID.toString() + ": injecting message: " + text);
		
		sendMessage(sessionID,text);
		
	}

	@Override
	public void showError(SessionID sessionID, String error) {
		OtrDebugLogger.log( sessionID.toString() + ": ERROR=" + error);
		
    	showDialog ("Encryption Error", error);
	}

	@Override
	public void showWarning(SessionID sessionID, String warning) {
		OtrDebugLogger.log( sessionID.toString() + ": WARNING=" +  warning);
		
    	showDialog ("Encryption Warning", warning);

	}
	
	private void showDialog (String title, String msg)
	{

//		Intent nIntent = new Intent(mContext, WarningDialogActivity.class);
//		nIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

//		nIntent.putExtra("title", title);
//		nIntent.putExtra("msg", msg);
		
//		mContext.startActivity(nIntent);
		
	}

}
