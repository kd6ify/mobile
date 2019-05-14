package com.futureconcepts.drake.client;

import android.os.Parcel;
import android.os.Parcelable;

public class Invitation implements Parcelable
{
    private String mId;
    private String mGroupAddress;
    private String mSender;
    private String mReason;
    private String mPassword;

    public Invitation(String id, String groupAddress, String sender, String reason, String password)
    {
        mId = id;
        mGroupAddress = groupAddress;
        mSender = sender;
        mReason = reason;
        mPassword = password;
    }

    public Invitation(Parcel source)
    {
        mId = source.readString();
        mGroupAddress = source.readString();
        mSender = source.readString();
        mReason = source.readString();
        mPassword = source.readString();
    }

    public String getInviteID()
    {
        return mId;
    }

    public String getGroupAddress()
    {
        return mGroupAddress;
    }

    public String getSender()
    {
        return mSender;
    }

    public String getReason()
    {
        return mReason;
    }

    public String getPassword()
    {
    	return mPassword;
    }
    
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(mId);
        dest.writeString(mGroupAddress);
        dest.writeString(mSender);
        dest.writeString(mReason);
        dest.writeString(mPassword);
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Invitation> CREATOR = new Parcelable.Creator<Invitation>() {
        public Invitation createFromParcel(Parcel source) {
            return new Invitation(source);
        }

        public Invitation[] newArray(int size) {
            return new Invitation[size];
        }
    };
}
