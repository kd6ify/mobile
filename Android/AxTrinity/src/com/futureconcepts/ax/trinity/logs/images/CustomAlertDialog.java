package com.futureconcepts.ax.trinity.logs.images;
import com.futureconcepts.ax.trinity.R;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/*
 * This class create a custom Dialog with 3 buttons
 *  Buttons are organized left to right.
*/
public class CustomAlertDialog extends Dialog {
	
	public interface DialogButtonClickListener {
        public  void onDialogButtonClick(View v);        
    }

    private DialogButtonClickListener dialogClickListener;
    private String[] buttons;
    private Context context;
    public static int DialogInformationType = 0;
    public static int DialogWanrningType = 0;
    private String message;
    private String title;
    private int dialogIconID;

    public CustomAlertDialog(Context context,String [] ButtonsText,String Title, String Message, int iconID, DialogButtonClickListener readyListener) {
        super(context);
        this.dialogClickListener = readyListener;
        buttons = ButtonsText;
        this.context = context;
        message = Message;
        title = Title;
        dialogIconID = iconID;
    }

	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.customalertdialog);
	        if(title!=null){
	        	setTitle(title);
	        }
	        TextView dialogText = (TextView)findViewById(R.id.dialogText);
	        dialogText.setText(message);
	        findViewById(R.id.dialogImage).setBackgroundResource(dialogIconID);
	        LinearLayout container = (LinearLayout)findViewById(R.id.diloagButtonsContainer);
	    	LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
	    	layoutParams.weight = 1;
	    	 //Buttons are organized left to right.
	    	for(int x=0;x<buttons.length;x++)
	        {
	        	Button button = new Button(context);
	        	button.setOnClickListener(new ClickListener() {
	             
				@Override
				public void onClick(View v) {
					dialogClickListener.onDialogButtonClick(v);
		            CustomAlertDialog.this.dismiss();
				}});
	        	button.setText(buttons[x]);
	        	button.setLayoutParams(layoutParams);
	        	//Set the name as tag to identify button.
	        	button.setTag(buttons[x]);
	        	container.addView(button);
	        }
	    }

	    private class ClickListener implements android.view.View.OnClickListener {
	        @Override
	        public void onClick(View v) {
	        	
	        }
	    }
}