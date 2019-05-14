package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class JournalEntryParticipant extends BaseTable
{
    public static final String JOURNAL_ENTRY = "JournalEntry";
    public static final String PARTICIPANT = "Participant";

	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/JournalEntryMedia");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.JournalEntryMedia";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.JournalEntryMedia";

	public JournalEntryParticipant(Context context, Cursor cursor)
	{
		super(context, cursor);
	}

	@Override
	public Uri getContentUri()
	{
		return CONTENT_URI;
	}
	
	public String getJournalEntryID()
	{
		return getString(getColumnIndex(JOURNAL_ENTRY));
	}
	
	public String getParticipantID()
	{
		return getString(getColumnIndex(PARTICIPANT));
	}
}
