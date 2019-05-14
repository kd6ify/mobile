package com.futureconcepts.ax.trinity.logs.images;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.futureconcepts.ax.trinity.logs.images.AESEncryption.AESEncryptionChunk;
import com.futureconcepts.gqueue.MercurySettings;
import com.futureconcepts.localmedia.database.LocalMediaTable;
import com.futureconcepts.localmedia.database.LocalMediaChunksTable;
import com.futureconcepts.localmedia.operations.ImageObject;
import com.futureconcepts.localmedia.operations.MediaHandler;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

public class SendImageToServer extends IntentService implements AESEncryptionChunk {
	public static String TAG = SendImageToServer.class.getSimpleName().toString();
	private final int SEND_NEXT_IMAGE = 1;
	private final int SEND_NEXT_REQUEST = 2;
	private final int SEND_SAME_REQUEST_AGAIN = 3;
	private String currentImageID;
	private int currentChunkPositionValue;
	private int  attempt = 0;
	private ImagesNotification imageNotification;
	
	public SendImageToServer() {
		super(TAG);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG,"Send Images Service Start");		
		List<ImageObject> imagesToSend= new LinkedList<ImageObject>();
		imageNotification= new ImagesNotification(getApplicationContext());	
		imageNotification.cancelAllNotification();
		if(intent.hasExtra("mediaID"))
		{
			ImageObject img = MediaHandler.getOneMedia(getApplicationContext(), MediaHandler.ACTION_UPLOAD,intent.getStringExtra("mediaID"));
			if(img!=null)
			imagesToSend.add(img);
		}else
		{
			imagesToSend = MediaHandler.getAllMedia(getApplicationContext(), MediaHandler.ACTION_UPLOAD);
		}
		if(imagesToSend.size()>0){
			if(!NetworkHandler.isNetworkAvailable(getApplicationContext()))
			{
				setAllImagesWithFail(imagesToSend);
				imageNotification.notifyUploadFail("Failed to send Image. No internet connection found.","null");
			}else
			{
				sendImagesToServer(imagesToSend);
			}		
	//	imageNotification.cancelNotificationProgress();
			imagesToSend.clear();
			imagesToSend = null;
			Log.d(TAG,"Send Images Service Finish");
		}else
		{
			Log.d(TAG,"Send Images Service Finish: No Images to send");
		}
			
	}
	
	public static boolean isMyServiceRunning(Context context) {
	    ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if ("com.futureconcepts.ax.trinity.logs.images.SendImageToServer".equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}

	
	/**
	 * Mark all given  Images With fail status
	 * @param imagesToSend
	 */
	private void setAllImagesWithFail (List<ImageObject>imagesToSend)
	{
		for(ImageObject imageToSend : imagesToSend)
		{
			MediaHandler.updateMediaError(getApplicationContext(), imageToSend.getID(), "yes");
		}
	}
	
	/**
	 * Process and send each image to server.
	 * @param imagesToSend
	 */
	private void sendImagesToServer(List<ImageObject>imagesToSend )
	{
		for(ImageObject imageToSend : imagesToSend)
		{
			if(checkMedia(imageToSend))
			{
				currentImageID = imageToSend.getID();
				int TotalPosts = convertImageInChuncksWithAes256(imageToSend);
				for(int requestIndex=1;requestIndex<=TotalPosts;requestIndex++)
				{
					HttpPost post = createServerRequests(getMimeType(imageToSend.getImagePath()),imageToSend.getID());
					int processResult;
					if(post!=null)
					{
						processResult= handleServerResponse(sendImagePieceToServer(getApplicationContext(),post),imageToSend.getID(),requestIndex);
					}else
					{
						processResult=handleUnknownError(imageToSend.getID());
					}					
					switch(processResult)//((int)(pieces*requestIndex))))
					{
						case SEND_NEXT_IMAGE:
							requestIndex = TotalPosts;
							break;
						case SEND_SAME_REQUEST_AGAIN:
							if(requestIndex>0){
								requestIndex--;
							}
							break;
					}	
					
				}
			}else
			{
				imageNotification.notifyUploadFail("Failed to send image. Cannot find image in the device","null");
				MediaHandler.deleteMediaWithID(LocalMediaTable.CONTENT_URI,getApplicationContext(),
						LocalMediaTable.COLUMN_ID+"=?", new String []{imageToSend.getID()});
				//delete all chunks create of this image
			}
		}
	}
	
	
	/**
	 * Handles if the response from the server was null
	 * @param response: response form server
	 * @param imageId
	 * @return
	 */
	private int handleServerResponse(JSONObject response,String imageId, int requestIndex)
	{
		if(response!=null)
		{
			return handleServerResponseSuccess(response,imageId, requestIndex);
		}
		return handleUnknownError(imageId);
	}
	
	/**
	 * @param response: response from server
	 * @param imageId: media ID 
	 * @return action to perform: SEND_NEXT_IMAGE|SEND_SAME_REQUEST_AGAIN
	 */
	private int handleServerResponseSuccess(JSONObject response, String imageId,int requestIndex)
	{
		int result = SEND_SAME_REQUEST_AGAIN;
		try{			
			if(!"Sha1 is different".equals(response.getString("error"))){
				//modifyMediaRow(response.getInt("totalSaved"),response.getString("status"),getApplicationContext(),imageId);
				if(response.getString("status").equals("1")){
			    	//IMAGE COMPLETE
			    	MediaHandler.updateMedia(getApplicationContext(), imageId, response.getInt("totalSaved"),"complete");			    
			    	//imageNotification.notifyUploadSuccess("Image was sent successfully");
				}else{
			    	//IMAGE NOT COMPLTE UPDATE LAST PART SENT
					//imageNotification.notifyUpdateProgress("Ax Trinity","Sendign Image % " + progress );
			    	MediaHandler.updateMedia(getApplicationContext(), imageId, response.getInt("totalSaved"),"incomplete");			    	
				}
				MediaHandler.deleteMediaWithID(LocalMediaChunksTable.CONTENT_URI,getApplicationContext(),
						LocalMediaChunksTable.COLUMN_ID+"=? AND "+LocalMediaChunksTable.COLUMN_POSITION+"=?",
						new String []{currentImageID,""+currentChunkPositionValue});
				
				result= SEND_NEXT_REQUEST;
			}
			else if("Sha1 is different".equals(response.getString("error")) && attempt<=3)
			{
				result = SEND_SAME_REQUEST_AGAIN;
				attempt +=1;	
			}
			else {
				handleUnknownError(imageId);
		    }
		}catch(Exception e){e.printStackTrace();}
		response = null;
		return result;		
	}
	
	/** 
	 * @param imageId
	 * @return
	 */
	private int handleUnknownError(String imageId)
	{
		if(attempt<=3)
	    {
			attempt +=1;
			return SEND_SAME_REQUEST_AGAIN;							
	    }else
	    {
	    	return sendFailServerResponseFail(imageId);
	    }
	}
	
	/**
	 * Notifies to the user that there is an error sending the image.
	 * @param imageId the media ID that has the Issue;
	 * @return SEND_NEXT_IMAGE to try to send the next image;
	 */
	private int sendFailServerResponseFail(String imageId)
	{
		attempt = 0;
		//imageNotification.cancelNotificationProgress();
    	imageNotification.notifyUploadFail("Failed to send Image. Click here to retry", imageId);
 		MediaHandler.updateMediaError(getApplicationContext(), imageId, "yes");
    	return  SEND_NEXT_IMAGE;
	}
	
	/**
	 * This methods create a list of HttpPost request
	 * @param imageInChunks
	 * @param mimeType
	 * @param imageId
	 * @return 
	 */
	private HttpPost createServerRequests(String mimeType, String imageId)
	{
		//get image filezise and get position for next chunk to send
		Cursor last = MediaHandler.queryMedia(LocalMediaChunksTable.CONTENT_URI,
				new String[]{LocalMediaChunksTable.COLUMN_POSITION,LocalMediaChunksTable.COLUMN_FILE_SIZE},
				LocalMediaChunksTable.COLUMN_ID+" =?",
				new String[]{imageId}, getApplicationContext(),LocalMediaChunksTable.COLUMN_POSITION +" DESC");
		if(last.getCount()>0){
			last.moveToFirst();
			//get image file Size;
			String fileSize = last.getString(last.getColumnIndex(LocalMediaChunksTable.COLUMN_FILE_SIZE));
			//Get position of the chunk to send;
			last.moveToLast();
			int chunkNumber = last.getInt(last.getColumnIndex(LocalMediaChunksTable.COLUMN_POSITION));
			last.close();
			//Get all info of the next chunk to send
			Cursor imageChunks = MediaHandler.queryMedia(LocalMediaChunksTable.CONTENT_URI,
				LocalMediaChunksTable.MEDIA_ACTIVITY_CHUNKS_TABLE_PROJECTION,
				LocalMediaChunksTable.COLUMN_ID+" =? AND "+LocalMediaChunksTable.COLUMN_POSITION+"=?",
				new String[]{imageId,""+chunkNumber}, getApplicationContext(),null);
			imageChunks.moveToFirst();
			currentChunkPositionValue =chunkNumber; //imageChunks.getInt(imageChunks.getColumnIndex(LocalMediaChunksTable.COLUMN_POSITION));
			HttpPost request = madeRequest(imageChunks.getString(imageChunks.getColumnIndex(LocalMediaChunksTable.COLUMN_CHUNK)),
					mimeType,fileSize,imageId);
			imageChunks.close();
			return request;
		}else
		{
			return null;
		}
	}
	
	/**
	 * Creates a HttpPost
	 * @param chunk
	 * @param mimeType
	 * @param fileSize
	 * @return 
	 */
	private HttpPost madeRequest(String chunk, String mimeType,String fileSize,String currentImageID) {
		String ServerAddress = MercurySettings.getMediaImagesServerAddress(getApplicationContext());
		HttpPost post = new HttpPost(ServerAddress + "Media.php");
		MultipartEntity mpEntity = new MultipartEntity();
		// Add the data to the multipart entity
		String sha1OfChunk = "";	
		try {
			sha1OfChunk = SHA1.sha1OfString(chunk);
			mpEntity.addPart("data",new StringBody(chunk, Charset.forName("UTF-8")));
			mpEntity.addPart("mediaID",	new StringBody(currentImageID, Charset.forName("UTF-8")));
			mpEntity.addPart("application",	new StringBody("Trinity", Charset.forName("UTF-8")));
			mpEntity.addPart("mimeType",new StringBody(mimeType, Charset.forName("UTF-8")));
			mpEntity.addPart("size",new StringBody(fileSize, Charset.forName("UTF-8")));
			mpEntity.addPart("cheksum",	new StringBody(sha1OfChunk, Charset.forName("UTF-8")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		post.setEntity(mpEntity);
		return post;
	}
	
	/**
	 * Executes the post request to server
	 * @param context
	 * @param request
	 * @return The response of the server as JSONObject
	 */
	public JSONObject sendImagePieceToServer(Context context, HttpPost request){		
		DefaultHttpClient _client = Client.madeClient(getApplicationContext());							
		// Execute the post request
		HttpResponse httpResponse = null;
		JSONObject jsonobject = null;
		//String Response =null;
		//JSONArray jsonarray = null;
		try {
			httpResponse = _client.execute(request);
			HttpEntity resEntity = httpResponse.getEntity();
			if (resEntity != null) {
				InputStream instream = resEntity.getContent();
				BufferedReader reader = new BufferedReader(	new InputStreamReader(instream));
				String data = null;
				while ((data = reader.readLine()) != null) {
						jsonobject=	new JSONObject(data);
				}
				instream.close();
				instream = null;
				reader.close();
				reader = null;
			}		
		}catch(Exception e){
			e.printStackTrace();
		}finally
		{
			httpResponse = null;
			_client.getConnectionManager().shutdown();	// Close the connection		
		}
		return jsonobject;	
	}
	
	/**
	 * @param filePath
	 * @return return the extension of the file
	 */
	private String getMimeType(String filePath)
	{
		File file = new File(filePath);
		String filename = file.getName();
		String mimeType = filename.substring(filename.lastIndexOf('.') + 1);
		file = null;
		return mimeType;
	}	
	
	/**
	 * This method convert the image in chucks with 256AES encription and 64BaseString
	 * @param image to encode in chunks
	 * @return a list with all the chucnk of the image;
	 */
	private int convertImageInChuncksWithAes256(ImageObject image)
	{
		Cursor cursor =MediaHandler.queryMedia(LocalMediaChunksTable.CONTENT_URI,
				LocalMediaChunksTable.MEDIA_ACTIVITY_CHUNKS_TABLE_PROJECTION,
				LocalMediaChunksTable.COLUMN_ID+"=?", new String[]{image.getID()},getApplicationContext(), null);
		int chunks = cursor.getCount();
		cursor.close();
		if(chunks==0){
			AESEncryption aes = new AESEncryption();
			return aes.encryptAsBase64(image.getImagePath(), (int)image.getBytesStored(), this);
		}
		return chunks;
	}

	/**
	 * This method verifies that the current Image still available in the phone;
	 * @param image: The image Object we want to send
	 * @return True if the Image exist in the phone;
	 */
	private boolean checkMedia(ImageObject image) {
		boolean exist = false;
		File file = new File(image.getImagePath());
		if (!file.exists()) {
			Log.i(TAG, "Image not found");
		} else {
			exist = true;
		}
		return exist;
	}

	@Override
	public void saveChunk(String chunkAsBase64String, int totalImageSize, int chunkNumber) {
		// TODO Auto-generated method stub
		    ContentValues values = new ContentValues();
			values.put(LocalMediaChunksTable.COLUMN_ID, currentImageID);
			values.put(LocalMediaChunksTable.COLUMN_CHUNK,chunkAsBase64String );
			values.put(LocalMediaChunksTable.COLUMN_POSITION,chunkNumber );
			values.put(LocalMediaChunksTable.COLUMN_FILE_SIZE,totalImageSize );
			MediaHandler.insertUpdateMediaWithContentValues(LocalMediaChunksTable.CONTENT_URI,
					getApplicationContext(), values, MediaHandler.insertRow,null,null);
			//fileSize = ""+totalImageSize;
			//if(totalImageSize>0)//conversion of image in chunks finished
			//{
//				ContentValues values2 = new ContentValues();
//				values2.put(LocalMediaChunksTable.COLUMN_ID, currentImageID);
//				values2.put(LocalMediaChunksTable.COLUMN_FILE_SIZE,totalImageSize );
//				MediaHandler.insertUpdateMediaWithContentValues(LocalMediaChunksTable.CONTENT_URI,
//						getApplicationContext(), values, MediaHandler.updateRow,LocalMediaChunksTable.COLUMN_ID+"=?",
//						new String []{currentImageID});
			//}		
		
	}

}
