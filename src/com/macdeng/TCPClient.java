package com.macdeng;

import java.awt.Frame;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.spec.SecretKeySpec;

public class TCPClient {
	public Socket socket = null;
	private ObjectOutputStream outputStream = null;
	private String sourceFilePath = "";
	private FileEvent fileEvent = null;
	private String destinationPath = "./received_file/";
	private static int elapsedSeconds;
	public static boolean clientState=false;
	public static  String remoteServerIP="127.0.0.1";
	public static String defaultIP="";	
	public TCPClient() {

	}

	public boolean connect() {
		if (!clientState) {
			try {
				socket = new Socket(remoteServerIP, 12345);
				outputStream = new ObjectOutputStream(socket.getOutputStream());
				clientState = true;
				return true;
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@SuppressWarnings("resource")
	public void sendFile() {
		if(!socket.isClosed()){
			sourceFilePath = CommonUtil.loadFile(new Frame(),
					"Please choose file...", ".\\", "*.*");
			FileSafer.text.append("Load file: " + sourceFilePath
					+ ", be patient!\n\n");
			FileSafer.text.append("Sending file: " + sourceFilePath
					+ ", be patient!\n\n");
			fileEvent = new FileEvent();
			String fileName = sourceFilePath.substring(
					Math.max(sourceFilePath.lastIndexOf("/"), sourceFilePath.lastIndexOf("\\")) + 1, sourceFilePath.length());
			fileEvent.setDestinationDirectory(destinationPath);
			fileEvent.setFileName(fileName);
			File file = new File(sourceFilePath);
			if (file.isFile()) {
				try {
					DataInputStream diStream = new DataInputStream(
							new FileInputStream(file));
					int len = (int)file.length();
					byte[] fileBytes = new byte[(int) len];
					int read = 0;
					int numRead = 0;
					while (read < fileBytes.length
							&& (numRead = diStream.read(fileBytes, read,
									fileBytes.length - read)) >= 0) {
						read = read + numRead;
					}
					if(FileSafer.secureTransfer){
						SecretKeySpec key=AESUtil.generateKey();
						byte[] fileBytesE=AESUtil.encrypt(fileBytes, key);
						FileSafer.text.append("File has been encrypted using AES algorithm.\n\n");
						PublicKey publicKey=null;
						if(RSAUtil.areKeysPresent()){
							publicKey=(PublicKey) ObjectUtil.file2OBject(RSAUtil.PUBLIC_KEY_FILE);
						}
						byte[] keyE=RSAUtil.encrypt(BytesUtil.serialize(key), publicKey);
						FileSafer.text.append("AES key has been encrypted using RSA algorithm.\n\n");
						int keyLen=keyE.length;
						int fileLen=fileBytesE.length;
						fileEvent.setFileSize(fileLen);
						fileEvent.setKeyLength(keyLen);
						fileBytes=BytesUtil.combine(fileBytesE, keyE);
					}
					fileEvent.setFileData(fileBytes);
					fileEvent.setStatus("Success");
				} catch (FileNotFoundException e2) {
					e2.printStackTrace();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			} else {
				FileSafer.text.append("path specified is not pointing a file!\n\n");
				fileEvent.setStatus("Error");
			}
			try {
				TaskHolder.clientTimer=new Timer();
				TaskHolder.clientTimerTask.type=5;
				TaskHolder.clientTimerTask.info="Sending file...please wait...  time elapsed: ";
				TaskHolder.clientTimerTaskHandler=TaskHolder.executor.submit(TaskHolder.clientTimerTask);
				outputStream.writeObject(fileEvent);
				TaskHolder.clientTimerTaskHandler.cancel(true);
				TaskHolder.clientTimer.cancel();
				TaskHolder.clientTimer.purge();
				elapsedSeconds=0;
				FileSafer.text.append("Send file: " + sourceFilePath
						+ " successfully!\n\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			FileSafer.text.append("lose the connection with remote server!\n\n");
			clientState=false;
		}
		
	}
	public static void startTimer(String info,Timer timer){
		FileSafer.text.append(info+elapsedSeconds+" seconds.\n\n");
		final String info1=info;
		final int length=info.length();
		timer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run(){
				elapsedSeconds+=1;
				String temp=FileSafer.text.getText();
				temp=temp.substring(0, temp.lastIndexOf(info1)+length);
				FileSafer.text.setText(temp);
				FileSafer.text.append(elapsedSeconds+" seconds.\n\n");
			}
		}, 1000, 1000);
	}
}
