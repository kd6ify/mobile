package com.futureconcepts.ax.trinity;

import android.database.ContentObserver;
import android.net.Uri;
import android.widget.BaseAdapter;

public interface IContentObserverRegistrar
{
    void registerContentObserver(BaseAdapter adapter);
    void registerContentObserver(Uri uri, boolean descendants, ContentObserver observer);
    void unregisterContentObserver(ContentObserver observer);
}
