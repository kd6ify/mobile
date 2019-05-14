package com.futureconcepts.cameraintent;

import java.io.File;
import java.util.HashMap;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Window;


public class TakePhoto extends Activity {
	

	File fileImage;
	public static HashMap<String,Integer> GalleryList = new HashMap<String,Integer>();
	public static String sFilePath = ""; 
	public static File CurrentFile = null;
	public static Uri CurrentUri = null;
	private int takeOrSelectPhoto = 0;
	private String folderName;
	private int RESULT_ERROR = 999;
	private final String TAG = TakePhoto.class.getSimpleName();
	Context context;
	Activity activityCaller;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//if (savedInstanceState == null) {
			Bundle sender = getIntent().getExtras();
			takeOrSelectPhoto = sender.getInt("action");
			folderName = sender.getString("folderName");
			if (takeOrSelectPhoto == TakePhotoIntentGlobals.TAKE_PICTURE) {
				takeImage();
			} else if (takeOrSelectPhoto == TakePhotoIntentGlobals.SELECT_PICTURE) {
				selectImage();
			} else {
				// Unknown action
				returnResult("UnknownAction", RESULT_CANCELED, null);
			}
		//}
	}
	
	private void returnResult(String Result, int resultCode, String FilePath) {
		Intent returnIntent = new Intent();
		returnIntent.putExtra("result", Result);
		returnIntent.putExtra("filePath", FilePath);
		setResult(resultCode, returnIntent);		
		finish();
	}

	 /**
	   * Stores in a list(GalleryList) all the images on the device.
	   * */
	  private void fillPhotoList()
	  {
	     GalleryList.clear();
	     String[] projection = { MediaStore.Images.ImageColumns.DISPLAY_NAME };
	     // intialize the Uri and the Cursor.
	     Cursor c = null; 
	     Uri u = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	     // Query the Uri to get the data path.  Only if the Uri is valid.
	     if (u != null)
	     {
	        c = managedQuery(u, projection, null, null, null);
	     }
	     // If we found the cursor and found a record in it (we also have the id).
	     if ((c != null) && (c.moveToFirst())) 
	     {
	    	 int count=0;
	        do 
	        {
	          // Loop each and add to the list.
	          GalleryList.put(c.getString(0), count);//this is the key c.getString(0).
	          count+=1;//Value
	        }     
	        while (c.moveToNext());
	     }

	  }
	  
	// Displays the photo gallery
	public void selectImage() {
		Intent i = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		i.setType("image/*");
		startActivityForResult(i, TakePhotoIntentGlobals.SELECT_PICTURE);
	}
		
	/*
	 * Initialises the camera and adds the uri for the image that will be taken.
	 */
	public void takeImage() {
		if (isExternalStorageWritable()) {
			PackageManager pm = getPackageManager();
			if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA))
			{
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				// Specify the output. This will be unique.
				setsFilePath(getTempFileString());
				intent.putExtra(MediaStore.EXTRA_OUTPUT, CurrentUri);
				Log.d(TAG, "" + CurrentUri + " size is " + CurrentFile.length());
				// Keep a list for afterwards
				fillPhotoList();
				// finally start the intent and wait for a result.
				startActivityForResult(intent, TakePhotoIntentGlobals.TAKE_PICTURE);
			}else{
				pm = null;
				returnResult("Camera is not available", RESULT_CANCELED, null);
			}
			 //howCustomToast("Camera is not available");			
		}else
		{
			returnResult("External storage is not available ",RESULT_ERROR,null);
		}
	}
		  
	  
	  /*This method is called after the user take a picture or select one
	   * from the gallery.
	   * */
	  protected void onActivityResult(int requestCode, int resultCode, Intent data)
	  {	
	     if (requestCode == TakePhotoIntentGlobals.TAKE_PICTURE)
	     {
	        // based on the result we either set the preview or show a quick toast splash.
	        if (resultCode == RESULT_OK)
	        {         
	            if(CurrentFile.length()>0){	
	            	//Intent.ACTION_MEDIA_MOUNTED before was this but in kitkak it will crash new security change
	        	sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
	        			Uri.parse("file://" + Environment.getExternalStorageDirectory())));	        
	           // Some versions of Android save to the MediaStore as well. Not sure why!
	           //so we need to search for this.  
	           String[] projection = { MediaStore.Images.ImageColumns.SIZE,
	                                   MediaStore.Images.ImageColumns.DISPLAY_NAME,
	                                   MediaStore.Images.ImageColumns.DATA,
	                                   BaseColumns._ID,};    
	           // intialize the Uri and the Cursor.
	           Cursor c = null; 
	         //  String filePath = null;
	           Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	              // Query the Uri to get the data path.  Only if the Uri is valid,
	              if ((uri != null))
	              {	            	  
	                 c = managedQuery(uri, projection, null, null, null);
	              }
	              // If we found the cursor.
	              if ((c != null) && (c.moveToFirst())) 
	              {	     
	            	  do{
	                	 //Search in the list that we build before the image was taken.
	                	//We are looking for the image that is  not in GalleryList.
	                    boolean found = GalleryList.containsKey(c.getString(1));        
	                    // here we loop the full gallery.
	                    if (!found)//if found is true mean this is not the new image.
	                    {
	                      // exist=true;                  
	                       File f = new File(c.getString(2));
	                       // Ensure it's there,
	                       if ((f.exists() && !c.getString(2).contains(folderName))) 
	                       {
	 		                   ContentResolver cr = getContentResolver();
	 		                   cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
	 		                   BaseColumns._ID + "=" + c.getString(3), null);
	 		                   Log.d(TAG, "Image Deletete: "+c.getString(2));
	 		                   break;//Image already found, no need to keep searching.      
	                       }	                                         
	                    }	                    
	                 }while (c.moveToNext());
	            	  c.close();
	            	  GalleryList.clear();
	            	  returnResult("success", RESULT_OK,CurrentFile.getAbsolutePath());
	              }else{             
	                 GalleryList.clear();
	                 returnResult("The picture can't be taken select one from the gallery.", RESULT_ERROR,null);
	              }
	            }else
	            { 
	            	returnResult("Your device do not support this feature.",RESULT_CANCELED,null);
	            }     
	        }else if (resultCode == RESULT_CANCELED)
	        {
	        	returnResult("Take Picture Canceled", RESULT_CANCELED,null);
	        }else{
	        	returnResult("The picture can't be taken select one from the gallery.",RESULT_ERROR,null);
	        }	    
	          	
		} else if (requestCode == TakePhotoIntentGlobals.SELECT_PICTURE) {
			if (resultCode == RESULT_OK) {
				Uri uriImage = data.getData();
				String[] projection = { MediaStore.Images.Media.DATA };
				Cursor cursor = getContentResolver().query(uriImage,projection, null, null, null);
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(projection[0]);
				String filePath = cursor.getString(columnIndex);
				cursor.close();
				returnResult("success", RESULT_OK,filePath);
			} else if (resultCode == RESULT_CANCELED) {
				returnResult("Select Picture Canceled", RESULT_CANCELED,null);
			} else {
				returnResult("Unknown Error", RESULT_ERROR, null);
			}
		}
	  }	
	  
	  
	   /**Sets a valid uri from the file where the image will be stored.
	   * "value" is the path where the image will  be stored.
	   * */
	  public void setsFilePath(String value)
	  {
	     // We just updated this value. Set the property first.
	     sFilePath = value;
	     // initialize these two
	     CurrentFile = null;
	     CurrentUri = null;
	     // If we have something real, setup the file and the Uri.
	     if (!sFilePath.equalsIgnoreCase(""))
	     {
	        CurrentFile = new File(sFilePath);
	        CurrentUri = Uri.fromFile(CurrentFile);
	     }
	  }
	  
	  /**
	   * Creates a Folder to store the images taken with the camera.
	   * The folder will have the name of the app(NEXAR).
	   * */
	  private String getTempFileString()
	  {
		  Log.d(TAG,"get the folder path were we are goign to save our image");
	     // Only one time will we grab this location.
	     final File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),folderName);
	     // If this does not exist, we can create it here.
	     if (!path.exists())
	     {
	        path.mkdir();
	        //Notifies folder created
	        sendBroadcast(new Intent(
					Intent.ACTION_MEDIA_MOUNTED,
					Uri.parse("file://" + Environment.getExternalStorageDirectory())));
	     }
	     return new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg").getPath();
	  }
	  
	  /* Checks if external storage is available for read and write */
	  public boolean isExternalStorageWritable() {
	      String state = Environment.getExternalStorageState();
	      if (Environment.MEDIA_MOUNTED.equals(state)) {
	          return true;
	      }
	      return false;
	  }

	  /* Checks if external storage is available to at least read */
	  public boolean isExternalStorageReadable() {
	      String state = Environment.getExternalStorageState();
	      if (Environment.MEDIA_MOUNTED.equals(state) ||
	          Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	          return true;
	      }
	      return false;
	  }
	  
}
