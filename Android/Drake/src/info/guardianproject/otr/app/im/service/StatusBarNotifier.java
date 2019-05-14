package info.guardianproject.otr.app.im.service;

import java.util.Collection;
import java.util.HashMap;

import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jsoup.Jsoup;

import com.futureconcepts.drake.R;
import com.futureconcepts.drake.client.FileTransferRequestParcel;
import com.futureconcepts.drake.client.ImErrorInfo;
import com.futureconcepts.drake.client.Imps;
import com.futureconcepts.drake.client.Imps.FileTransfer;
import com.futureconcepts.drake.settings.Settings;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;

public class StatusBarNotifier
{
    private static final boolean DBG = false;
    
    private static final int ID_INVITATION = 1;
    private static final int ID_SUBSCRIPTION = 2;
    private static final int ID_CHAT = 3;
    private static final int ID_FILE_TRANSFER = 4;
    private static final int ID_ERROR = 5;
    private static final int ID_CONNECTED = 3000;

    private static final long SUPPRESS_SOUND_INTERVAL_MS = 3000L;

    static final long[] VIBRATE_PATTERN = new long[] {0, 250, 250, 250};

    private Context mContext;
    private NotificationManager mNotificationManager;

    private Handler mHandler;
    private HashMap<Integer, NotificationInfo> mNotificationInfos;
    private long mLastSoundPlayedMs;

    public StatusBarNotifier(Context context)
    {
        mContext = context;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mHandler = new Handler();
        mNotificationInfos = new HashMap<Integer, NotificationInfo>();
    }

    public void onServiceStop()
    {
    	dismissNotifications();
    }

    public void notifyChat(long chatId, String username, String nickname, String msg, boolean lightWeightNotify)
    {
        msg = html2text(msg); // strip tags for html client inbound msgs
        
        String title = nickname;
        String snippet = mContext.getString(R.string.new_messages_notify) + ' ' + nickname;
        Intent intent = new Intent(Intent.ACTION_VIEW,ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, chatId));
//        intent.addCategory(info.guardianproject.otr.app.im.app.ImApp.IMPS_CATEGORY);
        notify(ID_CHAT, username, title, snippet, msg, intent, lightWeightNotify);
    }

    public void notifySubscriptionRequest(long contactId, String username, String nickname)
    {
        String title = nickname;
        String message = mContext.getString(R.string.subscription_notify_text, nickname);
        Intent intent = new Intent(ImServiceConstants.ACTION_MANAGE_SUBSCRIPTION,
                ContentUris.withAppendedId(Imps.Contacts.CONTENT_URI, contactId));
        intent.putExtra(ImServiceConstants.EXTRA_INTENT_FROM_ADDRESS, username);
        notify(ID_SUBSCRIPTION, username, title, message, message, intent, false);
    }

    public void notifyGroupInvitation(long invitationId, String username)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                ContentUris.withAppendedId(Imps.Invitation.CONTENT_URI, invitationId));

        String title = mContext.getString(R.string.notify_groupchat_label);
        String message = mContext.getString(R.string.group_chat_invite_notify_text, username);
        notify(ID_INVITATION, username, title, message, message, intent, false);
    }

    public void notifyConnected()
    {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.futureconcepts.drake.ui", "com.futureconcepts.drake.ui.app.ContactListActivity"));

        String title = mContext.getString(R.string.app_name);
        String message = mContext.getString(R.string.presence_available);
        notify(ID_CONNECTED, message, title, message, message, intent, false);
    }
    
    public void notifyDisconnected()
    {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.futureconcepts.drake.ui.app", "ContactListActivity"));

        String title = mContext.getString(R.string.app_name);
        String message = mContext.getString(R.string.presence_offline);
        notify(ID_CONNECTED, message, title, message, message, intent, false);
    }
    
    public void notifyFileTransferRequest(FileTransferRequestParcel fileTransferRequestParcel, Uri uri)
    {
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.putExtra(FileTransfer.STREAM_ID, fileTransferRequestParcel.getStreamID());
		intent.putExtra(FileTransfer.FILENAME, fileTransferRequestParcel.getFileName());
		intent.putExtra(FileTransfer.DESCRIPTION, fileTransferRequestParcel.getDescription());
		intent.putExtra(FileTransfer.PEER, fileTransferRequestParcel.getRequestor());
		String title = mContext.getString(R.string.notify_file_transfer_request_label);
		String message = mContext.getString(R.string.file_transfer_request_notify_text, fileTransferRequestParcel.getRequestor());
		notify(ID_FILE_TRANSFER, message, title, message, message, intent, false);
    }

    public void notifyError(ImErrorInfo error)
    {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.futureconcepts.drake.ui.app", "ContactListActivity"));
		notify(ID_ERROR, error.getDescription(), "Messenger Service Error", error.getDescription(), error.getDescription(), intent, false);
    }
    
    public void dismissNotifications()
    {
        synchronized (mNotificationInfos)
        {
        	Collection<NotificationInfo> notifications = mNotificationInfos.values();
        	for (NotificationInfo info : notifications)
        	{
        		int id = info.getId();
                mNotificationManager.cancel(id);
                mNotificationInfos.remove(id);
        	}
        }
    }

    public void dismissChatNotification(String username)
    {
        NotificationInfo info;
        boolean removed;
        synchronized (mNotificationInfos)
        {
            info = mNotificationInfos.get(ID_CHAT);
            if (info == null)
            {
                return;
            }
            removed = info.removeItem(username);
        }
        if (removed)
        {
            if (info.getMessage() == null)
            {
                if (DBG)
                {
                	log("dismissChatNotification: removed notification");
                }
                mNotificationManager.cancel(info.getId());
            }
            else
            {
                if (DBG)
                {
                    log("cancelNotify: new notification" +
                            " mTitle=" + info.getTitle() +
                            " mMessage=" + info.getMessage() +
                            " mIntent=" + info.getIntent());
                }
                mNotificationManager.notify(info.getId(), info.createNotification("", true));
            }
        }
    }

    private boolean isNotificationEnabled()
    {
    	return Settings.getEnableNotification(mContext);
    }

    private void notify(int id, String sender, String title, String tickerText, String message,
            Intent intent, boolean lightWeightNotify)
    {
        
    	intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

    	NotificationInfo info;
        
        synchronized (mNotificationInfos)
        {
            info = mNotificationInfos.get(id);
            if (info == null)
            {
                info = new NotificationInfo(id);
                mNotificationInfos.put(id, info);
            }
            info.addItem(sender, title, message, intent);
        }        
        mNotificationManager.notify(info.getId(), info.createNotification(tickerText, lightWeightNotify));
    }

    private void setRinger(Notification notification)
    {
        String ringtoneUri = Settings.getRingtoneURI(mContext);
        boolean vibrate = Settings.getVibrate(mContext);

        notification.sound = TextUtils.isEmpty(ringtoneUri) ? null : Uri.parse(ringtoneUri);
        if (notification.sound != null)
        {
            mLastSoundPlayedMs = SystemClock.elapsedRealtime();
        }
        if (DBG)
        {
        	log("setRinger: notification.sound = " + notification.sound);
        }
        if (vibrate)
        {
            notification.defaults |= Notification.DEFAULT_VIBRATE;
            if (DBG) log("setRinger: defaults |= vibrate");
        }
    }

    class NotificationInfo
    {
        class Item
        {
            String mTitle;
            String mMessage;
            Intent mIntent;

            public Item(String title, String message, Intent intent)
            {
                mTitle = title;
                mMessage = message;
                mIntent = intent;
            }
        }

        private HashMap<String, Item> mItems;

        private int mId;
        
        public NotificationInfo(int id)
        {
            mId = id;
            mItems = new HashMap<String, Item>();
        }

        public int getId()
        {
            return mId;
        }

        public synchronized void addItem(String sender, String title, String message, Intent intent)
        {
            Item item = mItems.get(sender);
            if (item == null)
            {
                item = new Item(title, message, intent);
                mItems.put(sender, item);
            }
            else
            {
                item.mTitle = title;
                item.mMessage = message;
                item.mIntent = intent;
            }
        }

        public synchronized boolean removeItem(String sender)
        {
            Item item =  mItems.remove(sender);
            if (item != null)
            {
                return true;
            }
            return false;
        }

        public Notification createNotification(String tickerText, boolean lightWeightNotify)
        {
            Notification notification = new Notification(
                    R.drawable.status,
                    lightWeightNotify ? null : tickerText,
                    System.currentTimeMillis());

            Intent intent = getIntent();

            notification.setLatestEventInfo(mContext, getTitle(), getMessage(),
                    PendingIntent.getActivity(mContext, 0, intent, 0));
            if (mId == ID_CONNECTED)
            {
            	notification.flags |= Notification.FLAG_ONGOING_EVENT;
            }
            else
            {
            	notification.flags |= Notification.FLAG_AUTO_CANCEL;
            }
            if (!(lightWeightNotify || shouldSuppressSoundNotification()))
            {
                setRinger(notification);
            }
            return notification;
        }

        private Intent getDefaultIntent()
        {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setType(Imps.Contacts.CONTENT_TYPE);
            intent.setComponent(new ComponentName("com.futureconcepts.drake.ui.app", "ContactListActivity"));            
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            return intent;
        }

        private Intent getMultipleNotificationIntent()
        {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setComponent(new ComponentName("com.futureconcepts.drake.ui.app", "NewChatActivity"));
            intent.putExtra(ImServiceConstants.EXTRA_INTENT_SHOW_MULTIPLE, true);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            return intent;
        }

        public String getTitle()
        {
            int count = mItems.size();
            if (count == 0)
            {
                return null;
            }
            else if (count == 1)
            {
                Item item = mItems.values().iterator().next();
                return item.mTitle;
            }
            else
            {
                return mContext.getString(R.string.newMessages_label);
            }
        }

        public String getMessage()
        {
            int count = mItems.size();
            if (count == 0)
            {
                return null;
            }
            else if (count == 1)
            {
                Item item = mItems.values().iterator().next();
                return item.mMessage;
            }
            else
            {
                return mContext.getString(R.string.num_unread_chats);
            }
        }

        public Intent getIntent()
        {
            int count = mItems.size();
            if (count == 0)
            {
                return getDefaultIntent();
            }
            else if (count == 1)
            {
                Item item = mItems.values().iterator().next();
                return item.mIntent;
            }
            else
            {
                return getMultipleNotificationIntent();
            }
        }
    }

    private static void log(String msg)
    {
        RemoteImService.debug( "[StatusBarNotify] " + msg);
    }

    private boolean shouldSuppressSoundNotification()
    {
        return (SystemClock.elapsedRealtime() - mLastSoundPlayedMs < SUPPRESS_SOUND_INTERVAL_MS);
    }
    
    public static String html2text(String html)
    {
    	return Jsoup.parse(html).text();
    }
}
