package com.futureconcepts.anonymous;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class ArrayAdapterAnnouncements  extends ArrayAdapter<DataList> {
		
	private ArrayList<DataList> announcements;
    private int viewId;
		public ArrayAdapterAnnouncements(Context context, int textViewResourceId, ArrayList<DataList> announcements) {
		super(context, textViewResourceId, announcements);
		this.announcements = announcements;
	    viewId=textViewResourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
		LayoutInflater vi =(LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		v = vi.inflate(viewId, null);

		}

		DataList announcement = announcements.get(position);
		if (announcement != null) {
		TextView notice = (TextView) v.findViewById(R.id.notice);
		TextView detailsText = (TextView) v.findViewById(R.id.detailsText);

		if (notice != null) {
		notice.setText(announcement.ID);
		}

		if(detailsText != null) {
		detailsText.setText(announcement.Name );
		}
		}
		return v;
		}
}
	

