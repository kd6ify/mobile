package com.futureconcepts.ax.sync;

import android.os.Parcel;
import android.os.Parcelable;

public class UpdateItem implements Parcelable
{
	private long mUpdateId;
	private String mIncidentId;
	private String mDataContractType;
	private String mAction;
	private String mID;

	public UpdateItem()
	{
	}
	
	private UpdateItem(Parcel parcel)
	{
		mUpdateId = parcel.readLong();
		mAction = parcel.readString();
		mDataContractType = parcel.readString();
		mID = parcel.readString();
	}
	
	public long getUpdateId()
	{
		return mUpdateId;
	}
	
	public void setUpdateId(long value)
	{
		mUpdateId = value;
	}

	public String getIncidentId()
	{
		return mIncidentId;
	}
	
	public void setIncidentId(String value)
	{
		mIncidentId = value;
	}
	
	public String getAction()
	{
		return mAction;
	}
	
	public void setAction(String value)
	{
		mAction = value;
	}
	
	public String getDataContractType()
	{
		return mDataContractType;
	}
	
	public void setDataContractType(String value)
	{
		mDataContractType = value;
	}
	
	public String getID()
	{
		return mID;
	}
	
	public void setID(String value)
	{
		mID = value;
	}
	
	
	public int describeContents()
    {
	    // TODO Auto-generated method stub
	    return 0;
    }

	public void writeToParcel(Parcel dest, int flags)
    {
		dest.writeLong(mUpdateId);
		dest.writeString(mAction);
		dest.writeString(mDataContractType);
		dest.writeString(mID);
    }
	
	public static final Parcelable.Creator<UpdateItem> CREATOR = new Parcelable.Creator<UpdateItem>() {
		public UpdateItem createFromParcel(Parcel in) {
			return new UpdateItem(in);
		}
		public UpdateItem[] newArray(int size)
        {
			return new UpdateItem[size];
        }
	};
}
