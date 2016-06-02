package com.macdeng;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Timer;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class TaskHolder implements Callable<Integer>{

	public byte[] bytes;//transfer value
	public PublicKey publicKey;
	public PrivateKey privateKey;
	public int type=0;//1:encrypt 2:decrypt 3:start server 4:send file 5:timer
	private static final String ENCRYPTED_FILE="./encrypted/encrypted_file.zip";
	private static final String DECRYPTED_FILE="./decrypted/decrypted_file.macdeng";
	public String info;
	public static TaskHolder sendFileTask=new TaskHolder();
	public static Future<Integer> sendFileTaskHandler;
	
	public static Timer clientTimer=new Timer();
	public static TaskHolder clientTimerTask=new TaskHolder();
	public static Future<Integer> clientTimerTaskHandler=null;
	
	public static TaskHolder rsaTask=new TaskHolder();
	
	public static TCPServer server=new TCPServer();
	public static TaskHolder serverTask=new TaskHolder();
	public static Future<Integer> serverTaskHandler=null;
	
	public static ThreadPoolExecutor executor=(ThreadPoolExecutor)Executors.newFixedThreadPool(10);

	@Override
	public Integer call() throws Exception {
		Integer ret=null;
		switch(type){
		case 1:
			bytes=RSAUtil.encrypt(bytes, publicKey);
			break;
		case 2:
			bytes=RSAUtil.decrypt(bytes, privateKey);
			break;
		case 3:
			startServer();
			break;
		case 4:
			sendFile();
			break;
		case 5:
			startTimer();
			break;
		}
		callback();//use callback function here, so screen text can be updated to show progress.
		return ret;
	}
	
	public void callback(){
		switch(type){
		case 1:
			finishEncrypt();
			break;
		case 2:
			finishDecrypt();
			break;
		case 3:
			break;
		case 4:
			finishSendFile();
			break;
		case 5:
			break;
		}
	}
	private void startTimer(){
		TCPClient.startTimer(info, clientTimer);
	}
	private void sendFile(){
		FileSafer.client.sendFile();
	}
	private void startServer(){
		try {
			TaskHolder.server.doConnect();
		} catch (IOException e1) {
			//e1.printStackTrace();
			FileSafer.serverState=false;
		}
		while(FileSafer.serverState){
			try {
				TaskHolder.server.downloadFile();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				//e.printStackTrace();
				FileSafer.serverState=false;
			}
		}
	}
	private void finishSendFile(){
		sendFileTaskHandler.cancel(true);
	}
	private void finishEncrypt(){
		//byte[] secretBytes=null;
		File secretFile=new File(ENCRYPTED_FILE);
		// Create *.zip file to store encrypted file
		if (secretFile.getParentFile() != null) {
			secretFile.getParentFile().mkdirs();
		}
		try {
			secretFile.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		FileOutputStream fos = null;
		try {
			fos=new FileOutputStream(secretFile);
			//fos.write(secretBytes);
			fos.write(bytes);
			fos.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileSafer.text.append("Plain file has been encrypted:"+secretFile.getAbsolutePath()+"\n\n");
	}
	private void finishDecrypt(){
		File decryptedFile=new File(DECRYPTED_FILE);
		// Create destination file to store decrypted file
		if (decryptedFile.getParentFile() != null) {
			decryptedFile.getParentFile().mkdirs();
		}
		try {
			decryptedFile.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		FileOutputStream fos = null;
		try {
			fos=new FileOutputStream(decryptedFile);
			fos.write(bytes);
			fos.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileSafer.text.append("Cipher file has been decrypted:"+decryptedFile.getAbsolutePath()+"\n\n");
	}
}
