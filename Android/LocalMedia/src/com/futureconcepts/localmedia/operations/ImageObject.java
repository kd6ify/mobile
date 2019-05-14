package com.futureconcepts.localmedia.operations;

import java.util.ArrayList;


public class ImageObject {
	
	public static  ArrayList<ImageObject> images = new ArrayList<ImageObject>();
	private String ID;
	private long bytesStored;
	private long fileSize;
	private String filePath;
	private String creationDate;
	private String action;
	private String status;
	
	public ImageObject(String iD, long bytesStored, long fileSize,String imagePath, String creationDate, String action, String status) {
		super();
		ID = iD;
		this.bytesStored = bytesStored;
		this.fileSize = fileSize;
		this.filePath = imagePath;
		this.creationDate = creationDate;
		this.action = action;
		this.status = status;
	}
	
	/**
	 * @return the iD
	 */
	public String getID() {
		return ID;
	}
	/**
	 * @param iD the iD to set
	 */
	public void setID(String iD) {
		ID = iD;
	}
	/**
	 * @return the bytesStored
	 */
	public long getBytesStored() {
		return bytesStored;
	}
	/**
	 * @param bytesStored the bytesStored to set
	 */
	public void setBytesStored(long bytesStored) {
		this.bytesStored = bytesStored;
	}
	/**
	 * @return the fileSize
	 */
	public long getFileSize() {
		return fileSize;
	}
	/**
	 * @param fileSize the fileSize to set
	 */
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	/**
	 * @return the imagePath
	 */
	public String getImagePath() {
		return filePath;
	}
	/**
	 * @param imagePath the imagePath to set
	 */
	public void setImagePath(String imagePath) {
		this.filePath = imagePath;
	}
	/**
	 * @return the creationDate
	 */
	public String getCreationDate() {
		return creationDate;
	}
	/**
	 * @param creationDate the creationDate to set
	 */
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}
	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}
	/**
	 * @param action the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	

}
