package com.futureconcepts.drake.client;

import android.os.Parcel;
import android.os.Parcelable;

public class FileTransferRequestParcel implements Parcelable
{
	/**
	 * A recieve request is constructed from the Stream Initiation request
	 * received from the initator.
	 * 
	 * @param manager
	 *            The manager handling this file transfer
	 * 
	 * @param si
	 *            The Stream initiaton recieved from the initiator.
	 */

	private String _streamID;
	private String _fileName;
	private long _fileSize;
	private String _description;
	private String _mimeType;
	private String _requestor;
	
	public FileTransferRequestParcel(String streamID, String requestor)
	{
		_streamID = streamID;
		_requestor = requestor;
	}

	public FileTransferRequestParcel(Parcel source)
    {
    	_streamID = source.readString();
    	_fileName = source.readString();
    	_fileSize = source.readLong();
    	_description = source.readString();
    	_mimeType = source.readString();
    	_requestor = source.readString();
    }

	public void setFileInfo(String fileName, long fileSize, String mimeType)
	{
		_fileName = fileName;
		_fileSize = fileSize;
		_mimeType = mimeType;
	}

	public void setDescription(String value)
	{
		_description = value;
	}
	/**
	 * Returns the name of the file.
	 * 
	 * @return Returns the name of the file.
	 */
	public String getFileName()
	{
		return _fileName;
	}

	/**
	 * Returns the size in bytes of the file.
	 * 
	 * @return Returns the size in bytes of the file.
	 */
	public long getFileSize()
	{
		return _fileSize;
	}

	/**
	 * Returns the description of the file provided by the requestor.
	 * 
	 * @return Returns the description of the file provided by the requestor.
	 */
	public String getDescription()
	{
		return _description;
	}

	/**
	 * Returns the mime-type of the file.
	 * 
	 * @return Returns the mime-type of the file.
	 */
	public String getMimeType()
	{
		return _mimeType;
	}

	public String getRequestor()
	{
		return _requestor;
	}

	/**
	 * Returns the stream ID that uniquely identifies this file transfer.
	 * 
	 * @return Returns the stream ID that uniquely identifies this file
	 *         transfer.
	 */
	public String getStreamID()
	{
		return _streamID;
	}

	@Override
    public void writeToParcel(Parcel dest, int flags)
    {
    	dest.writeString(_streamID);
    	dest.writeString(_fileName);
    	dest.writeLong(_fileSize);
    	dest.writeString(_description);
    	dest.writeString(_mimeType);
    	dest.writeString(_requestor);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Parcelable.Creator<FileTransferRequestParcel> CREATOR = new Parcelable.Creator<FileTransferRequestParcel>() {
        public FileTransferRequestParcel createFromParcel(Parcel source) {
            return new FileTransferRequestParcel(source);
        }

        public FileTransferRequestParcel[] newArray(int size) {
            return new FileTransferRequestParcel[size];
        }
    };
}
