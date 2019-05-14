package com.futureconcepts.ax.trinity.logs.images;

public class EntryImageObject {
	 
	private String ID;
	private String imagePath;
	private String bytesSent;
	private int fileSize =0;
	private String journalEntryID;
	public int needDownload = 0;
	
	public EntryImageObject(String id, String path, int needDownload, String journalEntryID)
	{
		ID= id;
		imagePath=path;
		this.needDownload = needDownload;
		this.journalEntryID = journalEntryID;		
	}
	public EntryImageObject(String id,String bytesSent,String path)
	{
		ID=id;
		imagePath=path;
		this.bytesSent = bytesSent;
	}	
	public EntryImageObject(String id,String path,int download,int fileSize, String journalEntryID)
	{
		ID=id;
		imagePath=path;
		needDownload = download;
		this.fileSize = fileSize;
		this.journalEntryID = journalEntryID;
	}
	
	public void setID(String id)
	{
		ID = id;
	}
	public String getID()
	{
		return ID;
	}
	public void setImagePath(String path)
	{
		imagePath = path;
	}
	public String getImagePath()
	{
		return imagePath;
	}
	
	public int getBytesSent() {
		return Integer.parseInt(bytesSent);
	}
	public void setBytesSent(String bytesSent) {
		this.bytesSent = bytesSent;
	}
	/**
	 * @return the fileSize
	 */
	public int getFileSize() {
		return fileSize;
	}
	/**
	 * @param fileSize the fileSize to set
	 */
	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}
	
	public int isNeedDownload() {
		return needDownload;
	}

	public void setNeedDownload(int needDownload) {
		this.needDownload = needDownload;
	}

	public String getJournalEntryID() {
		return journalEntryID;
	}
	public void setJournalEntryID(String journalEntryID) {
		this.journalEntryID = journalEntryID;
	}
 
}
