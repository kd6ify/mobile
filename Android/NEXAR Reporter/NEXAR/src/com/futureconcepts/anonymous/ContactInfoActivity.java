package com.futureconcepts.anonymous;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ContactInfoActivity extends Activity {
	private EditText phoneNumber, studentName, email;
	SharedPreferences settings;
	String test = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_contact_info);
		settings = getSharedPreferences("MyPrefsFile", 0);

		studentName = (EditText) findViewById(R.id.studentNameInfo);
		phoneNumber = (EditText) findViewById(R.id.phonenumberInfo);
		email = (EditText) findViewById(R.id.emailInfo);
		phoneNumber
				.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
		TextView contactInfoMessage = (TextView) findViewById(R.id.contactInfoMessage);
		contactInfoMessage
				.setText(Html
						.fromHtml("<p>This feature provides a fast and easy way to "
								+ "have your contact information entered into each report. The information entered on this "
								+ "page is private and only viewable by you.</p>"));
		checkPreferences();

		TextWatcher Name = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// you can check for enter key here
				// Log.d("watcher before","charsequence"+s+"start"+start+"after"+after+"count"+count);
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				String textCap = new String(arg0.toString());
				final StringTokenizer st = new StringTokenizer(textCap, " ",
						true);
				final StringBuilder sb = new StringBuilder();

				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					token = String.format("%s%s",
							Character.toUpperCase(token.charAt(0)),
							token.substring(1));
					sb.append(token);
				}
				test = sb.toString();
				Log.d("aasas", "" + sb.toString());
				studentName.removeTextChangedListener(this);
				studentName.setText(test);
				studentName.setSelection(test.length());
				studentName.addTextChangedListener(this);

			}

		};

		studentName.addTextChangedListener(Name);

	}

	public void cancel(View v) {
		Intent intent = new Intent();
		setResult(RESULT_OK, intent);
		finish();
		Toast.makeText(ContactInfoActivity.this, "All changes discarded.",
				Toast.LENGTH_SHORT).show();
	}

	public void getPhone(View view) {
		String number = null;
		String service = Context.TELEPHONY_SERVICE;
		TelephonyManager tel_manager = (TelephonyManager) getSystemService(service);
		int device_type = tel_manager.getPhoneType();

		switch (device_type) {
		case (TelephonyManager.PHONE_TYPE_CDMA):
			number = tel_manager.getLine1Number();
			break;
		case (TelephonyManager.PHONE_TYPE_GSM):
			number = tel_manager.getLine1Number();
			break;
		case (TelephonyManager.PHONE_TYPE_NONE):
			number = "No phone";
			break;
		default:
			// return something else
			number = "No phone";
			break;
		}
		phoneNumber.setText(number);

	}

	public void saveContactInfo(View v) {
		String userName = studentName.getText().toString();
		String userPhone = phoneNumber.getText().toString();
		String userEmail = email.getText().toString();
		boolean verifyEmail = false;

		boolean WhitespaceName = userName.matches("^\\s*$");
		boolean WhitespacePhone = userPhone.matches("^\\s*$");
		boolean WhitespaceEmail = userEmail.matches("^\\s*$");

		SharedPreferences.Editor editor = settings.edit();
		if (WhitespaceName == false) {
			editor.putString("UserName", userName);
		}
		if (WhitespacePhone == false) {
			editor.putString("UserPhone", userPhone);
		}
		if (WhitespaceEmail == false) {		
			verifyEmail = isEmailValid(userEmail);
			if (verifyEmail == true) {
				editor.putString("UserEmail", userEmail);
				editor.commit();
				Toast.makeText(ContactInfoActivity.this,
						"Contact Information Saved Successfully.",
						Toast.LENGTH_SHORT).show();
				finish();

			} else {
				Toast.makeText(this,
						"Please enter a valid email (ID@example.com).",
						Toast.LENGTH_SHORT).show();

			}
			
			
		}else if ((userEmail.length() == 0 || WhitespaceEmail == true) && WhitespaceName == false) {
			editor.commit();
			Toast.makeText(ContactInfoActivity.this,
					"Contact Information Saved Successfully.",
					Toast.LENGTH_SHORT).show();
			
			finish();

		}else{
			Toast.makeText(ContactInfoActivity.this,
					"Add a Name or Email to save contact information.",
					Toast.LENGTH_SHORT).show();
		}
		
		
		/*if (userEmail.length() == 0 || WhitespaceEmail == true) {
			editor.commit();
			finish();

		} 
		
		else {
			verifyEmail = isEmailValid(userEmail);
			if (verifyEmail == true) {
				editor.putString("UserEmail", userEmail);
				editor.commit();
				Toast.makeText(ContactInfoActivity.this,
						"Contact Information Saved Successfully.",
						Toast.LENGTH_SHORT).show();
				finish();

			} else {
				Toast.makeText(this,
						"Please enter a valid email (ID@example.com).",
						Toast.LENGTH_SHORT).show();

			}

		}*/

	}

	public void checkPreferences() {

		String userName = settings.getString("UserName", "");
		String userPhone = settings.getString("UserPhone", "");
		String userEmail = settings.getString("UserEmail", "");

		studentName.setText(userName);
		phoneNumber.setText(userPhone);
		email.setText(userEmail);

	}

	public static boolean isEmailValid(String email) {
		boolean isValid = false;

		String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		CharSequence inputStr = email;

		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		if (matcher.matches()) {
			isValid = true;
		}
		return isValid;
	}

}
