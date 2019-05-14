package com.futureconcepts.ax.trinity.widget;

import com.futureconcepts.ax.trinity.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SyncProgressView extends LinearLayout
{	
	private static final String LOG_TAG = SyncProgressView.class.getSimpleName();
	
    private TextView _actionView;
    private TextView _datasetView;
    private TextView _tableView;
    private ProgressBar _progressBar;

    public SyncProgressView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.sync_progress_view, this);
    }

    @Override
    protected void onFinishInflate()
    {
    	_actionView = (TextView)findViewById(R.id.action);
        _datasetView = (TextView)findViewById(R.id.dataset);
        _tableView = (TextView)findViewById(R.id.table);
        _progressBar = (ProgressBar)findViewById(R.id.progress);
    }
    
    public void hide()
    {
    	setVisibility(View.GONE);
    }
    
    public void show()
    {
    	setVisibility(View.VISIBLE);
    	_actionView.setText("");
		_progressBar.setVisibility(View.VISIBLE);
		_datasetView.setText("");
		_tableView.setText("");
    }
    
    public void setAction(String value)
    {
    	_actionView.setText(value);
    }
    
    public void setDataset(String value)
    {
    	_datasetView.setText(value);
    }
    
    public void setTable(String value)
    {
    	_tableView.setText(value);
    }
    
    public void setDownloading(boolean value)
    {
    	_progressBar.setIndeterminate(value);
    }
    
    public void setProgress(int position, int count)
    {
		_progressBar.setMax(count);
		_progressBar.setProgress(position);
    }
}
