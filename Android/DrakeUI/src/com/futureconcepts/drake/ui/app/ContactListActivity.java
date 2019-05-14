package com.futureconcepts.drake.ui.app;

import com.futureconcepts.drake.client.DrakeIntent;
import com.futureconcepts.drake.client.IImConnection;
import com.futureconcepts.drake.client.Imps;
import com.futureconcepts.drake.client.MessengerServiceConnection;
import com.futureconcepts.drake.ui.R;
import com.futureconcepts.drake.ui.settings.Settings;
import com.futureconcepts.drake.ui.widget.ContactListFilterView;
import com.futureconcepts.drake.ui.widget.ContactListView;

import android.app.Activity;
import android.app.SearchManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

public class ContactListActivity extends Activity implements
		View.OnCreateContextMenuListener,
		OnSharedPreferenceChangeListener,
		MessengerServiceConnection.Client
{
	private static final String LOG_TAG = ContactListActivity.class.getSimpleName();

	private static final int MENU_START_CONVERSATION = Menu.FIRST;
	private static final int MENU_VIEW_PROFILE = Menu.FIRST + 1;
	private static final int MENU_BLOCK_CONTACT = Menu.FIRST + 2;
	private static final int MENU_DELETE_CONTACT = Menu.FIRST + 3;
	private static final int MENU_END_CONVERSATION = Menu.FIRST + 4;

	private static final String FILTER_STATE_KEY = "Filtering";

	private MessengerServiceConnection _serviceConnection;
	IImConnection mConn;
	public ContactListView mContactListView; // TODO public property?
	ContactListFilterView mFilterView;

	ContextMenuHandler mContextMenuHandler;

	boolean mIsFiltering;

	private Intent _initialIntent;
	
	private BroadcastReceiver _connectionReceiver;

	@Override
	protected void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);

		// The following broadcast will be handled by RemoteImService.Receiver
		// Receiver will call startService on itself. RemoteImService.onStart()
		// will check to see if a connection has already been made and if so
		// just return.
		sendBroadcast(new Intent(DrakeIntent.ACTION_START_SERVICE));

		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);

		getWindow().requestFeature(Window.FEATURE_LEFT_ICON);

		LayoutInflater inflate = getLayoutInflater();

		mContactListView = (ContactListView) inflate.inflate(
				R.layout.contact_list_view, null);

		mFilterView = (ContactListFilterView) getLayoutInflater().inflate(
				R.layout.contact_list_filter_view, null);

		mFilterView.setActivity(this);

		mFilterView.getListView().setOnCreateContextMenuListener(this);

		setTitle(R.string.buddy_list_title);
		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				R.drawable.icon); // TODO: append username?

		_initialIntent = getIntent();

		_serviceConnection = new MessengerServiceConnection(this, this);
		_serviceConnection.connect();
		
		_connectionReceiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context arg0, Intent arg1)
			{
				if (mIsFiltering)
				{
					mFilterView.mPresenceView.setConnection(mConn);
				}
			}
		};
	}

	@Override
	public void onMessengerServiceConnected()
	{
		try
		{
			mConn = _serviceConnection.getConnection();
			mFilterView.mPresenceView.setConnection(mConn);
			mContactListView.setConnection(mConn);
			mContactListView.setHideOfflineContacts(false);
			mContextMenuHandler = new ContextMenuHandler();
			mContactListView.getListView().setOnCreateContextMenuListener(
					ContactListActivity.this);

			showFilterView();

			// Get the intent, verify the action and get the query

			if (Intent.ACTION_SEARCH.equals(_initialIntent.getAction()))
			{
				if (mIsFiltering)
				{
					String filterText = _initialIntent
							.getStringExtra(SearchManager.QUERY);
					mFilterView.doFilter(filterText);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			onServiceFailure();
		}
	}

	@Override
	public void onMessengerServiceDisconnected()
	{
		onServiceFailure();
	}

	private void onServiceFailure()
	{
		Log.e(LOG_TAG, "The connection has disappeared!");
		clearConnectionStatus();
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.contact_list_menu, menu);

		// TODO make sure this works
		// menu.findItem(R.id.menu_invite_user).setTitle(
		// brandingRes.getString(BrandingResourceIDs.STRING_MENU_ADD_CONTACT));

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		if (mIsFiltering)
		{
			menu.findItem(R.id.menu_view_groups).setTitle("Grouped");
		}
		else
		{
			menu.findItem(R.id.menu_view_groups).setTitle("Filtered");
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{

		// TODO make sure this works

		// case R.id.menu_invite_user:
		// Intent i = new Intent(this, AddContactActivity.class);
		// i.putExtra(ImServiceConstants.EXTRA_INTENT_LIST_NAME,
		// mContactListView.getSelectedContactList());
		// startActivity(i);
		// return true;

		/*
		 * case R.id.menu_blocked_contacts: Uri.Builder builder =
		 * Imps.BlockedList.CONTENT_URI.buildUpon();
		 * ContentUris.appendId(builder, mProviderId);
		 * ContentUris.appendId(builder, mAccountId); startActivity(new
		 * Intent(Intent.ACTION_VIEW, builder.build())); return true;
		 */

		case R.id.menu_settings:
			Intent sintent = new Intent(this, SettingActivity.class);
			startActivity(sintent);
			return true;

//		case R.id.menu_signout:
//			handleSignout();
//			return true;

		case R.id.menu_view_groups:
			if (mIsFiltering)
			{
				showContactListView();
			}
			else
			{
				showFilterView();
			}
			return true;
//		case R.id.menu_send_loc:
//			onMenuSendLocation();
//			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void onMenuSendLocation()
	{
		if (mConn != null)
		{
			LocationManager locMgr = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
			Location location = locMgr
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location != null)
			{
				try
				{
					mConn.publishLocation(location);
				}
				catch (RemoteException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

//	private void handleSignout()
//	{
//		try
//		{
//			if (mConn != null)
//			{
//				mConn.logout();
//			}
//			Intent intent = new Intent(Intent.ACTION_MAIN);
//			intent.addCategory(Intent.CATEGORY_HOME);
//			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			startActivity(intent);
//			finish();
//		}
//		catch (RemoteException e)
//		{
//			Log.e(LOG_TAG, e.getMessage());
//		}
//	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putBoolean(FILTER_STATE_KEY, mIsFiltering);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		boolean isFiltering = savedInstanceState.getBoolean(FILTER_STATE_KEY);
		if (isFiltering)
		{
			showFilterView();
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		int keyCode = event.getKeyCode();

		boolean handled = false;

		if (!mIsFiltering)
		{
			handled = mFilterView.dispatchKeyEvent(event);
			if (!handled && (KeyEvent.KEYCODE_BACK == keyCode)
					&& (KeyEvent.ACTION_DOWN == event.getAction()))
			{
				showFilterView();
				handled = true;
			}
		}
		else
		{
			handled = mContactListView.dispatchKeyEvent(event);

			if (!handled && KeyEvent.KEYCODE_SEARCH == keyCode
					&& (KeyEvent.ACTION_DOWN == event.getAction()))
			{
				InputMethodManager inputMgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMgr.toggleSoftInput(0, 0);

				if (!mIsFiltering)
				{
					showFilterView();
				}
				onSearchRequested();
			}
			else if (!handled && isReadable(keyCode, event)
					&& (KeyEvent.ACTION_DOWN == event.getAction()))
			{
				if (!mIsFiltering)
				{
					showFilterView();
				}
				handled = mFilterView.dispatchKeyEvent(event);
			}
		}
		if (!handled)
		{
			handled = super.dispatchKeyEvent(event);
		}
		return handled;
	}

	/*
	 * @Override public boolean onSearchRequested() { // Open up the search/go
	 * dialog startSearch("", true, null, false); return true; }
	 */

	@Override
	protected void onNewIntent(Intent intent)
	{
		// The user has probably entered a URL into "Go"
		String action = intent.getAction();
		if (Intent.ACTION_SEARCH.equals(action))
		{
			if (mIsFiltering)
			{
				String filterText = intent.getStringExtra(SearchManager.QUERY);
				mFilterView.doFilter(filterText);
			}
		}
	}

	private static boolean isReadable(int keyCode, KeyEvent event)
	{
		if (KeyEvent.isModifierKey(keyCode) || event.isSystem())
		{
			return false;
		}
		switch (keyCode)
		{
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_DPAD_DOWN:
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_ENTER:
			return false;
		}
		return true;
	}

	private void showFilterView()
	{
		boolean hideOfflineContacts = Settings.getHideOfflineContacts(this);
		Uri uri = hideOfflineContacts ? Imps.Contacts.CONTENT_URI_ONLINE_CONTACTS_BY
				: Imps.Contacts.CONTENT_URI;
		mFilterView.doFilter(uri, null);

		setContentView(mFilterView);
		mFilterView.requestFocus();
		mIsFiltering = true;
	}

	void showContactListView()
	{
		setContentView(mContactListView);
		mContactListView.requestFocus();
		mContactListView.invalidate();
		mIsFiltering = false;
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		mContactListView.setAutoRefreshContacts(true);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(DrakeIntent.EVENT_CONNECTION_CREATED);
		intentFilter.addAction(DrakeIntent.EVENT_CONNECTION_DISCONNECTED);
		intentFilter.addAction(DrakeIntent.EVENT_CONNECTION_LOGGED_IN);
		intentFilter.addAction(DrakeIntent.EVENT_CONNECTION_LOGGING_IN);
		intentFilter.addAction(DrakeIntent.EVENT_CONNECTION_LOGGING_OUT);
		intentFilter.addAction(DrakeIntent.EVENT_CONNECTION_SUSPENDED);
		intentFilter.addAction(DrakeIntent.EVENT_CONNECTION_SUSPENDING);
		registerReceiver(_connectionReceiver, intentFilter);
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		unregisterReceiver(_connectionReceiver);
	}

	@Override
	protected void onDestroy()
	{
		// set connection to null to unregister listeners.
		mContactListView.setConnection(null);
		_serviceConnection.disconnect();
		super.onDestroy();
	}

	static void log(String msg)
	{
		Log.v(LOG_TAG, "<ContactListActivity> " + msg);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo)
	{
		boolean chatSelected = false;
		boolean contactSelected = false;
		Cursor contactCursor;
		if (mIsFiltering)
		{
			AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			mContextMenuHandler.mPosition = info.position;
			contactSelected = true;
			contactCursor = mFilterView.getContactAtPosition(info.position);
		}
		else
		{
			ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
			mContextMenuHandler.mPosition = info.packedPosition;
			contactSelected = mContactListView
					.isContactAtPosition(info.packedPosition);
			contactCursor = mContactListView
					.getContactAtPosition(info.packedPosition);
		}

		boolean allowBlock = true;
		if (contactCursor != null)
		{
			int nickNameIndex = contactCursor
					.getColumnIndexOrThrow(Imps.Contacts.NICKNAME);
			menu.setHeaderTitle(contactCursor.getString(nickNameIndex));
		}
		String menu_end_conversation = getString(R.string.menu_end_conversation);
		String menu_view_profile = getString(R.string.menu_view_profile);
		String menu_block_contact = getString(R.string.menu_block_contact);
		String menu_start_conversation = getString(R.string.menu_start_chat);
//		String menu_delete_contact = getString(R.string.menu_remove_contact);

		if (chatSelected)
		{
			menu.add(0, MENU_END_CONVERSATION, 0, menu_end_conversation)
			// TODO
			// .setIcon(info.guardianproject.otr.app.internal.R.drawable.ic_menu_end_conversation)
					.setOnMenuItemClickListener(mContextMenuHandler);
			menu.add(0, MENU_VIEW_PROFILE, 0, menu_view_profile)
					.setIcon(R.drawable.ic_menu_my_profile)
					.setOnMenuItemClickListener(mContextMenuHandler);
			if (allowBlock)
			{
				menu.add(0, MENU_BLOCK_CONTACT, 0, menu_block_contact)
				// .setIcon(info.guardianproject.otr.app.internal.R.drawable.ic_menu_block)
						.setOnMenuItemClickListener(mContextMenuHandler);
			}
		}
		else if (contactSelected)
		{
			menu.add(0, MENU_START_CONVERSATION, 0, menu_start_conversation)
			// .setIcon(info.guardianproject.otr.app.internal.R.drawable.ic_menu_start_conversation)
					.setOnMenuItemClickListener(mContextMenuHandler);
			menu.add(0, MENU_VIEW_PROFILE, 0, menu_view_profile)
					.setIcon(R.drawable.ic_menu_view_profile)
					.setOnMenuItemClickListener(mContextMenuHandler);
//			if (allowBlock)
//			{
//				menu.add(0, MENU_BLOCK_CONTACT, 0, menu_block_contact)
//				// .setIcon(info.guardianproject.otr.app.internal.R.drawable.ic_menu_block)
//						.setOnMenuItemClickListener(mContextMenuHandler);
//			}
//			menu.add(0, MENU_DELETE_CONTACT, 0, menu_delete_contact)
//					.setIcon(android.R.drawable.ic_menu_delete)
//					.setOnMenuItemClickListener(mContextMenuHandler);
		}
	}

	void clearConnectionStatus()
	{
	}

	final class ContextMenuHandler implements MenuItem.OnMenuItemClickListener
	{
		long mPosition;

		public boolean onMenuItemClick(MenuItem item)
		{
			Cursor c;
			if (mIsFiltering)
			{
				c = mFilterView.getContactAtPosition((int) mPosition);
			}
			else
			{
				c = mContactListView.getContactAtPosition(mPosition);
			}

			switch (item.getItemId())
			{
			case MENU_START_CONVERSATION:
				mContactListView.startChat(c);
				break;
			case MENU_VIEW_PROFILE:
				mContactListView.viewContactPresence(c);
				break;
			case MENU_BLOCK_CONTACT:
				mContactListView.blockContact(c);
				break;
			case MENU_DELETE_CONTACT:
				mContactListView.removeContact(c);
				break;
			case MENU_END_CONVERSATION:
				mContactListView.endChat(c);
				break;
			default:
				return false;
			}

			if (mIsFiltering)
			{
				showContactListView();
			}
			return true;
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if (key.equals(Settings.KEY_HIDE_OFFLINE_CONTACTS))
		{
			if (mIsFiltering)
			{
				showFilterView();
			}
		}
	}
}
