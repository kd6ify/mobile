package com.futureconcepts.mercury.main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.futureconcepts.mercury.R;

public class LegalInfoActivity extends Activity {
	
	private int apacheLicense = R.raw.apache_2;
	private int otherLicenses = R.raw.android_mercury_legal;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.legal_activity);
		displayLegalInformation(otherLicenses);
	}
	
	private void displayLegalInformation(int Resource)
	{
		((TextView)findViewById(R.id.textView1)).setText(readFile(Resource));
	}
	
	public void showLicense(View view)
	{
		Button v = (Button)view;
		if((v.getText().toString().contains("Apache")))
		{
			displayLegalInformation(apacheLicense);
			v.setText("Show Other Licenses");		
		}else{
			displayLegalInformation(otherLicenses);
			v.setText("Show Apache License 2.0");
		}
	}
	private String readFile(int resource) {

		InputStream inputStream = getResources().openRawResource(resource);
		System.out.println(inputStream);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int i;
		try {
			i = inputStream.read();
			while (i != -1) {
				byteArrayOutputStream.write(i);
				i = inputStream.read();
			}
			inputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return byteArrayOutputStream.toString();
	}

}
