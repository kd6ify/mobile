package com.futureconcepts.ax.trinity.logs.images;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;
import android.util.Log;

import com.futureconcepts.localmedia.operations.MediaHandler;

public class AESEncryption {
	public static final String TAG = "AESEncryption";

	private static Cipher aesCipher;
	private static SecretKey secretKey;
	private static IvParameterSpec ivParameterSpec;

	private static String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
	private static String CIPHER_ALGORITHM = "AES";
	private static String rawSecretKey ="19e00f6186238edcea47af6e6ddc2ede";
	private AESEncryptionChunk listener;
	
	public interface AESEncryptionChunk
	{
		void saveChunk(String chunkAsBase64String, int totalImageSize, int chunkNumber);
	}
	
	public AESEncryption() {

		byte[] IVparameter = new byte[16];
		System.arraycopy(rawSecretKey.getBytes(), 0, IVparameter, 0, IVparameter.length);
		try {
			aesCipher = Cipher.getInstance(CIPHER_TRANSFORMATION);			
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "No such algorithm " + CIPHER_ALGORITHM, e);
		} catch (NoSuchPaddingException e) {
			Log.e(TAG, "No such padding PKCS5", e);
		}
		secretKey = new SecretKeySpec(rawSecretKey.getBytes(), CIPHER_ALGORITHM);
		ivParameterSpec = new IvParameterSpec(IVparameter);
	}

	public int encryptAsBase64(String FilePath, int skip,AESEncryptionChunk handler)
	{
		listener = handler;
		try {
			aesCipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
			File file = new File(FilePath);
			FileInputStream inFile = new FileInputStream(file);
			int chunkSize =128*1024;//128kb
			byte[] encrypted = new byte[chunkSize];
			int bytesRead; int length = 0;int chunkCounter=0;
			while ((bytesRead = inFile.read(encrypted, 0, chunkSize)) != -1) {				
				byte[] output = aesCipher.update(encrypted, 0, bytesRead);
				if (output != null){
					length +=output.length;
					if(length>skip)
						chunkCounter++;
						listener.saveChunk(Base64.encodeToString(output, Base64.DEFAULT),length,chunkCounter);
				}	
				output = null;
			}
			encrypted=null;
			 byte[] output = aesCipher.doFinal();
			 length +=output.length;			
			 if(length>skip){
				 chunkCounter++;
				 listener.saveChunk(Base64.encodeToString(output, Base64.DEFAULT),length,chunkCounter);
			 }
			 output = null;
			inFile.close();		
			return chunkCounter;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
		
	public String dencryptInChunks(String FilePath, String ID) {
		try {
			aesCipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
			File file = new File(FilePath);
			String filename = file.getName();
			String mimeType = filename.substring(filename.lastIndexOf('.') + 1);
			String decryptedFilePath = MediaHandler.rootDirectoryFolderPathForImages+ ID + "." + mimeType;
			FileInputStream inFile = new FileInputStream(file);
			FileOutputStream outFile = new FileOutputStream(decryptedFilePath);
			int chunk =128*1024;//65536 //262144;
			byte[] decryptet = new byte[chunk];
			int bytesRead;
			while ((bytesRead = inFile.read(decryptet, 0, chunk)) != -1) {
				byte[] output = aesCipher.update(decryptet, 0, bytesRead);
				if (output != null)
					outFile.write(output);
			}
			inFile.close();
			inFile =null;
			byte[] output = aesCipher.doFinal();
			if (output != null)
				outFile.write(output);
			outFile.flush();
			outFile.close();
			outFile = null;
			decryptet=null;
			file.delete();
			file = null;
			return decryptedFilePath;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
