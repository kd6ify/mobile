package com.futureconcepts.drake.ui.widget;

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

public class InvitationView extends LinearLayout
{
	private static final String LOG_TAG = InvitationView.class.getSimpleName();
    private SimpleAlertHandler mHandler;
    private String mRoomJid;
    private TextView  mTitle;
    private TextView mTextNote;
    private TextView mInvitationText;
    
    private long mInvitationId;

    private Listener mListener;
    
	public SimpleAlertHandler getHandler ()
	{
		return mHandler;
	}
	
    static final void log(String msg)
    {
        Log.d(LOG_TAG, "<InvitationView> " +msg);
    }

    public InvitationView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mHandler = new SimpleAlertHandler((Activity)getContext());
    }

    @Override
    protected void onFinishInflate()
    {
        mTitle          = (TextView) findViewById(R.id.title);
        mTextNote = (TextView)findViewById(R.id.textNote);
        mInvitationText = (TextView)findViewById(R.id.txtInvitation);

        Button acceptInvitation = (Button)findViewById(R.id.btnAccept);
        Button declineInvitation= (Button)findViewById(R.id.btnDecline);

        acceptInvitation.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
            	if (mListener != null)
            	{
            		mListener.onInvitationAccepted(mInvitationId, mRoomJid);
            	}
            }
        });
        declineInvitation.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
            	if (mListener != null)
            	{
            		mListener.onInvitationDeclined(mInvitationId, mRoomJid);
            	}
            }
        });
    }

    public void setListener(Listener listener)
    {
    	mListener = listener;
    }
    
    public void bindInvitation(long invitationId)
    {
    	InvitationCursor invitation = InvitationCursor.query(getContext(), invitationId);
        if (invitation == null || !invitation.moveToFirst())
        {
            if (Log.isLoggable(LOG_TAG, Log.DEBUG))
            {
                log("Failed to query invitation: " + invitationId);
            }
        }
        else
        {
        	mInvitationId = invitationId;
            findViewById(R.id.btnAccept).requestFocus();
            mRoomJid = invitation.getGroupName();
            String sender = StringUtils.parseBareAddress(invitation.getSender());
            mInvitationText.setText(getContext().getString(R.string.invitation_prompt, sender));
            mTitle.setText(getContext().getString(R.string.chat_with, mRoomJid));
            String note = invitation.getNote();
            if (note != null)
            {
            	mTextNote.setText(note);
            }
        }
        if (invitation != null)
        {
            invitation.close();
        }
    }

    public String getRoomJid()
    {
        return mRoomJid;
    }

    public interface Listener
    {
    	void onInvitationAccepted(long id, String roomJid);
    	void onInvitationDeclined(long id, String roomJid);
    }
}
