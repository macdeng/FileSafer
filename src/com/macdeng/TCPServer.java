package com.macdeng;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

public class TCPServer {
	public ServerSocket serverSocket = null;
	public Socket socket = null;
	private ObjectInputStream inputStream = null;
	private FileEvent fileEvent;
	private File dstFile = null;
	private FileOutputStream fileOutputStream = null;

	public TCPServer() {

	}

	public void doConnect() throws IOException{
		serverSocket = new ServerSocket(12345);
		socket = serverSocket.accept();
//		text.append("connect to "+client.socket.getRemoteSocketAddress()+" successfully!\n\n");
		FileSafer.text.append("client: "+ socket.getRemoteSocketAddress()+ "connected!\n\n");
		inputStream = new ObjectInputStream(socket.getInputStream());
	}
	public void downloadFile() throws ClassNotFoundException, IOException{
		FileSafer.text.append("waiting for receiving file\n\n");
		fileEvent=(FileEvent) inputStream.readObject();
		if(fileEvent.getStatus().equalsIgnoreCase("Error")){
			FileSafer.text.append("Error occurred .. so exiting\n\n");
		}
		String outputFile=fileEvent.getDestinationDirectory()+fileEvent.getFileName();
		if(!new File(fileEvent.getDestinationDirectory()).exists()){
			new File(fileEvent.getDestinationDirectory()).mkdirs();
		}
		dstFile=new File(outputFile);
		fileOutputStream = new FileOutputStream(dstFile);
		byte[] fileData=fileEvent.getFileData();
		int totalLen=fileData.length;
		int fileLen=(int)fileEvent.getFileSize();
		if(FileSafer.secureTransfer){
			byte[] key=null;
			byte[] fileBytes=null;
			key=Arrays.copyOfRange(fileData, fileLen, totalLen);
			fileBytes=Arrays.copyOfRange(fileData, 0, fileLen);
			PrivateKey privateKey=null;
			if(RSAUtil.areKeysPresent()){
				privateKey=(PrivateKey)ObjectUtil.file2OBject(RSAUtil.PRIVATE_KEY_FILE);
			}
			key=RSAUtil.decrypt(key, privateKey);//decrypt AES key
			SecretKeySpec AESkey=(SecretKeySpec) BytesUtil.deserialize(key);//deserialize AES key
			FileSafer.text.append("AES key has been decrypted using RSA algorithm!\n\n");
			fileData=AESUtil.decrypt(fileBytes, AESkey);
			FileSafer.text.append("Well done! file has been decrypted using AES algorithm!\n\n");
		}
		fileOutputStream.write(fileData);
		fileOutputStream.flush();
		fileOutputStream.close();
		FileSafer.text.append("File :"+dstFile.getAbsolutePath()+" is successfully saved.\n\n");
	}
}
