package com.futureconcepts.drake.client.constants;

public interface ContactListManagerConstants
{
    /**
     * ContactListManager state that indicates the contact list(s) has not been loaded.
     */
    public static final int LISTS_NOT_LOADED = 0;

    /**
     * ContactListManager state that indicates the contact list(s) is loading.
     */
    public static final int LISTS_LOADING = 1;

    /**
     * ContactListManager state that indicates the blocked list has been loaded.
     */
    public static final int BLOCKED_LIST_LOADED = 2;

    /**
     * ContactListManager state that indicates the contact list(s) has been loaded.
     */
    public static final int LISTS_LOADED = 3;
}
