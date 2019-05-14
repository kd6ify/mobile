package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class MediaAddress extends BaseTable {

	    public static final String ADDRESS = "Address";
	    public static final String MEDIA = "Media";
		/**
	     * The content:// style URL for this table
	     */
	    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/MediaAddress");

	    /**
	     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
	     */
	    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.MediaAddress";
	    /**
	     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
	     */
	    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.MediaAddress";

	    /**
	     * The default sort order for this table
	     */
	    public static final String DEFAULT_SORT_ORDER = "modified DESC";

		public MediaAddress(Context context, Cursor cursor)
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
				setAddress(getCursorGuid(ADDRESS));
			}
		}
		
		public String getAddressID()
		{
			return getModelString(ADDRESS);
		}
		
		public void setAddress(String Address)
		{
			setModel(ADDRESS, Address);
		}
		
		public String getMediaID()
		{
			return getModelString(MEDIA);
		}
		public void setMedia(String media)
		{
			setModel(MEDIA, media);
		}
		
		public static Uri insertMediaAddressRelation(Context context, String MediaID, String AddresID, String action)
		{
			Uri result = null;
			MediaAddress _mediaAddress = new MediaAddress(context,null);
			_mediaAddress.beginEdit();
			_mediaAddress.setID(Guid.newGuid().toString());
			_mediaAddress.setMedia(MediaID);
			_mediaAddress.setAddress(AddresID);
			result = context.getContentResolver().insert(CONTENT_URI, _mediaAddress.endEditAndUpload(action));
			return result;
		}
		
	}
