/**
 * 
 */
package info.guardianproject.otr;

import com.futureconcepts.drake.client.IOtrKeyManager;

import net.java.otr4j.session.SessionID;
import android.os.RemoteException;

/**
 * @author n8fr8
 *
 */
public class OtrKeyManagerAdapter extends IOtrKeyManager.Stub {

	private OtrAndroidKeyManagerImpl _keyManager;

	private SessionID _sessionId;
	
	public OtrKeyManagerAdapter (OtrAndroidKeyManagerImpl keyManager, SessionID sessionId)
	{
		_keyManager = keyManager;
		_sessionId = sessionId;
	}	
	
	public OtrKeyManagerAdapter (OtrAndroidKeyManagerImpl keyManager)
	{
	}	
	
	public void setSessionId(SessionID _sessionId) {
		this._sessionId = _sessionId;
	}

	/* (non-Javadoc)
	 * @see info.guardianproject.otr.IOtrKeyManager#verifyKey(java.lang.String)
	 */
	@Override
	public void verifyKey(String address) throws RemoteException {
		
		
		_keyManager.verifyUser(address);
		
	}

	/* (non-Javadoc)
	 * @see info.guardianproject.otr.IOtrKeyManager#unverifyKey(java.lang.String)
	 */
	@Override
	public void unverifyKey(String address) throws RemoteException {
		
		_keyManager.unverifyUser(address);

	}

	/* (non-Javadoc)
	 * @see info.guardianproject.otr.IOtrKeyManager#isKeyVerified(java.lang.String)
	 */
	@Override
	public boolean isKeyVerified(String address) throws RemoteException {
		return _keyManager.isVerifiedUser(address);
	}

	/* (non-Javadoc)
	 * @see info.guardianproject.otr.IOtrKeyManager#getLocalFingerprint(java.lang.String)
	 */
	@Override
	public String getLocalFingerprint() throws RemoteException {
		
		if (_sessionId != null)
			return _keyManager.getLocalFingerprint(_sessionId);
		else
			return null;
	}

	/* (non-Javadoc)
	 * @see info.guardianproject.otr.IOtrKeyManager#getRemoteFingerprint(java.lang.String)
	 */
	@Override
	public String getRemoteFingerprint() throws RemoteException {
		
		return _keyManager.getRemoteFingerprint(_sessionId);
	}

	/* (non-Javadoc)
	 * @see info.guardianproject.otr.IOtrKeyManager#generateLocalKeyPair(java.lang.String)
	 */
	@Override
	public void generateLocalKeyPair() throws RemoteException {
		
		if (_sessionId != null)
			_keyManager.generateLocalKeyPair(_sessionId);
	}
}
