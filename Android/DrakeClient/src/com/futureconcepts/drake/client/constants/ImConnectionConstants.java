package com.futureconcepts.drake.client.constants;

public abstract class ImConnectionConstants
{
    /**
     * Connection state that indicates the connection is not connected yet.
     */
    public static final int DISCONNECTED = 0;

    /**
     * Connection state that indicates the user is logging into the server.
     */
    public static final int LOGGING_IN = 1;

    /**
     * Connection state that indicates the user has logged into the server.
     */
    public static final int LOGGED_IN = 2;

    /**
     * Connection state that indicates the user is logging out the server.
     */
    public static final int LOGGING_OUT = 3;

    /**
     * Connection state that indicate the connection is suspending.
     */
    public static final int SUSPENDING = 4;

    /**
     * Connection state that indicate the connection has been suspended.
     */
    public static final int SUSPENDED = 5;

    /**
     * The capability of supporting group chat.
     */
    public static final int CAPABILITY_GROUP_CHAT = 1;
    /**
     * The capability of supporting session re-establishment.
     */
    public static final int CAPABILITY_SESSION_REESTABLISHMENT = 2;
}
