package info.guardianproject.otr.app.im.engine;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import com.futureconcepts.drake.client.Contact;
import com.futureconcepts.drake.client.ImEntity;
import com.futureconcepts.drake.client.ImErrorInfo;

public abstract class ChatGroup extends ImEntity
{
    private ChatGroupManager mManager;
    private String mAddress;
    private String mName;
    private String mPassword;
    private Vector<Contact> mMembers;
    private CopyOnWriteArrayList<GroupMemberListener> mMemberListeners;

    public ChatGroup(String address, String name, ChatGroupManager manager)
    {
        this(address, name, null, manager);
    }

    public ChatGroup(String address, String name, Collection<Contact> members, ChatGroupManager manager)
    {
        mAddress = address;
        mName = name;
        mManager = manager;
        mMembers = new Vector<Contact>();

        if(members != null)
        {
            mMembers.addAll(members);
        }
        mMemberListeners = new CopyOnWriteArrayList<GroupMemberListener>();
    }

    @Override
    public String getAddress()
    {
        return mAddress;
    }

    /**
     * Gets the name of the group.
     *
     * @return the name of the group.
     */
    public String getName()
    {
        return mName;
    }

    public void setPassword(String value)
    {
    	mPassword = value;
    }
    
    public String getPassword()
    {
    	return mPassword;
    }
    
    public void addMemberListener(GroupMemberListener listener)
    {
        mMemberListeners.add(listener);
    }

    public void removeMemberListener(GroupMemberListener listener)
    {
        mMemberListeners.remove(listener);
    }

    /**
     * Gets an unmodifiable collection of the members of the group.
     *
     * @return an unmodifiable collection of the members of the group.
     */
    public List<Contact> getMembers()
    {
        return Collections.unmodifiableList(mMembers);
    }

    /**
     * Notifies that a contact has joined into this group.
     *
     * @param contact the contact who has joined into the group.
     */
    void notifyMemberJoined(Contact contact)
    {
        mMembers.add(contact);
        for(GroupMemberListener listener : mMemberListeners)
        {
            listener.onMemberJoined(this, contact);
        }
    }

    /**
     * Notifies that a contact has left this group.
     *
     * @param contact the contact who has left this group.
     */
    void notifyMemberLeft(Contact contact)
    {
        if(mMembers.remove(contact))
        {
            for(GroupMemberListener listener : mMemberListeners)
            {
                listener.onMemberLeft(this, contact);
            }
        }
    }

    /**
     * Notifies that previous operation on this group has failed.
     *
     * @param error the error information.
     */
    void notifyGroupMemberError(ImErrorInfo error)
    {
        for(GroupMemberListener listener : mMemberListeners)
        {
            listener.onError(this, error);
        }
    }

    public abstract void joinAsync();
    public abstract void leaveAsync();
    public abstract boolean isJoined();
    public abstract void inviteUserAsync(Contact invitee);
}
