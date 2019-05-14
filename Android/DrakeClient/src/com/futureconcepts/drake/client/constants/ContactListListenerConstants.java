package com.futureconcepts.drake.client.constants;

/**
 * Interfaces that allows the implementing classes to listen to contact list
 * relative events. Listeners are registered with ContactListManager.
 */
public interface ContactListListenerConstants
{
    public static final int LIST_CREATED        = 1;
    public static final int LIST_DELETED        = 2;
    public static final int LIST_LOADED         = 3;
    public static final int LIST_RENAMED        = 4;
    public static final int LIST_CONTACT_ADDED  = 5;
    public static final int LIST_CONTACT_REMOVED = 6;
    public static final int CONTACT_BLOCKED     = 7;
    public static final int CONTACT_UNBLOCKED   = 8;

    public static final int ERROR_CREATING_LIST = -1;
    public static final int ERROR_DELETING_LIST = -2;
    public static final int ERROR_RENAMING_LIST = -3;

    public static final int ERROR_LOADING_LIST          = -4;
    public static final int ERROR_LOADING_BLOCK_LIST    = -5;

    public static final int ERROR_RETRIEVING_PRESENCE   = -6;

    public static final int ERROR_ADDING_CONTACT    = -7;
    public static final int ERROR_REMOVING_CONTACT  = -8;
    public static final int ERROR_BLOCKING_CONTACT  = -9;
    public static final int ERROR_UNBLOCKING_CONTACT = -10;
}
