package com.futureconcepts.drake.ui.app;

import com.futureconcepts.drake.client.Contact;
import com.futureconcepts.drake.client.DrakeIntent;
import com.futureconcepts.drake.client.IChatGroupManager;
import com.futureconcepts.drake.client.IChatListener;
import com.futureconcepts.drake.client.IChatSession;
import com.futureconcepts.drake.client.IOtrChatSession;
import com.futureconcepts.drake.client.ImErrorInfo;
import com.futureconcepts.drake.client.Imps;
import com.futureconcepts.drake.client.Message;
import com.futureconcepts.drake.client.MessengerServiceConnection;
import com.futureconcepts.drake.client.constants.ImServiceConstants;
import com.futureconcepts.drake.ui.R;
import com.futureconcepts.drake.ui.os.SimpleAlertHandler;
import com.futureconcepts.drake.ui.utils.ChatSwitcher;
import com.futureconcepts.drake.ui.utils.StringUtils;
import com.futureconcepts.drake.ui.widget.ChatView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class NewChatActivity extends Activity implements
		MessengerServiceConnection.Client
{
	private static final String LOG_TAG = NewChatActivity.class.getSimpleName();
	private static final int REQUEST_PICK_CONTACTS = RESULT_FIRST_USER + 1;
	private static final int REQUEST_PICK_FILE = RESULT_FIRST_USER + 2;
	private static final int REQUEST_PICK_IMAGE = RESULT_FIRST_USER + 3;
	private static final int REQUEST_PICK_VIDEO = RESULT_FIRST_USER + 4;

	private MessengerServiceConnection _serviceConnection;

	private ChatView mChatView;
	private SimpleAlertHandler mHandler;

	private MenuItem menuOtr;

	private AlertDialog mSmileyDialog;
	private ChatSwitcher mChatSwitcher;

	private LayoutInflater mInflater;
	
	private Intent _initialIntent;
	
	private BroadcastReceiver _broadcastReceiver;

	@Override
	protected void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.chat_view);

		mChatView = (ChatView) findViewById(R.id.chatView);
		mHandler = mChatView.getHandler();
		mInflater = LayoutInflater.from(this);

		_initialIntent = getIntent();
		
		_serviceConnection = new MessengerServiceConnection(this, this);
		_serviceConnection.connect();
		
		_broadcastReceiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent)
			{
				String action = intent.getAction();
				if (action != null)
				{
					if (action.equals(DrakeIntent.EVENT_CONNECTION_LOGGED_IN))
					{
				        Log.d(LOG_TAG, "Connection resumed");
				        mChatView.updateWarningView();
					}
					else if (action.equals(DrakeIntent.EVENT_CONNECTION_SUSPENDED))
					{
						Log.d(LOG_TAG, "Connection suspended");
						mChatView.updateWarningView();
					}
				}
			}
		};
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DrakeIntent.EVENT_CONNECTION_LOGGED_IN);
        intentFilter.addAction(DrakeIntent.EVENT_CONNECTION_SUSPENDED);
        registerReceiver(_broadcastReceiver, intentFilter);
	}

	@Override
	public void onMessengerServiceConnected()
	{
		mChatView.setServiceConnection(_serviceConnection);
		mChatSwitcher = new ChatSwitcher(this, mHandler, mInflater, null);
		resolveIntent(_initialIntent);
	}

	@Override
	public void onMessengerServiceDisconnected()
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void onResume()
	{
		super.onResume();
		mChatView.onResume();
	}

	@Override
	protected void onPause()
	{
		mChatView.onPause();
		super.onPause();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (_serviceConnection != null)
		{
			_serviceConnection.disconnect();
		}
		unregisterReceiver(_broadcastReceiver);
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		resolveIntent(intent);
	}

	void resolveIntent(Intent intent)
	{
		if (requireOpenDashboardOnStart(intent))
		{
			mChatSwitcher.open();
			return;
		}

		if (ImServiceConstants.ACTION_MANAGE_SUBSCRIPTION.equals(intent
				.getAction()))
		{
			String from = intent
					.getStringExtra(ImServiceConstants.EXTRA_INTENT_FROM_ADDRESS);
			if (from == null)
			{
				finish();
			}
			else
			{
				mChatView.bindSubscription(from);
			}
		}
		else
		{
			Uri data = intent.getData();
			String type = getContentResolver().getType(data);
			if (Imps.Chats.CONTENT_ITEM_TYPE.equals(type))
			{
				mChatView.bindChat(ContentUris.parseId(data));
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.chat_screen_menu, menu);

//		menuOtr = menu.findItem(R.id.menu_view_otr);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);
		if (mChatView.isGroupChat())
		{
			// menu.findItem(R.id.menu_send_file).setVisible(false);
//			menu.findItem(R.id.menu_send_video).setVisible(false);
//			menu.findItem(R.id.menu_send_image).setVisible(false);
			menu.findItem(R.id.menu_show_participants).setVisible(true);
		}
		else
		{
			// menu.findItem(R.id.menu_send_file).setVisible(true);
//			menu.findItem(R.id.menu_send_video).setVisible(true);
//			menu.findItem(R.id.menu_send_image).setVisible(true);
			menu.findItem(R.id.menu_show_participants).setVisible(false);
		}
		updateOtrMenuState();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{

		// case R.id.menu_send_file:
		// onMenuSendFile();
		// return true;
//		case R.id.menu_send_image:
//			onMenuSendImage();
//			return true;
//		case R.id.menu_send_video:
//			onMenuSendVideo();
//			return true;
//		case R.id.menu_view_otr:
//			switchOtrState();
//			return true;

		case R.id.menu_view_profile:
			mChatView.viewProfile();
			return true;

		case R.id.menu_show_participants:
			onMenuShowParticipants();
			return true;

			/*
			 * case R.id.menu_view_friend_list: finish(); showRosterScreen();
			 * return true;
			 */
		case R.id.menu_end_conversation:
			mChatView.closeChatSession();
			finish();
			return true;

		case R.id.menu_switch_chats:
			if (mChatSwitcher.isOpen())
			{
				mChatSwitcher.close();
			}
			else
			{
				mChatSwitcher.open();
			}

			return true;

			/*
			 * case R.id.menu_invite_contact: startContactPicker(); return true;
			 * 
			 * 
			 * case R.id.menu_block_contact: mChatView.blockContact(); return
			 * true;
			 */
		case R.id.menu_prev_chat:
			switchChat(-1);
			return true;

		case R.id.menu_next_chat:
			switchChat(1);
			return true;

		case R.id.menu_quick_switch_0:
		case R.id.menu_quick_switch_1:
		case R.id.menu_quick_switch_2:
		case R.id.menu_quick_switch_3:
		case R.id.menu_quick_switch_4:
		case R.id.menu_quick_switch_5:
		case R.id.menu_quick_switch_6:
		case R.id.menu_quick_switch_7:
		case R.id.menu_quick_switch_8:
		case R.id.menu_quick_switch_9:
			mChatSwitcher.handleShortcut(item.getAlphabeticShortcut());
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN)
		{
			mChatView.closeChatSessionIfInactive();
		}
		return super.dispatchKeyEvent(event);
	}

	/**
	 * Check whether we are asked to open Dashboard on startup.
	 */
	private boolean requireOpenDashboardOnStart(Intent intent)
	{
		return intent.getBooleanExtra(
				ImServiceConstants.EXTRA_INTENT_SHOW_MULTIPLE, false);
	}

	private void onMenuSendFile()
	{
		Intent i = new Intent("org.openintents.action.PICK_FILE");
		try
		{
			startActivityForResult(i, REQUEST_PICK_FILE);
		}
		catch (Exception e)
		{
			mHandler.showAlert("Error launching file picker", e.getMessage());
		}
	}

	private void onMenuSendImage()
	{
		Intent i = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		try
		{
			startActivityForResult(i, REQUEST_PICK_IMAGE);
		}
		catch (Exception e)
		{
			mHandler.showAlert("Error launching image picker", e.getMessage());
		}
	}

	private void onMenuSendVideo()
	{
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.setType("video/*");
		try
		{
			startActivityForResult(i, REQUEST_PICK_VIDEO);
		}
		catch (Exception e)
		{
			mHandler.showAlert("Error launching video picker", e.getMessage());
		}
	}

	private void switchOtrState()
	{
		// TODO OTRCHAT switch state on/off

		IOtrChatSession otrChatSession = mChatView.getOtrChatSession();
		int toastMsgId;

		try
		{
			boolean isOtrEnabled = otrChatSession.isChatEncrypted();
			if (!isOtrEnabled)
			{
				otrChatSession.startChatEncryption();
				toastMsgId = R.string.starting_otr_chat;
			}
			else
			{
				otrChatSession.stopChatEncryption();
				toastMsgId = R.string.stopping_otr_chat;
			}
			Toast.makeText(this, getString(toastMsgId), Toast.LENGTH_SHORT)
					.show();
		}
		catch (RemoteException e)
		{
			Log.d("Messenger", "error getting remote activity", e);
		}
	}

	private void updateOtrMenuState()
	{
		IChatSession chatSession = mChatView.getCurrentChatSession();
		boolean isGroupChatSession = false;
		try
		{
			isGroupChatSession = chatSession.isGroupChatSession();
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
		}
		if (isGroupChatSession)
		{
//			menuOtr.setVisible(false);
		}
		else
		{
//			menuOtr.setVisible(true);
//			IOtrChatSession otrChatSession = mChatView.getOtrChatSession();
//			if (otrChatSession != null)
//			{
//				try
//				{
//					boolean isOtrEnabled = otrChatSession.isChatEncrypted();
//					if (isOtrEnabled)
//					{
//						menuOtr.setTitle(R.string.menu_otr_stop);
//					}
//					else
//					{
//						menuOtr.setTitle(R.string.menu_otr_start);
//					}
//				}
//				catch (RemoteException e)
//				{
//					Log.d("NewChat", "Error accessing remote service", e);
//				}
//			}
//			else
//			{
//				menuOtr.setTitle(R.string.menu_otr_start);
//			}
		}
		mChatView.updateWarningView();
	}

	private void showRosterScreen()
	{
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setClass(this, ContactListActivity.class);
		startActivity(intent);
	}

	private void showSmileyDialog()
	{
		if (mSmileyDialog == null)
		{
			int[] icons = getResources().getIntArray(R.array.smiley_names);
			String[] names = getResources()
					.getStringArray(R.array.smiley_names);
			final String[] texts = getResources().getStringArray(
					R.array.default_smiley_texts);

			final int N = names.length;

			List<Map<String, ?>> entries = new ArrayList<Map<String, ?>>();
			for (int i = 0; i < N; i++)
			{
				// We might have different ASCII for the same icon, skip it if
				// the icon is already added.
				boolean added = false;
				for (int j = 0; j < i; j++)
				{
					if (icons[i] == icons[j])
					{
						added = true;
						break;
					}
				}
				if (!added)
				{
					HashMap<String, Object> entry = new HashMap<String, Object>();

					entry.put("icon", icons[i]);
					entry.put("name", names[i]);
					entry.put("text", texts[i]);

					entries.add(entry);
				}
			}

			final SimpleAdapter a = new SimpleAdapter(this, entries,
					R.layout.smiley_menu_item, new String[] { "icon", "name",
							"text" }, new int[] { R.id.smiley_icon,
							R.id.smiley_name, R.id.smiley_text });
			SimpleAdapter.ViewBinder viewBinder = new SimpleAdapter.ViewBinder()
			{
				public boolean setViewValue(View view, Object data,
						String textRepresentation)
				{
					if (view instanceof ImageView)
					{
						Drawable img = getResources().getDrawable(
								(Integer) data);
						((ImageView) view).setImageDrawable(img);
						return true;
					}
					return false;
				}
			};
			a.setViewBinder(viewBinder);

			AlertDialog.Builder b = new AlertDialog.Builder(this);

			b.setTitle(getString(R.string.menu_insert_smiley));

			b.setCancelable(true);
			b.setAdapter(a, new DialogInterface.OnClickListener()
			{
				public final void onClick(DialogInterface dialog, int which)
				{
					HashMap<String, Object> item = (HashMap<String, Object>) a
							.getItem(which);
					mChatView.insertSmiley((String) item.get("text"));
				}
			});

			mSmileyDialog = b.create();
		}

		mSmileyDialog.show();
	}

	private void switchChat(int delta)
	{
		String contact = mChatView.getUserName();

		mChatSwitcher.rotateChat(delta, contact);
	}

	private void startContactPicker()
	{
		try
		{
			Intent i = new Intent(Intent.ACTION_PICK,
					Imps.Contacts.CONTENT_URI_ONLINE_CONTACTS_BY);
			i.putExtra(ContactsPickerActivity.EXTRA_EXCLUDED_CONTACTS,
					mChatView.getCurrentChatSession().getPariticipants());
			startActivityForResult(i, REQUEST_PICK_CONTACTS);
		}
		catch (RemoteException e)
		{
			mHandler.showServiceErrorAlert();
		}
	}

	private void onMenuShowParticipants()
	{
		AlertDialog dialog = null;
		IChatSession chatSession = mChatView.getCurrentChatSession();
		if (chatSession != null)
		{
			try
			{
				String[] participants = chatSession.getPariticipants();
				final int N = participants.length;
				List<Map<String, String>> entries = new ArrayList<Map<String, String>>();
				for (int i = 0; i < N; i++)
				{
					HashMap<String, String> entry = new HashMap<String, String>();
					entry.put("participant",
							StringUtils.parseResource(participants[i]));
					entry.put("jid", "");
					entries.add(entry);
				}
				final SimpleAdapter a = new SimpleAdapter(this, entries,
						R.layout.participant_list_item, new String[] {
								"participant", "jid" }, new int[] {
								R.id.participant, R.id.jid });
				// SimpleAdapter.ViewBinder viewBinder = new
				// SimpleAdapter.ViewBinder()
				// {
				// public boolean setViewValue(View view, Object data, String
				// textRepresentation)
				// {
				// if (view instanceof ImageView)
				// {
				// Drawable img = getResources().getDrawable((Integer)data);
				// ((ImageView)view).setImageDrawable(img);
				// return true;
				// }
				// return false;
				// }
				// };
				// a.setViewBinder(viewBinder);
				AlertDialog.Builder b = new AlertDialog.Builder(this);
				b.setTitle("Participants");
				b.setCancelable(true);
				b.setAdapter(a, new DialogInterface.OnClickListener()
				{
					public final void onClick(DialogInterface dialog, int which)
					{
						// HashMap<String, Object> item = (HashMap<String,
						// Object>) a.getItem(which);
						// mChatView.insertSmiley((String)item.get("text"));
					}
				});
				dialog = b.create();
				dialog.show();
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == RESULT_OK)
		{
			if (requestCode == REQUEST_PICK_CONTACTS)
			{
				String username = data
						.getStringExtra(ContactsPickerActivity.EXTRA_RESULT_USERNAME);
				try
				{
					IChatSession chatSession = mChatView
							.getCurrentChatSession();
					if (chatSession.isGroupChatSession())
					{
						IChatGroupManager cgm = _serviceConnection
								.getConnection().getChatGroupManager();
						// chatSession.inviteContact(username);
						showInvitationHasSent(username);
					}
					else
					{
						chatSession.convertToGroupChat();
						new ContactInvitor(chatSession, username).start();
					}
				}
				catch (RemoteException e)
				{
					mHandler.showServiceErrorAlert();
				}
			}
			else if (requestCode == REQUEST_PICK_FILE)
			{
			//	try
				{
					IChatSession chatSession = mChatView
							.getCurrentChatSession();
					String path = data.getDataString();
				//	chatSession.sendFile(path);
				}
			//	catch (RemoteException e)
			//	{
			//		mHandler.showServiceErrorAlert();
			//	}
			}
			else if (requestCode == REQUEST_PICK_IMAGE)
			{
//				try
				{
					IChatSession chatSession = mChatView
							.getCurrentChatSession();
					Uri selectedImage = data.getData();
					String[] filePathColumn = { MediaStore.Images.Media.DATA };
					Cursor cursor = getContentResolver().query(selectedImage,
							filePathColumn, null, null, null);
					cursor.moveToFirst();
					int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
					String filePath = cursor.getString(columnIndex);
					cursor.close();
				//	chatSession.sendFile(filePath);
				}
	//			catch (RemoteException e)
	//			{
	//				mHandler.showServiceErrorAlert();
	//			}
			}
			else if (requestCode == REQUEST_PICK_VIDEO)
			{
//				try
//				{
//					IChatSession chatSession = mChatView
//							.getCurrentChatSession();
//					Uri selectedVideo = data.getData();
//					String[] filePathColumn = { MediaStore.Video.Media.DATA };
//					Cursor cursor = getContentResolver().query(selectedVideo,
//							filePathColumn, null, null, null);
//					cursor.moveToFirst();
//					int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//					String filePath = cursor.getString(columnIndex);
//					cursor.close();
//				//	chatSession.sendFile(filePath);
//				}
//				catch (RemoteException e)
//				{
//					mHandler.showServiceErrorAlert();
//				}
			}
		}
	}

	private String getRealVideoPathFromURI(Uri contentUri)
	{
		String result = null;
		String[] proj = { MediaStore.Video.Media.DATA };
		Cursor cursor = managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		result = cursor.getString(column_index);
		return result;
	}

	void showInvitationHasSent(String contact)
	{
		Toast.makeText(NewChatActivity.this,
				getString(R.string.invitation_sent_prompt, contact),
				Toast.LENGTH_SHORT).show();
	}

	private class ContactInvitor extends IChatListener.Stub
	{
		private final IChatSession mChatSession;
		String mContact;

		public ContactInvitor(IChatSession session, String data)
		{
			mChatSession = session;
			mContact = data;
		}

		@Override
		public void onConvertedToGroupChat(IChatSession ses)
		{
			try
			{
				final long chatId = mChatSession.getId();
				// mChatSession.inviteContact(mContact);
				mHandler.post(new Runnable()
				{
					public void run()
					{
						mChatView.bindChat(chatId);
						showInvitationHasSent(mContact);
					}
				});
				mChatSession.unregisterChatListener(this);
			}
			catch (RemoteException e)
			{
				mHandler.showServiceErrorAlert();
			}
		}

		public void start() throws RemoteException
		{
			mChatSession.registerChatListener(this);
		}

		@Override
		public void onIncomingMessage(IChatSession ses, Message msg)
				throws RemoteException
		{
		}

		@Override
		public void onSendMessageError(IChatSession ses, Message msg,
				ImErrorInfo error) throws RemoteException
		{
		}

		@Override
		public void onContactJoined(IChatSession ses, Contact contact)
				throws RemoteException
		{
		}

		@Override
		public void onContactLeft(IChatSession ses, Contact contact)
				throws RemoteException
		{
		}

		@Override
		public void onInviteError(IChatSession ses, ImErrorInfo error)
				throws RemoteException
		{
		}

		@Override
		public void onIncomingReceipt(IChatSession ses, String packetId)
				throws RemoteException
		{
		}

		@Override
		public void onStatusChanged(IChatSession ses) throws RemoteException
		{
		}
	}
}
