package com.macdeng;

import java.io.Serializable;

public class FileEvent implements Serializable{

	public FileEvent(){
		
	}
	private static final long serialVersionUID = -3691846171952547947L;
	private String destinationDirectory;
	private String sourceDiretory;
	private String fileName;
	private long fileLen;
	private int keyLen;
	private byte[] fileData;
	private String status;
	
	public int getKeyLength() {
		return keyLen;
	}
	public void setKeyLength(int keyLength) {
		this.keyLen = keyLength;
	}
	public String getDestinationDirectory() {
		return destinationDirectory;
	}
	public void setDestinationDirectory(String destinationDirectory) {
		this.destinationDirectory = destinationDirectory;
	}
	public String getSourceDiretory() {
		return sourceDiretory;
	}
	public void setSourceDiretory(String sourceDiretory) {
		this.sourceDiretory = sourceDiretory;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public long getFileSize() {
		return fileLen;
	}
	public void setFileSize(long fileSize) {
		this.fileLen = fileSize;
	}
	public byte[] getFileData() {
		return fileData;
	}
	public void setFileData(byte[] fileData) {
		this.fileData = fileData;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
}
