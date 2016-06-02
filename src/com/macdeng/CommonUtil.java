package com.macdeng;

import java.awt.FileDialog;
import java.awt.Frame;
import java.math.BigInteger;
import java.security.SecureRandom;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class CommonUtil {
	@SuppressWarnings("deprecation")
	public static String loadFile(Frame f, String title, String defDir,
			String fileType) {
		FileDialog fd = new FileDialog(f, title, FileDialog.LOAD);
		fd.setFile(fileType);
		fd.setDirectory(defDir);
		fd.setLocation(50, 50);
		fd.show();
		return fd.getDirectory() + fd.getFile();
	}

//	@SuppressWarnings("deprecation")
//	public static String saveFile(Frame f, String title, String defDir,
//			String fileType) {
//		FileDialog fd = new FileDialog(f, title, FileDialog.SAVE);
//		fd.setFile(fileType);
//		fd.setDirectory(defDir);
//		fd.setLocation(50, 50);
//		fd.show();
//		return fd.getDirectory() + fd.getFile();
//	}
	public static String showInputDialog(String info,String defaultIP){
		JFrame frame=new JFrame();
		String ip=JOptionPane.showInputDialog(frame,info,defaultIP);
		return ip;
	}
	public static String generateRandomString(){
		SecureRandom random = new SecureRandom();
		return new BigInteger(130, random).toString(32);
	}
}
