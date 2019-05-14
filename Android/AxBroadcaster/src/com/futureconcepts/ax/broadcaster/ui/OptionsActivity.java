package com.futureconcepts.ax.broadcaster.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.futureconcepts.ax.broadcaster.AxBroadcasterApplication;
import com.futureconcepts.ax.broadcaster.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

@SuppressWarnings("deprecation")
public class OptionsActivity extends PreferenceActivity
{
	private AxBroadcasterApplication mApplication = null;
	public static final String KEY_STREAMING_SOUND = "streaming_sound";

	private SharedPreferences _settings;
	
	private static final int SUBACTIVITY_TONE_PICKER = 1;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mApplication = (AxBroadcasterApplication) getApplication();

		addPreferencesFromResource(R.xml.preferences);

		_settings = PreferenceManager.getDefaultSharedPreferences(this);
		final Preference audioEnabled = findPreference("stream_audio");
		final ListPreference cameraSelection = (ListPreference) findPreference("camera");
		final ListPreference audioEncoder = (ListPreference) findPreference("audio_encoder");
		final ListPreference videoEncoder = (ListPreference) findPreference("video_encoder");
		final ListPreference videoResolution = (ListPreference) findPreference("video_resolution");
		final ListPreference videoBitrate = (ListPreference) findPreference("video_bitrate");
		final ListPreference videoFramerate = (ListPreference) findPreference("video_framerate");
		final ClickPreference streamingSound = (ClickPreference)findPreference(KEY_STREAMING_SOUND);

		videoResolution.setEnabled(true);
		videoBitrate.setEnabled(true);
		videoFramerate.setEnabled(true);
		if (audioEncoder != null)
		{
			audioEncoder.setEnabled(_settings.getBoolean("stream_audio", true));
			audioEncoder.setValue(String.valueOf(mApplication.audioEncoder));
		}
		cameraSelection.setValue(String.valueOf(_settings.getString("camera", "back")));
		videoFramerate.setValue(String.valueOf(mApplication.videoQuality.framerate));
		videoBitrate.setValue(String.valueOf(mApplication.videoQuality.bitrate/1000));
		videoResolution.setValue(mApplication.videoQuality.resX+"x"+mApplication.videoQuality.resY);

		cameraSelection.setSummary(cameraSelection.getValue());
		videoResolution.setSummary(getString(R.string.settings0)+" "+videoResolution.getValue()+"px");
		videoFramerate.setSummary(getString(R.string.settings1)+" "+videoFramerate.getValue()+"fps");
		videoBitrate.setSummary(getString(R.string.settings2)+" "+videoBitrate.getValue()+"kbps");
		streamingSound.setSummary(_settings.getString(KEY_STREAMING_SOUND, null));
		
		cameraSelection.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				cameraSelection.setSummary((String)newValue);
				return true;
			}
		});
		
		videoResolution.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				Editor editor = _settings.edit();
				Pattern pattern = Pattern.compile("([0-9]+)x([0-9]+)");
				Matcher matcher = pattern.matcher((String)newValue);
				matcher.find();
				editor.putInt("video_resX", Integer.parseInt(matcher.group(1)));
				editor.putInt("video_resY", Integer.parseInt(matcher.group(2)));
				editor.commit();
				videoResolution.setSummary(getString(R.string.settings0)+" "+(String)newValue+"px");
				return true;
			}
		});

		videoFramerate.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				videoFramerate.setSummary(getString(R.string.settings1)+" "+(String)newValue+"fps");
				return true;
			}
		});

		videoBitrate.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				videoBitrate.setSummary(getString(R.string.settings2)+" "+(String)newValue+"kbps");
				return true;
			}
		});
		streamingSound.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER); 
				intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
				startActivityForResult(intent, SUBACTIVITY_TONE_PICKER);
				return true;
			}
		});
		streamingSound.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				streamingSound.setSummary((String)newValue);
				return false;
			}
		});
		if (audioEnabled != null && audioEncoder != null)
		{
			audioEnabled.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
			{
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					boolean state = (Boolean)newValue;
					audioEncoder.setEnabled(state);
					return true;
				}
			});
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK)
		{
			if (requestCode == SUBACTIVITY_TONE_PICKER)
			{
				Editor editor = _settings.edit();
				Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
				if (uri != null)
				{
					editor.putString(KEY_STREAMING_SOUND, uri.toString());
				}
				else
				{
					editor.remove(KEY_STREAMING_SOUND);
				}
				editor.commit();
			}
		}
	}
}
