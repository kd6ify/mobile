package com.futureconcepts.ax.trinity;

import java.io.File;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Agency;
import com.futureconcepts.ax.model.data.Icon;
import com.futureconcepts.ax.model.data.IndexedType;
import com.futureconcepts.ax.model.data.JournalEntryPriorityBinding;
import com.futureconcepts.ax.trinity.logs.images.CustomAlertDialog;
import com.futureconcepts.ax.trinity.logs.images.EntryImageObject;
import com.futureconcepts.ax.trinity.logs.images.EntryImageObjectManager;
import com.futureconcepts.ax.trinity.logs.images.ImageManager;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ViewItemActivity extends ModelListActivity 
{
//	private static final String TAG = ViewItemActivity.class.getSimpleName();
	
	public static final int ITEM_TYPE_TEXT = 0;
	public static final int ITEM_TYPE_INDEXED_TYPE = 1;
	public static final int ITEM_TYPE_LOCATION = 5;
	public static final int ITEM_TYPE_DATE_TIME = 6;
	public static final int ITEM_TYPE_DATE_TIME_RANGE = 7;
	public static final int ITEM_TYPE_INT = 8;
	public static final int ITEM_TYPE_REAL = 9;
	public static final int ITEM_TYPE_BOOLEAN_CHECKBOX = 11;
	public static final int ITEM_TYPE_BLOB_AS_STRING = 12;
	public static final int ITEM_TYPE_ADDRESS = 13;
	public static final int ITEM_TYPE_JOURNAL_ENTRY_PRIORITY = 14;
	public static final int ITEM_TYPE_SPECIAL = 1000;
	private boolean _useDefaultOptionsMenu = false;	
	private View[] _viewCache;
	//protected ImageManager imageManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
	    super.onCreate(savedInstanceState);
	}
    
	@Override
	public void onResume()
	{
		moveToFirstIfOneRow();
		super.onResume();
	}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		if (useDefaultOptionsMenu())
		{
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.view_item_options_menu, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_edit:
			onMenuEditItem();
			break;
		case R.id.menu_delete:
			onMenuDeleteItem();
			break;
		}
		return false;
	}

	public boolean useDefaultOptionsMenu()
    {
    	return _useDefaultOptionsMenu;
    }
    
    public void setDefaultOptionsMenu(boolean value)
    {
    	_useDefaultOptionsMenu = value;
    }  
	
	protected void onMenuEditItem()
	{
		finish();
		startActivity(new Intent(Intent.ACTION_EDIT, getData()));
	}

	protected void onMenuDeleteItem() {
		try {
			if (getData() != null) {				
				getContentResolver().delete(getData(), null, null);
				finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void clearViewCache()
	{
		for (int i = 0; i < _viewCache.length; i++)
		{
			_viewCache[i] = null;
		}
	}
	
	public class ViewItemDescriptor
	{
		public String displayName;
		public int type;
		public String source1;
		public String source2;
		public boolean isClickable;
		
		public ViewItemDescriptor(String displayName, int type, String source1, String source2)
		{
			this.displayName = displayName;
			this.type = type;
			this.source1 = source1;
			this.source2 = source2;
			this.isClickable = false;
		}
		public ViewItemDescriptor(String displayName, int type, String source1, boolean isClickable)
		{
			this(displayName, type, source1, null);
			this.isClickable = isClickable;
		}
		public ViewItemDescriptor(String displayName, int type, String source1)
		{
			this(displayName, type, source1, null);
		}
	}
    
	public class ViewItemAdapter extends BaseAdapter
	{
		private ViewItemDescriptor[] _items;
		private Cursor _cursor;
		private ViewStub _viewStub;
		
		public ViewItemAdapter(Cursor cursor, ViewItemDescriptor[] items)
		{
			_cursor = cursor;
			_items = items;
			_viewStub = new ViewStub(ViewItemActivity.this);
			_viewStub.setEnabled(false);
			_viewCache = new View[items.length];
			
		}

		@Override
		public int getCount() 
		{
			if (_cursor.getCount() == 0)
			{
				return 0;
			}
			else
			{
				return _items.length;
			}
		}

		@Override
		public Object getItem(int position)
		{
			return position;
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (_viewCache[position] == null)
			{
				_viewCache[position] = getItemView(_cursor, _items[position]);
			}
			return _viewCache[position];
		}
		
		@Override
		public boolean isEnabled(int position)
		{
			return _items[position].isClickable;
		}
		
		protected View getItemView(Cursor cursor, ViewItemDescriptor vid)
		{
			View result = _viewStub;
			try
			{
				int ci = cursor.getColumnIndex(vid.source1);
				if (ci != -1)
				{
					switch (vid.type)
					{
						case ITEM_TYPE_TEXT:
							result = getTextView(vid, cursor.getString(ci));
							break;
						case ITEM_TYPE_DATE_TIME:
							String strVal = cursor.getString(ci);
							if (strVal != null)
							{
								DateTime val = DateTime.parse(strVal);
								result = getDateTimeView(vid, val);
							}
							else
							{
								result = getTextView(vid, "");
							}
							break;
						case ITEM_TYPE_DATE_TIME_RANGE:
							result = getDateTimeRangeView(vid, cursor.getString(ci), cursor.getString(cursor.getColumnIndex(vid.source2)));
							break;
						case ITEM_TYPE_INT:
							result = getIntView(vid, cursor.getInt(ci));
							break;
						case ITEM_TYPE_REAL:
							result = getDoubleView(vid, cursor.getDouble(ci));
							break;
						case ITEM_TYPE_BOOLEAN_CHECKBOX:
							int val2 = cursor.getInt(ci);
							result = getBooleanCheckboxView(vid, val2 == 1);
							break;
						case ITEM_TYPE_BLOB_AS_STRING:
							result = getTextView(vid, new String(cursor.getBlob(ci)));
							break;
						case ITEM_TYPE_JOURNAL_ENTRY_PRIORITY:
							result = getTextView(vid, new String(JournalEntryPriorityBinding.intToString(cursor.getInt(ci))));
							break;
					}
				}
				if (result != null)
				{
					if (vid.isClickable)
					{
						ImageView imageView = (ImageView)result.findViewById(R.id.clickable);
						if (imageView != null)
						{
							imageView.setImageResource(android.R.drawable.ic_menu_more);
						}
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return result;
		}
						
		protected View getTextView(ViewItemDescriptor vid, String value)
		{
			View view = getNameValueListItem();
			((TextView)view.findViewById(R.id.name)).setText(vid.displayName);
			((TextView)view.findViewById(R.id.value)).setText(value);
			return view;
		}
		
		protected View getTextViewAndSetColor(ViewItemDescriptor vid, String value, int color)
		{
			View view = getNameValueListItem();
			((TextView)view.findViewById(R.id.name)).setText(vid.displayName);
			((TextView)view.findViewById(R.id.value)).setText(value);
			((TextView)view.findViewById(R.id.value)).setTextColor(color);
			return view;
		}		

		protected View getIndexedTypeView(ViewItemDescriptor vid, IndexedType type)
		{
			View view = getLayoutInflater().inflate(R.layout.view_indexed_type, null);
			((TextView)view.findViewById(R.id.label)).setText(vid.displayName);
			if (type != null)
			{
				((TextView)view.findViewById(R.id.value)).setText(type.getName());
				String iconID = type.getIconID();
				if (iconID != null)
				{
					setIconImage(view, type.getIcon(ViewItemActivity.this));
				}else
				{
					((ImageView)view.findViewById(R.id.icon)).setVisibility(View.GONE);
				}
			}
			return view;
		}
		
		protected View getLocationView(double latitude, double longitude)
		{
			View view = getNameValueListItem();
			((TextView)view.findViewById(R.id.name)).setText("Location");
			if (latitude != 0)
			{
				StringBuilder sb = new StringBuilder();
				sb.append(Location.convert(latitude, Location.FORMAT_DEGREES));
				sb.append(" ");
				sb.append(Location.convert(longitude, Location.FORMAT_DEGREES));
				((TextView)view.findViewById(R.id.value)).setText(sb.toString());
			}
			return view;
		}
		
		protected View getDateTimeView(ViewItemDescriptor vid, DateTime datetime)
		{
			View view = getNameValueListItem();
			((TextView)view.findViewById(R.id.name)).setText(vid.displayName);
			TextView valueView = (TextView)view.findViewById(R.id.value);
			valueView.setText(getFormattedLocalTime(datetime, null));
			return view;
		}

		protected View getDateTimeRangeView(ViewItemDescriptor vid, String startStr, String endStr)
		{
			View result = getNameValueListItem();
			TextView nameView = (TextView)result.getTag(R.id.name);
			TextView valueView = (TextView)result.getTag(R.id.value);
			nameView.setText(vid.displayName);
			if (startStr == null && endStr == null)
			{
				valueView.setText("None specified");
			}
			else
			{
				StringBuilder sb = new StringBuilder();
				if (startStr != null)
				{
					sb.append(getFormattedLocalTime(DateTime.parse(startStr), "?"));
				}
				sb.append(" to ");
				if (endStr != null)
				{
					sb.append(getFormattedLocalTime(DateTime.parse(endStr), "?"));
				}
				valueView.setText(sb);
			}
			return result;
		}
		
		protected View getIntView(ViewItemDescriptor vid, int value)
		{
			View result = getNameValueListItem();
			TextView nameView = (TextView)result.getTag(R.id.name);
			TextView valueView = (TextView)result.getTag(R.id.value);
			nameView.setText(vid.displayName);
			valueView.setText(Integer.toString(value));
			return result;
		}

		protected View getFloatView(ViewItemDescriptor vid, Float value)
		{
			View result = getNameValueListItem();
			TextView nameView = (TextView)result.getTag(R.id.name);
			TextView valueView = (TextView)result.getTag(R.id.value);
			nameView.setText(vid.displayName);
			valueView.setText(Float.toString(value));
			return result;
		}

		protected View getDoubleView(ViewItemDescriptor vid, double value)
		{
			View result = getNameValueListItem();
			TextView nameView = (TextView)result.getTag(R.id.name);
			TextView valueView = (TextView)result.getTag(R.id.value);
			nameView.setText(vid.displayName);
			valueView.setText(Double.toString(value));
			return result;
		}
		
		protected View getImageView(ViewItemDescriptor vid,final Context context ,
				ArrayList<EntryImageObject> imageToSet,	ImageManager imageManager)
		{			
			View result = getLayoutInflater().inflate(R.layout.activity_log_entry_images, null);
			LinearLayout container =(LinearLayout)result.findViewById(R.id.container);			
			for(int i=0;i<imageToSet.size();i++)
			{				
				LinearLayout horLayout = new LinearLayout(context);
				horLayout.setOrientation(LinearLayout.HORIZONTAL);
				horLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
				ImageButton image = null; TextView text = null; ProgressBar progressBar = null;
				if(imageToSet.get(i).isNeedDownload() == EntryImageObjectManager.NO_NEED_DOWNLOAD){
					text=createTextViewForImage("Status: Downloading",i,context,imageToSet.get(i));
					progressBar = createProgressBarForImage(context, imageToSet.get(i).getID());
					 image = createImageViewForImage(context, i, EntryImageObjectManager.NO_NEED_DOWNLOAD, imageToSet.get(i).getImagePath(),imageToSet.get(i).getID(),imageManager);
					 image.setOnClickListener(viewImageClickListener);
				}else if(imageToSet.get(i).isNeedDownload() == EntryImageObjectManager.IS_DOWNALODING){
					text=createTextViewForImage("Status: Downloading",i,context,imageToSet.get(i));
					progressBar = createProgressBarForImage(context, imageToSet.get(i).getID());
					image = createImageViewForImage(context,  i, EntryImageObjectManager.NEED_DOWNLOAD, imageToSet.get(i).getImagePath(),imageToSet.get(i).getID(),imageManager);		
				}else
				{
					text=createTextViewForImage("Status: Available for download",i,context,imageToSet.get(i));
					progressBar = createProgressBarForImage(context, imageToSet.get(i).getID());
					image = createImageViewForImage(context,  i, EntryImageObjectManager.NEED_DOWNLOAD, imageToSet.get(i).getImagePath(),imageToSet.get(i).getID(),imageManager);
					image.setOnClickListener(downloadImageListener);		
				}			    	
				horLayout.addView(image);
				if(text!=null){
					horLayout.addView(text);
					horLayout.addView(progressBar);
				}
				container.addView(horLayout);
				}			 
			return result;
		}


		protected View getBooleanCheckboxView(ViewItemDescriptor vid, boolean value)
		{
			View view = getLayoutInflater().inflate(R.layout.boolean_as_checkbox_list_item, null);
			((TextView)view.findViewById(R.id.name)).setText(vid.displayName);
			CheckBox checkbox = (CheckBox)view.findViewById(R.id.value);
			if (checkbox != null)
			{
				checkbox.setChecked(value);
				checkbox.setClickable(false);
			}
			return view;
		}
		
		protected View getAgencyView(ViewItemDescriptor vid, Agency agency)
		{
			View result = this._viewStub;
			if (agency != null)
			{
				String agencyName = agency.getName();
				if (agencyName != null)
				{
					result = getTextView(vid, agencyName);
				}
				else
				{
					result = getTextView(vid, agency.getID());
				}
			}
			else
			{
				result = getTextView(vid,  "NULL");
			}
			return result;
		}
		protected View getAddressView(ViewItemDescriptor vid, Address address)
		{
			View result = null;
			if (address != null)
			{
				int count = address.getCount();
				if (count == 1)
				{
					result = getTextView(vid, address.getMailingLabel(ViewItemActivity.this));
					result.findViewById(R.id.clickable).setVisibility(View.VISIBLE);
				}
			}
			if (result == null)
			{
				result = getTextView(vid, "None");
			}
			return result;
		}
		private void setIconImage(View view, Icon icon)
		{
			if (icon != null)
			{
				byte[] bytes = icon.getImage();
				if (bytes != null)
				{
					((ImageView)view.findViewById(R.id.icon)).setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length ));
				}
			}
		}
		
		private View getNameValueListItem()
		{
			View view = getLayoutInflater().inflate(R.layout.view_item_list_item, null);
			view.setTag(R.id.name, view.findViewById(R.id.name));
			view.setTag(R.id.value, view.findViewById(R.id.value));
			view.setTag(R.id.image, view.findViewById(R.id.image));
			return view;
		}
		
		private TextView createTextViewForImage(String Text,int position,Context context,EntryImageObject image)
		{
			LinearLayout.LayoutParams layoutParams =new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			layoutParams.topMargin =5;
			layoutParams.gravity=Gravity.CENTER_VERTICAL;
			TextView text = new TextView(context);
			text.setTextAppearance(context, R.style.HeaderText);
			text.setLayoutParams(layoutParams);
			text.setTag("Text"+image.getID());
			text.setTextSize(15);
			text.setTextColor(Color.GREEN);
			if(image.isNeedDownload()==EntryImageObjectManager.IS_DOWNALODING)
			{
				text.setText("Pending...");
			}else{
				text.setVisibility(View.GONE);
			}
			return text;			
		}
		
		private ProgressBar createProgressBarForImage(Context context, String ID)
		{
			LinearLayout.LayoutParams layoutParams =new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
			layoutParams.topMargin =5;
			layoutParams.leftMargin =5;
			layoutParams.gravity=Gravity.CENTER_VERTICAL;
			ProgressBar progressBar = new ProgressBar(context,null,android.R.attr.progressBarStyleHorizontal);
			progressBar.setLayoutParams(layoutParams);
			progressBar.setTag("ProgressBar"+ID);
			progressBar.setIndeterminate(false);
			progressBar.setVisibility(View.GONE);
			progressBar.setMax(100);
			return progressBar;			
		}
		private ImageButton createImageViewForImage(Context context,int position, int isNeedDownload,
				String FilePath,String ID, ImageManager imageManager)
		{			
			ImageButton image = new ImageButton(context);			
			if(isNeedDownload== EntryImageObjectManager.NO_NEED_DOWNLOAD){
				imageManager.displayImage(ID, image, R.drawable.image_icon, FilePath);
			}else
			{	
				Bitmap bg = BitmapFactory.decodeResource(getResources(), R.drawable.image_icon);
				image.setImageBitmap(bg);
			}
			LinearLayout.LayoutParams layoutParams =new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			layoutParams.topMargin =5;
		    image.setLayoutParams(layoutParams);
		    image.setScaleType(ScaleType.CENTER_INSIDE);
		    image.setBackgroundResource(R.drawable.grey_button);
		    image.setTag(ID);
		    image.setId(position);
		    EntryImageObjectManager.relationIDPosition.put(ID,position);
			return image;			
		}		
	}
	
	public final OnClickListener viewImageClickListener =new OnClickListener() {
	    @Override
	    public void onClick(View v) {
	        	if (EntryImageObjectManager.images.get(v.getId()).isNeedDownload() == EntryImageObjectManager.NO_NEED_DOWNLOAD){
					Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(new File(EntryImageObjectManager.images.get(v.getId()).getImagePath())), "image/*");
					startActivity(intent);
				}
	       
	    }
	};
	
	public final OnClickListener downloadImageListener = new OnClickListener() {		
		@Override
	    public void onClick(final View image) {				   		
			String[] buttonsText = {"Yes","No"}; //Buttons are organized left to right.
			CustomAlertDialog customDialog = new CustomAlertDialog(image.getContext(), buttonsText,"Information","Download this image?",android.R.drawable.ic_menu_info_details,
					new CustomAlertDialog.DialogButtonClickListener() {
	   					@Override
	   					public void onDialogButtonClick(View v) {
	   						if("Yes".equals(v.getTag()))
	   						{
	   							image.setClickable(false);
	   							View view = (View)image.getParent();
	   							((TextView)view.findViewWithTag("Text"+image.getTag())).setVisibility(View.VISIBLE);
	   							((TextView)view.findViewWithTag("Text"+image.getTag())).setText("Pending...");
	   							EntryImageObjectManager.verifyNetworkAndDownloadImage(image.getContext(), EntryImageObjectManager.images.get( image.getId()).getID(),
	   									EntryImageObjectManager.images.get( image.getId()).getImagePath(),
	   									EntryImageObjectManager.images.get( image.getId()).getFileSize(),
	   									EntryImageObjectManager.images.get( image.getId()).getJournalEntryID());
	   						}else if("No".equals(v.getTag()))
	   						{
	   							image.setClickable(true);
	   						}						
	   					}
	   				});
	   		 customDialog.show();
		}
	};	



}
