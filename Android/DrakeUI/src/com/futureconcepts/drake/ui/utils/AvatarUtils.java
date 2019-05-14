package com.futureconcepts.drake.ui.utils;

import com.futureconcepts.drake.client.Imps;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

public class AvatarUtils
{
    private static final String TAG = AvatarUtils.class.getSimpleName();

    private AvatarUtils()
    {
    }

    public static Drawable getAvatarFromCursor(Cursor cursor, int dataColumn)
    {
        byte[] rawData = cursor.getBlob(dataColumn);
        if (rawData == null) {
            return null;
        }
        return decodeAvatar(rawData);
    }

    public static Drawable getAvatarFromCursor(Cursor cursor, int dataColumn,
            int encodedDataColumn, String username, boolean updateBlobUseCursor,
            ContentResolver resolver, Uri updateBlobUri) {
        /**
         * Optimization: the avatar table in IM content provider have two
         * columns, one for the raw blob data, another for the base64 encoded
         * data. The reason for this is when the avatars are initially
         * downloaded, they are in the base64 encoded form, and instead of
         * base64 decode the avatars for all the buddies up front, we can just
         * simply store the encoded data in the table, and decode them on demand
         * when displaying them. Once we decode the avatar, we store the decoded
         * data as a blob, and null out the encoded column in the avatars table.
         * query the raw blob data first, if present, great; if not, query the
         * encoded data, decode it and store as the blob, and null out the
         * encoded column.
         */
        byte[] rawData = cursor.getBlob(dataColumn);

        if (rawData == null) {
            String encodedData = cursor.getString(encodedDataColumn);
            if (encodedData == null) {
                // Log.e(LogTag.LOG_TAG, "getAvatarFromCursor for " + username +
                // ", no raw or encoded data!");
                return null;
            }

            // TODO rawData = android.os.Base64Utils.decodeBase64(encodedData);

            // if (DBG) {
            // log("getAvatarFromCursor for " + username + ": found encoded
            // data,"
            // + " update blob with data, len=" + rawData.length);
            // }

            if (updateBlobUseCursor) {
                // TODO cursor.updateBlob(dataColumn, rawData);
                // cursor.updateString(encodedDataColumn, null);
                // cursor.commitUpdates();
            } else {
                updateAvatarBlob(resolver, updateBlobUri, rawData, username);
            }
        }

        return decodeAvatar(rawData);
    }

    private static void updateAvatarBlob(ContentResolver resolver, Uri updateUri, byte[] data, String username)
    {
        ContentValues values = new ContentValues(3);
        values.put(Imps.Avatars.DATA, data);

        StringBuilder buf = new StringBuilder(Imps.Avatars.CONTACT);
        buf.append("=?");

        String[] selectionArgs = new String[] {
            username
        };

        resolver.update(updateUri, values, buf.toString(), selectionArgs);
    }

    private static Drawable decodeAvatar(byte[] data)
    {
        Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
        Drawable avatar = new BitmapDrawable(b);
        return avatar;
    }
}
