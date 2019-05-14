package com.futureconcepts.anonymous;

import com.futureconcepts.customclass.SingletonInformation;
import com.google.ads.AdRequest;
import com.google.ads.AdView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity {
	//AdView adView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		// adView = (AdView)this.findViewById(R.id.ad);
		//  adView.loadAd(new AdRequest());
		 GridView gridview = (GridView) findViewById(R.id.categoryGrid);		
		gridview.setAdapter(new ButtonAdapter(this));
		gridview.setOnItemClickListener((OnItemClickListener) new MyOnClickListener());
		 SingletonInformation.getInstance().activityCategoryVisible=true;
		//Get the current context to display the AlertDialog for not send it reports.
			SingletonInformation.getInstance().currentCon=this;
		}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_choose_school, menu);
		return true;
	}
	
	 @Override
	  public void onDestroy() {
//	    if (adView != null) {
//	      adView.destroy();
//	    }
	    super.onDestroy();
	  }

	public boolean onOptionsItemSelected(MenuItem item)
    {
	  Intent tables = new Intent (this,Create.class);
 
        switch (item.getItemId())
        {
        case R.id.menu_1:	           
            tables.putExtra("Table","AnonymousCategory");	        	
            startActivity(tables);
            break;
         case R.id.menu_2:	           
             tables.putExtra("Table","AnonymousInfo");	        	
             startActivity(tables);
             break;
        case R.id.schools:	           
           tables.putExtra("Table","Schools");	        	
           startActivity(tables);
           break;
       /* case R.id.schooltype:	           
            tables.putExtra("Table","SchoolTypes");	        	
            startActivity(tables);
            break;
        case R.id.states:	           
            tables.putExtra("Table","States");	        	
            startActivity(tables);
            break;*/
        case R.id.media:	           
            tables.putExtra("Table","Media");	        	
            startActivity(tables);
            break;
        case R.id.mediaType:	           
            tables.putExtra("Table","MediaType");	        	
            startActivity(tables);
            break;
            
        case R.id.anonymousInfoMedia:	           
            tables.putExtra("Table","AnonymousInfoMedia");	        	
            startActivity(tables);
            break;
            
        case R.id.schoolFeedMenu:	           
            tables.putExtra("Table","SchoolFeed");	        	
            startActivity(tables);
            break;

//        case R.id.tableStatusMenu:	           
//            tables.putExtra("Table","TableStatus");	        	
//            startActivity(tables);
//            break;
		
        }
        return false;
   }
	

	
	public void exit(View view)
	{
		finish();
	}
	public void setOptions(View view) {
		Intent intent = new Intent(this, ChooseSchool.class);
		startActivity(intent);

	}
	
}
