package com.macdeng;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Timer;
import java.util.TimerTask;
import javax.crypto.Cipher;
import org.apache.commons.lang.ArrayUtils;


public class RSAUtil{
	private static int elapsedSeconds=0;
	private static Timer timer;
	public static final String ALGORITHM = "RSA";
	public static String PUBLIC_KEY_FILE="./keys/public.key";
	public static String PLAIN_FILE;
	public static String PRIVATE_KEY_FILE="./keys/private.key";
	public static String SECRET_FILE;

	public static void generateKey() {
		try {
			final KeyPairGenerator keyGen = KeyPairGenerator
					.getInstance(ALGORITHM);
			keyGen.initialize(1024);
			final KeyPair key = keyGen.generateKeyPair();

			File privateKeyFile = new File(PRIVATE_KEY_FILE);
			File publicKeyFile = new File(PUBLIC_KEY_FILE);

			// Create files to store public and private key
			if (privateKeyFile.getParentFile() != null) {
				privateKeyFile.getParentFile().mkdirs();
			}
			privateKeyFile.createNewFile();

			if (publicKeyFile.getParentFile() != null) {
				publicKeyFile.getParentFile().mkdirs();
			}
			publicKeyFile.createNewFile();

			// Saving the Public key in a file
			ObjectOutputStream publicKeyOS = new ObjectOutputStream(
					new FileOutputStream(publicKeyFile));
			publicKeyOS.writeObject(key.getPublic());
			publicKeyOS.close();

			// Saving the Private key in a file
			ObjectOutputStream privateKeyOS = new ObjectOutputStream(
					new FileOutputStream(privateKeyFile));
			privateKeyOS.writeObject(key.getPrivate());
			privateKeyOS.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static boolean areKeysPresent() {

		File privateKey = new File(PRIVATE_KEY_FILE);
		File publicKey = new File(PUBLIC_KEY_FILE);

		if (privateKey.exists() && publicKey.exists()) {
			return true;
		}
		return false;
	}
	public static byte[] encrypt(byte[] bytes, PublicKey key) {
		FileSafer.text.append("encrypt percentage: "+(int)FileSafer.processValue+"%");
		byte[] cipherText = null;
		try {
			// get an RSA cipher object and print the provider
			final Cipher cipher = Cipher.getInstance(ALGORITHM);
			// encrypt the plain text using the public key
			cipher.init(Cipher.ENCRYPT_MODE, key);
			StringBuilder sb = new StringBuilder();
			int length=bytes.length;
			int estimateTime=0;
			startTimer();
			for (int i = 0; i < length; i += 100) {
				byte[] doFinal = cipher.doFinal(ArrayUtils.subarray(bytes, i,
						i + 100));
				sb.append(new String(doFinal));
				cipherText = ArrayUtils.addAll(cipherText, doFinal);
				FileSafer.processValue+=10000.0f/(float)length;
				String temp=FileSafer.text.getText();
				temp=temp.substring(0, temp.lastIndexOf("encrypt percentage: ")+20);
				FileSafer.text.setText(temp);
				if(i==0){
					estimateTime=0;
				}
				else{
					estimateTime=(int)((float)length*(float)elapsedSeconds/(float)i)-elapsedSeconds;
				}
				if(estimateTime<0){
					estimateTime=0;
				}
				FileSafer.text.append(Math.min(FileSafer.processValue, 100)+"%"+
				"    Estimate remain time: "+estimateTime+" seconds"+ 
				"    Elapsed time: "+elapsedSeconds+" seconds"		);
			}
			timer.cancel();
			FileSafer.text.append("\n\n");
			FileSafer.processValue=0.0f;
			elapsedSeconds=0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cipherText;
	}
	public static byte[] decrypt(byte[] text, PrivateKey key) {
		FileSafer.text.append("decrypt percentage: "+(int)FileSafer.processValue+"%");
		byte[] dectyptedText = null;
		try {
			// get an RSA cipher object and print the provider
			final Cipher cipher = Cipher.getInstance(ALGORITHM);

			// decrypt the text using the private key
			cipher.init(Cipher.DECRYPT_MODE, key);
			//StringBuilder sb = new StringBuilder();
			int length=text.length;
			int estimateTime=0;
			startTimer();
			for (int i = 0; i < text.length; i += 128) {
				byte[] doFinal = cipher.doFinal(ArrayUtils.subarray(text, i,
						i + 128));
				//sb.append(new String(doFinal));
				dectyptedText = ArrayUtils.addAll(dectyptedText, doFinal);
				FileSafer.processValue+=12800.0f/(float)length;
				String temp=FileSafer.text.getText();
				temp=temp.substring(0, temp.lastIndexOf("decrypt percentage: ")+20);
				FileSafer.text.setText(temp);
				if(i==0){
					estimateTime=0;
				}
				else{
					estimateTime=(int)((float)length*(float)elapsedSeconds/(float)i)-elapsedSeconds;
				}
				if(estimateTime<0){
					estimateTime=0;
				}
				FileSafer.text.append(Math.min(FileSafer.processValue, 100)+"%"+
				"    Estimate remain time: "+estimateTime+" seconds"+ 
				"    Elapsed time: "+elapsedSeconds+" seconds"		);
			}
			timer.cancel();
			FileSafer.text.append("\n\n");
			FileSafer.processValue=0.0f;
			elapsedSeconds=0;

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return dectyptedText;
	}
	public static void startTimer(){
		timer=new Timer();
		timer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run(){
				elapsedSeconds+=1;
			}
		}, 1000, 1000);
	}
	public static void stopTimer(){
		timer.cancel();
	}
}