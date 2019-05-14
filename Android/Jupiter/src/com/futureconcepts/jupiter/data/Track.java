package com.futureconcepts.jupiter.data;

import android.database.Cursor;
import android.net.Uri;

public final class Track extends Placemark
{
    public static final String AUTHORITY = "com.futureconcepts.jupiter.provider.track";

    public static final String SEQUENCE = "Sequence";
        
    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/track");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.jupiter.Track";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.jupiter.Track";
    
    public Track(Cursor cursor)
    {
    	super(cursor);
    }
    
    public int getSequence()
    {
    	return getInt(getColumnIndex(SEQUENCE));
    }
}
