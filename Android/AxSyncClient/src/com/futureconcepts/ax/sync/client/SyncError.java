package com.futureconcepts.ax.sync.client;

import android.os.Parcel;
import android.os.Parcelable;

public class SyncError implements Parcelable
{
	private String _msg;
	
	public SyncError(String msg)
	{
		_msg = msg;
	}

	public SyncError(Parcel source)
    {
    }
		
	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
	}

	public final static Parcelable.Creator<SyncError> CREATOR = new Parcelable.Creator<SyncError>() {
        public SyncError createFromParcel(Parcel source)
        {
            return new SyncError(source);
        }

        public SyncError[] newArray(int size)
        {
            return new SyncError[size];
        }
    };
}
