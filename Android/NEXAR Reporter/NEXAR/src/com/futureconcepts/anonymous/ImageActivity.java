package com.futureconcepts.anonymous;

import java.io.File;
import java.util.HashMap;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;
import com.futureconcepts.customclass.ImageAdapter;
import com.futureconcepts.customclass.SingletonInformation;



public class ImageActivity extends Activity implements OnClickListener {

	ImageAdapter imgAdapter;
	File fileImage;
	private GridView gv;
	private int TAKE_PICTURE = 100;
	private int SELECT_PICTURE = 101;
	private long mLastClickTime = 0;
	public static HashMap<String,Integer> GalleryList = new HashMap<String,Integer>();
	public static String sFilePath = ""; 
	public static File CurrentFile = null;
	public static Uri CurrentUri = null;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.image_activity);
		//Get the current context to display the AlertDialog for not send it reports.
		SingletonInformation.getInstance().currentCon=this;
		setGridView();
		displayImages();
		fillPhotoList();
	}
	
	protected void onDestroy() {
		super.onDestroy();
		GalleryList.clear();
	}
	/*
	 * Sets the Grid view and adds the click listener.
	 * */
	public void setGridView(){
	    findViewById(R.id.select_image).setOnClickListener(this);
		findViewById(R.id.take_image).setOnClickListener(this);
		gv = (GridView) findViewById(R.id.grid_view);
		//imgAdapter = new ImageAdapter(this);
		imgAdapter = new ImageAdapter(ImageActivity.this,SingletonInformation.getInstance().bitmapImages );
		gv.setAdapter(imgAdapter);
		//gv.setAdapter(new ImageAdapter(this,ImageAdaper.class));
		gv.setOnItemClickListener(new OnItemClickListener() {			 
			 @Override
			 public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
						 
				imageActions(v,position);

		        }
		    });
	}
	
	/*Displays the selected images if exists.
	 * Only is execute oncreate.
	 * */
	public void displayImages(){
		  imgAdapter.notifyDataSetChanged();
	}
	public boolean onOptionsItemSelected(MenuItem item)
    {
		 switch (item.getItemId())
	        {
	        case R.id.menu_settings:
	        	Intent d = new Intent(this,ChooseSchool.class);
	    		startActivity(d);	        	
	        	break;
	        }
		return false;
		
    }
	
	//Displays the photo gallery
	public void selectImage() {
			if (SingletonInformation.getInstance().bitmapImages.size() < 5) {				
				Intent i = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				i.setType("image/*");
				startActivityForResult(i, 101);

			} else {
				alertDialog("","You can't attach more than 5 images per report.");
			}
	}
	
	  /*Initialises the camera and adds the uri for the image that will be taken.
	   * */
	  public void takeImage(){
			if (SingletonInformation.getInstance().bitmapImages.size() < 5) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				// Specify the output. This will be unique.
				setsFilePath(getTempFileString());
				intent.putExtra(MediaStore.EXTRA_OUTPUT, CurrentUri);
				Log.d("currentur is", "" + CurrentUri + " size is "	+ CurrentFile.length());
				// Keep a list for afterwards
				fillPhotoList();
				// finally start the intent and wait for a result.
				startActivityForResult(intent, 100);

			} else {
				alertDialog("","You can't attach more than 5 images per report.");
			}
	  }
	  
	  
	  
	  /***/
	  /*Sets a valid uri from the file where the image will be stored.
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
		  Log.d("aaaa","aaaaaaa");
	     // Only one time will we grab this location.
	     final File path = new File(Environment.getExternalStorageDirectory(), 
	           getString(getApplicationInfo().labelRes));
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
	  
	  /****/
	  
	  
    
	  //Generates the thumbail of the selected image. 
	  public Bitmap lessResolution (String filePath)
		{   int reqHeight=110;
		    int reqWidth=110;
			 BitmapFactory.Options options = new BitmapFactory.Options();	
			
			    // First decode with inJustDecodeBounds=true to check dimensions
			    options.inJustDecodeBounds = true;
			    BitmapFactory.decodeFile(filePath, options);

			    // Calculate inSampleSize
			    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

			    // Decode bitmap with inSampleSize set
			    options.inJustDecodeBounds = false;		   
			    
			    return BitmapFactory.decodeFile(filePath, options); 
		}
		
	  public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		
		final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;

	    if (height > reqHeight || width > reqWidth) {

	        // Calculate ratios of height and width to requested height and width
	        final int heightRatio = Math.round((float) height / (float) reqHeight);
	        final int widthRatio = Math.round((float) width / (float) reqWidth);

	        // Choose the smallest ratio as inSampleSize value, this will guarantee
	        // a final image with both dimensions larger than or equal to the
	        // requested height and width.
	        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	    }
	    return inSampleSize;
		}
	  
	  /*Verifies if the image was already selected
	   * If false, stores the selected image.
	   * */
	  public void sameImage(String filePath,Bitmap imageBitmap){
		  boolean exists=false;
		  for(int a=0; a<SingletonInformation.getInstance().pathImages.size(); a++){
			  if(filePath.matches(SingletonInformation.getInstance().pathImages.get(a)))
	 			 {
	 				 exists=true;
	 				 break;
	 			 }
		  	}
		  if(exists==false){
			  //Stores the Bitmap of the image.
			  SingletonInformation.getInstance().bitmapImages.add(imageBitmap);
			  //Stores the path of the image.
			  SingletonInformation.getInstance().pathImages.add(filePath);		 
			  imgAdapter.notifyDataSetChanged();
		}
		  else{
	  		Toast.makeText(getBaseContext(), "This image has already been attached.", Toast.LENGTH_SHORT).show();
	  	}
		  
	  }

	  //Displays a AlertDialog with the title and message specified.
	  public void alertDialog(String title,String message){
		  AlertDialog.Builder builder = new AlertDialog.Builder(this);
          builder.setTitle(title)
          	.setMessage(message)
              .setCancelable(false)
              .setNegativeButton("OK",new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                     	
                       }
                   });
                   AlertDialog alert = builder.create();
                   alert.show();
	  }
	  
	  public void back(View v){
		  finish();
	  }
	  
	  public void imageActions(final View v, final int position)
	  {		  
			//create the alert to show policies
				AlertDialog alert = new AlertDialog.Builder(this).create();
				alert.setMessage("Image Actions");
				alert.setButton("View Image", new DialogInterface.OnClickListener() {
				      public void onClick(DialogInterface dialog, int which) {
				    	  seeImage(v,position);
				    } });	
				
				alert.setButton2("Remove Image", new DialogInterface.OnClickListener() {
				      public void onClick(DialogInterface dialog, int which) {
				    	  deleteImage(v,position);
				    } });
				
				alert.setButton3("Cancel", new DialogInterface.OnClickListener() {
				      public void onClick(DialogInterface dialog, int which) {	
				    } }); 
				alert.show();
	 }
	  /*
	   * Displays the selected image
	   * */
	  public void seeImage(final View v, final int position)
	  {
		    Intent intent = new Intent();  
		    intent.setAction(android.content.Intent.ACTION_VIEW);  
		    File file = new File(SingletonInformation.getInstance().pathImages.get(position));		  
		    intent.setDataAndType(Uri.fromFile(file), "image/*");  
		    startActivity(intent);
	
	  }
	  
	  /**
	   * Deletes the path and bitmap of the selected image 
	   * */
	  public void deleteImage(final View v, final int position)
	  {
		
			AlertDialog alert = new AlertDialog.Builder(this).create();
			alert.setMessage("Are you sure you want to remove this image?");
			//prevent the user cancel the dialog
			alert.setCancelable(false);
			//alert.setIcon(R.drawable.ic_launcher);	
			alert.setButton2("OK", new DialogInterface.OnClickListener() {
			      public void onClick(DialogInterface dialog, int which) {
			          
						SingletonInformation.getInstance().pathImages.remove(position);
						SingletonInformation.getInstance().bitmapImages.remove(position);
						imgAdapter.notifyDataSetChanged();
				     
			    } });		
			alert.setButton3("Cancel", new DialogInterface.OnClickListener() {
			      public void onClick(DialogInterface dialog, int which) {	
			    	  
			    	 
			    } }); 
			alert.show();
	  }
	  public void setOptions(View view) {
			Intent intent = new Intent(this, ChooseSchool.class);
			startActivity(intent);

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
	     //
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
	        	//this is the key c.getString(0).
	          GalleryList.put(c.getString(0), count);
	          count+=1;//Value
	        }     
	        while (c.moveToNext());
	     }

	  }
 
	  /*This method is called after the user take a picture or select one
	   * from the gallery.
	   * */
	  protected void onActivityResult(int requestCode, int resultCode, Intent data)
	  {	
		  Log.d("camera","cam1");
	     if (requestCode == TAKE_PICTURE)
	     {
	    	 Log.d("camera","cam2 + sfsfsfsfsf"+CurrentFile.length());
	        // based on the result we either set the preview or show a quick toast splash.
	        if (resultCode == RESULT_OK)
	        {         
	            if(CurrentFile.length()>0){	
	        	boolean exist=false;
	        	sendBroadcast(new Intent(
						Intent.ACTION_MEDIA_MOUNTED,
						Uri.parse("file://" + Environment.getExternalStorageDirectory())));
	        	Log.d("camera","cam3");
	           // Some versions of Android save to the MediaStore as well. Not sure why!
	           //so we get to search for this.  
	           String[] projection = { MediaStore.Images.ImageColumns.SIZE,
	                                   MediaStore.Images.ImageColumns.DISPLAY_NAME,
	                                   MediaStore.Images.ImageColumns.DATA,
	                                   BaseColumns._ID,};    
	           // intialize the Uri and the Cursor.
	           Cursor c = null; 
	           Uri u = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	              // Query the Uri to get the data path.  Only if the Uri is valid,
	              if ((u != null))
	              {
	            	  Log.d("camera","cam4");
	                 c = managedQuery(u, projection, null, null, null);
	              }
	              // If we found the cursor.
	              if ((c != null) && (c.moveToFirst())) 
	              {
	            	  Log.d("camera","cam5");
	            	  do{
	                	 //Search in the list that we build before the image was taken.
	                	//We are looking for the image that is  not in GalleryList.
	                    boolean bFound = GalleryList.containsKey(c.getString(1));
	                    Log.d("camera","cam6");
	                    //       de
	                    // To here we loop the full gallery.
	                    if (!bFound)//if bFound is true mean this is not the new image.
	                    {
	                    	exist=true;
	                    	Log.d("camera","cam7");
	                       // c.getString(2) This is the NEW image (Path). The last image added to the gallery.  
	                       File f = new File(c.getString(2));
	                       // Ensure it's there,
	                       if ((f.exists())) 
	                       {
                                  //get the bitmap of the found it File.
	                              // Bitmap imageBitmap= lessResolution(f.getAbsolutePath());
	 	                    	   //sameImage(f.getAbsolutePath(),imageBitmap);
	                    	   Bitmap imageBitmap= lessResolution(CurrentFile.getAbsolutePath());
	                    	   sameImage(CurrentFile.getAbsolutePath(),imageBitmap);		                    	
	 		                   ContentResolver cr = getContentResolver();
	 		                   cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
	 		                   BaseColumns._ID + "=" + c.getString(3), null);
	 		                   break;
	                       }
	                       //Image already found, no need to keep searching.                        
	                    }
	                    Log.d("camera","cam9");
	                 }while (c.moveToNext());
	            	  
	                    if(!exist){
	                    	Log.d("camera","cam8");
	                    	Bitmap imageBitmap= lessResolution(CurrentFile.getAbsolutePath());
	                    	
	                    	sameImage(CurrentFile.getAbsolutePath(),imageBitmap);
	                    }
	            	  
	              }
	              Log.d("camera","cam10");
	                 GalleryList.clear();
	            }else
	            {
	            	alertDialog("Information","Your device do not support thise feature.");	 
	     	       
	            }     
	        }else if (resultCode == RESULT_CANCELED)
	        {
	        	
	        }else{
	        	alertDialog("","The image can't be taken select an image from the gallery.");	 
	        }	    
	          	
	     } else  if(requestCode == SELECT_PICTURE) {
	    	 if (resultCode == RESULT_OK)
		        {
	    		 Uri uriImage = data.getData();
	    		 String[] projection = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(uriImage, projection, null, null, null);
                cursor.moveToFirst();
                               
                int columnIndex = cursor.getColumnIndex(projection[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();
                Bitmap imageBitmap=null;
                try {

                	  imageBitmap = lessResolution(filePath);
                	 } catch (Exception e) {
                	  e.printStackTrace();
                	 }
                if(imageBitmap!=null){
                	sameImage(filePath,imageBitmap);
                }else{
                	alertDialog("Warning","This file is corrupted. Please select a different image or use Take Image to take a new one.");	 
                }
 
		       }else if (resultCode == RESULT_CANCELED)
		        {
		        	
		        }else{
		        	alertDialog("","The image can't be attached.");	 
		        }
        }	    
	  }

	@Override
	public void onClick(View view) {
		// prevent click multiple buttons at same time.
		 if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
	            return;
	        }
	        mLastClickTime = SystemClock.elapsedRealtime();
		switch(view.getId())
		{
		case R.id.take_image:
			takeImage();
			break;
		case R.id.select_image:
			selectImage();
			break;
		}
	}
	  

}
