package info.guardianproject.otr.app.im.service;

import com.futureconcepts.drake.client.Contact;
import com.futureconcepts.drake.client.IChatGroup;

import info.guardianproject.otr.app.im.engine.ChatGroup;

import android.os.RemoteException;
import android.util.Log;

public class ChatGroupServiceImpl extends IChatGroup.Stub
{
    private long mDataBaseId;
    private ChatGroup mAdaptee;

    public ChatGroupServiceImpl(ChatGroup adaptee, long dataBaseId)
    {
        mAdaptee = adaptee;
        mDataBaseId = dataBaseId;
    }

	@Override
	public long getId() throws RemoteException
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
    public long getDataBaseId()
    {
        return mDataBaseId;
    }

    public String getAddress()
    {
        return mAdaptee.getAddress();
    }

    @Override
    public String getName()
    {
        return mAdaptee.getName();
    }

	@Override
	public void addGroupMemberAsync(Contact contact) throws RemoteException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeGroupMemberAsync(Contact contact) throws RemoteException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPassword(String password)
	{
		mAdaptee.setPassword(password);
	}
	
	@Override
	public void joinAsync() throws RemoteException
	{
		mAdaptee.joinAsync();
	}

	@Override
	public void leaveAsync() throws RemoteException
	{
		mAdaptee.leaveAsync();
	}

	@Override
	public boolean isJoined() throws RemoteException
	{
		return mAdaptee.isJoined();
	}

	@Override
	public void inviteUserAsync(Contact invitee) throws RemoteException
	{
		mAdaptee.inviteUserAsync(invitee);
	}
}
