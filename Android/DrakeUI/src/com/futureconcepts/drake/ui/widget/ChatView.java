package com.futureconcepts.drake.ui.widget;

import com.futureconcepts.drake.client.Contact;
import com.futureconcepts.drake.client.DrakeIntent;
import com.futureconcepts.drake.client.IChatListener;
import com.futureconcepts.drake.client.IChatSession;
import com.futureconcepts.drake.client.IChatSessionListener;
import com.futureconcepts.drake.client.IChatSessionManager;
import com.futureconcepts.drake.client.IContactList;
import com.futureconcepts.drake.client.IContactListListener;
import com.futureconcepts.drake.client.IContactListManager;
import com.futureconcepts.drake.client.IImConnection;
import com.futureconcepts.drake.client.IOtrChatSession;
import com.futureconcepts.drake.client.IOtrKeyManager;
import com.futureconcepts.drake.client.ImErrorInfo;
import com.futureconcepts.drake.client.Imps;
import com.futureconcepts.drake.client.Message;
import com.futureconcepts.drake.client.MessengerServiceConnection;
import com.futureconcepts.drake.client.constants.ImConnectionConstants;
import com.futureconcepts.drake.ui.R;
import com.futureconcepts.drake.ui.os.SimpleAlertHandler;
import com.futureconcepts.drake.ui.utils.ChatBackgroundMaker;
import com.futureconcepts.drake.ui.utils.Markup;
import com.futureconcepts.drake.ui.utils.PresenceUtils;
import com.futureconcepts.drake.ui.widget.MessageView.DeliveryState;

import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Browser;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

public class ChatView extends LinearLayout
{	
	private static final String LOG_TAG = ChatView.class.getSimpleName();
	
    // This projection and index are set for the query of active chats
    private static final String[] CHAT_PROJECTION = {
        Imps.Contacts._ID,
        Imps.Contacts.USERNAME,
        Imps.Contacts.NICKNAME,
        Imps.Contacts.TYPE,
        Imps.Presence.PRESENCE_STATUS,
        Imps.Chats.LAST_UNREAD_MESSAGE,
    };
    private static final int CONTACT_ID_COLUMN             = 0;
    private static final int USERNAME_COLUMN               = 1;
    private static final int NICKNAME_COLUMN               = 2;
    private static final int TYPE_COLUMN                   = 3;
    private static final int PRESENCE_STATUS_COLUMN        = 4;
    private static final int LAST_UNREAD_MESSAGE_COLUMN    = 5;

    static final StyleSpan STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    private MessengerServiceConnection _serviceConnection;
    private Markup mMarkup;

    private Activity mScreen;
    private SimpleAlertHandler mHandler;
    private Cursor mCursor;

    private ImageView   mStatusIcon;
    private TextView    mTitle;
    /*package*/ListView    mHistory;
    private EditText    mComposeMessage;
    private Button      mSendButton;
    private View mStatusWarningView;
    private ImageView mWarningIcon;
    private TextView mWarningText;

    private MessageAdapter mMessageAdapter;
    private IChatSessionManager mChatSessionManager;
    private IChatSessionListener mChatSessionListener;

    private IChatSession mChatSession;
    private IOtrKeyManager mOtrKeyManager;
    private IOtrChatSession mOtrChatSession;
    private boolean mIsOtrChat = false;
    
    private long mChatId;
    private int mType;
    private String mNickName;
    private String mUserName;
    private Context mContext; // TODO
    private int mPresenceStatus;

    private int mViewType;

    private static final int VIEW_TYPE_CHAT = 1;
    private static final int VIEW_TYPE_INVITATION = 2;
    private static final int VIEW_TYPE_SUBSCRIPTION = 3;

    private static final int QUERY_TOKEN = 10;

    // Async QueryHandler
    private final class QueryHandler extends AsyncQueryHandler {
        public QueryHandler(Context context) {
            super(context.getContentResolver());
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor c) {
            Cursor cursor = new DeltaCursor(c);

            if (Log.isLoggable(LOG_TAG, Log.DEBUG))
            {
                log("onQueryComplete: cursor.count=" + cursor.getCount());
            }

            mMessageAdapter.changeCursor(cursor);
        }
    }
    
    private QueryHandler mQueryHandler;

	public SimpleAlertHandler getHandler ()
	{
		return mHandler;
	}
	
	public int getType ()
	{
		return mViewType;
	}
	
    private class RequeryCallback implements Runnable {
        public void run() {
//            if (Log.isLoggable(ImApp.LOG_TAG, Log.DEBUG)){
                log("RequeryCallback");
//            }
            requeryCursor();
        }
    }
    private RequeryCallback mRequeryCallback = null;

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (!(view instanceof MessageView)) {
                return;
            }
            Cursor cursor = (Cursor)mHistory.getAdapter().getItem(position);
            cursor.moveToPosition(position);
            int messageType = cursor.getInt(cursor.getColumnIndex(Imps.Messages.TYPE)); 
            if (messageType == Imps.MessageType.FILE_TRANSFER)
            {
            	onFileTransferItemClick(cursor);
            	return;
            }
            URLSpan[] links = ((MessageView)view).getMessageLinks();
            if (links.length == 0){
                return;
            }

            final ArrayList<String> linkUrls = new ArrayList<String>(links.length);
            for (URLSpan u : links) {
                linkUrls.add(u.getURL());
            }
            ArrayAdapter<String> a = new ArrayAdapter<String>(mScreen,
                    android.R.layout.select_dialog_item, linkUrls);
            AlertDialog.Builder b = new AlertDialog.Builder(mScreen);
            b.setTitle(R.string.select_link_title);
            b.setCancelable(true);
            b.setAdapter(a, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Uri uri = Uri.parse(linkUrls.get(which));
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.putExtra(Browser.EXTRA_APPLICATION_ID, mScreen.getPackageName());
                    mScreen.startActivity(intent);
                }
            });
            b.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            b.show();
        }
    };

    private void onFileTransferItemClick(Cursor cursor)
    {
    	String status = cursor.getString(cursor.getColumnIndex(Imps.Messages.BODY));
    	if (status != null && status.equals("Viewable"))
    	{
	    	String url = cursor.getString(cursor.getColumnIndex(Imps.Messages.FILE_PATH));
	    	if (url != null)
	    	{
	    		Uri uri = Uri.parse(url);
	    		if (uri != null)
	    		{
	    			String mimeType = cursor.getString(cursor.getColumnIndex(Imps.Messages.MIME_TYPE));
	    			if (mimeType != null)
	    			{
	    				Intent intent = new Intent(Intent.ACTION_VIEW);
	    				intent.setDataAndType(uri, mimeType);
	    				mScreen.startActivity(intent);
	    			}
	    		}
	    	}
    	}
    }
    
    private IChatListener mChatListener = new IChatListener.Stub()
    {
        @Override
        public void onIncomingMessage(IChatSession ses, Message msg)
        {
            scheduleRequery(0);
        }

//        @Override
//        public void onIncomingFileTransferRequest(final IChatSession ses, final FileTransferStatus fileTransferStatus)
//        {
//        	mHandler.post(new Runnable() {
//				@Override
//				public void run()
//				{
//		            AlertDialog.Builder b = new AlertDialog.Builder(mScreen);
//		            b.setTitle("Incoming File Transfer Request");
//		            b.setCancelable(true);
//		            b.setMessage(String.format("%s would like to send you %s", fileTransferStatus.getRequestor(), fileTransferStatus.getFileName()));
//		            b.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int which)
//						{
//							try
//							{
//								ses.acceptFileTransferRequest(fileTransferStatus.getID());
//							}
//							catch (RemoteException e)
//							{
//								e.printStackTrace();
//							}
//							dialog.dismiss();
//						}
//		            });
//		            b.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//		                public void onClick(DialogInterface dialog, int which) {
//		                	try
//		                	{
//		                		ses.rejectFileTransferRequest(fileTransferStatus.getID());
//		                	}
//		                	catch (RemoteException e)
//		                	{
//		                		e.printStackTrace();
//		                	}
//		                    dialog.dismiss();
//		                }
//		            });
//		            b.show();
//				}
 //       	});
  //      }
        
        @Override
        public void onContactJoined(IChatSession ses, Contact contact)
        {
            scheduleRequery(0);
        }

        @Override
        public void onContactLeft(IChatSession ses, Contact contact)
        {
            scheduleRequery(0);
        }

        @Override
        public void onSendMessageError(IChatSession ses, Message msg, ImErrorInfo error)
        {
            scheduleRequery(0);
        }
        
        public void onIncomingReceipt(IChatSession ses, String packetId) throws RemoteException
        {
            scheduleRequery(0);
        }
        
        public void onStatusChanged(IChatSession ses) throws RemoteException
        {
        	scheduleRequery(0);
        }

		@Override
		public void onConvertedToGroupChat(IChatSession ses) throws RemoteException
		{
		}

		@Override
		public void onInviteError(IChatSession ses, ImErrorInfo error) throws RemoteException
		{
		};
		
	//	@Override
	//	public void onFileTransferStatusChanged(IChatSession ses, FileTransferStatus fileTransferStatus)
	//	{
	//		Log.d(LOG_TAG, fileTransferStatus.getStatus());
	//		scheduleRequery(0);
	//	}
    };

    private Runnable mUpdateChatCallback = new Runnable() {
        public void run() {
            if (mCursor.requery() && mCursor.moveToFirst()) {
                updateChat();
            }
        }
    };
    private IContactListListener mContactListListener = new IContactListListener.Stub () {
        public void onAllContactListsLoaded() {
        }

        public void onContactChange(int type, IContactList list, Contact contact){
        }

        public void onContactError(int errorType, ImErrorInfo error, String listName, Contact contact) {
        }

        public void onContactsPresenceUpdate(Contact[] contacts)
        {
            if (Log.isLoggable(LOG_TAG, Log.DEBUG))
            {
                log("onContactsPresenceUpdate()");
            }
            for (Contact c : contacts) {
                if (c.getAddress().equals(mUserName)) {
                    mHandler.post(mUpdateChatCallback);
                    scheduleRequery(0);
                    break;
                }
            }
        }
    };

    static final void log(String msg)
    {
        Log.d(LOG_TAG, msg);
    }

    public ChatView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mScreen = (Activity) context;
        mHandler = new ChatViewHandler();
        mContext = context;
        
    }

    public void setServiceConnection(MessengerServiceConnection serviceConnection)
    {
    	_serviceConnection = serviceConnection;
    	registerChatListener();
    }
    
    @Override
    protected void onFinishInflate()
    {
        mStatusIcon     = (ImageView) findViewById(R.id.statusIcon);
        mTitle          = (TextView) findViewById(R.id.title);
        mHistory        = (ListView) findViewById(R.id.history);
        mComposeMessage       = (EditText) findViewById(R.id.composeMessage);
        mSendButton     = (Button)findViewById(R.id.btnSend);
        mHistory.setOnItemClickListener(mOnItemClickListener);

        mStatusWarningView = findViewById(R.id.warning);
        mWarningIcon = (ImageView)findViewById(R.id.warningIcon);
        mWarningText = (TextView)findViewById(R.id.warningText);

        Button approveSubscription = (Button)findViewById(R.id.btnApproveSubscription);
        Button declineSubscription = (Button)findViewById(R.id.btnDeclineSubscription);

        mWarningText.setOnTouchListener(new OnTouchListener()
        {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				viewProfile();
				return false;
			}
        });
        
        approveSubscription.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                approveSubscription();
            }
        });
        declineSubscription.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                declineSubscription();
            }
        });

        mComposeMessage.setOnKeyListener(new OnKeyListener(){
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                            sendMessage();
                            return true;

                        case KeyEvent.KEYCODE_ENTER:
                            if (event.isAltPressed()) {
                                mComposeMessage.append("\n");
                                return true;
                            }
                    }
                }
                return false;
            }
        });

        mComposeMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null) {
                    if (event.isAltPressed()) {
                        return false;
                    }
                }
                sendMessage();
                return true;
            }
        });

        // TODO: this is a hack to implement BUG #1611278, when dispatchKeyEvent() works with
        // the soft keyboard, we should remove this hack.
        mComposeMessage.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int before, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int after) {
                //log("TextWatcher: " + s);
                userActionDetected();
            }

            public void afterTextChanged(Editable s) {
            }
        });

        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    public void onResume()
    {
        if (mViewType == VIEW_TYPE_CHAT)
        {
            Cursor cursor = getMessageCursor();
            if (cursor == null)
            {
                startQuery();
            }
            else
            {
                requeryCursor();
            }
        }
        if (_serviceConnection != null)
        {
        	if (_serviceConnection.isConnected())
        	{
                registerChatListener();
                mHandler.post(mUpdateChatCallback);
                updateWarningView();
        	}
        	else
        	{
        		mHandler.post(mUpdateChatCallback);
        		updateWarningView();
        	}
        }
    }

    public void onPause()
    {
        Cursor cursor = getMessageCursor();
        if (cursor != null)
        {
            cursor.deactivate();
        }
        cancelRequery();
        if (mViewType == VIEW_TYPE_CHAT && mChatSession != null)
        {
            try
            {
            	if (mChatSession != null)
            	{
            		mChatSession.markAsRead();
            	}
            }
            catch (RemoteException e)
            {
                mHandler.showServiceErrorAlert();
            }
        }
        unregisterChatListener();
        unregisterChatSessionListener();
    }

    /*
    private void closeSoftKeyboard() {
        InputMethodManager inputMethodManager =
            (InputMethodManager)mApp.getSystemService(Context.INPUT_METHOD_SERVICE);

        inputMethodManager.hideSoftInputFromWindow(mComposeMessage.getWindowToken(), 0);
    }
*/
    void updateChat()
    {
        setViewType(VIEW_TYPE_CHAT);

        long oldChatId = mChatId;

        updateContactInfo();

        setStatusIcon();
        setTitle();

        IImConnection conn = null;
        try
        {
	        conn = _serviceConnection.getConnection();
	        if (conn == null)
	        {
	        	throw new Exception("Connection has been signed out");
	        }
        }
        catch (Exception e)
        {
        	e.printStackTrace();
            mScreen.finish();
            return;
        }
        if (mMessageAdapter == null)
        {
            mMessageAdapter = new MessageAdapter(mScreen, null);
            mHistory.setAdapter(mMessageAdapter);
        }

        // only change the message adapter when we switch to another chat
        if (mChatId != oldChatId)
        {
            startQuery();
            mComposeMessage.setText("");
            mOtrChatSession = null;
        }
        updateWarningView();
    }

    private void updateContactInfo()
    {
        mChatId = mCursor.getLong(CONTACT_ID_COLUMN);
        mPresenceStatus = mCursor.getInt(PRESENCE_STATUS_COLUMN);
        mType = mCursor.getInt(TYPE_COLUMN);
        mUserName = mCursor.getString(USERNAME_COLUMN);
        mNickName = mCursor.getString(NICKNAME_COLUMN);
    }

    private void setTitle()
    {
//        if (mType == Imps.Contacts.TYPE_GROUP)
//       {
//            final String[] projection = {Imps.GroupMembers.NICKNAME};
//            Uri memberUri = ContentUris.withAppendedId(Imps.GroupMembers.CONTENT_URI, mChatId);
//            ContentResolver cr = mScreen.getContentResolver();
//            Cursor c = cr.query(memberUri, projection, null, null, null);
//            StringBuilder buf = new StringBuilder();
//            if(c != null)
//            {
//                while(c.moveToNext())
//                {
//                    buf.append(c.getString(0));
//                    if(!c.isLast())
//                    {
//                        buf.append(',');
//                    }
//                }
//                c.close();
//            }
//            mTitle.setText(mContext.getString(R.string.chat_with, buf.toString()));
//        }
//        else
        {
            mTitle.setText(mContext.getString(R.string.chat_with, mNickName));
        }
    }

    private void setStatusIcon()
    {
        if (mType == Imps.Contacts.TYPE_GROUP)
        {
            // hide the status icon for group chat.
            mStatusIcon.setVisibility(GONE);
        }
        else
        {
            mStatusIcon.setVisibility(VISIBLE);
            int presenceResId = PresenceUtils.getStatusIconId(mPresenceStatus);
            mStatusIcon.setImageDrawable(mContext.getResources().getDrawable(presenceResId));
        }
    }

    public void bindChat(long chatId)
    {
        if (mCursor != null)
        {
            mCursor.deactivate();
        }
        Uri contactUri = ContentUris.withAppendedId(Imps.Contacts.CONTENT_URI, chatId);
        mCursor = mScreen.managedQuery(contactUri, CHAT_PROJECTION, null, null, null);
        if (mCursor == null || !mCursor.moveToFirst())
        {
            if (Log.isLoggable(LOG_TAG, Log.DEBUG))
            {
                log("Failed to query chat: " + chatId);
            }
            mScreen.finish();
            return;
        }
        else
        {
            mChatSession = getChatSession(mCursor);
          
        //    initOtr();
            
            updateChat();
            registerChatListener();
        }
    }

    private void initOtr ()
    {
        try
        {
        	if (mOtrChatSession == null && mChatSession != null)
        	{
        		mOtrChatSession = mChatSession.getOtrChatSession();
        		if (mOtrChatSession != null)
            		Log.i(LOG_TAG, "OtrChatSession was init'd");
        	}
        	if (mOtrChatSession != null)
        	{
        		if (mOtrKeyManager == null)
        		{
        			mOtrKeyManager = mChatSession.getOtrKeyManager();
        			if (mOtrKeyManager != null)
        			{
                		Log.i(LOG_TAG, "OtrKeyManager is init'd");
        			}
        		}
        	}
        }
        catch (Exception e)
        {
        	Log.e(LOG_TAG, "unable to get otr key mgr",e);
        }
    }
    
    public void bindSubscription(String from)
    {
        mUserName = from;

        setViewType(VIEW_TYPE_SUBSCRIPTION);

        TextView text =  (TextView)findViewById(R.id.txtSubscription);
        text.setText(mContext.getString(R.string.subscription_prompt, from));
        mTitle.setText(mContext.getString(R.string.chat_with, from));
    }

    void approveSubscription()
    {
        try
        {
            IImConnection conn = _serviceConnection.getConnection();
            IContactListManager manager = conn.getContactListManager();
            manager.approveSubscription(mUserName);
        }
        catch (RemoteException ex)
        {
            mHandler.showServiceErrorAlert();
        }
        mScreen.finish();
    }

    void declineSubscription()
    {
        try
        {
            IImConnection conn = _serviceConnection.getConnection();
            IContactListManager manager = conn.getContactListManager();
            manager.declineSubscription(mUserName);
        }
        catch (RemoteException ex)
        {
            mHandler.showServiceErrorAlert();
        }
        mScreen.finish();
    }

    private void setViewType(int type)
    {
        mViewType = type;
        if (type == VIEW_TYPE_CHAT)
        {
            findViewById(R.id.invitationPanel).setVisibility(GONE);
            findViewById(R.id.subscription).setVisibility(GONE);
            setChatViewEnabled(true);
        }
        else if (type == VIEW_TYPE_INVITATION)
        {
            setChatViewEnabled(false);
            findViewById(R.id.invitationPanel).setVisibility(VISIBLE);
            findViewById(R.id.btnAccept).requestFocus();
        }
        else if (type == VIEW_TYPE_SUBSCRIPTION)
        {
            setChatViewEnabled(false);
            findViewById(R.id.subscription).setVisibility(VISIBLE);
            findViewById(R.id.btnApproveSubscription).requestFocus();
        }
    }

    private void setChatViewEnabled(boolean enabled)
    {
        mComposeMessage.setEnabled(enabled);
        mSendButton.setEnabled(enabled);
        if (enabled)
        {
            mComposeMessage.requestFocus();
        }
        else
        {
            mHistory.setAdapter(null);
        }
    }

    private void startQuery()
    {
        if (mQueryHandler == null)
        {
            mQueryHandler = new QueryHandler(mContext);
        }
        else
        {
            // Cancel any pending queries
            mQueryHandler.cancelOperation(QUERY_TOKEN);
        }

        Uri uri = Imps.Messages.getContentUriByThreadId(mChatId);

        if (Log.isLoggable(LOG_TAG, Log.DEBUG))
        {
            log("queryCursor: uri=" + uri);
        }

        Log.d("ChatView.startQuery", "uri=" + uri.toString());
        mQueryHandler.startQuery(QUERY_TOKEN, null,
                uri,
                null,
                null /* selection */,
                null /* selection args */,
                null);
    }

    void scheduleRequery(long interval)
    {
        if (mRequeryCallback == null)
        {
            mRequeryCallback = new RequeryCallback();
        }
        else
        {
            mHandler.removeCallbacks(mRequeryCallback);
        }

        if (Log.isLoggable(LOG_TAG, Log.DEBUG))
        {
            log("scheduleRequery");
        }
        mHandler.postDelayed(mRequeryCallback, interval);
    }

    void cancelRequery() {
        if (mRequeryCallback != null)
        {
            if (Log.isLoggable(LOG_TAG, Log.DEBUG))
            {
                log("cancelRequery");
            }
            mHandler.removeCallbacks(mRequeryCallback);
            mRequeryCallback = null;
        }
    }

    void requeryCursor() {
        if (mMessageAdapter.isScrolling()) {
            mMessageAdapter.setNeedRequeryCursor(true);
            return;
        }
        
        // This is redundant if there are messages in view, because the cursor requery will update everything.
        // However, if there are no messages, no update will trigger below, and we still want this to update.
        updateWarningView();
        
        // TODO: async query?
        Cursor cursor = getMessageCursor();
        if (cursor != null) {
            cursor.requery();
        }
    }

    private Cursor getMessageCursor() {
        return mMessageAdapter == null ? null : mMessageAdapter.getCursor();
    }

    public void insertSmiley(String smiley) {
        mComposeMessage.append(mMarkup.applyEmoticons(smiley));
    }

    public void closeChatSession() {
        if (mChatSession != null) {
            try {
                mChatSession.leave();
            } catch (RemoteException e) {
                mHandler.showServiceErrorAlert();
            }
        } else {
            // the conversation is already closed, clear data in database
            ContentResolver cr = mContext.getContentResolver();
            cr.delete(ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, mChatId),
                    null, null);
        }
        mScreen.finish();
    }

    public void closeChatSessionIfInactive() {
        if (mChatSession != null) {
            try {
                mChatSession.leaveIfInactive();
            } catch (RemoteException e) {
                mHandler.showServiceErrorAlert();
            }
        }
    }

    public void viewProfile() {
    	String remoteFingerprint = null;
    	String localFingerprint = null;
    	
    	
    	String mLocalUserName = "";
    	boolean isVerified = false;
    	
    	if (mOtrKeyManager == null)
    		 initOtr ();

    	Uri data = ContentUris.withAppendedId(Imps.Contacts.CONTENT_URI, mChatId);
    	
        Intent intent = new Intent(Intent.ACTION_VIEW, data);

    	if (mOtrKeyManager != null)
    	{
	    	try {
	    		
	    		remoteFingerprint = mOtrKeyManager.getRemoteFingerprint();
	    		localFingerprint = mOtrKeyManager.getLocalFingerprint();
				isVerified = mOtrKeyManager.isKeyVerified(mUserName);
				
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			// TODO define these in ImServiceConstants
	        intent.putExtra("remoteFingerprint", remoteFingerprint);
	        intent.putExtra("localFingerprint", localFingerprint);
	        
	        intent.putExtra("remoteVerified", isVerified);
    	}
    	
        mScreen.startActivity(intent);
       
    }

    public void blockContact()
    {
        // TODO: unify with codes in ContactListView
        DialogInterface.OnClickListener confirmListener = new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton) {
                try {
                    IImConnection conn = _serviceConnection.getConnection();
                    IContactListManager manager = conn.getContactListManager();
                    manager.blockContact(mUserName);
                    mScreen.finish();
                } catch (RemoteException e) {
                    mHandler.showServiceErrorAlert();
                }
            }
        };

        Resources r = getResources();

        // The positive button is deliberately set as no so that
        // the no is the default value
        new AlertDialog.Builder(mContext)
            .setTitle(R.string.confirm)
            .setMessage(r.getString(R.string.confirm_block_contact, mNickName))
            .setPositiveButton(R.string.yes, confirmListener) // default button
            .setNegativeButton(R.string.no, null)
            .setCancelable(false)
            .show();
    }
    public String getUserName() {
        return mUserName;
    }

    public long getChatId () {
        try {
            return mChatSession == null ? -1 : mChatSession.getId();
        } catch (RemoteException e) {
            mHandler.showServiceErrorAlert();
            return -1;
        }
    }

    public IChatSession getCurrentChatSession() {
        return mChatSession;
    }

    private IChatSessionManager getChatSessionManager()
    {
        if (mChatSessionManager == null)
        {
        	try
        	{
	            IImConnection conn = _serviceConnection.getConnection();
                mChatSessionManager = conn.getChatSessionManager();
        	}
        	catch (Exception e)
        	{
                mHandler.showServiceErrorAlert();
        	}
        }
        return mChatSessionManager;
    }

    public IOtrKeyManager getOtrKeyManager ()
    {
    	initOtr();
    	return mOtrKeyManager;
    }
    
    public IOtrChatSession getOtrChatSession ()
    {
    	initOtr();
    	return mOtrChatSession;
    }
    
    private IChatSession getChatSession(Cursor cursor)
    {
        String username = cursor.getString(USERNAME_COLUMN);

        IChatSessionManager sessionMgr = getChatSessionManager();
        if (sessionMgr != null)
        {
            try
            {
                return sessionMgr.getChatSession(username);
            }
            catch (RemoteException e)
            {
                mHandler.showServiceErrorAlert();
            }
        }
        return null;
    }

    public boolean isGroupChat() {
        return Imps.Contacts.TYPE_GROUP == mType;
    }

    void sendMessage() {
        String msg = mComposeMessage.getText().toString();

        if (TextUtils.isEmpty(msg.trim())) {
            return;
        }

        if (mChatSession != null) {
            try {
                mChatSession.sendMessage(msg);
                mComposeMessage.setText("");
                mComposeMessage.requestFocus();
                requeryCursor();
            } catch (RemoteException e) {
                mHandler.showServiceErrorAlert();
            }
            catch (Exception e) {
                mHandler.showServiceErrorAlert();
            }
        }

        // Close the soft on-screen keyboard if we're in landscape mode so the user can see the
        // conversation.
        /*
        Configuration config = getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
          //  closeSoftKeyboard();
        }*/
        
    }

    private void registerChatListener()
    {
        if (Log.isLoggable(LOG_TAG, Log.DEBUG))
        {
            log("registerChatListener");
        }
        try
        {
            if (mChatSession != null)
            {
                mChatSession.registerChatListener(mChatListener);
            }
            IImConnection conn = _serviceConnection.getConnection();
            if (conn != null)
            {
                IContactListManager listMgr = conn.getContactListManager();
                listMgr.registerContactListListener(mContactListListener);
            }
        }
        catch (RemoteException e)
        {
            Log.w(LOG_TAG, "registerChatListener fail:" + e.getMessage());
        }
    }

    void unregisterChatListener()
    {
        if (Log.isLoggable(LOG_TAG, Log.DEBUG)){
            log("unregisterChatListener");
        }
        try {
            if (mChatSession != null && mChatListener != null) {
                mChatSession.unregisterChatListener(mChatListener);
            }
            if (_serviceConnection != null)
            {
	            IImConnection conn = _serviceConnection.getConnection();
	            if (conn != null) {
	                IContactListManager listMgr = conn.getContactListManager();
	                listMgr.unregisterContactListListener(mContactListListener);
	            }
            }
        }
        catch (Exception e)
        {
            Log.w(LOG_TAG, "<ChatView> unregisterChatListener fail:" + e.getMessage());
        }
    }

    void registerChatSessionListener() {
        IChatSessionManager sessionMgr = getChatSessionManager();
        if (sessionMgr != null) {
            mChatSessionListener = new ChatSessionListener();
            try {
                sessionMgr.registerChatSessionListener(mChatSessionListener);
            } catch (RemoteException e) {
                mHandler.showServiceErrorAlert();
            }
        }
    }

    void unregisterChatSessionListener() {
        if (mChatSessionListener != null) {
            try {
                IChatSessionManager sessionMgr = getChatSessionManager();
                sessionMgr.unregisterChatSessionListener(mChatSessionListener);
                // We unregister the listener when the chat session we are
                // waiting for has been created or the activity is stopped.
                // Clear the listener so that we won't unregister the listener
                // twice.
                mChatSessionListener = null;
            } catch (RemoteException e) {
                mHandler.showServiceErrorAlert();
            }
        }
    }

    public void updateWarningView()
    {
        int visibility = View.GONE;
        int iconVisibility = View.GONE;
        String message = null;
        boolean isConnected;
        
        mIsOtrChat = false;
        
    	initOtr();

        //check if the chat is otr or not
        if (mOtrChatSession != null)
        {
        	try
        	{
        		mIsOtrChat = mOtrChatSession.isChatEncrypted();
			}
        	catch (RemoteException e)
        	{
				Log.w("Gibber","Unable to call remote OtrChatSession from ChatView",e);
			}
        }
        
        try
        {
            IImConnection conn = _serviceConnection.getConnection();
            int connState = conn.getState();
            isConnected = (conn == null) ? false : connState == ImConnectionConstants.LOGGED_IN;
        }
        catch (RemoteException e)
        {
            // do nothing
            return;
        }

        if (isConnected)
        {
            if (mType == Imps.Contacts.TYPE_TEMPORARY)
            {
                visibility = View.VISIBLE;
                message = mContext.getString(R.string.contact_not_in_list_warning, mNickName);
            }
            else if (mPresenceStatus == Imps.Presence.OFFLINE)
            {
                visibility = View.VISIBLE;
                message = mContext.getString(R.string.contact_offline_warning, mNickName);
            } 
            else
            {
            	visibility = View.VISIBLE;
            }
        	if (mIsOtrChat)
        	{
            	try
            	{
            		if (mOtrKeyManager == null)
            		{
            			initOtr();
            			String rFingerprint = mOtrKeyManager.getRemoteFingerprint();
            			boolean rVerified = mOtrKeyManager.isKeyVerified(mUserName);
            			if (rFingerprint != null)
            			{
            				if (!rVerified)
            				{
            					message = mContext.getString(R.string.otr_session_status_encrypted);
            					mWarningText.setTextColor(Color.BLACK);
            					mWarningText.setBackgroundColor(Color.YELLOW);
            				}
            				else
            				{
            					message = mContext.getString(R.string.otr_session_status_verified);
            					mWarningText.setTextColor(Color.BLACK);
            					mWarningText.setBackgroundColor(Color.GREEN);
            				}
            			}
            			else
            			{
            				mWarningText.setTextColor(Color.WHITE);
            				mWarningText.setBackgroundColor(Color.RED);
            				message = mContext.getString(R.string.otr_session_status_plaintext);
            			}
            		}
            		ImageView imgSec = (ImageView)findViewById(R.id.composeSecureIcon);
            		imgSec.setImageResource(R.drawable.ic_menu_encrypt);
				}
            	catch (RemoteException e)
            	{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        	else
    		{
        		ImageView imgSec = (ImageView)findViewById(R.id.composeSecureIcon);
        		imgSec.setImageResource(R.drawable.ic_menu_unencrypt);
        		
//    			mWarningText.setTextColor(Color.WHITE);
    			mWarningText.setTextColor(Color.BLACK);
//    			mWarningText.setBackgroundColor(Color.RED);
    			mWarningText.setBackgroundColor(Color.GREEN);
//    			message = mContext.getString(R.string.otr_session_status_plaintext);
    			message = mContext.getString(R.string.connected_to_server);
    		}
        }
        else
        {
            visibility = View.VISIBLE;
            iconVisibility = View.VISIBLE;
            message = mContext.getString(R.string.disconnected_warning);
        }
        mStatusWarningView.setVisibility(visibility);
        if (visibility == View.VISIBLE)
        {
            mWarningIcon.setVisibility(iconVisibility);
            mWarningText.setText(message);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        userActionDetected();
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        userActionDetected();
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent ev) {
        userActionDetected();
        return super.dispatchTrackballEvent(ev);
    }

    private void userActionDetected() {
        if (mChatSession != null) {
            try {
                mChatSession.markAsRead();
                // TODO OTRCHAT updateSecureWarning
                //updateSecureWarning();
                updateWarningView();
             
            } catch (RemoteException e) {
                mHandler.showServiceErrorAlert();
            }
        }
    }

    private final class ChatViewHandler extends SimpleAlertHandler {
        public ChatViewHandler() {
            super(mScreen);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
//            switch(msg.what) {
// TODO            
//	            case ImApp.EVENT_CONNECTION_LOGGED_IN:
//	                log("Connection resumed");
//	                updateWarningView();
//	                return;
//	            case ImApp.EVENT_CONNECTION_SUSPENDED:
//	                log("Connection suspended");
//	                updateWarningView();
//	                return;
//	            
 //           }

           
            super.handleMessage(msg);
        }
    }

    class ChatSessionListener extends IChatSessionListener.Stub
    {
        @Override
        public void onChatSessionCreated(IChatSession session)
        {
            try
            {
                if (session.isGroupChatSession())
                {
                    final long id = session.getId();
                    unregisterChatSessionListener();
                    mHandler.post(new Runnable()
                    {
                        public void run()
                        {
                        	bindChat(id);
                        }
                    });
                }
                updateWarningView();
            }
            catch (RemoteException e)
            {
                mHandler.showServiceErrorAlert();
            }
        }

		@Override
		public void onChatSessionCreateError(String name, ImErrorInfo error) throws RemoteException
		{
		}
    }

    public static class DeltaCursor implements Cursor {
        static final String DELTA_COLUMN_NAME = "delta";

        private Cursor mInnerCursor;
        private String[] mColumnNames;
        private int mDateColumn = -1;
        private int mDeltaColumn = -1;

        DeltaCursor(Cursor cursor) {
            mInnerCursor = cursor;

            String[] columnNames = cursor.getColumnNames();
            int len = columnNames.length;

            mColumnNames = new String[len + 1];

            for (int i = 0 ; i < len ; i++) {
                mColumnNames[i] = columnNames[i];
                if (mColumnNames[i].equals(Imps.Messages.DATE)) {
                    mDateColumn = i;
                }
            }

            mDeltaColumn = len;
            mColumnNames[mDeltaColumn] = DELTA_COLUMN_NAME;

            //if (DBG) log("##### DeltaCursor constructor: mDeltaColumn=" +
            //        mDeltaColumn + ", columnName=" + mColumnNames[mDeltaColumn]);
        }

        public int getCount() {
            return mInnerCursor.getCount();
        }

        public int getPosition() {
            return mInnerCursor.getPosition();
        }

        public boolean move(int offset) {
            return mInnerCursor.move(offset);
        }

        public boolean moveToPosition(int position) {
            return mInnerCursor.moveToPosition(position);
        }

        public boolean moveToFirst() {
            return mInnerCursor.moveToFirst();
        }

        public boolean moveToLast() {
            return mInnerCursor.moveToLast();
        }

        public boolean moveToNext() {
            return mInnerCursor.moveToNext();
        }

        public boolean moveToPrevious() {
            return mInnerCursor.moveToPrevious();
        }

        public boolean isFirst() {
            return mInnerCursor.isFirst();
        }

        public boolean isLast() {
            return mInnerCursor.isLast();
        }

        public boolean isBeforeFirst() {
            return mInnerCursor.isBeforeFirst();
        }

        public boolean isAfterLast() {
            return mInnerCursor.isAfterLast();
        }

        public int getColumnIndex(String columnName) {
            if (DELTA_COLUMN_NAME.equals(columnName)) {
                return mDeltaColumn;
            }

            int columnIndex = mInnerCursor.getColumnIndex(columnName);
            return columnIndex;
        }

        public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
            if (DELTA_COLUMN_NAME.equals(columnName)) {
                return mDeltaColumn;
            }

            return mInnerCursor.getColumnIndexOrThrow(columnName);
        }

        public String getColumnName(int columnIndex) {
            if (columnIndex == mDeltaColumn) {
                return DELTA_COLUMN_NAME;
            }

            return mInnerCursor.getColumnName(columnIndex);
        }

        public int getColumnCount() {
            return mInnerCursor.getColumnCount() + 1;
        }

        public void deactivate() {
            mInnerCursor.deactivate();
        }

        public boolean requery() {
            return mInnerCursor.requery();
        }

        public void close() {
            mInnerCursor.close();
        }

        public boolean isClosed() {
            return mInnerCursor.isClosed();
        }

        public void registerContentObserver(ContentObserver observer) {
            mInnerCursor.registerContentObserver(observer);
        }

        public void unregisterContentObserver(ContentObserver observer) {
            mInnerCursor.unregisterContentObserver(observer);
        }

        public void registerDataSetObserver(DataSetObserver observer) {
            mInnerCursor.registerDataSetObserver(observer);
        }

        public void unregisterDataSetObserver(DataSetObserver observer) {
            mInnerCursor.unregisterDataSetObserver(observer);
        }

        public void setNotificationUri(ContentResolver cr, Uri uri) {
            mInnerCursor.setNotificationUri(cr, uri);
        }

        public boolean getWantsAllOnMoveCalls() {
            return mInnerCursor.getWantsAllOnMoveCalls();
        }

        public Bundle getExtras() {
            return mInnerCursor.getExtras();
        }

        public Bundle respond(Bundle extras) {
            return mInnerCursor.respond(extras);
        }

        public String[] getColumnNames() {
            return mColumnNames;
        }

        private void checkPosition() {
            int pos = mInnerCursor.getPosition();
            int count = mInnerCursor.getCount();

            if (-1 == pos || count == pos) {
                throw new CursorIndexOutOfBoundsException(pos, count);
            }
        }

        public byte[] getBlob(int column) {
            checkPosition();

            if (column == mDeltaColumn) {
                return null;
            }

            return mInnerCursor.getBlob(column);
        }

        public String getString(int column) {
            checkPosition();

            if (column == mDeltaColumn) {
                long value = getDeltaValue();
                return Long.toString(value);
            }

            return mInnerCursor.getString(column);
        }

        public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
            checkPosition();

            if (columnIndex == mDeltaColumn) {
                long value = getDeltaValue();
                String strValue = Long.toString(value);
                int len = strValue.length();
                char[] data = buffer.data;
                if (data == null || data.length < len) {
                    buffer.data = strValue.toCharArray();
                } else {
                    strValue.getChars(0, len, data, 0);
                }
                buffer.sizeCopied = strValue.length();
            } else {
                mInnerCursor.copyStringToBuffer(columnIndex, buffer);
            }
        }

        public short getShort(int column) {
            checkPosition();

            if (column == mDeltaColumn) {
                return (short)getDeltaValue();
            }

            return mInnerCursor.getShort(column);
        }

        public int getInt(int column) {
            checkPosition();

            if (column == mDeltaColumn) {
                return (int)getDeltaValue();
            }

            return mInnerCursor.getInt(column);
        }

        public long getLong(int column) {
        //if (DBG) log("DeltaCursor.getLong: column=" + column + ", mDeltaColumn=" + mDeltaColumn);
            checkPosition();

            if (column == mDeltaColumn) {
                return getDeltaValue();
            }

            return mInnerCursor.getLong(column);
        }

        public float getFloat(int column) {
            checkPosition();

            if (column == mDeltaColumn) {
                return getDeltaValue();
            }

            return mInnerCursor.getFloat(column);
        }

        public double getDouble(int column) {
            checkPosition();

            if (column == mDeltaColumn) {
                return getDeltaValue();
            }

            return mInnerCursor.getDouble(column);
        }

        public boolean isNull(int column) {
            checkPosition();

            if (column == mDeltaColumn) {
                return false;
            }

            return mInnerCursor.isNull(column);
        }

        private long getDeltaValue() {
            int pos = mInnerCursor.getPosition();
            //Log.i(LOG_TAG, "getDeltaValue: mPos=" + mPos);

            long t2, t1;

            if (pos == getCount()-1) {
                t1 = mInnerCursor.getLong(mDateColumn);
                t2 = System.currentTimeMillis();
            } else {
                mInnerCursor.moveToPosition(pos + 1);
                t2 = mInnerCursor.getLong(mDateColumn);
                mInnerCursor.moveToPosition(pos);
                t1 = mInnerCursor.getLong(mDateColumn);
            }

            return t2 - t1;
        }

		public int getType(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}
    }

    private class MessageAdapter extends CursorAdapter implements AbsListView.OnScrollListener {
        private int mScrollState;
        private boolean mNeedRequeryCursor;

        private int mNicknameColumn;
        private int mBodyColumn;
        private int mDateColumn;
        private int mTypeColumn;
        private int mErrCodeColumn;
        private int mErrorMessageColumn;
        private int mDeltaColumn;
		private int mDeliveredColumn;
		private int mP1Column;
		private int mP2Column;
        private ChatBackgroundMaker mBgMaker;

        private LayoutInflater mInflater;

        public MessageAdapter(Activity context, Cursor c) {
            super(context, c, false);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mBgMaker = new ChatBackgroundMaker(context);
            if (c != null) {
                resolveColumnIndex(c);
            }
        }

        private void resolveColumnIndex(Cursor c) {
            mNicknameColumn = c.getColumnIndexOrThrow(Imps.Messages.NICKNAME);
            mBodyColumn = c.getColumnIndexOrThrow(Imps.Messages.BODY);
            mDateColumn = c.getColumnIndexOrThrow(Imps.Messages.DATE);
            mTypeColumn = c.getColumnIndexOrThrow(Imps.Messages.TYPE);
            mErrCodeColumn = c.getColumnIndexOrThrow(Imps.Messages.ERROR_CODE);
            mErrorMessageColumn = c.getColumnIndexOrThrow(Imps.Messages.ERROR_MESSAGE);
            mDeltaColumn = c.getColumnIndexOrThrow(DeltaCursor.DELTA_COLUMN_NAME);
            mDeliveredColumn = c.getColumnIndexOrThrow(Imps.Messages.IS_DELIVERED);
            mP1Column = c.getColumnIndexOrThrow(Imps.Messages.LONG_P1);
            mP2Column = c.getColumnIndexOrThrow(Imps.Messages.LONG_P2);
        }

        @Override
        public void changeCursor(Cursor cursor) {
            super.changeCursor(cursor);
            if (cursor != null) {
                resolveColumnIndex(cursor);
            }
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return mInflater.inflate(R.layout.new_message_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            MessageView messageView = (MessageView) view;

            int type = cursor.getInt(mTypeColumn);
            String contact = isGroupChat() ? cursor.getString(mNicknameColumn) : mNickName;
            String body = cursor.getString(mBodyColumn);
            long delta = cursor.getLong(mDeltaColumn);
            long timestamp = cursor.getLong(mDateColumn);
			Date date = new Date(timestamp);
            boolean isDelivered = cursor.getLong(mDeliveredColumn) > 0;
            MessageView.DeliveryState deliveryState = isDelivered ? DeliveryState.DELIVERED
            		: DeliveryState.UNDELIVERED;
            switch (type) {
                case Imps.MessageType.INCOMING:
                	if (body != null)
                		messageView.bindIncomingMessage(contact, body, date, mMarkup, isScrolling());
                    
                    break;

                case Imps.MessageType.OUTGOING:
                case Imps.MessageType.POSTPONED:
                    int errCode = cursor.getInt(mErrCodeColumn);
                    if (errCode != 0) {
                        messageView.bindErrorMessage(errCode);
                    } else {
                        messageView.bindOutgoingMessage(body, date, mMarkup, isScrolling(), deliveryState);
                    }
                    break;
                case Imps.MessageType.FILE_TRANSFER:
                	long fileSize = cursor.getLong(mP1Column);
                	long amountWritten = cursor.getLong(mP2Column);
                	messageView.bindFileTransferMessage(cursor.getString(mErrorMessageColumn), cursor.getString(mNicknameColumn), body, fileSize, amountWritten);
                	break;

                default:
                    messageView.bindPresenceMessage(contact, type, isGroupChat(), isScrolling());
            }
            
            //if (!isScrolling()) {
                mBgMaker.setBackground(messageView, contact, type);
            //}
            updateWarningView();
        }

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount) {
            // do nothing
        }

        public void onScrollStateChanged(AbsListView view, int scrollState) {
            int oldState = mScrollState;
            mScrollState = scrollState;

            if (mChatSession != null) {
                try {
                    mChatSession.markAsRead();
                } catch (RemoteException e) {
                    mHandler.showServiceErrorAlert();
                }
            }

            if (oldState == OnScrollListener.SCROLL_STATE_FLING) {
                if (mNeedRequeryCursor) {
                    requeryCursor();
                } else {
                    notifyDataSetChanged();
                }
            }
        }

        boolean isScrolling() {
            return mScrollState == OnScrollListener.SCROLL_STATE_FLING;
        }

        void setNeedRequeryCursor(boolean requeryCursor) {
            mNeedRequeryCursor = requeryCursor;
        }
    }
}
