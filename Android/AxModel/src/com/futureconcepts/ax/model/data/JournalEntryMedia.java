package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class JournalEntryMedia extends BaseTable
{	
    public static final String JOURNAL_ENTRY = "JournalEntry";
    public static final String MEDIA = "Media";
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

    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = "modified DESC";

	public JournalEntryMedia(Context context, Cursor cursor)
	{
		super(context, cursor);
	}

	@Override
	public Uri getContentUri()
	{
		return CONTENT_URI;
	}
	
	@Override
	public void beginEdit()
	{
		super.beginEdit();
		if (getCount() != 0)
		{
			setMedia(getCursorGuid(MEDIA));
			setJournalEntry(getCursorGuid(JOURNAL_ENTRY));
		}
	}
	
	public String getJournalEntryID()
	{
		return getModelString(JOURNAL_ENTRY);
	}
	
	public void setJournalEntry(String journalEntry)
	{
		setModel(JOURNAL_ENTRY, journalEntry);
	}
	
	public String getMediaID()
	{
		return getModelString(MEDIA);
	}
	public void setMedia(String media)
	{
		setModel(MEDIA, media);
	}
	
	public JournalEntryMedia queryJournalEntryMediaAll(Context context)
	{
		JournalEntryMedia result = null;
		try
		{
			result = new JournalEntryMedia(context, context.getContentResolver().query(CONTENT_URI, null,null, null,null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	
	public static  JournalEntryMedia queryJournalEntryMedia(Context context, String id,String filed)
	{
		JournalEntryMedia result = null;
		try
		{
			result = new JournalEntryMedia(context, context.getContentResolver().query(CONTENT_URI, null, filed+"='"+id+"'", null,null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public static int queryTotalMediaOfJournal(Context context, String id)
	{
		int images=0;		
		JournalEntry _journalEntry = JournalEntry.queryJournal(context,id);
		if(_journalEntry.getCount()>0)
		{
			JournalEntryMedia _journalEntryMedia = null;
			_journalEntry.moveToFirst();
			do
			{
				_journalEntryMedia = queryJournalEntryMedia(context,_journalEntry.getID(),JournalEntryMedia.JOURNAL_ENTRY);
				images+= _journalEntryMedia.getCount();
			}while(_journalEntry.moveToNext());	
			if (_journalEntryMedia != null)
			{
				_journalEntryMedia.close();
				_journalEntryMedia = null;
			}			
		}
		if (_journalEntry != null)
			{
				_journalEntry.close();
				_journalEntry = null;
			}			
		return images;
	}
	
	public static Uri insertMediaRelation(Context context, String MediaID, String journalEntryID, String action)
	{
		Uri result = null;
		JournalEntryMedia _jem = new JournalEntryMedia(context,null);
		_jem.beginEdit();
	    _jem.setID(Guid.newGuid().toString());
		_jem.setMedia(MediaID);
		_jem.setJournalEntry(journalEntryID);
		result = context.getContentResolver().insert(CONTENT_URI, _jem.endEditAndUpload(action));
		return result;
	}
	
}
