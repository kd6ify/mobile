
package com.futureconcepts.anonymous;

import java.io.File;
import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

import com.futureconcepts.customclass.ImageAdapter;
import com.futureconcepts.database.AnonymousInfoMediaTable;
import com.futureconcepts.database.DatabaseHelper;
import com.futureconcepts.database.MediaTable;




public class ViewPendingImages extends Activity {
	public ProgressDialog progressDialog;
	ImageAdapter imgAdapter;
	private GridView gv;
	String IDReport;
	ArrayList<String> imagesPaths = new ArrayList<String>();
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_view_pending_images);
		imgAdapter = new ImageAdapter(this,null);
		Intent intent = getIntent();
		IDReport = intent.getStringExtra("IdReport");
		gv = (GridView) findViewById(R.id.grid_view_pending);
		gv.setOnItemClickListener(new OnItemClickListener() {			 
			 @Override
			 public void onItemClick(AdapterView<?> parent, View v, int position, long id) {						 
				imageActions(v,position);
		        }
		    });
		Log.d("test images","test1");
		 new LoadImages(this).execute();
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
		    File file = new File(imagesPaths.get(position));		  
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
						//imgAdapter.images.remove(position);
						//gv.setAdapter(new ImageAdapter(ImageActivity.this,SingletonInformation.getInstance().bitmapImages ));
			    	  deleteImageFromDatabase(position);
			    	  imgAdapter.images.remove(position);
			    	  imagesPaths.remove(position);
			    	  imgAdapter.notifyDataSetChanged();
			    	  if(imagesPaths.size()==0)
			    	  {
			    		  finish();
			    	  }
				      //v.setVisibility(View.INVISIBLE);	
				     
			    } });		
			alert.setButton3("Cancel", new DialogInterface.OnClickListener() {
			      public void onClick(DialogInterface dialog, int which) {	
			    	  			    	 
			    } }); 
			alert.show();
	  }
	  
	  private void deleteImageFromDatabase(int position)
	  {
		  DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
	        SQLiteDatabase newDB = dbHelper.getWritableDatabase();
	    	Cursor c = newDB.rawQuery("Select m.ID from Media AS m INNER JOIN AnonymousInfoMedia AS aim on m.ID = aim.Media AND aim.NEXARInfo=? Where m.File = ?",new String[]{IDReport,imagesPaths.get(position)});
			Log.d("PendingReportImages","Deleting image");
			if (c.getCount() != 0)
			{
				c.moveToFirst();
				try
				{
					String mediaID = c.getString(c.getColumnIndex("ID"));
					getContentResolver().delete(MediaTable.CONTENT_URI,"ID=?", new String [] {mediaID});			
					getContentResolver().delete(AnonymousInfoMediaTable.CONTENT_URI,"Media=?", new String [] {mediaID});

				}catch(Exception e)
				{
					e.printStackTrace();
				}		
			}			
			c.close();
			newDB.close();		
	  }
	
	private class LoadImages extends AsyncTask<Void, Object, Object>{
	public Context context;
	ArrayList<Bitmap> imagesContent;
	public LoadImages(Context con ){
		context=con;
		progressDialog=new ProgressDialog(context);
	}	

	@Override
    protected void onPreExecute() {
        progressDialog.setMessage("Loading Images please wait..");
        progressDialog.show();
        Log.d("test images","test2");
    }
    
	@Override
    protected Object doInBackground(Void... arg0) {
		imagesContent=new ArrayList<Bitmap>();
		imagesContent=getImages();
		return null;
	}
	
	 @Override
	    protected void onPostExecute (Object result){
		 if (progressDialog.isShowing()) {
 			progressDialog.dismiss();
         }
		 
			if(imagesContent.size()>0){
			   for(Bitmap t: imagesContent){
				  imgAdapter.images.add(t);
				}
				gv.setAdapter(imgAdapter);
			}
	 }

	//Get the pending images from the paths in Media
	public ArrayList<Bitmap> getImages(){
		Log.d("test images","test3");
		ArrayList<Bitmap> temp= new ArrayList<Bitmap>();
	    Bitmap imageBitmap=null;
	    String filePath="";

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase newDB = dbHelper.getWritableDatabase();
    	Cursor US = newDB.rawQuery("Select Distinct * from Media US Inner Join AnonymousInfoMedia sam on US.ID=sam.Media AND sam.NEXARInfo=?",new String[]{IDReport});
		
    	Log.d("test images","test4");
		if (US.getCount() != 0)
		{
			US.moveToFirst();
			do {
				filePath=US.getString(US.getColumnIndex("File"));
				 imagesPaths.add(filePath);
		        imageBitmap = lessResolution(filePath);
		        temp.add(imageBitmap);
			} while (US.moveToNext());

		}
		
        US.close();
        newDB.close();
        Log.d("test images","test5");
        return temp;
	}
	  //Generates the thumbail of the selected image. 
	  public Bitmap lessResolution (String filePath)
		{   int reqHeight=100;
		    int reqWidth=100;
			final BitmapFactory.Options options = new BitmapFactory.Options();	
			
			    // First decode with inJustDecodeBounds=true to check dimensions
			    options.inJustDecodeBounds = true;
			    BitmapFactory.decodeFile(filePath, options);

			    // Calculate inSampleSize
			    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

			    // Decode bitmap with inSampleSize set
			    options.inJustDecodeBounds = false;
			    return BitmapFactory.decodeFile(filePath, options); 
		}
		
	  public  int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		
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
	  
	}
}
