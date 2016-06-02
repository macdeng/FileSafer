package com.macdeng;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.spec.SecretKeySpec;
import javax.swing.JFrame;

public class FileSafer extends JFrame{
	private static final long serialVersionUID = -5058244288466680808L;
	private String destinationPath1 = "./encryptAES/";
	private String destinationPath2 = "./decryptAES/";
	private MenuBar menuBar;
	private Menu encrypt, decrypt, generateKey, fileTransfer,about;
	private MenuItem loadPublicKey, loadFile1, processEncryptRSA,processEncryptAES, loadPrivateKey,
			loadFile2, processDecryptRSA,processDecryptAES, processGenerateKey,startServer,stopServer,
			configServerAddress,connectServer,disconnectServer,
			sendFile,author, help;
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private int screenWidth = screenSize.width;
	private int screenHeight = screenSize.height;
	public static float processValue=0.0f;
	public static TextArea text=new TextArea();
	public static boolean serverState=false;
	public static TCPClient client=new TCPClient();
	public static final String CONFIG_FILE = "./config/config.txt";
	public static boolean secureTransfer=true;
	private Timer heartBeat;
	public FileSafer() {
		File config = new File(CONFIG_FILE);
		if(config.exists()){
			try {
				List<String> IP=FileUtil.readTextFileByLines(CONFIG_FILE);
				if(IP!=null&&IP.size()>0){
					TCPClient.defaultIP=IP.get(0);
					TCPClient.remoteServerIP=IP.get(0);
				}
			} catch (Exception e) {
			}
		}
		Container container=this.getContentPane();
		container.setLayout(new BorderLayout());
		container.add(text,BorderLayout.CENTER);
		text.setEditable(false);
		text.setFocusable(false);
		menuBar = new MenuBar();
		encrypt = new Menu("Encrypt");
		decrypt = new Menu("Decrypt");
		generateKey = new Menu("Generate Key");
		fileTransfer=new Menu("Secure Transfer");
		about = new Menu("About");
		loadPublicKey = new MenuItem("1.Load public key");
		loadPublicKey.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				RSAUtil.PUBLIC_KEY_FILE = CommonUtil.loadFile(new Frame(), "Load Public Key...",
						".\\", "*.key");
				text.append("Public key has been loaded:" + RSAUtil.PUBLIC_KEY_FILE+"\n\n");
			}

		});
		loadFile1 = new MenuItem("2.Load file");
		loadFile1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				RSAUtil.PLAIN_FILE = CommonUtil.loadFile(new Frame(), "Load plain file...", ".\\",
						"*.*");
				text.append("Plain file has been loaded:" + RSAUtil.PLAIN_FILE+"\n\n");
			}

		});
		processEncryptRSA = new MenuItem("3.Process(RSA)");
		processEncryptRSA.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				File file = new File(RSAUtil.PLAIN_FILE);
				FileInputStream fis = null;
				byte[] bytes = null;
				try {
					fis = new FileInputStream(file);
					bytes = new byte[(int) file.length()];
					fis.read(bytes);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					// close the streams using close method
					try {
						if (fis != null) {
							fis.close();
						}
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
				
				ObjectInputStream inputStream = null;
				PublicKey publicKey=null;
				// Encrypt the string using the public key
				try {
					inputStream = new ObjectInputStream(new FileInputStream(
							RSAUtil.PUBLIC_KEY_FILE));
					publicKey = (PublicKey) inputStream.readObject();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				finally{
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				TaskHolder.rsaTask.bytes=bytes;
				TaskHolder.rsaTask.type=1;
				TaskHolder.rsaTask.publicKey=publicKey;
				TaskHolder.executor.submit(TaskHolder.rsaTask);
			}

		});
		processEncryptAES = new MenuItem("3.Process(AES+RSA)");
		processEncryptAES.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				FileEvent fileEvent=new FileEvent();
				String fileName = RSAUtil.PLAIN_FILE.substring(
						Math.max(RSAUtil.PLAIN_FILE.lastIndexOf("/"), RSAUtil.PLAIN_FILE.lastIndexOf("\\")) + 1, RSAUtil.PLAIN_FILE.length());
				fileEvent.setDestinationDirectory(destinationPath1);
				fileEvent.setFileName(fileName);
				File file = new File(RSAUtil.PLAIN_FILE);
				//write AES key and file to fileEvent object
				DataInputStream diStream=null;
				if (file.isFile()) {
					try {
						diStream = new DataInputStream(
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
						text.append("file lenth: "+fileLen+" bytes\n");
						text.append("key lenth: "+keyLen+" bytes\n");
						text.append("total lenth: "+fileBytes.length+" bytes\n");
						fileEvent.setFileData(fileBytes);
						fileEvent.setStatus("Success");
					} catch (FileNotFoundException e2) {
						e2.printStackTrace();
					} catch (IOException e2) {
						e2.printStackTrace();
					}finally{
						try {
							diStream.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} else {
					FileSafer.text.append("path specified is not pointing a file!\n\n");
					fileEvent.setStatus("Error");
				}
				//output fileEvent object
				FileOutputStream fos = null;
				String outputFile=fileEvent.getDestinationDirectory()+fileEvent.getFileName();
				File secretFile=new File(outputFile);
				try {
					
					byte[] bytesEn=BytesUtil.serialize(fileEvent);
					if (secretFile.getParentFile() != null) {
						secretFile.getParentFile().mkdirs();
					}
					secretFile.createNewFile();
					fos=new FileOutputStream(secretFile);
					fos.write(bytesEn);
					fos.flush();
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

		});
		loadPrivateKey = new MenuItem("1.Load private key");
		loadPrivateKey.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				RSAUtil.PRIVATE_KEY_FILE = CommonUtil.loadFile(new Frame(), "Load Private Key...",
						".\\", "*.key");
				text.append("Private key has been loaded:" + RSAUtil.PRIVATE_KEY_FILE+"\n\n");

			}

		});
		loadFile2 = new MenuItem("2.Load file");
		loadFile2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				RSAUtil.SECRET_FILE = CommonUtil.loadFile(new Frame(), "Load secret file...", ".\\","*.zip");
				text.append("Cipher file has been loaded:" + RSAUtil.SECRET_FILE+"\n\n");
			}

		});
		processDecryptRSA = new MenuItem("3.Process(RSA)");
		processDecryptRSA.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				File file = new File(RSAUtil.SECRET_FILE);
				FileInputStream fis = null;
				byte[] bytes = null;
				try {
					fis = new FileInputStream(file);
					bytes = new byte[(int) file.length()];
					fis.read(bytes);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					// close the streams using close method
					try {
						if (fis != null) {
							fis.close();
						}
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
				
				ObjectInputStream inputStream = null;
				PrivateKey privateKey=null;
				// decrypt the string using the private key
				try {
					inputStream = new ObjectInputStream(new FileInputStream(
							RSAUtil.PRIVATE_KEY_FILE));
					privateKey = (PrivateKey) inputStream.readObject();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				finally{
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				TaskHolder.rsaTask.bytes=bytes;
				TaskHolder.rsaTask.type=2;
				TaskHolder.rsaTask.privateKey=privateKey;
				TaskHolder.executor.submit(TaskHolder.rsaTask);
			}

		});
		processDecryptAES = new MenuItem("3.Process(AES+RSA)");
		processDecryptAES.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				File file = new File(RSAUtil.SECRET_FILE);
				FileInputStream fis = null;
				FileEvent fileEvent=null;
				byte[] bytes = null;
				try {
					fis = new FileInputStream(file);
					bytes = new byte[(int) file.length()];
					fis.read(bytes);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					// close the streams using close method
					try {
						if (fis != null) {
							fis.close();
						}
						fileEvent=(FileEvent)BytesUtil.deserialize(bytes);
					} catch (IOException ioe) {
						ioe.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
				if(fileEvent.getStatus().equalsIgnoreCase("Error")){
					FileSafer.text.append("Error occurred .. so exiting\n\n");
				}
				String outputFile=destinationPath2+fileEvent.getFileName();
				File dstFile=new File(outputFile);
				if (dstFile.getParentFile() != null) {
					dstFile.getParentFile().mkdirs();
				}
				try {
					dstFile.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				FileOutputStream fileOutputStream=null;
				try {
					fileOutputStream = new FileOutputStream(dstFile);
					byte[] fileData=fileEvent.getFileData();
					int totalLen=fileData.length;
					text.append("totalLen: "+totalLen+"\n");
					int fileLen=(int)fileEvent.getFileSize();
					text.append("file lenth: "+fileLen+" bytes\n");
					text.append("key lenth: "+fileEvent.getKeyLength()+" bytes\n");
					text.append("total lenth: "+totalLen+" bytes\n");
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
					fileOutputStream.write(fileData);
					fileOutputStream.flush();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}finally{
					try {
						fileOutputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				FileSafer.text.append("File :"+dstFile.getAbsolutePath()+" is successfully saved.\n\n");
			}

		});
		encrypt.add(loadPublicKey);
		encrypt.add(loadFile1);
		encrypt.add(processEncryptRSA);
		encrypt.add(processEncryptAES);
		menuBar.add(encrypt);
		decrypt.add(loadPrivateKey);
		decrypt.add(loadFile2);
		decrypt.add(processDecryptRSA);
		decrypt.add(processDecryptAES);
		menuBar.add(decrypt);
		processGenerateKey = new MenuItem("Process");
		processGenerateKey.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				RSAUtil.generateKey();
				text.append("Public key and private key have been generated!\n\n");
			}

		});
		generateKey.add(processGenerateKey);
		menuBar.add(generateKey);
		author = new MenuItem("Author");
		author.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				text.setText("");
				text.append("***************\nAuthor: Macdeng\nE-mail:macdeng@126.com\nTel:13302963934\n***************\n\n");
			}

		});
		stopServer=new MenuItem("Stop Server");
		stopServer.setEnabled(false);
		stopServer.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				if(serverState){
					serverState=false;
					startServer.setEnabled(true);
					stopServer.setEnabled(false);
					TaskHolder.serverTaskHandler.cancel(true);
					try {
						TaskHolder.server.serverSocket.close();//release the port
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					text.append("Local server has been shut down successfully!\n\n");
				}
				
			}
			
		});
		startServer=new MenuItem("Start Server");
		startServer.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!serverState){
					serverState=true;
					startServer.setEnabled(false);
					stopServer.setEnabled(true);
					text.append("Local server has been started successfully!\n\n");
					TaskHolder.serverTask.type=3;
					TaskHolder.serverTaskHandler=TaskHolder.executor.submit(TaskHolder.serverTask);
				}
			}
			
		});
		disconnectServer=new MenuItem("Disconnect from Server");
		disconnectServer.setEnabled(false);
		disconnectServer.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				SocketAddress remote=client.socket.getRemoteSocketAddress();
				try {
					client.socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				connectServer.setEnabled(true);
				disconnectServer.setEnabled(false);
				sendFile.setEnabled(false);
				TCPClient.clientState=false;
				text.append("disconnect from "+remote+" successfully!\n");
				
			}
			
		});
		connectServer=new MenuItem("Connect to Server");
		connectServer.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(client.connect()){
					connectServer.setEnabled(false);
					disconnectServer.setEnabled(true);
					sendFile.setEnabled(true);
					text.append("connect to "+client.socket.getRemoteSocketAddress()+" successfully!\n\n");
				}
				else{
					text.append("can't connect to server!\n\n");
				}
				
			}
			
		});
		sendFile=new MenuItem("Send File(secure)");
		sendFile.setEnabled(false);
		sendFile.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				TaskHolder.sendFileTask.type=4;
				TaskHolder.sendFileTaskHandler=TaskHolder.executor.submit(TaskHolder.sendFileTask);
			}
			
		});
		configServerAddress=new MenuItem("Config Remote Server IP");
		configServerAddress.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				TCPClient.remoteServerIP=CommonUtil.showInputDialog("Please input remote server's IP:",TCPClient.defaultIP);
				if(TCPClient.remoteServerIP==null){
					TCPClient.remoteServerIP=TCPClient.defaultIP;
				}
				else{
					TCPClient.defaultIP=TCPClient.remoteServerIP;
					text.append("Config remote server address to: "+TCPClient.remoteServerIP+" successfully!\n\n");
					File config = new File(CONFIG_FILE);
					if (config.getParentFile() != null) {
						config.getParentFile().mkdirs();
					}
					try {
						config.createNewFile();
						FileUtil.writeToTextFile(CONFIG_FILE, TCPClient.remoteServerIP);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
			
		});
		fileTransfer.add(startServer);
		fileTransfer.add(stopServer);
		fileTransfer.add(configServerAddress);
		fileTransfer.add(connectServer);
		fileTransfer.add(disconnectServer);
		fileTransfer.add(sendFile);
		menuBar.add(fileTransfer);
		help = new MenuItem("Help");
		help.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				text.setText("");
				text.append("**********Instructions:**********\n");
				text.append("steps for encrypt:\n");
				text.append("1.load public key file.\n");
				text.append("2.load plain file.\n");
				text.append("3.process\n\n");
				text.append("steps for decrypt:\n");
				text.append("1.load private key file.\n");
				text.append("2.load cipher file.\n");
				text.append("3.process\n\n");
				text.append("steps for file transfer:\n");
				text.append("1.your partener should start server first.\n");
				text.append("2.config remote server IP.\n");
				text.append("3.connect to server.\n");
				text.append("4.send file(this step is repeatable).\n");
				text.append("**********************************\n\n");
				
			}

		});
		about.add(author);
		about.add(help);
		menuBar.add(about);
		this.setMenuBar(menuBar);
		this.setTitle("FileSafer");
		this.setSize(600, 600);
		int x = (screenWidth - getWidth()) / 2;
		int y = (screenHeight - getHeight()) / 2;
		setLocation(x, y);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		this.heartBeat();
	}
	private void heartBeat(){
		heartBeat=new Timer();
		heartBeat.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run(){
				if (!serverState){
					startServer.setEnabled(true);
					stopServer.setEnabled(false);
				}
				if(!TCPClient.clientState){
					connectServer.setEnabled(true);
					disconnectServer.setEnabled(false);
					sendFile.setEnabled(false);
				}
			}
		}, 1000, 5000);
	}
	public static void main(String args[]) {
		new FileSafer();
	}
}
