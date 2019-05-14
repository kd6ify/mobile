/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.futureconcepts.drake.ui.widget;

import com.futureconcepts.drake.client.Imps;
import com.futureconcepts.drake.ui.R;
import com.futureconcepts.drake.ui.app.ContactListActivity;
import com.futureconcepts.drake.ui.app.JoinChatGroupActivity;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class ContactListFilterView extends LinearLayout
{
    private ListView mFilterList;
    private Filter mFilter;
    private ContactAdapter mContactAdapter;

    private Uri mUri;
    private Context mContext;
    
    public UserPresenceView mPresenceView;
    private ContactListActivity mActivity;
    

	public ContactListFilterView(Context context, AttributeSet attrs)
	{
        super(context, attrs);
        
        mContext = context;
    }

    @Override
    protected void onFinishInflate()
    {
    	mFilterList = (ListView) findViewById(R.id.filteredList);
    	mFilterList.setTextFilterEnabled(true);

    	mFilterList.setOnItemClickListener(new OnItemClickListener()
    	{
            public void onItemClick(AdapterView parent, View view, int position, long id)
            {
                Cursor c = (Cursor) mFilterList.getItemAtPosition(position);
                int type = c.getInt(c.getColumnIndex(Imps.Contacts.TYPE));
                if (type == Imps.Contacts.TYPE_GROUP)
                {
                	Intent joinIntent = new Intent(mActivity, JoinChatGroupActivity.class);
                	joinIntent.setData(ContentUris.withAppendedId(Imps.Contacts.CONTENT_URI, c.getLong(c.getColumnIndex(Imps.Contacts._ID))));
                	mActivity.startActivity(joinIntent);
                }
                else
                {
                	mActivity.mContactListView.startChat(c);
                }
            }
        });
    	 mPresenceView = (UserPresenceView)findViewById(R.id.userPresence);
    }

    public ListView getListView() {
        return mFilterList;
    }

    public Cursor getContactAtPosition(int position) {
        return (Cursor) mContactAdapter.getItem(position);
    }

    public void doFilter(Uri uri, String filterString)
    {
        if (!uri.equals(mUri)) {
            mUri = uri;
            Cursor contactCursor = runQuery(filterString);
            if (mContactAdapter == null) {
                mContactAdapter = new ContactAdapter(mContext, contactCursor);
                mFilter = mContactAdapter.getFilter();
                mFilterList.setAdapter(mContactAdapter);
            } else {
                mContactAdapter.changeCursor(contactCursor);
            }
        } else {
            mFilter.filter(filterString);
        }
    }

    public void doFilter(String filterString) {
         mFilter.filter(filterString);
        
    }
    
    Cursor runQuery(CharSequence constraint)
    {
    	Cursor result = null;
        StringBuilder buf = new StringBuilder();

        // exclude chatting contact
     //   buf.append(Imps.Chats.LAST_MESSAGE_DATE);
       // buf.append(" IS NULL");
        //   buf.append(" AND ");
        
        if (constraint != null) {

            buf.append(Imps.Contacts.NICKNAME);
            buf.append(" LIKE ");
            DatabaseUtils.appendValueToSql(buf, "%" + constraint + "%");
        }

        result = mContext.getContentResolver().query(mUri, ContactView.CONTACT_PROJECTION,
                buf == null ? null : buf.toString(), null, Imps.Contacts.DEFAULT_SORT_ORDER);
//        for (int i = 0; i < result.getCount(); i++)
//        {
//        	result.moveToPosition(i);
//        	Log.d("ContactListFilterView", "username=" + result.getString(ContactView.COLUMN_CONTACT_USERNAME));
//        }
        return result;
    }

    private class ContactAdapter extends ResourceCursorAdapter {
        private String mSearchString;

        public ContactAdapter(Context context, Cursor cursor) {
            super(context, R.layout.contact_view, cursor);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ContactView v = (ContactView) view;
            v.setPadding(0, 0, 0, 0);
            v.bind(cursor, mSearchString, false);
        }

        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            if (constraint != null) {
                mSearchString = constraint.toString();
            }
            return ContactListFilterView.this.runQuery(constraint);
        }
    }
    

    public void setActivity(ContactListActivity mActivity) {
		this.mActivity = mActivity;
	}

}
