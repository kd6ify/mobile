package info.guardianproject.otr.app.im.plugin.xmpp;

import info.guardianproject.otr.app.im.engine.ChatGroup;
import info.guardianproject.otr.app.im.engine.ChatGroupManager;
import info.guardianproject.otr.app.im.engine.ChatSession;
import info.guardianproject.otr.app.im.engine.ChatSessionManager;
import info.guardianproject.otr.app.im.engine.ContactList;
import info.guardianproject.otr.app.im.engine.ContactListListener;
import info.guardianproject.otr.app.im.engine.ContactListManager;
import info.guardianproject.otr.app.im.engine.EngineFileTransferManager;
import info.guardianproject.otr.app.im.engine.GroupListener;
import info.guardianproject.otr.app.im.engine.ImConnection;
import info.guardianproject.otr.app.im.engine.ImException;
import info.guardianproject.util.DNSUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.harmony.javax.security.auth.callback.Callback;
import org.apache.harmony.javax.security.auth.callback.CallbackHandler;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.AndroidContext;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.proxy.ProxyInfo;
import org.jivesoftware.smack.proxy.ProxyInfo.ProxyType;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.ChatState;
import org.jivesoftware.smackx.EntityCapsManager;
import org.jivesoftware.smackx.PEPManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransfer.StatusChangedListener;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.DelayInfo;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.MUCUser;
import org.jivesoftware.smackx.packet.VCard;

import com.futureconcepts.drake.client.Contact;
import com.futureconcepts.drake.client.FileTransferParcel;
import com.futureconcepts.drake.client.FileTransferRequestParcel;
import com.futureconcepts.drake.client.ImEntity;
import com.futureconcepts.drake.client.ImErrorInfo;
import com.futureconcepts.drake.client.Imps;
import com.futureconcepts.drake.client.Invitation;
import com.futureconcepts.drake.client.Message;
import com.futureconcepts.drake.client.Presence;
import com.futureconcepts.drake.config.IXMPPConfig;
import com.futureconcepts.drake.config.XMPPConfigFactory;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class XmppConnection extends ImConnection implements CallbackHandler
{
	private final static String TAG = XmppConnection.class.getSimpleName();
	private final static boolean DEBUG_ENABLED = true;
	
	private Contact mUser;
	
	// watch out, this is a different XMPPConnection class than XmppConnection! ;)
	// Synchronized by executor thread
	private MyXMPPConnection mConnection;
	private XmppStreamHandler mStreamHandler;

	private XmppContactList mContactListManager;
	private XmppFileTransferManager mFileTransferManager;
	private XmppChatSessionManager mSessionManager;
	private XmppChatGroupManager mChatGroupManager;
	private ConnectionConfiguration mConfig;
	
	// True if we are in the process of reconnecting.  Reconnection is retried once per heartbeat.
	// Synchronized by executor thread.
	private boolean mNeedReconnect;
	
	private boolean mRetryLogin;
	private ThreadPoolExecutor mExecutor;

	private ProxyInfo mProxyInfo = null;
	
	private String mPasswordTemp;
	
	private final static String TRUSTSTORE_TYPE = "BKS";
	private final static String TRUSTSTORE_PATH = "cacerts.bks";
	private final static String TRUSTSTORE_PASS = "changeit";
	private final static String KEYMANAGER_TYPE = "X509";
	private final static String SSLCONTEXT_TYPE = "TLS";
	
	private ServerTrustManager sTrustManager;
	private SSLContext sslContext;	
	private KeyStore ks = null;
    private KeyManager[] kms = null;
	
	private final static int SOTIMEOUT = 15000;
	
	private PacketCollector mPingCollector;
	private String mUsername;
	private String mNickname;
	private String mPassword;
	private String mResource;
	
	public XmppConnection(Context context)
	{
		super(context);
		AndroidContext.getInstance(context);
		
		SmackConfiguration.setPacketReplyTimeout(SOTIMEOUT);
		
		// Create a single threaded executor.  This will serialize actions on the underlying connection.
		createExecutor();
		
		XmppStreamHandler.addExtensionProviders();
		DeliveryReceipts.addExtensionProviders();

		ServiceDiscoveryManager.setIdentityName("Drake");
		ServiceDiscoveryManager.setIdentityType("phone");
		ServiceDiscoveryManager.setNonCapsCaching(true);
		
	}


	@Override
	public void close()
	{
		if (mConnection != null)
		{
			mConnection.disconnect();
			mConnection.shutdown();
			mConnection = null;
		}
		if (mExecutor != null)
		{
			mExecutor.shutdownNow();
		}
	}
	
	private void createExecutor()
	{
		mExecutor = new ThreadPoolExecutor(1, 1,
				0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue< Runnable >());
	}
	
	private boolean execute(Runnable runnable)
	{
		try
		{
			mExecutor.execute(runnable);
		}
		catch (RejectedExecutionException ex)
		{
			return false;
		}
		return true;
	}
	
	// Execute a runnable only if we are idle
	private boolean executeIfIdle(Runnable runnable)
	{
		if (mExecutor.getActiveCount() + mExecutor.getQueue().size() == 0)
		{
			return execute(runnable);
		}
		return false;
	}
	
	public void join() throws InterruptedException
	{
		ExecutorService oldExecutor = mExecutor;
		createExecutor();
		oldExecutor.shutdown();
		oldExecutor.awaitTermination(10, TimeUnit.SECONDS);
	}
	
	public void sendPacket(final org.jivesoftware.smack.packet.Packet packet)
	{
		execute(new Runnable()
		{
			@Override
			public void run()
			{
				if (mConnection == null)
				{
					Log.w(TAG, "dropped packet to " + packet.getTo() + " because we are not connected");
					return;
				}
				try
				{
					mConnection.sendPacket(packet);
				}
				catch (IllegalStateException ex)
				{
					Log.w(TAG, "dropped packet to " + packet.getTo() + " because socket is disconnected");
				}
			}
		});
	}
	
	@Override
	public void publishLocationAsync(final Location location)
	{
		execute(new Runnable()
		{
			@Override
			public void run()
			{
				if (mConnection == null)
				{
					Log.w(TAG, "dropped UserLoc publish because we are not connected");
					return;
				}
				if (location != null)
				{
					PEPManager pepManager = new PEPManager(mConnection);
					UserLoc userLoc = new UserLoc(Packet.nextID(), location);
					pepManager.publish(userLoc);
					pepManager.destroy();
				}
			}
		});
	}
	
	 public VCard getVCard(String myJID)
	 {
		 VCard vCard = new VCard();
		 try
		 {       
        	// FIXME synchronize this to executor thread
            vCard.load(mConnection, myJID);
            
            // If VCard is loaded, then save the avatar to the personal folder.
            byte[] bytes = vCard.getAvatar();
            
            if (bytes != null)
            {
            	try
            	{
            		String filename = vCard.getAvatarHash() + ".jpg";
            		File sdCard = Environment.getExternalStorageDirectory();
            		File file = new File(sdCard, filename);
            		OutputStream output = new FileOutputStream(file);
            		output.write(bytes);
            		output.close();
            	}
            	catch (Exception e)
            	{
            		e.printStackTrace();
            	}
            }
        }
        catch (XMPPException ex)
        {
            ex.printStackTrace();
        }
        return vCard;
    }

	@Override
	protected void doUpdateUserPresenceAsync(Presence presence)
	{
		String statusText = presence.getStatusText();
        Type type = Type.available;
        Mode mode = Mode.available;
        int priority = 20;
        if (presence.getStatus() == Presence.AWAY)
        {
        	priority = 10;
        	mode = Mode.away;
        }
        else if (presence.getStatus() == Presence.IDLE)
        {
        	priority = 15;
        	mode = Mode.away;
        }
        else if (presence.getStatus() == Presence.DO_NOT_DISTURB)
        {
        	priority = 5;
        	mode = Mode.dnd;
        }
        else if (presence.getStatus() == Presence.OFFLINE)
        {
        	priority = 0;
        	type = Type.unavailable;
        	statusText = "Offline";
        }
		org.jivesoftware.smack.packet.Presence packet = 
        	new org.jivesoftware.smack.packet.Presence(type, statusText, priority, mode);
		
        sendPacket(packet);
		mUserPresence = presence;
        notifyUserPresenceUpdated();
	}

	@Override
	public int getCapability()
	{
		return ImConnection.CAPABILITY_SESSION_REESTABLISHMENT | ImConnection.CAPABILITY_GROUP_CHAT;
	}

	@Override
	public synchronized ChatGroupManager getChatGroupManager()
	{
		if (mChatGroupManager == null)
		{
			mChatGroupManager = new XmppChatGroupManager();
		}
		return mChatGroupManager;
	}

	@Override
	public synchronized EngineFileTransferManager getFileTransferManager()
	{
		if (mFileTransferManager == null)
		{
			mFileTransferManager = new XmppFileTransferManager();
		}
		return mFileTransferManager;
	}

	@Override
	public synchronized ChatSessionManager getChatSessionManager()
	{
		if (mSessionManager == null)
		{
			mSessionManager = new XmppChatSessionManager();
		}
		return mSessionManager;
	}

	@Override
	public synchronized XmppContactList getContactListManager()
	{
		if (mContactListManager == null)
		{
			mContactListManager = new XmppContactList();
		}
		return mContactListManager;
	}

	@Override
	public Contact getLoginUser()
	{
		return mUser;
	}

	@Override
	public Map<String, String> getSessionContext()
	{
		// Empty state for now (but must have at least one key)
		return Collections.singletonMap("state", "empty");
	}

	@Override
	public int[] getSupportedPresenceStatus()
	{
		return new int[] {
				Presence.AVAILABLE,
				Presence.AWAY,
				Presence.IDLE,
				Presence.OFFLINE,
				Presence.DO_NOT_DISTURB,
		};
	}

	@Override
	public void loginAsync(String passwordTemp, boolean retry)
	{
		mPasswordTemp = passwordTemp;
		mRetryLogin = retry;
		execute(new Runnable()
		{
			@Override
			public void run()
			{
				do_login();
			}
		});
	}
	
	// Runs in executor thread
	private void do_login()
	{
		if (mConnection != null)
		{
			setState(getState(), new ImErrorInfo(ImErrorInfo.CANT_CONNECT_TO_SERVER, "still trying..."));
			return;
		}
		IXMPPConfig config = XMPPConfigFactory.get(mContext);
		// providerSettings is closed in initConnection()
		String userName = config.getUserName();
		String password = config.getPassword();
		String domain = config.getDomain();
		
		if (mPasswordTemp != null)
		{
			password = mPasswordTemp;
		}
		
		// TODO should we really be using the same name for both address and name?
		String xmppName = userName + '@' + domain;
		mUser = new Contact(xmppName, userName);
		
		mNeedReconnect = true;
		setState(LOGGING_IN, null);
		mUserPresence = new Presence(Presence.AVAILABLE, "", null, null, Presence.CLIENT_TYPE_DEFAULT);

		try
		{
			if (userName.length() == 0)
				throw new XMPPException("empty username not allowed");
			initConnection(userName, password);
			
		}
		catch (Exception e)
		{
			Log.w(TAG, "login failed", e);
			mConnection = null;
			ImErrorInfo info = new ImErrorInfo(ImErrorInfo.CANT_CONNECT_TO_SERVER, e.getMessage());
			
			if (e == null || e.getMessage() == null)
			{
				Log.w(TAG, "NPE", e);
				info = new ImErrorInfo(ImErrorInfo.INVALID_USERNAME, "unknown error");
				disconnected(info);
				mRetryLogin = false;
			}
			else if (e.getMessage().contains("not-authorized")||e.getMessage().contains("authentication failed"))
			{
				Log.w(TAG, "not authorized - will not retry");
				info = new ImErrorInfo(ImErrorInfo.INVALID_USERNAME, "invalid user/password");
				disconnected(info);
				mRetryLogin = false;
			}
			else if (mRetryLogin)
			{
				Log.w(TAG, "will retry");
				setState(LOGGING_IN, info);
			}
			else
			{
				Log.w(TAG, "will not retry");
				mConnection = null;
				disconnected(info);
			}
			return;
			
		}
		finally
		{
			mNeedReconnect = false;
		}
		
		setState(LOGGED_IN, null);
		debug(TAG, "logged in");
		ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(mConnection);
		sdm.addFeature("ralph");
	}

	// TODO shouldn't setProxy be handled in Imps/settings?
	public void setProxy (String type, String host, int port)
	{

		if (type == null)
		{
			 mProxyInfo = ProxyInfo.forNoProxy();
		}
		else
		{
			ProxyInfo.ProxyType pType = ProxyType.valueOf(type);
			mProxyInfo = new ProxyInfo(pType, host, port,"","");
		}
	}
	
	// Runs in executor thread
	private void initConnection(String userName, final String password) throws Exception
	{
//		android.os.Debug.waitForDebugger();
		
		IXMPPConfig config = XMPPConfigFactory.get(mContext);
		boolean allowPlainAuth = config.getAllowPlainAuth();
		boolean requireTls = config.getRequireTls();
		boolean doDnsSrv = config.getDoDnsSrv();
		boolean tlsCertVerify = config.getTlsCertVerify();
		boolean allowSelfSignedCerts = !tlsCertVerify;
		boolean doVerifyDomain = tlsCertVerify;
		
		boolean useSASL = true;//!allowPlainAuth;
		
		String domain = config.getDomain();
		String server = config.getServer();
		String xmppResource = config.getResource();
		int serverPort = config.getServerPort();
		
		debug(TAG, "TLS required? " + requireTls);
		debug(TAG, "Do SRV check? " + doDnsSrv);
		debug(TAG, "cert verification? " + tlsCertVerify);
    	
    	if (mProxyInfo == null)
    		 mProxyInfo = ProxyInfo.forNoProxy();

    	// try getting a connection without DNS SRV first, and if that doesn't work and the prefs allow it, use DNS SRV
    	if (doDnsSrv) {
    		
    		//java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
    		//java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
    		
    		debug(TAG, "(DNS SRV) resolving: "+domain);
    		DNSUtil.HostAddress srvHost = DNSUtil.resolveXMPPDomain(domain);
    		server = srvHost.getHost();
    		//serverPort = srvHost.getPort(); //ignore port right now, as we are always a client
    		debug(TAG, "(DNS SRV) resolved: "+domain+"=" + server + ":" + serverPort);
    	}

    	if (server == null) { // no server specified in prefs, use the domain
    		debug(TAG, "(use domain) ConnectionConfiguration("+domain+", "+serverPort+", "+domain+", mProxyInfo);");
    		
    		if (mProxyInfo == null)
    			mConfig = new ConnectionConfiguration(domain, serverPort);
    		else
    			mConfig = new ConnectionConfiguration(domain, serverPort, mProxyInfo);
    		
    	} else {	
    		debug(TAG, "(use server) ConnectionConfiguration("+server+", "+serverPort+", "+domain+", mProxyInfo);");
    		
    		if (mProxyInfo == null)
    			mConfig = new ConnectionConfiguration(server, serverPort, domain);
    		else
    			mConfig = new ConnectionConfiguration(server, serverPort, domain, mProxyInfo);

    		//if domain of login user is the same as server
    		doVerifyDomain = (domain.equals(server));
    	}

    	mConfig.setDebuggerEnabled(DEBUG_ENABLED);
    	mConfig.setSASLAuthenticationEnabled(useSASL);   
    	    	
    	if (requireTls) {
    		
    		mConfig.setSecurityMode(SecurityMode.required);
    		
    		SASLAuthentication.supportSASLMechanism("PLAIN", 0);
    		SASLAuthentication.supportSASLMechanism("DIGEST-MD5", 1);
    		
	
    	} else {
    		// if it finds a cert, still use it, but don't check anything since 
    		// TLS errors are not expected by the user
    		mConfig.setSecurityMode(SecurityMode.enabled);    		
    		
    		if(allowPlainAuth)
    		{
    			SASLAuthentication.supportSASLMechanism("PLAIN", 0);
    			SASLAuthentication.supportSASLMechanism("DIGEST-MD5", 1);
    			
    		}
    		else
    		{
    			SASLAuthentication.unsupportSASLMechanism("PLAIN");
        		SASLAuthentication.supportSASLMechanism("DIGEST-MD5", 0);
    		}
    	}
    	// Android has no support for Kerberos or GSSAPI, so disable completely
    	SASLAuthentication.unregisterSASLMechanism("KERBEROS_V4");
    	SASLAuthentication.unregisterSASLMechanism("GSSAPI");

    	mConfig.setVerifyChainEnabled(tlsCertVerify);
    	mConfig.setVerifyRootCAEnabled(tlsCertVerify);
    	mConfig.setExpiredCertificatesCheckEnabled(tlsCertVerify);
    	mConfig.setNotMatchingDomainCheckEnabled(doVerifyDomain && (!allowSelfSignedCerts));
    	mConfig.setSelfSignedCertificateEnabled(allowSelfSignedCerts);
    	
		mConfig.setTruststoreType(TRUSTSTORE_TYPE);
		mConfig.setTruststorePath(TRUSTSTORE_PATH);
		mConfig.setTruststorePassword(TRUSTSTORE_PASS);
				
//		if (server == null)
//			initSSLContext(domain, mConfig);
//		else
 //   		initSSLContext(server, mConfig);
		
		// Don't use smack reconnection - not reliable
		mConfig.setReconnectionAllowed(false);		
		mConfig.setSendPresence(true);
		mConfig.setRosterLoadedAtLogin(true);

		mConnection = new MyXMPPConnection(mContext, mConfig);
		
        //debug(TAG,"is secure connection? " + mConnection.isSecureConnection());
        //debug(TAG,"is using TLS? " + mConnection.isUsingTLS());
        
        mConnection.addPacketListener(new PacketListener() {
			
			@Override
			public void processPacket(Packet packet) {
				org.jivesoftware.smack.packet.Message smackMessage = (org.jivesoftware.smack.packet.Message) packet;
				 MUCUser mucUser = (MUCUser) packet.getExtension("x", "http://jabber.org/protocol/muc#user");
                // Check if the MUCUser extension includes an invitation
                if (mucUser != null && mucUser.getInvite() != null)
                {				
					return;
				}
				String address = parseAddressBase(smackMessage.getFrom());
				ChatSession session = findOrCreateSession(address);
//				DeliveryReceipts.DeliveryReceipt dr =
//				(DeliveryReceipts.DeliveryReceipt)smackMessage.getExtension("received", DeliveryReceipts.NAMESPACE);
//				if (dr != null) {
//					debug(TAG, "got delivery receipt for " + dr.getId());
//					session.onMessageReceipt(dr.getId());
//				}
				ChatStateExtension goneChatState = (ChatStateExtension)packet.getExtension("gone", "http://jabber.org/protocol/chatstates");
				if (goneChatState != null)
				{
					Message goneMessage = new Message(session.getParticipant().getAddress().toString() + " has left the conversation");
					goneMessage.setTo(mUser.getAddress());
					goneMessage.setFrom(session.getParticipant().getAddress());
					goneMessage.setDateTime(new Date());
					session.onReceiveMessage(goneMessage);
					return;
				}
				if (smackMessage.getBody() == null)
				{
					return;
				}
				Message rec = new Message(smackMessage.getBody());
				rec.setTo(mUser.getAddress());
				if (session.isGroupSession())
				{
					String resourceName = org.jivesoftware.smack.util.StringUtils.parseResource(smackMessage.getFrom());
					rec.setFrom(resourceName);
				}
				else
				{
					rec.setFrom(session.getParticipant().getAddress());
				}
				DelayInfo delayInfo = (DelayInfo)packet.getExtension("delay", "urn:xmpp:delay");
				if (delayInfo != null)
				{
					rec.setDateTime(delayInfo.getStamp());
				}
				else
				{
					rec.setDateTime(new Date());
				}
				session.onReceiveMessage(rec);
//				if (smackMessage.getExtension("request", DeliveryReceipts.NAMESPACE) != null)
//				{
//					debug(TAG, "got delivery receipt request");
//					// got XEP-0184 request, send receipt
//					sendReceipt(smackMessage);
//					session.onReceiptsExpected();
//				}
			}
		}, new PacketTypeFilter(org.jivesoftware.smack.packet.Message.class));
        
        mConnection.addPacketListener(new PacketListener()
        {
			@Override
			public void processPacket(Packet packet)
			{
				org.jivesoftware.smack.packet.Presence presence = (org.jivesoftware.smack.packet.Presence)packet;
				String from = presence.getFrom();
				String address = parseAddressBase(from);
				String name = parseAddressName(from);
				String resource = org.jivesoftware.smack.util.StringUtils.parseResource(from);
				ImEntity entity = findOrCreateEntity(name,address);
				if (entity instanceof Contact)
				{
					Contact contact = (Contact)entity;
					if (presence.getType() == Type.subscribe) {
						debug(TAG, "sub request from " + address);
						mContactListManager.getSubscriptionRequestListener().onSubScriptionRequest((Contact)entity);
					}
					else 
					{
						int type = parsePresence(presence);
						contact.setPresence(new Presence(type, presence.getStatus(), null, null, Presence.CLIENT_TYPE_DEFAULT, resource));
					}
				}
			}
		}, new PacketTypeFilter(org.jivesoftware.smack.packet.Presence.class));

        mConnection.connect();

        initServiceDiscovery();
        
		mConnection.addConnectionListener(new ConnectionListener() {
        	/**
        	 * Called from smack when connect() is fully successful
        	 * 
        	 * This is called on the executor thread while we are in reconnect()
        	 */
			@Override
			public void reconnectionSuccessful() {
				debug(TAG, "reconnection success");
				mNeedReconnect = false;
				setState(LOGGED_IN, null);
			}
			
			@Override
			public void reconnectionFailed(Exception e) {
				// We are not using the reconnection manager
				throw new UnsupportedOperationException();
			}
			
			@Override
			public void reconnectingIn(int seconds) {
				// We are not using the reconnection manager
				throw new UnsupportedOperationException();
			}
			
			@Override
			public void connectionClosedOnError(final Exception e) {
				/*
				 * This fires when:
				 * - Packet reader or writer detect an error
				 * - Stream compression failed
				 * - TLS fails but is required
				 * - Network error
				 * - We forced a socket shutdown
				 */
				Log.e(TAG, "reconnect on error", e);
				if (e.getMessage().contains("conflict")) {
					execute(new Runnable() {
						@Override
						public void run() {
							disconnect();
							disconnected(new ImErrorInfo(ImErrorInfo.CANT_CONNECT_TO_SERVER,
									"logged in from another location"));
						}
					});
				}
				else if (!mNeedReconnect) {
					execute(new Runnable() {
						@Override
						public void run() {
							if (getState() == LOGGED_IN)
								setState(LOGGING_IN, new ImErrorInfo(ImErrorInfo.NETWORK_ERROR, e.getMessage()));
							maybe_reconnect();
						}
					});
				}
			}
			
			@Override
			public void connectionClosed() {
				
				debug(TAG, "connection closed");
				
				/*
				 * This can be called in these cases:
				 * - Connection is shutting down
				 *   - because we are calling disconnect
				 *     - in do_logout
				 *     
				 * - NOT
				 *   - because server disconnected "normally"
				 *   - we were trying to log in (initConnection), but are failing
				 *   - due to network error
				 *   - due to login failing
				 */
			}
		});
        
        Connection.addConnectionCreationListener(new ConnectionCreationListener (){

			@Override
			public void connectionCreated(Connection arg0) {
				debug(TAG, "connection created!");
				
			}
        });
        mNickname = userName;
        if (config.getConcatUsernameWithDomain())
        {
        	this.mUsername = userName + '@' + domain;
        }
        else
        {
        	this.mUsername = userName;
        }

        this.mPassword = password;
        this.mResource = xmppResource;

        mStreamHandler = new XmppStreamHandler(mConnection);
        mConnection.login(mUsername, mPassword, mResource);
        mStreamHandler.notifyInitialLogin();

        Roster roster = mConnection.getRoster();
		roster.setSubscriptionMode(Roster.SubscriptionMode.manual);			
		getContactListManager().listenToRoster(roster);
		getChatGroupManager();
		mChatGroupManager.bindConnection(mConnection);
		getFileTransferManager();
		mFileTransferManager.bindConnection(mConnection);

		org.jivesoftware.smack.packet.Presence presence = 
        		new org.jivesoftware.smack.packet.Presence(org.jivesoftware.smack.packet.Presence.Type.available);
        mConnection.sendPacket(presence);
	}
	
	public void sendReceipt(org.jivesoftware.smack.packet.Message msg)
	{
		debug(TAG, "sending XEP-0184 ack to " + msg.getFrom() + " id=" + msg.getPacketID());
		org.jivesoftware.smack.packet.Message ack =
				new org.jivesoftware.smack.packet.Message(msg.getFrom(),
						msg.getType());
		ack.addExtension(new DeliveryReceipts.DeliveryReceipt(msg.getPacketID()));
		mConnection.sendPacket(ack);
	}

	private void initSSLContext (String server, ConnectionConfiguration config) throws Exception
	{
		ks = KeyStore.getInstance(TRUSTSTORE_TYPE);
         try {
             ks.load(new FileInputStream(TRUSTSTORE_PATH), TRUSTSTORE_PASS.toCharArray());
         }
         catch(Exception e) {
             ks = null;
         }
        
	     KeyManagerFactory kmf = KeyManagerFactory.getInstance(KEYMANAGER_TYPE);
	     try {
	    	 kmf.init(ks, TRUSTSTORE_PASS.toCharArray());    	 
	         kms = kmf.getKeyManagers();
	     } catch (NullPointerException npe) {
	         kms = null;
	     }
	     
	     sslContext = SSLContext.getInstance(SSLCONTEXT_TYPE);
	     sTrustManager = new ServerTrustManager(mContext, server, config);
	     
	     sslContext.init(kms,
                 new javax.net.ssl.TrustManager[]{sTrustManager},
                 new java.security.SecureRandom());

//	    config.setCustomSSLContext(sslContext);
	    config.setCallbackHandler(this);
	}
	
	void sslCertificateError ()
	{
		this.disconnect();
	}

	void disconnected(ImErrorInfo info)
	{
		Log.w(TAG, "disconnected");
		setState(DISCONNECTED, info);
	}
	
	protected static int parsePresence(org.jivesoftware.smack.packet.Presence presence)
	{
		int type = Presence.AVAILABLE;
		Mode rmode = presence.getMode();
		Type rtype = presence.getType();
		
		if (rmode == Mode.away || rmode == Mode.xa)
			type = Presence.AWAY;
		else if (rmode == Mode.dnd)
			type = Presence.DO_NOT_DISTURB;
		else if (rtype == Type.unavailable)
			type = Presence.OFFLINE;
				
		return type;
	}
	
	protected static String parseAddressBase(String from) {
		return from.replaceFirst("/.*", "");
	}

	protected static String parseAddressName(String from) {
		return from.replaceFirst("@.*", "");
	}
	
	@Override
	public void logoutAsync()
	{
		// TODO invoke join() here?
		execute(new Runnable()
		{
			@Override
			public void run()
			{
				do_logout();
			}
		});
	}

	// Force immediate logout
	public void logout()
	{
		do_logout();
	}
	
	// Usually runs in executor thread, unless called from logout()
	private void do_logout()
	{
		Log.w(TAG, "logout");
		setState(LOGGING_OUT, null);
		disconnect();
		setState(DISCONNECTED, null);
	}

	// Runs in executor thread
	private void disconnect()
	{
		clearPing();
		mChatGroupManager.bindConnection(null);
		mFileTransferManager.bindConnection(null);
		XMPPConnection conn = mConnection;
		mConnection = null;
		try
		{
			conn.disconnect();
		}
		catch (Throwable th)
		{
			// ignore
		}
		mNeedReconnect = false;
		mRetryLogin = false;
	}
	
	@Override
	public void reestablishSessionAsync(Map<String, String> sessionContext) {
		execute(new Runnable() {
			@Override
			public void run() {
				if (getState() == SUSPENDED) {
					debug(TAG, "reestablish");
					setState(LOGGING_IN, null);
					maybe_reconnect();
				}
			}
		});
	}
	
	@Override
	public void suspend() {
		execute(new Runnable() {
			@Override
			public void run() {
				debug(TAG, "suspend");
				setState(SUSPENDED, null);
				mNeedReconnect = false;
				clearPing();
				// Do not try to reconnect anymore if we were asked to suspend
				mConnection.shutdown();
			}
		});
	}

	private ChatSession findOrCreateSession(String address)
	{
		ChatSession session = mSessionManager.findSession(address);
		if (session == null)
		{
			ImEntity entity = findOrCreateEntity(parseAddressName(address),address);
			session = mSessionManager.createChatSession(entity);
		}
		return session;
	}

	ImEntity findOrCreateEntity(String name, String address)
	{
		ImEntity result = null;
		result = mContactListManager.getContact(address);
		if (result == null)
		{
			result = mChatGroupManager.getChatGroup(address);
			if (result == null)
			{
				result = makeContact(name, address);
			}
		}
		return result;
	}

	private static Contact makeContact(String name, String address)
	{
		return new Contact(address, name);
	}
		
	private final class XmppFileTransferManager extends EngineFileTransferManager implements FileTransferListener
	{
		private FileTransferManager _smackFileTransferManager;
		private HashMap<String, FileTransferRequest> _fileTransferRequests = new HashMap<String, FileTransferRequest>();
		private HashMap<String, FileTransfer> _activeFileTransfers = new HashMap<String, FileTransfer>();

		public void bindConnection(Connection connection)
		{
			_smackFileTransferManager = new FileTransferManager(mConnection);
	        FileTransferNegotiator.setServiceEnabled(mConnection, true);
	        _smackFileTransferManager.addFileTransferListener(this);
		}
		
		@Override
		public FileTransferRequestParcel getFileTransferRequest(String streamID)
		{
			return fileTransferRequestToParcel(streamID);
		}
		
		@Override
		public FileTransferParcel getFileTransferStatus(String streamID)
		{
			return fileTransferToParcel(streamID);
		}
		
		@Override
		public void acceptFileTransferRequest(String streamID)
		{
			final FileTransferRequest fileTransferRequest = _fileTransferRequests.remove(streamID);
			if (fileTransferRequest != null)
			{
				IncomingFileTransfer incoming = fileTransferRequest.accept();
				File destFile = new File(incoming.getFilePath());
				incoming.setStatusChangedListener(new StatusChangedListener()
				{
					@Override
					public void onStatusChanged(FileTransfer fileTransfer)
					{
						_activeFileTransfers.remove(fileTransfer.getStreamID());
						_activeFileTransfers.put(fileTransfer.getStreamID(), fileTransfer);
						notifyFileTransferStatusChanged(fileTransferToParcel(fileTransfer.getStreamID()));
					}
				});
				_activeFileTransfers.put(streamID, incoming);
				try
				{
					incoming.recieveFile(destFile);
				}
				catch (XMPPException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		@Override
		public void rejectFileTransferRequest(String requestId)
		{
			FileTransferRequest fileTransferRequest = _fileTransferRequests.remove(requestId);
			if (fileTransferRequest != null)
			{
				fileTransferRequest.reject();
			}
		}

		@Override
		public void fileTransferRequest(FileTransferRequest request)
		{
			request.reject(); // TODO -- fix later
			boolean acceptable = false;
			if ( (request.getRequestor() != null) && (request.getFileName() != null) && acceptable ) // TODO fixme - remove && acceptable
			{
				String bareJid = parseAddressBase(request.getRequestor());
				ChatSession session = findOrCreateSession(bareJid);
				if (session != null)
				{
					String fileName = request.getFileName();
					String description = request.getDescription() == null ? request.getFileName() : request.getDescription();
					String mimeType = request.getMimeType();
					if (mimeType == null)
					{
						String lfname = fileName.toLowerCase();
						if (lfname.contains(".png"))
						{
							mimeType = "image/png";
						}
						else if (lfname.contains("jpg"))
						{
							mimeType = "image/jpg";
						}
						else if (lfname.contains("bmp"))
						{
							mimeType = "image/bmp";
						}
					}
					if (mimeType != null)
					{
						_fileTransferRequests.put(request.getStreamID(), request);
						acceptable = true;
						notifyFileTransferRequest(fileTransferRequestToParcel(request.getStreamID()));
					}
				}
			}
			if (acceptable == false)
			{
				request.reject();
			}
		}
		
		private FileTransferRequestParcel fileTransferRequestToParcel(String streamID)
		{
			FileTransferRequestParcel result = null;
			FileTransferRequest request = _fileTransferRequests.get(streamID);
			if (request != null)
			{
				result = new FileTransferRequestParcel(request.getStreamID(), request.getRequestor());
				result.setFileInfo(request.getFileName(), request.getFileSize(), request.getMimeType());
				result.setDescription(request.getDescription());
			}
			return result;
		}
		
		private FileTransferParcel fileTransferToParcel(String streamID)
		{
			FileTransferParcel result = null;
			FileTransfer fileTransfer = _activeFileTransfers.get(streamID);
			if (fileTransfer != null)
			{
				result = new FileTransferParcel(fileTransfer.getStreamID());
				result.setFileName(fileTransfer.getFileName());
				result.setFilePath(fileTransfer.getFilePath());
				result.setStatus(fileTransfer.getStatus().toString(), fileTransfer.getAmountWritten());
			}
			return result;
		}
		
		@Override
		public void sendFileAsync(final ChatSession session, String path)
		{
			String bareJid = session.getParticipant().getAddress().toString();
			ImEntity entity = mContactListManager.getContact(bareJid);
			if (entity != null && entity instanceof Contact)
			{
				Contact contact = (Contact)entity;
				Presence presence = contact.getPresence();
				if (presence != null && presence.getResource() != null)
				{
					String fullJid = bareJid+"/"+presence.getResource();
					final OutgoingFileTransfer outgoing = _smackFileTransferManager.createOutgoingFileTransfer(fullJid);
					outgoing.setStatusChangedListener(new StatusChangedListener()
					{
						@Override
						public void onStatusChanged(FileTransfer fileTransfer)
						{
//							logSystemMsg(session, String.format("Transfer of %s %s", fileTransfer.getFileName(), fileTransfer.getStatus().toString()));
						}
					});
					try
					{
						final File file = new File(path);
						outgoing.sendFile(file, file.getName());
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private final class XmppChatSessionManager extends ChatSessionManager
	{
		@Override
		public void sendMessageAsync(ChatSession session, Message message) {
			if (message.getType() == Message.Type.GROUPCHAT)
			{
				org.jivesoftware.smack.packet.Message msg =
						new org.jivesoftware.smack.packet.Message(
								message.getTo(),
								org.jivesoftware.smack.packet.Message.Type.groupchat
								);
				msg.setBody(message.getBody());
				debug(TAG, "sending packet ID " + msg.getPacketID());
				message.setID(msg.getPacketID());
				sendPacket(msg);
			}
			else
			{
				org.jivesoftware.smack.packet.Message msg =
						new org.jivesoftware.smack.packet.Message(
								message.getTo(),
								org.jivesoftware.smack.packet.Message.Type.chat
								);
//				msg.addExtension(new DeliveryReceipts.DeliveryReceiptRequest());
				msg.setBody(message.getBody());
				debug(TAG, "sending packet ID " + msg.getPacketID());
				message.setID(msg.getPacketID());
				sendPacket(msg);
			}
		}
				
		private ChatSession findSession(String address)
		{
			for (Iterator<ChatSession> iter = mSessions.iterator(); iter.hasNext();)
			{
				ChatSession session = iter.next();
				if (session.getParticipant().getAddress().equals(address))
				{
					return session;
				}
			}
			return null;
		}

		@Override
	    public void closeChatSession(ChatSession session)
	    {
			org.jivesoftware.smack.packet.Message msg =
					new org.jivesoftware.smack.packet.Message(
							session.getParticipant().getAddress().toString(),
							org.jivesoftware.smack.packet.Message.Type.chat
							);
			msg.setFrom(getLoginUser().getAddress().toString());
			msg.addExtension(new ChatStateExtension(ChatState.gone));
			debug(TAG, "sending packet ID " + msg.getPacketID());
			sendPacket(msg);
			super.closeChatSession(session);
	    }
	}

	public ChatSession findSession (String address)
	{
		return mSessionManager.findSession(address);
	}
	
	public ChatSession createChatSession (Contact contact)
	{
		return mSessionManager.createChatSession(contact);
	}

	private class XmppChatGroupManager extends ChatGroupManager
	{
		private XMPPConnection _connection;
		private InvitationListener _invitationListener;

		public XmppChatGroupManager()
		{
			_invitationListener = new InvitationListener()
			{
				@Override
				public void invitationReceived(Connection conn,
						String room,
						String inviter,
						String reason,
						String password,
						org.jivesoftware.smack.packet.Message message)
				{
					Invitation invitation = new Invitation(message.getPacketID(), room, inviter, reason, password);
					notifyGroupInvitation(invitation);
				}
			};
		}
		
		@Override
		public void createChatGroupAsync(String address)
		{
			notifyGroupError(GroupListener.ERROR_UNSUPPORTED_OPERATION, address, new ImErrorInfo(ImErrorInfo.UNKNOWN_ERROR, "createChatGroupAsync"));
		}
	
		@Override
		public void deleteChatGroupAsync(String address)
		{
			notifyGroupError(GroupListener.ERROR_UNSUPPORTED_OPERATION, address, new ImErrorInfo(ImErrorInfo.UNKNOWN_ERROR, "deleteChatGroupAsync"));
		}
		
		@Override
		public void acceptInvitationAsync(long id)
		{
			ContentResolver resolver = mContext.getContentResolver();
			Cursor c = resolver.query(Uri.withAppendedPath(Imps.Invitation.CONTENT_URI, Long.toString(id)), null, null, null, null);
			if (c != null)
			{
				if (c.moveToFirst())
				{
					String roomJid = c.getString(c.getColumnIndex(Imps.InvitationColumns.GROUP_NAME));
					ChatGroup chatGroup = getChatGroup(roomJid);
					String password = c.getString(c.getColumnIndex(Imps.InvitationColumns.PASSWORD));
					if (password != null)
					{
						chatGroup.setPassword(password);
					}
					chatGroup.joinAsync();
				}
				else
				{
					notifyGroupError(GroupListener.ERROR_JOINING_IN_GROUP, "invitation not found", new ImErrorInfo(ImErrorInfo.UNKNOWN_ERROR, "acceptInvitationAsync"));
				}
				c.close();
			}
		}
	
		@Override
		public void rejectInvitationAsync(long id)
		{
			ContentResolver resolver = mContext.getContentResolver();
			Cursor c = resolver.query(Uri.withAppendedPath(Imps.Invitation.CONTENT_URI, Long.toString(id)), null, null, null, null);
			if (c != null)
			{
				if (c.moveToFirst())
				{
					String roomJid = c.getString(c.getColumnIndex(Imps.InvitationColumns.GROUP_NAME));
					String sender = c.getString(c.getColumnIndex(Imps.InvitationColumns.SENDER));
					MultiUserChat.decline(mConnection, roomJid, sender, "not accepted");
				}
				else
				{
					notifyGroupError(GroupListener.ERROR_JOINING_IN_GROUP, "invitation not found", new ImErrorInfo(ImErrorInfo.UNKNOWN_ERROR, "rejectInvitationAsync"));
				}
				c.close();
			}
		}
	
		public void bindConnection(XMPPConnection xmppConnection)
		{
			if (_connection != null && _invitationListener != null)
			{
				MultiUserChat.removeInvitationListener(_connection, _invitationListener);
				_connection = null;
			}
			_connection = xmppConnection;
			if (_connection == null)
			{
				return;
			}
			MultiUserChat.addInvitationListener(_connection, _invitationListener);
		}

		public void rejoinRooms()
		{
			Collection<ChatGroup> chatGroups = mGroups.values();
			for (ChatGroup chatGroup : chatGroups)
			{
				if (chatGroup.isJoined())
				{
					chatGroup.joinAsync();
				}
			}
		}
		
		@Override
		public ChatGroup getChatGroup(String address)
		{
			ChatGroup result = super.getChatGroup(address);
			if (result == null)
			{
				Contact contact = mContactListManager.getContact(address);
				if (contact == null)
				{
					contact = mContactListManager.createTemporaryContact(address);
				}
				if (contact != null)
				{
					result = new XmppChatGroup(address, contact.getName());
					notifyGroupCreated(result);
				}
			}
			return result;
		}
	}
		
	public class XmppChatGroup extends ChatGroup
	{
		private MultiUserChat _muc;
		
		public XmppChatGroup(final String address, String name)
		{
			super(address, name, mChatGroupManager);
			_muc = new MultiUserChat(mConnection, address);
			_muc.addParticipantStatusListener(new ParticipantStatusListener()
			{
				@Override
				public void joined(String participant)
				{
					ArrayList<Contact> joined = new ArrayList<Contact>();
					String nickname = org.jivesoftware.smack.util.StringUtils.parseResource(participant);
					joined.add(new Contact(participant, nickname));
					mChatGroupManager.notifyGroupChanged(address, joined, null);
				}
				@Override
				public void left(String participant)
				{
					ArrayList<Contact> left = new ArrayList<Contact>();
					String nickname = org.jivesoftware.smack.util.StringUtils.parseResource(participant);
					left.add(new Contact(participant, nickname));
					mChatGroupManager.notifyGroupChanged(address, null, left);
				}
				@Override
				public void kicked(String participant, String actor, String reason)
				{
				}
				@Override
				public void voiceGranted(String participant)
				{
				}
				@Override
				public void voiceRevoked(String participant)
				{
				}
				@Override
				public void banned(String participant, String actor, String reason)
				{
				}
				@Override
				public void membershipGranted(String participant)
				{
				}
				@Override
				public void membershipRevoked(String participant)
				{
				}
				@Override
				public void moderatorGranted(String participant)
				{
				}
				@Override
				public void moderatorRevoked(String participant)
				{
				}
				@Override
				public void ownershipGranted(String participant)
				{
				}
				@Override
				public void ownershipRevoked(String participant)
				{
				}
				@Override
				public void adminGranted(String participant)
				{
				}
				@Override
				public void adminRevoked(String participant)
				{
				}
				@Override
				public void nicknameChanged(String participant, String newNickname)
				{
				}
			});
			_muc.addMessageListener(new PacketListener()
			{
				@Override
				public void processPacket(Packet packet)
				{
					MUCUser mucUser = (MUCUser)packet.getExtension("x", MUCUser.NAMESPACE);
					if (mucUser != null)
					{
						MUCUser.Status status = mucUser.getStatus();
						if (status != null)
						{
							if (status.getCode().equals("100"))
							{
								ArrayList<Contact> joined = new ArrayList<Contact>();
								joined.add(mUser);
								mChatGroupManager.notifyGroupChanged(address, joined, null);
							}
						}
					}
				}
			});
		}
		
		@Override
		public void joinAsync()
		{
			try
			{
				if (getPassword() != null)
				{
					_muc.join(mNickname, getPassword());
				}
				else
				{
					_muc.join(mNickname);
				}
				mChatGroupManager.notifyJoinedGroup(this);
			}
			catch (XMPPException e)
			{
				e.printStackTrace();
				mChatGroupManager.notifyGroupError(GroupListener.ERROR_JOINING_IN_GROUP, getAddress(), new ImErrorInfo(ImErrorInfo.NETWORK_ERROR, e.getMessage()));
			}
		}

		@Override
		public void leaveAsync()
		{
			try
			{
				_muc.leave();
			}
			catch (IllegalStateException e) // not connected to server
			{
				e.printStackTrace();
			}
		}

		@Override
		public boolean isJoined()
		{
			return _muc.isJoined();
		}

		@Override
		public void inviteUserAsync(Contact invitee)
		{
			_muc.invite(invitee.getAddress(), "because-I-said-so");
		}
	}
	
	public class XmppContactList extends ContactListManager
	{
		//private Hashtable<String, org.jivesoftware.smack.packet.Presence> unprocdPresence = new Hashtable<String, org.jivesoftware.smack.packet.Presence>();
		
		@Override
		protected void setListNameAsync(final String name, final ContactList list)
		{
			execute(new Runnable()
			{
				@Override
				public void run()
				{
					do_setListName(name, list);
				}
			});
		}
		
		// Runs in executor thread
		private void do_setListName(String name, ContactList list)
		{
			debug(TAG, "set list name");
			mConnection.getRoster().getGroup(list.getName()).setName(name);
			notifyContactListNameUpdated(list, name);
		}

		@Override
		public String normalizeAddress(String address)
		{
			return address;
		}

		@Override
		public void loadContactListsAsync()
		{
			
			execute(new Runnable()
			{
				@Override
				public void run()
				{
					do_loadContactLists();
				}
			});
			
		}

		// For testing
		public void loadContactLists()
		{
			do_loadContactLists();
		}
		
		/**
		 *  Create new list of contacts from roster entries.
		 *  
		 *  Runs in executor thread
		 *
		 *  @param roster		the parent roster
		 *  @param entryIter	iterator of roster entries to add to contact list
		 *  @param skipList		list of contacts which should be omitted; new contacts are added to this list automatically
		 *  @return				contacts from roster which were not present in skiplist.
		 */
		private Collection<Contact> fillContacts(Roster roster, Collection<RosterEntry> entryIter, Set<String> skipList)
		{
			Collection<Contact> contacts = new ArrayList<Contact>();
			for (RosterEntry entry : entryIter)
			{
				String address = parseAddressBase(entry.getUser());

				/* Skip entries present in the skip list */
				if (skipList != null && !skipList.add(address))
					continue;

				String name = entry.getName();
				if (name == null)
					name = address;
				
				Contact contact = mContactListManager.getContact(address);
				
				if (contact == null)
				{
					contact = new Contact(address, name);
				}
				org.jivesoftware.smack.packet.Presence presence = roster.getPresence(address);	

				String resource = StringUtils.parseResource(presence.getFrom());
				contact.setPresence(new Presence(parsePresence(presence), presence.getStatus(), null, null, Presence.CLIENT_TYPE_DEFAULT, resource));

				contacts.add(contact);
				// getVCard(xaddress.getFullName());  // commented out to fix slow contact loading
			}
			return contacts;
		}

		private Collection<Contact> fillContacts(Collection<HostedRoom> rooms)
		{
			Collection<Contact> result = new ArrayList<Contact>();
			for (HostedRoom room : rooms)
			{
				Contact contact = new Contact(room.getJid(), room.getName(), Contact.Type.GROUP);
				result.add(contact);
			}
			return result;
		}
		
		// Runs in executor thread
		private void do_loadContactLists()
		{
			debug(TAG, "load contact lists");
			
			if (mConnection == null)
				return;
			
			Roster roster = mConnection.getRoster();
			
			//Set<String> seen = new HashSet<String>();

			// This group will also contain all the unfiled contacts.  We will create it locally if it
			// does not exist.
			String generalGroupName = "Buddies";

			for (Iterator<RosterGroup> giter = roster.getGroups().iterator(); giter.hasNext();) {

				RosterGroup group = giter.next();

				debug(TAG, "loading group: " + group.getName() + " size:" + group.getEntryCount());
						

				Collection<Contact> contacts = fillContacts(roster, group.getEntries(), null);
				
				if (group.getName().equals(generalGroupName) && roster.getUnfiledEntryCount() > 0)
				{
					Collection<Contact> unfiled = fillContacts(roster, roster.getUnfiledEntries(), null);
					contacts.addAll(unfiled);
				}
				ContactList cl =
						new ContactList(mUser.getAddress(), group.getName(), group.getName().equals(generalGroupName), contacts, this);
				
				notifyContactListCreated(cl);
				
				notifyContactsPresenceUpdated(contacts.toArray(new Contact[contacts.size()]));
			}			
			
			Collection<Contact> contacts;
			if (roster.getUnfiledEntryCount() > 0)
			{
				contacts = fillContacts(roster, roster.getUnfiledEntries(), null);
			}
			else
			{
				contacts = new ArrayList<Contact>();
			}
			ContactList cl = getContactListByName(generalGroupName);

			// We might have already created the Buddies contact list above
			if (cl == null)
			{
				cl = new ContactList(mUser.getAddress(), generalGroupName, true, contacts, this);
				notifyContactListCreated(cl);
				notifyContactsPresenceUpdated(contacts.toArray(new Contact[contacts.size()]));
			}

			Collection<HostedRoom> hostedRooms = null;
			try
			{
				hostedRooms = MultiUserChat.getHostedRooms(mConnection, "conference.chat.antaresx.net");
			}
			catch (XMPPException e)
			{
				e.printStackTrace();
			}
			if (hostedRooms != null)
			{
				Collection<Contact> chatRooms = fillContacts(hostedRooms);
				cl = new ContactList(mUser.getAddress(), "Chat Rooms", false, chatRooms, this);
				notifyContactListCreated(cl);
				notifyContactsPresenceUpdated(chatRooms.toArray(new Contact[chatRooms.size()]));
			}
			notifyContactListsLoaded();
		}

		/*
		 * iterators through a list of contacts to see if there were any Presence
		 * notifications sent before the contact was loaded
		 */
		/*
		private void processQueuedPresenceNotifications (Collection<Contact> contacts)
		{
			
			Roster roster = mConnection.getRoster();
			
			//now iterate through the list of queued up unprocessed presence changes
			for (Contact contact : contacts)
			{
				
				String address = parseAddressBase(contact.getAddress().getFullName());
			
				org.jivesoftware.smack.packet.Presence presence = roster.getPresence(address);

				if (presence != null)
				{
					debug(TAG, "processing queued presence: " + address + " - " + presence.getStatus());

					unprocdPresence.remove(address);

					contact.setPresence(new Presence(parsePresence(presence), presence.getStatus(), null, null, Presence.CLIENT_TYPE_DEFAULT));
					
					Contact[] updatedContact = {contact};
					notifyContactsPresenceUpdated(updatedContact);	
				}
				
				

			}
		}*/
		
		public void listenToRoster(final Roster roster)
		{
			roster.addRosterListener(rListener);
		}

		RosterListener rListener = new RosterListener() {
			
//			private Stack<String> entriesToAdd = new Stack<String>();
//			private Stack<String> entriesToDel = new Stack<String>();

			@Override
			public void presenceChanged(org.jivesoftware.smack.packet.Presence presence) {
				
				handlePresenceChanged(presence);
				
				
			}
			
			@Override
			public void entriesUpdated(Collection<String> addresses) {
				
				debug(TAG, "roster entries updated");
				
				//entriesAdded(addresses);
			}
			
			@Override
			public void entriesDeleted(Collection<String> addresses) {
				
				debug(TAG, "roster entries deleted: " + addresses.size());
				
				
				
				/*
				if (addresses != null)
					entriesToDel.addAll(addresses);
				
				if (mContactListManager.getState() == ContactListManager.LISTS_LOADED)
				{
					

					synchronized (entriesToDel)
					{
						while (!entriesToDel.empty())
							try {
								Contact contact = mContactListManager.getContact(entriesToDel.pop());
								mContactListManager.removeContactFromListAsync(contact, mContactListManager.getDefaultContactList());
							} catch (ImException e) {
								Log.e(TAG,e.getMessage(),e);
							}
					}
				}
				else
				{
					debug(TAG, "roster delete entries queued");
				}*/
			}
			
			@Override
			public void entriesAdded(Collection<String> addresses) {
				
				debug(TAG, "roster entries added: " + addresses.size());
				
				
				/*
				if (addresses != null)
					entriesToAdd.addAll(addresses);
				
				if (mContactListManager.getState() == ContactListManager.LISTS_LOADED)
				{							
					debug(TAG, "roster entries added");

					while (!entriesToAdd.empty())
						try {
							mContactListManager.addContactToListAsync(entriesToAdd.pop(), mContactListManager.getDefaultContactList());
						} catch (ImException e) {
							Log.e(TAG,e.getMessage(),e);
						}
				}
				else
				{
					debug(TAG, "roster add entries queued");
				}*/
			}
		};
		
		
		private void handlePresenceChanged (org.jivesoftware.smack.packet.Presence presence)
		{
			
			String name = parseAddressName(presence.getFrom());
			String address = parseAddressBase(presence.getFrom());
			
			Contact contact = getContact(address);
			/*
			if (mConnection != null)
			{
				Roster roster = mConnection.getRoster();
				
				// Get it from the roster - it handles priorities, etc.
				
				if (roster != null)
					presence = roster.getPresence(address);
			}*/
			
			int type = parsePresence(presence);
			
			if (contact == null)
			{
				contact = new Contact(address, name);
	
				debug(TAG, "got presence updated for NEW user: " + contact.getAddress() + " presence:" + type);
				//store the latest presence notification for this user in this queue
				//unprocdPresence.put(user, presence);
				
				
			}
			else
			{
				debug(TAG, "Got present update for EXISTING user: " + contact.getAddress() + " presence:" + type);
		
				Presence p = new Presence(type, presence.getStatus(), null, null, Presence.CLIENT_TYPE_DEFAULT, StringUtils.parseResource(presence.getFrom()));			
				contact.setPresence(p);
				
	
				Contact []contacts = new Contact[] { contact };
	
				notifyContactsPresenceUpdated(contacts);
			}
		}
		
		@Override
		protected ImConnection getConnection() {
			return XmppConnection.this;
		}

		@Override
		protected void doRemoveContactFromListAsync(Contact contact, ContactList list)
		{
			// FIXME synchronize this to executor thread
			if (mConnection == null)
			{
				return;
			}
			Roster roster = mConnection.getRoster();
			String address = contact.getAddress();
			try
			{
				RosterGroup group = roster.getGroup(list.getName());
				if (group == null)
				{
					Log.e(TAG, "could not find group " + list.getName() + " in roster");
					return;
				}
				RosterEntry entry = roster.getEntry(address);
				if (entry == null)
				{
					Log.e(TAG, "could not find entry " + address + " in group " + list.getName());
					return;
				}
				group.removeEntry(entry);
			}
			catch (XMPPException e)
			{
				Log.e(TAG, "remove entry failed", e);
				throw new RuntimeException(e);
			}
            org.jivesoftware.smack.packet.Presence response =
            	new org.jivesoftware.smack.packet.Presence(org.jivesoftware.smack.packet.Presence.Type.unsubscribed);
            response.setTo(address);
            sendPacket(response);
			notifyContactListUpdated(list, ContactListListener.LIST_CONTACT_REMOVED, contact);
		}

		@Override
		protected void doDeleteContactListAsync(ContactList list)
		{
			// TODO delete contact list
			debug(TAG, "delete contact list " + list.getName());
		}

		@Override
		protected void doCreateContactListAsync(String name, Collection<Contact> contacts, boolean isDefault)
		{
			// TODO create contact list
			debug(TAG, "create contact list " + name + " default " + isDefault);
		}

		@Override
		protected void doBlockContactAsync(String address, boolean block)
		{
			// TODO block contact
		}

		@Override
		protected void doAddContactToListAsync(String address, ContactList list) throws ImException
		{
			debug(TAG, "add contact to " + list.getName());
			org.jivesoftware.smack.packet.Presence response =
				new org.jivesoftware.smack.packet.Presence(org.jivesoftware.smack.packet.Presence.Type.subscribed);
			response.setTo(address);

			sendPacket(response);

			Roster roster = mConnection.getRoster();
			String[] groups = new String[] { list.getName() };
			try {
				final String name = parseAddressName(address);
				roster.createEntry(address, name, groups);

				// If contact exists locally, don't create another copy
				Contact contact = makeContact(name, address);
				if (!containsContact(contact))
				{
					notifyContactListUpdated(list, ContactListListener.LIST_CONTACT_ADDED, contact);
				}
				else
				{
					debug(TAG, "skip adding existing contact locally " + name);
				}
			}
			catch (XMPPException e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public void declineSubscriptionRequest(String contact)
		{
			debug(TAG, "decline subscription");
            org.jivesoftware.smack.packet.Presence response =
            	new org.jivesoftware.smack.packet.Presence(org.jivesoftware.smack.packet.Presence.Type.unsubscribed);
            response.setTo(contact);
            sendPacket(response);
            mContactListManager.getSubscriptionRequestListener().onSubscriptionDeclined(contact);
		}

		@Override
		public void approveSubscriptionRequest(String contact)
		{
			debug(TAG, "approve subscription");
			try
			{
				mContactListManager.doAddContactToListAsync(contact, getDefaultContactList());
			}
			catch (ImException e)
			{
				Log.e(TAG, "failed to add " + contact + " to default list");
			}
			mContactListManager.getSubscriptionRequestListener().onSubscriptionApproved(contact);
		}

		@Override
		public Contact createTemporaryContact(String address)
		{
			debug(TAG, "create temporary " + address);
			return makeContact(parseAddressName(address),address);
		}
	}
	/*
	 * Alarm event fired
	 * @see info.guardianproject.otr.app.im.engine.ImConnection#sendHeartbeat()
	 */
	public void sendHeartbeat() {
		// Don't let heartbeats queue up if we have long running tasks - only
		// do the heartbeat if executor is idle.
		boolean success = executeIfIdle(new Runnable() {
			@Override
			public void run() {
				debug(TAG, "heartbeat state = " + getState());
				doHeartbeat();
			}
		});
		
		if (!success) {
			debug(TAG, "failed to schedule heartbeat state = " + getState());
		}
	}
	
	// Runs in executor thread
	public void doHeartbeat() {
		if (mConnection == null && mRetryLogin) {
			debug(TAG, "reconnect with login");
			do_login();
		}
		
		if (mConnection == null)
			return;
		
		if (getState() == SUSPENDED) {
			debug(TAG, "heartbeat during suspend");
			return;
		}
		
		if (mNeedReconnect) {
			reconnect();
		}
		else if (mConnection.isConnected() && getState() == LOGGED_IN) {
	//		debug(TAG, "ping");
	//		if (!sendPing()) {
	//			Log.w(TAG, "reconnect on ping failed");
	//			setState(LOGGING_IN, new ImErrorInfo(ImErrorInfo.NETWORK_ERROR, "network timeout"));
	//			force_reconnect();
	//		}
		}
	}
	
	private void clearPing() {
		debug(TAG, "clear ping");
		mPingCollector = null;
	}
	
	// Runs in executor thread
	private boolean sendPing() {
		// Check ping result from previous send
		if (mPingCollector != null) {
			IQ result = (IQ)mPingCollector.pollResult();
			mPingCollector.cancel();
			if (result == null || result.getError() != null)
			{
				clearPing();
				Log.e(TAG, "ping timeout");
				return false;
			}
		}

	    IQ req = new IQ() {
			public String getChildElementXML() {
				return "<ping xmlns='urn:xmpp:ping'/>";
			}
		};
		
		req.setType(IQ.Type.GET);
	    PacketFilter filter = new AndFilter(new PacketIDFilter(req.getPacketID()),
	            new PacketTypeFilter(IQ.class));
	    mPingCollector = mConnection.createPacketCollector(filter);
	    mConnection.sendPacket(req);
	    return true;
	}

	// watch out, this is a different XMPPConnection class than XmppConnection! ;)
	// org.jivesoftware.smack.XMPPConnection
	//    - vs -
	// info.guardianproject.otr.app.im.plugin.xmpp.XmppConnection
	public static class MyXMPPConnection extends XMPPConnection {

		public MyXMPPConnection(Context context, ConnectionConfiguration config) {
			super(context, config);
			
			//this.getConfiguration().setSocketFactory(arg0)
			
		}
		
		public void shutdown() {
			
			try
			{
				// Be forceful in shutting down since SSL can get stuck
				try { socket.shutdownInput(); } catch (Exception e) {}
				socket.close();
				shutdown(new org.jivesoftware.smack.packet.Presence(org.jivesoftware.smack.packet.Presence.Type.unavailable));
			
			}
			catch (Exception e)
			{
				Log.e(TAG, "error on shutdown()",e);
			}
		}
	}

	@Override
	public void networkTypeChanged() {
		super.networkTypeChanged();
	}

	/*
	 * Force a shutdown and reconnect, unless we are already reconnecting.
	 * 
	 * Runs in executor thread
	 */
	private void force_reconnect() {
		debug(TAG, "force_reconnect need=" + mNeedReconnect);
		if (mConnection == null)
			return;
		if (mNeedReconnect)
			return;
		
		mNeedReconnect = true;

		try {
			if (mConnection != null && mConnection.isConnected())
			{
				mConnection.shutdown();
			}
		}
		catch (Exception e) {
			Log.w(TAG, "problem disconnecting on force_reconnect: " + e.getMessage());
		}
		
		reconnect();
	}

	/*
	 * Reconnect unless we are already in the process of doing so.
	 * 
	 * Runs in executor thread.
	 */
	private void maybe_reconnect() {
		debug(TAG, "maybe_reconnect mNeedReconnect=" + mNeedReconnect + " state=" + getState() +
				" connection?=" + (mConnection != null));

		// This is checking whether we are already in the process of reconnecting.  If we are,
		// doHeartbeat will take care of reconnecting.
		if (mNeedReconnect)
			return;
		
		if (getState() == SUSPENDED)
			return;
		
		if (mConnection == null)
			return;
		
		mNeedReconnect = true;
		reconnect();
	}
	
	/*
	 * Retry connecting
	 * 
	 * Runs in executor thread
	 */
	private void reconnect()
	{
		if (getState() == SUSPENDED)
		{
			debug(TAG, "reconnect during suspend, ignoring");
			return;
		}
		try
		{
			Thread.sleep(2000);  // Wait for network to settle
		}
		catch (InterruptedException e) { /* ignore */ }
		
		if (mConnection != null)
		{
			// It is safe to ask mConnection whether it is connected, because either:
			// - We detected an error using ping and called force_reconnect, which did a shutdown
			// - Smack detected an error, so it knows it is not connected
			// so there are no cases where mConnection can be confused about being connected here.
			// The only left over cases are reconnect() being called too many times due to errors
			// reported multiple times or errors reported during a forced reconnect.
			if (mConnection.isConnected())
			{
				Log.w(TAG, "reconnect while already connected, assuming good");
				mNeedReconnect = false;
				setState(LOGGED_IN, null);
				return;
			}
			Log.i(TAG, "reconnect");
			clearPing();
			try
			{
				if (mStreamHandler.isResumePossible())
				{
					// Connect without binding, will automatically trigger a resume
					mConnection.connect();
//					mConnection.connect(false);
					//initServiceDiscovery();
				}
				else
				{
					mConnection.connect();
					//initServiceDiscovery();
					if (!mConnection.isAuthenticated())
					{
						// This can happen if a reconnect failed and the smack connection now has wasAuthenticated = false.
						// It can also happen if auth exception was swallowed by smack.
						// Try to login manually.
						
						Log.e(TAG, "authentication did not happen in connect() - login manually");
						mConnection.login(mUsername, mPassword, mResource);
						
						// Make sure
						if (!mConnection.isAuthenticated())
						{
							throw new XMPPException("manual auth failed");
						}
						// Manually set the state since manual auth doesn't notify listeners
						mNeedReconnect = false;
						setState(LOGGED_IN, null);
					}
				}
			}
			catch (Exception e)
			{
				mConnection.shutdown();
				Log.e(TAG, "reconnection attempt failed", e);
				// Smack incorrectly notified us that reconnection was successful, reset in case it fails
				mNeedReconnect = true;
				setState(LOGGING_IN, new ImErrorInfo(ImErrorInfo.NETWORK_ERROR, e.getMessage()));
			}
		}
		else
		{
			mNeedReconnect = true;
			debug(TAG, "reconnection on network change failed");
			setState(LOGGING_IN, new ImErrorInfo(ImErrorInfo.NETWORK_ERROR, "reconnection on network change failed"));
		}
	}
	
	@Override
	protected void setState(int state, ImErrorInfo error) {
		debug(TAG, "setState to " + state);
		super.setState(state, error);
		if (state == LOGGED_IN)
		{
			if (mChatGroupManager != null)
			{
				mChatGroupManager.bindConnection(mConnection);
				mChatGroupManager.rejoinRooms();
			}
			if (mFileTransferManager != null)
			{
				mFileTransferManager.bindConnection(mConnection);
			}
		}
	}

	public static void debug (String tag, String msg)
	{
		//Log.d(tag, msg);
	}


	@Override
	public void handle(Callback[] arg0) throws IOException {
		
		for (Callback cb : arg0)
		{
			debug(TAG, cb.toString());
		}
		
	}
	
	/*
	public class MySASLDigestMD5Mechanism extends SASLMechanism
	{
		 
	    public MySASLDigestMD5Mechanism(SASLAuthentication saslAuthentication)
	    {
	        super(saslAuthentication);
	    }
	 
	    protected void authenticate()
	        throws IOException, XMPPException
	    {
	        String mechanisms[] = {
	            getName()
	        };
	        java.util.Map props = new HashMap();
	        sc = Sasl.createSaslClient(mechanisms, null, "xmpp", hostname, props, this);
	        super.authenticate();
	    }
	 
	    public void authenticate(String username, String host, String password)
	        throws IOException, XMPPException
	    {
	        authenticationId = username;
	        this.password = password;
	        hostname = host;
	        String mechanisms[] = {
	            getName()
	        };
	        java.util.Map props = new HashMap();
	        sc = Sasl.createSaslClient(mechanisms, null, "xmpp", host, props, this);
	        super.authenticate();
	    }
	 
	    public void authenticate(String username, String host, CallbackHandler cbh)
	        throws IOException, XMPPException
	    {
	        String mechanisms[] = {
	            getName()
	        };
	        java.util.Map props = new HashMap();
	        sc = Sasl.createSaslClient(mechanisms, null, "xmpp", host, props, cbh);
	        super.authenticate();
	    }
	 
	    protected String getName()
	    {
	        return "DIGEST-MD5";
	    }
	 
	    public void challengeReceived(String challenge)
	        throws IOException
	    {
	        //StringBuilder stanza = new StringBuilder();
	        byte response[];
	        if(challenge != null)
	            response = sc.evaluateChallenge(Base64.decode(challenge));
	        else
	            //response = sc.evaluateChallenge(null);
	            response = sc.evaluateChallenge(new byte[0]);
	        //String authenticationText = "";
	        Packet responseStanza;
	        //if(response != null)
	        //{
	            //authenticationText = Base64.encodeBytes(response, 8);
	            //if(authenticationText.equals(""))
	                //authenticationText = "=";
	           
	            if (response == null){
	                responseStanza = new Response();
	            } else {
	                responseStanza = new Response(Base64.encodeBytes(response,Base64.DONT_BREAK_LINES));   
	            }
	        //}
	        //stanza.append("<response xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\">");
	        //stanza.append(authenticationText);
	        //stanza.append("</response>");
	        //getSASLAuthentication().send(stanza.toString());
	        getSASLAuthentication().send(responseStanza);
	    }
	}
	 */
	private void initServiceDiscovery()
	{
		debug(TAG, "init service discovery");
		// register connection features
		ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(mConnection);
		if (sdm == null)
		{
			sdm = new ServiceDiscoveryManager(mConnection);
		}
		sdm.addFeature("http://jabber.org/protocol/disco#info");
		sdm.addFeature("http://jabber.org/protocol/disco#items");
		sdm.addFeature(DeliveryReceipts.NAMESPACE);
		sdm.addFeature(UserLoc.NAMESPACE);
		sdm.addFeature(UserLoc.NAMESPACE+"+notify");
	}
}
