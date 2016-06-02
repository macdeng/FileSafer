package com.macdeng;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class FileUtil {
//	public static String readTextFile(String fileName) throws IOException {
//		String content = new String(Files.readAllBytes(Paths.get(fileName)));
//		return content;
//	}

	public static List<String> readTextFileByLines(String fileName)
			throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(fileName),Charset.defaultCharset());
		return lines;
	}

	public static void writeToTextFile(String fileName, String content)
			throws IOException {
		Files.write(Paths.get(fileName), content.getBytes(),
				StandardOpenOption.CREATE);
	}
}
