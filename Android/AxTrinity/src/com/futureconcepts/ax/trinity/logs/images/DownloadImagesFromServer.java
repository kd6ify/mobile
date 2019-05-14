package com.futureconcepts.ax.trinity.logs.images;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.LinkedList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.futureconcepts.gqueue.MercurySettings;
import com.futureconcepts.localmedia.database.LocalMediaChunksTable;
//import com.futureconcepts.ax.trinity.logs.ViewJournalEntryActivity.DownloadImageServiceReceiver;
import com.futureconcepts.localmedia.database.LocalMediaTable;
import com.futureconcepts.localmedia.operations.ImageObject;
import com.futureconcepts.localmedia.operations.MediaHandler;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

public class DownloadImagesFromServer extends IntentService {
	private static final String TAG = DownloadImagesFromServer.class.getSimpleName();
	private final int NEXT_IMAGE = 1;
	private final int SEND_NEXT_REQUEST = 2;
	private final int STOP_SERVICE = 3;
	boolean stopService = false;
	private int  attempt = 0;
	private static DownloadImageNotifier listener;
	
	public DownloadImagesFromServer() {
		super(TAG);
		// TODO Auto-generated constructor stub
	}
	public static void  setDownloadImageNotifierListener(DownloadImageNotifier Listener)
	{
		listener = Listener;		
	}
	
	public static void removeDownloadNotifierListener(DownloadImageNotifier Listener)
	{
		if(listener == Listener)
			listener=null;
	}
	
	public interface DownloadImageNotifier{
		public void downloadComplete( String imageID,String serverResponse, String filePath);
		public void downloadFail( String imageID,String serverResponse);
		public void downloadUpdateProgress( String imageID,int progress);
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG,"Images download Service Start");		
		String journalEntryID = intent.getStringExtra("JournalEntryID");
		LinkedList<ImageObject> imagesToDownload = MediaHandler.getAllMediaToDownload(getApplicationContext(),MediaHandler.ACTION_DOWNLOAD,journalEntryID);
		for(ImageObject image: imagesToDownload)
		{
			attempt = 0;
			boolean imageComplete=false;
			while(!imageComplete){
				if (NetworkHandler.isNetworkAvailable(getApplicationContext())) {
					switch(handleServerResponse(sendPostToServer(madeRequest(getMimeType(image.getImagePath()),image)),image))
					{
					case NEXT_IMAGE:
						imageComplete = true;
						break;
					case STOP_SERVICE:
						if(listener!=null){
							listener.downloadFail(image.getID(), "Unable to download image. SDCard is not mounted.");
						}
						imageComplete = true;
						stopService = true;//skip all other images;				
						break;
					}
				}else
				{
					//stopService =true;
					imageComplete = true;
				//	Log.e(TAG, "Stop no Internet Concetion");
					if(image.getID().equals(imagesToDownload.getLast().getID())){
						
						if(listener!=null){
							listener.downloadFail(image.getID(),  "Unable to download image. No internet connection found.");
						}
					}else
					{
						if(listener!=null){
							listener.downloadFail(image.getID(),null);
						}
					}
				}
			}
			if(stopService){
			//Log.e(TAG, "Stop Service Internet Concetion");
				break;
			}
		}
		imagesToDownload.clear();	
		imagesToDownload =null;
    	Log.e(TAG, "Service download Finish ");	
	}
	
	public static boolean isMyServiceRunning(Context context) {
	    ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if ("com.futureconcepts.ax.trinity.logs.images.DownloadImagesFromServer".equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
   }
	
	/**
	 * Executes the post request to server
	 * @param context
	 * @param request
	 * @return The response of the server as JSONObject
	 */
	private JSONObject sendPostToServer(HttpPost request){		
		DefaultHttpClient _client = Client.madeClient(getApplicationContext());		
		HttpResponse response = null;
		JSONObject jsonobject = null;
		try {
			response = _client.execute(request);// Execute the post request
			HttpEntity resEntity = response.getEntity();// Get the response from the server
			if (resEntity != null) {
				if (resEntity != null) {
					InputStream instream = resEntity.getContent();
					BufferedReader reader = new BufferedReader(	new InputStreamReader(instream));
					String data = null;
					while ((data = reader.readLine()) != null) {
						//	Log.e(TAG, "this is data:  "+data);
							jsonobject=	new JSONObject(data);
					}
					instream.close();
					instream = null;
					reader.close();
					reader = null;
				}				
			}			
			resEntity = null;
			response = null;
		}catch(Exception e){
			e.printStackTrace();
		}finally
		{
			_client.getConnectionManager().shutdown();	// Close the connection		
		}
		return jsonobject;	
	}
	
	/**
	 * Notifies the user what was the result of the intent to download the image.
	 * @param error
	 * @param imageID
	 * @param serverResponse
	 * @param filePath
	 */
//	private void sendBrsoadcast(boolean error, String imageID,String serverResponse, String filePath)
//	{
//		Intent broadcastIntent = new Intent();
//        broadcastIntent.setAction(DownloadImageServiceReceiver.PROCESS_RESPONSE);
//        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
//        if(error)
//        {
//        	broadcastIntent.putExtra("result","fail");
//            broadcastIntent.putExtra("message", serverResponse);
//            broadcastIntent.putExtra("ID", imageID);
//            broadcastIntent.putExtra("filePath", filePath);
//        }else{
//        	broadcastIntent.putExtra("result","success");
//            broadcastIntent.putExtra("message", "Image Download Complete");
//        	//   broadcastIntent.putExtra("message", serverResponse);
//            broadcastIntent.putExtra("ID", imageID);
//            broadcastIntent.putExtra("filePath", filePath);
//        }
//        sendBroadcast(broadcastIntent);
//	}
	
	/**
	 * Handles success response message  from server. Writes the chunk in the file for this image and updates the 
	 * LocalMedia record for this image ID.
	 * @param response: response from server
	 * @param image: current image
	 * @param context
	 * @return 
	 * @throws JSONException
	 */
	private int handleServerResponseSuccess(JSONObject response, ImageObject image,Context context) throws  JSONException
	{		
		if(isExternalStorageWritable())
		{	
			byte[] chunk = Base64.decode(response.getString("chunk"),Base64.DEFAULT);
			writeChunkInFile(context, chunk, image.getImagePath());	
			chunk=null;
			long fileSize = MediaHandler.getFileSize(image.getImagePath());	
			image.setBytesStored(fileSize);	
			//we get the filesize from the server because is encrypted and the size change.
			long total = Long.parseLong(response.getString("fileSize"));			
			String serverSha1 = response.getString("checksum");			
			response=null;
			if(fileSize==total || fileSize>total)
			{
				return handleCompletedImage(context, image,serverSha1,fileSize);
			}
			else
			{
				MediaHandler.updateMedia(context, image.getID(), fileSize,"incomplete");
				//sendBroadcast(false,image.getID(),"%"+((fileSize*100)/total),null);
				if(listener!=null){
					listener.downloadUpdateProgress(image.getID(),(int)((fileSize*100)/total));
				}
				return SEND_NEXT_REQUEST;
			}
		}else
		{
			Log.d(TAG,"Service stopped because SD card is not Available");
			return STOP_SERVICE;
		}
	}
	
	/**
	 * Handles a fail message from server
	 * @param response: response from server
	 * @param image: current image
	 * @param context
	 * @return Action to perform
	 * @throws JSONException
	 */
	private int handleServerResponseFail(JSONObject response,ImageObject image, Context context) throws JSONException {
		   String serverResponse = response.getString("error");		
		if ("The Image file is not available yet".equals(response.getString("error"))) {
			response = null;
			if(listener!=null){
				listener.downloadFail(image.getID(), serverResponse);
			}
			return handleUnknownError(image.getID());			
		} else {// the image was not found in the server
			MediaHandler.deleteMediaWithID(LocalMediaTable.CONTENT_URI,context, "ID=?", new String[] {image.getID()});
			if(listener!=null){
				listener.downloadFail(image.getID(), serverResponse);
			}
			response = null;
			return NEXT_IMAGE;
		}
	}
	
	
	/**
	 * Handles a complete downloaded image
	 * @param context
	 * @param image
	 * @param serverSha1
	 * @param fileSize
	 * @return
	 */
	private int handleCompletedImage(Context context, ImageObject image, String serverSha1, long fileSize)
	{
		String sha1 = null;					
		try{	
			sha1 = SHA1.genereteSha1OfFile(image.getImagePath());
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		if(serverSha1.equals(sha1))
		{
			String completeFilePath = decryptFile(image.getImagePath(),context,image.getID());
			if(completeFilePath != null){
				MediaHandler.updateMediaFilePath(context, image.getID(),completeFilePath);
				Log.e(TAG, "Image Complete Move to next one");									
				MediaHandler.updateMedia(context, image.getID(), fileSize,"complete");	
				//sendBroadcast(false,image.getID(),null,completeFilePath);
				if(listener!=null){
					listener.downloadComplete(image.getID(), "Image Download Complete", completeFilePath);
				}
			}else 
			{
				MediaHandler.deleteMediaWithID(LocalMediaTable.CONTENT_URI,context,
						LocalMediaTable.COLUMN_ID+"=?",new String []{image.getID()});
				File corruptedFile = new File(image.getImagePath());
				corruptedFile.delete();//delete the file because is corrupted	
				corruptedFile = null;
				if(listener!=null){
					listener.downloadFail(image.getID(),"Fail to decrypt image. File is corrupted." );
				}
			}
			return NEXT_IMAGE;
		}else
		{
			File corruptedFile = new File(image.getImagePath());
			corruptedFile.delete();//delete the file because is corrupted			
			MediaHandler.updateMedia(context, image.getID(), 0,"incomplete");
			return handleUnknownError(image.getID());
		}
	}
	
	
	/**
	 * Handles the response from the server
	 * @param response: response form server
	 * @return
	 */
	private int handleServerResponse(JSONObject response, ImageObject image)
	{
		if(response!=null)
		{
			try {
				if("success".equals(response.getString("result")))
				{
					return handleServerResponseSuccess(response,image,getApplicationContext());
				}else
				{
					return handleServerResponseFail(response,image,getApplicationContext());
				}				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		return handleUnknownError(image.getID());
			
	}
	
	/**
	 * Handles where we get an UnknowError more thatn 3 times.
	 * @param imageId
	 * @return 
	 */
	private int handleUnknownError(String imageId)
	{
		if(attempt<=3)
	    {
			attempt +=1;
			return SEND_NEXT_REQUEST;							
	    }else
	    {
	    	attempt = 0;
	 		MediaHandler.updateMediaError(getApplicationContext(), imageId, "yes");
	    	return  NEXT_IMAGE;
	    }
	}
	
	/**
	 * @param context
	 * @param chunk: bytes to write.
	 * @param filePath: where we want to write the bytes
	 */
	private void writeChunkInFile(Context context, byte chunk[], String filePath) {
		File trinity = new File(Environment.getExternalStorageDirectory(), "Trinity");			
		if (!trinity.exists()) {
			trinity.mkdir();// If this does not exist, we create it here.				
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
	 * Creates a HttpPost
	 * @param chunk
	 * @param mimeType
	 * @param fileSize
	 * @return 
	 */
	private HttpPost madeRequest(String mimeType,ImageObject image) {
		String ServerAddress = MercurySettings.getMediaImagesServerAddress(getApplicationContext());
		HttpPost post = new HttpPost(ServerAddress+"getImage.php");		
		MultipartEntity mpEntity = new MultipartEntity();
		try {	
				// Add the data to the multipart entity
				mpEntity.addPart("mediaID", new StringBody(image.getID(),Charset.forName("UTF-8")));
				mpEntity.addPart("application", new StringBody("Trinity",Charset.forName("UTF-8")));
				mpEntity.addPart("mimeType",new StringBody("."+mimeType, Charset.forName("UTF-8")));
				mpEntity.addPart("bytesStored",new StringBody("" +image.getBytesStored(), Charset.forName("UTF-8")));		
			post.setEntity(mpEntity);// Execute the post request	
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return post;
	}	
	
	/**
	 * @param filePath: File to Decrypt
	 * @param context : current Context.
	 * @param ID:  Image ID
	 * @return String: FilePath that contains location of the file decrypted.
	 */
	private String decryptFile(String filePath,Context context, String ID)
	{		
		AESEncryption aesEnc = new AESEncryption();
		return aesEnc.dencryptInChunks(filePath, ID);		
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
	 * Notifies the external storage that we add a new Item.
	 * @param context
	 */
	private static void notifySDcard(Context context) {
			context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri
			.parse("file://" + Environment.getExternalStorageDirectory())));
	}
	
	/**
	 * @param filePath
	 * @return return the extension of the file
	 */
	private String getMimeType(String filePath)
	{
		String filename = new File(filePath).getName(); 
		return filename.substring(filename.lastIndexOf('.') + 1);
	}
	

}
