package com.futureconcepts.drake.client;

import android.os.Parcel;
import android.os.Parcelable;

public class FileTransferParcel implements Parcelable
{
	private String fileName;
	private String filePath;
	private long fileSize;
	private String peer;
	private String status;
	private String streamID;
	private long amountWritten = -1;

	public FileTransferParcel(String streamID)
    {
    	this.streamID = streamID;
	}

    public FileTransferParcel(Parcel source)
    {
    	this.fileName = source.readString();
    	this.filePath = source.readString();
    	this.fileSize = source.readLong();
    	this.peer = source.readString();
    	this.status = source.readString();
    	this.streamID = source.readString();
    	this.amountWritten = source.readLong();
    }

    public void setFileName(String value)
    {
    	this.fileName = value;
    }
    
    public void setFilePath(String value)
    {
    	this.filePath = value;
    }
    
	public void setFileInfo(String fileName, long fileSize)
	{
		this.fileName = fileName;
		this.fileSize = fileSize;
	}

	public void setFileInfo(String path, String fileName, long fileSize)
	{
		this.filePath = path;
		this.fileName = fileName;
		this.fileSize = fileSize;
	}

	/**
	 * Returns the size of the file being transfered.
	 *
	 * @return Returns the size of the file being transfered.
	 */
	public long getFileSize()
	{
		return fileSize;
	}

	/**
	 * Returns the name of the file being transfered.
	 *
	 * @return Returns the name of the file being transfered.
	 */
	public String getFileName()
	{
		return fileName;
	}

	/**
	 * Returns the local path of the file.
	 *
	 * @return Returns the local path of the file.
	 */
	public String getFilePath()
	{
		return filePath;
	}

	/**
	 * Returns the JID of the peer for this file transfer.
	 *
	 * @return Returns the JID of the peer for this file transfer.
	 */
	public String getPeer()
	{
		return peer;
	}

	/**
	 * Returns the progress of the file transfer as a number between 0 and 1.
	 *
	 * @return Returns the progress of the file transfer as a number between 0
	 *         and 1.
	 */
	public double getProgress()
	{
        if (amountWritten <= 0 || fileSize <= 0) {
            return 0;
        }
        return (double) amountWritten / (double) fileSize;
	}

	/**
	 * Returns the current status of the file transfer.
	 *
	 * @return Returns the current status of the file transfer.
	 */
	public String getStatus()
	{
		return status;
	}

    public String getStreamID()
    {
        return streamID;
    }

	public void setStatus(String status, long amountWritten)
	{
	    this.status = status;
	    this.amountWritten = amountWritten;
    }

    /**
     * Return the length of bytes written out to the stream.
     * @return the amount in bytes written out.
     */
    public long getAmountWritten()
    {
        return amountWritten;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
    	dest.writeString(fileName);
    	dest.writeString(filePath);
    	dest.writeLong(fileSize);
    	dest.writeString(peer);
    	dest.writeString(status);
    	dest.writeString(streamID);
    	dest.writeLong(amountWritten);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Parcelable.Creator<FileTransferParcel> CREATOR = new Parcelable.Creator<FileTransferParcel>() {
        public FileTransferParcel createFromParcel(Parcel source) {
            return new FileTransferParcel(source);
        }

        public FileTransferParcel[] newArray(int size) {
            return new FileTransferParcel[size];
        }
    };
}
