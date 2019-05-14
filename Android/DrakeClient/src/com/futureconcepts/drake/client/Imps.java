/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.futureconcepts.drake.client;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * The IM provider stores all information about roster contacts, chat messages, presence, etc.
 *
 * @hide
 */
public class Imps {
    /**
     * no public constructor since this is a utility class
     */
    private Imps() {}

    /**
     * Columns from the Contacts table.
     */
    public interface ContactsColumns {
        /**
         * The username
         * <P>Type: TEXT</P>
         */
        String USERNAME = "username";

        /**
         * The nickname or display name
         * <P>Type: TEXT</P>
         */
        String NICKNAME = "nickname";

        /**
         * The contactList this contact belongs to
         * <P>Type: INTEGER</P>
         */
        String CONTACTLIST = "contactList";

        /**
         * Contact type
         * <P>Type: INTEGER</P>
         */
        String TYPE = "type";

        /**
         * normal IM contact
         */
        int TYPE_NORMAL = 0;
        /**
         * temporary contact, someone not in the list of contacts that we
         * subscribe presence for. Usually created because of the user is
         * having a chat session with this contact.
         */
        int TYPE_TEMPORARY = 1;
        /**
         * temporary contact created for group chat.
         */
        int TYPE_GROUP = 2;
        /**
         * blocked contact.
         */
        int TYPE_BLOCKED = 3;
        /**
         * the contact is hidden. The client should always display this contact to the user.
         */
        int TYPE_HIDDEN = 4;
        /**
         * the contact is pinned. The client should always display this contact to the user.
         */
        int TYPE_PINNED = 5;

        /**
         * Contact subscription status
         * <P>Type: INTEGER</P>
         */
        String SUBSCRIPTION_STATUS = "subscriptionStatus";

        /**
         * no pending subscription
         */
        int SUBSCRIPTION_STATUS_NONE = 0;
        /**
         * requested to subscribe
         */
        int SUBSCRIPTION_STATUS_SUBSCRIBE_PENDING = 1;
        /**
         * requested to unsubscribe
         */
        int SUBSCRIPTION_STATUS_UNSUBSCRIBE_PENDING = 2;

        /**
         * Contact subscription type
         * <P>Type: INTEGER </P>
         */
        String SUBSCRIPTION_TYPE = "subscriptionType";

        /**
         * The user and contact have no interest in each other's presence.
         */
        int SUBSCRIPTION_TYPE_NONE = 0;
        /**
         * The user wishes to stop receiving presence updates from the contact.
         */
        int SUBSCRIPTION_TYPE_REMOVE = 1;
        /**
         * The user is interested in receiving presence updates from the contact.
         */
        int SUBSCRIPTION_TYPE_TO = 2;
        /**
         * The contact is interested in receiving presence updates from the user.
         */
        int SUBSCRIPTION_TYPE_FROM = 3;
        /**
         * The user and contact have a mutual interest in each other's presence.
         */
        int SUBSCRIPTION_TYPE_BOTH = 4;
        /**
         * This is a special type reserved for pending subscription requests
         */
        int SUBSCRIPTION_TYPE_INVITATIONS = 5;

        /**
         * Quick Contact: derived from Google Contact Extension's "message_count" attribute.
         * <P>Type: INTEGER</P>
         */
        String QUICK_CONTACT = "qc";

        /**
         * Google Contact Extension attribute
         *
         * Rejected: a boolean value indicating whether a subscription request from
         * this client was ever rejected by the user. "true" indicates that it has.
         * This is provided so that a client can block repeated subscription requests.
         * <P>Type: INTEGER</P>
         */
        String REJECTED = "rejected";

        /**
         * Off The Record status: 0 for disabled, 1 for enabled
         * <P>Type: INTEGER </P>
         */
        String OTR = "otr";
    }

    /**
     * This defines the different type of values of {@link ContactsColumns#OTR}
     */
    public interface OffTheRecordType {
        /*
         * Off the record not turned on
         */
        int DISABLED = 0;
        /**
         * Off the record turned on, but we don't know who turned it on
         */
        int ENABLED = 1;
        /**
         * Off the record turned on by the user
         */
        int ENABLED_BY_USER = 2;
        /**
         * Off the record turned on by the buddy
         */
        int ENABLED_BY_BUDDY = 3;
    };

    /**
     * This table contains contacts.
     */
    public static final class Contacts implements BaseColumns, ContactsColumns, PresenceColumns, ChatsColumns{
        /**
         * no public constructor since this is a utility class
         */
        private Contacts() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
            Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/contacts");

        /**
         * The content:// style URL for contacts joined with presence
         */
        public static final Uri CONTENT_URI_WITH_PRESENCE =
            Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/contactsWithPresence");

        /**
         * The content:// style URL for barebone contacts, not joined with any other table
         */
        public static final Uri CONTENT_URI_CONTACTS_BAREBONE =
            Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/contactsBarebone");

        /**
         * The content:// style URL for contacts who have an open chat session
         */
        public static final Uri CONTENT_URI_CHAT_CONTACTS =
            Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/contacts/chatting");

        /**
         * The content:// style URL for contacts who have been blocked
         */
        public static final Uri CONTENT_URI_BLOCKED_CONTACTS =
            Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/contacts/blocked");

        /**
         * The content:// style URL for contacts who have an open chat session
         */
        public static final Uri CONTENT_URI_CHAT_CONTACTS_BY =
            Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/contacts/chatting");

        /**
         * The content:// style URL for contacts who are online
         */
        public static final Uri CONTENT_URI_ONLINE_CONTACTS_BY =
            Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/contacts/online");

        /**
         * The content:// style URL for contacts who are offline
         */
        public static final Uri CONTENT_URI_OFFLINE_CONTACTS_BY =
            Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/contacts/offline");

        /**
         * The content:// style URL for operations on bulk contacts
         */
        public static final Uri BULK_CONTENT_URI =
                Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/bulk_contacts");

        /**
         * The content:// style URL for the count of online contacts in each contact list
         */
        public static final Uri CONTENT_URI_ONLINE_COUNT =
            Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/contacts/onlineCount");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of
         * people.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-contacts";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
         * person.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/imps-contacts";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER =
                "subscriptionType DESC, last_message_date DESC," +
                        " mode DESC, nickname COLLATE UNICODE ASC";

        public static final String CHATS_CONTACT = "chats_contact";

        public static final String AVATAR_HASH = "avatars_hash";

        public static final String AVATAR_DATA = "avatars_data";
    }

    /**
     * Columns from the ContactList table.
     */
    public interface ContactListColumns {
        String NAME = "name";
    }

    /**
     * This table contains the contact lists.
     */
    public static final class ContactList implements BaseColumns, ContactListColumns {
        private ContactList() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
            Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/contactLists");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of
         * people.
         */
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/imps-contactLists";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
         * person.
         */
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/imps-contactLists";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "name COLLATE UNICODE ASC";
    }

    /**
     * Columns from the BlockedList table.
     */
    public interface BlockedListColumns {
        /**
         * The username of the blocked contact.
         * <P>Type: TEXT</P>
         */
        String USERNAME = "username";

        /**
         * The nickname of the blocked contact.
         * <P>Type: TEXT</P>
         */
        String NICKNAME = "nickname";
    }

    /**
     * This table contains blocked lists
     */
    public static final class BlockedList implements BaseColumns, BlockedListColumns {
        private BlockedList() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
            Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/blockedList");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of
         * people.
         */
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/imps-blockedList";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
         * person.
         */
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/imps-blockedList";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "nickname ASC";

        public static final String AVATAR_DATA = "avatars_data";
    }

    /**
     * Columns from the contactsEtag table
     */
    public interface ContactsEtagColumns {
        /**
         * The roster etag, computed by the server, stored on the client. There is one etag
         * per account roster.
         * <P>Type: TEXT</P>
         */
        String ETAG = "etag";

        /**
         * The OTR etag, computed by the server, stored on the client. There is one OTR etag
         * per account roster.
         * <P>Type: TEXT</P>
         */
        String OTR_ETAG = "otr_etag";
    }

    public static final class ContactsEtag implements BaseColumns, ContactsEtagColumns {
        private ContactsEtag() {}

        public static final Cursor query(ContentResolver cr,
                String[] projection) {
            return cr.query(CONTENT_URI, projection, null, null, null);
        }

        public static final Cursor query(ContentResolver cr,
                String[] projection, String where, String orderBy) {
            return cr.query(CONTENT_URI, projection, where,
                    null, orderBy == null ? null : orderBy);
        }

        public static final String getRosterEtag(ContentResolver resolver) {
            String retVal = null;

            Cursor c = resolver.query(CONTENT_URI,
                    CONTACT_ETAG_PROJECTION,
                    null,
                    null /* selection args */,
                    null /* sort order */);

            try {
                if (c.moveToFirst()) {
                    retVal = c.getString(COLUMN_ETAG);
                }
            } finally {
                c.close();
            }

            return retVal;
        }

        public static final String getOtrEtag(ContentResolver resolver) {
            String retVal = null;

            Cursor c = resolver.query(CONTENT_URI,
                    CONTACT_OTR_ETAG_PROJECTION,
                    null,
                    null /* selection args */,
                    null /* sort order */);

            try {
                if (c.moveToFirst()) {
                    retVal = c.getString(COLUMN_OTR_ETAG);
                }
            } finally {
                c.close();
            }

            return retVal;
        }

        private static final String[] CONTACT_ETAG_PROJECTION = new String[] {
                Imps.ContactsEtag.ETAG    // 0
        };

        private static int COLUMN_ETAG = 0;

        private static final String[] CONTACT_OTR_ETAG_PROJECTION = new String[] {
                Imps.ContactsEtag.OTR_ETAG    // 0
        };

        private static int COLUMN_OTR_ETAG = 0;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
            Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/contactsEtag");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of
         * people.
         */
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/imps-contactsEtag";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
         * person.
         */
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/imps-contactsEtag";
    }

    /**
     * Message type definition
     */
    public interface MessageType {
        /* sent message */
        int OUTGOING = 0;
        /* received message */
        int INCOMING = 1;
        /* presence became available */
        int PRESENCE_AVAILABLE = 2;
        /* presence became away */
        int PRESENCE_AWAY = 3;
        /* presence became DND (busy) */
        int PRESENCE_DND = 4;
        /* presence became unavailable */
        int PRESENCE_UNAVAILABLE = 5;
        /* the message is converted to a group chat */
        int CONVERT_TO_GROUPCHAT = 6;
        /* generic status */
        int STATUS = 7;
        /* the message cannot be sent now, but will be sent later */
        int POSTPONED = 8;
        /* off The Record status is turned off */
        int OTR_IS_TURNED_OFF = 9;
        /* off the record status is turned on */
        int OTR_IS_TURNED_ON = 10;
        /* off the record status turned on by user */
        int OTR_TURNED_ON_BY_USER = 11;
        /* off the record status turned on by buddy */
        int OTR_TURNED_ON_BY_BUDDY = 12;
        /* file transfer status changed */
        int FILE_TRANSFER = 13;
    }

    /**
     * The common columns for messages table
     */
    public interface MessageColumns {
        /**
         * The thread_id column stores the contact id of the contact the message belongs to.
         * For groupchat messages, the thread_id stores the group id, which is the contact id
         * of the temporary group contact created for the groupchat. So there should be no
         * collision between groupchat message thread id and regular message thread id.
         */
        String THREAD_ID = "thread_id";

        /**
         * The nickname. This is used for groupchat messages to indicate the participant's
         * nickname. For non groupchat messages, this field should be left empty.
         */
        String NICKNAME = "nickname";

        /**
         * The body
         * <P>Type: TEXT</P>
         */
        String BODY = "body";

        /**
         * The date this message is sent or received
         * <P>Type: INTEGER</P>
         */
        String DATE = "date";

        /**
         * Message Type, see {@link MessageType}
         * <P>Type: INTEGER</P>
         */
        String TYPE = "type";

        /**
         * Error Code: 0 means no error.
         * <P>Type: INTEGER </P>
         */
        String ERROR_CODE = "err_code";

        /**
         * Error Message
         * <P>Type: TEXT</P>
         */
        String ERROR_MESSAGE = "err_msg";
        
        String FILE_PATH = "file_path";
        String MIME_TYPE = "mime_type";

        /**
         * Packet ID, auto assigned by the GTalkService for outgoing messages or the
         * GTalk server for incoming messages. The packet id field is optional for messages,
         * so it could be null.
         * <P>Type: STRING</P>
         */
        String PACKET_ID = "packet_id";

        /**
         * Is groupchat message or not
         * <P>Type: INTEGER</P>
         */
        String IS_GROUP_CHAT = "is_muc";

        /**
         * A hint that the UI should show the sent time of this message
         * <P>Type: INTEGER</P>
         */
        String DISPLAY_SENT_TIME = "show_ts";

        /**
         * Whether a delivery confirmation was received.
         * <P>Type: INTEGER</P>
         */
        String IS_DELIVERED = "is_delivered";
        
        /**
         * generic argument used for message extensions
         * Currently used for file transfer status file size
         * <P>Type: LONG</P>
         */
        String LONG_P1 = "long_p1";

        /**
         * generic argument used for message extensions
         * Currently used for file transfer status amount written
         * <P>Type: LONG</P>
         */
        String LONG_P2 = "long_p2";
    }

    /**
     * This table contains messages.
     */
    public static final class Messages implements BaseColumns, MessageColumns {
        /**
         * no public constructor since this is a utility class
         */
        private Messages() {}

        /**
         * Gets the Uri to query messages by thread id.
         *
         * @param threadId the thread id of the message.
         * @return the Uri
         */
        public static final Uri getContentUriByThreadId(long threadId) {
            Uri.Builder builder = CONTENT_URI_MESSAGES_BY_THREAD_ID.buildUpon();
            ContentUris.appendId(builder, threadId);
            return builder.build();
        }

        /**
         * @deprecated
         *
         * Gets the Uri to query messages by contact.
         *
         * @param username the user name of the contact.
         * @return the Uri
         */
        public static final Uri getContentUriByContact(String username) {
            Uri.Builder builder = CONTENT_URI_MESSAGES_BY_CONTACT.buildUpon();
            builder.appendPath(username);
            return builder.build();
        }

        /**
         * Gets the Uri to query off the record messages by thread id.
         *
         * @param threadId the thread id of the message.
         * @return the Uri
         */
        public static final Uri getOtrMessagesContentUriByThreadId(long threadId) {
            Uri.Builder builder = OTR_MESSAGES_CONTENT_URI_BY_THREAD_ID.buildUpon();
            ContentUris.appendId(builder, threadId);
            return builder.build();
        }

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
                Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/messages");

        /**
         * The content:// style URL for messages by thread id
         */
        public static final Uri CONTENT_URI_MESSAGES_BY_THREAD_ID =
                Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/messagesByThreadId");

        /**
         * The content:// style URL for messages by contact
         */
        public static final Uri CONTENT_URI_MESSAGES_BY_CONTACT =
                Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/messagesByContact");

        /**
         * The content:// style url for off the record messages
         */
        public static final Uri OTR_MESSAGES_CONTENT_URI =
                Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/otrMessages");

        /**
         * The content:// style url for off the record messages by thread id
         */
        public static final Uri OTR_MESSAGES_CONTENT_URI_BY_THREAD_ID =
                Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/otrMessagesByThreadId");


        public static final Uri OTR_MESSAGES_CONTENT_URI_BY_PACKET_ID =
                Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/otrMessagesByPacketId");


        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of
         * people.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-messages";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
         * person.
         */
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/imps-messages";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "date ASC";

        /**
         * The "contact" column. This is not a real column in the messages table, but a
         * temoprary column created when querying for messages (joined with the contacts table)
         */
        public static final String CONTACT = "contact";
    }

    /**
     * Columns for the GroupMember table.
     */
    public interface GroupMemberColumns {
        /**
         * The id of the group this member belongs to.
         * <p>Type: INTEGER</p>
         */
        String GROUP = "groupId";

        /**
         * The full name of this member.
         * <p>Type: TEXT</p>
         */
        String USERNAME = "username";

        /**
         * The nick name of this member.
         * <p>Type: TEXT</p>
         */
        String NICKNAME = "nickname";
    }

    public final static class GroupMembers implements GroupMemberColumns {
        private GroupMembers(){}

        public static final Uri CONTENT_URI =
            Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/groupMembers");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of
         * group members.
         */
        public static final String CONTENT_TYPE =
            "vnd.android.cursor.dir/imps-groupMembers";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
         * group member.
         */
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/imps-groupMembers";
    }

    /**
     * Columns from the Invitation table.
     */
    public interface InvitationColumns {
        /**
         * The invitation id.
         * <p>Type: TEXT</p>
         */
        String INVITE_ID = "inviteId";

        /**
         * The name of the sender of the invitation.
         * <p>Type: TEXT</p>
         */
        String SENDER = "sender";

        /**
         * The name of the group which the sender invite you to join.
         * <p>Type: TEXT</p>
         */
        String GROUP_NAME = "groupName";

        /**
         * A note
         * <p>Type: TEXT</p>
         */
        String NOTE = "note";

        /**
         * The password (if any)
         * <p>Type: TEXT</p>
         */
        String PASSWORD = "password";

        /**
         * The current status of the invitation.
         * <p>Type: TEXT</p>
         */
        String STATUS = "status";

        int STATUS_PENDING = 0;
        int STATUS_ACCEPTED = 1;
        int STATUS_REJECTED = 2;
    }

    /**
     * This table contains the invitations received from others.
     */
    public final static class Invitation implements InvitationColumns,
            BaseColumns {
        private Invitation() {
        }

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
            Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/invitations");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of
         * invitations.
         */
        public static final String CONTENT_TYPE =
            "vnd.android.cursor.dir/imps-invitations";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
         * invitation.
         */
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/imps-invitations";
    }
    
    /**
     * Columns from the FileTransfer table.
     */
    public interface FileTransferColumns {
        /**
         * The request id.
         * <p>Type: TEXT</p>
         */
        String STREAM_ID = "streamId";

        /**
         * The name of the requestor of the request.
         * <p>Type: TEXT</p>
         */
        String PEER = "peer";

        /**
         * The file path.
         * <p>Type: TEXT</p>
         */
        String FILE_PATH = "filePath";

        /**
         * The file name.
         * <p>Type: TEXT</p>
         */
        String FILENAME = "fileName";

        /**
         * The mime type
         * <p>Type: TEXT</p>
         */
        String MIME_TYPE = "mimeType";

        /**
         * The description (if any)
         * <p>Type: TEXT</p>
         */
        String DESCRIPTION = "description";

        /**
         * The file size.
         * <p>Type: TEXT</p>
         */
        String FILE_SIZE = "fileSize";

        /**
         * The amount written so far.
         * <p>Type: TEXT</p>
         */
        String AMOUNT_WRITTEN = "amountWritten";

        /**
         * The amount written so far.
         * <p>Type: TEXT</p>
         */
        String STATUS = "status";
    }

    /**
     * This table contains the file transfer requests received.
     */
    public final static class FileTransfer implements FileTransferColumns, BaseColumns
    {
        private FileTransfer()
        {
        }

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
            Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/file-transfer");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of
         * invitations.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-file-transfer";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
         * invitation.
         */
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/imps-file-transfer";
    }

    /**
     * Columns from the Avatars table
     */
    public interface AvatarsColumns {
        /**
         * The contact this avatar belongs to
         * <P>Type: TEXT</P>
         */
        String CONTACT = "contact";

        /**
         * The hash of the image data
         * <P>Type: TEXT</P>
         */
        String HASH = "hash";

        /**
         * raw image data
         * <P>Type: BLOB</P>
         */
        String DATA = "data";
    }

    /**
     * This table contains avatars.
     */
    public static final class Avatars implements BaseColumns, AvatarsColumns {
        /**
         * no public constructor since this is a utility class
         */
        private Avatars() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/avatars");

        /**
         * The content:// style URL for avatars by contact
         */
        public static final Uri CONTENT_URI_AVATARS_BY =
                Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/avatarsBy");

        /**
         * The MIME type of {@link #CONTENT_URI} providing the avatars
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-avatars";

        /**
         * The MIME type of a {@link #CONTENT_URI}
         */
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/imps-avatars";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "contact ASC";

    }

    /**
     * Common presence columns shared between the IM and contacts presence tables
     */
    public interface CommonPresenceColumns {
        /**
         * The priority, an integer, used by XMPP presence
         * <P>Type: INTEGER</P>
         */
        String PRIORITY = "priority";

        /**
         * The server defined status.
         * <P>Type: INTEGER (one of the values below)</P>
         */
        String PRESENCE_STATUS = "mode";

        /**
         * Presence Status definition
         */
        int OFFLINE = 0;
        int INVISIBLE = 1;
        int AWAY = 2;
        int IDLE = 3;
        int DO_NOT_DISTURB = 4;
        int AVAILABLE = 5;

        /**
         * The user defined status line.
         * <P>Type: TEXT</P>
         */
        String PRESENCE_CUSTOM_STATUS = "status";
    }

    /**
     * Columns from the Presence table.
     */
    public interface PresenceColumns extends CommonPresenceColumns {
        /**
         * The contact id
         * <P>Type: INTEGER</P>
         */
        String CONTACT_ID = "contact_id";

        /**
         * The contact's JID resource, only relevant for XMPP contact
         * <P>Type: TEXT</P>
         */
        String JID_RESOURCE = "jid_resource";

        /**
         * The contact's client type
         */
        String CLIENT_TYPE = "client_type";

        /**
         * client type definitions
         */
        int CLIENT_TYPE_DEFAULT = 0;
        int CLIENT_TYPE_MOBILE = 1;
        int CLIENT_TYPE_ANDROID = 2;
    }

    /**
     * Contains presence infomation for contacts.
     */
    public static final class Presence implements BaseColumns, PresenceColumns {
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/presence");

        /**
         * The content:// style URL for operations on bulk contacts
         */
        public static final Uri BULK_CONTENT_URI = Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/bulk_presence");

        /**
         * The MIME type of a {@link #CONTENT_URI} providing a directory of presence
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-presence";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "mode DESC";
    }

    /**
     * Columns from the Chats table.
     */
    public interface ChatsColumns {
        /**
         * The contact ID this chat belongs to. The value is a long.
         * <P>Type: INT</P>
         */
        String CONTACT_ID = "contact_id";

        /**
         * The GTalk JID resource. The value is a string.
         * <P>Type: TEXT</P>
         */
        String JID_RESOURCE = "jid_resource";

        /**
         * Whether this is a groupchat or not.
         * <P>Type: INT</P>
         */
        String GROUP_CHAT = "groupchat";

        /**
         * The last unread message. This both indicates that there is an
         * unread message, and what the message is.
         * <P>Type: TEXT</P>
         */
        String LAST_UNREAD_MESSAGE = "last_unread_message";

        /**
         * The last message timestamp
         * <P>Type: INT</P>
         */
        String LAST_MESSAGE_DATE = "last_message_date";

        /**
         * A message that is being composed.  This indicates that there was a
         * message being composed when the chat screen was shutdown, and what the
         * message is.
         * <P>Type: TEXT</P>
         */
        String UNSENT_COMPOSED_MESSAGE = "unsent_composed_message";

        /**
         * A value from 0-9 indicating which quick-switch chat screen slot this
         * chat is occupying.  If none (for instance, this is the 12th active chat)
         * then the value is -1.
         * <P>Type: INT</P>
         */
        String SHORTCUT = "shortcut";
    }

    /**
     * Contains ongoing chat sessions.
     */
    public static final class Chats implements BaseColumns, ChatsColumns {
        /**
         * no public constructor since this is a utility class
         */
        private Chats() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
            Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/chats");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of chats.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-chats";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single chat.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/imps-chats";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "last_message_date ASC";
    }

    /**
     * Columns from session cookies table. Used for IMPS.
     */
    public static interface SessionCookiesColumns {
        String NAME = "name";
        String VALUE = "value";
    }

    /**
     * Contains IMPS session cookies.
     */
    public static class SessionCookies implements SessionCookiesColumns, BaseColumns {
        private SessionCookies() {
        }

        /**
         * The content:// style URI for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/sessionCookies");


        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of
         * people.
         */
        public static final String CONTENT_TYPE = "vnd.android-dir/imps-sessionCookies";
    }

    /**
     * //TODO: move these to MCS specific provider.
     * The following are MCS stuff, and should really live in a separate provider specific to
     * MCS code.
     */

    /**
     * Columns from OutgoingRmq table
     */
    public interface OutgoingRmqColumns {
        String RMQ_ID = "rmq_id";
        String TIMESTAMP = "ts";
        String DATA = "data";
        String PROTOBUF_TAG = "type";
    }

    /**
     * //TODO: we should really move these to their own provider and database.
     * The table for storing outgoing rmq packets.
     */
    public static final class OutgoingRmq implements BaseColumns, OutgoingRmqColumns {
        private static String[] RMQ_ID_PROJECTION = new String[] {
                RMQ_ID,
        };

        /**
         * queryHighestRmqId
         *
         * @param resolver the content resolver
         * @return the highest rmq id assigned to the rmq packet, or 0 if there are no rmq packets
         *         in the OutgoingRmq table.
         */
        public static final long queryHighestRmqId(ContentResolver resolver) {
            Cursor cursor = resolver.query(Imps.OutgoingRmq.CONTENT_URI_FOR_HIGHEST_RMQ_ID,
                    RMQ_ID_PROJECTION,
                    null, // selection
                    null, // selection args
                    null  // sort
                    );

            long retVal = 0;
            try {
                //if (DBG) log("initializeRmqid: cursor.count= " + cursor.count());

                if (cursor.moveToFirst()) {
                    retVal = cursor.getLong(cursor.getColumnIndexOrThrow(RMQ_ID));
                }
            } finally {
                cursor.close();
            }

            return retVal;
        }

        /**
         * The content:// style URL for this table.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/outgoingRmqMessages");

        /**
         * The content:// style URL for the highest rmq id for the outgoing rmq messages
         */
        public static final Uri CONTENT_URI_FOR_HIGHEST_RMQ_ID =
                Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/outgoingHighestRmqId");

        /**
         * The default sort order for this table.
         */
        public static final String DEFAULT_SORT_ORDER = "rmq_id ASC";
    }

    /**
     * Columns for the LastRmqId table, which stores a single row for the last client rmq id
     * sent to the server.
     */
    public interface LastRmqIdColumns {
        String RMQ_ID = "rmq_id";
    }

    /**
     * //TODO: move these out into their own provider and database
     * The table for storing the last client rmq id sent to the server.
     */
    public static final class LastRmqId implements BaseColumns, LastRmqIdColumns {
        private static String[] PROJECTION = new String[] {
                RMQ_ID,
        };

        /**
         * queryLastRmqId
         *
         * queries the last rmq id saved in the LastRmqId table.
         *
         * @param resolver the content resolver.
         * @return the last rmq id stored in the LastRmqId table, or 0 if not found.
         */
        public static final long queryLastRmqId(ContentResolver resolver) {
            Cursor cursor = resolver.query(Imps.LastRmqId.CONTENT_URI,
                    PROJECTION,
                    null, // selection
                    null, // selection args
                    null  // sort
                    );

            long retVal = 0;
            try {
                if (cursor.moveToFirst()) {
                    retVal = cursor.getLong(cursor.getColumnIndexOrThrow(RMQ_ID));
                }
            } finally {
                cursor.close();
            }

            return retVal;
        }

        /**
         * saveLastRmqId
         *
         * saves the rmqId to the lastRmqId table. This will override the existing row if any,
         * as we only keep one row of data in this table.
         *
         * @param resolver the content resolver.
         * @param rmqId the rmq id to be saved.
         */
        public static final void saveLastRmqId(ContentResolver resolver, long rmqId) {
            ContentValues values = new ContentValues();

            // always replace the first row.
            values.put(_ID, 1);
            values.put(RMQ_ID, rmqId);
            resolver.insert(CONTENT_URI, values);
        }

        /**
         * The content:// style URL for this table.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/lastRmqId");
    }

    /**
     * Columns for the s2dRmqIds table, which stores the server-to-device message
     * persistent ids. These are used in the RMQ2 protocol, where in the login request, the
     * client selective acks these s2d ids to the server.
     */
    public interface ServerToDeviceRmqIdsColumn {
        String RMQ_ID = "rmq_id";
    }

    public static final class ServerToDeviceRmqIds implements BaseColumns,
            ServerToDeviceRmqIdsColumn {

        /**
         * The content:// style URL for this table.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://info.guardianproject.otr.app.im.provider.Imps/s2dids");
    }

}
