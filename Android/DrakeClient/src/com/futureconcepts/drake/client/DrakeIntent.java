package com.futureconcepts.drake.client;

public interface DrakeIntent
{
	public static final String ACTION_START_SERVICE = "com.futureconcepts.drake.action.START_SERVICE";
	
	public static final String CATEGORY_IMPS = "info.guardianproject.otr.app.im.IMPS_CATEGORY";
	
    public static final String EVENT_SERVICE_CONNECTED = "com.futureconcepts.drake.event.EVENT_SERVICE_CONNECTED";
    public static final String EVENT_CONNECTION_CREATED = "com.futureconcepts.drake.event.EVENT_CONNECTION_CREATED";
    public static final String EVENT_CONNECTION_LOGGING_IN = "com.futureconcepts.drake.event.EVENT_CONNECTION_LOGGIN_IN";
    public static final String EVENT_CONNECTION_LOGGED_IN = "com.futureconcepts.drake.event.EVENT_CONNECTION_LOGGED_IN";
    public static final String EVENT_CONNECTION_LOGGING_OUT = "com.futureconcepts.drake.event.EVENT_LOGGING_OUT";
    public static final String EVENT_CONNECTION_DISCONNECTED = "com.futureconcepts.drake.event.EVENT_CONNECTION_DISCONNECTED";
    public static final String EVENT_CONNECTION_SUSPENDING = "com.futureconcepts.drake.event.EVENT_CONNECTION_SUSPENDING";
    public static final String EVENT_CONNECTION_SUSPENDED = "com.futureconcepts.drake.event.EVENT_CONNECTION_SUSPENDED";
    public static final String EVENT_USER_PRESENCE_UPDATED = "com.futureconcepts.drake.event.EVENT_USER_PRESENCE_UPDATED";
    public static final String EVENT_UPDATE_USER_PRESENCE_ERROR = "com.futureconcepts.drake.event.EVENT_UPDATE_USER_PRESENCE_ERROR";
}
