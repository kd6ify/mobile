package com.futureconcepts.dbcommunication;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RestClient {

	public static String convertStreamToString(InputStream is) throws Exception {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is),65728);
	    StringBuilder sb = new StringBuilder();
	    String line = null;

	    while ((line = reader.readLine()) != null) {
	        sb.append(line);
	    }

	    is.close();

	    return sb.toString();
	}
	
}
