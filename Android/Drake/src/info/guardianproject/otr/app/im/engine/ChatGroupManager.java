package info.guardianproject.otr.app.im.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.futureconcepts.drake.client.Contact;
import com.futureconcepts.drake.client.ImErrorInfo;
import com.futureconcepts.drake.client.Invitation;

/**
 * ChatGroupManager manages the creating, removing and the member of ChatGroups.
 */
public abstract class ChatGroupManager
{
    protected ConcurrentHashMap<String, ChatGroup> mGroups;

    protected CopyOnWriteArrayList<GroupListener> mGroupListeners;

    protected InvitationListener mInvitationListener;

    protected ChatGroupManager()
    {
        mGroups = new ConcurrentHashMap<String,ChatGroup>();
        mGroupListeners = new CopyOnWriteArrayList<GroupListener>();
    }

    /**
     * Adds a GroupListener to this manager so that it will be notified when a
     * certain group changes.
     *
     * @param listener the listener to be notified.
     */
    public void addGroupListener(GroupListener listener)
    {
        mGroupListeners.add(listener);
    }

    /**
     * Removes a GroupListener from this manager so that it won't be notified
     * any more.
     *
     * @param listener the listener to remove.
     */
    public void removeGroupListener(GroupListener listener)
    {
        mGroupListeners.remove(listener);
    }

    /**
     * Sets the InvitationListener to the manager so that it will be notified
     * when an invitation from another users received.
     *
     * @param listener the InvitationListener.
     */
    public synchronized void setInvitationListener(InvitationListener listener)
    {
        mInvitationListener = listener;
    }

    /**
     * Creates a new ChatGroup with specified name. This method returns
     * immediately and the registered GroupListeners will be notified when the
     * group is created or any error occurs. The newly created group is a
     * temporary group and will be automatically deleted when all joined users
     * have left.
     *
     * @param name the name of the ChatGroup to be created.
     */
    public abstract void createChatGroupAsync(String address);

    /**
     * Deletes a certain ChatGroup. This method returns immediately and the
     * registered GroupListeners will be notified when the group is deleted or
     * any error occurs. Only the administrator of the ChatGroup can delete it.
     *
     * @param group the ChatGroup to be deleted.
     */
    public abstract void deleteChatGroupAsync(String address);

    /**
     * Accepts an invitation. The user will join the group automatically after
     * accept the invitation.
     *
     * @param id the _ID in the invitations table of the invitation to accept.
     */
    public abstract void acceptInvitationAsync(long id);

    /**
     * Rejects an invitation.
     *
     * @param id the id of the invitation to reject.
     */
    public abstract void rejectInvitationAsync(long id);

    /**
     * Gets a ChatGroup by address.
     *
     * @param address the address of the ChatGroup.
     * @return a ChatGroup.
     */
    public ChatGroup getChatGroup(String address)
    {
        return mGroups.get(address);
    }

    /**
     * Notifies the GroupListeners that a ChatGroup has changed.
     *
     * @param groupAddress the address of group which has changed.
     * @param joined a list of users that have joined the group.
     * @param left a list of users that have left the group.
     */
    public void notifyGroupChanged(String groupAddress, ArrayList<Contact> joined, ArrayList<Contact> left)
    {
        ChatGroup group = mGroups.get(groupAddress);
        if (group != null)
        {
	        if (joined != null)
	        {
	            for (Contact contact : joined)
	            {
	                group.notifyMemberJoined(contact);
	            }
	        }
	        if (left != null)
	        {
	            for (Contact contact : left)
	            {
	                group.notifyMemberLeft(contact);
	            }
	        }
        }
    }

    public synchronized void notifyGroupCreated(ChatGroup group)
    {
        mGroups.put(group.getAddress(), group);
        for (GroupListener listener : mGroupListeners)
        {
            listener.onGroupCreated(group);
        }
    }

    public synchronized void notifyGroupDeleted(ChatGroup group)
    {
        mGroups.remove(group.getAddress());
        for (GroupListener listener : mGroupListeners)
        {
            listener.onGroupDeleted(group);
        }
    }

    public synchronized void notifyJoinedGroup(ChatGroup group)
    {
        mGroups.put(group.getAddress(), group);
        for (GroupListener listener : mGroupListeners)
        {
            listener.onJoinedGroup(group);
        }
    }

    /**
     * Notifies the GroupListeners that the user has left a certain group.
     *
     * @param groupAddress the address of the group.
     */
    public synchronized void notifyLeftGroup(ChatGroup group)
    {
        mGroups.remove(group.getAddress());
        for (GroupListener listener : mGroupListeners)
        {
            listener.onLeftGroup(group);
        }
    }

    public synchronized void notifyGroupError(int errorType, String groupName, ImErrorInfo error)
    {
        for (GroupListener listener : mGroupListeners)
        {
            listener.onGroupError(errorType, groupName, error);
        }
    }

    /**
     * Notifies the InvitationListener that another user invited the current
     * logged user to join a group chat.
     */
    public synchronized void notifyGroupInvitation(Invitation invitation)
    {
        if (mInvitationListener != null)
        {
            mInvitationListener.onGroupInvitation(invitation);
        }
    }
}
