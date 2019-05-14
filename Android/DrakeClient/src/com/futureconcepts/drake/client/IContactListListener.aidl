package com.futureconcepts.drake.client;

import com.futureconcepts.drake.client.IContactList;
import com.futureconcepts.drake.client.Contact;
import com.futureconcepts.drake.client.ImErrorInfo;

oneway interface IContactListListener
{
    /**
     * Called when:
     *  <ul>
     *  <li> a contact list has been created, deleted, renamed or loaded, or
     *  <li> a contact has been added to or removed from a list, or
     *  <li> a contact has been blocked or unblocked
     *  </ul>
     *
     * @see info.guardianproject.otr.app.im.engine.ContactListListener#onContactChange(int, ContactList, Contact)
     */
    void onContactChange(int type, IContactList list, in Contact contact);

    /**
     * Called when all the contact lists have been loaded from server.
     *
     * @see info.guardianproject.otr.app.im.engine.ContactListListener#onAllContactListsLoaded()
     */
    void onAllContactListsLoaded();

    /**
     * Called when one or more contacts' presence information has updated.
     *
     * @see info.guardianproject.otr.app.im.engine.ContactListListener#onContactsPresenceUpdate(Contact[])
     */
    void onContactsPresenceUpdate(in Contact[] contacts);

    /**
     * Called when a previous contact related request has failed.
     *
     * @see info.guardianproject.otr.app.im.engine.ContactListListener#onContactError(int, ImErrorInfo, String, Contact)
     */
    void onContactError(int errorType, in ImErrorInfo error, String listName, in Contact contact);
}
