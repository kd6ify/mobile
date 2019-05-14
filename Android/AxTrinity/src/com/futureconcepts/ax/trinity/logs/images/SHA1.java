package com.futureconcepts.ax.trinity.logs.images;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1 {
	
	
	private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

	//generates SHA1 of a File
    public static String genereteSha1OfFile(String filePath) throws NoSuchAlgorithmException, UnsupportedEncodingException {
    	File file  = new File(filePath);
    	FileInputStream fis; 
    	try {
			 fis = new FileInputStream(file);
			 MessageDigest md = MessageDigest.getInstance("SHA-1");
	    	 int nread = 0; 
	    	// byte[] dataBytes = new byte[(int) file.length()];
	    	 byte[] dataBytes = new byte[1024];//128kb
	    	  while ((nread = fis.read(dataBytes)) != -1) {
	    	      md.update(dataBytes, 0, nread);
	    	    };
	    	    byte[] sha1hash = md.digest();
	    	    fis.close();
	            return convertToHex(sha1hash);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	 return null;       
   }
    
    public static String sha1OfString(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        if(text ==null)
        {
        	text = "null";
        }
    	MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

}
