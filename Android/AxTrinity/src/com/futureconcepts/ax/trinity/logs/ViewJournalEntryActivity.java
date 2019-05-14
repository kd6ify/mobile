package com.futureconcepts.ax.trinity.logs;
import java.io.File;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.futureconcepts.ax.model.data.JournalEntry;
import com.futureconcepts.ax.model.data.JournalEntryMedia;
import com.futureconcepts.ax.model.data.JournalEntryPriorityBinding;
import com.futureconcepts.ax.model.data.Media;
import com.futureconcepts.ax.model.data.SourceType;
import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.ax.trinity.ViewItemActivity;
import com.futureconcepts.ax.trinity.logs.images.DownloadImagesFromServer.DownloadImageNotifier;
import com.futureconcepts.ax.trinity.logs.images.DownloadImagesFromServer;
import com.futureconcepts.ax.trinity.logs.images.EntryImageObject;
import com.futureconcepts.ax.trinity.logs.images.EntryImageObjectManager;
import com.futureconcepts.ax.trinity.logs.images.GetImageBitmap;
import com.futureconcepts.ax.trinity.logs.images.ImageManager;
import com.futureconcepts.ax.trinity.logs.images.ImageManager.ImageManagerGetImageListener;
import com.futureconcepts.ax.trinity.logs.images.SendImageToServer;
import com.futureconcepts.localmedia.database.LocalMediaTable;
import com.futureconcepts.localmedia.operations.MediaHandler;

public class ViewJournalEntryActivity extends ViewItemActivity implements ImageManagerGetImageListener, DownloadImageNotifier
{
	private JournalEntry _journalEntry;
	private MyAdapter _adapter;
	private ImageManager imageManager;
	private static final int MY_ITEM_TYPE_SOURCE = ITEM_TYPE_SPECIAL + 1;
	private static final int MY_ITEM_TYPE_IMAGE = ITEM_TYPE_SPECIAL + 2;	
	
    private ViewItemDescriptor[] _myItems = {
    		new ViewItemDescriptor( "Text", ITEM_TYPE_TEXT, JournalEntry.TEXT ),
    		new ViewItemDescriptor( "Status", ITEM_TYPE_INDEXED_TYPE, null ),
    		new ViewItemDescriptor( "Priority", ITEM_TYPE_JOURNAL_ENTRY_PRIORITY, JournalEntry.PRIORITY ),
    		new ViewItemDescriptor( "Log Time", ITEM_TYPE_DATE_TIME, JournalEntry.JOURNAL_TIME),
    		//Hide Source to fix bug #8851
    		//new ViewItemDescriptor( "Source", MY_ITEM_TYPE_SOURCE, JournalEntry.SOURCE ),
    		new ViewItemDescriptor( "Image", MY_ITEM_TYPE_IMAGE, null )
    };		
  
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_log_entry);
        setDefaultOptionsMenu(true);        
        if (getData() != null)
        {
			startManagingModel(_journalEntry = JournalEntry.query(this, getData()));
			if (moveToFirstIfOneRow())
			{	
	        		setTitle("Log Entry: " + _journalEntry.getText());	        
	        		_adapter = new MyAdapter();
	        		setListAdapter(_adapter);
	        		registerContentObserver(_adapter);
	        		getEntryImages();
			}		
			
        }
        imageManager= new ImageManager(this);
     }
    
    
    
    private void getEntryImages()
    {
    	MediaHandler.madeFailImagesAvailable(this, MediaHandler.ACTION_DOWNLOAD,  _journalEntry.getID());
    	EntryImageObjectManager.currentJournalEntry =  _journalEntry.getID();
    	JournalEntryMedia _journalEntryMedia;
    	startManagingCursor(_journalEntryMedia = JournalEntryMedia.queryJournalEntryMedia(this, _journalEntry.getID(),JournalEntryMedia.JOURNAL_ENTRY));
    	if(_journalEntryMedia.getCount()>0){
    		_journalEntryMedia.moveToFirst();    		
    		try{
    			do
    			{
    				Cursor media;
    				startManagingCursor(media = MediaHandler.queryLocalMediaID(this, _journalEntryMedia.getMediaID(), null));
    				if(media.getCount()>0 ){
    					media.moveToFirst();
    					String filePath = media.getString(media.getColumnIndex("filePath"));    					
    					if(MediaHandler.STATUS_COMPLETE.equals(media.getString(media.getColumnIndex("status"))) || MediaHandler.ACTION_UPLOAD.equals(media.getString(media.getColumnIndex("action"))))
    					{
    						if(!(new File(filePath).exists()))
    						{
    							MediaHandler.deleteMediaWithID(LocalMediaTable.CONTENT_URI,this, LocalMediaTable.COLUMN_ID+"=?",new String[]{media.getString(media.getColumnIndex("ID"))} );
    							addMediaFromServerData(_journalEntryMedia.getMediaID());
    						}else{    						
    							EntryImageObjectManager.images.add(new EntryImageObject(media.getString(media.getColumnIndex("ID")),filePath,EntryImageObjectManager.NO_NEED_DOWNLOAD,_journalEntry.getID()));
    						}
    					}else
    					{
    						EntryImageObjectManager.images.add(new EntryImageObject(media.getString(media.getColumnIndex("ID")),filePath,EntryImageObjectManager.IS_DOWNALODING,_journalEntry.getID()));
    					}
    				}else//we have media to download
    				{
    					addMediaFromServerData(_journalEntryMedia.getMediaID());
    				}    				
    			}while(_journalEntryMedia.moveToNext());
    			_adapter.notifyDataSetChanged();
    			callDownloadImageService();    			
    		}catch(Exception e)
    		{
    			e.printStackTrace();
    		}	
    	}
    }
    
    private void callDownloadImageService()
    {
    	EntryImageObjectManager.callService(this, EntryImageObjectManager.DownloadImageService, _journalEntry.getID());
    }
    
    private void addMediaFromServerData(String entryMediaID)
    {
    	Media mediaInServer = Media.queryMediaId(this,entryMediaID);
		if(mediaInServer.moveToFirst()){
			int size = mediaInServer.getMediaSize();
			EntryImageObjectManager.images.add(new EntryImageObject(mediaInServer.getID(),mediaInServer.getMediaNotes(),EntryImageObjectManager.NEED_DOWNLOAD,size,_journalEntry.getID()));	
			mediaInServer.close();
			mediaInServer = null;			
		}else{
			mediaInServer.close();
		}
    }

    @Override
    public void onResume()
    {
    	super.onResume();
    	DownloadImagesFromServer.setDownloadImageNotifierListener(this);
    	moveToFirstIfOneRow(); 
    	verifyMedia();  
	}
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	DownloadImagesFromServer.removeDownloadNotifierListener(this);
    }
    
        
    public void verifyMedia()
    {    
    	new Thread(new Runnable() {
    		public void run() {  	        	
    	     	if(MediaHandler.getAllMedia(ViewJournalEntryActivity.this, MediaHandler.ACTION_UPLOAD).size()>0 && !SendImageToServer.isMyServiceRunning(ViewJournalEntryActivity.this))
    	       	{
    	      		runOnUiThread(new Runnable() {
    	       		    public void run() {
    	       		    	((LinearLayout)findViewById(R.id.resendImages)).setVisibility(View.VISIBLE);
    	       		    }
    	       		});    	       		
    	       	}else
    	       	{
    	       		runOnUiThread(new Runnable() {
    	       		    public void run() {
    	       		    	((LinearLayout)findViewById(R.id.resendImages)).setVisibility(View.GONE);
    	       		    }
    	       		});    	       		
    	       	}    	        	
    	       }
    	 }).start();    	
    }
    
    public void sendImages(View view)
    { 
    	EntryImageObjectManager.callService(this,EntryImageObjectManager.SendImagesService,null);  
    	((LinearLayout)findViewById(R.id.resendImages)).setVisibility(View.GONE);
    }
    
    
    public void goBack(View view)
	{
		finish();
	}
	public void displayMenuOptions(View view)
	{
		final String[] options = {"Edit Entry","Delete Entry"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle("Menu");
		builder.setItems(options,new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if("Edit Entry".equals(options[which]))
						{
							onMenuEditItem();
						}else if("Delete Entry".equals(options[which]))
						{
							onMenuDeleteItem();
						} 
						dialog.dismiss();
					}
				});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {					
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
    
    @Override
	protected void onMenuDeleteItem()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to delete this log entry?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    	   public void onClick(DialogInterface dialog, int id)
		    	   {
		    		   ViewJournalEntryActivity.super.onMenuDeleteItem();		
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id)
		           {
		        	   dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}   

	private final class MyAdapter extends ViewItemAdapter
	{
		public MyAdapter()
		{
			super(_journalEntry, _myItems);
			DateTime.setContext(ViewJournalEntryActivity.this);
			DateTimeZone.setProvider(null);
		}
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View result = null;			
			ViewItemDescriptor vid = _myItems[position];
			if ( (_journalEntry != null) && (_journalEntry.getCount() == 1))
			{
				if (vid.type == ITEM_TYPE_TEXT)
				{
					result = getTextViewAndSetColor(vid,_journalEntry.getText(),
							JournalEntryPriorityBinding.getPriorityColor(null,_journalEntry.getPriority()));
				}
				else if (vid.type == ITEM_TYPE_INDEXED_TYPE)
				{
					if (vid.displayName.equals("Type"))
					{
						result = getIndexedTypeView(vid, _journalEntry.getType(ViewJournalEntryActivity.this));
					}
					else
					{
						result = getIndexedTypeView(vid, _journalEntry.getStatus(ViewJournalEntryActivity.this));
					}
				}
				else if (vid.type == MY_ITEM_TYPE_SOURCE)
				{
					result = getSourceView(vid, _journalEntry.getSourceType(ViewJournalEntryActivity.this));
				}
				else if(vid.type == MY_ITEM_TYPE_IMAGE){					
						result = getImageView(vid,ViewJournalEntryActivity.this,EntryImageObjectManager.images,imageManager);
				}else
				{
					result = super.getView(position, convertView, parent);
				}
			}
			else
			{
				result = this.getTextView(vid, "entry deleted");
			}
			return result;
		}
		
		private View getSourceView(ViewItemDescriptor vid, SourceType sourceType)
		{
			View result = null;
			if (sourceType != null)
			{
				result = getTextView(vid, sourceType.getName());
			}
			else
			{
				result = getTextView(vid, "Unknown");
			}
			return result;
		}
		
		@Override
		public void notifyDataSetChanged()
		{
			clearViewCache();
			super.notifyDataSetChanged();
		}
		
	}

	 @Override
	    public void onDestroy() {
		    EntryImageObjectManager.relationIDPosition.clear();
	       if(imageManager!=null){
	    	   imageManager.clearCache();
	    	   imageManager.removeImageManagerGetImageListener(this);
	    	   imageManager.close();
	       }	     
	        super.onDestroy();
	    }

		@Override
		public Bitmap getImage(String imageID, String filePath, int defaultDrawable) {
			// TODO Auto-generated method stub	
			return GetImageBitmap.lessResolution(filePath,50,50);
			//	bm.setDensity(Bitmap.DENSITY_NONE);
		}

		@Override
		public void downloadComplete(String imageID, String serverResponse,String filePath) {
			// TODO Auto-generated method stub
			if(EntryImageObjectManager.relationIDPosition.containsKey(imageID)){
			 int position = EntryImageObjectManager.relationIDPosition.get(imageID);
			 View container = (View)findViewById(R.id.view_log_entry_container);
				final TextView txt = (TextView)container.findViewWithTag("Text"+imageID);
			final Bitmap bm = GetImageBitmap.lessResolution(filePath,50,50);
			final ImageButton image = (ImageButton)container.findViewWithTag(imageID);
			final ProgressBar progressBar = (ProgressBar)container.findViewWithTag("ProgressBar"+imageID);
			Handler mHandler = new Handler(getMainLooper());
    	    mHandler.post(new Runnable() {
    	        @Override
    	        public void run() {
    	        	if(image!=null ){
    	        		image.setImageBitmap(bm);
    	        		image.setOnClickListener(viewImageClickListener);
    	        	}  
    	        	if( txt!=null){txt.setVisibility(View.GONE);}
    	        	if( progressBar!=null){progressBar.setVisibility(View.GONE);}
    	        }
    	    });	        	
    	    if(bm!=null){
    	    	imageManager.addBitmapToMemoryCache(imageID, bm);
    	    }
			if(EntryImageObjectManager.images.size()>position){
				EntryImageObjectManager.images.get(position).setImagePath(filePath);
				EntryImageObjectManager.images.get(position).setNeedDownload(EntryImageObjectManager.NO_NEED_DOWNLOAD);   	
			}
			}
		}

		@Override
		public void downloadFail(String imageID, final String serverResponse) {
			// TODO Auto-generated method stub
			View container = (View)findViewById(R.id.view_log_entry_container);
			final TextView txt = (TextView)container.findViewWithTag("Text"+imageID);
			final ImageButton image = (ImageButton)container.findViewWithTag(imageID);	
			final ProgressBar progressBar  = (ProgressBar)container.findViewWithTag("ProgressBar"+imageID);
    		Handler mHandler = new Handler(getMainLooper());
    	    mHandler.post(new Runnable() {
    	        @Override
    	        public void run() {
    	        	if(image!=null ){image.setImageResource(R.drawable.download_fail);}  
    	        	if( txt!=null){txt.setVisibility(View.GONE);}
    	        	if( progressBar!=null){progressBar.setVisibility(View.GONE);}
    	        	if(serverResponse!=null)
    	        	Toast.makeText(ViewJournalEntryActivity.this,serverResponse, Toast.LENGTH_LONG).show();
    	        }
    	    });	          		
		}

		@Override
		public void downloadUpdateProgress(final String imageID, final int progress) {
			// TODO Auto-generated method stub	 
			if(EntryImageObjectManager.relationIDPosition.containsKey(imageID)){
		        View container = (View)findViewById(R.id.view_log_entry_container);
				final TextView txt = (TextView)container.findViewWithTag("Text"+imageID);
				final ProgressBar progressBar = (ProgressBar)container.findViewWithTag("ProgressBar"+imageID);
				if(txt!=null)
				txt.post(new Runnable(){
					@Override
					public void run() {
						if(progress<100)
						{
							if(txt!=null)
								txt.setVisibility(View.VISIBLE);
							    txt.setText(progress+"%");
							    progressBar.setVisibility(View.VISIBLE);
							    progressBar.setProgress(progress);							    
						}
					}
				});	    
			
			}		
		}


}