/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.futureconcepts.mercury.download;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * The Download Manager
 *
 * @hide
 */
public final class Downloads
{
    /**
     * Download status codes
     */

    /**
     * This download hasn't started yet
     */
    public static final int STATUS_PENDING = 190;

    /**
     * This download has started
     */
    public static final int STATUS_RUNNING = 192;

    /**
     * This download has successfully completed.
     * Warning: there might be other status values that indicate success
     * in the future.
     * Use isSucccess() to capture the entire category.
     */
    public static final int STATUS_SUCCESS = 200;

    /**
     * This download can't be performed because the content type cannot be
     * handled.
     */
    public static final int STATUS_NOT_ACCEPTABLE = 406;

    /**
     * This download has completed with an error.
     * Warning: there will be other status values that indicate errors in
     * the future. Use isStatusError() to capture the entire category.
     */
    public static final int STATUS_UNKNOWN_ERROR = 491;

    /**
     * This download couldn't be completed because of an HTTP
     * redirect response that the download manager couldn't
     * handle.
     */
    public static final int STATUS_UNHANDLED_REDIRECT = 493;

    /**
     * This download couldn't be completed due to insufficient storage
     * space.  Typically, this is because the SD card is full.
     */
    public static final int STATUS_INSUFFICIENT_SPACE_ERROR = 498;

    /**
     * This download couldn't be completed because no external storage
     * device was found.  Typically, this is because the SD card is not
     * mounted.
     */
    public static final int STATUS_DEVICE_NOT_FOUND_ERROR = 499;

    /**
     * Returns whether the status is a success (i.e. 2xx).
     */
    public static boolean isStatusSuccess(int status) {
        return (status >= 200 && status < 300);
    }

    /**
     * Returns whether the status is an error (i.e. 4xx or 5xx).
     */
    public static boolean isStatusError(int status) {
        return (status >= 400 && status < 600);
    }

    /**
     * Download destinations
     */

    /**
     * This download will be saved to the external storage. This is the
     * default behavior, and should be used for any file that the user
     * can freely access, copy, delete. Even with that destination,
     * unencrypted DRM files are saved in secure internal storage.
     * Downloads to the external destination only write files for which
     * there is a registered handler. The resulting files are accessible
     * by filename to all applications.
     */
    public static final int DOWNLOAD_DESTINATION_EXTERNAL = 1;

    /**
     * This download will be saved to the download manager's private
     * partition. This is the behavior used by applications that want to
     * download private files that are used and deleted soon after they
     * get downloaded. All file types are allowed, and only the initiating
     * application can access the file (indirectly through a content
     * provider). This requires the
     * android.permission.ACCESS_DOWNLOAD_MANAGER_ADVANCED permission.
     */
    public static final int DOWNLOAD_DESTINATION_CACHE = 2;

    /**
     * This download will be saved to the download manager's private
     * partition and will be purged as necessary to make space. This is
     * for private files (similar to CACHE_PARTITION) that aren't deleted
     * immediately after they are used, and are kept around by the download
     * manager as long as space is available.
     */
    public static final int DOWNLOAD_DESTINATION_CACHE_PURGEABLE = 3;


    /**
     * An invalid download id
     */
    public static final long DOWNLOAD_ID_INVALID = -1;


    /**
     * Broadcast Action: this is sent by the download manager to the app
     * that had initiated a download when that download completes. The
     * download's content: uri is specified in the intent's data.
     */
    public static final String ACTION_DOWNLOAD_COMPLETED = "com.futureconcepts.mercury.intent.action.DOWNLOAD_COMPLETED";

    /**
     * If extras are specified when requesting a download they will be provided in the intent that
     * is sent to the specified class and package when a download has finished.
     * <P>Type: TEXT</P>
     * <P>Owner can Init</P>
     */
    public static final String COLUMN_NOTIFICATION_EXTRAS = "notificationextras";

    /**
     * Status class for a download
     */
    public static final class StatusInfo
    {
        public boolean completed = false;
        /** The filename of the active download. */
        public String filename = null;
        /** An opaque id for the download */
        public long id = DOWNLOAD_ID_INVALID;
        /** An opaque status code for the download */
        public int statusCode = -1;
        /** Approximate number of bytes downloaded so far, for debugging purposes. */
        public long bytesSoFar = -1;

        /**
         * Returns whether the download is completed
         * @return a boolean whether the download is complete.
         */
        public boolean isComplete()
        {
            return com.futureconcepts.mercury.download.Downloads.Impl.isStatusCompleted(statusCode);
        }

        /**
         * Returns whether the download is successful
         * @return a boolean whether the download is successful.
         */
        public boolean isSuccessful()
        {
            return Impl.isStatusCompleted(statusCode);
        }
    }

    /**
     * Class to access initiate and query download by server uri
     */
    public static final class ByUri extends DownloadBase
    {
        /** @hide */
        private ByUri() {}

        /**
         * Query where clause by app data.
         * @hide
         */
        private static final String QUERY_WHERE_APP_DATA_CLAUSE = Impl.APP_DATA + "=?";

        /**
         * Gets a Cursor pointing to the download(s) of the current system update.
         * @hide
         */
        private static final Cursor getCurrentOtaDownloads(Context context, String url)
        {
            return context.getContentResolver().query(
                    Impl.CONTENT_URI,
                    DOWNLOADS_PROJECTION,
                    QUERY_WHERE_APP_DATA_CLAUSE,
                    new String[] {url},
                    null);
        }

        /**
         * Returns a StatusInfo with the result of trying to download the
         * given URL.  Returns null if no attempts have been made.
         */
        public static final StatusInfo getStatus(Context context, String url, long redownload_threshold)
        {
            StatusInfo result = null;
            boolean hasFailedDownload = false;
            long failedDownloadModificationTime = 0;
            Cursor c = getCurrentOtaDownloads(context, url);
            try
            {
                while (c != null && c.moveToNext())
                {
                    if (result == null)
                    {
                        result = new StatusInfo();
                    }
                    int status = getStatusOfDownload(c, redownload_threshold);
                    if (status == STATUS_DOWNLOADING_UPDATE || status == STATUS_DOWNLOADED_UPDATE)
                    {
                        result.completed = (status == STATUS_DOWNLOADED_UPDATE);
                        result.filename = c.getString(DOWNLOADS_COLUMN_FILENAME);
                        result.id = c.getLong(DOWNLOADS_COLUMN_ID);
                        result.statusCode = c.getInt(DOWNLOADS_COLUMN_STATUS);
                        result.bytesSoFar = c.getLong(DOWNLOADS_COLUMN_CURRENT_BYTES);
                        return result;
                    }
                    long modTime = c.getLong(DOWNLOADS_COLUMN_LAST_MODIFICATION);
                    if (hasFailedDownload && modTime < failedDownloadModificationTime)
                    {
                        // older than the one already in result; skip it.
                        continue;
                    }

                    hasFailedDownload = true;
                    failedDownloadModificationTime = modTime;
                    result.statusCode = c.getInt(DOWNLOADS_COLUMN_STATUS);
                    result.bytesSoFar = c.getLong(DOWNLOADS_COLUMN_CURRENT_BYTES);
                }
            }
            finally
            {
                if (c != null)
                {
                    c.close();
                }
            }
            return result;
        }

        /**
         * Query where clause for general querying.
         */
        private static final String QUERY_WHERE_CLAUSE = Impl.NOTIFICATION_PACKAGE + "=? AND " + Impl.NOTIFICATION_CLASS + "=?";

        /**
         * Delete all the downloads for a package/class pair.
         */
        public static final void removeAllDownloadsByPackage(Context context, String notification_package, String notification_class)
        {
            context.getContentResolver().delete(
                    Impl.CONTENT_URI,
                    QUERY_WHERE_CLAUSE,
                    new String[] { notification_package, notification_class });
        }

        /**
         * The column for the id in the Cursor returned by
         * getProgressCursor()
         */
        public static final int getProgressColumnId()
        {
            return 0;
        }

        /**
         * The column for the current byte count in the Cursor returned by
         * getProgressCursor()
         */
        public static final int getProgressColumnCurrentBytes()
        {
            return 1;
        }

        /**
         * The column for the total byte count in the Cursor returned by
         * getProgressCursor()
         */
        public static final int getProgressColumnTotalBytes()
        {
            return 2;
        }

        /** @hide */
        private static final String[] PROJECTION = {
            BaseColumns._ID,
            Impl.CURRENT_BYTES,
            Impl.TOTAL_BYTES
        };

        /**
         * Returns a Cursor representing the progress of the download identified by the ID.
         */
        public static final Cursor getProgressCursor(Context context, long id)
        {
            Uri downloadUri = Uri.withAppendedPath(Impl.CONTENT_URI, String.valueOf(id));
            return context.getContentResolver().query(downloadUri, PROJECTION, null, null, null);
        }
    }

    /**
     * Class to access downloads by opaque download id
     */
    public static final class ById extends DownloadBase
    {
        /** @hide */
        private ById() {}

        /**
         * Get the mime tupe of the download specified by the download id
         */
        public static String getMimeTypeForId(Context context, long downloadId)
        {
            ContentResolver cr = context.getContentResolver();

            String mimeType = null;
            Cursor downloadCursor = null;

            try
            {
                Uri downloadUri = getDownloadUri(downloadId);

                downloadCursor = cr.query(downloadUri, new String[]{Impl.MIME_TYPE}, null, null, null);
                if (downloadCursor.moveToNext())
                {
                    mimeType = downloadCursor.getString(0);
                }
            }
            finally
            {
                if (downloadCursor != null)
            	{
                	downloadCursor.close();
            	}
            }
            return mimeType;
        }

        /**
         * Delete a download by Id
         */
        public static void deleteDownload(Context context, long downloadId)
        {
            ContentResolver cr = context.getContentResolver();

            Uri downloadUri = getDownloadUri(downloadId);

            cr.delete(downloadUri, null, null);
        }

        /**
         * Open a filedescriptor to a particular download
         */
        public static ParcelFileDescriptor openDownload(Context context, long downloadId, String mode) throws FileNotFoundException
        {
            ContentResolver cr = context.getContentResolver();

            Uri downloadUri = getDownloadUri(downloadId);

            return cr.openFileDescriptor(downloadUri, mode);
        }

        /**
         * Open a stream to a particular download
         */
        public static InputStream openDownloadStream(Context context, long downloadId) throws FileNotFoundException, IOException
        {
            ContentResolver cr = context.getContentResolver();

            Uri downloadUri = getDownloadUri(downloadId);

            return cr.openInputStream(downloadUri);
        }

        private static Uri getDownloadUri(long downloadId)
        {
            return Uri.parse(Impl.CONTENT_URI + "/" + downloadId);
        }

        /**
         * Returns a StatusInfo with the result of trying to download the
         * given URL.  Returns null if no attempts have been made.
         */
        public static final StatusInfo getStatus(Context context, long downloadId)
        {
            StatusInfo result = null;
            Uri downloadUri = getDownloadUri(downloadId);

            ContentResolver cr = context.getContentResolver();

            Cursor c = cr.query(downloadUri, DOWNLOADS_PROJECTION, null /* selection */, null /* selection args */, null /* sort order */);
            try
            {
                if (!c.moveToNext())
                {
                    return result;
                }
                if (result == null)
                {
                    result = new StatusInfo();
                }
                int status = getStatusOfDownload(c,0);
                if (status == STATUS_DOWNLOADING_UPDATE || status == STATUS_DOWNLOADED_UPDATE)
                {
                    result.completed = (status == STATUS_DOWNLOADED_UPDATE);
                    result.filename = c.getString(DOWNLOADS_COLUMN_FILENAME);
                    result.id = c.getLong(DOWNLOADS_COLUMN_ID);
                    result.statusCode = c.getInt(DOWNLOADS_COLUMN_STATUS);
                    result.bytesSoFar = c.getLong(DOWNLOADS_COLUMN_CURRENT_BYTES);
                    return result;
                }

                result.statusCode = c.getInt(DOWNLOADS_COLUMN_STATUS);
                result.bytesSoFar = c.getLong(DOWNLOADS_COLUMN_CURRENT_BYTES);
            }
            finally
            {
                if (c != null)
                {
                    c.close();
                }
            }
            return result;
        }
    }

    /**
     * Base class with common functionality for the various download classes
     */
    public static class DownloadBase
    {
        /** @hide */
        DownloadBase() {}

        /**
          * Initiate a download where the download will be tracked by its URI.
          */
        public static long startDownloadByUri(
                Context context,
                String url,
                String cookieData,
                boolean showDownload,
                int downloadDestination,
                boolean allowRoaming,
                boolean skipIntegrityCheck,
                String title,
                String notification_package,
                String notification_class,
                String notification_extras) {
            ContentResolver cr = context.getContentResolver();

            // Tell download manager to start downloading update.
            ContentValues values = new ContentValues();
            values.put(Impl.URI, url);
            values.put(Impl.COOKIE_DATA, cookieData);
            values.put(Impl.VISIBILITY, showDownload ? Impl.VISIBILITY_VISIBLE : Impl.VISIBILITY_HIDDEN);
            if (title != null)
            {
                values.put(Impl.TITLE, title);
            }
            values.put(Impl.APP_DATA, url);

            // NOTE:  destination should be seperated from whether the download
            // can happen when roaming
            int destination = Impl.DESTINATION_EXTERNAL;
            switch (downloadDestination)
            {
                case DOWNLOAD_DESTINATION_EXTERNAL:
                    destination = Impl.DESTINATION_EXTERNAL;
                    break;
                case DOWNLOAD_DESTINATION_CACHE:
                    if (allowRoaming)
                    {
                        destination = Impl.DESTINATION_CACHE_PARTITION;
                    }
                    else
                    {
                        destination = Impl.DESTINATION_CACHE_PARTITION_NOROAMING;
                    }
                    break;
                case DOWNLOAD_DESTINATION_CACHE_PURGEABLE:
                    destination = Impl.DESTINATION_CACHE_PARTITION_PURGEABLE;
                    break;
            }
            values.put(Impl.DESTINATION, destination);
            values.put(Impl.NO_INTEGRITY, skipIntegrityCheck);  // Don't check ETag
            if (notification_package != null && notification_class != null)
            {
                values.put(Impl.NOTIFICATION_PACKAGE, notification_package);
                values.put(Impl.NOTIFICATION_CLASS, notification_class);

                if (notification_extras != null)
                {
                    values.put(Impl.NOTIFICATION_EXTRAS, notification_extras);
                }
            }

            Uri downloadUri = cr.insert(Impl.CONTENT_URI, values);

            long downloadId = DOWNLOAD_ID_INVALID;
            if (downloadUri != null) {
                downloadId = Long.parseLong(downloadUri.getLastPathSegment());
            }
            return downloadId;
        }
    }

    /** @hide */
    private static final int STATUS_INVALID = 0;
    /** @hide */
    private static final int STATUS_DOWNLOADING_UPDATE = 3;
    /** @hide */
    private static final int STATUS_DOWNLOADED_UPDATE = 4;

    /**
     * Column projection for the query to the download manager. This must match
     * with the constants DOWNLOADS_COLUMN_*.
     * @hide
     */
    private static final String[] DOWNLOADS_PROJECTION = {
            BaseColumns._ID,
            Impl.APP_DATA,
            Impl.STATUS,
            Impl._DATA,
            Impl.LAST_MODIFICATION,
            Impl.CURRENT_BYTES,
    };

    /**
     * The column index for the ID.
     * @hide
     */
    private static final int DOWNLOADS_COLUMN_ID = 0;
    /**
     * The column index for the URI.
     * @hide
     */
    private static final int DOWNLOADS_COLUMN_URI = 1;
    /**
     * The column index for the status code.
     * @hide
     */
    private static final int DOWNLOADS_COLUMN_STATUS = 2;
    /**
     * The column index for the filename.
     * @hide
     */
    private static final int DOWNLOADS_COLUMN_FILENAME = 3;
    /**
     * The column index for the last modification time.
     * @hide
     */
    private static final int DOWNLOADS_COLUMN_LAST_MODIFICATION = 4;
    /**
     * The column index for the number of bytes downloaded so far.
     * @hide
     */
    private static final int DOWNLOADS_COLUMN_CURRENT_BYTES = 5;

    /**
     * Gets the status of a download.
     *
     * @param c A Cursor pointing to a download.  The URL column is assumed to be valid.
     * @return The status of the download.
     * @hide
     */
    private static final int getStatusOfDownload( Cursor c, long redownload_threshold) {
        int status = c.getInt(DOWNLOADS_COLUMN_STATUS);

        if (!Impl.isStatusCompleted(status)) {
            // Check if it's stuck
            long modified = c.getLong(DOWNLOADS_COLUMN_LAST_MODIFICATION);
            long now = System.currentTimeMillis();
            if (now < modified || now - modified > redownload_threshold) {
                return STATUS_INVALID;
            }
            return STATUS_DOWNLOADING_UPDATE;
        }

        if (Impl.isStatusError(status)) {
            return STATUS_INVALID;
        }

        String filename = c.getString(DOWNLOADS_COLUMN_FILENAME);
        if (filename == null) {
            return STATUS_INVALID;
        }
        return STATUS_DOWNLOADED_UPDATE;
    }

    public static final class Impl implements BaseColumns
    {
    	    private Impl() {}

    	    /**
    	     * The content:// URI for the data table in the provider
    	     */
    	    public static final Uri CONTENT_URI = Uri.parse("content://com.futureconcepts.downloads/download");

    	    /**
    	     * Broadcast Action: this is sent by the download manager to the app
    	     * that had initiated a download when that download completes. The
    	     * download's content: uri is specified in the intent's data.
    	     */
    	    public static final String ACTION_DOWNLOAD_COMPLETED =
    	            "com.futureconcepts.mercury.intent.action.DOWNLOAD_COMPLETED";

    	    /**
    	     * Broadcast Action: this is sent by the download manager to the app
    	     * that had initiated a download when the user selects the notification
    	     * associated with that download. The download's content: uri is specified
    	     * in the intent's data if the click is associated with a single download,
    	     * or Downloads.CONTENT_URI if the notification is associated with
    	     * multiple downloads.
    	     * Note: this is not currently sent for downloads that have completed
    	     * successfully.
    	     */
    	    public static final String ACTION_NOTIFICATION_CLICKED =
    	            "com.futureconcepts.mercury.intent.action.DOWNLOAD_NOTIFICATION_CLICKED";

    	    /**
    	     * The name of the column containing the URI of the data being downloaded.
    	     * <P>Type: TEXT</P>
    	     * <P>Owner can Init/Read</P>
    	     */
    	    public static final String URI = "uri";

    	    /**
    	     * The name of the column containing application-specific data.
    	     * <P>Type: TEXT</P>
    	     * <P>Owner can Init/Read/Write</P>
    	     */
    	    public static final String APP_DATA = "entity";

    	    /**
    	     * The name of the column containing the flags that indicates whether
    	     * the initiating application is capable of verifying the integrity of
    	     * the downloaded file. When this flag is set, the download manager
    	     * performs downloads and reports success even in some situations where
    	     * it can't guarantee that the download has completed (e.g. when doing
    	     * a byte-range request without an ETag, or when it can't determine
    	     * whether a download fully completed).
    	     * <P>Type: BOOLEAN</P>
    	     * <P>Owner can Init</P>
    	     */
    	    public static final String NO_INTEGRITY = "no_integrity";

    	    /**
    	     * The name of the column containing the filename that the initiating
    	     * application recommends. When possible, the download manager will attempt
    	     * to use this filename, or a variation, as the actual name for the file.
    	     * <P>Type: TEXT</P>
    	     * <P>Owner can Init</P>
    	     */
    	    public static final String FILENAME_HINT = "hint";

    	    /**
    	     * The name of the column containing the filename where the downloaded data
    	     * was actually stored.
    	     * <P>Type: TEXT</P>
    	     * <P>Owner can Read</P>
    	     */
    	    public static final String _DATA = "_data";

    	    /**
    	     * The name of the column containing the MIME type of the downloaded data.
    	     * <P>Type: TEXT</P>
    	     * <P>Owner can Init/Read</P>
    	     */
    	    public static final String MIME_TYPE = "mimetype";

    	    /**
    	     * The name of the column containing the flag that controls the destination
    	     * of the download. See the DESTINATION_* constants for a list of legal values.
    	     * <P>Type: INTEGER</P>
    	     * <P>Owner can Init</P>
    	     */
    	    public static final String DESTINATION = "destination";

    	    /**
    	     * The name of the column containing the flags that controls whether the
    	     * download is displayed by the UI. See the VISIBILITY_* constants for
    	     * a list of legal values.
    	     * <P>Type: INTEGER</P>
    	     * <P>Owner can Init/Read/Write</P>
    	     */
    	    public static final String VISIBILITY = "visibility";

    	    /**
    	     * The name of the column containing the current control state  of the download.
    	     * Applications can write to this to control (pause/resume) the download.
    	     * the CONTROL_* constants for a list of legal values.
    	     * <P>Type: INTEGER</P>
    	     * <P>Owner can Read</P>
    	     */
    	    public static final String CONTROL = "control";

    	    /**
    	     * The name of the column containing the current status of the download.
    	     * Applications can read this to follow the progress of each download. See
    	     * the STATUS_* constants for a list of legal values.
    	     * <P>Type: INTEGER</P>
    	     * <P>Owner can Read</P>
    	     */
    	    public static final String STATUS = "status";

    	    /**
    	     * The name of the column containing the date at which some interesting
    	     * status changed in the download. Stored as a System.currentTimeMillis()
    	     * value.
    	     * <P>Type: BIGINT</P>
    	     * <P>Owner can Read</P>
    	     */
    	    public static final String LAST_MODIFICATION = "lastmod";

    	    /**
    	     * The name of the column containing the package name of the application
    	     * that initiating the download. The download manager will send
    	     * notifications to a component in this package when the download completes.
    	     * <P>Type: TEXT</P>
    	     * <P>Owner can Init/Read</P>
    	     */
    	    public static final String NOTIFICATION_PACKAGE = "notificationpackage";

    	    /**
    	     * The name of the column containing the component name of the class that
    	     * will receive notifications associated with the download. The
    	     * package/class combination is passed to
    	     * Intent.setClassName(String,String).
    	     * <P>Type: TEXT</P>
    	     * <P>Owner can Init/Read</P>
    	     */
    	    public static final String NOTIFICATION_CLASS = "notificationclass";

    	    /**
    	     * If extras are specified when requesting a download they will be provided in the intent that
    	     * is sent to the specified class and package when a download has finished.
    	     * <P>Type: TEXT</P>
    	     * <P>Owner can Init</P>
    	     */
    	    public static final String NOTIFICATION_EXTRAS = "notificationextras";

    	    /**
    	     * The name of the column contain the values of the cookie to be used for
    	     * the download. This is used directly as the value for the Cookie: HTTP
    	     * header that gets sent with the request.
    	     * <P>Type: TEXT</P>
    	     * <P>Owner can Init</P>
    	     */
    	    public static final String COOKIE_DATA = "cookiedata";

    	    /**
    	     * The name of the column containing the user agent that the initiating
    	     * application wants the download manager to use for this download.
    	     * <P>Type: TEXT</P>
    	     * <P>Owner can Init</P>
    	     */
    	    public static final String USER_AGENT = "useragent";

    	    /**
    	     * The name of the column containing the referer (sic) that the initiating
    	     * application wants the download manager to use for this download.
    	     * <P>Type: TEXT</P>
    	     * <P>Owner can Init</P>
    	     */
    	    public static final String REFERER = "referer";

    	    /**
    	     * The name of the column containing the total size of the file being
    	     * downloaded.
    	     * <P>Type: INTEGER</P>
    	     * <P>Owner can Read</P>
    	     */
    	    public static final String TOTAL_BYTES = "total_bytes";

    	    /**
    	     * The name of the column containing the size of the part of the file that
    	     * has been downloaded so far.
    	     * <P>Type: INTEGER</P>
    	     * <P>Owner can Read</P>
    	     */
    	    public static final String CURRENT_BYTES = "current_bytes";

    	    /**
    	     * The name of the column where the initiating application can provided the
    	     * title of this download. The title will be displayed ito the user in the
    	     * list of downloads.
    	     * <P>Type: TEXT</P>
    	     * <P>Owner can Init/Read/Write</P>
    	     */
    	    public static final String TITLE = "title";

    	    /**
    	     * The name of the column where the initiating application can provide the
    	     * description of this download. The description will be displayed to the
    	     * user in the list of downloads.
    	     * <P>Type: TEXT</P>
    	     * <P>Owner can Init/Read/Write</P>
    	     */
    	    public static final String DESCRIPTION = "description";

    	    /*
    	     * Lists the destinations that an application can specify for a download.
    	     */

    	    /**
    	     * This download will be saved to the external storage. This is the
    	     * default behavior, and should be used for any file that the user
    	     * can freely access, copy, delete. Even with that destination,
    	     * unencrypted DRM files are saved in secure internal storage.
    	     * Downloads to the external destination only write files for which
    	     * there is a registered handler. The resulting files are accessible
    	     * by filename to all applications.
    	     */
    	    public static final int DESTINATION_EXTERNAL = 0;

    	    /**
    	     * This download will be saved to the download manager's private
    	     * partition. This is the behavior used by applications that want to
    	     * download private files that are used and deleted soon after they
    	     * get downloaded. All file types are allowed, and only the initiating
    	     * application can access the file (indirectly through a content
    	     * provider). This requires the
    	     * android.permission.ACCESS_DOWNLOAD_MANAGER_ADVANCED permission.
    	     */
    	    public static final int DESTINATION_CACHE_PARTITION = 1;

    	    /**
    	     * This download will be saved to the download manager's private
    	     * partition and will be purged as necessary to make space. This is
    	     * for private files (similar to CACHE_PARTITION) that aren't deleted
    	     * immediately after they are used, and are kept around by the download
    	     * manager as long as space is available.
    	     */
    	    public static final int DESTINATION_CACHE_PARTITION_PURGEABLE = 2;

    	    /**
    	     * This download will be saved to the download manager's private
    	     * partition, as with DESTINATION_CACHE_PARTITION, but the download
    	     * will not proceed if the user is on a roaming data connection.
    	     */
    	    public static final int DESTINATION_CACHE_PARTITION_NOROAMING = 3;

    	    /**
    	     * This download is allowed to run.
    	     */
    	    public static final int CONTROL_RUN = 0;

    	    /**
    	     * This download must pause at the first opportunity.
    	     */
    	    public static final int CONTROL_PAUSED = 1;

    	    /*
    	     * Lists the states that the download manager can set on a download
    	     * to notify applications of the download progress.
    	     * The codes follow the HTTP families:<br>
    	     * 1xx: informational<br>
    	     * 2xx: success<br>
    	     * 3xx: redirects (not used by the download manager)<br>
    	     * 4xx: client errors<br>
    	     * 5xx: server errors
    	     */

    	    /**
    	     * Returns whether the status is informational (i.e. 1xx).
    	     */
    	    public static boolean isStatusInformational(int status) {
    	        return (status >= 100 && status < 200);
    	    }

    	    /**
    	     * Returns whether the download is suspended. (i.e. whether the download
    	     * won't complete without some action from outside the download
    	     * manager).
    	     */
    	    public static boolean isStatusSuspended(int status) {
    	        return (status == STATUS_PENDING_PAUSED || status == STATUS_RUNNING_PAUSED);
    	    }

    	    /**
    	     * Returns whether the status is a success (i.e. 2xx).
    	     */
    	    public static boolean isStatusSuccess(int status) {
    	        return (status >= 200 && status < 300);
    	    }

    	    /**
    	     * Returns whether the status is an error (i.e. 4xx or 5xx).
    	     */
    	    public static boolean isStatusError(int status) {
    	        return (status >= 400 && status < 600);
    	    }

    	    /**
    	     * Returns whether the status is a client error (i.e. 4xx).
    	     */
    	    public static boolean isStatusClientError(int status) {
    	        return (status >= 400 && status < 500);
    	    }

    	    /**
    	     * Returns whether the status is a server error (i.e. 5xx).
    	     */
    	    public static boolean isStatusServerError(int status) {
    	        return (status >= 500 && status < 600);
    	    }

    	    /**
    	     * Returns whether the download has completed (either with success or
    	     * error).
    	     */
    	    public static boolean isStatusCompleted(int status) {
    	        return (status >= 200 && status < 300) || (status >= 400 && status < 600);
    	    }

    	    /**
    	     * This download hasn't stated yet
    	     */
    	    public static final int STATUS_PENDING = 190;

    	    /**
    	     * This download hasn't stated yet and is paused
    	     */
    	    public static final int STATUS_PENDING_PAUSED = 191;

    	    /**
    	     * This download has started
    	     */
    	    public static final int STATUS_RUNNING = 192;

    	    /**
    	     * This download has started and is paused
    	     */
    	    public static final int STATUS_RUNNING_PAUSED = 193;

    	    /**
    	     * This download has successfully completed.
    	     * Warning: there might be other status values that indicate success
    	     * in the future.
    	     * Use isSucccess() to capture the entire category.
    	     */
    	    public static final int STATUS_SUCCESS = 200;

    	    /**
    	     * This request couldn't be parsed. This is also used when processing
    	     * requests with unknown/unsupported URI schemes.
    	     */
    	    public static final int STATUS_BAD_REQUEST = 400;

    	    /**
    	     * This download can't be performed because the content type cannot be
    	     * handled.
    	     */
    	    public static final int STATUS_NOT_ACCEPTABLE = 406;

    	    /**
    	     * This download cannot be performed because the length cannot be
    	     * determined accurately. This is the code for the HTTP error "Length
    	     * Required", which is typically used when making requests that require
    	     * a content length but don't have one, and it is also used in the
    	     * client when a response is received whose length cannot be determined
    	     * accurately (therefore making it impossible to know when a download
    	     * completes).
    	     */
    	    public static final int STATUS_LENGTH_REQUIRED = 411;

    	    /**
    	     * This download was interrupted and cannot be resumed.
    	     * This is the code for the HTTP error "Precondition Failed", and it is
    	     * also used in situations where the client doesn't have an ETag at all.
    	     */
    	    public static final int STATUS_PRECONDITION_FAILED = 412;

    	    /**
    	     * This download was canceled
    	     */
    	    public static final int STATUS_CANCELED = 490;

    	    /**
    	     * This download has completed with an error.
    	     * Warning: there will be other status values that indicate errors in
    	     * the future. Use isStatusError() to capture the entire category.
    	     */
    	    public static final int STATUS_UNKNOWN_ERROR = 491;

    	    /**
    	     * This download couldn't be completed because of a storage issue.
    	     * Typically, that's because the filesystem is missing or full.
    	     */
    	    public static final int STATUS_FILE_ERROR = 492;

    	    /**
    	     * This download couldn't be completed because of an HTTP
    	     * redirect response that the download manager couldn't
    	     * handle.
    	     */
    	    public static final int STATUS_UNHANDLED_REDIRECT = 493;

    	    /**
    	     * This download couldn't be completed because of an
    	     * unspecified unhandled HTTP code.
    	     */
    	    public static final int STATUS_UNHANDLED_HTTP_CODE = 494;

    	    /**
    	     * This download couldn't be completed because of an
    	     * error receiving or processing data at the HTTP level.
    	     */
    	    public static final int STATUS_HTTP_DATA_ERROR = 495;

    	    /**
    	     * This download couldn't be completed because of an
    	     * HttpException while setting up the request.
    	     */
    	    public static final int STATUS_HTTP_EXCEPTION = 496;

    	    /**
    	     * This download couldn't be completed because there were
    	     * too many redirects.
    	     */
    	    public static final int STATUS_TOO_MANY_REDIRECTS = 497;

    	    /**
    	     * This download is visible but only shows in the notifications
    	     * while it's in progress.
    	     */
    	    public static final int VISIBILITY_VISIBLE = 0;

    	    /**
    	     * This download is visible and shows in the notifications while
    	     * in progress and after completion.
    	     */
    	    public static final int VISIBILITY_VISIBLE_NOTIFY_COMPLETED = 1;

    	    /**
    	     * This download doesn't show in the UI or in the notifications.
    	     */
    	    public static final int VISIBILITY_HIDDEN = 2;
    	}
    	

    /**
     * @hide
     */
    private Downloads() {}
}
