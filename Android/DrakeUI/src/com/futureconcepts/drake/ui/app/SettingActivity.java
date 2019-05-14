package com.futureconcepts.drake.ui.app;

import com.futureconcepts.drake.ui.R;
import com.futureconcepts.drake.ui.settings.Settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.util.Log;
import android.widget.Toast;

public class SettingActivity extends android.preference.PreferenceActivity implements OnSharedPreferenceChangeListener
{
    private EditTextPreference mXmppResource;
    private ListPreference mOtrMode;
    private CheckBoxPreference mHideOfflineContacts;
    private CheckBoxPreference mEnableNotification;
    private CheckBoxPreference mNotificationVibrate;
    private CheckBoxPreference mNotificationSound;
    
    private void setInitialValues()
    {

        mOtrMode.setValue(Settings.getOtrMode(this));
        mHideOfflineContacts.setChecked(Settings.getHideOfflineContacts(this));
        mEnableNotification.setChecked(Settings.getEnableNotification(this));
        mNotificationVibrate.setChecked(Settings.getVibrate(this));
        mNotificationSound.setChecked(Settings.getRingtoneURI(this) != null);
    }

    /* save the preferences in Imps so they are accessible everywhere */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
    {
//    	String value = null;
//    	if (key.equals(getString(R.string.pref_security_otr_mode)))
//    	{
//    		Settings.setOtrMode(this, prefs.getString(key, "auto"));
//    	}
//    	else if (key.equals(getString(R.string.pref_hide_offline_contacts)))
//    	{
//    		Settings.setHideOfflineContacts(this, prefs.getBoolean(key, false));
//    	}
//    	else if (key.equals(getString(R.string.pref_enable_notification)))
//    	{
//    		Settings.setEnableNotification(this, prefs.getBoolean(key, true));
//    	}
//    	else if (key.equals(getString(R.string.pref_notification_vibrate)))
//    	{
//    		Settings.setVibrate(this, prefs.getBoolean(key, true));
//    	}
//    	else if (key.equals(getString(R.string.pref_notification_sound)))
//    	{
//    		// TODO sort out notification sound pref
//            if (prefs.getBoolean(key, false))
//            {
//            	Settings.setRingtoneURI(this, Settings.RINGTONE_DEFAULT);
//            }
//            else
//            {
//            	Settings.setRingtoneURI(this, null);
//            }
//    	}
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	addPreferencesFromResource(R.xml.preferences);
    	Intent intent = getIntent();
    
    	mHideOfflineContacts = (CheckBoxPreference) findPreference(getString(R.string.pref_hide_offline_contacts));
    	mXmppResource = (EditTextPreference) findPreference(getString(R.string.pref_account_xmpp_resource));
    	mOtrMode = (ListPreference) findPreference(getString(R.string.pref_security_otr_mode));
    	mEnableNotification = (CheckBoxPreference) findPreference(getString(R.string.pref_enable_notification));
    	mNotificationVibrate = (CheckBoxPreference) findPreference(getString(R.string.pref_notification_vibrate));
    	mNotificationSound = (CheckBoxPreference) findPreference(getString(R.string.pref_notification_sound));
    	// TODO re-enable Ringtone preference
    	//mNotificationRingtone = (CheckBoxPreference) findPreference(getString(R.string.pref_notification_ringtone));
    }

    @Override
    protected void onResume()
    {
    	super.onResume();

    	setInitialValues();
    	getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause()
    {
    	super.onPause();
    	getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
    }
}
