package com.futureconcepts.mercury.maps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.json.JSONException;
import org.w3c.util.InvalidDateException;

import com.futureconcepts.mercury.maps.ServerRequest.ServerRequestAddParameters;
import com.futureconcepts.mercury.sync.TransactionException;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.util.Base64;
import android.util.Log;

public class DownloadMapFileService extends IntentService implements ServerRequestAddParameters{

	private static final String TAG= DownloadMapFileService.class.getSimpleName();
	public final static String rootLocalDirectoryFolderPathForMaps = 
			Environment.getExternalStorageDirectory()+"/Trinity/Maps/";
	private String mapFile;
	private long bytesStored;
	private String localMapFilePath;// = rootLocalDirectoryFolderPathForMaps+mapFile;
	private static final int SEND_NEXT_REQUEST = 1;
	private static final int STOP_SERVICE = 2;
	private static final int FILE_IS_COMPLETE = 0;
	private int attempt;
	private static DonwloadMapFileNotifier notifier;
	private MapsNotification mapsNotification;
	public DownloadMapFileService() {
		super(TAG);
		// TODO Auto-generated constructor stub
	}
	
	public interface DonwloadMapFileNotifier
	{
		void downloadFileServiceStart(String mapName);
		void downloadProgress(int progress,String mapName);
		void downloadFail(String errorMessage,String mapName);
		void downloadComplete(String mapName);
	}

	public static DonwloadMapFileNotifier getNotifier() {
		return notifier;
	}

	public static void setNotifier(DonwloadMapFileNotifier notifier2) {
		notifier = notifier2;
	}

	public static void removeNotifier() {
		notifier = null;
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		Log.d(TAG, "MapDownladservice start");
		mapsNotification= new MapsNotification(getApplicationContext());	
		mapFile = intent.getExtras().getString("mapFile");//set the current map we want
		localMapFilePath = rootLocalDirectoryFolderPathForMaps+"TEMP"+mapFile;	
		if(notifier!=null){
			notifier.downloadFileServiceStart(mapFile);
		}
		ServerResponseObject response = null;
		downloadMap(response);
		Log.d(TAG, "MapDownladservice end");
	}
	
	private void downloadMap(ServerResponseObject response)
	{
		boolean mapComplete = false;
		while (!mapComplete) {
			bytesStored = getFileSize(localMapFilePath);//set the current bytes we have of the map.
			response = requestServerAndParseResult();//ask server for file piece and parse result
			if (response != null) {
				if ("success".equals(response.getResult())) {
					if(enoughSpace(response)){
						switch (handleWriteChunkToFile(response)) {
						case STOP_SERVICE:
							mapComplete = true;
							break;
						case FILE_IS_COMPLETE:
							mapComplete = true;
							notifyComplete(mapFile);
							// send A notification and update GUI
							break;
						}
					}else{ 
						mapComplete = true;
						notifyFail("Not enough space in the SD card",STOP_SERVICE);
					}
				} else {
					if(handleUnknownError() == STOP_SERVICE){
						mapComplete = true;
						notifyFail(response.getError(),STOP_SERVICE);
					}
				}
			}else{ //Hanlde unknown error
				if(handleUnknownError() == STOP_SERVICE){
					mapComplete = true;
				    notifyFail("Conection to Server fail",STOP_SERVICE );
				}
			}
		}
	}
	
	private void notifyFail(String message, int status)
	{
		if(notifier!=null){
			notifier.downloadFail(message,mapFile);
		}
		else
		{
			if(status==STOP_SERVICE){
				mapsNotification.notifyDownloadFail("Fail to download "+mapFile+":\n"+message,"");
			}
		}
	}
	
	private void notifyComplete(String mapName)
	{
		if(notifier!=null){
			notifier.downloadComplete(mapName);
		}
		else
		{
			mapsNotification.notifyDownloadSuccess("File map successfully downloaded: \n"+mapName);
		}
	}
	
	private int  handleUnknownError()
	{		
		if(attempt<=3)
	    {
			attempt +=1;
			return SEND_NEXT_REQUEST;							
	    }else
	    {
	    	attempt = 0;	 	
	    	return  STOP_SERVICE;
	    }
	}
	
	private double  getAvailableSpace()
	{
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		double sdAvailSize = (double)stat.getAvailableBlocks()
		                   * (double)stat.getBlockSize();
		//One binary gigabyte equals 1,073,741,824 bytes.
		//One binary megabyte equals 1048576.
		return  sdAvailSize / 1048576;//1073741824;//double gigaAvailable = sdAvailSize / 1073741824;
	}
	
	private boolean fileIsClean(ServerResponseObject response)
	{
		String sha1Local = null;
		try {
			sha1Local = SHA1.genereteSha1OfFile(localMapFilePath);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(response.getChecksum().equals(sha1Local))
		{
			return true;//handleServerResponseSuccess(response);
		}
		return true;
	}
	
	/**
	 * Request the server for a file
	 * @return parsed server response
	 */
	private ServerResponseObject requestServerAndParseResult()
	{		
		try {
			ServerRequest serverRequest = new ServerRequest(ServerRequest.ACTION_DOWNLOAD_FILE, this);
			return parse(serverRequest.madeRequestToServer(getApplicationContext()));
		} catch (Exception e) {
			e.printStackTrace();
		}	
		return null;
	}
	
	//expected: result = success, chunk=piece, fileSize ="123123", checksum="sha1"
	
	private ServerResponseObject parse(JsonParser p) throws JsonParseException, IOException, InvalidDateException, TransactionException
	{
		ServerResponseObject response = new ServerResponseObject();
		p.nextToken();
		while (p.nextToken() != JsonToken.END_OBJECT)
		{
			String name = p.getCurrentName();
			p.nextToken();
			if (name.equals("result"))
			{
				response.setResult(p.getText());
				Log.d(TAG, p.getText());
			}
			else if (name.equals("checksum"))
			{
				response.setChecksum(p.getText());
				//Log.d(TAG, p.getText());
			}
			else if (name.equals("fileSize"))
			{
				response.setFileSize(p.getText());
				//Log.d(TAG, p.getText());
			}
			else if (name.equals("chunk"))
			{
				response.setChunk(p.getText());
				//Log.d(TAG, p.getText());
			}
			else if(name.equals("error"))
			{
				response.setError(p.getText());
				Log.d(TAG, p.getText());
			}			
		}
		return response;
	}

	private boolean enoughSpace(ServerResponseObject response)
	{
		double sdCardSpace = getAvailableSpace();//use Megaby instead of GB;
		double fileSize = (Double.parseDouble(response.getFileSize())/1024)/1024;//use Megabyte
		//double fileSizeMB = Double.parseDouble(response.getFileSize());
		double localFileSize = (getFileSize(localMapFilePath)/1024)/1024;//use Megabyte
	//	double localFileSizeMB = getFileSize(localMapFilePath);		
		if(sdCardSpace>(fileSize-localFileSize))
		{
			Log.e(TAG, "Can write we have enough space in the SD CARD");
			return true;
		}
		return false;
	}
	
	/**
	 *  Writes the chunk in the file for this image
	 * @param response: response from server
	 * @return 
	 * @throws JSONException
	 */
	private int handleWriteChunkToFile(ServerResponseObject response)
	{		
		if(isExternalStorageWritable())
		{			
			byte[] chunk = Base64.decode(response.getChunk(),Base64.DEFAULT);
			writeChunkInFile(getApplicationContext(), chunk,localMapFilePath);	
			chunk=null;
			long fileSize = getFileSize(localMapFilePath);	
			//image.setBytesStored(fileSize);	
			//we get the filesize from the server because is encrypted and the size change.
			long total = Long.parseLong(response.getFileSize());	
			response.setChunk(null);
			if(fileSize==total || fileSize>total )
			{
				if(fileIsClean(response))
				{
					renameCompleteFile(rootLocalDirectoryFolderPathForMaps,mapFile);				
					return FILE_IS_COMPLETE;
				}else
				{
					deleteCorruptedFile(localMapFilePath);
					//notifyFail("File get corrupted. Please try again.");
					return handleUnknownError();
				}	
			}else
			{
				if(notifier!=null){
					notifier.downloadProgress((int)((fileSize*100)/total),mapFile);
				}
				return SEND_NEXT_REQUEST;
			}
		}else
		{
			notifyFail("SD card is not Available",STOP_SERVICE);
			return STOP_SERVICE;
		}
	}
	
	private void deleteCorruptedFile(String path)
	{
		File corruptedFile = new File(path);
		corruptedFile.delete();//delete the file because is corrupted	
		corruptedFile = null;
	}
	
	private void renameCompleteFile(String localDir, String mapName )
	{
		File dir = new File(localDir);
		if(dir.exists()){
		    File from = new File(dir,"TEMP"+mapName);
		    File to = new File(dir,mapName);
		     if(from.exists())
		        from.renameTo(to);
		}
	}

	
	@Override
	public void addParameters(HttpPost post) {
		// TODO Auto-generated method stu
		Log.d(TAG, "parameter"+  mapFile);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("action", ServerRequest.ACTION_DOWNLOAD_FILE));
		nameValuePairs.add(new BasicNameValuePair("fileName", mapFile));
		nameValuePairs.add(new BasicNameValuePair("bytesStored", ""+bytesStored));		
		try {
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param context
	 * @param chunk: bytes to write.
	 * @param filePath: where we want to write the bytes
	 */
	private void writeChunkInFile(Context context, byte chunk[], String filePath) {
		File trinity = new File(rootLocalDirectoryFolderPathForMaps);			
		if (!trinity.exists()) {
			trinity.mkdirs();// If this does not exist, we create it here.				
			notifySDcard(context);// Notifies folder created
		}
		trinity = null;
		FileOutputStream fop = null; File file;
		try {
			file = new File(filePath);
			fop = new FileOutputStream(file, true);
			fop.write(chunk);
			fop.flush();
			fop.close();
			fop=null;
			System.out.println("Done write chunk");
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	/**
	 * Notifies the external storage that we add a new Item.
	 * @param context
	 */
	private static void notifySDcard(Context context) {
			context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri
			.parse("file://" + Environment.getExternalStorageDirectory())));
	}
	
	
	/**
	 * @param filepath 
	 * @return the size of the file or 0 if the file not exists.
	 */
	public static long getFileSize(String filepath)
	{
		File file = new File(filepath);
		if(file.exists()){
			return file.length();
		}else
		{
			return 0;
		}
	}
	
	/**Checks if external storage is available for read and write
	 * @return true || false if we cannot write on the SD card.
	 */
	public static boolean isExternalStorageWritable() {
		//String state = Environment.getExternalStorageState();
		boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if (isSDPresent) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param context: context of the activity.
	 * @return true: if has connection to Internet and is connected.
	 */
	public static  boolean isNetworkAvailable(Context context) {
	    ConnectivityManager cm = (ConnectivityManager) 
	      context.getSystemService(Context.CONNECTIVITY_SERVICE);
	     NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    // if no network is available networkInfo will be null
	    // otherwise check if we are connected
	    if (netInfo != null && netInfo.isConnected()) {
	        return true;
	    }
	    return false;
	}
	
}
