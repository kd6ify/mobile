package com.futureconcepts.ax.trinity.osm;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.futureconcepts.ax.trinity.R;

public class SelectFileActivity extends ListActivity{
	

    private static final String PARENT_DIR = "Parent Dir";
    private final String TAG = getClass().getName();
    private String[] fileList;
    private File currentPath;
    FileAdapter adapter;
    private boolean selectDirectoryOption;
    private String fileEndsWith;    
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_file_activity);		
		fileEndsWith = ".map";
		if(getIntent().getExtras()!=null){
			loadFileList(new File(getIntent().getExtras().getString("path"))); 
		}else{
			loadFileList(new File(Environment.getExternalStorageDirectory(), "")); 
		}
        adapter = new FileAdapter();
        getListView().setAdapter(adapter);
	}
	
//	@Override
//	public void onBackPressed() {
//		// TODO Auto-generated method stub
//		//super.onBackPressed();
//		
//		 loadFileList(getChosenFile(PARENT_DIR));
//         adapter.notifyDataSetChanged();
//	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		   String fileChosen = fileList[position];
           File chosenFile = getChosenFile(fileChosen);
           if (chosenFile.isDirectory()) {
               loadFileList(chosenFile);
               adapter.notifyDataSetChanged();
           }else{
        	//   fireFileSelectedEvent(chosenFile);
        	   Intent i = new Intent();
        	   i.putExtra("mapFile" , chosenFile.getAbsolutePath());
        	   setResult(RESULT_OK,i);
        	   finish();
        	   //Toast.makeText(getApplicationContext(),chosenFile.getAbsolutePath(),Toast.LENGTH_SHORT).show();
           }
	}
	private File getChosenFile(String fileChosen) {
        if (fileChosen.equals(PARENT_DIR)) return currentPath.getParentFile();
        else return new File(currentPath, fileChosen);
    }
	
	private void loadFileList(File path) {
        this.currentPath = path;
        List<String> r = new ArrayList<String>();
        if (path.exists()) {
            if (path.getParentFile() != null) r.add(PARENT_DIR);
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    if (!sel.canRead()) return false;
                    if (selectDirectoryOption) return sel.isDirectory();
                    else {
                        boolean endsWith = fileEndsWith != null ? filename.toLowerCase().endsWith(fileEndsWith) : true;
                        if(filename.contains("TEMP"))
                        {
                        	return false;
                        }
                        return endsWith || sel.isDirectory();
                    }
                }
            };
            String[] fileList1 = path.list(filter);
            for (String file : fileList1) {
                r.add(file);
            }
        }
        fileList = (String[]) r.toArray(new String[]{});
    }

	
	
	
	private class FileAdapter extends BaseAdapter
	{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return fileList.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return fileList[position];
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if(convertView==null){
				convertView = getLayoutInflater().inflate(R.layout.select_file_activity_list_item, null);
			}			
			((TextView)convertView.findViewById(R.id.fileText)).setText(fileList[position]);
		//	Log.e("asd", );
			
			File file = getChosenFile(fileList[position]);
			if(file.isFile())
			{
				((ImageView)convertView.findViewById(R.id.type_icon)).setImageResource(R.drawable.file);	
			}else if(PARENT_DIR.equals(fileList[position]))
			{
				((ImageView)convertView.findViewById(R.id.type_icon)).setImageResource((R.drawable.ic_menu_back));	
			}else
			{
				((ImageView)convertView.findViewById(R.id.type_icon)).setImageResource(R.drawable.ic_menu_archive);	
			}
			file = null;
			 return convertView;
		}
		
	}
	
	
	

}
