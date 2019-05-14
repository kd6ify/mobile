package com.futureconcepts.ax.rex.activities;

import org.joda.time.DateTime;

import com.futureconcepts.ax.model.data.IncidentRequest;
import com.futureconcepts.ax.model.data.IncidentType;
import com.futureconcepts.ax.rex.R;
import com.futureconcepts.ax.rex.activities.geo.MainMapActivity;
import com.futureconcepts.ax.rex.config.Config;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.app.Activity;
import android.content.Intent;

public class SelectTypeActivity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_type_activity);
        DateTime.setContext(this);
    }
    
    public void onLawClick(View view)
    {
    	next(IncidentRequest.createRequest(this, IncidentType.TYPE_LAW, Config.getDeviceId(this)));
    }
    
    public void onFireClick(View view)
    {
    	next(IncidentRequest.createRequest(this, IncidentType.TYPE_FIRE, Config.getDeviceId(this)));
    }
    
    private void next(Uri uri)
    {
		Intent intent = new Intent(this, MainMapActivity.class);
		intent.setData(uri);
		startActivity(intent);
		finish();
    }
}
