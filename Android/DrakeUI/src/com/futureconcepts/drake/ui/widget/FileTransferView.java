package com.futureconcepts.drake.ui.widget;

import com.futureconcepts.drake.client.model.FileTransferCursor;
import com.futureconcepts.drake.client.model.InvitationCursor;
import com.futureconcepts.drake.ui.R;
import com.futureconcepts.drake.ui.os.SimpleAlertHandler;
import com.futureconcepts.drake.ui.utils.StringUtils;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FileTransferView extends LinearLayout
{
	private static final String LOG_TAG = FileTransferView.class.getSimpleName();
    private SimpleAlertHandler _handler;
    private String mRoomJid;
    private TextView  mTitle;
    private TextView mTextNote;
    private TextView mInvitationText;
    
    private long _fileTransferId;
    
    private Listener _listener;
    
	public SimpleAlertHandler getHandler ()
	{
		return _handler;
	}
	
    static final void log(String msg)
    {
        Log.d(LOG_TAG, "<FileTransferView> " +msg);
    }

    public FileTransferView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        _handler = new SimpleAlertHandler((Activity)getContext());
    }

    @Override
    protected void onFinishInflate()
    {
        mTitle          = (TextView) findViewById(R.id.title);
        mTextNote = (TextView)findViewById(R.id.textNote);
        mInvitationText = (TextView)findViewById(R.id.txtInvitation);

        Button acceptButton = (Button)findViewById(R.id.btnAccept);
        Button rejectButton = (Button)findViewById(R.id.btnReject);

        acceptButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
            	if (_listener != null)
            	{
            		_listener.onFileTransferAccepted(_fileTransferId);
            	}
            }
        });
        rejectButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
            	if (_listener != null)
            	{
            		_listener.onFileTransferAccepted(_fileTransferId);
            	}
            }
        });
    }

    public void setListener(Listener listener)
    {
    	_listener = listener;
    }
    
    public void bindFileTransfer(long fileTransferId)
    {
    	FileTransferCursor cursor = FileTransferCursor.query(getContext(), fileTransferId);
        if (cursor == null || !cursor.moveToFirst())
        {
            if (Log.isLoggable(LOG_TAG, Log.DEBUG))
            {
                log("Failed to query file transfer: " + fileTransferId);
            }
        }
        else
        {
        	_fileTransferId = fileTransferId;
            findViewById(R.id.btnAccept).requestFocus();
//            mRoomJid = invitation.getGroupName();
//            String sender = StringUtils.parseBareAddress(invitation.getSender());
    //        mInvitationText.setText(getContext().getString(R.string.invitation_prompt, sender));
            mTitle.setText(getContext().getString(R.string.chat_with, mRoomJid));
  //          String note = invitation.getNote();
      //      if (note != null)
            {
        //    	mTextNote.setText(note);
            }
        }
        if (cursor != null)
        {
            cursor.close();
        }
    }

    public interface Listener
    {
    	void onFileTransferAccepted(long id);
    	void onFileTransferRejected(long id);
    }
}
