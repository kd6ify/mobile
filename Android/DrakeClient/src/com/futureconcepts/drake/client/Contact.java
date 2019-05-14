package com.futureconcepts.drake.client;

import android.os.Parcel;
import android.os.Parcelable;

public class Contact extends ImEntity implements Parcelable
{
    private String mAddress;
    private String mName;
    private Presence mPresence;
    private int mType;

    public Contact(String address, String name)
    {
        mAddress = address;
        mName = name;
        mPresence = new Presence();
        mType = Type.NORMAL;
    }

    public Contact(String address, String name, int type)
    {
        mAddress = address;
        mName = name;
        mPresence = new Presence();
        mType = type;
    }

    public Contact(Parcel source)
    {
        mAddress = source.readString();
        mName = source.readString();
        mPresence = new Presence(source);
        mType = source.readInt();
    }

    @Override
    public String getAddress()
    {
        return mAddress;
    }

    public String getName()
    {
        return mName;
    }

    public Presence getPresence()
    {
        return mPresence;
    }

    public int getType()
    {
    	return mType;
    }
    
    public boolean equals(Object other)
    {
        return other instanceof Contact && mAddress.equals(((Contact)other).mAddress);
    }

    public int hashCode()
    {
        return mAddress.hashCode();
    }

    /**
     * Set the presence of the Contact. Note that this method is public
     * but not provide to the user.
     * @param presence the new presence
     */
    public void setPresence(Presence presence)
    {
        mPresence = presence;
    }

    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(mAddress);
        dest.writeString(mName);
        mPresence.writeToParcel(dest, 0);
        dest.writeInt(mType);
    }

    public int describeContents()
    {
        return 0;
    }

    public final static Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
        public Contact createFromParcel(Parcel source)
        {
            return new Contact(source);
        }

        public Contact[] newArray(int size)
        {
            return new Contact[size];
        }
    };

    public interface Type
    {
	    int NORMAL = 0;
	    int TEMPORARY = 1;
	    int GROUP = 2;
	    int BLOCKED = 3;
	    int HIDDEN = 4;
	    int PINNED = 5;
    }
}
