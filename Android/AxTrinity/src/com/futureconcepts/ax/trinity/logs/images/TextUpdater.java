package com.futureconcepts.ax.trinity.logs.images;

import java.util.Stack;

import com.futureconcepts.ax.model.data.JournalEntry;
import com.futureconcepts.ax.model.data.JournalEntryMedia;
import com.futureconcepts.ax.trinity.logs.images.ImageManager.ImageManagerGetImageListener;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

public class TextUpdater {

	private TextQueue textQueue = new TextQueue();
	private Thread textLoaderThread = new Thread(new TextQueueManager());
	private TextUpdaterGetTextListener listener;
	private boolean textLoaderIsRunning =false;
	
	public TextUpdater(TextUpdaterGetTextListener listener) {
		textLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);
		this.listener = listener;
	}
	
	public interface  TextUpdaterGetTextListener 
	{
		 public String getText(String ID);
	}
	
	public void removeTextUpdaterGetTextListener(TextUpdaterGetTextListener Listener)
	{
		if(listener == Listener)
			listener=null;
	}

	public void displayText(String ID, TextView view,String defaultText) {
		queueText(ID, view,defaultText);
	}
	
	public void close()
    {
		textLoaderIsRunning  = false; 	
    	synchronized (textQueue.textRefs) {
    		textQueue.textRefs.notifyAll();//Wake Up the thread so we can finish it.
		}
    }

	private void queueText(String ID, TextView  textView, String defaultText) {
		// This textView might have been used for other texts, so we clear
		// the queue of old tasks before starting.
		textQueue.Clean(textView);
		TextRef p = new TextRef(ID, textView, defaultText);

		synchronized (textQueue.textRefs) {
			textQueue.textRefs.push(p);
			textQueue.textRefs.notifyAll();
		}

		// Start thread if it's not started yet
		if (textLoaderThread.getState() == Thread.State.NEW) {
			textLoaderIsRunning = true;
			textLoaderThread.start();
		}
	}

	/** Classes **/

	private class TextRef {
		public String ID;
		public TextView textView;
		public String defaultText;

		public TextRef(String u, TextView i, String a) {
			ID = u;
			textView = i;
			defaultText = a;
		}
	}

	// stores list of texts to download
	private class TextQueue {
		private Stack<TextRef> textRefs = new Stack<TextRef>();
		// removes all instances of this textView
		public void Clean(TextView textView) {

			for (int i = 0; i < textRefs.size();) {
				//if (textRefs.size()>i)			
				if(textRefs.get(i).textView == textView)
						textRefs.remove(i);
				else
					++i;
			}
		}
	}

	private class TextQueueManager implements Runnable {
		@Override
		public void run() {
			try {
				while (textLoaderIsRunning) {
					// Thread waits until there are texts in the
					// queue to be retrieved
					if (textQueue.textRefs.size() == 0) {
						synchronized (textQueue.textRefs) {
							textQueue.textRefs.wait();
						}
					}
					// When we have text to be loaded
					if (textQueue.textRefs.size() != 0) {
						TextRef textToLoad;
						
						synchronized (textQueue.textRefs) {
							textToLoad = textQueue.textRefs.pop();
						}						
						String text = listener.getText(textToLoad.ID);//;getText(textToLoad.textView.getContext(),textToLoad.ID);												
						Object tag = textToLoad.textView.getTag();
						// Make sure we have the right view - thread safety defender
						if (tag != null	&& ((String) tag).equals(textToLoad.ID)) {
							TextDisplayer bmpDisplayer = new TextDisplayer(text, textToLoad.textView,textToLoad.defaultText);
							Activity a = (Activity) textToLoad.textView.getContext();
							a.runOnUiThread(bmpDisplayer);
						}
					}
					if (Thread.interrupted())
						break;
				}
			} catch (InterruptedException e) {
			}
		}
	}

	// Used to display text in the UI thread
	private class TextDisplayer implements Runnable {
		String text;
		TextView textView;
		String defaultText;

		public TextDisplayer(String b, TextView i, String defaultTextParam) {
			text = b;
			textView = i;
			defaultText = defaultTextParam;
		}

		public void run() {
			if (text != null)
				textView.setText(text);
			else
				textView.setText(defaultText);
		}
	}
}
