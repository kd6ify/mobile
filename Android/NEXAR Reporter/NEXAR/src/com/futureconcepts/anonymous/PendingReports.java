package com.futureconcepts.anonymous;

import java.util.ArrayList;
import java.util.HashMap;
import com.futureconcepts.customclass.SingletonInformation;
import com.futureconcepts.database.DatabaseHelper;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class PendingReports extends ListActivity implements OnClickListener, OnItemClickListener {
	ListView pendingList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_pending_reports);
		pendingList=(ListView)findViewById(android.R.id.list);
		pendingList.setOnItemClickListener(this);
		getData();
		SingletonInformation.getInstance().activityPendingVisible=true;
		//Get the current context to display the AlertDialog for not send it reports.
		SingletonInformation.getInstance().currentCon=this;
    	}

	
	public void getData(){
		ArrayList<DataList> list =new 	ArrayList<DataList>();
		HashMap<String,Integer> IdStored = new HashMap<String,Integer>();
		
		DatabaseHelper dbHelper = new DatabaseHelper(this);
		SQLiteDatabase newDB = dbHelper.getWritableDatabase(); 
		Cursor CSF = newDB.rawQuery("Select * from AnonymousInfo ORDER BY Date ASC",null);
        Cursor CAIM = newDB.rawQuery("Select * from AnonymousInfoMedia",null);
        Cursor CType = newDB.rawQuery("Select * from AnonymousCategory",null);
    		int count=1;
    		IdStored.clear();    	
    		if(CSF.getCount()!=0){
    		CSF.moveToFirst();			
    			do{ 
    				String type=getType(CSF.getString(CSF.getColumnIndex("Type")),CType);
    				list.add(new DataList("Report "+count+": "+type,CSF.getString(CSF.getColumnIndex("ID"))));
    				IdStored.put(CSF.getString(CSF.getColumnIndex("ID")), count);
    				count+=1;
    			}while(CSF.moveToNext());	

    		}
    	
        if(CAIM.getCount()!=0){
    			CAIM.moveToFirst();			
    			do{ 
    				
    					if(!(IdStored.containsKey(CAIM.getString(CAIM.getColumnIndex("NEXARInfo"))))){
    						String type=getType(CAIM.getString(CAIM.getColumnIndex("Type")),CType);
    						IdStored.put(CAIM.getString(CAIM.getColumnIndex("NEXARInfo")), count);
    						list.add(new DataList("Report "+count+": "+type,CAIM.getString(CAIM.getColumnIndex("NEXARInfo"))));
    						count+=1;
    					}
    			}while(CAIM.moveToNext());	
    		}
        
        if(CSF.getCount()==0 &&CAIM.getCount()==0){ 
    			list.add(new DataList("No Pending Reports.",""));	
    		     }
        CAIM.close();
        CSF.close();
        CType.close();
        newDB.close();
    	 ArrayAdapterAnnouncements adapter= new ArrayAdapterAnnouncements(this,R.layout.pending_list_layout,list);
    	 pendingList.setAdapter(adapter);
		
	}
	

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long arg3) {
		String IDReport="";
		String category="";
		
		IDReport=((TextView) v.findViewById(R.id.detailsText)).getText().toString();
		category=((TextView) v.findViewById(R.id.notice)).getText().toString();
		String[] arrayCategory = category.split(":");
		
		if(arrayCategory.length>1){
			category=arrayCategory[1];
		}else{
			category="";
		}
		
		if(IDReport.equals("")){
			alertDialog("","There are no pending reports.");
		}
		else{
			Intent PendingData = new Intent(this,ViewPendingData.class);
			PendingData.putExtra("IdReport",IDReport);
			PendingData.putExtra("ReportNumber", position);
			PendingData.putExtra("Category", category);
			startActivityForResult(PendingData,1);
		}
		
	}

	@Override
	public void onClick(DialogInterface arg0, int arg1) {
		// TODO Auto-generated method stub
		
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

	  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		  Log.d("1a","1a");
		  if (requestCode == 1) {

		     if(resultCode == RESULT_OK){      
		       String result=data.getStringExtra("action");
		        if(result.equals("deletion")){
					alertDialog("","Report deleted successfully.");
		        }
		    	getData();

		        Log.d("2a","2a");
		        
		     }
		     if (resultCode == RESULT_CANCELED) {    

		    	 Log.d("3a","3a");
		     }
		  }
		}

public String getType(String IdType, Cursor CTypes){
	String type="";
	//CType=Report type
    	CTypes.moveToFirst();			
			do{ 
				if(IdType.equals(CTypes.getString(CTypes.getColumnIndex("ID")))){
				type=CTypes.getString(CTypes.getColumnIndex("Type"));
				break;
				}
			   }while(CTypes.moveToNext());	
 return type;
}

}
