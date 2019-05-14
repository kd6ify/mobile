package com.futureconcepts.drake.client;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents an instant message send between users.
 */
public class Message implements Parcelable
{
    private String mId;
    private String mFrom;
    private String mTo;
    private String mBody;
    private Date mDate;
    private int mType;

    /**
     *
     * @param msg
     * @throws NullPointerException if msg is null.
     */
    public Message(String msg)
    {
        if (msg == null)
        {
            throw new NullPointerException("null msg");
        }
        mBody = msg;
        mType = Type.CHAT;
    }

    public Message(Parcel source) {
        mId = source.readString();
        mFrom = source.readString();
        mTo = source.readString();
        mBody = source.readString();
        long time = source.readLong();
        if(time != -1)
        {
            mDate = new Date(time);
        }
        mType = source.readInt();
    }

    /**
     * Gets an identifier of this message. May be <code>null</code> if the
     * underlying protocol doesn't support it.
     *
     * @return the identifier of this message.
     */
    public String getID()
    {
        return mId;
    }

    /**
     * Gets the body of this message.
     *
     * @return the body of this message.
     */
    public String getBody() {
        return mBody;
    }

    /**
     * Gets the address where the message is sent from.
     *
     * @return the address where the message is sent from.
     */
    public String getFrom()
    {
        return mFrom;
    }

    /**
     * Gets the address where the message is sent to.
     *
     * @return the address where the message is sent to.
     */
    public String getTo()
    {
        return mTo;
    }

    /**
     * Gets the date time associated with this message. If it's a message sent
     * from this client, the date time is when the message is sent. If it's a
     * message received from other users, the date time is either when the
     * message was received or sent, depending on the underlying protocol.
     *
     * @return the date time.
     */
    public Date getDateTime() {
        if (mDate == null) {
            return null;
        }
        return new Date(mDate.getTime());
    }

    public void setID(String id) {
        mId = id;
    }

    public void setBody(String body) {
        mBody = body;
    }

    public void setFrom(String from)
    {
        mFrom = from;
    }

    public void setTo(String to)
    {
        mTo = to;
    }

    public void setDateTime(Date dateTime)
    {
        long time = dateTime.getTime();
        if (mDate == null)
        {
            mDate = new Date(time);
        }
        else
        {
            mDate.setTime(time);
        }
    }

    public void setType(int value)
    {
    	mType = value;
    }
    
    public int getType()
    {
    	return mType;
    }
    
    public String toString()
    {
        return "From: " + mFrom + " To: " + mTo + " " + mBody;
    }

    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(mId);
        dest.writeString(mFrom);
        dest.writeString(mTo);
        dest.writeString(mBody);
        dest.writeLong(mDate == null ? -1 : mDate.getTime());
        dest.writeInt(mType);
    }

    public int describeContents()
    {
        return 0;
    }

    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
    
    public interface Type
    {
	    int CHAT = 0;
	    int GROUPCHAT = 1;
    }
}
